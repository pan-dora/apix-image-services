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
package edu.amherst.acdc.services.inference;

import static org.apache.jena.rdf.model.ModelFactory.createDefaultModel;
import static org.apache.jena.rdf.model.ModelFactory.createInfModel;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static org.apache.jena.reasoner.ReasonerRegistry.getOWLReasoner;
import static org.apache.jena.riot.RDFLanguages.contentTypeToLang;
import static org.apache.jena.vocabulary.RDF.type;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.InputStream;

import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;

/**
 * @author acoburn
 * @since 6/16/16
 */
public class InferenceServiceImpl implements InferenceService {

    private static final Logger LOGGER  = getLogger(InferenceServiceImpl.class);

    @Override
    public boolean hasType(final String subject, final String rdfType, final InputStream input,
            final String contentType, final InputStream restrictions, final String restrictionFormat) {

        final Resource inferredType = createResource(rdfType);
        final Resource subjectResource = createResource(subject);
        final Model model = createDefaultModel();
        model.read(input, subject, contentTypeToLang(contentType).getName());

        final Model schema = createDefaultModel();
        schema.read(restrictions, null,
                contentTypeToLang(restrictionFormat).getName());

        if (model.contains(subjectResource, type, inferredType)) {
            return true;
        }

        final InfModel infmodel = createInfModel(getOWLReasoner(), model);
        infmodel.add(schema);

        return infmodel.contains(subjectResource, type, inferredType);
    }
}
