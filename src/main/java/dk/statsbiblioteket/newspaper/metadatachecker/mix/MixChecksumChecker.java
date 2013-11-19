package dk.statsbiblioteket.newspaper.metadatachecker.mix;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.newspaper.metadatachecker.checker.FailureType;
import dk.statsbiblioteket.newspaper.metadatachecker.checker.XmlAttributeChecker;
import org.w3c.dom.Document;

public class MixChecksumChecker extends XmlAttributeChecker {
    private Document batchXmlStructure;

    public MixChecksumChecker(ResultCollector resultCollector, Document batchXmlStructure) {
        super(resultCollector, FailureType.METADATA);
        this.batchXmlStructure = batchXmlStructure;
    }

    @Override
    public void validate(AttributeParsingEvent event, Document doc) {
        //        mix:mix/mix:BasicDigitalObjectInformation/mix:Fixity/mix:messageDigest
        //       J – skal sammenlignes med filstrukturen

        final String xpath2KX = "mix:mix/mix:BasicDigitalObjectInformation/mix:Fixity/mix:messageDigest";

        String mixMd5sum = XPATH.selectString(doc, xpath2KX);


        String mixPath = event.getName();
        String jp2Path = mixPath.replaceAll(".mix.xml$", "")
                                .concat(".jp2/contents");
        String treeMd5sum = XPATH.selectString(batchXmlStructure, "//attribute[@name='" + jp2Path + "']/@checksum");

        if (!mixMd5sum.equalsIgnoreCase(treeMd5sum)) {
            addFailure(
                    event, "Checksum '" + mixMd5sum + "' does not agree with checksum '" + treeMd5sum + "'");
        }
    }
    @Override
    public boolean shouldCheckEvent(AttributeParsingEvent event) {
        return event.getName()
                    .endsWith(".mix.xml");
    }


}
