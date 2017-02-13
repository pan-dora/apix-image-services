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

import static java.util.stream.IntStream.range;
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

import java.io.File;

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
public class AcrepoTemplateIT extends AbstractOSGiIT {

    private static Logger LOGGER = getLogger(AcrepoTemplateIT.class);

    @Configuration
    public Option[] config() {
        final ConfigurationManager cm = new ConfigurationManager();
        final String fcrepoPort = cm.getProperty("fcrepo.dynamic.test.port");
        final String templateServicePort = cm.getProperty("karaf.template.port");
        final String rmiRegistryPort = cm.getProperty("karaf.rmiRegistry.port");
        final String rmiServerPort = cm.getProperty("karaf.rmiServer.port");
        final String fcrepoBaseUrl = "http://localhost:" + fcrepoPort + "/fcrepo/rest";
        final String ldpathPort = cm.getProperty("karaf.ldpath.port");
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
            features(maven().groupId("org.fcrepo.camel").artifactId("toolbox-features")
                        .type("xml").classifier("features").versionAsInProject()),
            features(maven().groupId("edu.amherst.acdc").artifactId("acrepo-karaf")
                        .type("xml").classifier("features").versionAsInProject(),
                    "acrepo-exts-template", "acrepo-exts-ldpath"),

            systemProperty("karaf.template.port").value(templateServicePort),
            systemProperty("fcrepo.port").value(fcrepoPort),

            editConfigurationFilePut("etc/org.apache.karaf.management.cfg", "rmiRegistryPort", rmiRegistryPort),
            editConfigurationFilePut("etc/org.apache.karaf.management.cfg", "rmiServerPort", rmiServerPort),
            editConfigurationFilePut("etc/org.apache.karaf.shell.cfg", "sshPort", sshPort),
            editConfigurationFilePut("etc/edu.amherst.acdc.exts.ldpath.cfg", "rest.port", ldpathPort),
            editConfigurationFilePut("etc/edu.amherst.acdc.exts.template.cfg", "fcrepo.baseUrl", fcrepoBaseUrl),
            editConfigurationFilePut("etc/edu.amherst.acdc.exts.template.cfg", "rest.port", templateServicePort),
            editConfigurationFilePut("etc/edu.amherst.acdc.exts.template.cfg", "ldpath.serviceUrl",
                    "http://localhost:" + ldpathPort + "/ldpath")
       };
    }

    @Test
    public void testInstallation() throws Exception {
        assertTrue(featuresService.isInstalled(featuresService.getFeature("camel-core")));
        assertTrue(featuresService.isInstalled(featuresService.getFeature("fcrepo-camel")));
        assertTrue(featuresService.isInstalled(featuresService.getFeature("acrepo-exts-template")));
        assertTrue(featuresService.isInstalled(featuresService.getFeature("acrepo-exts-ldpath")));
    }

    @Test
    public void testTemplateService() throws Exception {
        // make sure that the camel context has started up.
        assertNotNull(getOsgiService(CamelContext.class, "(camel.context.name=AcrepoTemplateService)", 10000));
        assertNotNull(getOsgiService(CamelContext.class, "(camel.context.name=AcrepoLDPathContext)", 10000));

        final String baseUrl = "http://localhost:" + System.getProperty("fcrepo.port") + "/fcrepo/rest";
        final String baseSvcUrl = "http://localhost:" + System.getProperty("karaf.template.port") + "/template";

        // Wait 10 seconds
        Thread.sleep(10000);

        assertTrue(options(baseSvcUrl).contains("apix:bindsTo fedora:Resource"));

        range(1, 2).mapToObj(x -> post(baseUrl)).forEach(url -> {
            patch(url, "PREFIX dcterms: <http://purl.org/dc/terms/>\n" +
                "INSERT { <> dcterms:title \"Title\" ; dcterms:description \"A description: " + url + "\" } " +
                "WHERE {}");

            final String id = url.replace(baseUrl, "");
            final String html = get(baseSvcUrl + id);
            assertTrue(html.contains("<title>Title</title>"));
            assertTrue(html.contains("<h1>Fedora Template Service: Title</h1>"));
            assertTrue(html.contains("<p>A description: " + url + "</p>"));
        });
    }
}
