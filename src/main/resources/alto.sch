<?xml version='1.0' encoding='UTF-8'?>
<!-- author: The State and University Library, Denmark -->

<s:schema xmlns:s="http://purl.oclc.org/dsdl/schematron"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://purl.oclc.org/dsdl/schematron ">

  <s:ns uri="http://www.loc.gov/standards/alto/ns-v2#" prefix="alto"/>

  <s:title>Schematron checks for alto files for document pages.</s:title>
  <s:pattern>
  	
    <!--Measurement unit-->
    <!--OCRProcessing-->
    <s:rule context="alto:alto/alto:Description">
      <s:assert test="alto:MeasurementUnit = 'inch1200'">Measurement should match value inch1200</s:assert>
      <s:assert test="boolean(alto:OCRProcessing)">OCRProcessing element should exist</s:assert>
    </s:rule>
    
	<!--processingStepSettings -->
	<!--softwareName -->
	<!--softwareVersion -->
    <s:rule context="alto:alto/alto:Description/alto:OCRProcessing">
      <s:assert test="alto:ocrProcessingStep/alto:processingStepSettings/string-length() > 0">Software configuration should exist and have a value</s:assert>
      <s:assert test="alto:ocrProcessingStep/alto:processingSoftware/alto:softwareName/string-length() > 0">Software name should exist and have a value</s:assert>
      <s:assert test="alto:ocrProcessingStep/alto:processingSoftware/alto:softwareVersion/string-length() > 0">Software version should exist and have a value</s:assert>
    </s:rule>
    
    <!--Page height attribute-->
    <!--Page width attribute-->
    <s:rule context="alto:alto/alto:Layout/alto:Page">
      <s:assert test="@HEIGHT/string-length() > 0">Page should have HEIGHT attribute <s:value-of select="@HEIGHT"/></s:assert>
      <s:assert test="@WIDTH/string-length() > 0">Page should have WIDTH attribute <s:value-of select="@WIDTH"/></s:assert>
    </s:rule>
    
    <!--String element must have HEIGHT attribute, assuming content syntax is verified by schema check-->
    <!--String element must have WIDTH attribute, assuming content syntax is verified by schema check-->
    <!--String element must have HPOS attribute, assuming content syntax is verified by schema check-->
    <!--String element must have VPOS attribute, assuming content syntax is verified by schema check-->
    <!--String element must have CONTENT attribute-->
    <s:rule context="alto:alto/alto:Layout/alto:Page/alto:PrintSpace/alto:TextBlock/alto:TextLine/alto:String">
      <s:assert test="@HEIGHT/string-length() > 0">String should have HEIGHT attribute <s:value-of select="@HEIGHT"/></s:assert>
      <s:assert test="@WIDTH/string-length() > 0">String should have WIDTH attribute <s:value-of select="@WIDTH"/></s:assert>
      <s:assert test="@HPOS/string-length() > 0">String should have HPOS attribute <s:value-of select="@HPOS"/></s:assert>
      <s:assert test="@VPOS/string-length() > 0">String should have VPOS attribute <s:value-of select="@VPOS"/></s:assert>
      <s:assert test="matches(@CONTENT, '^\s*\S+\s*$')">String should have CONTENT attribute <s:value-of select="@CONTENT"/></s:assert>
    </s:rule>
    
<!--     /alto:altox/alto:Layout/alto:Page/alto:PrintSpace/alto:TextBlock[@language] -->

      <!--App 2J-16: nested textblocks must have the same language -->
     <s:rule context="alto:TextBlock/descendant::alto:TextBlock">
         <s:let name="desLang" value="@language"/>
         <s:let name="ancLang" value="ancestor::alto:TextBlock/@language"/>
          <s:assert test="$desLang = $ancLang">2J-16: Nested TextBlocks should have the same language.
              Changed from '<s:value-of select="$ancLang"/>' to '<s:value-of select="$desLang"/>' at TextBlock with ID=<s:value-of select="@ID"/> </s:assert>
     </s:rule>

  </s:pattern>
</s:schema>