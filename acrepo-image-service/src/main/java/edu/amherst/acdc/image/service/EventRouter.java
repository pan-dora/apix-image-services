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
package edu.amherst.acdc.image.service;

import static java.util.Arrays.stream;
import static org.apache.camel.Exchange.CONTENT_TYPE;
import static org.apache.camel.Exchange.HTTP_METHOD;
import static org.apache.camel.Exchange.HTTP_PATH;
import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.apache.camel.Exchange.HTTP_URI;
import static org.apache.camel.LoggingLevel.INFO;
import static org.apache.camel.builder.PredicateBuilder.and;
import static org.apache.camel.component.exec.ExecBinding.EXEC_COMMAND_ARGS;
import static org.fcrepo.camel.FcrepoHeaders.FCREPO_IDENTIFIER;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.InputStream;

import org.apache.camel.RuntimeCamelException;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;

/**
 * A content router for handling JMS events.
 *
 * @author Aaron Coburn
 */
public class EventRouter extends RouteBuilder {

    private final String IMAGE_OUTPUT = "CamelImageOutput";
    private final String IMAGE_INPUT = "CamelImageInput";
    private final String HTTP_ACCEPT = "Accept";
    private final String DEFAULT_OUTPUT_FORMAT = "jpeg";
    private final String HTTP_QUERY_OPTIONS = "options";

    private static final Logger LOGGER = getLogger(EventRouter.class);

    /**
     * Configure the message route workflow.
     */
    public void configure() throws Exception {

        from("jetty:http://{{rest.host}}:{{rest.port}}{{rest.prefix}}?" +
              "matchOnUriPrefix=true&sendServerVersion=false&httpMethodRestrict=GET,OPTIONS")
          .routeId("ImageRouter")
          .setHeader(FCREPO_IDENTIFIER).header(HTTP_PATH)
          .setHeader(IMAGE_OUTPUT).header(HTTP_ACCEPT)
          .removeHeaders(HTTP_ACCEPT)
          .choice()
            .when(header(HTTP_METHOD).isEqualTo("GET"))
              .setHeader(FCREPO_IDENTIFIER).header(HTTP_PATH)
              .to("direct:get")
            .when(header(HTTP_METHOD).isEqualTo("OPTIONS"))
              .setHeader(CONTENT_TYPE).constant("text/turtle")
              .setHeader(HTTP_ACCEPT).constant("GET,OPTIONS")
              .to("language:simple:resource:classpath:options.ttl");

        from("direct:get")
          .routeId("ImageGet")
          .setHeader(HTTP_METHOD).constant("HEAD")
          .setHeader(HTTP_URI).simple("http://{{fcrepo.baseUrl}}")
          .to("http4://{{fcrepo.baseUrl}}?authUsername={{fcrepo.authUsername}}" +
              "&authPassword={{fcrepo.authPassword}}&throwExceptionOnFailure=false")
          .choice()
            .when(and(header(CONTENT_TYPE).startsWith("image/"),
                        header("Link").contains("<http://www.w3.org/ns/ldp#NonRDFSource>;rel=\"type\"")))
              .log(INFO, LOGGER, "Image Processing ${headers[CamelHttpPath]}")
              .to("direct:convert")
            .when(header("Link").contains("<http://www.w3.org/ns/ldp#NonRDFSource>;rel=\"type\""))
              .setBody(constant("Error: this resource is not an image"))
              .to("direct:invalidFormat")
            .when(header(HTTP_RESPONSE_CODE).isEqualTo(200))
              .setBody(constant("Error: this resource is not a fedora:Binary"))
              .to("direct:invalidFormat")
            .otherwise()
              .to("direct:error");

         from("direct:invalidFormat")
             .routeId("ImageInvalidFormat")
             .removeHeaders("*")
             .setHeader(CONTENT_TYPE).constant("text/plain")
             .setHeader(HTTP_RESPONSE_CODE).constant(400);

         from("direct:error")
             .routeId("ImageError")
             .setBody(constant("Error: this resource is not accessible"))
             .setHeader(CONTENT_TYPE).constant("text/plain");

        from("direct:convert")
          .routeId("ImageConvert")
          .setHeader(HTTP_METHOD).constant("GET")
          .setHeader(HTTP_PATH).header(FCREPO_IDENTIFIER)
          .to("http4://{{fcrepo.baseUrl}}?authUsername={{fcrepo.authUsername}}" +
              "&authPassword={{fcrepo.authPassword}}&throwExceptionOnFailure=true")
          .setHeader(IMAGE_INPUT).header(CONTENT_TYPE)
          .process(exchange -> {
              final String accept = exchange.getIn().getHeader(IMAGE_OUTPUT, "", String.class);
              final String fmt = accept.matches("^image/\\w+$") ? accept.replace("image/", "") : DEFAULT_OUTPUT_FORMAT;
              final boolean valid;
              try {
                  valid = stream(getContext().resolvePropertyPlaceholders("{{valid.formats}}").split(","))
                                            .anyMatch(fmt::equals);
              } catch (final Exception ex) {
                  throw new RuntimeCamelException("Couldn't resolve property placeholder", ex);
              }

              if (valid) {
                  exchange.getIn().setHeader(IMAGE_OUTPUT, "image/" + fmt);
                  exchange.getIn().setHeader(EXEC_COMMAND_ARGS,
                      " - " + exchange.getIn().getHeader(HTTP_QUERY_OPTIONS, "", String.class) + " " + fmt + ":-");
              } else {
                  throw new RuntimeCamelException("Invalid format: " + fmt);
              }
          })
          .removeHeaders("CamelHttp*")
          .log(INFO, LOGGER, "Converting from ${headers[CamelImageInput]} to ${headers[CamelImageOutput]}")
          .to("exec:{{convert.path}}")
          .process(exchange -> {
              exchange.getOut().setBody(exchange.getIn().getBody(InputStream.class));
          });
    }
}
