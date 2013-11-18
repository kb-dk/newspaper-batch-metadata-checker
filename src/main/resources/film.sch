<?xml version='1.0' encoding='UTF-8'?>
<s:schema xmlns:s="http://purl.oclc.org/dsdl/schematron"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://purl.oclc.org/dsdl/schematron ">

    <s:ns uri="http://www.statsbiblioteket.dk/avisdigitalisering/microfilm/1/0/" prefix="avis"/>

    <s:title>Schematron checks for film metadata files (SB inspired by ndnp).</s:title>

    <s:pattern>
        <s:rule context="/avis:reelMetadata[avis:resolutionOfDuplicateNegative &lt; 4.5]/avis:resolutionCommentDuplicateNegative">
            <!--2E-14 + 2E-15: Negative resolution / comments concerning negative resolution-->
            <s:assert test="not(matches(text(), '^\s*$'))">When negative resolution is below 4.5, resolutionCommentDuplicateNegative must contain an explanation.</s:assert>
        </s:rule>

        <s:rule context="/avis:reelMetadata">
            <!--2E-6: Reduction ratio-->
            <s:assert test="matches(avis:reductionRatio, '^\s*([1-9]|1[0-9])x\s*$')">Should be a integer number (19 or lower) followed by an x (no leading zeroes)</s:assert>

            <!--2E-7: Original newspaper resolution-->
            <s:assert test="matches(avis:captureResolutionOriginal, '^\s*[1-9][0-9]*\s*$')">Must be an integer number</s:assert>
            <s:report test="avis:captureResolutionOriginal &lt; 300">Original newspaper resolution must be 300 pixels per inch or higher</s:report>

            <!--2E-8: Original resolution unit-->
            <s:assert test="matches(avis:captureResolutionOriginal/@measurement, '^\s*pixels/inch\s*$')">Original resolution unit should be 'pixels/inch'</s:assert>

            <!--2E-9: Scanning resolution  Film-->

            <!--2E-10: Scanning resolution  Film  unit-->
            <s:assert test="matches(avis:captureResolutionFilm/@measurement, '^\s*pixels/inch\s*$')">Scanning resolution Film unit should be 'pixels/inch'</s:assert>

        </s:rule>

    </s:pattern>
</s:schema>
