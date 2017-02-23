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
package edu.amherst.acdc.connector.triplestore;

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
import java.io.UncheckedIOException;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.fcrepo.camel.processor.EventProcessor;
import org.fcrepo.camel.processor.SparqlUpdateProcessor;
import org.slf4j.Logger;

/**
 * A content router for handling Fedora events.
 *
 * @author Aaron Coburn
 */
public class TriplestoreRouter extends RouteBuilder {

    private static final Logger LOGGER = getLogger(TriplestoreRouter.class);

    private static final String RESOURCE_DELETION = "http://fedora.info/definitions/v4/event#ResourceDeletion";
    private static final String DELETE = "https://www.w3.org/ns/activitystreams#Delete";

    /**
     * Configure the message route workflow.
     */
    public void configure() throws Exception {

        /**
         * A generic error handler (specific to this RouteBuilder)
         */
        onException(Exception.class)
            .maximumRedeliveries("{{error.maxRedeliveries}}")
            .log("Index Routing Error: ${routeId}");

        /**
         * route a message to the proper queue, based on whether
         * it is a DELETE or UPDATE operation.
         */
        from("{{input.stream}}")
            .routeId("FcrepoTriplestoreRouter")
            .process(new EventProcessor())
            .setHeader(FCREPO_NAMED_GRAPH).header(FCREPO_URI)
            .choice()
                .when(or(header(FCREPO_EVENT_TYPE).contains(RESOURCE_DELETION),
                            header(FCREPO_EVENT_TYPE).contains(DELETE)))
                    .to("direct:delete.triplestore")
                .when(not(header(FCREPO_URI).contains("#")))
                    .to("direct:index.triplestore");

        /**
         * Handle re-index events
         */
        from("{{triplestore.reindex.stream}}")
            .routeId("FcrepoTriplestoreReindex")
            .setHeader(FCREPO_NAMED_GRAPH).header(FCREPO_URI)
            .to("direct:index.triplestore");

        /**
         * Based on an item's metadata, determine if it is indexable.
         */
        from("direct:index.triplestore")
            .routeId("FcrepoTriplestoreIndexer")
            .filter(not(in(tokenizePropertyPlaceholder(getContext(), "{{filter.containers}}", ",").stream()
                        .map(uri -> or(
                            header(FCREPO_URI).startsWith(constant(uri + "/")),
                            header(FCREPO_URI).isEqualTo(constant(uri))))
                        .collect(toList()))))
            .removeHeaders("CamelHttp*")
            .to("direct:update.triplestore");

        /**
         * Remove an item from the triplestore index.
         */
        from("direct:delete.triplestore")
            .routeId("FcrepoTriplestoreDeleter")
            .log(LoggingLevel.INFO, LOGGER,
                    "Deleting Triplestore Graph ${headers[CamelFcrepoUri]}")
            .setHeader(HTTP_METHOD).constant("POST")
            .setHeader(CONTENT_TYPE).constant("application/x-www-form-urlencoded; charset=utf-8")
            .process(e -> e.getIn().setBody(sparqlUpdate(deleteAll(getMandatoryHeader(e, FCREPO_URI, String.class)))))
            .to("{{triplestore.baseUrl}}?useSystemProperties=true");

        /**
         * Perform the sparql update.
         */
        from("direct:update.triplestore")
            .routeId("FcrepoTriplestoreUpdater")
            .to("fcrepo:{{fcrepo.baseUrl}}?accept=application/n-triples" +
                    "&preferOmit={{prefer.omit}}&preferInclude={{prefer.include}}")
            .process(new SparqlUpdateProcessor())
            .log(LoggingLevel.INFO, LOGGER,
                    "Indexing Triplestore Object ${headers[CamelFcrepoUri]}")
            .to("{{triplestore.baseUrl}}?useSystemProperties=true");
    }

    private static String deleteAll(final String graphName) {
        return "DELETE WHERE { GRAPH <" + graphName + "> { ?s ?p ?o } }";
    }

    private static String sparqlUpdate(final String command) {
        try {
            return "update=" + encode(command, "UTF-8");
        } catch (final IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
