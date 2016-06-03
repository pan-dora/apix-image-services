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
package edu.amherst.acdc.fits.rest;

import static org.apache.camel.Exchange.CONTENT_TYPE;
import static org.apache.camel.Exchange.HTTP_METHOD;
import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.apache.camel.Exchange.HTTP_URI;
import static org.apache.camel.Exchange.HTTP_PATH;
import static org.apache.camel.LoggingLevel.INFO;
import static org.apache.http.entity.mime.MultipartEntityBuilder.create;

import java.io.InputStream;

import org.apache.camel.builder.RouteBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A content router for handling JMS events.
 *
 * @author Bethany Seeger
 */
public class FitsRouter extends RouteBuilder {

    private static final String FEDORA_PATH = "CamelFedoraPath";

    private static final Logger LOGGER = LoggerFactory.getLogger(FitsRouter.class);

    /**
     * Configure the message route workflow.
     */
    public void configure() throws Exception {
        /**
         * A generic error handler (specific to this RouteBuilder)
         */
        onException(Exception.class)
            .maximumRedeliveries("{{error.maxRedeliveries}}")
            .log("Fits Routing Error: ${routeId}");

        from("jetty:http://{{rest.host}}:{{rest.port}}{{rest.prefix}}?" +
            "matchOnUriPrefix=true&httpMethodRestrict=GET,OPTIONS&sendServerVersion=false")
          .routeId("AcrepoFitsRest")
          .routeDescription(
              "FITS service to gather technical information about a binary located in a Fedora repository.")
          .log(INFO, LOGGER, "Received request for Fits data for: ${headers[CamelHttpPath]}")
          .setHeader(FEDORA_PATH).header(HTTP_PATH)
          .choice()
              .when(header(HTTP_METHOD).isEqualTo("GET"))
                  .to("direct:fitsService")
              .when(header(HTTP_METHOD).isEqualTo("OPTIONS"))
                  .setHeader(CONTENT_TYPE).constant("text/turtle")
                  .setHeader("Allow").constant("GET,OPTIONS")
                  .to("language:simple:resource:classpath:options.ttl");

        from("direct:fitsService")
          .routeId("AcrepoFitsFedoraLookup")
          .log(INFO, LOGGER, "FitsFedoraLookup - fetching ${headers[CamelHttpPath]}")
          .setHeader(HTTP_METHOD).constant("HEAD")
          .setHeader(HTTP_URI).simple("http://{{fcrepo.baseUrl}}")
          .to("http4://{{fcrepo.baseUrl}}?authUsername={{fcrepo.authUsername}}" +
              "&authPassword={{fcrepo.authPassword}}&throwExceptionOnFailure=false")
          .choice()
            .when(header("Link").contains("<http://www.w3.org/ns/ldp#NonRDFSource>;rel=\"type\""))
                .to("direct:fits")
            .when(header(HTTP_RESPONSE_CODE).isEqualTo(200))
                .removeHeaders("*")
                .log(INFO, LOGGER, "Object is not a binary resource, sending 4xx")
                .setBody(constant("Error: this resource is not a fedora:Binary"))
                .setHeader(CONTENT_TYPE).constant("text/plain")
                .setHeader(HTTP_RESPONSE_CODE).constant(400);

        from("direct:fits")
            .routeId("AcrepoFitsEndpoint")
            .log(INFO, LOGGER, "Object is invoking Fits Service")
            .removeHeaders("CamelHttp*")
            .setHeader(HTTP_METHOD).constant("GET")
            .setHeader(HTTP_PATH).header(FEDORA_PATH)
            .to("http4://{{fcrepo.baseUrl}}?authUsername={{fcrepo.authUsername}}" +
              "&authPassword={{fcrepo.authPassword}}&throwExceptionOnFailure=false")
            .removeHeaders("CamelHttp*")
            .process(exchange -> {
                exchange.getOut().setBody(
                    create().addPart("datafile",
                        new InputStreamBody(exchange.getIn().getBody(InputStream.class), "UTF-8")).build());
            })
            .setHeader(CONTENT_TYPE).constant("multipart/form-data")
            .setHeader(HTTP_METHOD).constant("POST")
            .to("http4://{{fits.endpoint}}");
    }
}
