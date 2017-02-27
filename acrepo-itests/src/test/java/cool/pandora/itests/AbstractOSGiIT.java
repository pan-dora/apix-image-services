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

import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.impl.client.HttpClients.createDefault;
import static org.junit.Assert.assertEquals;
import static org.osgi.framework.Constants.OBJECTCLASS;
import static org.osgi.framework.FrameworkUtil.createFilter;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import org.apache.karaf.features.FeaturesService;
import org.ops4j.pax.exam.Option;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;

/**
 * @author Aaron Coburn
 * @since May 11, 2016
 */
public abstract class AbstractOSGiIT {

    private static final Logger LOGGER = getLogger(AbstractOSGiIT.class);

    private final CloseableHttpClient httpclient = createDefault();

    @Inject
    protected FeaturesService featuresService;

    @Inject
    protected BundleContext bundleContext;

    public abstract Option[] config();

    protected String post(final String url) {
        return post(url, null, null);
    }

    protected String post(final String url, final InputStream stream, final String contentType) {
        try {
            final HttpPost req = new HttpPost(url);
            if (stream != null) {
              req.setHeader("Content-Type", contentType);
              req.setEntity(new InputStreamEntity(stream));
            }

            final HttpResponse response = httpclient.execute(req);
            assertEquals(SC_CREATED, response.getStatusLine().getStatusCode());
            return EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (final IOException ex) {
            LOGGER.error("Unable to extract HttpEntity response into an InputStream: ", ex);
        }
        return "";
    }

    protected boolean put(final String url) {
        return put(url, null, null);
    }

    protected boolean put(final String url, final InputStream stream, final String contentType) {
        try {
            final HttpPut req = new HttpPut(url);
            if (stream != null) {
                req.setHeader("Content-Type", contentType);
                req.setEntity(new InputStreamEntity(stream));
            }
            final HttpResponse response = httpclient.execute(req);
            assertEquals(SC_CREATED, response.getStatusLine().getStatusCode());
            return true;
        } catch (final IOException ex) {
            LOGGER.error("Error patching to {}: {}", url, ex.getMessage());
        }
        return false;
    }

    protected boolean patch(final String url, final String sparql) {
        try {
            final HttpPatch req = new HttpPatch(url);
            req.addHeader("Content-Type", "application/sparql-update");
            req.setEntity(new StringEntity(sparql));
            final HttpResponse response = httpclient.execute(req);
            assertEquals(SC_NO_CONTENT, response.getStatusLine().getStatusCode());
            return true;
        } catch (final IOException ex) {
            LOGGER.error("Error patching to {}: {}", url, ex.getMessage());
        }
        return false;
    }

    protected String get(final String url) {
        return get(url, null);
    }

    protected String get(final String url, final String accept) {
        try {
            final HttpGet req = new HttpGet(url);
            if (accept != null) {
                req.addHeader("Accept", accept);
            }
            final HttpResponse response = httpclient.execute(req);
            assertEquals(SC_OK, response.getStatusLine().getStatusCode());
            return EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (final IOException ex) {
            LOGGER.error("Unable to extract HttpEntity response into an InputStream: ", ex);
        }
        return "";
    }

    protected String options(final String url) {
        try {
            final HttpOptions req = new HttpOptions(url);
            final HttpResponse response = httpclient.execute(req);
            assertEquals(SC_OK, response.getStatusLine().getStatusCode());
            return EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (final IOException ex) {
            LOGGER.error("Unable to extract HttpEntity response into an InputStream: ", ex);
        }
        return "";
    }

    @SuppressWarnings("unchecked")
    protected <T> T getOsgiService(final Class<T> type, final String filter, final long timeout) {
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


}
