<?xml version='1.0' encoding='UTF-8'?>
<s:schema xmlns:s="http://purl.oclc.org/dsdl/schematron"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://purl.oclc.org/dsdl/schematron ">

    <s:ns uri="http://www.loc.gov/mods/v3" prefix="mods"/>

    <s:title>Schematron checks for mods files for document pages.</s:title>

    <!--2C-1 Nothing to check, since section labels are not controlled, and may be empty. -->

    <!--2C-2 Full validation in ModsXPathEventHandler.java -->
    <s:pattern id="pagesequence">
        <s:rule context="mods:mods">
            <s:assert test="mods:part/mods:extent[@unit='pages']/mods:start">2C-2: Page Sequence Number is required.</s:assert>
        </s:rule>

        <s:rule context="mods:mods/mods:part/mods:extent[@unit='pages']/mods:start">
            <s:assert test="matches(., '^[0-9]+$')">2C-2: Page Sequence Number is not a number: <s:value-of select="."/></s:assert>
        </s:rule>
    </s:pattern>

    <!--2C-3-->
    <s:pattern id="pagenumber">
        <s:rule context="mods:mods/mods:part/mods:detail[@type='pageNumber']">
            <s:assert test="mods:number/string-length() > 0">2C-3: If present, Page Number should not be empty.
            </s:assert>
        </s:rule>
    </s:pattern>

    <!--2C-4 Full validation in ModsXPathEventHandler.java -->
    <s:pattern id="filmID">
        <s:rule context="mods:mods">
            <s:let name="filmID" value="mods:relatedItem[@type='original']/mods:identifier[@type='reel number']"/>
            <s:assert test="$filmID">Reel number (filmID) must be specified</s:assert>
            <s:assert test="matches($filmID, '^[0-9]{12}-[0-9]+$')">2C-4: Reel number
                <s:value-of select="$filmID"/>
                does not match expected pattern
            </s:assert>
        </s:rule>
    </s:pattern>

    <!--2C-5 validation in ModsXPathEventHandler.java -->

    <!--2C-6-->
    <s:pattern id="pagephysicaldescription">
        <s:rule context="mods:mods">
            <s:assert test="mods:relatedItem[@type='original']/mods:physicalDescription/mods:form[@type='microfilm']">
                2C-6: mods:form element of type 'microfilm' must be present
            </s:assert>
        </s:rule>
    </s:pattern>

    <!--2C-7  Omitted option
    <s:pattern id="pagephysicalcondition">
           <s:rule context="mods:mods">
               <s:let name="condition" value="mods:relatedItem[@type='original']/mods:physicalDescription/mods:note[@type='pagecondition']"/>
               <s:assert test="$condition">
                   2C-7: Page Physical Condition element must be present
               </s:assert>
               <s:assert test="matches(lower-case($condition),'^not tested|not acceptable|acceptable$')">
                   2C-7: Page Physical Condition must be one of 'Not Tested', 'Not Acceptable' or 'Acceptable' not
                   <s:value-of select="$condition"/>.
               </s:assert>
           </s:rule>
    </s:pattern>
    -->

    <!--2C-8 Omitted option
    <s:pattern id="pagephotographicquality">
        <s:rule context="mods:mods">
            <s:let name="condition" value="mods:relatedItem[@type='original']/mods:physicalDescription/mods:note[@type='photocondition']"/>
            <s:assert test="$condition">
                2C-8: Page Photographic Quality element must be present
            </s:assert>
            <s:assert test="matches(lower-case($condition),'^not tested|not acceptable|acceptable$')">
                2C-8: Page Photographic Quality must be one of 'Not Tested', 'Not Acceptable' or 'Acceptable' not
                <s:value-of select="$condition"/>.
            </s:assert>
        </s:rule>
    </s:pattern>
    -->

    <!--2C-9-->
    <s:pattern id="pagepresentindicator">
        <s:rule context="mods:mods">
            <s:let name="note" value="mods:relatedItem/mods:note[@type='noteAboutReproduction']"/>
            <s:assert test="count($note) = 1">2C-9: Page Present Indicator must be present exactly once.</s:assert>
        </s:rule>
        <s:rule context="mods:mods/mods:relatedItem/mods:note[@type='noteAboutReproduction']">
            <s:assert test="matches(lower-case(text()), '^present|not digitised, published|not digitised, not published|not digitised, publication unknown$')">
                2C-9: Page Present Indicator
                <s:value-of select="text()"/>
                must be one of:
                Present
                Not digitised, published
                Not digitised, not published
                Not digitised, publication unknown
            </s:assert>
        </s:rule>

    </s:pattern>

    <!--2C-10-->
    <!--There is nothing to test here except that, if present, this element must not be empty. But there is a
     cross-check between multiple pages implied by the text of the specification. -->
    <s:pattern id="pagepresentcomment">
        <s:rule context="mods:mods/mods:relatedItem/mods:note[@type='noteAboutReproduction']/@displayLabel">
            <s:assert test="./string-length() > 0">2C-10: displayLabel attribute (if present) must not be empty.
            </s:assert>
        </s:rule>
    </s:pattern>

    <!--2C-11 validation in ModsXPathEventHandler.java -->

</s:schema>

