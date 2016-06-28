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

import static java.util.stream.Collectors.toSet;
import static org.apache.jena.atlas.iterator.Iter.asStream;
import static org.apache.jena.rdf.model.ModelFactory.createDefaultModel;
import static org.apache.jena.rdf.model.ModelFactory.createInfModel;
import static org.apache.jena.rdf.model.ResourceFactory.createProperty;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static org.apache.jena.reasoner.ReasonerRegistry.getOWLMicroReasoner;
import static org.apache.jena.riot.RDFLanguages.contentTypeToLang;
import static org.apache.jena.vocabulary.RDF.type;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Set;

import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.RDFNode;

/**
 * @author acoburn
 * @since 6/16/16
 */
public class PcdmServiceImpl implements PcdmService {

    private static final String PCDM_NAMESPACE = "http://pcdm.org/models#";

    private final Model pcdmModel = createDefaultModel();

    /**
     * Create a PCDM Service object
     */
    public PcdmServiceImpl() {
        pcdmModel.read(getClass().getResourceAsStream("/pcdm.owl"), null, "TTL");
    }

    @Override
    public Model parse(final InputStream input, final String contentType) {
        return parseInto(createDefaultModel(), input, contentType);
    }

    @Override
    public Model parseInto(final Model model, final InputStream input, final String contentType) {
        model.read(input, null, contentTypeToLang(contentType).getName());
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
    public Set<String> getMemberOf(final Model model, final String subject) {
        return getObjectsOfProperty(model, subject, PCDM_NAMESPACE + "memberOf");
    }

    @Override
    public Set<String> getHasMember(final Model model, final String subject) {
        return getObjectsOfProperty(model, subject, PCDM_NAMESPACE + "hasMember");
    }

    @Override
    public Set<String> getFileOf(final Model model, final String subject) {
        return getObjectsOfProperty(model, subject, PCDM_NAMESPACE + "fileOf");
    }

    @Override
    public Set<String> getHasFile(final Model model, final String subject) {
        return getObjectsOfProperty(model, subject, PCDM_NAMESPACE + "hasFile");
    }

    @Override
    public Set<String> getRelatedObjectOf(final Model model, final String subject) {
        return getObjectsOfProperty(model, subject, PCDM_NAMESPACE + "relatedObjectOf");
    }

    @Override
    public Set<String> getHasRelatedObject(final Model model, final String subject) {
        return getObjectsOfProperty(model, subject, PCDM_NAMESPACE + "hasRelatedObject");
    }

    @Override
    public InputStream getTriples(final Model model, final String contentType) {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        model.write(os, contentTypeToLang(contentType).getName());
        return new ByteArrayInputStream(os.toByteArray());
    }

    private Set<String> getObjectsOfProperty(final Model model, final String subject, final String property) {
        final InfModel infModel = createInfModel(getOWLMicroReasoner(), model);
        infModel.add(pcdmModel);
        return asStream(infModel.listObjectsOfProperty(createResource(subject), createProperty(property)))
                .filter(RDFNode::isURIResource).map(RDFNode::asResource).map(Resource::getURI).collect(toSet());
    }
}
