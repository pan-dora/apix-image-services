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
package cool.pandora.itests;

import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.configureConsole;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.editConfigurationFilePut;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.logLevel;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;

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
public class AcrepoServicesIT extends AbstractOSGiIT {

    private static Logger LOGGER = getLogger(AcrepoServicesIT.class);

    @Configuration
    public Option[] config() {
        final ConfigurationManager cm = new ConfigurationManager();
        final String rmiRegistryPort = cm.getProperty("karaf.rmiRegistry.port");
        final String rmiServerPort = cm.getProperty("karaf.rmiServer.port");
        final String sshPort = cm.getProperty("karaf.ssh.port");
        final String idiomaticPort = cm.getProperty("karaf.idiomatic.port");
        final String metadataPort = cm.getProperty("karaf.metadata.port");
        final String templatePort = cm.getProperty("karaf.template.port");
        final String pcdmPort = cm.getProperty("karaf.pcdm.port");
        final String imagePort = cm.getProperty("karaf.image.port");
        final String ldpathPort = cm.getProperty("karaf.ldpath.port");
        final String fitsPort = cm.getProperty("karaf.fits.port");

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
                        .type("xml").classifier("features").versionAsInProject(), "camel-blueprint"),
            features(maven().groupId("org.apache.activemq").artifactId("activemq-karaf")
                        .type("xml").classifier("features").versionAsInProject(), "activemq-camel"),
            features(maven().groupId("org.apache.camel.karaf").artifactId("apache-camel")
                        .type("xml").classifier("features").versionAsInProject()),
            features(maven().groupId("org.fcrepo.camel").artifactId("fcrepo-camel")
                        .type("xml").classifier("features").versionAsInProject()),
            features(maven().groupId("org.fcrepo.camel").artifactId("toolbox-features")
                        .type("xml").classifier("features").versionAsInProject(), "fcrepo-service-activemq"),
            features(maven().groupId("cool.pandora").artifactId("acrepo-karaf")
                        .type("xml").classifier("features").versionAsInProject(),
                    "acrepo-connector-broadcast",
                    "acrepo-connector-triplestore",

                    "acrepo-exts-fits",
                    "acrepo-exts-image",
                    "acrepo-exts-ldpath",
                    "acrepo-exts-pcdm",
                    "acrepo-exts-serialize-xml",
                    "acrepo-exts-template",

                    "acrepo-libs-jena",
                    "acrepo-libs-jsonld",
                    "acrepo-libs-jackson",

                    "acrepo-services-inference",
                    "acrepo-services-mint",
                    "acrepo-services-pcdm"),

            editConfigurationFilePut("etc/cool.pandora.exts.fits.cfg", "rest.port", fitsPort),
            editConfigurationFilePut("etc/cool.pandora.exts.image.cfg", "rest.port", imagePort),
            editConfigurationFilePut("etc/cool.pandora.exts.ldpath.cfg", "rest.port", ldpathPort),
            editConfigurationFilePut("etc/cool.pandora.exts.pcdm.cfg", "rest.port", pcdmPort),
            editConfigurationFilePut("etc/cool.pandora.exts.serialize.xml.cfg", "rest.port", metadataPort),
            editConfigurationFilePut("etc/cool.pandora.exts.template.cfg", "rest.port", templatePort),
            editConfigurationFilePut("etc/org.apache.karaf.management.cfg", "rmiRegistryPort", rmiRegistryPort),
            editConfigurationFilePut("etc/org.apache.karaf.management.cfg", "rmiServerPort", rmiServerPort),
            editConfigurationFilePut("etc/org.apache.karaf.shell.cfg", "sshPort", sshPort)
       };
    }

    @Test
    public void testInstallation() throws Exception {
        assertTrue(featuresService.isInstalled(featuresService.getFeature("camel-core")));
        assertTrue(featuresService.isInstalled(featuresService.getFeature("fcrepo-camel")));
        assertTrue(featuresService.isInstalled(featuresService.getFeature("fcrepo-service-activemq")));
        assertTrue(featuresService.isInstalled(featuresService.getFeature("acrepo-connector-broadcast")));
        assertTrue(featuresService.isInstalled(featuresService.getFeature("acrepo-connector-triplestore")));
        assertTrue(featuresService.isInstalled(featuresService.getFeature("acrepo-exts-fits")));
        assertTrue(featuresService.isInstalled(featuresService.getFeature("acrepo-exts-image")));
        assertTrue(featuresService.isInstalled(featuresService.getFeature("acrepo-exts-ldpath")));
        assertTrue(featuresService.isInstalled(featuresService.getFeature("acrepo-exts-pcdm")));
        assertTrue(featuresService.isInstalled(featuresService.getFeature("acrepo-exts-serialize-xml")));
        assertTrue(featuresService.isInstalled(featuresService.getFeature("acrepo-exts-template")));
        assertTrue(featuresService.isInstalled(featuresService.getFeature("acrepo-libs-jackson")));
        assertTrue(featuresService.isInstalled(featuresService.getFeature("acrepo-libs-jena")));
        assertTrue(featuresService.isInstalled(featuresService.getFeature("acrepo-libs-jsonld")));
        assertTrue(featuresService.isInstalled(featuresService.getFeature("acrepo-services-inference")));
        assertTrue(featuresService.isInstalled(featuresService.getFeature("acrepo-services-mint")));
        assertTrue(featuresService.isInstalled(featuresService.getFeature("acrepo-services-pcdm")));
    }
}
