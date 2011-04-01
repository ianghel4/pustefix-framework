<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:ixsl="http://www.w3.org/1999/XSL/TransformOutputAlias"
                xmlns:pfx="http://www.schlund.de/pustefix/core"
                xmlns:rex="java:de.schlund.pfixxml.RenderExtensionSaxon1"
                exclude-result-prefixes="rex">

  <xsl:param name="__rendercontext__"/>

  <xsl:param name="render_href"/>
  <xsl:param name="render_part"/>
  <xsl:param name="render_module"/>
  <xsl:param name="render_search"/>

  <xsl:template name="__render_start__">
    <xsl:if test="rex:renderStart($__rendercontext__)"/>
  </xsl:template>

  <xsl:template match="pfx:rendercontent">
    <xsl:call-template name="pfx:include">
      <xsl:with-param name="href"><xsl:value-of select="$render_href"/></xsl:with-param>
      <xsl:with-param name="part"><xsl:value-of select="$render_part"/></xsl:with-param>
      <xsl:with-param name="module"><xsl:value-of select="$render_module"/></xsl:with-param>
      <xsl:with-param name="search"><xsl:value-of select="$render_search"/></xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="pfx:render">
    <ixsl:call-template name="pfx:render">
      <ixsl:with-param name="href">
        <xsl:choose>
          <xsl:when test="pfx:href"><xsl:apply-templates select="pfx:href/node()"/></xsl:when>
          <xsl:otherwise><xsl:value-of select="@href"/></xsl:otherwise>
        </xsl:choose>
      </ixsl:with-param>
      <ixsl:with-param name="part">
        <xsl:choose>
          <xsl:when test="pfx:part"><xsl:apply-templates select="pfx:part/node()"/></xsl:when>
          <xsl:otherwise><xsl:value-of select="@part"/></xsl:otherwise>
        </xsl:choose>
      </ixsl:with-param>
      <ixsl:with-param name="module">
        <xsl:choose>
          <xsl:when test="pfx:module"><xsl:apply-templates select="pfx:module/node()"/></xsl:when>
          <xsl:otherwise><xsl:value-of select="@module"/></xsl:otherwise>
        </xsl:choose>
      </ixsl:with-param>
      <ixsl:with-param name="search"><xsl:value-of select="@search"/></ixsl:with-param>
    </ixsl:call-template>
  </xsl:template>
  
  <xsl:template name="pfx:render">
    <xsl:param name="href"/>
    <xsl:param name="part"/>
    <xsl:param name="module"/>
    <xsl:param name="search"/>
    <xsl:if test="rex:render($__target_gen, $href, $part, $module, $search, node(), $__context__, $__rendercontext__)"/>
  </xsl:template>

</xsl:stylesheet>
