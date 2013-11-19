package dk.statsbiblioteket.newspaper.metadatachecker.film;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.util.xml.DOM;
import org.testng.annotations.BeforeMethod;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import static org.mockito.Mockito.mock;

public class FilmNumberOfPicturesCheckerTest {
    private ResultCollector resultCollector;
    private Document batchXmlStructure;
    private FilmDateVsEditionsChecker checker;

    @BeforeMethod
    public void setup() {
        resultCollector = mock(ResultCollector.class);
        batchXmlStructure = createBatchXmlStructure();
        checker = new FilmDateVsEditionsChecker(resultCollector, batchXmlStructure);
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
