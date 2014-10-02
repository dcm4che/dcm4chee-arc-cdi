<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

  <xsl:output method="html" indent="yes" media-type="text/html"
    encoding="UTF-8" />
  <xsl:param name="wadoURL" />
  <!-- <xsl:variable name="and"><![CDATA[&]]></xsl:variable> -->
  <!-- <xsl:param name="srImageRows" /> -->

  <!-- the stylesheet processing entry point -->
  <xsl:template match="/">
    <xsl:apply-templates select="NativeDicomModel" />
    <!-- <xsl:variable name="studyUID"><xsl:value-of
            select="DicomAttribute[@tag='0040A375']/Item/DicomAttribute[@tag='0020000D']/Value" /></xsl:variable>
    <xsl:variable name="seriesUID"><xsl:value-of
            select="DicomAttribute[@tag='0040A375']/Item/DicomAttribute[@tag='00081115']/Item/DicomAttribute[@tag='0020000E']/Value" /></xsl:variable> -->
            
  </xsl:template>

  <xsl:template match="NativeDicomModel">
    <html>
      <head>
        <title>
          <xsl:value-of
            select="DicomAttribute[@tag='0040A043']/Item/DicomAttribute[@tag='00080104']" />
        </title>
      </head>
      <body>
        <font size="-1">
          By
          <xsl:value-of select="DicomAttribute[@tag='00080080']/Value" />
          , Ref. Phys.
          <xsl:value-of select="DicomAttribute[@tag='00080090']/Value" />
        </font>
        <br />
        <table border="0">
          <tr>
            <td>Patient Name:</td>
            <td>
              <xsl:value-of
                select="DicomAttribute[@tag='00100010']/PersonName/Alphabetic/FamilyName" />
              <xsl:choose>
                <xsl:when
                  test="DicomAttribute[@tag='00100010']/PersonName/Alphabetic/GivenName!=''">
                  ,
                  <xsl:value-of
                    select="DicomAttribute[@tag='00100010']/PersonName/Alphabetic/GivenName" />
                </xsl:when>
              </xsl:choose>

            </td>
          </tr>
          <tr>
            <td>Patient ID:</td>
            <td>
              <xsl:value-of select="DicomAttribute[@tag='00100020']/Value" />
            </td>
          </tr>
          <tr>
            <td>Patient Birthdate:</td>
            <td>
              <xsl:value-of select="DicomAttribute[@tag='00100030']/Value" />
            </td>
          </tr>
          <tr>
            <td>Patient Sex:</td>
            <td>
              <xsl:value-of select="DicomAttribute[@tag='00100040']/Value" />
            </td>
          </tr>
        </table>
        <hr />

        <xsl:apply-templates select="DicomAttribute[@tag='0040A730']/Item"
          mode="content" />

      </body>
    </html>
  </xsl:template>

  <!-- Contentsequence output starts here -->

  <xsl:template match="Item" mode="content">
    <font size="+2">
      <xsl:value-of
        select="DicomAttribute[@tag='0040A043']/Item/DicomAttribute[@tag='00080104']/Value" />
    </font>
    <xsl:apply-templates select="." mode="contentItem" />
    <br />
  </xsl:template>


  <!-- Displays the content in the context of a list -->
  <xsl:template match="Item" mode="contentLI">
    <li>
      <font size="+1">
        <xsl:value-of
          select="DicomAttribute[@tag='0040A043']/Item/DicomAttribute[@tag='00080104']/Value" />
      </font>
      <xsl:apply-templates select="." mode="contentItem" />
    </li>
  </xsl:template>

  <xsl:template mode="contentItem" match="Item">
    <xsl:choose>
      <xsl:when test="DicomAttribute[@tag='0040A040']/Value='TEXT'">
        <p>
          <xsl:call-template name="escape_crlf">
            <xsl:with-param name="string"
              select="DicomAttribute[@tag='0040A160']/Value" />
          </xsl:call-template>
        </p>
      </xsl:when>

      <xsl:when
        test="DicomAttribute[@tag='0040A040']/Value='IMAGE ' or DicomAttribute[@tag='0040A040']/Value='IMAGE'">
