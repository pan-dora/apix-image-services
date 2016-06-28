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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import org.apache.jena.rdf.model.Model;
import org.junit.Test;

/**
 * @author acoburn
 * @since 9/14/15
 */
public class PcdmServiceTest {

    private final PcdmService svc = new PcdmServiceImpl();

    private static InputStream getPcdmFile(final String uri) {
        final String triples = "@prefix premis: <http://www.loc.gov/premis/rdf/v1#> .\n" +
            "@prefix pcdm: <http://pcdm.org/models#> .\n" +
            "@prefix fedora: <http://fedora.info/definitions/v4/repository#> .\n" +
            "@prefix ebucore: <http://www.ebu.ch/metadata/ontologies/ebucore/ebucore#> .\n" +
            "@prefix ldp: <http://www.w3.org/ns/ldp#> .\n" +
            "@prefix iana: <http://www.iana.org/assignments/relation/> .\n" +
            "<" + uri + "> a fedora:Binary , fedora:Resource , ldp:NonRDFSource, pcdm:File ;\n" +
            "premis:hasSize \"1565421\"^^<http://www.w3.org/2001/XMLSchema#long> ;\n" +
            "premis:hasMessageDigest <urn:sha1:c9790c378f3589bfcfe092f834d212685f44fff9> ;\n" +
            "ebucore:hasMimeType \"image/jpeg\"^^<http://www.w3.org/2001/XMLSchema#string> ;\n" +
            "ebucore:filename \"image.jpg\"^^<http://www.w3.org/2001/XMLSchema#string> ;\n" +
            "iana:describedby <" + uri + "/fcr:metadata> ;\n" +
            "fedora:createdBy \"bypassAdmin\"^^<http://www.w3.org/2001/XMLSchema#string> ;\n" +
            "fedora:created \"2016-06-28T00:21:22.621Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> ;\n" +
            "fedora:lastModified \"2016-06-28T00:21:22.621Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> ;\n" +
            "fedora:lastModifiedBy \"bypassAdmin\"^^<http://www.w3.org/2001/XMLSchema#string> ;\n" +
            "fedora:writable \"true\"^^<http://www.w3.org/2001/XMLSchema#boolean> ;\n" +
            "fedora:hasParent <http://localhost:8080/fcrepo/rest/> ;\n" +
            "fedora:numberOfChildren \"0\"^^<http://www.w3.org/2001/XMLSchema#long> ;\n" +
            "fedora:hasFixityService <" + uri + "/fcr:fixity> .";
        return new ByteArrayInputStream(triples.getBytes(UTF_8));
    }

    private static InputStream getPcdmObject(final String uri, final List<String> members, final List<String> files) {
        final StringJoiner joiner = new StringJoiner("", "@prefix premis: <http://www.loc.gov/premis/rdf/v1#> .\n" +
            "@prefix pcdm: <http://pcdm.org/models#> .\n" +
            "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n" +
            "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
            "@prefix fedora: <http://fedora.info/definitions/v4/repository#> .\n" +
            "@prefix ebucore: <http://www.ebu.ch/metadata/ontologies/ebucore/ebucore#> .\n" +
            "@prefix ldp: <http://www.w3.org/ns/ldp#> .\n" +
            "@prefix iana: <http://www.iana.org/assignments/relation/> .\n" +
            "@prefix dc: <http://purl.org/dc/elements/1.1/> .\n" +
            "<" + uri + "> a fedora:Container , fedora:Resource , pcdm:Object , ldp:RDFSource , ldp:Container ;\n" +
            "fedora:lastModifiedBy \"bypassAdmin\"^^<http://www.w3.org/2001/XMLSchema#string> ;\n" +
            "fedora:createdBy \"bypassAdmin\"^^<http://www.w3.org/2001/XMLSchema#string> ;\n" +
            "fedora:created \"2016-06-28T00:14:06.677Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> ;\n" +
            "fedora:lastModified \"2016-06-28T00:16:25.55Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> ;\n" +
            "fedora:writable \"true\"^^<http://www.w3.org/2001/XMLSchema#boolean> ;\n" +
            "fedora:hasParent <http://localhost:8080/fcrepo/rest/> ;\n" +
            "fedora:numberOfChildren \"2\"^^<http://www.w3.org/2001/XMLSchema#long> ;\n",
            "ldp:contains <" + uri + "/files> , <" + uri + "/members> .\n");
        members.forEach(member -> {
            joiner.add("pcdm:hasMember <" + uri + "/members/" + member + "> ;\n");
        });

        files.forEach(file -> {
            joiner.add("pcdm:hasFile <" + uri + "/files/" + file + "> ;\n");
        });
        return new ByteArrayInputStream(joiner.toString().getBytes(UTF_8));
    }

