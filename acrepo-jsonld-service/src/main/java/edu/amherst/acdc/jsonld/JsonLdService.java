/*
 * Copyright 2015 Amherst College
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
package edu.amherst.acdc.jsonld;

import java.io.InputStream;

/**
 * @author acoburn
 * @since 9/14/15
 */
public interface JsonLdService {

    /**
     *  Generate a compact representation of the input stream
     *
     *  @param input The input JSON document
     *  @param contextUrl the location of a context URL
     *  @return the compacted JSON Object
     */
    String compact(final InputStream input, final String contextUrl);

    /**
     *  Generate a compact representation of the input stream
     *
     *  @param input The input JSON document
     *  @param context the context document as an InputStream
     *  @return the compact JSON Object
     */
    String compact(final InputStream input, final InputStream context);

    /**
     * Generate an expanded representation of the input document
     *
     * @param input the input JSON document
     * @return the expanded JSON
     */
    String expand(final InputStream input);
}
