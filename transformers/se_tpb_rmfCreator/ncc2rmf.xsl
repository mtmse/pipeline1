<?xml version="1.0" encoding="utf-8"?>

<xsl:transform version="1.0" 
               xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
               xmlns="http://www.w3.org/1999/xhtml"
               xmlns:h="http://www.w3.org/1999/xhtml"
               exclude-result-prefixes="h">

	<xsl:output method="text" 
	      encoding="iso-8859-1" 
	      indent="no" 
	/>
	
	<xsl:param name="num_images" select="'0'"/>
	<xsl:param name="current_volume" select="'1'"/>
	<xsl:param name="num_volumes" select="'1'"/>
	
	<xsl:template match="/">
		<xsl:text>[MergeMetaData]&#13;&#10;</xsl:text>
		<xsl:apply-templates select="//h:meta[@name='dc:identifier'][1]"/>
		<xsl:apply-templates select="//h:meta[@name='dc:title'][1]"/>
		<xsl:apply-templates select="//h:meta[@name='dc:creator'][1]"/>
		<xsl:apply-templates select="//h:meta[@name='dc:publisher'][1]"/>
		<xsl:apply-templates select="//h:meta[@name='dc:format'][1]"/>
		<xsl:call-template name="print">
			<xsl:with-param name="name" select="'ncc:setInfo'"/>
			<xsl:with-param name="value">
				<xsl:value-of select="$current_volume"/>
				<xsl:text> of </xsl:text>
				<xsl:value-of select="$num_volumes"/>
			</xsl:with-param>
		</xsl:call-template>
		<xsl:apply-templates select="//h:meta[@name='ncc:totalTime'][1]"/>
		<xsl:apply-templates select="//h:meta[@name='ncc:narrator'][1]"/>
		<xsl:call-template name="print">
			<xsl:with-param name="name" select="'tpb:copies'"/>
			<xsl:with-param name="value" select="'1'"/>
		</xsl:call-template>
		<xsl:apply-templates select="//h:meta[@name='dc:date'][1]"/>
		<xsl:apply-templates select="//h:meta[@name='ncc:maxPageNormal'][1]"/>
		<xsl:apply-templates select="//h:meta[@name='ncc:multimediaType'][1]"/>
		
		<xsl:call-template name="print">
			<xsl:with-param name="name" select="'illustrationer'"/>
			<xsl:with-param name="value" select="$num_images"/>
		</xsl:call-template>		
		
	</xsl:template>
	
	<xsl:template name="print">
		<xsl:param name="name"/>
		<xsl:param name="value"/>
		<xsl:value-of select="$name"/>
		<xsl:text> = </xsl:text>
		<xsl:value-of select="$value"/>
		<xsl:text>&#13;&#10;</xsl:text>
	</xsl:template>
	
	<xsl:template match="h:meta[@name='dc:identifier']">
		<xsl:call-template name="print">
			<xsl:with-param name="name" select="@name"/>
			<xsl:with-param name="value" select="@content"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template match="h:meta[@name='dc:title']">
		<xsl:call-template name="print">
			<xsl:with-param name="name" select="@name"/>
			<xsl:with-param name="value">
				<xsl:choose>
					<xsl:when test="contains(@content, ':')">
						<xsl:value-of select="substring-before(@content, ':')"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="@content"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:with-param>			 
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template match="h:meta[@name='dc:creator']">
		<xsl:call-template name="print">
			<xsl:with-param name="name" select="@name"/>
			<xsl:with-param name="value" select="@content"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template match="h:meta[@name='dc:publisher']">
		<xsl:call-template name="print">
			<xsl:with-param name="name" select="@name"/>
			<xsl:with-param name="value" select="@content"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template match="h:meta[@name='dc:format']">
		<xsl:call-template name="print">
			<xsl:with-param name="name" select="@name"/>
			<xsl:with-param name="value" select="@content"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template match="h:meta[@name='ncc:setInfo']">
		<xsl:call-template name="print">
			<xsl:with-param name="name" select="@name"/>
			<xsl:with-param name="value" select="@content"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template match="h:meta[@name='ncc:totalTime']">
		<xsl:call-template name="print">
			<xsl:with-param name="name" select="@name"/>
			<xsl:with-param name="value" select="@content"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template match="h:meta[@name='ncc:narrator']">
		<xsl:call-template name="print">
			<xsl:with-param name="name" select="@name"/>
			<xsl:with-param name="value" select="@content"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template match="h:meta[@name='dc:date']">
		<xsl:call-template name="print">
			<xsl:with-param name="name" select="@name"/>
			<xsl:with-param name="value" select="@content"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template match="h:meta[@name='ncc:maxPageNormal']">
		<xsl:call-template name="print">
			<xsl:with-param name="name" select="@name"/>
			<xsl:with-param name="value" select="@content"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template match="h:meta[@name='ncc:multimediaType']">
		<xsl:call-template name="print">
			<xsl:with-param name="name" select="@name"/>
			<xsl:with-param name="value" select="@content"/>
		</xsl:call-template>
	</xsl:template>
	
	
	
</xsl:transform>