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
package cool.pandora.exts.serialize.xml;

import static org.apache.camel.Exchange.HTTP_METHOD;
import static org.apache.camel.Exchange.HTTP_PATH;
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

        from("jetty:http://{{rest.host}}:{{rest.port}}{{rest.prefix}}?" +
                "matchOnUriPrefix=true&httpMethodRestrict=GET,OPTIONS&sendServerVersion=false")
            .routeId("XmlAccept")
            .choice()
                .when(header(HTTP_METHOD).isEqualTo("OPTIONS"))
                    .to("direct:options")
                .when(header(HTTP_PATH).contains("dc"))
                    .to("direct:dc")
                .when(header(HTTP_PATH).contains("mods"))
                    .to("direct:mods")
                .otherwise()
                    .to("direct:choice");

        from("direct:dc")
            .routeId("XmlDcXslt")
            .to("direct:getResource")
            .filter(header(HTTP_RESPONSE_CODE).isEqualTo(200))
              .setHeader(CONTENT_TYPE).constant("application/xml")
              .convertBodyTo(org.w3c.dom.Document.class)
              .log(LoggingLevel.INFO, "Converting resource to DC/XML: ${headers[CamelFcrepoIdentifier]}")
              .to("xslt:{{dc.xslt}}?saxon=true");

        from("direct:mods")
            .routeId("XmlModsXslt")
            .to("direct:getResource")
            .filter(header(HTTP_RESPONSE_CODE).isEqualTo(200))
              .setHeader(CONTENT_TYPE).constant("application/xml")
              .convertBodyTo(org.w3c.dom.Document.class)
              .log(LoggingLevel.INFO, "Converting resource to MODS/XML: ${headers[CamelFcrepoIdentifier]}")
              .to("xslt:{{mods.xslt}}?saxon=true");

        from("direct:options")
            .routeId("XmlOptions")
            .setHeader(CONTENT_TYPE).constant("text/turtle")
            .setHeader("Allow").constant("GET,OPTIONS")
            .to("language:simple:resource:classpath:options.ttl");

        from("direct:choice")
            .routeId("ChooseFormat")
            .setHeader(CONTENT_TYPE).constant("text/html")
            .setHeader("Allow").constant("GET,OPTIONS")
            .to("language:simple:resource:classpath:index.html");

        from("direct:getResource")
            .routeId("XmlTransformationCommon")
            .removeHeader("breadcrumbId")
            .removeHeader("Accept")
            .removeHeader("User-Agent")
            .process(e -> e.getIn().setHeader(FCREPO_IDENTIFIER,
                    e.getIn().getHeader("Apix-Ldp-Resource-Path",
                            e.getIn().getHeader(HTTP_PATH, "", String.class).replaceFirst("^/.+?/", "/"))))
            .setHeader(FCREPO_BASE_URL).simple("{{fcrepo.baseUrl}}")
            .to("fcrepo:{{fcrepo.baseUrl}}?throwExceptionOnFailure=false");

    }
}

