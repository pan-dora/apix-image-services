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

        final StringBuilder key = new StringBuilder();

        try {
            final String prefix = ctx.resolvePropertyPlaceholders("{{riak.prefix}}");
            key.append(prefix);
            if (!prefix.endsWith("/")) {
                key.append("/");
            }
        } catch (final Exception ex) {
            throw new RuntimeCamelException("Could not resolve properties", ex);
        }

        key.append(URLEncoder.encode(
                in.getHeader(FcrepoHeaders.FCREPO_IDENTIFIER, String.class), "UTF-8"));
        in.setHeader(Exchange.HTTP_PATH, key.toString());
    }
}

