package edu.amherst.acdc.template.mustache;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.blueprint.CamelBlueprintTestSupport;

import org.junit.Test;

/**
 * @author acoburn
 */
public class RouteTest extends CamelBlueprintTestSupport {

    @EndpointInject(uri = "mock:result")
    protected MockEndpoint resultEndpoint;

    @Produce(uri = "direct:template")
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

    @Test
    public void testRoute() throws Exception {

        context.getRouteDefinition("TemplateRoute").adviceWith(context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                weaveAddLast().to("mock:result");
            }
        });

        context.start();

        resultEndpoint.expectedMinimumMessageCount(1);
        resultEndpoint.expectedHeaderReceived("Content-Type", "text/html");
        resultEndpoint.allMessages().body().contains("Fedora Template Service: Foo");
        resultEndpoint.allMessages().body().contains("<p>sample description</p>");

        template.sendBody("{\"title\" : \"Foo\", \"description\" : \"sample description\"}");

        // assert expectations
        assertMockEndpointsSatisfied();
    }
}
