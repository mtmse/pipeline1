<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/" exclude-result-prefixes="dtb">
<xsl:import href="./messages/errors.xsl"/>

<xsl:template name="verify-dtbook-ns">
	<xsl:choose>
		<xsl:when test="count(/dtb:*)=1"/>
		<xsl:otherwise><xsl:message terminate="yes"><xsl:value-of select="$errorMsg-notDTBookNamespace"/></xsl:message></xsl:otherwise>
	</xsl:choose>
</xsl:template>

<xsl:template name="verify-xml-lang-on-root">
	<xsl:if test="not(/dtb:*/@xml:lang)"><xsl:message terminate="yes"><xsl:value-of select="$errorMsg-unspecifiedLanguage"/></xsl:message></xsl:if>
</xsl:template>

<xsl:template name="check-nested-chapters">
	<xsl:if test="count(//dtb:level1[@class='chapter' and dtb:level2[@class='chapter']])&gt;0"><xsl:message terminate="yes"><xsl:value-of select="$errorMsg-nestedChapters"/></xsl:message></xsl:if>
</xsl:template>

</xsl:stylesheet>