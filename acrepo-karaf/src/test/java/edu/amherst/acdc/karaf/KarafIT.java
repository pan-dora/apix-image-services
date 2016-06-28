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
package edu.amherst.acdc.karaf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.configureConsole;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.editConfigurationFilePut;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.logLevel;
import static org.ops4j.pax.exam.util.PathUtils.getBaseDir;
import static org.osgi.framework.Bundle.ACTIVE;
import static org.osgi.framework.Constants.OBJECTCLASS;
import static org.osgi.framework.FrameworkUtil.createFilter;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import javax.inject.Inject;

import org.apache.karaf.features.FeaturesService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.ConfigurationManager;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.LogLevelOption.LogLevel;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;

/**
 * @author Aaron Coburn
 * @since June 1, 2016
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class KarafIT {

    private static Logger LOGGER = getLogger(KarafIT.class);

    @Inject
    protected FeaturesService featuresService;

    @Inject
    protected BundleContext bundleContext;

    @Configuration
    public Option[] config() {
        final ConfigurationManager cm = new ConfigurationManager();
        final String rmiRegistryPort = cm.getProperty("karaf.rmiRegistry.port");
        final String rmiServerPort = cm.getProperty("karaf.rmiServer.port");
        final String sshPort = cm.getProperty("karaf.ssh.port");

        final String version = cm.getProperty("project.version");
        final String acrepoIdiomatic = getBundleUri("acrepo-idiomatic", version);
        final String acrepoPcdmSvc = getBundleUri("acrepo-services-pcdm", version);
        final String acrepoValidationSvc = getBundleUri("acrepo-services-validation", version);
        final String acrepoJsonLdSvc = getBundleUri("acrepo-services-jsonld", version);
        final String acrepoJsonLd = getBundleUri("acrepo-jsonld-service", version);
        final String acrepoMintSvc = getBundleUri("acrepo-services-mint", version);

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
                        .versionAsInProject().classifier("features").type("xml"), "scr"),
            features(maven().groupId("org.apache.camel.karaf").artifactId("apache-camel")
                        .type("xml").classifier("features").versionAsInProject(), "camel-mustache",
                        "camel-blueprint", "camel-http4", "camel-spring", "camel-exec", "camel-jetty9",
                        "camel-sql", "camel-jacksonxml"),
            features(maven().groupId("org.apache.activemq").artifactId("activemq-karaf")
                        .type("xml").classifier("features").versionAsInProject(), "activemq-camel"),
            features(maven().groupId("org.fcrepo.camel").artifactId("fcrepo-camel")
                        .type("xml").classifier("features").versionAsInProject(), "fcrepo-camel"),
            mavenBundle().groupId("org.codehaus.woodstox").artifactId("woodstox-core-asl").versionAsInProject(),
            mavenBundle().groupId("org.apache.commons").artifactId("commons-lang3").versionAsInProject(),
            mavenBundle().groupId("com.github.jsonld-java").artifactId("jsonld-java").versionAsInProject(),
            mavenBundle().groupId("org.apache.httpcomponents").artifactId("httpclient-osgi").versionAsInProject(),
            mavenBundle().groupId("org.apache.httpcomponents").artifactId("httpcore-osgi").versionAsInProject(),
            mavenBundle().groupId("commons-io").artifactId("commons-io").versionAsInProject(),
            mavenBundle().groupId("commons-codec").artifactId("commons-codec").versionAsInProject(),
            mavenBundle().groupId("org.apache.jena").artifactId("jena-osgi").versionAsInProject(),
            mavenBundle().groupId("com.github.andrewoma.dexx").artifactId("collection").versionAsInProject(),

            CoreOptions.systemProperty("acdc.idiomatic-bundle").value(acrepoIdiomatic),
            CoreOptions.systemProperty("acdc.validation-svc-bundle").value(acrepoValidationSvc),
            CoreOptions.systemProperty("acdc.jsonld-bundle").value(acrepoJsonLd),
            CoreOptions.systemProperty("acdc.jsonld-svc-bundle").value(acrepoJsonLdSvc),
            CoreOptions.systemProperty("acdc.mint-svc-bundle").value(acrepoMintSvc),
            CoreOptions.systemProperty("acdc.pcdm-svc-bundle").value(acrepoPcdmSvc),

            bundle(acrepoIdiomatic).start(),
            bundle(acrepoValidationSvc).start(),
            bundle(acrepoJsonLd).start(),
            bundle(acrepoJsonLdSvc).start(),
            bundle(acrepoMintSvc).start(),
            bundle(acrepoPcdmSvc).start(),

            editConfigurationFilePut("etc/org.apache.karaf.management.cfg", "rmiRegistryPort", rmiRegistryPort),
            editConfigurationFilePut("etc/org.apache.karaf.management.cfg", "rmiServerPort", rmiServerPort),
            editConfigurationFilePut("etc/org.apache.karaf.shell.cfg", "sshPort", sshPort),
       };
    }


    @Test
    public void testInstallation() throws Exception {

        assertTrue(featuresService.isInstalled(featuresService.getFeature("camel-core")));
        assertTrue(featuresService.isInstalled(featuresService.getFeature("fcrepo-camel")));
        assertTrue(featuresService.isInstalled(featuresService.getFeature("activemq-camel")));
        assertTrue(featuresService.isInstalled(featuresService.getFeature("camel-blueprint")));
        assertTrue(featuresService.isInstalled(featuresService.getFeature("camel-jetty9")));
        assertNotNull(bundleContext);

        assertEquals(ACTIVE, bundleContext.getBundle(System.getProperty("acdc.idiomatic-bundle")).getState());
        assertEquals(ACTIVE, bundleContext.getBundle(System.getProperty("acdc.validation-svc-bundle")).getState());
        assertEquals(ACTIVE, bundleContext.getBundle(System.getProperty("acdc.jsonld-bundle")).getState());
        assertEquals(ACTIVE, bundleContext.getBundle(System.getProperty("acdc.jsonld-svc-bundle")).getState());
        assertEquals(ACTIVE, bundleContext.getBundle(System.getProperty("acdc.mint-svc-bundle")).getState());
        assertEquals(ACTIVE, bundleContext.getBundle(System.getProperty("acdc.pcdm-svc-bundle")).getState());
    }

    private <T> T getOsgiService(final Class<T> type, final String filter, final long timeout) {
        try {
            final ServiceTracker tracker = new ServiceTracker(bundleContext,
                    createFilter("(&(" + OBJECTCLASS + "=" + type.getName() + ")" + filter + ")"), null);
            tracker.open(true);
            final Object svc = type.cast(tracker.waitForService(timeout));
            if (svc == null) {
                throw new RuntimeException("Gave up waiting for service " + filter);
            }
            return type.cast(svc);
        } catch (InvalidSyntaxException e) {
            throw new IllegalArgumentException("Invalid filter", e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getBundleUri(final String artifactId, final String version) {
        final File artifact = new File(getBaseDir() + "/../" + artifactId + "/target/" +
                artifactId + "-" + version + ".jar");
        if (artifact.exists()) {
            return artifact.toURI().toString();
        }
        return "mvn:edu.amherst.acdc/" + artifactId + "/" + version;
    }

}
