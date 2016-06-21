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
package edu.amherst.acdc.mods;

import static org.apache.camel.Exchange.HTTP_PATH;
import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.apache.camel.util.ObjectHelper.loadResourceAsStream;
import static org.apache.camel.builder.PredicateBuilder.and;


import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.language.XPathExpression;
import org.apache.camel.test.blueprint.CamelBlueprintTestSupport;
import org.junit.Test;

/**
 * @author acoburn
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
    protected Properties useOverridePropertiesWithPropertiesComponent() {
         final Properties props = new Properties();
         props.put("rest.port", "9999");
         return props;
    }

    @Test
    public void testDCRoute() throws Exception {
        context.getRouteDefinition("XmlDcXslt").adviceWith(context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                replaceFromWith("direct:start");
                weaveAddLast().to("mock:result");
            }
        });

        context.getRouteDefinition("XmlTransformationCommon").adviceWith(context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                mockEndpointsAndSkip("fcrepo*");
            }
        });

        final Map<String, String> namespaces = new HashMap<>();
        namespaces.put("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
        namespaces.put("dc", "http://purl.org/dc/elements/1.1/");

        final XPathExpression hasDescription = new XPathExpression(
            "/oai_dc:dc/dc:description[@xsi:type='http://www.w3.org/2001/XMLSchema#string' and " +
                "text()='First this, then that']");
        hasDescription.setNamespaces(namespaces);

        final XPathExpression hasFormat1 = new XPathExpression(
            "/oai_dc:dc/dc:format[@xsi:type='http://www.w3.org/2001/XMLSchema#string' and " +
                "text()='1 volume (420 pages): photographs']");
        hasFormat1.setNamespaces(namespaces);

        final XPathExpression hasFormat2 = new XPathExpression(
            "/oai_dc:dc/dc:format[@xsi:type='http://www.w3.org/2001/XMLSchema#string' and " +
                "text()='Pen and ink on linen']");
        hasFormat2.setNamespaces(namespaces);

        final XPathExpression hasFormat3 = new XPathExpression(
            "/oai_dc:dc/dc:format[@xsi:type='dcterms:URI' and text()='http://pcdm.org/file-format-types#Document']");
        hasFormat3.setNamespaces(namespaces);

        final XPathExpression hasRights = new XPathExpression(
            "/oai_dc:dc/dc:rights[@xsi:type='dcterms:URI' and text()='http://rightsstatements.org/vocab/NKC/1.0/']");
        hasRights.setNamespaces(namespaces);

        final XPathExpression hasTitle = new XPathExpression(
            "/oai_dc:dc/dc:title[@xsi:type='http://www.w3.org/2001/XMLSchema#string' and text()='Manuscript Title']");
        hasTitle.setNamespaces(namespaces);

        final XPathExpression hasSubject = new XPathExpression(
            "/oai_dc:dc/dc:subject[@xsi:type='dcterms:URI' and text()='http://localhost:8080/fcrepo/rest/test']");
        hasSubject.setNamespaces(namespaces);

        final XPathExpression hasDate = new XPathExpression(
            "/oai_dc:dc/dc:date[@xsi:type='http://id.loc.gov/datatypes/edtf/EDTF' and text()='2001-02-03']");
        hasDate.setNamespaces(namespaces);

        final XPathExpression hasType1 = new XPathExpression(
            "/oai_dc:dc/dc:type[@xsi:type='dcterms:URI' and text()='http://id.loc.gov/vocabulary/resourceTypes/txt']");
        hasType1.setNamespaces(namespaces);

        final XPathExpression hasType2 = new XPathExpression(
            "/oai_dc:dc/dc:type[@xsi:type='dcterms:URI' and text()='http://purl.org/dc/dcmitype/StillImage']");
        hasType2.setNamespaces(namespaces);

        final XPathExpression hasType3 = new XPathExpression(
            "/oai_dc:dc/dc:type[@xsi:type='dcterms:URI' and text()='http://vocab.getty.edu/aat/30002491']");
        hasType3.setNamespaces(namespaces);


        resultEndpoint.expectedMinimumMessageCount(1);
        resultEndpoint.expectedMessagesMatches(and(hasDescription, hasFormat1, hasFormat2, hasFormat3, hasRights,
                    hasSubject, hasTitle, hasDate, hasType1, hasType2, hasType3));

        final Map<String, Object> headers = new HashMap<>();
        headers.put(HTTP_RESPONSE_CODE, 200);
        headers.put(HTTP_PATH, "/acdc/manuscript");
        template.sendBodyAndHeaders("direct:start", loadResourceAsStream("resource.rdf"), headers);

        assertMockEndpointsSatisfied();
    }
}
