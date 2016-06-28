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

import java.io.InputStream;
import java.util.Set;

import org.apache.jena.rdf.model.Model;

/**
 * @author acoburn
 * @since 6/16/16
 */
public interface PcdmService {

    /**
     * Parse an InputStream whether an RDF graph is valid according to a set of OWL restrictions
     *
     * @param input The input RDF graph
     * @param contentType The mimeType of the input
     * @return the RDF Model
     */
    Model parse(final InputStream input, final String contentType);

    /**
     * Parse an InputStream into an existing model
     *
     * @param model the existing model
     * @param input the input RDF graph
     * @param contentType the mimeType of the input
     * @return the RDF Model
     */
    Model parseInto(final Model model, final InputStream input, final String contentType);

    /**
     * Is this a pcdm:Object
     *
     * @param model the model
     * @param subject the subject
     * @return whether the subject is a pcdm:Object
     */
    boolean isObject(final Model model, final String subject);

    /**
     * Is this a pcdm:File
     *
     * @param model the model
     * @param subject the subject
     * @return whether the subject is a pcdm:File
     */
    boolean isFile(final Model model, final String subject);

    /**
     * Is this a pcdm:Collection
     *
     * @param model the model
     * @param subject the subject
     * @return whether the subject is a pcdm:Collection
     */
    boolean isCollection(final Model model, final String subject);

    /**
     * Get the object values for the pcdm:memberOf triples
     *
     * @param model The model
     * @param subject the subject
     * @return a set of object values
     */
    Set<String> getMemberOf(final Model model, final String subject);

    /**
     * Get the object values for the pcdm:hasMember triples
     *
     * @param model The model
     * @param subject the subject
     * @return a set of object values
     */
    Set<String> getHasMember(final Model model, final String subject);

    /**
     * Get the object values for the pcdm:fileOf triples
     *
     * @param model The model
     * @param subject the subject
     * @return a set of object values
     */
    Set<String> getFileOf(final Model model, final String subject);

    /**
     * Get the object values for the pcdm:hasFile triples
     *
     * @param model The model
     * @param subject the subject
     * @return a set of object values
     */
    Set<String> getHasFile(final Model model, final String subject);

    /**
     * Get the object values for the pcdm:relatedObjectOf triples
     *
     * @param model The model
     * @param subject the subject
     * @return a set of object values
     */
    Set<String> getRelatedObjectOf(final Model model, final String subject);

    /**
     * Get the object values for the pcdm:hasRelatedObject triples
     *
     * @param model The model
     * @param subject the subject
     * @return a set of object values
     */
    Set<String> getHasRelatedObject(final Model model, final String subject);

    /**
     * Get the triples from this model
     *
     * @param model the model
     * @param contentType the contentType
     * @return the RDF serialization
     */
    InputStream getTriples(final Model model, final String contentType);
}
