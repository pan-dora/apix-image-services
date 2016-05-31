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
package edu.amherst.acdc.jsonld;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author acoburn
 * @since 9/14/15
 */
public class JsonLdServiceTest {

    @Test
    public void testCompact() {
        final JsonLdService jsonProcessor = new JsonLdServiceImpl();

        final String response = jsonProcessor.compact(
                JsonLdServiceTest.class.getResourceAsStream("/expanded.json"),
                JsonLdServiceTest.class.getResourceAsStream("/context.json"));

        final String compact = "{" +
                "\"@id\":\"http://me.markus-lanthaler.com/\"," +
                "\"website\":{\"@id\":\"http://www.markus-lanthaler.com/\"}," +
                "\"http://xmlns.com/foaf/0.1/name\":\"Markus Lanthaler\"," +
                "\"@context\":{\"website\":\"http://xmlns.com/foaf/0.1/homepage\"}}";

        assertEquals(compact, response);
    }

    @Test
    public void testExpand() {
        final JsonLdService jsonProcessor = new JsonLdServiceImpl();

        final String response = jsonProcessor.expand(
                JsonLdServiceTest.class.getResourceAsStream("/compact.json"));

        final String expanded = "[{" +
            "\"@id\":\"http://me.markus-lanthaler.com/\"," +
            "\"http://xmlns.com/foaf/0.1/homepage\":[{\"@id\":\"http://www.markus-lanthaler.com/\"}]," +
            "\"http://xmlns.com/foaf/0.1/name\":[{\"@value\":\"Markus Lanthaler\"}]}]";
        assertEquals(expanded, response);
    }

}
