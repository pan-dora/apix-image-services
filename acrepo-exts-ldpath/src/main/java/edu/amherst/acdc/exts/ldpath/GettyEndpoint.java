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
package edu.amherst.acdc.exts.ldpath;

import org.apache.marmotta.commons.http.ContentType;
import org.apache.marmotta.ldclient.api.endpoint.Endpoint;

/**
 * @author acoburn
 */
public class GettyEndpoint extends Endpoint {

    /**
     * Create a Getty endpoint to be used with the Marmotta LDClient
     *
     * @param timeout the length of time (in seconds) to cache the data
     */
    public GettyEndpoint(final Long timeout) {
        super("Getty Endpoint", GettyProvider.PROVIDER_NAME, "http://vocab.getty.edu/.*", null, timeout);
        setPriority(PRIORITY_HIGH);
        addContentType(new ContentType("application", "ld+json", 1.0));
    }
}
