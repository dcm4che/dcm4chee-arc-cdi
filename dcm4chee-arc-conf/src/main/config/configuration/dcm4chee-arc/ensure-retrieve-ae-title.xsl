<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="xml"/>
  <xsl:template match="/NativeDicomModel">
    <NativeDicomModel>
      <xsl:if test="not(DicomAttribute[@tag='00080054']/Value)">
        <DicomAttribute tag="00080054" vr="SH">
        DCM4CHEE
        </DicomAttribute>
      </xsl:if>
    </NativeDicomModel>
  </xsl:template>
</xsl:stylesheet>
