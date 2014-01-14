<?xml version='1.0' encoding='UTF-8'?>
<s:schema xmlns:s="http://purl.oclc.org/dsdl/schematron"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://purl.oclc.org/dsdl/schematron ">

    <s:ns uri="http://www.statsbiblioteket.dk/avisdigitalisering/microfilm/1/0/" prefix="avis"/>

    <s:title>Schematron checks for film metadata files (SB inspired by ndnp).</s:title>

    <s:pattern>
        <s:rule context="/avis:reelMetadata">
            <!--Fuzzy date format, i.e. YYYY-MM-DD, YYYY-MM, or YYYY-->
            <s:let name="dateFormat" value="'^[12][0-9]{3}(-(0[1-9]|1[0-2])(-(0[1-9]|[12][0-9]|3[01]))?)?$'"/>

            <!--2E-2 + 2E-3: Start date and End date, proper format-->
            <s:assert test="matches(avis:startDate, $dateFormat)">2E-2: Start date should be of ISO 8601 format YYYY-MM-DD, YYYY-MM, or YYYY</s:assert>
            <s:assert test="matches(avis:endDate, $dateFormat)">2E-3: Start date should be of ISO 8601 format YYYY-MM-DD, YYYY-MM, or YYYY</s:assert>

            <!--2E-2 + 2E-3: Start date and End date, proper order-->
            <s:let name="startDateNoDashes" value="translate(avis:startDate,'-','')"/>
            <s:let name="endDateNoDashes" value="translate(avis:endDate,'-','')"/>
            <s:let name="startDateAppended" value="concat($startDateNoDashes,'0000')"/>
            <s:let name="endDateAppended" value="concat($endDateNoDashes,'9999')"/>
            <s:let name="startDateForComparison" value="substring($startDateAppended,1,8)"/>
            <s:let name="endDateForComparison" value="substring($endDateAppended,1,8)"/>
            <s:assert test="$startDateForComparison &lt;= $endDateForComparison">2E-2 / 2E-3: Start date must be before end date</s:assert>

            <!--2E-6: Reduction ratio-->
            <s:assert test="matches(avis:reductionRatio, '^\s*([1-9]|1[0-9])x\s*$')">2E-6: Should be a integer (19 or lower) followed by an x (no leading zeroes)</s:assert>

            <!--2E-7: Original newspaper resolution-->
            <s:assert test="matches(avis:captureResolutionOriginal, '^\s*[1-9][0-9]*\s*$')">2E-7: Original newspaper resolution must be an integer</s:assert>
            <s:report test="avis:captureResolutionOriginal &lt; 300">2E-7: Original newspaper resolution must be 300 pixels per inch or higher</s:report>

            <!--2E-8: Original resolution unit-->
            <s:assert test="matches(avis:captureResolutionOriginal/@measurement, '^\s*pixels/inch\s*$')">2E-8: Original resolution unit should be 'pixels/inch'</s:assert>

            <!--2E-9: Scanning resolution  Film-->
            <s:let name="reductionRatioAsInteger" value="substring-before(avis:reductionRatio,'x')"/>
            <s:let name="ratioResolutionProduct" value="number($reductionRatioAsInteger) * number(avis:captureResolutionOriginal)"/>
            <s:assert test="avis:captureResolutionFilm = $ratioResolutionProduct">2E-9: captureResolutionFilm should equal reductionRatio * captureResolutionOriginal</s:assert>

            <!--2E-10: Scanning resolution  Film  unit-->
            <s:assert test="matches(avis:captureResolutionFilm/@measurement, '^\s*pixels/inch\s*$')">2E-10: Scanning resolution Film unit should be 'pixels/inch'</s:assert>

            <!--2E-11: Date microfilm created-->
            <s:assert test="avis:dateMicrofilmCreated='' or matches(avis:dateMicrofilmCreated,$dateFormat)">2E-11: Date microfilm created must be of format YYYY-MM-DD, YYYY-MM, YYYY, or empty if no date is available.</s:assert>
            <s:let name="dateMicrofilmCreatedNoDashes" value="translate(avis:dateMicrofilmCreated,'-','')"/>
            <s:let name="endDate0000Appended" value="concat($endDateNoDashes,'0000')"/>
            <s:let name="dateMicrofilmCreatedAppended" value="concat($dateMicrofilmCreatedNoDashes,'9999')"/>
            <s:let name="endDate0000ForComparison" value="substring($endDate0000Appended,1,8)"/>
            <s:let name="dateMicrofilmCreatedForComparison" value="substring($dateMicrofilmCreatedAppended,1,8)"/>
            <s:assert test="$endDate0000ForComparison &lt;= $dateMicrofilmCreatedForComparison">2E-11: End date must be before date microfilm created</s:assert>
        </s:rule>

        <s:rule context="/avis:reelMetadata[avis:resolutionOfNegative &lt; 4.5]/avis:resolutionCommentNegative">
            <!--2E-14 + 2E-15: Negative resolution / comments concerning negative resolution-->
            <s:assert test="not(matches(text(), '^\s*$'))">2E-14 / 2E-15: When negative resolution is below 4.5, resolutionCommentNegative must contain an explanation.</s:assert>
        </s:rule>
    </s:pattern>
</s:schema>
