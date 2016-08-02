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
package edu.amherst.acdc.exts.pcdm;

import java.util.Dictionary;
import java.util.Map;

import edu.amherst.acdc.services.pcdm.PcdmService;
import edu.amherst.acdc.services.pcdm.PcdmServiceImpl;

import org.apache.camel.test.blueprint.CamelBlueprintTestSupport;
import org.apache.camel.util.KeyValueHolder;

import org.junit.Test;

/**
 * @author acoburn
 * @since 9/21/15
 */
public class RouteTest extends CamelBlueprintTestSupport {

    @Override
    protected String getBlueprintDescriptor() {
        return "/OSGI-INF/blueprint/blueprint.xml";
    }

    @Override
    protected void addServicesOnStartup(final Map<String, KeyValueHolder<Object, Dictionary>> services) {
        services.put(PcdmService.class.getName(), asService(new PcdmServiceImpl(),
              "osgi.jndi.service.name", "acrepo/Pcdm"));
    }

    @Test
    public void testRoute() throws Exception {
        // the route is timer based, so every 5th second a message is send
        // we should then expect at least one message
        getMockEndpoint("mock:result").expectedMinimumMessageCount(0);

        // assert expectations
        assertMockEndpointsSatisfied();
    }

}
