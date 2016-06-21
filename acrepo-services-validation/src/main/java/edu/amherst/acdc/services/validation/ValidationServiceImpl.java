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
package edu.amherst.acdc.services.validation;

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
import org.apache.jena.reasoner.ValidityReport;
import org.slf4j.Logger;

/**
 * @author acoburn
 * @since 6/16/16
 */
public class ValidationServiceImpl implements ValidationService {

    private static final Logger LOGGER  = getLogger(ValidationServiceImpl.class);

    private final Resource validationType;

    /**
     * Instantiate a ValidationService object
     * @param validType a URI for the RDF type used for validation.
     */
    public ValidationServiceImpl(final String validType) {
        this.validationType = createResource(validType);
    }

    /**
     *  Generate a compact representation of the input stream
     *
     *  @param input The input JSON document
     *  @param contextUrl the location of a context URL
     *  @return the compacted JSON Object
     */
    @Override
    public boolean validate(final String subject, final InputStream input, final String contentType,
            final InputStream restrictions, final String restrictionFormat) {

        final Resource subjectResource = createResource(subject);
        final Model model = createDefaultModel();
        model.read(input, subject, contentTypeToLang(contentType).getName());

        final Model schema = createDefaultModel();
        schema.read(restrictions, null,
                contentTypeToLang(restrictionFormat).getName());

        final InfModel infmodel = createInfModel(getOWLReasoner(), model);
        infmodel.add(schema);

        final ValidityReport validity = infmodel.validate();
        if (validity.isValid()) {
            return infmodel.contains(subjectResource, type, validationType);
        }

        validity.getReports().forEachRemaining(report -> {
            LOGGER.warn(report.toString());
        });
        return false;
    }
}
