package dk.statsbiblioteket.newspaper.metadatachecker.crosscheck;

import java.io.IOException;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.DefaultTreeEventHandler;
import dk.statsbiblioteket.util.xml.DOM;
import dk.statsbiblioteket.util.xml.XPathSelector;
import org.w3c.dom.Document;

public class EditionXmlNumberVsFileName extends DefaultTreeEventHandler {
    private final ResultCollector resultCollector;
    private final XPathSelector xpath;

    public EditionXmlNumberVsFileName(ResultCollector resultCollector) {
        this.resultCollector = resultCollector;
        xpath = DOM.createXPathSelector("mods", "http://www.loc.gov/mods/v3");
    }

    @Override
    public void handleAttribute(AttributeParsingEvent event) {
        if (event.getName().endsWith(".edition.xml")) {
            Document doc;
            try {
                doc = DOM.streamToDOM(event.getData());
                if (doc == null) {
                    addFailure(event.getName(), "Could not parse xml");
                    return;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            final String xpathForEditionNumberXPath =
                    "mods:mods/mods:relatedItem[@type='host']/mods:part/mods:detail[@type='edition']/mods:number";
            String xpathForEditionNumber = xpath.selectString(doc, xpathForEditionNumberXPath);
            String editionIDFromNode = event.getName().split("/")[2];
            String nodeEditionNumber = editionIDFromNode.split("-")[3];
            if (Integer.parseInt(xpathForEditionNumber) != Integer.parseInt(nodeEditionNumber) ) {
                addFailure(event.getName(), "Edition number (" + xpathForEditionNumber + ") in edition xml doesn't " +
                        "correspond to node edition number: " + editionIDFromNode);
            }
        }
    }

    private void addFailure(String eventName, String description) {
        resultCollector.addFailure(
                eventName, "metadata", getClass().getSimpleName(), description);
    }
}
