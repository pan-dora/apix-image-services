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
package cool.pandora.exts.encoder;

import static java.net.URI.create;
import static java.util.stream.Collectors.toList;
import static org.apache.camel.Exchange.*;
import static org.apache.camel.Exchange.HTTP_URI;
import static org.apache.camel.LoggingLevel.INFO;
import static org.apache.camel.builder.PredicateBuilder.*;
import static org.fcrepo.camel.FcrepoHeaders.FCREPO_URI;
import static org.fcrepo.camel.processor.ProcessorUtils.tokenizePropertyPlaceholder;
import static org.slf4j.LoggerFactory.getLogger;

import org.apache.camel.Predicate;
import org.apache.camel.PropertyInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.xml.Namespaces;
import org.fcrepo.camel.processor.EventProcessor;
import org.slf4j.Logger;

import java.util.List;

/**
 * A content router for handling Fedora events.
 *
 * @author Aaron Coburn
 */
public class EventRouter extends RouteBuilder {

    private static final Logger LOGGER = getLogger(EventRouter.class);

    private static final String REPOSITORY = "http://fedora.info/definitions/v4/repository#";

    public static final String SERIALIZATION_PATH = "CamelSerializationPath";

    public final List<Predicate> uriFilter = tokenizePropertyPlaceholder(getContext(), "{{filter.containers}}", ",")
            .stream().map(uri -> or(
                    header(FCREPO_URI).startsWith(constant(uri + "/")),
                    header(FCREPO_URI).isEqualTo(constant(uri))))
            .collect(toList());
    
    @PropertyInject("{{fcrepo.collection.uri}}")
    public String COLLECTION_URI;
    
    /**
     * Configure the message route workflow.
     */
    public void configure() throws Exception {

        final Namespaces ns = new Namespaces("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
                .add("fedora", REPOSITORY);

        /*
         * A generic error handler (specific to this RouteBuilder)
         */
        onException(Exception.class)
                .maximumRedeliveries("{{error.maxRedeliveries}}")
                .log("Index Routing Error: ${routeId}");

        from("{{input.stream}}")
                .routeId("PandoraEncoder")
                .process(new EventProcessor())
                .process(exchange -> {
                    final String uri = exchange.getIn().getHeader(FCREPO_URI, "", String.class);
                    exchange.getIn().setHeader(SERIALIZATION_PATH, create(uri).getPath());
                })
                .filter(not(in(uriFilter)))
                .to("direct:get");

        from("{{reserialization.stream}}")
                .routeId("PandoraReEncoder")
                .filter(not(in(uriFilter)))
                .process(exchange -> {
                    final String uri = exchange.getIn().getHeader(FCREPO_URI, "", String.class);
                    exchange.getIn().setHeader(SERIALIZATION_PATH, create(uri).getPath());
                })
                .to("direct:get");

        from("direct:get")
                .routeId("ImageGet")
                .removeHeaders("CamelHttp*")
                // set up a request to request the headers of the resource
                .setHeader(HTTP_METHOD).constant("HEAD")
                .setHeader(HTTP_URI).header(FCREPO_URI)
                .to("http4://localhost")
                // keep only LDP-NRs with a content-type of image/tiff (
                .log(INFO, LOGGER, "Encoder Processing ${headers[CamelHttpUri]}")
                .filter(header(CONTENT_TYPE).isEqualTo("image/tiff"))
                .removeHeaders("CamelHttp*")
                // fetch the derivative image from the image service
                .setHeader(HTTP_METHOD).constant("GET")
                .setHeader(HTTP_URI).simple("{{image.server.uri}}")
                .process(ex -> {
                    // rewrite the path
                    final String uri = ex.getIn().getHeader(FCREPO_URI, String.class);
                    final String collection_replace = COLLECTION_URI + "(.+?)";
                    final String path = uri.replaceAll(collection_replace, "$1");
                    final String newpath = path.replaceAll("/", "_");
                    final String getpath= "iiif:" + newpath;
                    ex.getIn().setHeader(HTTP_PATH, getpath);
                })
                .log(INFO, LOGGER, "Encoder Replacing " + COLLECTION_URI + "in ${headers[CamelFcrepoUri]}")
                .log(INFO, LOGGER, "Encoder Requesting ${headers[CamelHttpPath]}")
                .to("http4://localhost");
    }
}

