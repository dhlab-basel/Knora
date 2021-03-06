<?xml version="1.0" encoding="UTF-8"?>
<!--

An example stylesheet that transforms an RDF/XML representation of a beol:letter into
a TEI/XML header. This stylesheet assumes that the input consists only of
<rdf:Description> elements.

-->
<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
               xmlns:xs="http://www.w3.org/2001/XMLSchema"
               xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
               xmlns:rdfs1="http://www.w3.org/2000/01/rdf-schema#"
               xmlns:beol="http://0.0.0.0:3333/ontology/0801/beol/v2#"
               xmlns:knora-api="http://api.knora.org/ontology/knora-api/v2#"
               exclude-result-prefixes="rdf beol knora-api xs rdfs1" version="2.0">

    <xsl:output method="xml" omit-xml-declaration="yes" encoding="utf-8" indent="yes"/>

    <!-- make IAF id a URL -->
    <xsl:function name="knora-api:iaf" as="xs:string">
        <xsl:param name="input" as="xs:string"/>
        <xsl:sequence select="replace($input, '\(DE-588\)', 'http://d-nb.info/gnd/')"/>
    </xsl:function>

    <!-- https://www.safaribooksonline.com/library/view/xslt-cookbook/0596003722/ch03s03.html?orpq -->
    <xsl:function name="knora-api:last-day-of-month" as="xs:string">
        <xsl:param name="month"/>
        <xsl:param name="year"/>
        <xsl:choose>
            <xsl:when test="$month = 2 and
            not($year mod 4) and
            ($year mod 100 or not($year mod 400))">
                <xsl:value-of select="29"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of
                        select="substring('312831303130313130313031',
         2 * $month - 1,2)"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <!-- make a standard date (Gregorian calendar assumed) -->
    <xsl:function name="knora-api:dateformat" as="element()*">
        <xsl:param name="input" as="element()*"/>

        <xsl:choose>
            <xsl:when
                    test="$input/knora-api:dateValueHasStartYear/text() = $input/knora-api:dateValueHasEndYear/text() and $input/knora-api:dateValueHasStartMonth/text() = $input/knora-api:dateValueHasEndMonth/text() and $input/knora-api:dateValueHasStartDay/text() = $input/knora-api:dateValueHasEndDay/text()">
                <!-- no period, day precision -->
                <date>
                    <xsl:attribute name="when">
                        <xsl:value-of
                                select="format-number($input/knora-api:dateValueHasStartYear/text(), '0000')"/>-<xsl:value-of
                            select="format-number($input/knora-api:dateValueHasStartMonth/text(), '00')"/>-<xsl:value-of
                            select="format-number($input/knora-api:dateValueHasStartDay/text(), '00')"/>
                    </xsl:attribute>
                </date>

            </xsl:when>
            <xsl:otherwise>
                <!-- period -->
                <date>
                    <!-- start date could be imprecise -->
                    <xsl:variable name="startDay">
                        <xsl:choose>
                            <xsl:when test="$input/knora-api:dateValueHasStartDay">
                                <xsl:value-of select="$input/knora-api:dateValueHasStartDay/text()"/>
                            </xsl:when>
                            <xsl:otherwise>01</xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>

                    <xsl:variable name="startMonth">
                        <xsl:choose>
                            <xsl:when test="$input/knora-api:dateValueHasStartMonth">
                                <xsl:value-of select="$input/knora-api:dateValueHasStartMonth/text()"/>
                            </xsl:when>
                            <xsl:otherwise>01</xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>

                    <xsl:attribute name="notBefore">
                        <xsl:value-of
                                select="format-number($input/knora-api:dateValueHasStartYear/text(), '0000')"/>-<xsl:value-of
                            select="format-number($startMonth, '00')"/>-<xsl:value-of
                            select="format-number($startDay, '00')"/>
                    </xsl:attribute>

                    <!-- end date could be imprecise -->

                    <xsl:variable name="endMonth">
                        <xsl:choose>
                            <xsl:when test="$input/knora-api:dateValueHasEndMonth">
                                <xsl:value-of select="$input/knora-api:dateValueHasEndMonth/text()"/>
                            </xsl:when>
                            <xsl:otherwise>12</xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>

                    <xsl:variable name="endDay">
                        <xsl:choose>
                            <xsl:when test="$input/knora-api:dateValueHasEndDay">
                                <xsl:value-of select="$input/knora-api:dateValueHasEndDay/text()"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of
                                        select="knora-api:last-day-of-month(number($endMonth), number($input/knora-api:dateValueHasEndYear/text()))"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>


                    <xsl:attribute name="notAfter">
                        <xsl:value-of
                                select="format-number($input/knora-api:dateValueHasEndYear/text(), '0000')"/>-<xsl:value-of
                            select="format-number($endMonth, '00')"/>-<xsl:value-of
                            select="format-number($endDay, '00')"/>
                    </xsl:attribute>
                </date>

            </xsl:otherwise>
        </xsl:choose>


    </xsl:function>

    <xsl:template match="//rdf:RDF">
        <xsl:variable name="resourceIri" select="//rdf:Description[./rdf:type/@rdf:resource='http://0.0.0.0:3333/ontology/0801/beol/v2#letter']/@rdf:about"/>
        <xsl:variable name="label" select="//rdf:Description[@rdf:about=$resourceIri]/rdfs1:label/text()"/>

        <teiHeader>
            <fileDesc>
                <titleStmt>
                    <title>
                        <xsl:value-of select="$label"/>
                    </title>
                </titleStmt>
                <publicationStmt>
                    <p>This is the TEI/XML representation of the resource identified by the Iri
                        <xsl:value-of select="$resourceIri"/>.
                    </p>
                </publicationStmt>
                <sourceDesc>
                    <p>Representation of the resource's text as TEI/XML</p>
                </sourceDesc>
            </fileDesc>
            <profileDesc>

                <correspDesc>
                    <xsl:attribute name="ref">
                        <xsl:value-of select="$resourceIri"/>
                    </xsl:attribute>
                    <xsl:apply-templates select="//rdf:Description/beol:hasAuthorValue"/>
                    <xsl:apply-templates select="//rdf:Description/beol:hasRecipientValue"/>
                </correspDesc>
            </profileDesc>
        </teiHeader>
    </xsl:template>

    <xsl:template match="//rdf:Description/beol:hasAuthorValue">
        <xsl:variable name="authorValueIri" select="@rdf:resource"/>
        <xsl:variable name="authorIri" select="//rdf:Description[@rdf:about=$authorValueIri]//knora-api:linkValueHasTarget/@rdf:resource"/>

        <xsl:variable name="authorIAFValue"
                      select="//rdf:Description[@rdf:about=$authorIri]//beol:hasIAFIdentifier/@rdf:resource"/>
        <xsl:variable name="authorFamilyNameValue"
                      select="//rdf:Description[@rdf:about=$authorIri]//beol:hasFamilyName/@rdf:resource"/>
        <xsl:variable name="authorGivenNameValue"
                      select="//rdf:Description[@rdf:about=$authorIri]//beol:hasGivenName/@rdf:resource"/>

        <correspAction type="sent">
            <xsl:variable name="authorIAFText"
                          select="//rdf:Description[@rdf:about=$authorIAFValue]/knora-api:valueAsString/text()"/>
            <xsl:variable name="authorFamilyNameText"
                          select="//rdf:Description[@rdf:about=$authorFamilyNameValue]/knora-api:valueAsString/text()"/>
            <xsl:variable name="authorGivenNameText"
                          select="//rdf:Description[@rdf:about=$authorGivenNameValue]/knora-api:valueAsString/text()"/>

            <persName>
                <xsl:attribute name="ref">
                    <xsl:value-of select="knora-api:iaf($authorIAFText)"
                    />
                </xsl:attribute>
                <xsl:value-of select="$authorFamilyNameText"/>,
                <xsl:value-of
                        select="$authorGivenNameText"/>
            </persName>

            <xsl:variable name="dateValue" select="//beol:creationDate/@rdf:resource"/>

            <xsl:variable name="dateObj"
                          select="//rdf:Description[@rdf:about=$dateValue]"/>

            <xsl:copy-of select="knora-api:dateformat($dateObj)"/>

        </correspAction>
    </xsl:template>

    <xsl:template match="//rdf:Description/beol:hasRecipientValue">
        <xsl:variable name="recipientValueIri" select="@rdf:resource"/>
        <xsl:variable name="recipientIri" select="//rdf:Description[@rdf:about=$recipientValueIri]//knora-api:linkValueHasTarget/@rdf:resource"/>

        <xsl:variable name="recipientIAFValue"
                      select="//rdf:Description[@rdf:about=$recipientIri]//beol:hasIAFIdentifier/@rdf:resource"/>
        <xsl:variable name="recipientFamilyNameValue"
                      select="//rdf:Description[@rdf:about=$recipientIri]//beol:hasFamilyName/@rdf:resource"/>
        <xsl:variable name="recipientGivenNameValue"
                      select="//rdf:Description[@rdf:about=$recipientIri]//beol:hasGivenName/@rdf:resource"/>

        <correspAction type="received">

            <xsl:variable name="recipientIAFText"
                          select="//rdf:Description[@rdf:about=$recipientIAFValue]/knora-api:valueAsString/text()"/>
            <xsl:variable name="recipientFamilyNameText"
                          select="//rdf:Description[@rdf:about=$recipientFamilyNameValue]/knora-api:valueAsString/text()"/>
            <xsl:variable name="recipientGivenNameText"
                          select="//rdf:Description[@rdf:about=$recipientGivenNameValue]/knora-api:valueAsString/text()"/>

            <persName>
                <xsl:attribute name="ref">
                    <xsl:value-of select="knora-api:iaf($recipientIAFText)"
                    />
                </xsl:attribute>
                <xsl:value-of select="$recipientFamilyNameText"/>,
                <xsl:value-of
                        select="$recipientGivenNameText"/>
            </persName>

        </correspAction>
    </xsl:template>

    <!-- ignore text if there is no template for the element containing it -->
    <xsl:template match="text()"/>


</xsl:transform>
