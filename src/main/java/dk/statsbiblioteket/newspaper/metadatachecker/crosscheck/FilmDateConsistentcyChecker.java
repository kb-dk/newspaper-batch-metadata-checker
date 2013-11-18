package dk.statsbiblioteket.newspaper.metadatachecker.crosscheck;

import java.io.IOException;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.DefaultTreeEventHandler;
import dk.statsbiblioteket.util.xml.DOM;
import dk.statsbiblioteket.util.xml.XPathSelector;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class FilmDateConsistentcyChecker extends DefaultTreeEventHandler {
    private final ResultCollector resultCollector;
    private final Document batchXmlStructure;
    private final XPathSelector xpath;

    public FilmDateConsistentcyChecker(ResultCollector resultCollector, Document batchXmlStructure) {
        this.resultCollector = resultCollector;
        this.batchXmlStructure = batchXmlStructure;
        xpath = DOM.createXPathSelector("avis",
                "http://www.statsbiblioteket.dk/avisdigitalisering/microfilm/1/0/");
    }

    @Override
    public void handleAttribute(AttributeParsingEvent event) {
        if (event.getName().endsWith(".film.xml")) {
            Document filmMetaData;
            try {
                filmMetaData = DOM.streamToDOM(event.getData());
                if (filmMetaData == null) {
                    addFailure(event.getName(), "Could not parse xml");
                    return;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String filmStartdate = xpath.selectString(filmMetaData, "/avis:reelMetadata/avis:startDate");
            String filmEnddate = xpath.selectString(filmMetaData, "/avis:reelMetadata/avis:endDate");
            String filmID = event.getName().split("/")[1].replace(".film.xml","");
            NodeList editions = xpath.selectNodeList(batchXmlStructure,
                    "/node/node[@shortName='" + filmID + "']/node[@shortName!='FILM-ISO-target'][@shortName!='UNMATCHED']");
            for (int index = 0 ; index < editions.getLength() ; index++) {
                verifyEditionDateContainment(filmStartdate, filmEnddate,
                        editions.item(index).getAttributes().getNamedItem("shortName").getNodeValue(), filmID);
            }
        }
    }

    protected void verifyEditionDateContainment(String filmStartdate, String filmEnddate, String editionID, String filmID) {
        FuzzyDate filmStart = new FuzzyDate(filmStartdate);
        FuzzyDate filmEnd = new FuzzyDate(filmEnddate);
        FuzzyDate editionDate = new FuzzyDate(editionID.substring(0, editionID.lastIndexOf('-')));
        if (filmStart.compareTo(editionDate) > 0) {
            addFailure(editionID, "2E-2: Edition earlier than film start date " + filmStartdate + " in film " + filmID);  // Include filmid
        }
        if (filmEnd.compareTo(editionDate) < 0) {
            addFailure(editionID, "2E-2: Edition later than film end date " + filmEnddate + " in film " + filmID);
        }
    }

    private void addFailure(String eventName, String description) {
        resultCollector.addFailure(
                eventName, "metadata", getClass().getSimpleName(), description);
    }
}
