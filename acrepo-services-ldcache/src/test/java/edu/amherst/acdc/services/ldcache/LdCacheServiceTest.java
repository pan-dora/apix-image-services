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
import static org.junit.Assume.assumeTrue;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

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
    public void testFetchDbpedia() throws Exception {

        final List<String> dbpedia = svc.get("http://dbpedia.org/resource/Berlin", LABEL);
        assumeTrue(dbpedia.size() > 0);

        assertTrue(dbpedia.contains("Berlin"));
    }

    @Test
    public void testFetchGeonames() throws Exception {
        final List<String> geonames = svc.get("http://sws.geonames.org/2658434/", OFFICIAL_NAME);
        assumeTrue(geonames.size() > 0);

        assertTrue(geonames.contains("Switzerland"));
    }

    @Test
    public void testFetchGetty() throws Exception {

        final List<String> getty = svc.get("http://vocab.getty.edu/tgn/7003712", PREF_LABEL);

        assumeTrue(getty.size() > 0);
        assertTrue(getty.contains("Berlin"));
    }

    @Test
    public void testFetchResourceTypes() throws Exception {

        final List<String> types = svc.get("http://id.loc.gov/vocabulary/resourceTypes/txt", PREF_LABEL);

        assumeTrue(types.size() > 0);
        assertTrue(types.contains("Text"));
    }

    @Test
    public void testFetchLocName() throws Exception {

        final List<String> name = svc.get("http://id.loc.gov/authorities/names/n79006936", PREF_LABEL);

        assumeTrue(name.size() > 0);
        assertTrue(name.contains("Melville, Herman, 1819-1891"));
    }

    @Test
    public void testDcmiTypes() throws Exception {

        final List<String> types = svc.get("http://purl.org/dc/dcmitype/StillImage", LABEL);

        assumeTrue(types.size() > 0);
        assertTrue(types.contains("Still Image"));
    }

    @Test
    public void testFetchDbpediaWithLang() throws Exception {

        final List<String> dbpedia = svc.get("http://dbpedia.org/resource/Berlin", LABEL, "en");
        assumeTrue(dbpedia.size() > 0);

        assertEquals(dbpedia.size(), 1);
        assertTrue(dbpedia.contains("Berlin"));
    }

    @Test
    public void testFetchGeonamesWithLang() throws Exception {
        final List<String> geonames = svc.get("http://sws.geonames.org/2658434/", OFFICIAL_NAME, "en");
        assumeTrue(geonames.size() > 0);

        assertEquals(geonames.size(), 1);
        assertTrue(geonames.contains("Switzerland"));
    }

    @Test
    public void testFetchGettyWithLang() throws Exception {

        final List<String> getty = svc.get("http://vocab.getty.edu/tgn/7003712", PREF_LABEL, "en");

        assumeTrue(getty.size() > 0);

        assertEquals(getty.size(), 1);
        assertTrue(getty.contains("Berlin"));
    }

    @Test
    public void testFetchResourceTypesWithLang() throws Exception {

        final List<String> types = svc.get("http://id.loc.gov/vocabulary/resourceTypes/txt", PREF_LABEL, "en");

        assumeTrue(types.size() > 0);

        assertEquals(types.size(), 1);
        assertTrue(types.contains("Text"));
    }

    @Test
    public void testFetchLocNameWithLang() throws Exception {

        final List<String> name = svc.get("http://id.loc.gov/authorities/names/n79006936", PREF_LABEL, "en");

        assumeTrue(name.size() > 0);

        assertEquals(name.size(), 1);
        assertTrue(name.contains("Melville, Herman, 1819-1891"));
    }

    @Test
    public void testDcmiTypesWithLang() throws Exception {

        final List<String> types = svc.get("http://purl.org/dc/dcmitype/StillImage", LABEL, "en");

        assumeTrue(types.size() > 0);

        assertEquals(types.size(), 1);
        assertTrue(types.contains("Still Image"));
    }
}
