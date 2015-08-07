package dk.statsbiblioteket.newspaper.metadatachecker.film;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.newspaper.metadatachecker.checker.FailureType;
import dk.statsbiblioteket.util.xml.DOM;
import dk.statsbiblioteket.util.xml.XPathSelector;

import java.io.IOException;
import java.io.InputStream;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class FilmNegativeResolutionNoTargetCheckerTest {
    private ResultCollector resultCollector;
    private Document batchXmlStructure;
    private FilmNegativeResolutionNoTargetChecker checker;

    @BeforeMethod
    public void setup() {
        resultCollector = mock(ResultCollector.class);
        batchXmlStructure = createBatchXmlStructure();
        XPathSelector xpathSelector = DOM.createXPathSelector("avis",
                "http://www.statsbiblioteket.dk/avisdigitalisering/microfilm/1/0/");
        checker = new FilmNegativeResolutionNoTargetChecker(resultCollector, xpathSelector, batchXmlStructure);
    }

    @Test
    public void goodCaseTest() {
        String filmID = "film-1";
        addFilmNode(batchXmlStructure, filmID);
        addISONode(batchXmlStructure, filmID);
        Document filmDoc = createFilmXmlDoc(35.5);
        AttributeParsingEvent filmEvent = createFilmEvent(filmID);
        checker.validate(filmEvent, filmDoc);
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void goodCaseNoISOTest() {
        String filmID = "film-1";
        addFilmNode(batchXmlStructure, filmID);
        Document filmDoc = createFilmXmlDoc(0.0);
        AttributeParsingEvent filmEvent = createFilmEvent(filmID);
        checker.validate(filmEvent, filmDoc);
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void noReadingWithISO() {
        String filmID = "film-1";
        addFilmNode(batchXmlStructure, filmID);
        addISONode(batchXmlStructure, filmID);
        Document filmDoc = createFilmXmlDoc(0.0);
        AttributeParsingEvent filmEvent = createFilmEvent(filmID);
        checker.validate(filmEvent, filmDoc);
        verifyFailure(filmEvent.getName());
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void noISOWithReading() {
        String filmID = "film-1";
        addFilmNode(batchXmlStructure, filmID);
        Document filmDoc = createFilmXmlDoc(35.5);
        AttributeParsingEvent filmEvent = createFilmEvent(filmID);
        checker.validate(filmEvent, filmDoc);
        verifyFailure(filmEvent.getName());
        verifyNoMoreInteractions(resultCollector);
    }

    private Document createBatchXmlStructure() {
        String batchXmlStructure = "<node></node>";
        return DOM.stringToDOM(batchXmlStructure);
    }


    private AttributeParsingEvent createFilmEvent(final String filmID) {
        return new AttributeParsingEvent("reelID/" + filmID + ".film.xml") {
            @Override
            public InputStream getData() throws IOException {
                return null;
            }
            @Override
            public String getChecksum() throws IOException {
                return null;
            }
        };
    }

    private Document createFilmXmlDoc(double resolutionOfNegative) {
        final String filmXmlStructure =
                "<avis:reelMetadata xmlns:avis=\"http://www.statsbiblioteket.dk/avisdigitalisering/microfilm/1/0/\">\n"
                        + "    <avis:resolutionOfNegative>" + resolutionOfNegative + "</avis:resolutionOfNegative>\n"
                        + "</avis:reelMetadata>";
        return DOM.stringToDOM(filmXmlStructure);
    }

    private void verifyFailure(String eventName) {
        verify(resultCollector).addFailure(eq(eventName), eq(FailureType.METADATA.value()),
                eq(FilmNegativeResolutionNoTargetChecker.class.getSimpleName()), anyString());
    }

    /**
     * Adds a film node to a batchXmlStructure with the given short name.
     */
    private void addFilmNode(Document doc, String filmID) {
        Element filmNode = batchXmlStructure.createElement("node");
        filmNode.setAttribute("shortName", filmID);
        doc.getFirstChild().appendChild(filmNode);
    }

    /**
     * Adds a FILM-ISO-target node
     */
    private void addISONode(Document doc, String filmID) {
        Element isoFolderNode = batchXmlStructure.createElement("node");
        isoFolderNode.setAttribute("shortName", "FILM-ISO-target");
        Element isoNode = batchXmlStructure.createElement("node");
        isoNode.setAttribute("shortName", "adresseavisen1759-400022028241-1-ISO-0001");

        String xPathStr = "/node/node[@shortName='" + filmID + "']";
        Node filmNode = DOM.createXPathSelector().selectNodeList(doc, xPathStr).item(0);
        filmNode.appendChild(isoFolderNode);
        isoFolderNode.appendChild(isoNode);
    }
}
