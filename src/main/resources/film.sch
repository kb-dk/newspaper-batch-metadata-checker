<?xml version='1.0' encoding='UTF-8'?>
<s:schema xmlns:s="http://purl.oclc.org/dsdl/schematron"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://purl.oclc.org/dsdl/schematron ">

    <s:ns uri="http://www.statsbiblioteket.dk/avisdigitalisering/microfilm/1/0/" prefix="avis"/>

    <s:title>Schematron checks for film metadata files (SB inspired by ndnp).</s:title>

    <s:pattern>
        <!--Negative resolution-->
        <s:rule context="avis:reelMetadata">
          <s:assert test="avis:resolutionOfDuplicateNegative >= '4.5'">Negative resolution should be 4.5 or higher.</s:assert>
        </s:rule>
    </s:pattern>
</s:schema>
