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

import static org.apache.camel.component.mock.MockEndpoint.assertIsSatisfied;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
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
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;
import org.junit.Before;
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
 * @author Bethany Seeger
 * @since June 24, 2016
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class AcrepoBroadcastIT extends AbstractOSGiIT {

    private static Logger LOGGER = getLogger(AcrepoBroadcastIT.class);

    @Configuration
    public Option[] config() {
        final ConfigurationManager cm = new ConfigurationManager();
        final String jmsPort = cm.getProperty("fcrepo.dynamic.jms.port");
        final String fcrepoPort = cm.getProperty("fcrepo.dynamic.test.port");
        final String rmiRegistryPort = cm.getProperty("karaf.rmiRegistry.port");
        final String rmiServerPort = cm.getProperty("karaf.rmiServer.port");
        final String sshPort = cm.getProperty("karaf.ssh.port");
        final String inputStream = "broker:topic:fedora";
        final String messageRecipients = "mock:queue1,mock:queue2,mock:queue3";
        final String brokerUrl = "tcp://localhost:" + jmsPort;

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
            features(maven().groupId("org.apache.activemq").artifactId("activemq-karaf")
                        .type("xml").classifier("features").versionAsInProject(), "activemq-camel"),
            features(maven().groupId("edu.amherst.acdc").artifactId("acrepo-karaf")
                        .type("xml").classifier("features").versionAsInProject(),
                        "acrepo-services-activemq", "acrepo-connector-broadcast"),

            mavenBundle().groupId("org.apache.httpcomponents").artifactId("httpclient-osgi").versionAsInProject(),
            mavenBundle().groupId("org.apache.httpcomponents").artifactId("httpcore-osgi").versionAsInProject(),

            systemProperty("fcrepo.port").value(fcrepoPort),

            editConfigurationFilePut("etc/org.apache.karaf.management.cfg", "rmiRegistryPort", rmiRegistryPort),
            editConfigurationFilePut("etc/org.apache.karaf.management.cfg", "rmiServerPort", rmiServerPort),
            editConfigurationFilePut("etc/org.apache.karaf.shell.cfg", "sshPort", sshPort),
            editConfigurationFilePut("etc/edu.amherst.acdc.services.activemq.cfg", "jms.brokerUrl", brokerUrl),
            editConfigurationFilePut("etc/edu.amherst.acdc.connector.broadcast.cfg", "input.stream", inputStream),
            editConfigurationFilePut("etc/edu.amherst.acdc.connector.broadcast.cfg", "message.recipients",
                                     messageRecipients)
       };
    }

    @Before
    public void setup() throws Exception {
        // Clean out the fedora queue before these tests start, or the wrong number
        // of messages might be received (more then expected).
        final CamelContext ctx = getOsgiService(CamelContext.class, "(camel.context.name=AcrepoConnectorBroadcast)",
                                                10000);
        assertNotNull(ctx);

        ((MockEndpoint) ctx.getEndpoint("mock:queue1")).reset();
        ((MockEndpoint) ctx.getEndpoint("mock:queue2")).reset();
        ((MockEndpoint) ctx.getEndpoint("mock:queue3")).reset();
    }

    @Test
    public void testInstallation() throws Exception {
        assertTrue(featuresService.isInstalled(featuresService.getFeature("camel-core")));
        assertTrue(featuresService.isInstalled(featuresService.getFeature("camel-blueprint")));
        assertTrue(featuresService.isInstalled(featuresService.getFeature("activemq-camel")));
        assertTrue(featuresService.isInstalled(featuresService.getFeature("acrepo-services-activemq")));
        assertTrue(featuresService.isInstalled(featuresService.getFeature("acrepo-connector-broadcast")));
    }

    @Test
    public void testBroadcastingConnector() throws Exception {
        final CamelContext ctx = getOsgiService(CamelContext.class, "(camel.context.name=AcrepoConnectorBroadcast)",
                                                10000);
        assertNotNull(ctx);

        final String baseUrl = "http://localhost:" + System.getProperty("fcrepo.port") + "/fcrepo/rest";
        final String url1 = post(baseUrl).replace(baseUrl, "");
        final String url2 = post(baseUrl).replace(baseUrl, "");
        final String url3 = post(baseUrl + url1).replace(baseUrl, "");
        final String url4 = post(baseUrl + url2).replace(baseUrl, "");

        final MockEndpoint queue1 = (MockEndpoint) ctx.getEndpoint("mock:queue1");
        final MockEndpoint queue2 = (MockEndpoint) ctx.getEndpoint("mock:queue2");
        final MockEndpoint queue3 = (MockEndpoint) ctx.getEndpoint("mock:queue3");

        queue1.expectedMinimumMessageCount(6);
        queue2.expectedMinimumMessageCount(6);
        queue3.expectedMinimumMessageCount(6);

        assertIsSatisfied(queue1);
        assertIsSatisfied(queue2);
        assertIsSatisfied(queue3);
    }
}
