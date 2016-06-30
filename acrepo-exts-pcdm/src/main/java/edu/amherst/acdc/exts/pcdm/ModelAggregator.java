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

import static org.apache.jena.rdf.model.ModelFactory.createDefaultModel;
import static edu.amherst.acdc.exts.pcdm.PcdmHeaders.PCDM_MODEL;
import static org.slf4j.LoggerFactory.getLogger;

import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;

/**
 * Aggregate the CamelPcdmModel header values across exchanges
 *
 * @author acoburn
 * @since 6/29/16
 */
class ModelAggregator implements AggregationStrategy {

    private static final Logger LOGGER = getLogger(ModelAggregator.class);

    @Override
    public Exchange aggregate(final Exchange a, final Exchange b) {
        if (a == null) {
            return b;
        }

        final Model modelA = a.getIn().getHeader(PCDM_MODEL, Model.class);
        final Model modelB = b.getIn().getHeader(PCDM_MODEL, Model.class);

        if (modelA == null && modelB == null) {
            a.getIn().setHeader(PCDM_MODEL, createDefaultModel());
        } else if (modelA == null) {
            a.getIn().setHeader(PCDM_MODEL, modelB);
        } else if (modelB != null) {
            modelA.add(modelB);
            a.getIn().setHeader(PCDM_MODEL, modelA);
        }

        return a;
    }
}
