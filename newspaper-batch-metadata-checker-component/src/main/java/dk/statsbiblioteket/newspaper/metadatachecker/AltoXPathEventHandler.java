package dk.statsbiblioteket.newspaper.metadatachecker;

import dk.statsbiblioteket.newspaper.metadatachecker.caches.DocumentCache;
import dk.statsbiblioteket.util.Strings;

import java.io.IOException;

import org.w3c.dom.Document;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.DefaultTreeEventHandler;
import dk.statsbiblioteket.util.xml.DOM;
import dk.statsbiblioteket.util.xml.XPathSelector;

/**
 *
 */
public class AltoXPathEventHandler extends DefaultTreeEventHandler {
    private ResultCollector resultCollector;
    private DocumentCache documentCache;

    /**
     * Constructor for this class.
     * @param resultCollector the result collector to collect errors in
     */
    public AltoXPathEventHandler(ResultCollector resultCollector, DocumentCache documentCache) {
        this.resultCollector = resultCollector;
        this.documentCache = documentCache;
    }

    @Override
    public void handleAttribute(AttributeParsingEvent event) {
        if (event.getName().endsWith("alto.xml")) {
            try {
                doValidate(event);
            } catch (Exception e) {    //Fault Barrier
                resultCollector.addFailure(
                        event.getName(),
                        "exception",
                        getClass().getSimpleName(),
                        "Error processing ALTO metadata: " + e.toString(),
                        Strings.getStackTrace(e)
                );
            }
        }
    }

    private void doValidate(AttributeParsingEvent event) {
        XPathSelector xpath = DOM.createXPathSelector("alto", "http://www.loc.gov/standards/alto/ns-v2#");
        Document doc;
        try {
            doc = documentCache.getDocument(event);
            if (doc == null) {
                resultCollector
                        .addFailure(event.getName(), "exception", getClass().getSimpleName(), "Could not parse xml");
                return;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final String xpath2J3 = "alto:alto/alto:Description/alto:sourceImageInformation/alto:fileName";
        String pathInAlto = xpath.selectString(doc, xpath2J3);
        String expectedName = event.getName().substring(event.getName().indexOf('/') + 1).replaceAll("/", "\\\\");
        expectedName = expectedName.replace(".alto.xml", ".jp2");
        if (pathInAlto == null || !expectedName.equals(pathInAlto)) {
            resultCollector.addFailure(event.getName(), "metadata", getClass().getSimpleName(),
                                       "2J-3: file path in ALTO file '" + pathInAlto
                                               + "' does not match actual file path '" + expectedName + "'.", xpath2J3);
        }
    }

}
