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
package edu.amherst.acdc.jsonld.cache;

import java.io.IOException;
import java.net.URLEncoder;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.RuntimeCamelException;
import org.fcrepo.camel.FcrepoHeaders;

/**
 * @author Aaron Coburn
 */
public class RiakKeyBuilder implements Processor {
    /**
     * Define how the message should be processed.
     *
     * @param exchange the current camel message exchange
     */
    public void process(final Exchange exchange) throws IOException {
        final Message in = exchange.getIn();
        final CamelContext ctx = exchange.getContext();

        final StringBuilder key = new StringBuilder("/buckets/");

        try {
            final String prefix = ctx.resolvePropertyPlaceholders("{{riak.bucket}}");
            key.append(prefix);
        } catch (final Exception ex) {
            throw new RuntimeCamelException("Could not resolve properties", ex);
        }

        key.append("/keys/");
        key.append(URLEncoder.encode(
                in.getHeader(FcrepoHeaders.FCREPO_IDENTIFIER, String.class), "UTF-8"));
        in.removeHeader(Exchange.HTTP_URL);
        in.setHeader(Exchange.HTTP_PATH, key.toString());
    }
}

