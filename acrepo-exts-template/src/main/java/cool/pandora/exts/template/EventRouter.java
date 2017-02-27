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
package cool.pandora.exts.template;

import static org.apache.camel.component.mustache.MustacheConstants.MUSTACHE_RESOURCE_URI;
import static org.apache.camel.Exchange.CONTENT_TYPE;
import static org.apache.camel.Exchange.HTTP_METHOD;
import static org.apache.camel.Exchange.HTTP_PATH;
import static org.apache.camel.Exchange.HTTP_QUERY;
import static org.apache.camel.Exchange.HTTP_URI;
import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.apache.camel.LoggingLevel.INFO;
import static org.apache.camel.model.dataformat.JsonLibrary.Jackson;
import static org.fcrepo.camel.FcrepoHeaders.FCREPO_IDENTIFIER;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;

/**
 * @author Aaron Coburn
 */
public class EventRouter extends RouteBuilder {

    private static final Logger LOGGER = getLogger(EventRouter.class);

    /**
     * Configure the message route workflow.
     */
    public void configure() throws Exception {

        /**
         * A generic error handler (specific to this RouteBuilder)
         */
        onException(Exception.class)
            .maximumRedeliveries("{{error.maxRedeliveries}}")
            .log("Event Routing Error: ${routeId}");

        from("jetty:http://{{rest.host}}:{{rest.port}}/template?" +
                "matchOnUriPrefix=true&httpMethodRestrict=GET,OPTIONS&sendServerVersion=false")
            .routeId("TemplateTransformation")
            .choice()
                .when(header(HTTP_METHOD).isEqualTo("GET"))
                    .process(e -> e.getIn().setHeader(FCREPO_IDENTIFIER,
                            e.getIn().getHeader("Apix-Ldp-Resource-Path",
                                    e.getIn().getHeader(HTTP_PATH))))
                    .log(INFO, LOGGER, "PATH: ${headers[CamelFcrepoIdentifier]}")
                    .removeHeader("breadcrumbId")
                    .removeHeader("Accept")
                    .removeHeader("User-Agent")
                    .to("direct:getFromFedora")
                .when(header(HTTP_METHOD).isEqualTo("OPTIONS"))
                    .setHeader(CONTENT_TYPE).constant("text/turtle")
                    .setHeader("Allow").constant("GET,OPTIONS")
                    .to("language:simple:resource:classpath:options.ttl");

        from("direct:getFromFedora")
            .routeId("FetchFromRepository")
            .removeHeaders("CamelHttp*")
            .setHeader(HTTP_URI).simple("{{ldpath.serviceUrl}}")
            .setHeader(HTTP_QUERY).simple("context={{fcrepo.baseUrl}}${headers[CamelFcrepoIdentifier]}")
            .setHeader(HTTP_METHOD).constant("GET")
            .to("http4://localhost")
            .filter(header(HTTP_RESPONSE_CODE).isEqualTo(200))
              .to("direct:template");


        from("direct:template")
            .routeId("TemplateRoute")
            .setHeader(CONTENT_TYPE).simple("{{mustache.contentType}}")
            .unmarshal().json(Jackson, List.class)
            .setBody(simple("${body[0]}"))
            .choice()
               .when(header("templateUri").isNull())
                   .to("direct:defaultTemplate")
               .otherwise()
                   .setHeader(MUSTACHE_RESOURCE_URI).simple("${headers.templateUri}")
                   /* the MUSTACHE_RESOURCE_URI in header overrides the passed in value below */
                   .to("mustache:dummy");

        from("direct:defaultTemplate")
            .routeId("DefaultTemplateRoute")
            .to("mustache:{{mustache.templateUri}}");
    }
}

