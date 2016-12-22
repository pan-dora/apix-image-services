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

import static javax.xml.parsers.DocumentBuilderFactory.newInstance;
import static org.junit.Assert.assertEquals;
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
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.apache.camel.CamelContext;
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
public class AcrepoExtsSerializeXmlIT extends AbstractOSGiIT {

    private static Logger LOGGER = getLogger(AcrepoExtsSerializeXmlIT.class);

    @Configuration
    public Option[] config() {
        final ConfigurationManager cm = new ConfigurationManager();
        final String fcrepoPort = cm.getProperty("fcrepo.dynamic.test.port");
        final String metadataServicePort = cm.getProperty("karaf.metadata.port");
        final String rmiRegistryPort = cm.getProperty("karaf.rmiRegistry.port");
        final String rmiServerPort = cm.getProperty("karaf.rmiServer.port");
        final String fcrepoBaseUrl = "http://localhost:" + fcrepoPort + "/fcrepo/rest";
        final String sshPort = cm.getProperty("karaf.ssh.port");

        return new Option[] {
            karafDistributionConfiguration()
                .frameworkUrl(maven().groupId("org.apache.karaf").artifactId("apache-karaf")
                        .version(cm.getProperty("karaf.version")).type("zip"))
                .unpackDirectory(new File("build", "exam"))
                .useDeployFolder(false),
            logLevel(LogLevel.INFO),
            keepRuntimeFolder(),
            configureConsole().ignoreLocalConsole(),
            features(maven().groupId("org.apache.karaf.features").artifactId("standard")
                        .versionAsInProject().classifier("features").type("xml"), "scr"),
            features(maven().groupId("org.apache.camel.karaf").artifactId("apache-camel")
                        .type("xml").classifier("features").versionAsInProject()),
            features(maven().groupId("org.fcrepo.camel").artifactId("fcrepo-camel")
                        .type("xml").classifier("features").versionAsInProject()),
            features(maven().groupId("edu.amherst.acdc").artifactId("acrepo-karaf")
                        .type("xml").classifier("features").versionAsInProject(),
                    "acrepo-exts-serialize-xml"),

            systemProperty("karaf.metadata.port").value(metadataServicePort),
            systemProperty("fcrepo.port").value(fcrepoPort),

            editConfigurationFilePut("etc/org.apache.karaf.management.cfg", "rmiRegistryPort", rmiRegistryPort),
            editConfigurationFilePut("etc/org.apache.karaf.management.cfg", "rmiServerPort", rmiServerPort),
            editConfigurationFilePut("etc/org.apache.karaf.shell.cfg", "sshPort", sshPort),
            editConfigurationFilePut("etc/edu.amherst.acdc.exts.serialize.xml.cfg", "fcrepo.baseUrl", fcrepoBaseUrl),
            editConfigurationFilePut("etc/edu.amherst.acdc.exts.serialize.xml.cfg", "rest.port", metadataServicePort),
            editConfigurationFilePut("etc/edu.amherst.acdc.exts.serialize.xml.cfg", "extension.load", "false")
       };
    }

    @Test
    public void testInstallation() throws Exception {
        assertTrue(featuresService.isInstalled(featuresService.getFeature("camel-core")));
        assertTrue(featuresService.isInstalled(featuresService.getFeature("fcrepo-camel")));
        assertTrue(featuresService.isInstalled(featuresService.getFeature("acrepo-exts-serialize-xml")));
    }

    @Test
    public void testDcMetadataService() throws Exception {
        // make sure that the camel context has started up.
        final CamelContext ctx = getOsgiService(CamelContext.class, "(camel.context.name=AcrepoExtsSerializeXml)",
                10000);
        assertNotNull(ctx);

        final String baseUrl = "http://localhost:" + System.getProperty("fcrepo.port") + "/fcrepo/rest";
        final String baseSvcUrl = "http://localhost:" + System.getProperty("karaf.metadata.port") + "/xml";

        assertTrue(options(baseSvcUrl).contains("apix:bindsTo fedora:Resource"));

        final String id = post(baseUrl, getClass().getResourceAsStream("/resource.ttl"), "text/turtle")
                .replace(baseUrl, "");

        final InputStream input = new ByteArrayInputStream(get(baseSvcUrl + "/dc" + id).getBytes("UTF-8"));
        final Document doc = newInstance().newDocumentBuilder().parse(input);

        final NodeList root = doc.getChildNodes();

        assertEquals(1, root.getLength());
        final NodeList properties = root.item(0).getChildNodes();

        final List<String> types = new ArrayList<>();
        for (int i = 0; i < properties.getLength(); i++) {
            final Node node = properties.item(i);
            if (node.getNodeName().equals("dc:type")) {
                types.add(node.getTextContent());
            }
        }

        assertTrue(types.contains("http://id.loc.gov/vocabulary/resourceTypes/txt"));
        assertTrue(types.contains("http://vocab.getty.edu/aat/30002491"));
        assertTrue(types.contains("http://purl.org/dc/dcmitype/StillImage"));
    }
}
