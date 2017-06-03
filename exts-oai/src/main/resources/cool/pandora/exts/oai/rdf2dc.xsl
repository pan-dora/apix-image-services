<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'
    xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'
    xmlns:skos="http://www.w3.org/2004/02/skos/core#"
    xmlns:dcterms='http://purl.org/dc/terms/'
    xmlns:dc='http://purl.org/dc/elements/1.1/'
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/"
    xmlns:bf="http://id.loc.gov/ontologies/bibframe/"
    exclude-result-prefixes="rdf rdfs bf skos"
    version="2.0">
    <xsl:output indent="yes" method="xml"/>
    <xsl:strip-space elements="dc:*"/>
    <xsl:param name="CamelFcrepoUri"/>

    <xsl:template name="getValue">
        <xsl:choose>
            <xsl:when test="./@rdf:resource">
                <xsl:attribute name="xsi:type">dcterms:URI</xsl:attribute>
                <xsl:value-of select="@rdf:resource"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:if test="@rdf:datatype">
                    <xsl:attribute name="xsi:type"><xsl:value-of select="@rdf:datatype"/></xsl:attribute>
                </xsl:if>
                <xsl:value-of select="normalize-space(text())"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="node()|@*">
        <xsl:apply-templates select="node()|@*"/>
    </xsl:template>


    <xsl:template match="dc:contributor | dc:coverage | dc:creator | dc:description | dc:format | dc:identifier | dc:language
            | dc:publisher | dc:relation | dc:rights | dc:source | dc:subject | dc:title | dc:type">
        <xsl:element name="{name()}">
            <xsl:call-template name="getValue"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="dcterms:contributor | dcterms:coverage | dcterms:creator | dcterms:description | dcterms:format
          | dcterms:identifier | dcterms:language | dcterms:publisher | dcterms:relation | dcterms:rights | dcterms:source
            | dcterms:subject | dcterms:title | dcterms:type">
        <xsl:element name="{concat('dc:', local-name())}">
            <xsl:call-template name="getValue"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="dcterms:spatial | dcterms:temporal">
        <dc:coverage>
            <xsl:call-template name="getValue"/>
        </dc:coverage>
    </xsl:template>

    <xsl:template match="dc:date | dcterms:date | dcterms:available | dcterms:created | dcterms:dateAccepted | dcterms:dateCopyrighted | dcterms:dateSubmitted
            | dcterms:issued | dcterms:modified | dcterms:valid">
        <dc:date>
            <xsl:call-template name="getValue"/>
        </dc:date>
    </xsl:template>

    <xsl:template match="dcterms:abstract | dcterms:tableOfContents | rdfs:comment">
        <dc:description>
            <xsl:call-template name="getValue"/>
        </dc:description>
    </xsl:template>

    <xsl:template match="dcterms:extent | dcterms:medium | bf:extent">
        <dc:format>
            <xsl:call-template name="getValue"/>
        </dc:format>
    </xsl:template>

    <xsl:template match="dcterms:bibliographicCitation">
        <dc:identifier>
            <xsl:call-template name="getValue"/>
        </dc:identifier>
    </xsl:template>

    <xsl:template match="dcterms:conformsTo | dcterms:hasFormat | dcterms:hasPart | dcterms:hasVersion | dcterms:isFormatOf | dcterms:isPartOf
            | dcterms:isReferencedBy | dcterms:isReplacedBy | dcterms:isRequiredBy | dcterms:isVersionOf | dcterms:references | dcterms:replaces
            | dcterms:requires | bf:heldBy">
        <dc:relation>
            <xsl:call-template name="getValue"/>
        </dc:relation>
    </xsl:template>

    <xsl:template match="dcterms:accessRights | dcterms:license">
        <dc:rights>
            <xsl:call-template name="getValue"/>
        </dc:rights>
    </xsl:template>

    <xsl:template match="skos:prefLabel | skos:altLabel | rdfs:label">
        <dc:title>
            <xsl:call-template name="getValue"/>
        </dc:title>
    </xsl:template>

    <xsl:template match="rdf:Description">
        <xsl:if test="ends-with(@rdf:about, $CamelFcrepoUri)">
            <xsl:apply-templates select="node()|@*"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="/rdf:RDF">
        <oai_dc:dc>
            <xsl:apply-templates select="node()|@*"/>
        </oai_dc:dc>
    </xsl:template>
</xsl:stylesheet>
