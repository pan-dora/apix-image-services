<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'
    xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'
    xmlns:dcterms='http://purl.org/dc/terms/'
    xmlns:dc='http://purl.org/dc/elements/1.1/'
    xmlns:mods="http://www.loc.gov/mods/v3"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs rdf rdfs dc dcterms"
    version="2.0">
    <xsl:output indent="yes" method="xml"></xsl:output>
    <xsl:param name="CamelFcrepoIdentifier"/>
    <xsl:param name="CamelFcrepoBaseUrl"/>
    <xsl:template match="/rdf:RDF/rdf:Description">

        <xsl:if test="ends-with(@rdf:about, concat($CamelFcrepoBaseUrl, $CamelFcrepoIdentifier))">
            <mods:mods>
                <xsl:for-each select="dc:title">
                    <mods:titleInfo>
                        <mods:title><xsl:value-of select="normalize-space(text())"/></mods:title>
                    </mods:titleInfo>
                </xsl:for-each>

                <xsl:for-each select="dc:subject">
                    <mods:subject authority="lcsh">
                        <mods:topic>
                            <xsl:value-of select="normalize-space(text())"/>
                        </mods:topic>
                    </mods:subject>
                </xsl:for-each>

                <xsl:for-each select="dcterms:abstract">
                    <xsl:if test="string-length(normalize-space(text()))">
                        <mods:abstract><xsl:value-of select="normalize-space(text())"/></mods:abstract>
                    </xsl:if>
                </xsl:for-each>

                <xsl:for-each select="rdfs:comment">
                    <xsl:if test="string-length(normalize-space(text()))">
                        <mods:note><xsl:value-of select="normalize-space(text())"/></mods:note>
                    </xsl:if>
                </xsl:for-each>
            </mods:mods>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>
