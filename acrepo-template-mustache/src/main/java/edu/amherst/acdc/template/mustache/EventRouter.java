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
package edu.amherst.acdc.template.mustache;

import static org.apache.camel.Exchange.CONTENT_TYPE;
import static org.apache.camel.Exchange.HTTP_PATH;
import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.apache.camel.Exchange.HTTP_URI;
import static org.apache.camel.Exchange.HTTP_URL;
import static org.fcrepo.camel.FcrepoHeaders.FCREPO_IDENTIFIER;

import java.util.Map;

import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.builder.RouteBuilder;
import edu.amherst.acdc.jsonld.cache.RiakKeyBuilder;

/**
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

        from("jetty:http://0.0.0.0:{{rest.port}}/template?" +
                "matchOnUriPrefix=true&httpMethodRestrict=GET&sendServerVersion=false")
            .routeId("TemplateTransformation")
            .log("PATH: ${headers[CamelHttpPath]}")
            .setHeader(FCREPO_IDENTIFIER).header(HTTP_PATH)
            .removeHeader("breadcrumbId")
            .removeHeader("Accept")
            .removeHeader("User-Agent")
            .to("direct:getFromCache")
            .choice()
                .when(header(HTTP_RESPONSE_CODE).isEqualTo(200)).to("direct:template")
                .otherwise().to("direct:getFromFedora");

        from("direct:getFromCache")
            .routeId("FetchFromCache")
            .removeHeader(HTTP_URL)
            .removeHeader(HTTP_URI)
            .process(new RiakKeyBuilder())
            .to("http4:{{riak.host}}?throwExceptionOnFailure=false");

        from("direct:getFromFedora")
            .routeId("FetchFromRepository")
            .to("fcrepo:{{fcrepo.baseUrl}}?accept=application/ld+json&throwExceptionOnFailure=false")
            .filter(header(HTTP_RESPONSE_CODE).isEqualTo(200))
              .to("direct:compact")
              .to("direct:template");

        from("direct:template")
            .routeId("TemplateRoute")
            .setHeader(CONTENT_TYPE).simple("{{mustache.contentType}}")
            .unmarshal().json(JsonLibrary.Jackson, Map.class)
            .to("mustache:{{mustache.template}}");
    }
}

