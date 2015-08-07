package dk.statsbiblioteket.newspaper.metadatachecker.mix;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.newspaper.metadatachecker.checker.FailureType;
import dk.statsbiblioteket.newspaper.metadatachecker.checker.XmlAttributeChecker;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Map;

public class MixChecksumChecker extends XmlAttributeChecker {

    private Map<String,String> jp2Checksums= new HashMap<>();

    public MixChecksumChecker(ResultCollector resultCollector, Document batchXmlStructure) {
        super(resultCollector, FailureType.METADATA);
        NodeList contentNodes = XPATH.selectNodeList(batchXmlStructure, "//attribute[@shortName='contents']");
        for (int i = 0; i < contentNodes.getLength(); i++) {
            Node node = contentNodes.item(i);
            String checksum = node.getAttributes().getNamedItem("checksum").getTextContent();
            String name = node.getAttributes().getNamedItem("name").getTextContent();
            jp2Checksums.put(name,checksum);
        }
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
        //this select is expensive, like 1000 times more expensive than the other mix checkers
        //String treeMd5sum = XPATH.selectString(batchXmlStructure, "//attribute[@name='" + jp2Path + "']/@checksum");
        String treeMd5sum = jp2Checksums.get(jp2Path);

        if (!mixMd5sum.equalsIgnoreCase(treeMd5sum)) {
            addFailure(
                    event, "2K-7: Checksum '" + mixMd5sum + "' does not agree with checksum '" + treeMd5sum + "'");
        }
    }
    @Override
    public boolean shouldCheckEvent(AttributeParsingEvent event) {
        return event.getName()
                    .endsWith(".mix.xml");
    }


}
