package edu.amherst.acdc.template.mustache;

import org.apache.camel.test.blueprint.CamelBlueprintTestSupport;

import org.junit.Test;

/**
 * @author acoburn
 */
public class RouteTest extends CamelBlueprintTestSupport {

    @Override
    protected String getBlueprintDescriptor() {
        return "/OSGI-INF/blueprint/blueprint.xml";
    }

    @Test
    public void testRoute() throws Exception {
        getMockEndpoint("mock:result").expectedMinimumMessageCount(0);

        // assert expectations
        assertMockEndpointsSatisfied();
    }

}
