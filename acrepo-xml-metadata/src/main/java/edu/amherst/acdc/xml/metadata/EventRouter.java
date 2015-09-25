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
package edu.amherst.acdc.xml.metadata;

import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.apache.camel.Exchange.CONTENT_TYPE;
import static org.fcrepo.camel.FcrepoHeaders.FCREPO_IDENTIFIER;
import static org.fcrepo.camel.FcrepoHeaders.FCREPO_BASE_URL;

import org.apache.camel.LoggingLevel;
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

        from("jetty:http://0.0.0.0:{{rest.port}}/dc?" +
                "matchOnUriPrefix=true&httpMethodRestrict=GET&sendServerVersion=false")
            .routeId("DcTransformation")
            .to("direct:getResource")
            .filter(header(HTTP_RESPONSE_CODE).isEqualTo(200))
              .setHeader(CONTENT_TYPE).constant("application/xml")
              .convertBodyTo(org.w3c.dom.Document.class)
              .log(LoggingLevel.INFO, "Converting resource to DC/XML: ${headers[CamelFcrepoIdentifier]}")
              .to("xslt:{{dc.xslt}}?saxon=true");

        from("jetty:http://0.0.0.0:{{rest.port}}/mods?" +
                "matchOnUriPrefix=true&httpMethodRestrict=GET&sendServerVersion=false")
            .routeId("ModsTransformation")
            .to("direct:getResource")
            .filter(header(HTTP_RESPONSE_CODE).isEqualTo(200))
              .setHeader(CONTENT_TYPE).constant("application/xml")
              .convertBodyTo(org.w3c.dom.Document.class)
              .log(LoggingLevel.INFO, "Converting resource to MODS/XML: ${headers[CamelFcrepoIdentifier]}")
              .to("xslt:{{mods.xslt}}?saxon=true");

        from("direct:getResource")
            .routeId("XmlTransformationCommon")
            .removeHeader("breadcrumbId")
            .removeHeader("Accept")
            .removeHeader("User-Agent")
            .setHeader(FCREPO_IDENTIFIER).simple("${headers[CamelHttpPath]}")
            .setHeader(FCREPO_BASE_URL).simple("{{fcrepo.baseUrl}}")
            .to("fcrepo:{{fcrepo.baseUrl}}?throwExceptionOnFailure=false");

    }
}

