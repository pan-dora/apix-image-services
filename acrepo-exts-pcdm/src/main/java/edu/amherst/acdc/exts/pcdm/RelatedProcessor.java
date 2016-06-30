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
package edu.amherst.acdc.exts.pcdm;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toSet;
import static edu.amherst.acdc.exts.pcdm.PcdmHeaders.PCDM_FILES;
import static edu.amherst.acdc.exts.pcdm.PcdmHeaders.PCDM_MEMBERS;
import static edu.amherst.acdc.exts.pcdm.PcdmHeaders.PCDM_RELATED_OBJECTS;
import static org.fcrepo.camel.FcrepoHeaders.FCREPO_BASE_URL;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import java.util.HashSet;
import java.util.Set;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;

/**
 * Aggregate all of the pcdm:hasMember, pcdm:hasFile and pcdm:hasRelatedObject values
 * into a single Collection in the body.
 *
 * @author acoburn
 * @since 6/29/16
 */
class RelatedProcessor implements Processor {

    private static final Logger LOGGER = getLogger(RelatedProcessor.class);

    @Override
    public void process(final Exchange exchange) throws Exception {
        final Set<String> ids = new HashSet<>();
        @SuppressWarnings("unchecked")
        final List<String> members = exchange.getIn().getHeader(PCDM_MEMBERS, emptyList(), List.class);
        @SuppressWarnings("unchecked")
        final List<String> files = exchange.getIn().getHeader(PCDM_FILES, emptyList(), List.class);
        @SuppressWarnings("unchecked")
        final List<String> relatedObjects = exchange.getIn().getHeader(PCDM_RELATED_OBJECTS, emptyList(), List.class);

        ids.addAll(members);
        ids.addAll(files);
        ids.addAll(relatedObjects);

        exchange.getIn().setBody(ids.stream()
            .map(id -> id.replace(exchange.getIn().getHeader(FCREPO_BASE_URL, "", String.class), ""))
            .collect(toSet()));
    }
}
