/**
 * Copyright 2015 DuraSpace, Inc.
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
package edu.amherst.acdc.idiomatic;

import static org.fcrepo.camel.JmsHeaders.IDENTIFIER;

import java.util.Arrays;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

/**
 * @author Aaron Coburn
 */

public class IdProcessor implements Processor {

    /**
     * Define how a message should be processed.
     *
     * @param exchange the current camel message exchange     */
    public void process(final Exchange exchange) throws Exception {
        final Message in = exchange.getIn();
        final String id = in.getBody(String.class);

        // update exchange
        final List<String> data = Arrays.asList(in.getHeader(IDENTIFIER, String.class), id);
        if (data.size() == 2) {
            in.setBody(data);
        } else {
            in.setBody(null);
        }
    }
}
