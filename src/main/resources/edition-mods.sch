<?xml version='1.0' encoding='UTF-8'?>
<s:schema xmlns:s="http://purl.oclc.org/dsdl/schematron"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://purl.oclc.org/dsdl/schematron ">

    <s:ns uri="http://www.loc.gov/mods/v3" prefix="mods"/>

    <s:title>Schematron checks for mods xml files for edition.</s:title>

    <!--2D-1-->
    <!--Check avisID (The unique ID for the newspaper concerned provided by the State and University Library)
              mods:mods/mods:titleInfo/mods:title [@type=”uniform” authority=”Statens Avissamling”]
              feltet svarer til id’et i filstrukturen -->
    <s:pattern id="avisID">
        <s:rule context="mods:mods/mods:titleInfo/mods:title [@type=”uniform” authority=”Statens Avissamling”]">
        </s:rule>
    </s:pattern>




</s:schema>