<xsl:variable name="objectUID"><xsl:value-of select="DicomAttribute[@tag='00081199']/Item/DicomAttribute[@tag='00081155']/Value"/></xsl:variable>
        <xsl:apply-templates select="/NativeDicomModel/DicomAttribute[@tag='0040A375']/Item/DicomAttribute[@tag='00081115']/Item/DicomAttribute[@tag='00081199']/Item[DicomAttribute[@tag='00081155']/Value=$objectUID]"
          mode="imageref" />
      </xsl:when>

      <xsl:when test="DicomAttribute[@tag='0040A040']/Value='CODE'">
        <xsl:call-template name="escape_crlf">
          <xsl:with-param name="string"
            select="concat(': ',DicomAttribute[@tag='0040A168']/Item/DicomAttribute[@tag='00080104']/Value)" />
        </xsl:call-template>
      </xsl:when>

      <xsl:when
        test="DicomAttribute[@tag='0040A040']/Value='PNAME ' or DicomAttribute[@tag='0040A040']/Value='PNAME'">
        :
        <xsl:value-of select="DicomAttribute[@tag='0040A123']/Value" />
      </xsl:when>

      <xsl:when
        test="DicomAttribute[@tag='0040A040']/Value='NUM ' or DicomAttribute[@tag='0040A040']/Value='NUM'">
        <xsl:value-of
          select="concat(': ',DicomAttribute[@tag='0040A300']/Item/DicomAttribute[@tag='0040A30A']/Value)" />
        <xsl:if
          test="DicomAttribute[@tag='0040A300']/Item/DicomAttribute[@tag='004008EA']/Item/DicomAttribute[@tag='00080100']/Value != 1"> <!-- No unit (UCUM) -->
          <xsl:value-of
            select="concat(' ',DicomAttribute[@tag='0040A300']/Item/DicomAttribute[@tag='004008EA']/Item/DicomAttribute[@tag='00080100']/Value)" />
        </xsl:if>
      </xsl:when>


      <xsl:when
        test="DicomAttribute[@tag='0040A040']/Value='CONTAINER ' or DicomAttribute[@tag='0040A040']/Value='CONTAINER'">
        <ul>
          <xsl:apply-templates select="DicomAttribute[@tag='0040A730']/Item"
            mode="contentLI" />
        </ul>
      </xsl:when>

      <xsl:otherwise>
        <i>
          [
          <xsl:value-of select="DicomAttribute[@tag='0040A040']/Value" />
          ] (Unspecified value)
        </i>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <xsl:template match="Item" mode="imageref">
    Image
    <img align="top">
      <xsl:attribute name="src">
        <xsl:value-of select="$wadoURL" />
        <xsl:text disable-output-escaping="yes">?requestType=WADO&amp;studyUID=</xsl:text>
        <xsl:value-of select="../../../../DicomAttribute[@tag='0020000D']/Value"/>
        <xsl:text>&amp;seriesUID=</xsl:text>
        <xsl:value-of select="../../DicomAttribute[@tag='0020000E']/Value"/>
        <xsl:text>&amp;objectUID=</xsl:text>
        <xsl:value-of select="DicomAttribute[@tag='00081155']/Value"/>
			<!-- 	<xsl:if test="$srImageRows">&amp;rows=<xsl:value-of select="$srImageRows" /></xsl:if> -->
        </xsl:attribute>
    </img>
    <br />
  </xsl:template>

  <xsl:template name="escape_crlf">
    <xsl:param name="string" />
    <xsl:variable name="CR" select="'&#xD;'" />
    <xsl:variable name="LF" select="'&#xA;'" />
    <xsl:variable name="CRLF" select="concat($CR, $LF)" />

    <xsl:choose>
      <!-- crlf -->
      <xsl:when test="contains($string,$CRLF)">
        <xsl:value-of select="substring-before($string,$CRLF)" />
        <br />
        <xsl:call-template name="escape_crlf">
          <xsl:with-param name="string"
            select="substring-after($string,$CRLF)" />
        </xsl:call-template>
      </xsl:when>
      <!-- carriage return -->
      <xsl:when test="contains($string,$CR)">
        <xsl:value-of select="substring-before($string,$CR)" />
        <br />
        <xsl:call-template name="escape_crlf">
          <xsl:with-param name="string"
            select="substring-after($string,$CR)" />
        </xsl:call-template>
      </xsl:when>
      <!-- line feed -->
      <xsl:when test="contains($string,$LF)">
        <xsl:value-of select="substring-before($string,$LF)" />
        <br />
        <xsl:call-template name="escape_crlf">
          <xsl:with-param name="string"
            select="substring-after($string,$LF)" />
        </xsl:call-template>
      </xsl:when>

      <xsl:otherwise>
        <xsl:value-of select="$string" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>


