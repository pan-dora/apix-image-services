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
package edu.amherst.acdc.exts.jsonld;

import static org.apache.camel.Exchange.CONTENT_TYPE;
import static org.apache.camel.Exchange.HTTP_METHOD;
import static org.apache.camel.Exchange.HTTP_PATH;
import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.fcrepo.camel.FcrepoHeaders.FCREPO_IDENTIFIER;
import static org.fcrepo.camel.FcrepoHeaders.FCREPO_BASE_URL;

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

        from("jetty:http://{{rest.host}}:{{rest.port}}{{rest.prefix}}?" +
              "matchOnUriPrefix=true&sendServerVersion=false&httpMethodRestrict=GET,OPTIONS")
          .routeId("JsonLdRouter")
          .choice()
            .when(header(HTTP_METHOD).isEqualTo("GET"))
              .log("JSONLD Processing ${headers[CamelHttpPath]}")
              .setHeader(FCREPO_IDENTIFIER).header(HTTP_PATH)
              .to("direct:get")
            .when(header(HTTP_METHOD).isEqualTo("OPTIONS"))
              .setHeader(CONTENT_TYPE).constant("text/turtle")
              .setHeader("Allow").constant("GET,OPTIONS")
              .to("language:simple:resource:classpath:options.ttl");

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
