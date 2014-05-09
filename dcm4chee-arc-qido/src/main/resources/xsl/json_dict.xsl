<?xml version="1.0" encoding="UTF-8"?>
<!-- ***** BEGIN LICENSE BLOCK *****
   - Version: MPL 1.1/GPL 2.0/LGPL 2.1
   -
   - The contents of this file are subject to the Mozilla Public License Version
   - 1.1 (the "License"); you may not use this file except in compliance with
   - the License. You may obtain a copy of the License at
   - http://www.mozilla.org/MPL/
   -
   - Software distributed under the License is distributed on an "AS IS" basis,
   - WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
   - for the specific language governing rights and limitations under the
   - License.
   -
   - The Original Code is part of dcm4che, an implementation of DICOM(TM) in
   - Java(TM), hosted at https://github.com/gunterze/dcm4che.
   -
   - The Initial Developer of the Original Code is
   - Agfa Healthcare.
   - Portions created by the Initial Developer are Copyright (C) 2011
   - the Initial Developer. All Rights Reserved.
   -
   - Contributor(s):
   - Hesham Elbadawi <bsdreko@gmail.com>
   -
   - Alternatively, the contents of this file may be used under the terms of
   - either the GNU General Public License Version 2 or later (the "GPL"), or
   - the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
   - in which case the provisions of the GPL or the LGPL are applicable instead
   - of those above. If you wish to allow use of your version of this file only
   - under the terms of either the GPL or the LGPL, and not to allow others to
   - use your version of this file under the terms of the MPL, indicate your
   - decision by deleting the provisions above and replace them with the notice
   - and other provisions required by the GPL or the LGPL. If you do not delete
   - the provisions above, a recipient may use your version of this file under
   - the terms of any one of the MPL, the GPL or the LGPL.
   -
   - ***** END LICENSE BLOCK *****  -->


<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="text"></xsl:output>
  <xsl:template match="elements">
  <xsl-text>{ </xsl-text>
  <xsl:apply-templates select="element"/>
  </xsl:template>  
  <xsl:template match="element">
    <xsl:text>"</xsl:text>
    <xsl:value-of select="@tag" />
    <xsl:text>": { </xsl:text>
    <xsl:text> "VR": "</xsl:text>
    <xsl:value-of select="@vr" />
    <xsl:text>" , </xsl:text>
    <xsl:text> "VM": "</xsl:text>
    <xsl:value-of select="@vm" />
    <xsl:text>" , </xsl:text>
    <xsl:text> "KEYWORD": "</xsl:text>
    <xsl:value-of select="@keyword" />
    <xsl:text>" }</xsl:text>
    <xsl:choose>
    <xsl:when test="position()=last()">
    <xsl:text>}</xsl:text>
    </xsl:when>
    <xsl:otherwise>
    <xsl:text> , </xsl:text>
    </xsl:otherwise>
</xsl:choose>
<xsl:text>
</xsl:text>
  </xsl:template>
</xsl:stylesheet>
