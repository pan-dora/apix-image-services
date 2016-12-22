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
package edu.amherst.acdc.exts.template;

import static org.apache.camel.component.mustache.MustacheConstants.MUSTACHE_RESOURCE_URI;
import static org.apache.camel.Exchange.CONTENT_TYPE;
import static org.apache.camel.Exchange.HTTP_METHOD;
import static org.apache.camel.Exchange.HTTP_PATH;
import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.fcrepo.camel.FcrepoHeaders.FCREPO_IDENTIFIER;
import java.util.Map;

import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.builder.RouteBuilder;

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

        from("jetty:http://{{rest.host}}:{{rest.port}}/template?" +
                "matchOnUriPrefix=true&httpMethodRestrict=GET,OPTIONS&sendServerVersion=false")
            .routeId("TemplateTransformation")
            .choice()
                .when(header(HTTP_METHOD).isEqualTo("GET"))
                    .process(e -> e.getIn().setHeader(FCREPO_IDENTIFIER,
                            e.getIn().getHeader("Apix-Ldp-Resource-Path",
                                    e.getIn().getHeader(HTTP_PATH))))
                    .log("PATH: ${headers[CamelFcrepoIdentifier]}")
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
            .to("fcrepo:{{fcrepo.baseUrl}}?accept=application/ld+json&throwExceptionOnFailure=false")
            .filter(header(HTTP_RESPONSE_CODE).isEqualTo(200))
              .to("direct:compact")
              .to("direct:template");


        from("direct:template")
            .routeId("TemplateRoute")
            .setHeader(CONTENT_TYPE).simple("{{mustache.contentType}}")
            .unmarshal().json(JsonLibrary.Jackson, Map.class)
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

