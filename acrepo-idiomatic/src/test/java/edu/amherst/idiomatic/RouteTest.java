/**
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
package edu.amherst.acdc.idiomatic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.blueprint.CamelBlueprintTestSupport;
import org.apache.camel.util.KeyValueHolder;
import org.apache.camel.util.ObjectHelper;
import org.apache.commons.io.IOUtils;
import org.fcrepo.camel.JmsHeaders;
import org.postgresql.ds.PGPoolingDataSource;
import edu.amherst.acdc.mint.MinterService;

import org.junit.Test;

/**
 * Test the route workflow.
 *
 * @author Aaron Coburn
 * @since 2015-04-10
 */
public class RouteTest extends CamelBlueprintTestSupport {

    @EndpointInject(uri = "mock:result")
    protected MockEndpoint resultEndpoint;

    @Produce(uri = "direct:start")
    protected ProducerTemplate template;

    @Override
    protected String getBlueprintDescriptor() {
        return "/OSGI-INF/blueprint/blueprint.xml";
    }

    @Override
    public boolean isUseAdviceWith() {
        return true;
    }

    @Override
    public boolean isUseRouteBuilder() {
        return false;
    }

    @Override
    protected Properties useOverridePropertiesWithPropertiesComponent() {
         final Properties props = new Properties();
         props.put("input.stream", "seda:foo");
         return props;
    }

    @Override
    protected void addServicesOnStartup(final Map<String, KeyValueHolder<Object, Dictionary>> services) {
        services.put("dataSource", asService(new PGPoolingDataSource(), "name", "idmapperds"));
        services.put("minterService", asService(new MinterService(7), "name", "minter"));
    }

    @Test
    public void testMint() throws Exception {
        context.getRouteDefinition("IdMappingRouter").adviceWith(context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                replaceFromWith("direct:start");
            }
        });
        context.getRouteDefinition("MinterRoute").adviceWith(context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                weaveAddLast().to("mock:result");
            }
        });

        context.start();

        getMockEndpoint("mock:result").expectedMessageCount(2);

        template.sendBody("direct:minter", null);
        template.sendBody("direct:minter", null);

        final String id1 = getMockEndpoint("mock:result").getExchanges().get(0).getIn().getBody(String.class);
        final String id2 = getMockEndpoint("mock:result").getExchanges().get(1).getIn().getBody(String.class);
        assertEquals(7, id1.length());
        assertEquals(7, id2.length());
        assertFalse(id1.equals(id2));

        assertMockEndpointsSatisfied();
    }

    @Test
    public void testEvent() throws Exception {

        context.getRouteDefinition("IdMappingRouter").adviceWith(context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                replaceFromWith("direct:start");
            }
        });

        context.getRouteDefinition("IdMappingEventRouter").adviceWith(context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                mockEndpointsAndSkip("fcrepo*");
            }
        });

        context.getRouteDefinition("IdMappingUpdateRouter").adviceWith(context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                mockEndpointsAndSkip("sql*");
                weaveAddLast().to("mock:result");
            }
        });


        context.start();

        getMockEndpoint("mock:result").expectedMessageCount(2);
        final Map<String, Object> headers = new HashMap<>();
        headers.put(JmsHeaders.IDENTIFIER, "/foo/bar");
        template.sendBodyAndHeaders(
                IOUtils.toString(ObjectHelper.loadResourceAsStream("indexable.rdf"),
                "UTF-8"), headers);

        assertMockEndpointsSatisfied();
    }
}
