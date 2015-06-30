<?xml version="1.0"?>
<!--
	Move cover text
		Version
			2011-01-25

		Description

		Nodes
			*

		Namespaces
			( ) ""
			(x) "http://www.daisy.org/z3986/2005/dtbook/"

		Doctype assignment
			(x) DTBook
			( ) None

		Tests
			XMLSpy XSLT engine	( ) 2005	( ) 2006	( ) 2007
			MSXML				( ) 3.0		( ) 4.0		( ) 6.0
			Saxon				( ) 6.5.3	( ) 8.8		(x) 9.0.0.2j

			(x) = pass
			(-) = fail
			( ) = not tested

		Author
			Joel Håkansson
-->
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
	xmlns:d="http://www.tpb.se/stylesheets/dtbinfo"
	xmlns="http://www.daisy.org/z3986/2005/dtbook/"
	exclude-result-prefixes="dtb d">

	<xsl:include href="./lib/recursive-copy.xsl"/>
	<xsl:include href="./lib/dtbook-output.xsl"/>
	
	<xsl:variable name="rearCoverNode" select="/dtb:dtbook/dtb:book/dtb:frontmatter/dtb:level1[@class='jacketcopy']/dtb:prodnote[@class='rearcover']"/>
	<xsl:template match="dtb:level1[parent::dtb:frontmatter and @class='jacketcopy']"/>
	
	<xsl:template match="dtb:bodymatter">
		<xsl:call-template name="copy"/>
		<xsl:if test="not(/dtb:dtbook/dtb:book/dtb:rearmatter) and $rearCoverNode">
			<xsl:element name="rearmatter" namespace="http://www.daisy.org/z3986/2005/dtbook/">
				<xsl:call-template name="insertRearCover"/>
			</xsl:element>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="dtb:rearmatter">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates/>
			<xsl:call-template name="insertRearCover"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template name="insertRearCover">
		<xsl:if test="$rearCoverNode">
			<xsl:element name="level1" namespace="http://www.daisy.org/z3986/2005/dtbook/">
				<xsl:attribute name="class">rearjacketcopy</xsl:attribute>
				
				<xsl:choose>
					<xsl:when test="$rearCoverNode[count(node())=1 and count(text())=1]">
						<xsl:element name="p" namespace="http://www.daisy.org/z3986/2005/dtbook/">
							<xsl:copy-of select="$rearCoverNode/node()"/>
						</xsl:element>
					</xsl:when>
					<xsl:otherwise><xsl:copy-of select="$rearCoverNode/node()"/></xsl:otherwise>
				</xsl:choose>
			</xsl:element>
		</xsl:if>
	</xsl:template>

</xsl:stylesheet>