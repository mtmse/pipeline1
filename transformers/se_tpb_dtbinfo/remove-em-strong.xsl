<?xml version="1.0"?>
<!--
	remove em/strong
		Version
			2007-09-10

		Description
			Removes em and strong in dtbook

		Nodes
			* em
			* strong

		Namespaces
			( ) ""
			(x) "http://www.daisy.org/z3986/2005/dtbook/"

		Doctype assignment
			(x) DTBook
			( ) None

		Tests
			XMLSpy XSLT engine	( ) 2005	( ) 2006	( ) 2007
			MSXML				( ) 3.0		( ) 4.0
			Saxon				(x) 6.5.3	( ) 8.8

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
	<xsl:include href="./lib/validation-tests.xsl"/>
	
	<xsl:template match="/">
		<xsl:call-template name="verify-dtbook-ns"/>
		<xsl:apply-templates/>
	</xsl:template>

	<xsl:template match="dtb:em|dtb:strong">
		<xsl:apply-templates/>
	</xsl:template>
	
</xsl:stylesheet>