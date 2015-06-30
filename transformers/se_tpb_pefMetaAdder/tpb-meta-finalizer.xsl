<?xml version="1.0" encoding="UTF-8"?>
<!--	
		MTM meta finalizer.
		The meta finalizer inserts meta data by examining various properties of the PEF-file.
-->
<!--
		Joel Håkansson
		Version 2010-02-02
 -->

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:pef="http://www.daisy.org/ns/2008/pef" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:ext="http://www.tpb.se/pef/extensions/">
	<xsl:param name="input-uri"/>

	<xsl:output method="xml" media-type="application/x-pef+xml" encoding="utf-8" indent="yes"/>
	<xsl:variable name="dtbook" select="document($input-uri)"/>
	
	<xsl:template match="pef:meta">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:attribute name="version" namespace="http://www.tpb.se/pef/extensions/">2010-1</xsl:attribute>
			<xsl:apply-templates select="node()[not(self::ext:sheets or self::ext:volumes or self::ext:kiloChars)]"/>
			<xsl:element name="ext:sheets">
				<xsl:value-of select="(
					sum(
						//pef:section/(
							if (ancestor-or-self::pef:*[@duplex][1]/@duplex=false()) then (
								count(descendant::pef:page) * 2
							) else (
								count(descendant::pef:page) + count(descendant::pef:page) mod 2
							)
						)
					)
				) div 2"/>
			</xsl:element>
			<xsl:element name="ext:volumes"><xsl:value-of select="count(//pef:volume)"/></xsl:element>
			<xsl:element name="ext:kiloChars"><xsl:value-of select="round(sum(//pef:row/string-length(translate(., '⠀',''))) div 1000)"/></xsl:element>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="*|comment()|processing-instruction()">
		<xsl:call-template name="copy"/>
	</xsl:template>

	<xsl:template name="copy">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
