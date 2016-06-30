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
package edu.amherst.acdc.services.pcdm;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;
import static org.apache.jena.atlas.iterator.Iter.asStream;
import static org.apache.jena.rdf.model.ModelFactory.createDefaultModel;
import static org.apache.jena.rdf.model.ModelFactory.createInfModel;
import static org.apache.jena.rdf.model.ResourceFactory.createProperty;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static org.apache.jena.reasoner.ReasonerRegistry.getOWLMicroReasoner;
import static org.apache.jena.vocabulary.RDF.type;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Optional;
import java.util.Set;

import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.slf4j.Logger;

/**
 * @author acoburn
 * @since 6/16/16
 */
public class PcdmServiceImpl implements PcdmService {

    private static final Logger LOGGER = getLogger(PcdmServiceImpl.class);

    private static final String PCDM_NAMESPACE = "http://pcdm.org/models#";

    private static final String DEFAULT_LANG = "TTL";

    private final Model pcdmModel = createDefaultModel();

    /**
     * Create a PCDM Service object
     */
    public PcdmServiceImpl() {
        pcdmModel.read(getClass().getResourceAsStream("/pcdm.owl"), null, "TTL");
    }

    @Override
    public Model parseInto(final Model model, final InputStream input, final String contentType) {
        if (model == null) {
            return parseInto(createDefaultModel(), input, contentType);
        }
        model.read(input, null, getRdfLanguage(contentType).orElse(DEFAULT_LANG));
        return model;
    }

    @Override
    public boolean isObject(final Model model, final String subject) {
        return model.contains(createResource(subject), type,
                createResource(PCDM_NAMESPACE + "Object"));
    }

    @Override
    public boolean isFile(final Model model, final String subject) {
        return model.contains(createResource(subject), type,
                createResource(PCDM_NAMESPACE + "File"));
    }

    @Override
    public boolean isCollection(final Model model, final String subject) {
        return model.contains(createResource(subject), type,
                createResource(PCDM_NAMESPACE + "Collection"));
    }

    @Override
    public Set<String> memberOf(final Model model, final String subject) {
        return getObjectsOfProperty(model, subject, PCDM_NAMESPACE + "memberOf");
    }

    @Override
    public Set<String> hasMember(final Model model, final String subject) {
        return getObjectsOfProperty(model, subject, PCDM_NAMESPACE + "hasMember");
    }

    @Override
    public Set<String> fileOf(final Model model, final String subject) {
        return getObjectsOfProperty(model, subject, PCDM_NAMESPACE + "fileOf");
    }

    @Override
    public Set<String> hasFile(final Model model, final String subject) {
        return getObjectsOfProperty(model, subject, PCDM_NAMESPACE + "hasFile");
    }

    @Override
    public Set<String> relatedObjectOf(final Model model, final String subject) {
        return getObjectsOfProperty(model, subject, PCDM_NAMESPACE + "relatedObjectOf");
    }

    @Override
    public Set<String> hasRelatedObject(final Model model, final String subject) {
        return getObjectsOfProperty(model, subject, PCDM_NAMESPACE + "hasRelatedObject");
    }

    @Override
    public InputStream serialize(final Model model, final String contentType) {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        model.write(os, getRdfLanguage(contentType).orElse(DEFAULT_LANG));
        return new ByteArrayInputStream(os.toByteArray());
    }

    private Optional<String> getRdfLanguage(final String contentType) {
        return ofNullable(contentType).map(RDFLanguages::contentTypeToLang).map(Lang::getName);
    }

    private Set<String> getObjectsOfProperty(final Model model, final String subject, final String property) {
        final InfModel infModel = createInfModel(getOWLMicroReasoner(), createDefaultModel());
        infModel.add(model.listStatements());
        infModel.add(pcdmModel.listStatements());
        return asStream(infModel.listObjectsOfProperty(createResource(subject), createProperty(property)))
                .filter(RDFNode::isURIResource).map(RDFNode::asResource).map(Resource::getURI).collect(toSet());
    }
}
