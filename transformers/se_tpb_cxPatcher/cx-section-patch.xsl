<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:pef="http://www.daisy.org/ns/2008/pef">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" />

	<xsl:template match="pef:section">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates/>
			<xsl:if test="ancestor-or-self::pef:*[@duplex][1][@duplex='true'] and count(descendant::pef:page) mod 2 = 1">
				<xsl:element name="page" namespace="http://www.daisy.org/ns/2008/pef">
					<xsl:element name="row" namespace="http://www.daisy.org/ns/2008/pef">
					</xsl:element>
				</xsl:element>
			</xsl:if>
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
