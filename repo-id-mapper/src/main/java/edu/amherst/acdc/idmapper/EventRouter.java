/**
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
package edu.amherst.acdc.idmapper;

import org.apache.camel.builder.xml.Namespaces;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;

/**
 * A content router for handling JMS events.
 *
 * @author Aaron Coburn
 * @author escowles
 */
public class EventRouter extends RouteBuilder {

    private static final String CQL_INSERT = "insert into uris(fedora, public) values (?, ?)";
    private static final String CQL_GET = "";
    private static final String CQL_DELETE = "";

    /**
     * Configure the message route workflow.
     */
    public void configure() throws Exception {

        final Namespaces ns = new Namespaces("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        ns.add("dc", "http://purl.org/dc/elements/1.1/");


        /**
         * A generic error handler (specific to this RouteBuilder)
         */
        onException(Exception.class)
            .maximumRedeliveries("{{error.maxRedeliveries}}")
            .log("Event Routing Error: ${routeId}");

        /**
         * Process a message.
         */
        from("{{input.stream}}")
            .routeId("IdMapperRouter")
            .to("direct:event");

        from("direct:event")
            .routeId("IdMappingEventRouter")
            .log(LoggingLevel.INFO, "edu.amherst.acdc.idmapper",
                    "IdMapping Event: ${headers[org.fcrepo.jms.identifier]}")
            .to("fcrepo:localhost:8080/rest?preferOmit=PreferContainment")
            .filter(ns.xpath("/rdf:RDF/rdf:Description/dc:identifier"))
              .split().xtokenize("//dc:identifier", ns)
                .transform().xpath("/dc:identifier/@rdf:resource|/dc:identifier/text()", String.class, ns)
                .log("Body: ${body}")
                .process(new IdProcessor())
                .to("jdbc:idmapper");

        from("direct:get")
            .routeId("IdMappingFetchRouter")
            .log("${headers}")
            .to("mock:jdbc:idmapper");

        from("direct:put")
            .routeId("IdMappingPutRouter")
            .log("${headers}")
            .to("mock:jdbc:idmapper");

        from("direct:delete")
            .routeId("IdMappingDeleteRouter")
            .log("${headers}")
            .to("mock:jdbc:idmapper");
    }
}
