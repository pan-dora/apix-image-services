/*
 * Copyright 2016 Amherst College
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cool.pandora.exts.oai;

import static org.apache.camel.Exchange.*;
import static java.net.URLEncoder.encode;
import static java.util.stream.Collectors.toList;
import static org.apache.camel.Exchange.CONTENT_TYPE;
import static org.apache.camel.Exchange.HTTP_METHOD;
import static org.apache.camel.builder.PredicateBuilder.in;
import static org.apache.camel.builder.PredicateBuilder.not;
import static org.apache.camel.builder.PredicateBuilder.or;
import static org.apache.camel.util.ExchangeHelper.getMandatoryHeader;
import static org.fcrepo.camel.FcrepoHeaders.FCREPO_NAMED_GRAPH;
import static org.fcrepo.camel.FcrepoHeaders.FCREPO_EVENT_TYPE;
import static org.fcrepo.camel.FcrepoHeaders.FCREPO_URI;
import static org.fcrepo.camel.processor.ProcessorUtils.tokenizePropertyPlaceholder;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.NoSuchHeaderException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.jena.util.URIref;
import org.fcrepo.camel.processor.EventProcessor;
import org.slf4j.Logger;
import static org.fcrepo.camel.FcrepoHeaders.FCREPO_IDENTIFIER;
import static org.slf4j.LoggerFactory.getLogger;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;

/**
 * @author Christopher Johnson
 */
public class EventRouter extends RouteBuilder {
    private static final String FCREPO_URI = "http://fcrepo:8080/fcrepo/rest";
    private static final String HTTP_ACCEPT = "Accept";
    private static final Logger LOGGER = getLogger(EventRouter.class);
    /**
     * Configure the message route workflow.
     */
    public void configure() throws Exception {

        from("jetty:http://{{rest.host}}:{{rest.port}}{{rest.prefix}}?" +
                "optionsEnabled=true&matchOnUriPrefix=true&sendServerVersion=false&httpMethodRestrict=GET,OPTIONS")
                .routeId("SPARQLRouter")
                .process(e -> e.getIn().setHeader(FCREPO_IDENTIFIER,
                        e.getIn().getHeader("Apix-Ldp-Resource-Path",
                                e.getIn().getHeader(HTTP_PATH))))
                .removeHeaders(HTTP_ACCEPT)
                .choice()
                    .when(header(HTTP_METHOD).isEqualTo("GET"))
                        .to("direct:get")
                .when(header(HTTP_METHOD).isEqualTo("OPTIONS"))
                .log(LoggingLevel.INFO, "Test: ${headers[CamelHttpPath]}")
                .to("direct:options");
        from("direct:options")
                .routeId("XmlOptions")
                .setHeader(CONTENT_TYPE).constant("text/turtle")
                .setHeader("Allow").constant("GET,OPTIONS")
                .to("language:simple:resource:classpath:options.ttl");
        from("direct:get")
                .routeId("SparqlGet")
                .log(LoggingLevel.INFO, LOGGER, "Selecting Triplestore Object ${headers[CamelFcrepoUri]}")
                .setHeader(HTTP_METHOD).constant("POST")
                .setHeader(CONTENT_TYPE).constant("application/x-www-form-urlencoded; charset=utf-8")
                .process(e -> e.getIn().setBody(sparqlSelect(SelectAll(FCREPO_URI))))
                .to("{{triplestore.baseUrl}}?useSystemProperties=true&bridgeEndpoint=true")
               // .to("direct:getResource")
                .filter(header(HTTP_RESPONSE_CODE).isEqualTo(200))
                .setHeader(CONTENT_TYPE).constant("application/xml")
                .convertBodyTo(org.w3c.dom.Document.class);
        from("direct:getResource")
                .routeId("XmlTransformationCommon")
                .removeHeader("breadcrumbId")
                .removeHeader("Accept")
                .removeHeader("User-Agent")
                .to("fcrepo:localhost?throwExceptionOnFailure=false");
    }

    private static String sparqlSelect(final String command) {
        try {
            return "query=" + encode(command, "UTF-8");
        } catch (final IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
    private static String SelectAll(final String graphName) {
        return "SELECT * WHERE { GRAPH <" + graphName + "> { ?s ?p ?o } }";
    }

    private static String graph(final Exchange ex) throws NoSuchHeaderException {
        return URIref.encode(getMandatoryHeader(ex, FCREPO_URI, String.class));
    }
}
