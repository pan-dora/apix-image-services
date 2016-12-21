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

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

import java.util.List;

import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.provider.rdf.LinkedDataProvider;

/**
 * @author acoburn
 */
public class GettyProvider extends LinkedDataProvider {

    public static final String PROVIDER_NAME = "Getty Vocabulary";

    @Override
    public String getName() {
        return PROVIDER_NAME;
    }

    @Override
    public List<String> buildRequestUrl(final String resourceUri, final Endpoint endpoint) {
        requireNonNull(resourceUri);
        return singletonList("http://vocab.getty.edu/download/jsonld?uri=" + resourceUri);
    }
}
