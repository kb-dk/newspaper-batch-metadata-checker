<?xml version='1.0' encoding='UTF-8'?>
<!-- author: The State and University Library, Denmark -->


<!--TODO Attach to specs specific requirements-->
<s:schema xmlns:s="http://purl.oclc.org/dsdl/schematron"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://purl.oclc.org/dsdl/schematron ">
    <s:pattern>
        <s:title>SB Avis-scan jp2-profil-check</s:title>

        <!-- Valid JP2 test -->
        <s:rule context="/jpylyzer">
            <s:assert test="isValidJP2 = 'True'">2B: File should be a valid JP2 file</s:assert>
        </s:rule>

        <!-- Colour space test -->
        <s:rule context="/jpylyzer/properties/jp2HeaderBox/colourSpecificationBox">
            <s:assert test="meth = 'Enumerated'">2B-3: Not enumerated colour space</s:assert>
            <s:assert test="enumCS = 'greyscale'">2B-3: Not greyscale colour space</s:assert>
        </s:rule>

        <!-- Colour depth test -->
        <s:rule context="/jpylyzer/properties/jp2HeaderBox/imageHeaderBox">
            <s:assert test="bPCDepth = '8'">2B-3: Colour depth different from 8.</s:assert>
        </s:rule>
    
        <!-- No metadata should be present in the JP2 file-->
        <!--<s:rule context="com">
            <s:assert test="false()">2B-11: No comments (metadata) allowed</s:assert>
        </s:rule>-->

        <!-- Transformation/compression test + Coding style tests -->
        <s:rule context="/jpylyzer/properties/contiguousCodestreamBox/cod">
            <s:assert test="transformation = '5-3 reversible'">2B-4: Not lossless</s:assert>
            <s:assert test="codeBlockWidth = '64'">2B-9: Code block width different from 64.</s:assert>
            <s:assert test="codeBlockHeight = '64'">2B-9: Code block height different from 64.</s:assert>
            <s:assert test="layers = '16'">2B-6: Number of quality layers different from 16.</s:assert>
            <s:assert test="levels = '6'">2B-5: Number of decomposition levels different from 6.</s:assert>
            <s:assert test="precincts = 'no'">2B-10: File contains precincts, which it shouldn't.</s:assert>
            <s:assert test="codingBypass = 'yes'">2B-12: Coding bypass should be enabled.</s:assert>
            <s:assert test="sop = 'yes'">2B-14: Should have start of packet (SOP) marker segments.</s:assert>
            <s:assert test="eph = 'yes'">2B-15: Should have end of packet header (EPH) marker segments.</s:assert>
            <s:assert test="segmentationSymbols = 'yes'">2B-16: Should have segmentation symbols.</s:assert>
            <s:assert test="order = 'RPCL'">2B-7: Should have progression order RPCL</s:assert>
        </s:rule>

        <!-- Tile size tests + encoding-->
        <s:rule context="/jpylyzer/properties/contiguousCodestreamBox/siz">
            <s:assert test="rsiz = 'ISO/IEC 15444-1'">2B-1: Decoder capabilities should be ISO/IEC 15444-1</s:assert>
            <s:assert test="xTsiz = '1024'">2B-8: Horizontal tile size different from 1024.</s:assert>
            <s:assert test="yTsiz = '1024'">2B-8: Vertical tile size different from 1024.</s:assert>
            <s:assert test="ssizDepth = '8'">2B-3: Colour depth different from 8.</s:assert>
        </s:rule>

        <!-- TilePart tests -->
        <s:rule context="/jpylyzer/properties/contiguousCodestreamBox/tileParts/tilePart/sot">
            <s:assert test="psot">2B-8: Length of tile part (psot) missing.</s:assert>
        </s:rule>
    </s:pattern>
</s:schema>

