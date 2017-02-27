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
package cool.pandora.services.inference;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author acoburn
 * @since 6/16/16
 */
public class InferenceServiceTest {

    private final String TURTLE = "text/turtle";

    private final String subject = "http://localhost:8080/fcrepo/rest/test";

    private final String appType = "urn:app#Complete";

    private final InferenceService svc = new InferenceServiceImpl();

    @Test
    public void testIsEquivalentClass() {
        assertTrue(svc.hasType(subject, appType,
                    getClass().getResourceAsStream("/resource.ttl"), TURTLE,
                    getClass().getResourceAsStream("/isResource.ttl"), TURTLE));
    }

    @Test
    public void testIsNotEquivalentClass() {
        assertFalse(svc.hasType(subject, appType,
                    getClass().getResourceAsStream("/resource.ttl"), TURTLE,
                    getClass().getResourceAsStream("/isBinary.ttl"), TURTLE));
    }

    @Test
    public void testHasValue() {
        assertTrue(svc.hasType(subject, appType,
                    getClass().getResourceAsStream("/resource.ttl"), TURTLE,
                    getClass().getResourceAsStream("/hasValue.ttl"), TURTLE));
    }

    @Test
    public void testCardinality() {
        assertTrue(svc.hasType(subject, appType,
                    getClass().getResourceAsStream("/resource.ttl"), TURTLE,
                    getClass().getResourceAsStream("/hasPrefLabel.ttl"), TURTLE));
    }

    @Test
    public void testResourceSubClass() {
        assertTrue(svc.hasType(subject, appType,
                    getClass().getResourceAsStream("/resource.ttl"), TURTLE,
                    getClass().getResourceAsStream("/isResourceSubClass.ttl"), TURTLE));
    }

    @Test
    public void testIntersectionOf() {
        assertTrue(svc.hasType(subject, appType,
                    getClass().getResourceAsStream("/resource.ttl"), TURTLE,
                    getClass().getResourceAsStream("/intersectionOf.ttl"), TURTLE));
    }

    @Test
    public void testUnionOf() {
        assertTrue(svc.hasType(subject, appType,
                    getClass().getResourceAsStream("/resource.ttl"), TURTLE,
                    getClass().getResourceAsStream("/unionOf.ttl"), TURTLE));
    }

    @Test
    public void testApplicationProfile() {
        assertTrue(svc.hasType(subject, appType,
                    getClass().getResourceAsStream("/resource.ttl"), TURTLE,
                    getClass().getResourceAsStream("/applicationProfile.ttl"), TURTLE));
    }

}
