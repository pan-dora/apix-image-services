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
package edu.amherst.acdc.itests;

import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.configureConsole;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.editConfigurationFilePut;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.logLevel;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;

import edu.amherst.acdc.services.ldcache.LDCacheService;
import edu.amherst.acdc.services.ldcache.LDCacheServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.ConfigurationManager;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.LogLevelOption.LogLevel;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.slf4j.Logger;

/**
 * @author Aaron Coburn
 * @since May 2, 2016
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class AcrepoLdCacheIT extends AbstractOSGiIT {

    private static final String PREF_LABEL = "http://www.w3.org/2004/02/skos/core#prefLabel";
    private static final String LABEL = "http://www.w3.org/2000/01/rdf-schema#label";
    private static final String OFFICIAL_NAME = "http://www.geonames.org/ontology#officialName";

    private static final Logger LOGGER = getLogger(AcrepoLdCacheIT.class);
    private static final LDCacheService svc = new LDCacheServiceImpl("target/ldcache", 100000);

    @Configuration
    public Option[] config() {
        final ConfigurationManager cm = new ConfigurationManager();
        final String fcrepoPort = cm.getProperty("fcrepo.dynamic.test.port");
        final String rmiRegistryPort = cm.getProperty("karaf.rmiRegistry.port");
        final String rmiServerPort = cm.getProperty("karaf.rmiServer.port");
        final String fcrepoBaseUrl = "localhost:" + fcrepoPort + "/fcrepo/rest";
        final String sshPort = cm.getProperty("karaf.ssh.port");

        return new Option[] {
            karafDistributionConfiguration()
                .frameworkUrl(maven().groupId("org.apache.karaf").artifactId("apache-karaf")
                        .versionAsInProject().type("zip"))
                .unpackDirectory(new File("target", "exam"))
                .useDeployFolder(false),
            logLevel(LogLevel.WARN),
            keepRuntimeFolder(),
            configureConsole().ignoreLocalConsole(),
            features(maven().groupId("org.apache.karaf.features").artifactId("standard")
                        .versionAsInProject().classifier("features").type("xml"), "scr", "wrap"),
            features(maven().groupId("edu.amherst.acdc").artifactId("acrepo-karaf")
                        .type("xml").classifier("features").versionAsInProject(),
                    "acrepo-services-ldcache"),

            systemProperty("fcrepo.port").value(fcrepoPort),

            editConfigurationFilePut("etc/org.apache.karaf.management.cfg", "rmiRegistryPort", rmiRegistryPort),
            editConfigurationFilePut("etc/org.apache.karaf.management.cfg", "rmiServerPort", rmiServerPort),
            editConfigurationFilePut("etc/org.apache.karaf.shell.cfg", "sshPort", sshPort)
       };
    }

    @Test
    public void testInstallation() throws Exception {
        assertTrue(featuresService.isInstalled(featuresService.getFeature("acrepo-services-ldcache")));
    }

    @Test
    public void testLdCacheService() throws Exception {

        assertTrue(svc.get("http://dbpedia.org/resource/Berlin", LABEL).contains("Berlin"));
        assertTrue(svc.get("http://sws.geonames.org/2658434/", OFFICIAL_NAME).contains("Switzerland"));
        assertTrue(svc.get("http://id.loc.gov/vocabulary/resourceTypes/txt", PREF_LABEL).contains("Text"));
        assertTrue(svc.get("http://id.loc.gov/authorities/names/n79006936", PREF_LABEL)
                .contains("Melville, Herman, 1819-1891"));
        assertTrue(svc.get("http://purl.org/dc/dcmitype/StillImage", LABEL).contains("Still Image"));
        assertTrue(svc.get("http://vocab.getty.edu/tgn/7003712", PREF_LABEL).contains("Berlin"));
    }

    @Test
    public void testLdCacheServiceWithLang() throws Exception {

        assertTrue(svc.get("http://dbpedia.org/resource/Berlin", LABEL, "en").contains("Berlin"));
        assertTrue(svc.get("http://sws.geonames.org/2658434/", OFFICIAL_NAME, "en").contains("Switzerland"));
        assertTrue(svc.get("http://id.loc.gov/vocabulary/resourceTypes/txt", PREF_LABEL, "en").contains("Text"));
        assertTrue(svc.get("http://id.loc.gov/authorities/names/n79006936", PREF_LABEL, "en")
                .contains("Melville, Herman, 1819-1891"));
        assertTrue(svc.get("http://purl.org/dc/dcmitype/StillImage", LABEL, "en").contains("Still Image"));
        assertTrue(svc.get("http://vocab.getty.edu/tgn/7003712", PREF_LABEL, "en").contains("Berlin"));
    }
}
