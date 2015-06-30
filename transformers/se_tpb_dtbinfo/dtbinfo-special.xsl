<?xml version="1.0"?>
<!--
	dtbinfo
		Version
			2008-06-25

		Description
			Prepares a dtbook for use in DTB-production at MTM by:
				* Inserting a "blank page" prodnote where needed
					(no text or images between two pagenum's)
				* Flyttar pagenum innuti ord till efter ordet (undantaget xml:space='preserve')
				* Skapar rubriker i form av kapitelnummer för kapitel på level1 eller level2 som saknar rubrik.
				* Insert title as h1 if only one level1 in bodymatter

			Note that:
				* dc:Publisher must be either TPB, MTM or SIT
				* the main language must be swedish or english

			To change the phrasing in a particular language,
				see ./localizations/dtbinfo.xml

			Use parameters 'synthetic' and 'year' to prepare for synthetic speech

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
	<xsl:include href="./lib/validation-tests.xsl"/>
	<xsl:include href="./lib/messages/errors.xsl"/>
	<xsl:include href="./lib/localization.xsl"/>
	
	<xsl:param name="synthetic" select="false()"/>
	<xsl:param name="year" select="2007"/>

	<xsl:variable name="lang">
		<xsl:for-each select="/dtb:dtbook">
			<xsl:choose>
				<xsl:when test="lang('sv')">sv</xsl:when>
				<xsl:when test="lang('en')">en</xsl:when>
				<xsl:when test="lang('no')">sv</xsl:when>
				<xsl:otherwise>
					<xsl:message terminate="yes">
						<xsl:value-of select="$errorMsg-undefinedLanguage"/>
					</xsl:message>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
	</xsl:variable>
	
	<xsl:variable name="data" select="document('./localizations/dtbinfo.xml')//language[lang($lang)]"/>

	<xsl:template match="dtb:dtbook">
		<xsl:call-template name="verify-dtbook-ns"/>
		<xsl:call-template name="verify-xml-lang-on-root"/>
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>

	
	<!-- Insert title as h1 if only one level1 in bodymatter -->
	<xsl:template match="dtb:bodymatter/dtb:level1[count(//dtb:bodymatter/dtb:level1)=1 and not(dtb:h1)]" priority="10">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates select="node()[position()=1 and self::dtb:pagenum]"/>
			<xsl:element name="h1">
				<xsl:value-of select="/dtb:dtbook/dtb:head/dtb:meta[@name='dc:Title'][1]/@content"/>
			</xsl:element>
			<xsl:apply-templates select="node()[not(position()=1 and self::dtb:pagenum)]"/>
		</xsl:copy>
	</xsl:template>
	<!-- / Insert title as h1 if only one level1 in bodymatter -->
	
	<!-- Number chapters -->
	<xsl:template match="dtb:level1[not(dtb:h1)]">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates select="node()[position()=1 and self::dtb:pagenum]"/>
			<xsl:choose>
				<xsl:when test="ancestor::dtb:frontmatter">
					<xsl:element name="h1">
						<xsl:value-of select="$data/string[@ref='headingFront']"/>
						<xsl:variable name="fmsCount" select="count(preceding::*[self::dtb:level1 and ancestor::dtb:frontmatter]) + 1"/>
						<xsl:if test="$fmsCount>1">
							<xsl:value-of select="concat(' ', $fmsCount)"/>
						</xsl:if>
					</xsl:element>					
				</xsl:when>
				<xsl:when test="ancestor::dtb:bodymatter">
					<!-- Damn XSLT 1.0! -->
					<xsl:variable name="node1" select="descendant::text()[not(ancestor::dtb:pagenum)][1]"/>
					<xsl:variable name="node2" select="descendant::text()[not(ancestor::dtb:pagenum)][2]"/>
					<xsl:variable name="node3" select="descendant::text()[not(ancestor::dtb:pagenum)][3]"/>
					<xsl:variable name="oneString" select="normalize-space(concat($node1, ' ', $node2, ' ', $node3))"/>
					<xsl:variable name="threeWords">
						<xsl:call-template name="getWords">
							<xsl:with-param name="str" select="$oneString"/>
							<xsl:with-param name="target" select="3"/>
						</xsl:call-template>
					</xsl:variable>
					<xsl:element name="h1">
						<xsl:value-of select="concat(normalize-space($threeWords), '...')"/>
					</xsl:element>
				</xsl:when>
				<xsl:when test="ancestor::dtb:rearmatter">
					<xsl:element name="h1">
						<xsl:choose>
							<xsl:when test="count(*)=count(dtb:note) and count(*)>0">
								<xsl:value-of select="$data/string[@ref='notes']"/>
							</xsl:when>
							<xsl:when test="@class='rearjacketcopy'">
								<xsl:value-of select="$data/string[@ref='rearJacketCopy']"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="$data/string[@ref='headingRear']"/>
								<xsl:variable name="rmsCount" select="count(preceding::*[self::dtb:level1 and ancestor::dtb:rearmatter]) + 1"/>
								<xsl:if test="$rmsCount>1">
									<xsl:value-of select="concat(' ', $rmsCount)"/>
								</xsl:if>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:element>
				</xsl:when>
				<xsl:otherwise>
					<xsl:message terminate="yes">Error: DTBInfo - Unknown ancestor</xsl:message>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:apply-templates select="node()[not(position()=1 and self::dtb:pagenum)]"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template name="getWords">
		<xsl:param name="str"/>
		<xsl:param name="target" select="0"/>
		<xsl:param name="wi" select="0"/>
		<xsl:choose>
			<xsl:when test="$wi>=$target"><!-- We're done --></xsl:when>
			<xsl:otherwise>
				<!-- Tail recursion -->
				<xsl:choose>
					<xsl:when test="contains($str, ' ')">
						<xsl:value-of select="concat(substring-before($str, ' '), ' ')"/>
						<xsl:variable name="str2" select="substring-after($str, ' ')"/>
						<xsl:call-template name="getWords">
							<xsl:with-param name="str" select="$str2"/>
							<xsl:with-param name="target" select="$target"/>
							<xsl:with-param name="wi" select="$wi+1"/>
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<!-- No more words, stop recursion even if target not reached -->
						<xsl:message terminate="no">WARNING: getWords returned before reaching the target number of words</xsl:message>
						<xsl:value-of select="$str"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!--
	<xsl:template match="dtb:level2[not(dtb:h2)]">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates select="node()[position()=1 and self::dtb:pagenum]"/>
			<xsl:element name="h2">
				<xsl:value-of select="count(preceding::*[self::dtb:level1 or self::dtb:level2]) + 1"/>
			</xsl:element>
			<xsl:apply-templates select="node()[not(position()=1 and self::dtb:pagenum)]"/>
		</xsl:copy>
	</xsl:template>-->
	<!-- / Number chapters -->

	<!-- Pagenum processing -->
	
	<!-- Ignore these text nodes, they are processed below -->
	<xsl:template match="text()[normalize-space()!='' and preceding-sibling::node()[1][self::dtb:pagenum] and
							preceding-sibling::node()[2][self::text() and normalize-space()!=''] and not(ancestor-or-self::*[@xml:space][1]/@xml:space='preserve')]"/>
							
	<xsl:template match="dtb:pagenum">
		<xsl:choose>
			<xsl:when test="generate-id(following::text()[normalize-space(.)!=''][1])=generate-id(following::text()[ancestor::dtb:pagenum][1])">
				<xsl:call-template name="blankPageProdnote"/>
			</xsl:when>
			<xsl:when test="preceding-sibling::node()[1][self::text() and normalize-space()!=''] and following-sibling::node()[1][self::text() and normalize-space()!=''] and not(ancestor-or-self::*[@xml:space][1]/@xml:space='preserve')">
				<xsl:call-template name="movePagenumInWords"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="copy"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template name="blankPageProdnote">
		<xsl:variable name="current-id" select="@id"/>
		<xsl:call-template name="copy"/>
		<xsl:variable name="insert"><xsl:for-each select="(following::dtb:img|following::dtb:imggroup)[1]"><xsl:if test="preceding::dtb:pagenum[1]/@id=$current-id">0</xsl:if></xsl:for-each></xsl:variable>
		<xsl:if test="$insert!=0">
			<xsl:element name="prodnote">
				<xsl:attribute name="render">optional</xsl:attribute>
				<xsl:value-of select="$data/string[@ref='blank-page']"/>
			</xsl:element>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="movePagenumInWords">
		<xsl:variable name="A1" select="translate(following-sibling::node()[1], '&#x9;&#xa;&#xd;', '   ')"/>
		<xsl:variable name="A2" select="translate(preceding-sibling::node()[1], '&#x9;&#xa;&#xd;', '   ')"/>
		<xsl:choose>
			<!--  ends-with: substring($A, string-length($A) - string-length($B) + 1) = $B
                  Se XSLT programmers reference, second edition, Michael Kay, sidan 541 -->
			<!-- Om föregående textnod slutar med mellanslag eller om nästkommande textnod börjar med mellanslag så ska denna tagg inte flyttas. -->
			<xsl:when test="starts-with($A1, ' ') or substring($A2, string-length($A2))=' '">
				<xsl:call-template name="copy"/>
				<xsl:value-of select="following-sibling::node()[1]"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:choose>
					<xsl:when test="contains($A1,' ')">
						<xsl:value-of select="substring-before($A1,' ')"/>
						<xsl:call-template name="copy"/>
						<xsl:value-of select="concat(' ',substring-after($A1,' '))"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="following-sibling::node()[1]"/>
						<xsl:call-template name="copy"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>	
	</xsl:template>
	<!-- /Pagenum processing -->

</xsl:stylesheet>