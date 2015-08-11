package dk.statsbiblioteket.newspaper.metadatachecker.film;

import java.io.IOException;
import java.io.InputStream;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.newspaper.metadatachecker.checker.FailureType;
import dk.statsbiblioteket.util.xml.DOM;
import dk.statsbiblioteket.util.xml.XPathSelector;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class FilmNumberOfPicturesCheckerTest {
    private ResultCollector resultCollector;
    private Document batchXmlStructure;
    private FilmNumberOfPicturesChecker checker;

    @BeforeMethod
    public void setupEnvironment() {
        resultCollector = mock(ResultCollector.class);
        batchXmlStructure = DOM.stringToDOM("<node></node>");
        XPathSelector xpathSelector = DOM.createXPathSelector("avis",
                "http://www.statsbiblioteket.dk/avisdigitalisering/microfilm/1/0/");
        checker = new FilmNumberOfPicturesChecker(resultCollector, xpathSelector, batchXmlStructure);
    }

    @Test
    public void simpleNumberTest() {
        String filmID = "film-1";
        String filmSubnodeID = "2013-10-10-1";
        addFilmNode(batchXmlStructure, filmID);
        addFilmSubnode(batchXmlStructure, filmID, filmSubnodeID);
        addImageNode(batchXmlStructure, filmSubnodeID, "image1.jp2");
        Document filmEventDoc = createFilmXmlDoc(1);
        checker.validate(createFilmEvent(filmID), filmEventDoc);
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void multipleFilmTest() {
        String film1ID = "film-1";
        String film2ID = "film-2";
        String film1SubnodeID = "2013-10-10-1";
        String film2SubnodeID = "2013-11-10-1";
        addFilmNode(batchXmlStructure, film1ID);
        addFilmNode(batchXmlStructure, film2ID);
        addFilmSubnode(batchXmlStructure, film1ID, film1SubnodeID);
        addFilmSubnode(batchXmlStructure, film2ID, film2SubnodeID);
        addImageNode(batchXmlStructure, film1SubnodeID, "film1-image1.jp2");
        addImageNode(batchXmlStructure, film2SubnodeID, "film2-image1.jp2");
        addImageNode(batchXmlStructure, film2SubnodeID, "film2-image2.jp2");
        Document film1EventDoc = createFilmXmlDoc(1);

        checker.validate(createFilmEvent(film1ID), film1EventDoc);
        verifyNoMoreInteractions(resultCollector);

        Document film2EventDoc = createFilmXmlDoc(2);
        checker.validate(createFilmEvent(film2ID), film2EventDoc);
        verifyNoMoreInteractions(resultCollector);
    }
    @Test
    public void multipleFilmSubnodesTest() {
        String film1ID = "film-1";
        String film1SubnodeID = "2013-10-10-1";
        String film2SubnodeID = "UNMATCHED";
        addFilmNode(batchXmlStructure, film1ID);
        addFilmSubnode(batchXmlStructure, film1ID, film1SubnodeID);
        addFilmSubnode(batchXmlStructure, film1ID, film2SubnodeID);
        addImageNode(batchXmlStructure, film1SubnodeID, "film1-image1.jp2");
        addImageNode(batchXmlStructure, film2SubnodeID, "film2-image1.jp2");
        addImageNode(batchXmlStructure, film2SubnodeID, "film2-image2.jp2");
        Document film1EventDoc = createFilmXmlDoc(3);

        checker.validate(createFilmEvent(film1ID), film1EventDoc);
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void invalidNumberTest() {
        String filmID = "film-1";
        String filmSubnodeID = "2013-10-10-1";
        addFilmNode(batchXmlStructure, filmID);
        addFilmSubnode(batchXmlStructure, filmID, filmSubnodeID);
        addImageNode(batchXmlStructure, filmSubnodeID, "image1.jp2");
        Document filmEventDoc = createFilmXmlDoc(2);
        AttributeParsingEvent event = createFilmEvent(filmID);
        checker.validate(event, filmEventDoc);
        verify(resultCollector).addFailure(eq(event.getName()), eq(FailureType.METADATA.value()),
                eq(FilmNumberOfPicturesChecker.class.getSimpleName()), anyString());
        verifyNoMoreInteractions(resultCollector);
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

    private Document createFilmXmlDoc(final int numberOfPictures) {
        final String filmXmlStructure =
                "<avis:reelMetadata xmlns:avis=\"http://www.statsbiblioteket.dk/avisdigitalisering/microfilm/1/0/\">\n" +
                        "    <avis:numberOfPictures>" + numberOfPictures + "</avis:numberOfPictures>\n" +
                        "</avis:reelMetadata>";
        return DOM.stringToDOM(filmXmlStructure);
    }

    /**
     * Adds a film node to a batchXmlStructure with the given short name.
     */
    private void addFilmNode(Document batchXmlStructure, String filmID) {
        Element filmNode = batchXmlStructure.createElement("node");
        filmNode.setAttribute("shortName", filmID);
        batchXmlStructure.getFirstChild().appendChild(filmNode);
    }

    /**
     * Adds a sub node to the indicated film node in the supplied batchXmlStructure xml document.
     */
    private void addFilmSubnode(Document batchXmlStructure, String filmID, String subnodeID) {
        Element editionNode = batchXmlStructure.createElement("node");
        editionNode.setAttribute("shortName", subnodeID);

        String xPathStr = "/node/node[@shortName='" + filmID + "']";
        Node filmNode = DOM.createXPathSelector().selectNodeList(batchXmlStructure, xPathStr).item(0);
        filmNode.appendChild(editionNode);
    }

    /**
     * Adds a edition node to the indicated film node in the supplied batchXmlStructure xml document.
     */
    private void addImageNode(Document batchXmlStructure, String subnodeID, String imageNodeID) {
        Element imageNode = batchXmlStructure.createElement("node");
        Element imageNodeParent = batchXmlStructure.createElement("node");
        imageNode.setAttribute("shortName", imageNodeID);
        imageNodeParent.appendChild(imageNode);

        String xPathStr = "/node/node/node[@shortName='" + subnodeID + "']";
        Node subnode = DOM.createXPathSelector().selectNodeList(batchXmlStructure, xPathStr).item(0);
        subnode.appendChild(imageNodeParent);
    }
}
