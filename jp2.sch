<!-- author: The State and University Library, Denmark -->

<schema xmlns="http://purl.oclc.org/dsdl/schematron">
    <title>Jpylyzer output validation schema.</title>
    <pattern>
        <title>Valid JP2 test</title>
        <rule context="/jpylyzer">
            <assert test="isValidJP2 = 'True'">Invalid JP2</assert>
        </rule>
    </pattern>
    <pattern>
        <title>Transformation test</title>
        <rule context="/jpylyzer/properties/contiguousCodestreamBox/cod">
            <assert test="transformation = '5-3 reversible'">wrong transformation</assert>
        </rule>
    </pattern>
    <pattern>
        <title>Colour space test</title>
        <rule context="/jpylyzer/properties/jp2HeaderBox/colourSpecificationBox">
            <assert test="meth = 'Enumerated'">Not enumerated colour space</assert>
            <assert test="enumCS = 'greyscale'">Not greyscale colour space</assert>
        </rule>
    </pattern>
    <pattern>
        <title>Colour depth test</title>
        <rule context="/jpylyzer/properties/jp2HeaderBox/imageHeaderBox">
            <assert test="bPCDepth = '8'">Colour depth different from 8.</assert>
        </rule>
    </pattern>
    <pattern>
        <title>Coding style tests</title>
        <rule context="/jpylyzer/properties/contiguousCodestreamBox/cod">
            <assert test="codeBlockWidth = '64'">Code block width different from 64.</assert>
            <assert test="codeBlockHeight = '64'">Code block height different from 64.</assert>
            <assert test="layers = '16'">Number of quality layers different from 16.</assert>
            <assert test="levels = '6'">Number of decomposition levels different from 6.</assert>
            <assert test="precincts = 'no'">File contains precincts, which it shouldn't.</assert>
            <assert test="codingBypass = 'yes'">Coding bypass disabled.</assert>
            <assert test="sop = 'yes'">No start of packet marker segments.</assert>
            <assert test="eph = 'yes'">No end of packet marker segments.</assert>
            <assert test="segmentationSymbols = 'yes'">No segmentation symbols.</assert>
            <assert test="order = 'RPCL'">Wrong progression order</assert>
        </rule>
    </pattern>
    <pattern>
        <title>Tile size tests</title>
        <rule context="/jpylyzer/properties/contiguousCodestreamBox/siz">
            <assert test="xTsiz = '1024'">Horizontal tile size different from 1024.</assert>
            <assert test="yTsiz = '1024'">Vertical tile size different from 1024.</assert>
            <assert test="ssizDepth = '8'">Colour depth different from 8.</assert>
        </rule>
    </pattern>
    <pattern>
        <title>TilePart tests</title>
        <rule context="/jpylyzer/properties/contiguousCodestreamBox/tileParts/tilePart/sot">
            <assert test="psot">Length of tile missing.</assert>
        </rule>
    </pattern>
</schema>
