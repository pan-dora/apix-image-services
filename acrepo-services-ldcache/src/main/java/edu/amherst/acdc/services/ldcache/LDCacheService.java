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
package edu.amherst.acdc.services.ldcache;

import java.util.List;

/**
 * @author acoburn
 * @since 6/21/16
 */
public interface LDCacheService {

    /**
     * Get the object values for the URI
     *
     * @param subject the subject URI
     * @param predicate the predicate URI
     * @param lang the language tag for literals
     * @return the object value
     */
    List<String> get(final String subject, final String predicate, final String lang);

    /**
     * Get the object values for the URI
     *
     * @param subject the subject URI
     * @param predicate the predicate URI
     * @return the object value
     */
    default List<String> get(final String subject, final String predicate) {
        return get(subject, predicate, null);
    }

}
