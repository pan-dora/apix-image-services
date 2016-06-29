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

/**
 * Some header field definitions
 *
 * @author acoburn
 */
class PcdmHeaders {

    public final static String PCDM_MODEL = "CamelPcdmModel";
    public final static String PCDM_SUBJECT = "CamelPcdmSubject";
    public final static String PCDM_MEMBERS = "CamelPcdmMembers";
    public final static String PCDM_FILES = "CamelPcdmFiles";
    public final static String PCDM_RELATED_OBJECTS = "CamelPcdmRelatedObjects";

    private PcdmHeaders() {
        // prevent instantiation
    }
}
