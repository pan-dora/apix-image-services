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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;

import org.junit.Test;
import org.slf4j.Logger;

/**
 * @author acoburn
 * @since 9/14/15
 */
public class LdCacheServiceTest {

    private static Logger LOGGER = getLogger(LdCacheServiceTest.class);

    private static final String PREF_LABEL = "http://www.w3.org/2004/02/skos/core#prefLabel";
    private static final String LABEL = "http://www.w3.org/2000/01/rdf-schema#label";
    private static final String OFFICIAL_NAME = "http://www.geonames.org/ontology#officialName";

    private static final LDCacheService svc = new LDCacheServiceImpl("/tmp/ldcache");

    @Test
    public void testFetch() throws Exception {

        assertTrue(svc.get("http://dbpedia.org/resource/Berlin", LABEL).contains("Berlin"));
        assertTrue(svc.get("http://sws.geonames.org/2658434/", OFFICIAL_NAME).contains("Switzerland"));
        assertTrue(svc.get("http://vocab.getty.edu/tgn/7003712", PREF_LABEL).contains("Berlin"));
        assertTrue(svc.get("http://id.loc.gov/vocabulary/resourceTypes/txt", PREF_LABEL).contains("Text"));
        assertTrue(svc.get("http://id.loc.gov/authorities/names/n79006936", PREF_LABEL)
                .contains("Melville, Herman, 1819-1891"));
        assertTrue(svc.get("http://purl.org/dc/dcmitype/StillImage", LABEL).contains("Still Image"));
    }

    @Test
    public void testFetchWithLang() throws Exception {

        assertTrue(svc.get("http://dbpedia.org/resource/Berlin", LABEL, "en").contains("Berlin"));
        assertTrue(svc.get("http://sws.geonames.org/2658434/", OFFICIAL_NAME, "en").contains("Switzerland"));
        assertTrue(svc.get("http://vocab.getty.edu/tgn/7003712", PREF_LABEL, "en").contains("Berlin"));
        assertTrue(svc.get("http://id.loc.gov/vocabulary/resourceTypes/txt", PREF_LABEL, "en").contains("Text"));
        assertTrue(svc.get("http://id.loc.gov/authorities/names/n79006936", PREF_LABEL, "en")
                .contains("Melville, Herman, 1819-1891"));
        assertTrue(svc.get("http://purl.org/dc/dcmitype/StillImage", LABEL, "en").contains("Still Image"));
    }

    @Test
    public void testFetchWithLangCount() throws Exception {
        assertEquals(svc.get("http://dbpedia.org/resource/Berlin", LABEL, "en").size(), 1);
        assertEquals(svc.get("http://sws.geonames.org/2658434/", OFFICIAL_NAME, "en").size(), 1);
        assertEquals(svc.get("http://vocab.getty.edu/tgn/7003712", PREF_LABEL, "en").size(), 1);
        assertEquals(svc.get("http://id.loc.gov/vocabulary/resourceTypes/txt", PREF_LABEL, "en").size(), 1);
        assertEquals(svc.get("http://id.loc.gov/authorities/names/n79006936", PREF_LABEL, "en").size(), 1);
        assertEquals(svc.get("http://purl.org/dc/dcmitype/StillImage", LABEL, "en").size(), 1);
    }
}
