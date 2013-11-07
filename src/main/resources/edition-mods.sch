<?xml version='1.0' encoding='UTF-8'?>
<s:schema xmlns:s="http://purl.oclc.org/dsdl/schematron"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://purl.oclc.org/dsdl/schematron ">

    <s:ns uri="http://www.loc.gov/mods/v3" prefix="mods"/>

    <s:title>Schematron checks for mods xml files for edition.</s:title>

    <!--2D-6
    "Issue Present Indicator" (mods:mods/mods:note) should be one of the valid values:
	Present (i.e. published and digitised)
    Not digitised, published;
    Not digitised, not published;
    Not digitised, publication unknown.-->
    <s:pattern id="issuePresentIndicator">
        <s:rule context="/mods:mods/mods:note">
            <s:assert test="matches(lower-case(text()), '^present|not digitised, published|not digitised, not published|not digitised, publication unknown$')">
                2D-6: Issue Present Indicator <s:value-of select="text()"/> must be one of:
                Present
                Not digitised, published
                Not digitised, not published
                Not digitised, publication unknown
            </s:assert>
        </s:rule>
    </s:pattern>

    <!--2D-7
    If "Issue Present Comment" (mods:mods/mods:note/@displayLabel) er present,
	"Issue Present Indicator" (mods:mods/mods:note) above should be not digitised.
	valid values:
    Not digitised, published;
    Not digitised, not published;
    Not digitised, publication unknown.-->
    <s:pattern id="issuePresentComment">
        <s:rule context="/mods:mods/mods:note[@displayLabel]">
            <s:assert test="@displayLabel!=''">
                2D-7: Issue Present Comment <s:value-of select="@displayLabel"/> cannot be empty.
            </s:assert>
            <s:assert test="matches(lower-case(text()), '^not digitised, published|not digitised, not published|not digitised, publication unknown$')">
                2D-7: If Issue Present Comment '<s:value-of select="@displayLabel"/>' present,
                Issue Present Indicator '<s:value-of select="text()"/>' must be one of:
                Not digitised, published
                Not digitised, not published
                Not digitised, publication unknown
            </s:assert>
        </s:rule>
    </s:pattern>

    <!--2D-10
    "Edition Label"	Specified as printed on the newspaper.
    If symbols are used, e.g. asterisk, the field value must be “An asterisk"
    mods:mods/mods:relatedItem[@type="host"]/mods:part/mods:detail@type="edition"]/mods:caption
    check only expected characters. -->

    <s:pattern id="editionLabel">
        <s:rule context="/mods:mods/mods:relatedItem[@type='host']/mods:part/mods:detail[@type='edition']/mods:caption">
            <s:assert test="matches(text(), '^[a-zA-Z0-9 æøåÆØÅ]+$')">2D-10: Edition Label <s:value-of select="text()"/>
                contains unexpected characters.</s:assert>
        </s:rule>
    </s:pattern>

</s:schema>

