<?xml version='1.0' encoding='UTF-8'?>
<!-- author: The State and University Library, Denmark -->

<s:schema xmlns:s="http://purl.oclc.org/dsdl/schematron"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://purl.oclc.org/dsdl/schematron ">
    <s:pattern>
        <s:title>SB Avis-scan jp2-profil-check</s:title>

        <!-- Valid JP2 test -->
        <s:rule context="/jpylyzer">
            <s:assert test="isValidJP2 = 'False'">Invalid JP2</s:assert>
        </s:rule>

        <!-- Colour space test -->
        <s:rule context="/jpylyzer/properties/jp2HeaderBox/colourSpecificationBox">
            <s:assert test="meth = 'Enumerated'">Not enumerated colour space</s:assert>
            <s:assert test="enumCS = 'greyscale'">Not greyscale colour space</s:assert>
        </s:rule>

        <!-- Colour depth test -->
        <s:rule context="/jpylyzer/properties/jp2HeaderBox/imageHeaderBox">
            <s:assert test="bPCDepth = '8'">Colour depth different from 8.</s:assert>
        </s:rule>

        <!-- Transformation/compression test + Coding style tests -->
        <s:rule context="/jpylyzer/properties/contiguousCodestreamBox/cod">
            <s:assert test="transformation = '5-3 reversible'">Not lossless</s:assert>
            <s:assert test="codeBlockWidth = '64'">Code block width different from 64.</s:assert>
            <s:assert test="codeBlockHeight = '64'">Code block height different from 64.</s:assert>
            <s:assert test="layers = '16'">Number of quality layers different from 16.</s:assert>
            <s:assert test="levels = '6'">Number of decomposition levels different from 6.</s:assert>
            <s:assert test="precincts = 'no'">File contains precincts, which it shouldn't.</s:assert>
            <s:assert test="codingBypass = 'yes'">Coding bypass disabled.</s:assert>
            <s:assert test="sop = 'yes'">No start of packet (SOP) marker segments.</s:assert>
            <s:assert test="eph = 'yes'">No end of packet header (EPH) marker segments.</s:assert>
            <s:assert test="segmentationSymbols = 'yes'">No segmentation symbols.</s:assert>
            <s:assert test="order = 'RPCL'">Wrong progression order</s:assert>
        </s:rule>

        <!-- Tile size tests -->
        <s:rule context="/jpylyzer/properties/contiguousCodestreamBox/siz">
            <s:assert test="xTsiz = '1024'">Horizontal tile size different from 1024.</s:assert>
            <s:assert test="yTsiz = '1024'">Vertical tile size different from 1024.</s:assert>
            <s:assert test="ssizDepth = '8'">Colour depth different from 8.</s:assert>
        </s:rule>

        <!-- TilePart tests -->
        <s:rule context="/jpylyzer/properties/contiguousCodestreamBox/tileParts/tilePart/sot">
            <s:assert test="psot">Length of tile part (psot) missing.</s:assert>
        </s:rule>
    </s:pattern>
</s:schema>

