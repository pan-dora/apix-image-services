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
package edu.amherst.acdc.connector.idiomatic;

import static org.fcrepo.camel.FcrepoHeaders.FCREPO_URI;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Dictionary;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import edu.amherst.acdc.services.mint.MinterService;
import java.util.function.Supplier;
import org.apache.camel.Component;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.component.seda.SedaComponent;
import org.apache.camel.test.blueprint.CamelBlueprintTestSupport;
import org.apache.camel.util.KeyValueHolder;
import org.apache.camel.util.ObjectHelper;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

/**
 * Test the route workflow.
 *
 * @author Aaron Coburn
 * @since 2015-04-10
 */
public class RouteTest extends CamelBlueprintTestSupport {

    private final int MINT_LENGTH = 7;

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
         props.put("rest.port", "9999");
         props.put("id.property", "http://purl.org/dc/elements/1.1/identifier");
         return props;
    }

    @Override
    protected void addServicesOnStartup(final Map<String, KeyValueHolder<Object, Dictionary>> services) {
        services.put(DataSource.class.getName(),
                asService(new EmbeddedDataSource(), "osgi.jndi.service.name", "jdbc/idiomaticds"));
        services.put(Supplier.class.getName(),
                asService(new MinterService(MINT_LENGTH), "osgi.jndi.service.name", "acrepo/Minter"));
        services.put(Component.class.getName(),
                asService(new SedaComponent(), "osgi.jndi.service.name", "fcrepo/Broker"));
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

        final String id1 = resultEndpoint.getExchanges().get(0).getIn().getBody(String.class);
        final String id2 = resultEndpoint.getExchanges().get(1).getIn().getBody(String.class);
        assertEquals(MINT_LENGTH, id1.length());
        assertEquals(MINT_LENGTH, id2.length());
        assertFalse(id1.equals(id2));

        assertMockEndpointsSatisfied();
    }

    @Test
    public void testEvent() throws Exception {

        context.getRouteDefinition("IdMappingRouter").adviceWith(context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                replaceFromWith("direct:start");
                mockEndpointsAndSkip("fcrepo*");
                mockEndpoints("direct:update");
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

        resultEndpoint.expectedMessageCount(2);
        getMockEndpoint("mock:direct:update").expectedBodiesReceived(
                "http://localhost/foo/bar", "http://localhost/foo/bar");
        getMockEndpoint("mock:direct:update").expectedHeaderValuesReceivedInAnyOrder(IdiomaticHeaders.ID,
                "http://example.org/object/1", "http://example.org/object/2");
        template.sendBodyAndHeader(
                IOUtils.toString(ObjectHelper.loadResourceAsStream("indexable.json"),
                "UTF-8"), FCREPO_URI, "http://localhost/foo/bar");

        assertMockEndpointsSatisfied();
    }
}
