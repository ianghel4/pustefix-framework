<xsl:stylesheet version="1.0"
                exclude-result-prefixes="xsl cus" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pfx="http://www.schlund.de/pustefix/core"
                xmlns:cus="http://www.schlund.de/pustefix/customize"
                xmlns:ixsl="http://www.w3.org/1999/XSL/TransformOutputAlias">
  
  <xsl:output method="text" encoding="ISO-8859-1" indent="no"/>


  <xsl:template match="/">
    <xsl:apply-templates select="/checkresult/incfile"/>
  </xsl:template>

  <xsl:template match="incfile">
    <xsl:choose>
      <xsl:when test="not(./part/product[not(@UNUSED = 'true')])">
FILE UNUSED: <xsl:value-of select="@name"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="./part">
          <xsl:with-param name="file"><xsl:value-of select="@name"/></xsl:with-param>
        </xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="part">
    <xsl:param name="file"/>
    <xsl:choose>
      <xsl:when test="not(./product[not(@UNUSED = 'true')])">
PART UNUSED: <xsl:value-of select="$file"/> => <xsl:value-of select="@name"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="./product">
          <xsl:with-param name="file"><xsl:value-of select="$file"/></xsl:with-param>
          <xsl:with-param name="part"><xsl:value-of select="@name"/></xsl:with-param>
        </xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="product">
    <xsl:param name="file"/>
    <xsl:param name="part"/>
    <xsl:if test="@UNUSED = 'true'">
PROD UNUSED: <xsl:value-of select="$file"/> => <xsl:value-of select="$part"/> => <xsl:value-of select="@name"/>
    </xsl:if>
  </xsl:template>
  
</xsl:stylesheet>
