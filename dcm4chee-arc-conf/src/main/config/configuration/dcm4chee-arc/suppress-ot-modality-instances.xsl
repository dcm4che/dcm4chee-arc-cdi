<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="text"/>
<xsl:template match="/NativeDicomModel">
<xsl:choose>
<xsl:when test="DicomAttribute[@tag='00080016']/Value='1.2.840.10008.5.1.4.1.1.7.4'">true</xsl:when>
</xsl:choose>
</xsl:template>
</xsl:stylesheet>
