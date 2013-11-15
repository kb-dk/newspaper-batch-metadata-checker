<?xml version='1.0' encoding='UTF-8'?>
<s:schema xmlns:s="http://purl.oclc.org/dsdl/schematron"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://purl.oclc.org/dsdl/schematron ">

    <s:ns uri="http://www.statsbiblioteket.dk/avisdigitalisering/microfilm/1/0/" prefix="avis"/>

    <s:title>Schematron checks for film metadata files (SB inspired by ndnp).</s:title>

    <s:pattern>
        <s:rule context="avis:reelMetadata">
            <!--Negative resolution / comments concerning negative resolution-->
            <s:assert test="avis:resolutionOfDuplicateNegative >= '4.5' and avis:resolutionCommentDuplicateNegative != ''">When negative resolution is below 4.5, resolutionCommentDuplicateNegative must contain an explanation.</s:assert>

            <!--Reduction ratio-->
            <s:assert test="matches(avis:reductionRatio, '^\s*([1-9]|1[0-9])x\s*$')">Should be a integer number (19 or lower) followed by an x (no leading zeroes)</s:assert>

            <!--Original newspaper resolution-->
            <s:assert test="matches(avis:captureResolutionOriginal, '^\s*[1-9][0-9]*\s*$')">Must be an integer number</s:assert>
            <s:report test="avis:captureResolutionOriginal &lt; 300">Original newspaper resolution must be 300 pixels per inch or higher</s:report>

            <!--Original resolution unit-->
            <!--<s:assert test=""></s:assert>-->

        </s:rule>
    </s:pattern>
</s:schema>
