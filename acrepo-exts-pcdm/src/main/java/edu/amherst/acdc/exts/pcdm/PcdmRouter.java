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
package edu.amherst.acdc.exts.pcdm;

import static java.util.Optional.of;
import static org.apache.camel.Exchange.CONTENT_TYPE;
import static org.apache.camel.Exchange.HTTP_METHOD;
import static org.apache.camel.Exchange.HTTP_PATH;
import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.fcrepo.camel.FcrepoHeaders.FCREPO_IDENTIFIER;
import static org.fcrepo.camel.FcrepoHeaders.FCREPO_BASE_URL;
import static edu.amherst.acdc.exts.pcdm.PcdmHeaders.PCDM_ACCEPT;
import static edu.amherst.acdc.exts.pcdm.PcdmHeaders.PCDM_SUBJECT;

import java.util.Optional;

import org.apache.camel.builder.RouteBuilder;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;

/**
 * A content router for handling PCDM extension requests
 *
 * @author Aaron Coburn
 */
public class PcdmRouter extends RouteBuilder {

    private final static String DEFAULT_CONTENT_TYPE = "text/turtle";

    /**
     * Configure the message route workflow.
     */
    public void configure() throws Exception {

        from("jetty:http://{{rest.host}}:{{rest.port}}{{rest.prefix}}?" +
              "matchOnUriPrefix=true&sendServerVersion=false&httpMethodRestrict=GET,OPTIONS")
          .routeId("PcdmRouter")
          .removeHeader("User-Agent")
          .choice()
            .when(header(HTTP_METHOD).isEqualTo("GET"))
              .log("Building PCDM Object ${headers[CamelHttpPath]}")
              .to("direct:get")
            .when(header(HTTP_METHOD).isEqualTo("OPTIONS"))
              .setHeader(CONTENT_TYPE).constant("text/turtle")
              .setHeader("Allow").constant("GET,OPTIONS")
              .to("language:simple:resource:classpath:options.ttl");

        from("direct:get")
          .routeId("PcdmGet")
          .setHeader(PCDM_ACCEPT, header("Accept"))
          .setBody(header(HTTP_PATH))
          .setHeader(FCREPO_BASE_URL).simple("{{fcrepo.baseUrl}}")
          .to("seda:recurse")
          .removeHeader("breadcrumbId")
          .process(exchange -> {
              final String contentType = exchange.getIn().getHeader(PCDM_ACCEPT, String.class);
              final Optional<String> rdfLang = of(contentType).map(RDFLanguages::contentTypeToLang).map(Lang::getName);
              exchange.getIn().setHeader(CONTENT_TYPE, rdfLang.isPresent() ? contentType : DEFAULT_CONTENT_TYPE);
          })
          .to("direct:serialize");

        from("seda:recurse?concurrentConsumers={{pcdm.concurrency}}")
          .routeId("PcdmBuildRecursive")
          .setHeader(FCREPO_IDENTIFIER, body())
          .to("direct:getResource")
          .filter(header(HTTP_RESPONSE_CODE).isEqualTo(200))
            .log("Getting related resources for ${headers[CamelFcrepoIdentifier]}")
            .to("direct:parse")
            .setHeader(PCDM_SUBJECT).simple("${headers.CamelFcrepoBaseUrl}${headers.CamelFcrepoIdentifier}")
            .to("direct:members")
            .to("direct:files")
            .to("direct:relatedObjects")
            .process(new RelatedProcessor())
            .split(body(), new ModelAggregator())
            .to("seda:recurse");

        from("direct:getResource")
          .routeId("PcdmResource")
          .removeHeader("breadcrumbId")
          .removeHeader("Accept")
          .setHeader(FCREPO_BASE_URL).simple("{{fcrepo.baseUrl}}")
          .to("fcrepo:{{fcrepo.baseUrl}}?throwExceptionOnFailure=false");
    }
}
