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
package cool.pandora.connector.idiomatic;

import static cool.pandora.connector.idiomatic.IdiomaticHeaders.FEDORA;
import static cool.pandora.connector.idiomatic.IdiomaticHeaders.ID;
import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.apache.camel.builder.PredicateBuilder.not;
import static org.apache.camel.model.dataformat.JsonLibrary.Jackson;
import static org.fcrepo.camel.FcrepoHeaders.FCREPO_URI;

import java.util.List;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.fcrepo.camel.processor.EventProcessor;

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

        /**
         * Process a message via JMS
         */
        from("{{input.stream}}").routeId("IdMappingRouter")
            .process(new EventProcessor())
            .log(LoggingLevel.INFO, "IdMapping Event: ${headers[CamelFcrepoUri]}")
            .to("fcrepo:{{fcrepo.baseUrl}}?preferOmit=PreferContainment&accept=application/ld+json")
            .unmarshal().json(Jackson, List.class)
            .split(simple("${body}"))
            .filter(simple("${body[@id]} == ${header.CamelFcrepoUri}"))
              .filter(simple("${body[{{id.property}}]} != null"))
                .split(simple("${body[{{id.property}}]}"))
                  .setHeader(ID).simple("${body}")
                  .transform().header(FCREPO_URI)
                  .to("direct:update");

        /**
         * REST routing
         */
        rest("{{rest.prefix}}")
            .get("/{" + ID + "}").to("direct:get")
            .post("/").to("direct:minter")
            .put("/{" + ID + "}").to("direct:update")
            .delete("/{" + ID + "}").to("direct:delete");

        /**
         * Handle CRUD operations
         */
        from("direct:update")
            .routeId("IdMappingUpdateRouter")
            .setHeader(FEDORA).body()
            .setHeader(HTTP_RESPONSE_CODE).constant(400)
            .filter(header(FEDORA))
              .log(LoggingLevel.INFO, "Updating ${headers[" + ID + "]} with ${headers[" + FEDORA + "]}")
              .to("sql:UPDATE uris SET fedora=:#" + FEDORA + " WHERE public=:#" + ID)
              .setHeader(HTTP_RESPONSE_CODE).constant(204)
              .choice()
                .when(header("CamelSqlUpdateCount").isEqualTo("0"))
                  .to("sql:INSERT INTO uris (fedora, public) VALUES (:#" + FEDORA + ", :#" + ID + ")").end()
            .removeHeader(ID)
            .removeHeader(FEDORA);

        from("direct:get")
            .routeId("IdMappingFetchRouter")
            .to("sql:SELECT fedora FROM uris WHERE public=:#" + ID + "?outputType=SelectOne")
            .removeHeader(ID)
            .filter(not(header("CamelSqlRowCount").isEqualTo(1)))
                .setHeader(HTTP_RESPONSE_CODE).constant(404);

        from("direct:delete")
            .routeId("IdMappingDeleteRouter")
            .log(LoggingLevel.INFO, "Deleting ${headers[" + ID + "]}")
            .to("sql:DELETE FROM uris WHERE public=:#" + ID)
            .removeHeader(ID)
            .setHeader(HTTP_RESPONSE_CODE).constant(204);

    }
}
