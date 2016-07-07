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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.jena.rdf.model.ModelFactory.createDefaultModel;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Set;

import org.apache.camel.CamelContext;
import org.apache.jena.rdf.model.Model;
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
public class AcrepoPcdmIT extends AbstractOSGiIT {

    private static Logger LOGGER = getLogger(AcrepoPcdmIT.class);

    @Configuration
    public Option[] config() {
        final ConfigurationManager cm = new ConfigurationManager();
        final String fcrepoPort = cm.getProperty("fcrepo.dynamic.test.port");
        final String pcdmExtPort = cm.getProperty("karaf.pcdm.port");
        final String rmiRegistryPort = cm.getProperty("karaf.rmiRegistry.port");
        final String rmiServerPort = cm.getProperty("karaf.rmiServer.port");
        final String fcrepoBaseUrl = "http://localhost:" + fcrepoPort + "/fcrepo/rest";
        final String sshPort = cm.getProperty("karaf.ssh.port");

        return new Option[] {
            karafDistributionConfiguration()
                .frameworkUrl(maven().groupId("org.apache.karaf").artifactId("apache-karaf")
                        .versionAsInProject().type("zip"))
                .unpackDirectory(new File("target", "exam"))
                .useDeployFolder(false),
            logLevel(LogLevel.INFO),
            keepRuntimeFolder(),
            configureConsole().ignoreLocalConsole(),
            features(maven().groupId("org.apache.karaf.features").artifactId("standard")
                        .versionAsInProject().classifier("features").type("xml"), "scr"),
            features(maven().groupId("edu.amherst.acdc").artifactId("acrepo-karaf")
                        .type("xml").classifier("features").versionAsInProject(),
                    "acrepo-exts-pcdm"),

            systemProperty("karaf.pcdm.port").value(pcdmExtPort),
            systemProperty("fcrepo.port").value(fcrepoPort),

            editConfigurationFilePut("etc/org.apache.karaf.management.cfg", "rmiRegistryPort", rmiRegistryPort),
            editConfigurationFilePut("etc/org.apache.karaf.management.cfg", "rmiServerPort", rmiServerPort),
            editConfigurationFilePut("etc/org.apache.karaf.shell.cfg", "sshPort", sshPort),
            editConfigurationFilePut("etc/edu.amherst.acdc.exts.pcdm.cfg", "fcrepo.baseUrl", fcrepoBaseUrl),
            editConfigurationFilePut("etc/edu.amherst.acdc.exts.pcdm.cfg", "rest.port", pcdmExtPort)
       };
    }

    @Test
    public void testInstallation() throws Exception {
        assertTrue(featuresService.isInstalled(featuresService.getFeature("camel-core")));
        assertTrue(featuresService.isInstalled(featuresService.getFeature("fcrepo-camel")));
        assertTrue(featuresService.isInstalled(featuresService.getFeature("acrepo-exts-pcdm")));
        assertTrue(featuresService.isInstalled(featuresService.getFeature("acrepo-services-pcdm")));
    }

    @Test
    public void testPcdmOptions() throws Exception {
        // make sure that the camel context has started up.
        final CamelContext ctx = getOsgiService(CamelContext.class, "(camel.context.name=AcrepoExtPcdm)",
                10000);
        assertNotNull(ctx);

        final String baseUrl = "http://localhost:" + System.getProperty("fcrepo.port") + "/fcrepo/rest";
        final String baseSvcUrl = "http://localhost:" + System.getProperty("karaf.pcdm.port") + "/pcdm";

        assertTrue(options(baseSvcUrl).contains("apix:bindsTo pcdm:Object"));
    }

    @Test
    public void testPcdmObjectTurtle() throws Exception {
        // make sure that the camel context has started up.
        final CamelContext ctx = getOsgiService(CamelContext.class, "(camel.context.name=AcrepoExtPcdm)",
                10000);
        assertNotNull(ctx);

        final String baseUrl = "http://localhost:" + System.getProperty("fcrepo.port") + "/fcrepo/rest";
        final String baseSvcUrl = "http://localhost:" + System.getProperty("karaf.pcdm.port") + "/pcdm";

        final String pcdmObj = post(baseUrl, getClass().getResourceAsStream("/resource.ttl"), "text/turtle");
        final String members = pcdmObj + "/members";
        assertTrue(put(members, getClass().getResourceAsStream("/members.ttl"), "text/turtle"));

        final String page1 = post(members);
        final String page2 = post(members);
        final String page3 = post(members);
        final String page4 = post(members);

        final String response = get(baseSvcUrl + pcdmObj.replace(baseUrl, ""));
        final InputStream input = new ByteArrayInputStream(response.getBytes(UTF_8));
        final Model model = createDefaultModel();

        model.read(input, null, "TTL");

        final Set<String> subjects = model.listSubjects().mapWith(x -> x.getURI()).toSet();
        assertTrue(subjects.contains(pcdmObj));
        assertTrue(subjects.contains(page1));
        assertTrue(subjects.contains(page2));
        assertTrue(subjects.contains(page3));
        assertTrue(subjects.contains(page4));
        assertFalse(subjects.contains(members));
    }

    @Test
    public void testPcdmObjectJsonLD() throws Exception {
        // make sure that the camel context has started up.
        final CamelContext ctx = getOsgiService(CamelContext.class, "(camel.context.name=AcrepoExtPcdm)",
                10000);
        assertNotNull(ctx);

        final String baseUrl = "http://localhost:" + System.getProperty("fcrepo.port") + "/fcrepo/rest";
        final String baseSvcUrl = "http://localhost:" + System.getProperty("karaf.pcdm.port") + "/pcdm";

        final String pcdmObj = post(baseUrl, getClass().getResourceAsStream("/resource.ttl"), "text/turtle");
        final String members = pcdmObj + "/members";
        assertTrue(put(members, getClass().getResourceAsStream("/members.ttl"), "text/turtle"));

        final String page1 = post(members);
        final String page2 = post(members);
        final String page3 = post(members);
        final String page4 = post(members);

        final String response = get(baseSvcUrl + pcdmObj.replace(baseUrl, ""), "application/ld+json");
        LOGGER.info(response);

        final InputStream input = new ByteArrayInputStream(response.getBytes(UTF_8));
        final Model model = createDefaultModel();

        model.read(input, null, "JSONLD");

        final Set<String> subjects = model.listSubjects().mapWith(x -> x.getURI()).toSet();
        assertTrue(subjects.contains(pcdmObj));
        assertTrue(subjects.contains(page1));
        assertTrue(subjects.contains(page2));
        assertTrue(subjects.contains(page3));
        assertTrue(subjects.contains(page4));
        assertFalse(subjects.contains(members));
    }
}
