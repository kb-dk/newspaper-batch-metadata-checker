package dk.statsbiblioteket.newspaper.metadatachecker;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.DefaultTreeEventHandler;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem.FileAttributeParsingEvent;
import dk.statsbiblioteket.util.xml.DOM;
import dk.statsbiblioteket.util.xml.XPathSelector;
import org.w3c.dom.Document;

import java.io.IOException;

/**
 * This class uses xpath to validate metadata requirements for mods files that do no otherwise fit into the schematron
 * paradigm.
 */
public class ModsXPathEventHandler extends DefaultTreeEventHandler {

    private ResultCollector resultCollector;

    public ModsXPathEventHandler(ResultCollector resultCollector) {
        this.resultCollector = resultCollector;
    }

    @Override
    public void handleAttribute(AttributeParsingEvent event) {
        if (event.getName().endsWith("mods.xml")) {
           doValidate(event);
        }
    }

    private void doValidate(AttributeParsingEvent event) {
        XPathSelector xpath = DOM.createXPathSelector("mods", "http://www.loc.gov/mods/v3");
        Document doc;
        try {
            doc = DOM.streamToDOM(event.getData());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //2C-4
        final String xpath2C4 = "mods:mods/mods:relatedItem[@type='original']/mods:identifier[@type='reel number']";
        String reelNumber = xpath.selectString(doc, xpath2C4);
        //TODO check that this is equal to avisID from BatchContext

        //2C-5
        final String xpath1 = "mods:mods/mods:relatedItem[@type='original']/mods:identifier[@type='reel sequence number']";
        String sequenceNumber = xpath.selectString(doc, xpath1);
        if (!(event.getName().contains(sequenceNumber))) {
            resultCollector.addFailure(event.getName(),
                                    "metadata",
                                    getClass().getName(),
                                    "2C-5: " + sequenceNumber + " not found in file name",
                                    xpath1
                                    );
        }
    }
}
