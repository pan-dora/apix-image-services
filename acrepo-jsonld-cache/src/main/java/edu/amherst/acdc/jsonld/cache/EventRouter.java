/**
 * Copyright 2015 Amherst College
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
package edu.amherst.acdc.jsonld.cache;

import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.apache.camel.Exchange.HTTP_PATH;
import static org.apache.camel.Exchange.HTTP_METHOD;
import static org.fcrepo.camel.FcrepoHeaders.FCREPO_IDENTIFIER;
import static org.fcrepo.camel.FcrepoHeaders.FCREPO_BASE_URL;
import static org.fcrepo.camel.JmsHeaders.EVENT_TYPE;
import static org.fcrepo.camel.JmsHeaders.IDENTIFIER;
import static org.fcrepo.camel.RdfNamespaces.REPOSITORY;

//import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;

/**
 * A content router for handling JMS events.
 *
 * @author Aaron Coburn
 */
public class EventRouter extends RouteBuilder {

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

        from("{{input.stream}}")
            .setHeader(FCREPO_IDENTIFIER).header(IDENTIFIER)
            .choice()
                .when(header(EVENT_TYPE).isEqualTo(REPOSITORY + "NODE_REMOVED"))
                    .to("direct:delete")
                .otherwise()
                    .to("direct:update");

        from("jetty:http://0.0.0.0:{{rest.port}}/jsonld?" +
              "matchOnUriPrefix=true&sendServerVersion=false&httpMethodRestrict=GET,PUT,DELETE")
          .routeId("JsonLdRouter")
          .setHeader(FCREPO_IDENTIFIER).header(HTTP_PATH)
          .choice()
            .when(header(HTTP_METHOD).isEqualTo("GET")).to("direct:get")
            .when(header(HTTP_METHOD).isEqualTo("PUT")).to("direct:update")
            .when(header(HTTP_METHOD).isEqualTo("DELETE")).to("direct:delete");

        from("direct:update")
          .routeId("JsonLdUpdate")
          .to("direct:get")
          .filter(header(HTTP_RESPONSE_CODE).isEqualTo(200))
            .setHeader(HTTP_METHOD).constant("PUT")
            .process(new RiakKeyBuilder())
            .log("Updating cache entry for: ${headers[CamelFcrepoIdentifier]}")
            .to("http4://{{riak.host}}");

        from("direct:delete")
          .routeId("JsonLdDelete")
          .setHeader(HTTP_METHOD).constant("DELETE")
          .process(new RiakKeyBuilder())
          .log("Deleting cache entry for: ${headers[CamelFcrepoIdentifier]}")
          .to("http4://{{riak.host}}");

        from("direct:get")
          .routeId("JsonLdGet")
          .to("direct:getResource")
          .filter(header(HTTP_RESPONSE_CODE).isEqualTo(200))
            .log("Compacting resource ${headers[CamelFcrepoIdentifier]}")
            .to("direct:compact");

        from("direct:getResource")
          .routeId("JsonLdResource")
          .removeHeader("breadcrumId")
          .removeHeader("Accept")
          .removeHeader("User-Agent")
          .setHeader(FCREPO_BASE_URL).simple("{{fcrepo.baseUrl}}")
          .to("fcrepo:{{fcrepo.baseUrl}}?accept=application/ld+json&throwExceptionOnFailure=false");
    }
}