    private static InputStream getPcdmCollection(final String uri, final List<String> members) {
        final StringJoiner joiner = new StringJoiner("", "@prefix premis: <http://www.loc.gov/premis/rdf/v1#> .\n" +
            "@prefix pcdm: <http://pcdm.org/models#> .\n" +
            "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n" +
            "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
            "@prefix fedora: <http://fedora.info/definitions/v4/repository#> .\n" +
            "@prefix ebucore: <http://www.ebu.ch/metadata/ontologies/ebucore/ebucore#> .\n" +
            "@prefix ldp: <http://www.w3.org/ns/ldp#> .\n" +
            "@prefix iana: <http://www.iana.org/assignments/relation/> .\n" +
            "@prefix dc: <http://purl.org/dc/elements/1.1/> .\n" +
            "<" + uri + "> a fedora:Container , fedora:Resource , pcdm:Collection , ldp:RDFSource , ldp:Container ;\n" +
            "fedora:lastModifiedBy \"bypassAdmin\"^^<http://www.w3.org/2001/XMLSchema#string> ;\n" +
            "fedora:createdBy \"bypassAdmin\"^^<http://www.w3.org/2001/XMLSchema#string> ;\n" +
            "fedora:created \"2016-06-28T00:14:06.677Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> ;\n" +
            "fedora:lastModified \"2016-06-28T00:16:25.55Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> ;\n" +
            "fedora:writable \"true\"^^<http://www.w3.org/2001/XMLSchema#boolean> ;\n" +
            "fedora:hasParent <http://localhost:8080/fcrepo/rest/> ;\n" +
            "fedora:numberOfChildren \"1\"^^<http://www.w3.org/2001/XMLSchema#long> ;\n",
            "ldp:contains <" + uri + "/members> .\n");
        members.forEach(member -> {
            joiner.add("pcdm:hasMember <" + uri + "/members/" + member + "> ;\n");
        });
        return new ByteArrayInputStream(joiner.toString().getBytes(UTF_8));
    }

    @Test
    public void testParseFile() {
        final String uri = "http://localhost:8080/fcrepo/rest/pcdm/file";
        final Model m = svc.parse(getPcdmFile(uri), "text/turtle");
        assertNotNull(m);
        assertTrue(svc.isFile(m, uri));
        assertFalse(svc.isObject(m, uri));
        assertFalse(svc.isCollection(m, uri));
        assertEquals(svc.getMemberOf(m, uri).size(), 0);
        assertEquals(svc.getHasMember(m, uri).size(), 0);
        assertEquals(svc.getFileOf(m, uri).size(), 0);
        assertEquals(svc.getHasFile(m, uri).size(), 0);
        assertEquals(svc.getRelatedObjectOf(m, uri).size(), 0);
        assertEquals(svc.getHasRelatedObject(m, uri).size(), 0);
        assertNotNull(svc.getTriples(m, "text/turtle"));
    }

    @Test
    public void testParseObject() {
        final String uri = "http://localhost:8080/fcrepo/rest/pcdm/object";
        final List<String> members = new ArrayList<>();
        members.add("page1");
        members.add("page2");
        members.add("page3");
        final List<String> files = new ArrayList<>();
        files.add("file1");
        files.add("file2");
        files.add("file3");
        final Model m = svc.parse(getPcdmObject(uri, members, files), "text/turtle");
        assertNotNull(m);
        assertFalse(svc.isFile(m, uri));
        assertTrue(svc.isObject(m, uri));
        assertFalse(svc.isCollection(m, uri));
        assertEquals(svc.getMemberOf(m, uri).size(), 0);
        assertEquals(svc.getHasMember(m, uri).size(), members.size());
        assertEquals(svc.getFileOf(m, uri).size(), 0);
        assertEquals(svc.getHasFile(m, uri).size(), files.size());
        assertEquals(svc.getRelatedObjectOf(m, uri).size(), 0);
        assertEquals(svc.getHasRelatedObject(m, uri).size(), 0);
        assertNotNull(svc.getTriples(m, "text/turtle"));
        // test inference
        members.forEach(member -> {
            assertEquals(1, svc.getMemberOf(m, uri + "/members/" + member).size());
            svc.getMemberOf(m, uri + "/members/" + member).forEach(parent -> {
                assertEquals(parent, uri);
            });
        });
        files.forEach(file -> {
            assertEquals(svc.getFileOf(m, uri + "/files/" + file).size(), 1);
            svc.getFileOf(m, uri + "/files/" + file).forEach(parent -> {
                assertEquals(parent, uri);
            });
        });
    }

    @Test
    public void testParseCollection() {
        final String uri = "http://localhost:8080/fcrepo/rest/pcdm/collection";
        final List<String> members = new ArrayList<>();
        members.add("obj1");
        members.add("obj2");
        members.add("obj3");
        final Model m = svc.parse(getPcdmCollection(uri, members), "text/turtle");
        assertNotNull(m);
        assertFalse(svc.isFile(m, uri));
        assertFalse(svc.isObject(m, uri));
        assertTrue(svc.isCollection(m, uri));
        assertEquals(svc.getMemberOf(m, uri).size(), 0);
        assertEquals(svc.getHasMember(m, uri).size(), members.size());
        assertEquals(svc.getFileOf(m, uri).size(), 0);
        assertEquals(svc.getHasFile(m, uri).size(), 0);
        assertEquals(svc.getRelatedObjectOf(m, uri).size(), 0);
        assertEquals(svc.getHasRelatedObject(m, uri).size(), 0);
        assertNotNull(svc.getTriples(m, "text/turtle"));
        // Test inference
        members.forEach(member -> {
            assertEquals(1, svc.getMemberOf(m, uri + "/members/" + member).size());
            svc.getMemberOf(m, uri + "/members/" + member).forEach(parent -> {
                assertEquals(parent, uri);
            });
        });
    }
}
