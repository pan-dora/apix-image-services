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

import java.io.InputStream;

/**
 * @author acoburn
 * @since 6/16/16
 */
public interface ValidationService {

    /**
     *  Determine whether an RDF graph is valid according to a set of OWL restrictions
     *
     *  @param subject The subject of the RDF graph
     *  @param input The input RDF graph
     *  @param contentType The mimeType of the input
     *  @param restrictions any OWL restrictions
     *  @param restrictionFormat the mimeType of the restriction graph
     *  @return whether the input document is valid
     */
    boolean validate(final String subject, final InputStream input, final String contentType,
            final InputStream restrictions, final String restrictionFormat);
}
