package dk.statsbiblioteket.newspaper.metadatachecker;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem.FileAttributeParsingEvent;
import dk.statsbiblioteket.util.xml.DOM;
import dk.statsbiblioteket.util.xml.XPathSelector;
import org.w3c.dom.Document;

import java.io.IOException;

/**
 *
 */
public class ModsXPathEventHandler implements TreeEventHandler {

    private ResultCollector resultCollector;

    public ModsXPathEventHandler(ResultCollector resultCollector) {
        this.resultCollector = resultCollector;
    }

    @Override
    public void handleNodeBegin(NodeBeginsParsingEvent event) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void handleNodeEnd(NodeEndParsingEvent event) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void handleAttribute(AttributeParsingEvent event) {
        if (event.getName().endsWith("mods.xml")) {
           doValidate(event);
        } else {
            return;
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
        final String xpath1 = "mods:mods/mods:relatedItem[@type='original']/mods:identifier[@type='reel sequence number']";
        String sequenceNumber = xpath.selectString(doc, xpath1);
        if (!(event.getName().contains(sequenceNumber))) {
            resultCollector.addFailure(event.getName(),
                                    "metadata",
                                    getClass().getName(),
                                    sequenceNumber + " not found in file name",
                                    xpath1
                                    );
        }
    }

    @Override
    public void handleFinish() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
