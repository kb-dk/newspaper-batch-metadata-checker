package dk.statsbiblioteket.newspaper.metadatachecker.film;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.util.xml.DOM;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class FilmDateVsEditionsCheckerTest {
    private ResultCollector resultCollector;
    private Document batchXmlStructure;
    FilmDateVsEditionsChecker checker;

    @BeforeMethod
    public void setup() {
        resultCollector = mock(ResultCollector.class);
        batchXmlStructure = createBatchXmlStructure();
        checker = new FilmDateVsEditionsChecker(resultCollector, batchXmlStructure);
    }

    @Test
    public void goodCaseTest() {
        String filmID = "film-1";
        addFilmNode(batchXmlStructure, filmID);
        addEditionNode(batchXmlStructure, filmID, "2013-10-10-1");
        checker.handleAttribute(createFilmEvent(filmID, "2013-10-10", "2013-10-12"));
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void editionToOldTest() {
        String filmID = "film-1";
        addFilmNode(batchXmlStructure, filmID);
        addEditionNode(batchXmlStructure, filmID, "2012-10-10-1");
        checker.handleAttribute(createFilmEvent(filmID, "2013-10-10", "2013-10-12"));
        verify(resultCollector).addFailure(anyString(), anyString(), anyString(), anyString());
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void editionToYoungTest() {
        String filmID = "film-1";
        addFilmNode(batchXmlStructure, filmID);
        addEditionNode(batchXmlStructure, filmID, "2013-10-13-2");
        checker.handleAttribute(createFilmEvent(filmID, "2013-10-10", "2013-10-12"));
        verify(resultCollector).addFailure(anyString(), anyString(), anyString(), anyString());
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void editionToOldFluzzyTest() {
        String filmID = "film-1";
        addFilmNode(batchXmlStructure, filmID);
        addEditionNode(batchXmlStructure, filmID, "2012-10-10-1");
        checker.handleAttribute(createFilmEvent(filmID, "2013-10-00", "2013-10-00"));
        verify(resultCollector).addFailure(anyString(), anyString(), anyString(), anyString());
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void editionToYoungFluzzyTest() {
        String filmID = "film-1";
        addFilmNode(batchXmlStructure, filmID);
        addEditionNode(batchXmlStructure, filmID, "2013-11-01-2");
        checker.handleAttribute(createFilmEvent(filmID, "2013-10-00", "2013-10-00"));
        verify(resultCollector).addFailure(anyString(), anyString(), anyString(), anyString());
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void verifyEditionDateContainmentGoodcaseTest() {
        FilmDateVsEditionsChecker checker = new FilmDateVsEditionsChecker(resultCollector, null);
        checker.verifyEditionDateContainment("2013-10-10", "2013-10-12", "2013-10-11-1", "filmID");
        verifyNoMoreInteractions(resultCollector);
        checker.verifyEditionDateContainment("2013-10-10", "2013-10-12", "2013-10-10-1", "filmID");
        verifyNoMoreInteractions(resultCollector);
        checker.verifyEditionDateContainment("2013-10-10", "2013-10-12", "2013-10-12-1", "filmID");
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void verifyEditionDateContainmentFuzzyTest() {
        FilmDateVsEditionsChecker checker = new FilmDateVsEditionsChecker(resultCollector, null);
        checker.verifyEditionDateContainment("2013-10-10", "2013-10-12", "2013-10-00-1", "filmID");
        verifyNoMoreInteractions(resultCollector);
        checker.verifyEditionDateContainment("2013-10-10", "2013-10-12", "2013-00-00-1", "filmID");
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void verifyEditionDateContainmentBadcaseTest() {
        FilmDateVsEditionsChecker checker = new FilmDateVsEditionsChecker(resultCollector, null);
        checker.verifyEditionDateContainment("2013-10-10", "2013-10-12", "2013-10-14-1", "filmID");
        verify(resultCollector).addFailure(anyString(), anyString(), anyString(), anyString());
        verifyNoMoreInteractions(resultCollector);
    }

    private Document createBatchXmlStructure() {
        String batchXmlStructure =
                "<node></node>";
        return DOM.stringToDOM(batchXmlStructure);
    }

    private AttributeParsingEvent createFilmEvent(final String filmID, final String startDate, final String endDate) {
        final String filmXmlStructure =
                "<avis:reelMetadata xmlns:avis=\"http://www.statsbiblioteket.dk/avisdigitalisering/microfilm/1/0/\">\n" +
                "    <avis:startDate>" + startDate + "</avis:startDate>\n" +
                "    <avis:endDate>" + endDate + "</avis:endDate>\n" +
                "</avis:reelMetadata>";
        return new AttributeParsingEvent("reelID/" + filmID + ".film.xml") {
            @Override
            public InputStream getData() throws IOException {
                return new ByteArrayInputStream(filmXmlStructure.getBytes());
            }
            @Override
            public String getChecksum() throws IOException {
                return null;
            }
        };
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
     * Adds a edition node to the indicated film node in the supplied film xml document.
     */
    private void addEditionNode(Document doc, String filmID, String newEditionID) {
        Element editionNode = batchXmlStructure.createElement("node");
        editionNode.setAttribute("shortName", newEditionID);

        String xPathStr = "/node/node[@shortName='" + filmID + "']";
        Node filmNode = DOM.createXPathSelector().selectNodeList(doc, xPathStr).item(0);
        filmNode.appendChild(editionNode);
    }

}
