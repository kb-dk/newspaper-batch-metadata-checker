<?xml version='1.0' encoding='UTF-8'?>
<!-- author: The State and University Library, Denmark -->

<s:schema xmlns:s="http://purl.oclc.org/dsdl/schematron"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://purl.oclc.org/dsdl/schematron ">

  <s:ns uri="http://www.loc.gov/mix/v20" prefix="mix"/>

  <s:title>Schematron checks for mix files for document pages.</s:title>
  <s:pattern>

    <!--File format name-->
    <!--File format version-->
    <s:rule context="mix:mix/mix:BasicDigitalObjectInformation/mix:FormatDesignation">
      <s:assert test="mix:formatName = 'JPEG2000 – part 1'">2K-3: Format name should match JPEG2000 – part 1</s:assert>
      <s:assert test="mix:formatVersion = 'ISO-IEC 15444-1:2004'">2K-4: Format version should match ISO-IEC 15444-1:2004</s:assert>
    </s:rule>

    <!--Compression algorithm-->
    <s:rule context="mix:mix/mix:BasicDigitalObjectInformation/mix:Compression">
      <s:assert test="mix:compressionScheme = 'JP2 lossless'">2K-5: Compression algorithm should match JP2 lossless</s:assert>
    </s:rule>
    
    <!--Checksum algorithm-->
    <!--Organisation which calculated the checksum-->
    <s:rule context="mix:mix/mix:BasicDigitalObjectInformation/mix:Fixity">
      <s:assert test="mix:messageDigestAlgorithm = 'MD5'">2K-6: Checksum algorithm should match MD5</s:assert>
      <s:assert test="mix:messageDigestOriginator = 'Ninestars'">2K-7: Organisation should match Ninestars</s:assert>
    </s:rule>

    <!--colorSpace-->
    <s:rule context="mix:mix/mix:BasicImageInformation/mix:BasicImageCharacteristics">
      <s:assert test="mix:PhotometricInterpretation/mix:colorSpace = 'greyscale'">2K-10: ColorSpace should match greyscale</s:assert>
    </s:rule>

    <!--Scanner-organisation-->
    <s:rule context="mix:mix/mix:ImageCaptureMetadata/mix:GeneralCaptureInformation">
      <s:assert test="matches(mix:imageProducer,'^State and University Library; Ninestars Information Technologies LTD; .+')">2K-16: ImageProducer should match "State and University Library; Ninestars Information Technologies LTD; [operator]"</s:assert>
    </s:rule>
    
    <!--Scanner manufacturer-->
    <!--Scanner model name-->
    <!--Scanner model number-->
    <!--Scanner model serial number-->
    <!--Software used on the image-->
    <!--Software version-->
    <s:rule context="mix:mix/mix:ImageCaptureMetadata/mix:ScannerCapture">
      <s:assert test="mix:scannerManufacturer/string-length() > 0">2K-17: ScannerManufacturer should not be empty</s:assert>
      <s:assert test="mix:ScannerModel/mix:scannerModelName/string-length() > 0">2K-18: ScannerModelName should not be empty</s:assert>
      <s:assert test="mix:ScannerModel/mix:scannerModelNumber/string-length() > 0">2K-19: ScannerModelNumber should not be empty</s:assert>
      <s:assert test="mix:ScannerModel/mix:scannerModelSerialNo/string-length() > 0">2K-20: ScannerModelSerialNo should not be empty</s:assert>
      <s:assert test="mix:ScanningSystemSoftware/mix:scanningSoftwareName/string-length() > 0">2K-21: ScanningSoftwareName should not be empty</s:assert>
      <s:assert test="mix:ScanningSystemSoftware/mix:scanningSoftwareVersionNo/string-length() > 0">2K-22: ScanningSoftwareVersionNo should not be empty</s:assert>
    </s:rule>

    <!--bitsPerSampleValue-->
    <!--samplesPerPixel-->
    <s:rule context="mix:mix/mix:ImageAssessmentMetadata/mix:ImageColorEncoding">
      <s:assert test="mix:BitsPerSample[mix:bitsPerSampleUnit='integer']/mix:bitsPerSampleValue = '8'">2K-26: BitsPerSampleValue should match 8</s:assert>
      <s:assert test="mix:samplesPerPixel = '1'">2K-27: SamplesPerPixel should match 1</s:assert>
    </s:rule>

  </s:pattern>
</s:schema>