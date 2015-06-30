<?xml version="1.0" encoding="UTF-8"?>
<!--
	Localization module
		Version
			2007-01-31

		Description
			Enables easy localization

		Nodes
			N/A

		Namespaces
			N/A

		Doctype assignment
			N/A

		Tests
			XMLSpy XSLT engine	( ) 2005	( ) 2006	(-) 2007
			MSXML				( ) 3.0		(x) 4.0
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
	
	<xsl:template name="variableFormatter">
		<xsl:param name="value"/>
		<xsl:param name="lookup"/>
		<xsl:choose>
			<xsl:when test="$lookup/item[@name=$value]">
				<xsl:value-of select="$lookup/item[@name=$value]/@value"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$value"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="localizeString">
		<xsl:param name="context"/>
		<xsl:param name="arg1"/>
		<xsl:param name="arg2"/>
		<xsl:param name="arg3"/>
		<xsl:param name="arg4"/>
		<xsl:param name="arg5"/>
		<xsl:for-each select="$context/node()">
			<xsl:choose>
				<xsl:when test="self::test">
					<xsl:variable name="value">
						<xsl:call-template name="getArgValue">
							<xsl:with-param name="no" select="@vref"/>
							<xsl:with-param name="arg1" select="$arg1"/>
							<xsl:with-param name="arg2" select="$arg2"/>
							<xsl:with-param name="arg3" select="$arg3"/>
							<xsl:with-param name="arg4" select="$arg4"/>
							<xsl:with-param name="arg5" select="$arg5"/>
						</xsl:call-template>
					</xsl:variable>
					<xsl:if test="$value&gt;=@low and $value&lt;=@high or $value&gt;=@low and not(@high) or not(@low) and $value&lt;=@high">
						<xsl:call-template name="localizeString">
							<xsl:with-param name="context" select="."/>
							<xsl:with-param name="arg1" select="$arg1"/>
							<xsl:with-param name="arg2" select="$arg2"/>
							<xsl:with-param name="arg3" select="$arg3"/>
							<xsl:with-param name="arg4" select="$arg4"/>
							<xsl:with-param name="arg5" select="$arg5"/>
						</xsl:call-template>
					</xsl:if>
				</xsl:when>
				<xsl:when test="self::tests">
					<xsl:if test="if/@type='or'">
						<xsl:call-template name="runTest-OR">
							<xsl:with-param name="context" select="if/condition[1]"/>
							<xsl:with-param name="arg1" select="$arg1"/>
							<xsl:with-param name="arg2" select="$arg2"/>
							<xsl:with-param name="arg3" select="$arg3"/>
							<xsl:with-param name="arg4" select="$arg4"/>
							<xsl:with-param name="arg5" select="$arg5"/>
						</xsl:call-template>
					</xsl:if>
					<xsl:if test="if/@type='and'">
						<xsl:call-template name="runTest-AND">
							<xsl:with-param name="context" select="if/condition[1]"/>
							<xsl:with-param name="arg1" select="$arg1"/>
							<xsl:with-param name="arg2" select="$arg2"/>
							<xsl:with-param name="arg3" select="$arg3"/>
							<xsl:with-param name="arg4" select="$arg4"/>
							<xsl:with-param name="arg5" select="$arg5"/>
						</xsl:call-template>
					</xsl:if>
					<xsl:if test="if/@type='xor'">
						<xsl:call-template name="runTest-XOR">
							<xsl:with-param name="context" select="if/condition[1]"/>
							<xsl:with-param name="arg1" select="$arg1"/>
							<xsl:with-param name="arg2" select="$arg2"/>
							<xsl:with-param name="arg3" select="$arg3"/>
							<xsl:with-param name="arg4" select="$arg4"/>
							<xsl:with-param name="arg5" select="$arg5"/>
						</xsl:call-template>
					</xsl:if>
					<xsl:if test="if/@type='nor'">
						<xsl:call-template name="runTest-NOR">
							<xsl:with-param name="context" select="if/condition[1]"/>
							<xsl:with-param name="arg1" select="$arg1"/>
							<xsl:with-param name="arg2" select="$arg2"/>
							<xsl:with-param name="arg3" select="$arg3"/>
							<xsl:with-param name="arg4" select="$arg4"/>
							<xsl:with-param name="arg5" select="$arg5"/>
						</xsl:call-template>
					</xsl:if>
					<xsl:if test="if/@type='nand'">
						<xsl:call-template name="runTest-NAND">
							<xsl:with-param name="context" select="if/condition[1]"/>
							<xsl:with-param name="arg1" select="$arg1"/>
							<xsl:with-param name="arg2" select="$arg2"/>
							<xsl:with-param name="arg3" select="$arg3"/>
							<xsl:with-param name="arg4" select="$arg4"/>
							<xsl:with-param name="arg5" select="$arg5"/>
						</xsl:call-template>
					</xsl:if>
				</xsl:when>
				<xsl:when test="self::insert">
					<xsl:variable name="value">
						<xsl:call-template name="getArgValue">
							<xsl:with-param name="no" select="@vref"/>
							<xsl:with-param name="arg1" select="$arg1"/>
							<xsl:with-param name="arg2" select="$arg2"/>
							<xsl:with-param name="arg3" select="$arg3"/>
							<xsl:with-param name="arg4" select="$arg4"/>
							<xsl:with-param name="arg5" select="$arg5"/>
						</xsl:call-template>
					</xsl:variable>
					<xsl:variable name="lref" select="@lref"/>
					<xsl:call-template name="variableFormatter">
						<xsl:with-param name="value" select="$value"/>
						<xsl:with-param name="lookup" select="ancestor::language/lookup[@ref=$lref]"/>
					</xsl:call-template>
				</xsl:when>
				<xsl:when test="self::text() and normalize-space(.)!=''"><xsl:value-of select="."/></xsl:when>
			</xsl:choose>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template name="getArgValue">
		<xsl:param name="no"/>
		<xsl:param name="arg1"/>
		<xsl:param name="arg2"/>
		<xsl:param name="arg3"/>
		<xsl:param name="arg4"/>
		<xsl:param name="arg5"/>
		<xsl:choose>
			<xsl:when test="$no=1"><xsl:value-of select="$arg1"/></xsl:when>
			<xsl:when test="$no=2"><xsl:value-of select="$arg2"/></xsl:when>
			<xsl:when test="$no=3"><xsl:value-of select="$arg3"/></xsl:when>
			<xsl:when test="$no=4"><xsl:value-of select="$arg4"/></xsl:when>
			<xsl:when test="$no=5"><xsl:value-of select="$arg5"/></xsl:when>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template name="runTest-AND">
		<xsl:param name="context"/>
		<xsl:param name="arg1"/>
		<xsl:param name="arg2"/>
		<xsl:param name="arg3"/>
		<xsl:param name="arg4"/>
		<xsl:param name="arg5"/>
		<xsl:for-each select="$context">
			<xsl:variable name="value">
				<xsl:call-template name="getArgValue">
					<xsl:with-param name="no" select="@vref"/>
					<xsl:with-param name="arg1" select="$arg1"/>
					<xsl:with-param name="arg2" select="$arg2"/>
					<xsl:with-param name="arg3" select="$arg3"/>
					<xsl:with-param name="arg4" select="$arg4"/>
					<xsl:with-param name="arg5" select="$arg5"/>
				</xsl:call-template>
			</xsl:variable>
			<xsl:choose>
				<xsl:when test="$value&gt;=@low and $value&lt;=@high or $value&gt;=@low and not(@high) or not(@low) and $value&lt;=@high">
					<xsl:choose>
						<xsl:when test="following-sibling::condition[1]">
							<xsl:call-template name="runTest-AND">
								<xsl:with-param name="context" select="following-sibling::condition[1]"/>
								<xsl:with-param name="arg1" select="$arg1"/>
								<xsl:with-param name="arg2" select="$arg2"/>
								<xsl:with-param name="arg3" select="$arg3"/>
								<xsl:with-param name="arg4" select="$arg4"/>
								<xsl:with-param name="arg5" select="$arg5"/>
							</xsl:call-template>
						</xsl:when>
						<xsl:otherwise>
							<!-- Success -->
							<xsl:call-template name="localizeString">
								<xsl:with-param name="context" select="../../then"/>
								<xsl:with-param name="arg1" select="$arg1"/>
								<xsl:with-param name="arg2" select="$arg2"/>
								<xsl:with-param name="arg3" select="$arg3"/>
								<xsl:with-param name="arg4" select="$arg4"/>
								<xsl:with-param name="arg5" select="$arg5"/>
							</xsl:call-template>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:when>
				<xsl:otherwise>
					<!-- Fail -->
					<xsl:call-template name="localizeString">
						<xsl:with-param name="context" select="../../else"/>
						<xsl:with-param name="arg1" select="$arg1"/>
						<xsl:with-param name="arg2" select="$arg2"/>
						<xsl:with-param name="arg3" select="$arg3"/>
						<xsl:with-param name="arg4" select="$arg4"/>
						<xsl:with-param name="arg5" select="$arg5"/>
					</xsl:call-template>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template name="runTest-OR">
		<xsl:param name="context"/>
		<xsl:param name="arg1"/>
		<xsl:param name="arg2"/>
		<xsl:param name="arg3"/>
		<xsl:param name="arg4"/>
		<xsl:param name="arg5"/>
		<xsl:for-each select="$context">
			<xsl:variable name="value">
				<xsl:call-template name="getArgValue">
					<xsl:with-param name="no" select="@vref"/>
					<xsl:with-param name="arg1" select="$arg1"/>
					<xsl:with-param name="arg2" select="$arg2"/>
					<xsl:with-param name="arg3" select="$arg3"/>
					<xsl:with-param name="arg4" select="$arg4"/>
					<xsl:with-param name="arg5" select="$arg5"/>
				</xsl:call-template>
			</xsl:variable>
			<xsl:choose>
				<xsl:when test="$value&gt;=@low and $value&lt;=@high or $value&gt;=@low and not(@high) or not(@low) and $value&lt;=@high">
					<!-- Success -->
					<xsl:call-template name="localizeString">
						<xsl:with-param name="context" select="../../then"/>
						<xsl:with-param name="arg1" select="$arg1"/>
						<xsl:with-param name="arg2" select="$arg2"/>
						<xsl:with-param name="arg3" select="$arg3"/>
						<xsl:with-param name="arg4" select="$arg4"/>
						<xsl:with-param name="arg5" select="$arg5"/>
					</xsl:call-template>
				</xsl:when>
				<xsl:otherwise>
					<xsl:choose>
						<xsl:when test="following-sibling::condition[1]">
							<xsl:call-template name="runTest-OR">
								<xsl:with-param name="context" select="following-sibling::condition[1]"/>
								<xsl:with-param name="arg1" select="$arg1"/>
								<xsl:with-param name="arg2" select="$arg2"/>
								<xsl:with-param name="arg3" select="$arg3"/>
								<xsl:with-param name="arg4" select="$arg4"/>
								<xsl:with-param name="arg5" select="$arg5"/>
							</xsl:call-template>
						</xsl:when>
						<xsl:otherwise>
							<!-- Fail -->
							<xsl:call-template name="localizeString">
								<xsl:with-param name="context" select="../../else"/>
								<xsl:with-param name="arg1" select="$arg1"/>
								<xsl:with-param name="arg2" select="$arg2"/>
								<xsl:with-param name="arg3" select="$arg3"/>
								<xsl:with-param name="arg4" select="$arg4"/>
								<xsl:with-param name="arg5" select="$arg5"/>
							</xsl:call-template>
						</xsl:otherwise>
					</xsl:choose>			
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template name="runTest-NOR">
		<xsl:param name="context"/>
		<xsl:param name="arg1"/>
		<xsl:param name="arg2"/>
		<xsl:param name="arg3"/>
		<xsl:param name="arg4"/>
		<xsl:param name="arg5"/>
		<xsl:for-each select="$context">
			<xsl:variable name="value">
				<xsl:call-template name="getArgValue">
					<xsl:with-param name="no" select="@vref"/>
					<xsl:with-param name="arg1" select="$arg1"/>
					<xsl:with-param name="arg2" select="$arg2"/>
					<xsl:with-param name="arg3" select="$arg3"/>
					<xsl:with-param name="arg4" select="$arg4"/>
					<xsl:with-param name="arg5" select="$arg5"/>
				</xsl:call-template>
			</xsl:variable>
			<xsl:choose>
				<xsl:when test="not($value&gt;=@low and $value&lt;=@high or $value&gt;=@low and not(@high) or not(@low) and $value&lt;=@high)">
					<xsl:choose>
						<xsl:when test="following-sibling::condition[1]">
							<xsl:call-template name="runTest-NOR">
								<xsl:with-param name="context" select="following-sibling::condition[1]"/>
								<xsl:with-param name="arg1" select="$arg1"/>
								<xsl:with-param name="arg2" select="$arg2"/>
								<xsl:with-param name="arg3" select="$arg3"/>
								<xsl:with-param name="arg4" select="$arg4"/>
								<xsl:with-param name="arg5" select="$arg5"/>
							</xsl:call-template>
						</xsl:when>
						<xsl:otherwise>
							<!-- Success -->
							<xsl:call-template name="localizeString">
								<xsl:with-param name="context" select="../../then"/>
								<xsl:with-param name="arg1" select="$arg1"/>
								<xsl:with-param name="arg2" select="$arg2"/>
								<xsl:with-param name="arg3" select="$arg3"/>
								<xsl:with-param name="arg4" select="$arg4"/>
								<xsl:with-param name="arg5" select="$arg5"/>
							</xsl:call-template>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:when>
				<xsl:otherwise>
					<!-- Fail -->
					<xsl:call-template name="localizeString">
						<xsl:with-param name="context" select="../../else"/>
						<xsl:with-param name="arg1" select="$arg1"/>
						<xsl:with-param name="arg2" select="$arg2"/>
						<xsl:with-param name="arg3" select="$arg3"/>
						<xsl:with-param name="arg4" select="$arg4"/>
						<xsl:with-param name="arg5" select="$arg5"/>
					</xsl:call-template>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
	</xsl:template>

	<xsl:template name="runTest-XOR">
		<xsl:param name="context"/>
		<xsl:param name="arg1"/>
		<xsl:param name="arg2"/>
		<xsl:param name="arg3"/>
		<xsl:param name="arg4"/>
		<xsl:param name="arg5"/>
		<xsl:for-each select="$context">
			<xsl:variable name="value">
				<xsl:call-template name="getArgValue">
					<xsl:with-param name="no" select="@vref"/>
					<xsl:with-param name="arg1" select="$arg1"/>
					<xsl:with-param name="arg2" select="$arg2"/>
					<xsl:with-param name="arg3" select="$arg3"/>
					<xsl:with-param name="arg4" select="$arg4"/>
					<xsl:with-param name="arg5" select="$arg5"/>
				</xsl:call-template>
			</xsl:variable>
			<xsl:choose>
				<xsl:when test="$value&gt;=@low and $value&lt;=@high or $value&gt;=@low and not(@high) or not(@low) and $value&lt;=@high">
					<xsl:choose>
						<xsl:when test="following-sibling::condition[1]">
							<xsl:call-template name="runTest-NOR">
								<xsl:with-param name="context" select="following-sibling::condition[1]"/>
								<xsl:with-param name="arg1" select="$arg1"/>
								<xsl:with-param name="arg2" select="$arg2"/>
								<xsl:with-param name="arg3" select="$arg3"/>
								<xsl:with-param name="arg4" select="$arg4"/>
								<xsl:with-param name="arg5" select="$arg5"/>
							</xsl:call-template>
						</xsl:when>
						<xsl:otherwise>
							<!-- Success -->
							<xsl:call-template name="localizeString">
								<xsl:with-param name="context" select="../../then"/>
								<xsl:with-param name="arg1" select="$arg1"/>
								<xsl:with-param name="arg2" select="$arg2"/>
								<xsl:with-param name="arg3" select="$arg3"/>
								<xsl:with-param name="arg4" select="$arg4"/>
								<xsl:with-param name="arg5" select="$arg5"/>
							</xsl:call-template>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:when>
				<xsl:otherwise>
					<xsl:choose>
						<xsl:when test="following-sibling::condition[1]">
							<xsl:call-template name="runTest-XOR">
								<xsl:with-param name="context" select="following-sibling::condition[1]"/>
								<xsl:with-param name="arg1" select="$arg1"/>
								<xsl:with-param name="arg2" select="$arg2"/>
								<xsl:with-param name="arg3" select="$arg3"/>
								<xsl:with-param name="arg4" select="$arg4"/>
								<xsl:with-param name="arg5" select="$arg5"/>
							</xsl:call-template>
						</xsl:when>
						<xsl:otherwise>
							<!-- Fail -->
							<xsl:call-template name="localizeString">
								<xsl:with-param name="context" select="../../else"/>
								<xsl:with-param name="arg1" select="$arg1"/>
								<xsl:with-param name="arg2" select="$arg2"/>
								<xsl:with-param name="arg3" select="$arg3"/>
								<xsl:with-param name="arg4" select="$arg4"/>
								<xsl:with-param name="arg5" select="$arg5"/>
							</xsl:call-template>
						</xsl:otherwise>
					</xsl:choose>			
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template name="runTest-NAND">
		<xsl:param name="context"/>
		<xsl:param name="arg1"/>
		<xsl:param name="arg2"/>
		<xsl:param name="arg3"/>
		<xsl:param name="arg4"/>
		<xsl:param name="arg5"/>
		<xsl:for-each select="$context">
			<xsl:variable name="value">
				<xsl:call-template name="getArgValue">
					<xsl:with-param name="no" select="@vref"/>
					<xsl:with-param name="arg1" select="$arg1"/>
					<xsl:with-param name="arg2" select="$arg2"/>
					<xsl:with-param name="arg3" select="$arg3"/>
					<xsl:with-param name="arg4" select="$arg4"/>
					<xsl:with-param name="arg5" select="$arg5"/>
				</xsl:call-template>
			</xsl:variable>
			<xsl:choose>
				<xsl:when test="$value&gt;=@low and $value&lt;=@high or $value&gt;=@low and not(@high) or not(@low) and $value&lt;=@high">
					<xsl:choose>
						<xsl:when test="following-sibling::condition[1]">
							<xsl:call-template name="runTest-NAND">
								<xsl:with-param name="context" select="following-sibling::condition[1]"/>
								<xsl:with-param name="arg1" select="$arg1"/>
								<xsl:with-param name="arg2" select="$arg2"/>
								<xsl:with-param name="arg3" select="$arg3"/>
								<xsl:with-param name="arg4" select="$arg4"/>
								<xsl:with-param name="arg5" select="$arg5"/>
							</xsl:call-template>
						</xsl:when>
						<xsl:otherwise>
							<!-- Fail -->
							<xsl:call-template name="localizeString">
								<xsl:with-param name="context" select="../../else"/>
								<xsl:with-param name="arg1" select="$arg1"/>
								<xsl:with-param name="arg2" select="$arg2"/>
								<xsl:with-param name="arg3" select="$arg3"/>
								<xsl:with-param name="arg4" select="$arg4"/>
								<xsl:with-param name="arg5" select="$arg5"/>
							</xsl:call-template>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:when>
				<xsl:otherwise>
					<!-- Success -->
					<xsl:call-template name="localizeString">
						<xsl:with-param name="context" select="../../then"/>
						<xsl:with-param name="arg1" select="$arg1"/>
						<xsl:with-param name="arg2" select="$arg2"/>
						<xsl:with-param name="arg3" select="$arg3"/>
						<xsl:with-param name="arg4" select="$arg4"/>
						<xsl:with-param name="arg5" select="$arg5"/>
					</xsl:call-template>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
	</xsl:template>
	
</xsl:stylesheet>