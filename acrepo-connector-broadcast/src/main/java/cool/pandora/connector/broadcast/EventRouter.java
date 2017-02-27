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
package cool.pandora.connector.broadcast;

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

        from("{{input.stream}}")
            .routeId("MessageBroadcaster")
            .description("Broadcast messages from one queue/topic to other specified queues/topics.")
            .log("Distributing message: ${headers[org.fcrepo.jms.timestamp]}: " +
                    "${headers[org.fcrepo.jms.identifier]}:${headers[org.fcrepo.jms.eventType]}")
            .recipientList(simple("{{message.recipients}}")).ignoreInvalidEndpoints();
    }
}
