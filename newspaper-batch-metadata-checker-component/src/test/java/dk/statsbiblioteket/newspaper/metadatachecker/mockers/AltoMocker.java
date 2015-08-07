package dk.statsbiblioteket.newspaper.metadatachecker.mockers;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class AltoMocker {


    public static AttributeParsingEvent getAltoPageAttributeParsingEvent(final String film, final String avisID,
                                                                    final String publishDate, final String pictureNumber,
                                                                    final Batch batch,
                                                                    final int width,
                                                                    final int height) {
           return new AttributeParsingEvent(
                   batch.getFullID() + "/" +
                   batch.getBatchID() + "-" + film + "/" +
                   publishDate + "-01/" +
                   avisID + "-" + publishDate + "-" + pictureNumber + ".alto.xml") {
               @Override
               public InputStream getData() throws IOException {
                   return new ByteArrayInputStream(
                           getAltoXml(film,avisID,publishDate,pictureNumber,batch,width,height,400));
               }

               @Override
               public String getChecksum() throws IOException {
                   return null;
               }
           };
       }


    public static byte[] getAltoXml(final String film, final String avisID, final String publishDate,
                                    final String pictureNumber, final Batch batch, final int width, final int height,
                                    final int resolution) {
        try {

            String alto = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                          "<alto xmlns=\"http://www.loc.gov/standards/alto/ns-v2#\" xmlxsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                          "    <Description>\n" +
                          "        <MeasurementUnit>inch1200</MeasurementUnit>\n" +
                          "        <sourceImageInformation>\n" +
                          "            <fileName>"+batch.getFullID()+"\\"+batch.getBatchID()+"-"+film+"\\"+publishDate+"-01\\"+avisID+"-"+publishDate+"-01-"+pictureNumber+".jp2</fileName>\n" +
                          "        </sourceImageInformation>\n" +
                          "        <OCRProcessing ID=\"OCR1\">\n" +
                          "            <ocrProcessingStep>\n" +
                          "                <processingDateTime>2013-07-16T10:50:02</processingDateTime>\n" +
                          "                <processingAgency>Ninestars</processingAgency>\n" +
                          "                <processingStepDescription>Scanning;Import;Recognition</processingStepDescription>\n" +
                          "                <processingStepSettings>\n" +
                          "                    version:ABBYY Recognition Server 3.0\n" +
                          "                    language:Danish\n" +
                          "                    dictionaryFlag.Danish\n" +
                          "                    dictionaryOn:1\n" +
                          "                    page-reoriented:UP\n" +
                          "                    text-orientation:TO_Normal\n" +
                          "                    ABBYY Recognition Server.option.analyze-zones:false\n" +
                          "                    ABBYY Recognition Server.option.ocr-auto-pictures:false\n" +
                          "                    ABBYY Recognition Server.option.detect-negative-images:false\n" +
                          "                    Predicted Word Accuracy:83.67\n" +
                          "                    ABBYY Recognition Server OCR Engine Predicted Accuracy:83.67\n" +
                          "                    ABBYY Recognition Server OCR Engine Character Error Ratio:0.163293\n" +
                          "                    ABBYY Recognition Server OCR Engine Character Count:4189\n" +
                          "                    ABBYY Recognition Server OCR Engine Word Count:731\n" +
                          "                    Node Count:731\n" +
                          "                    width:"+width+"\n" +
                          "                    height:"+height+"\n" +
                          "                    xdpi:"+resolution+"\n" +
                          "                    ydpi:"+resolution+"\n" +
                          "                </processingStepSettings>\n" +
                          "                <processingSoftware>\n" +
                          "                    <softwareCreator>ABBYY</softwareCreator>\n" +
                          "                    <softwareName>ABBYY Recognition Server</softwareName>\n" +
                          "                    <softwareVersion>3.0</softwareVersion>\n" +
                          "                </processingSoftware>\n" +
                          "            </ocrProcessingStep>\n" +
                          "            <postProcessingStep>\n" +
                          "                <processingDateTime>2013-07-16T10:50:02</processingDateTime>\n" +
                          "                <processingAgency>Ninestars</processingAgency>\n" +
                          "                <processingStepDescription>Verification;Export</processingStepDescription>\n" +
                          "                <processingStepSettings>Recognition Server Properties;Load Settings;Save Settings;OCR languages;Processing Station Properties</processingStepSettings>\n" +
                          "                <processingSoftware>\n" +
                          "                    <softwareCreator>Ninestars</softwareCreator>\n" +
                          "                    <softwareName>NSExport</softwareName>\n" +
                          "                    <softwareVersion>1.0</softwareVersion>\n" +
                          "                    <applicationDescription>NSExport is a powerful yet easy document capture system that allows converting paper to electronic documents.</applicationDescription>\n" +
                          "                </processingSoftware>\n" +
                          "            </postProcessingStep>\n" +
                          "        </OCRProcessing>\n" +
                          "    </Description>\n" +
                          "    <Styles>\n" +
                          "        <TextStyle ID=\"TS9.5\" FONTSIZE=\"9.5\"/>\n" +
                          "        <TextStyle ID=\"TS18\" FONTSIZE=\"18\"/>\n" +
                          "        <TextStyle ID=\"TS20.5\" FONTSIZE=\"20.5\"/>\n" +
                          "        <TextStyle ID=\"TS71.5\" FONTSIZE=\"71.5\"/>\n" +
                          "        <TextStyle ID=\"TS21\" FONTSIZE=\"21\"/>\n" +
                          "        <TextStyle ID=\"TS14.5\" FONTSIZE=\"14.5\"/>\n" +
                          "        <ParagraphStyle ID=\"PAR1\" ALIGN=\"Left\"/>\n" +
                          "        <ParagraphStyle ID=\"PAR2\" ALIGN=\"Left\"/>\n" +
                          "        <ParagraphStyle ID=\"PAR3\" ALIGN=\"Right\"/>\n" +
                          "        <ParagraphStyle ID=\"PAR4\" ALIGN=\"Center\"/>\n" +
                          "    </Styles>\n" +
                          "    <Layout>\n" +
                          "        <Page ID=\"PAGE1\" HEIGHT=\""+height*1200/resolution+"\" WIDTH=\""+width*1200/resolution+"\" PHYSICAL_IMG_NR=\"1\" QUALITY=\"OK\" POSITION=\"Single\" PROCESSING=\"OCR1\"  ACCURACY=\"83.67\" PC=\"0.836707\">\n" +
                          "            <PrintSpace ID=\"SPACE\" HEIGHT=\"8728\" WIDTH=\"7776\" HPOS=\"216\" VPOS=\"580\">\n" +
                          "                <TextBlock ID=\"ART2-1\" IDNEXT=\"ART2-2\" STYLEREFS=\"PAR1\" HEIGHT=\"304\" WIDTH=\"5016\" HPOS=\"1564\" VPOS=\"2704\" language=\"dan\">\n" +
                          "                    <TextLine ID=\"LINE9\" STYLEREFS=\"TS9.5\" HEIGHT=\"184\" WIDTH=\"4896\" HPOS=\"1612\" VPOS=\"2720\" CS=\"1\">\n" +
                          "                        <String ID=\"S20\" CONTENT=\"Med\" WC=\"1\" CC=\"9 9 9\" HEIGHT=\"128\" WIDTH=\"352\" HPOS=\"1612\" VPOS=\"2764\"/>\n" +
                          "                        <SP ID=\"SP12\" WIDTH=\"76\" HPOS=\"1968\" VPOS=\"2760\"/>\n" +
                          "                        <String ID=\"S21\" CONTENT=\"derte\" WC=\"1\" CC=\"9 9 9 9 9\" HEIGHT=\"132\" WIDTH=\"360\" HPOS=\"2048\" VPOS=\"2760\"/>\n" +
                          "                        <SP ID=\"SP13\" WIDTH=\"64\" HPOS=\"2412\" VPOS=\"2752\"/>\n" +
                          "                        <String ID=\"S22\" CONTENT=\"Blad\" WC=\"1\" CC=\"9 9 9 9\" HEIGHT=\"140\" WIDTH=\"380\" HPOS=\"2480\" VPOS=\"2752\"/>\n" +
                          "                        <SP ID=\"SP14\" WIDTH=\"92\" HPOS=\"2864\" VPOS=\"2744\"/>\n" +
                          "                        <String ID=\"S23\" CONTENT=\"følger\" WC=\"1\" CC=\"9 9 9 9 9 9\" HEIGHT=\"168\" WIDTH=\"424\" HPOS=\"2948\" VPOS=\"2732\"/>\n" +
                          "                        <SP ID=\"SP15\" WIDTH=\"92\" HPOS=\"3372\" VPOS=\"2732\"/>\n" +
                          "                        <String ID=\"S24\" CONTENT=\"£70\" WC=\"1\" CC=\"9 9 9\" HEIGHT=\"156\" WIDTH=\"308\" HPOS=\"3464\" VPOS=\"2736\"/>\n" +
                          "                        <SP ID=\"SP16\" WIDTH=\"56\" HPOS=\"3776\" VPOS=\"2764\"/>\n" +
                          "                        <String ID=\"S25\" CONTENT=\"144\" WC=\"1\" CC=\"9 9 9\" HEIGHT=\"156\" WIDTH=\"256\" HPOS=\"3836\" VPOS=\"2764\"/>\n" +
                          "                        <SP ID=\"SP17\" WIDTH=\"44\" HPOS=\"4096\" VPOS=\"2760\"/>\n" +
                          "                        <String ID=\"S26\" CONTENT=\"og\" WC=\"1\" CC=\"9 9\" HEIGHT=\"156\" WIDTH=\"200\" HPOS=\"4144\" VPOS=\"2760\"/>\n" +
                          "                        <SP ID=\"SP18\" WIDTH=\"64\" HPOS=\"4348\" VPOS=\"2760\"/>\n" +
                          "                        <String ID=\"S27\" CONTENT=\"45,\" WC=\"1\" CC=\"9 9 9\" HEIGHT=\"156\" WIDTH=\"244\" HPOS=\"4416\" VPOS=\"2768\"/>\n" +
                          "                        <SP ID=\"SP19\" WIDTH=\"80\" HPOS=\"4664\" VPOS=\"2760\"/>\n" +
                          "                        <String ID=\"S28\" CONTENT=\"og\" WC=\"1\" CC=\"9 9\" HEIGHT=\"156\" WIDTH=\"184\" HPOS=\"4748\" VPOS=\"2760\"/>\n" +
                          "                        <SP ID=\"SP20\" WIDTH=\"48\" HPOS=\"4936\" VPOS=\"2720\"/>\n" +
                          "                        <String ID=\"S29\" CONTENT=\"Aftenposten\" WC=\"1\" CC=\"9 9 9 9 9 9 9 9 9 9 9\" HEIGHT=\"172\" WIDTH=\"864\" HPOS=\"4988\" VPOS=\"2720\"/>\n" +
                          "                        <SP ID=\"SP21\" WIDTH=\"48\" HPOS=\"5856\" VPOS=\"2732\"/>\n" +
                          "                        <String ID=\"S30\" CONTENT=\"£70.\" WC=\"1\" CC=\"9 9 9 9\" HEIGHT=\"172\" WIDTH=\"312\" HPOS=\"5908\" VPOS=\"2732\"/>\n" +
                          "                        <SP ID=\"SP22\" WIDTH=\"48\" HPOS=\"6224\" VPOS=\"2772\"/>\n" +
                          "                        <String ID=\"S31\" CONTENT=\"47.\" WC=\"1\" CC=\"9 9 9\" HEIGHT=\"184\" WIDTH=\"232\" HPOS=\"6276\" VPOS=\"2772\"/>\n" +
                          "                    </TextLine>\n" +
                          "                    <ComposedBlock ID=\"string\" STYLEREFS=\"string\" HEIGHT=\"3\" WIDTH=\"3\" HPOS=\"3\" VPOS=\"3\" ROTATION=\"1.5E2\" IDNEXT=\"string\">\n" +
                          "                      <TextBlock ID=\"string\" STYLEREFS=\"string string\" HEIGHT=\"3\" WIDTH=\"3\" HPOS=\"3\" VPOS=\"3\" ROTATION=\"1.5E2\" IDNEXT=\"string\" language=\"dan\">\n" +
                          "                        <TextLine ID=\"string\" STYLEREFS=\"string\" HEIGHT=\"1.5E2\" WIDTH=\"1.5E2\" HPOS=\"1.5E2\" VPOS=\"1.5E2\" BASELINE=\"1.5E2\" CS=\"true\">\n" +
                          "                          <String ID=\"string\" STYLEREFS=\"string\" HEIGHT=\"1.5E2\" WIDTH=\"1.5E2\" HPOS=\"1.5E2\" VPOS=\"1.5E2\" CONTENT=\"string\" STYLE=\"smallcaps italics\" SUBS_TYPE=\"HypPart2\" SUBS_CONTENT=\"string\" WC=\"1.5E2\" CC=\"string\">\n" +
                          "                            <!--1 or more repetitio-->\n" +
                          "                            <ALTERNATIVE PURPOSE=\"string\">string</ALTERNATIVE>\n" +
                          "                          </String>\n" +
                          "                          <!--Optional:-->\n" +
                          "                          <SP ID=\"string\" WIDTH=\"1.5E2\" HPOS=\"1.5E2\" VPOS=\"1.5E2\"/>\n" +
                          "                          <!--Optional:-->\n" +
                          "                          <HYP WIDTH=\"1.5E2\" HPOS=\"1.5E2\" VPOS=\"1.5E2\" CONTENT=\"anySimpleType\"/>\n" +
                          "                        </TextLine>\n" +
                          "                      </TextBlock>                      \n" +
                          "                    </ComposedBlock>\n" +
                          "                </TextBlock>\n" +
                          "            </PrintSpace>\n" +
                          "        </Page>\n" +
                          "    </Layout>\n" +
                          "</alto>\n";
            return alto.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }
    }
}
