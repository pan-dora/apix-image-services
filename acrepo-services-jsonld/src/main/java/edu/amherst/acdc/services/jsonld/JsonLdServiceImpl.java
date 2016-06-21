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
package edu.amherst.acdc.services.jsonld;

import static com.github.jsonldjava.utils.JsonUtils.fromInputStream;
import static com.github.jsonldjava.utils.JsonUtils.fromURL;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.utils.JsonUtils;
import com.github.jsonldjava.core.JsonLdError;
import org.slf4j.Logger;

/**
 * @author acoburn
 * @since 9/21/15
 */
public class JsonLdServiceImpl implements JsonLdService {

    private static final Logger LOGGER  = getLogger(JsonLdServiceImpl.class);

    final JsonLdOptions options;

    /**
     * Instantiate a JsonLdService object
     */
    public JsonLdServiceImpl() {
        options = new JsonLdOptions();
    }

    /**
     *  Generate a compact representation of the input stream
     *
     *  @param input The input JSON document
     *  @param contextUrl the location of a context URL
     *  @return the compacted JSON Object
     */
    public String compact(final InputStream input, final String contextUrl) {

        LOGGER.info("using context from: {}", contextUrl);

        try {
            return doCompact(input, fromURL(new URL(contextUrl)));
        } catch (final MalformedURLException ex) {
            throw new RuntimeException("Invalid URL", ex);
        } catch (final JsonParseException ex) {
            throw new RuntimeException("Error parsing JSON", ex);
        } catch (final JsonGenerationException ex) {
            throw new RuntimeException("Error generating JSON", ex);
        } catch (final IOException ex) {
            throw new RuntimeException("Error reading/writing JSON document", ex);
        }
    }

    /**
     *  Generate a compact representation of the input stream
     *
     *  @param input The input JSON document
     *  @param context the context document as an InputStream
     *  @return the compact JSON Object
     */
    public String compact(final InputStream input, final InputStream context) {
        try {
            return doCompact(input, fromInputStream(context));
        } catch (final JsonParseException ex) {
            throw new RuntimeException("Error parsing JSON", ex);
        } catch (final JsonGenerationException ex) {
            throw new RuntimeException("Error generating JSON", ex);
        } catch (final IOException ex) {
            throw new RuntimeException("Error reading/writing JSON document", ex);
        }
    }

    /**
     * Generate an expanded representation of the input document
     *
     * @param input the input JSON document
     * @return the expanded JSON
     */
    public String expand(final InputStream input) {
        try {
            return "[" + JsonLdProcessor.expand(fromInputStream(input), options)
                .stream().findFirst().map(stringify::apply).orElse("") + "]";
        } catch (final JsonParseException ex) {
            throw new RuntimeException("Error parsing JSON", ex);
        } catch (final JsonGenerationException ex) {
            throw new RuntimeException("Error generating JSON", ex);
        } catch (final JsonLdError ex) {
            throw new RuntimeException("Error converting JsonLd", ex);
        } catch (final IOException ex) {
            throw new RuntimeException("Error reading/writing JSON document", ex);
        }
    }

    @SuppressWarnings("unchecked")
    private String doCompact(final InputStream input, final Object context) throws IOException {
        try {
            final List<Map<String, Object>> document = ((List<Map<String, Object>>)fromInputStream(input)).stream()
                                .filter(filterExport::test)
                                .collect(Collectors.toList());

            return JsonUtils.toString(
                    JsonLdProcessor.compact(document, context, options));
        } catch (final JsonLdError ex) {
            throw new RuntimeException("Error converting JsonLd", ex);
        }
    }

    private Predicate<Map<String, Object>> filterExport = x -> {
        return x.containsKey("@id") && !(x.get("@id").toString().endsWith("/fcr:export?format=jcr/xml")
                || x.get("@id").toString().equals("http://fedora.info/definitions/v4/repository#jcr/xml"));
    };

    private Function<Object, String> stringify = x -> {
        try {
            return JsonUtils.toString(x);
        } catch (final JsonGenerationException ex) {
            throw new RuntimeException("Error generating JSON", ex);
        } catch (final IOException ex) {
            throw new RuntimeException("Error reading/writing JSON document", ex);
        }
    };
}
