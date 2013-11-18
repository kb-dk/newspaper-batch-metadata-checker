package dk.statsbiblioteket.newspaper.metadatachecker;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class MixerMockup {
    static AttributeParsingEvent getMixPageAttributeParsingEvent(final String film, final String avisID,
                                                                 final String publishDate, final String pictureNumber,
                                                                 final Batch batch,
                                                                 final int width,
                                                                 final int height,
                                                                 final String checksum) {
        return new AttributeParsingEvent(
                batch.getFullID() + "/" +
                batch.getBatchID() + "-" + film + "/" +
                publishDate + "-01/" +
                avisID + "-" + publishDate + "-01-" + pictureNumber + ".mix.xml") {
            @Override
            public InputStream getData() throws IOException {
                return new ByteArrayInputStream(
                        getCustomPageMix(
                                batch.getBatchID() + "-" + film, pictureNumber, "2013-11-12T11:48:06",width,height,400, checksum));
            }

            @Override
            public String getChecksum() throws IOException {
                return null;
            }
        };
    }

    static AttributeParsingEvent getMixWorkshiftIso(final String workshift, final String pictureNumber,
                                                    final Batch batch,
                                                    final String checksum) {
        return new AttributeParsingEvent(
                batch.getFullID() + "/" +
                "WORKSHIFT-ISO-TARGET/" +
                "Target-" + workshift + "-" + pictureNumber + ".mix.xml") {
            @Override
            public InputStream getData() throws IOException {
                return new ByteArrayInputStream(
                        getCustomPageMix(workshift, pictureNumber, "2013-11-12T11:48:06", 9304, 11408,400,checksum ));
            }

            @Override
            public String getChecksum() throws IOException {
                return null;
            }
        };
    }

    private static byte[] getCustomPageMix(String filmId, String billedID, String scannedDate, int width, int height, int resolution, String checksum) {
        String mix = "<mix:mix xmlns:mix=\"http://www.loc.gov/mix/v20\">\n" +
                     "    <mix:BasicDigitalObjectInformation>\n" +
                     "        <mix:ObjectIdentifier>\n" +
                     "            <mix:objectIdentifierType>Image Unique ID</mix:objectIdentifierType>\n" +
                     "            <mix:objectIdentifierValue>" + filmId + "-" + billedID + "</mix:objectIdentifierValue>\n" +
                     "        </mix:ObjectIdentifier>\n" +
                     "        <mix:fileSize>3662332</mix:fileSize>\n" +
                     "        <mix:Fixity>\n" +
                     "            <mix:messageDigestAlgorithm>MD5</mix:messageDigestAlgorithm>\n" +
                     "            <mix:messageDigest>"+checksum+"</mix:messageDigest>\n" +
                     "            <mix:messageDigestOriginator>Ninestars</mix:messageDigestOriginator>\n" +
                     "        </mix:Fixity>\n" +
                     "    </mix:BasicDigitalObjectInformation>\n" +
                     "    <mix:BasicImageInformation>\n" +
                     "        <mix:BasicImageCharacteristics>\n" +
                     "            <mix:imageWidth>"+width+"</mix:imageWidth>\n" +
                     "            <mix:imageHeight>"+height+"</mix:imageHeight>\n" +
                     "            <mix:PhotometricInterpretation>\n" +
                     "                <mix:colorSpace>greyscale</mix:colorSpace>\n" +
                     "            </mix:PhotometricInterpretation>\n" +
                     "        </mix:BasicImageCharacteristics>\n" +
                     "    </mix:BasicImageInformation>\n" +
                     "    <mix:ImageCaptureMetadata>\n" +
                     "        <mix:SourceInformation>\n" +
                     "            <mix:sourceType>microfilm</mix:sourceType>\n" +
                     "            <mix:SourceID>\n" +
                     "                <mix:sourceIDType>Microfilm reel barcode #</mix:sourceIDType>\n" +
                     "                <mix:sourceIDValue>" + filmId + "</mix:sourceIDValue>\n" +
                     "            </mix:SourceID>\n" +
                     "            <mix:SourceID>\n" +
                     "                <mix:sourceIDType>Location on microfilm</mix:sourceIDType>\n" +
                     "                <mix:sourceIDValue>" + billedID + "</mix:sourceIDValue>\n" +
                     "            </mix:SourceID>\n" +
                     "        </mix:SourceInformation>\n" +
                     "        <mix:GeneralCaptureInformation>\n" +
                     "            <mix:dateTimeCreated>" + scannedDate + "</mix:dateTimeCreated>\n" +
                     "            <mix:imageProducer>Ninestars</mix:imageProducer>\n" +
                     "        </mix:GeneralCaptureInformation>\n" +
                     "    </mix:ImageCaptureMetadata>\n" +
                     "    <mix:ImageAssessmentMetadata>\n" +
                     "        <mix:SpatialMetrics>\n" +
                     "            <mix:samplingFrequencyUnit>in.</mix:samplingFrequencyUnit>\n" +
                     "            <mix:xSamplingFrequency>\n" +
                     "                <mix:numerator>"+resolution+"</mix:numerator>\n" +
                     "                <mix:denominator>1</mix:denominator>\n" +
                     "            </mix:xSamplingFrequency>\n" +
                     "            <mix:ySamplingFrequency>\n" +
                     "                <mix:numerator>"+resolution+"</mix:numerator>\n" +
                     "                <mix:denominator>1</mix:denominator>\n" +
                     "            </mix:ySamplingFrequency>\n" +
                     "        </mix:SpatialMetrics>\n" +
                     "        <mix:ImageColorEncoding>\n" +
                     "            <mix:BitsPerSample>\n" +
                     "                <mix:bitsPerSampleValue>8</mix:bitsPerSampleValue>\n" +
                     "                <mix:bitsPerSampleUnit>integer</mix:bitsPerSampleUnit>\n" +
                     "            </mix:BitsPerSample>\n" +
                     "            <mix:samplesPerPixel>1</mix:samplesPerPixel>\n" +
                     "        </mix:ImageColorEncoding>\n" +
                     "    </mix:ImageAssessmentMetadata>\n" +
                     "</mix:mix>\n";
        try {
            return mix.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new Error("UTF-8 not known");
        }
    }
}
