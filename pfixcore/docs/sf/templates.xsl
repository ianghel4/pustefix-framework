<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:saxon="http://icl.com/saxon"
                extension-element-prefixes="saxon" version="1.0">

  <xsl:output method="html"/>

  <xsl:template match="page">
    <xsl:message>*** Generating <xsl:value-of select="@name"/> </xsl:message>
    <saxon:output href="gen/{@name}.html">
      <html>
        <head>
          <title>Pustefix: <xsl:value-of select="@title"/></title>
          <link rel="stylesheet" href="styles.css"/>
        </head>
        
        <body>
          <table border="0" cellpadding="0" cellspacing="0" width="100%" height="100%">
            <tr>
              <td colspan="2" id="top">
                The Pustefix Framework
              </td>
            </tr>
            <tr valign="top" height="100%">
              <td class="navibody">
                <xsl:call-template name="gen_navi">
                  <xsl:with-param name="thepage"><xsl:value-of select="@name"/></xsl:with-param>
                  <xsl:with-param name="parent" select="/pagedef"/>
                </xsl:call-template>
              </td>
              <td class="mainbody">
                <xsl:apply-templates select="document(concat(@name, '_main.xml'))">
                  <xsl:with-param name="thepage"><xsl:value-of select="@name"/></xsl:with-param></xsl:apply-templates>
              </td>
            </tr>
            <tr valign="bottom">
              <td class="navibody">
                <div style="position: fixed; bottom: 0%; left: 0px">
                  <a href="http://sourceforge.net"><img src="http://sourceforge.net/sflogo.php?group_id=72089&amp;type=4" width="125" height="37" border="0" alt="SourceForge.net Logo" /></a>
                </div>
              </td>
              <td class="mainbody"/>
            </tr>
          </table>
        </body>
      </html>
    </saxon:output>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="pustefix">
    <i><b>Pustefix</b></i>
  </xsl:template>
  
  <xsl:template match="*">
    <xsl:copy>
      <xsl:copy-of select="./@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template name="gen_navi">
    <xsl:param name="parent"/>
    <xsl:param name="thepage"/>
    <div id="navigation">
      <xsl:for-each select="$parent/page">
        <xsl:variable name="depth" select="count(ancestor::page)"/>
        <xsl:variable name="class"><xsl:choose>
            <xsl:when test="$depth = 0">menuentry</xsl:when>
            <xsl:otherwise>submenuentry</xsl:otherwise></xsl:choose></xsl:variable>
        <a>
          <xsl:attribute name="href"><xsl:value-of select="@name"/>.html</xsl:attribute>
          <div class="menuentry">
            <xsl:attribute name="class">
              <xsl:value-of select="$class"/>
              <xsl:if test="@name = $thepage"> selected</xsl:if>
            </xsl:attribute>
            <xsl:value-of select="@title"/>
          </div>
        </a>
        <xsl:if test="@name = $thepage or .//page[@name = $thepage]"><xsl:call-template name="gen_navi">
            <xsl:with-param name="thepage"><xsl:value-of select="$thepage"/></xsl:with-param>
            <xsl:with-param name="parent" select="."/>
          </xsl:call-template></xsl:if>
      </xsl:for-each>
    </div>
  </xsl:template>
</xsl:stylesheet>
