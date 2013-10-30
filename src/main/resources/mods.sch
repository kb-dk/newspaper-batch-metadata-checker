<?xml version='1.0' encoding='UTF-8'?>
<s:schema xmlns:s="http://purl.oclc.org/dsdl/schematron"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://purl.oclc.org/dsdl/schematron ">

    <s:ns uri="http://www.loc.gov/mods/v3" prefix="mods"/>

    <s:title>Schematron checks for mods files for document pages.</s:title>

    <!--2C-2 -->
    <s:pattern id="pagesequence">
        <s:rule context="mods:mods">
            <s:assert test="mods:part/mods:extent[@unit='pages']/mods:start">Page Sequence Number is required.</s:assert>
        </s:rule>

        <s:rule context="mods:mods/mods:part/mods:extent[@unit='pages']/mods:start">
            <s:assert test="matches(., '^[0-9]+$')">Page Sequence Number is not a number: <s:value-of select="."/></s:assert>
        </s:rule>
    </s:pattern>

    <!--2C-4 -->
    <s:pattern id="filmID">
        <s:rule context="mods:mods">
            <s:let name="filmID" value="mods:relatedItem[@type='original']/mods:identifier[@type='reel number']"/>
            <s:assert test="$filmID">Reel number (filmID) must be specified</s:assert>
            <s:assert test="matches($filmID, '^[0-9]{12}-[0-9]{2}$')">Reel number <s:value-of select="$filmID"/> does not match expected pattern</s:assert>
        </s:rule>
    </s:pattern>



</s:schema>

