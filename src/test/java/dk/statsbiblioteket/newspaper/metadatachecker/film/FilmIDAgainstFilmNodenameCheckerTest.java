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

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class FilmIDAgainstFilmNodenameCheckerTest {
    private ResultCollector resultCollector;
    private FilmIDAgainstFilmNodenameChecker checker;

    @BeforeMethod
    public void setup() {
        resultCollector = mock(ResultCollector.class);
        XPathSelector xpathSelector = DOM.createXPathSelector("avis",
                "http://www.statsbiblioteket.dk/avisdigitalisering/microfilm/1/0/");
        checker = new FilmIDAgainstFilmNodenameChecker(resultCollector, xpathSelector);
    }

    @Test
    public void simpleIDTest() {
        String filmID = "film-1";
        Document filmEventDoc = createFilmXmlDoc(filmID);
        checker.validate(createFilmEvent(filmID), filmEventDoc);
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void multipleFilmTest() {
        String film1ID = "film-1";
        String film2ID = "film-2";
        Document film1EventDoc = createFilmXmlDoc(film1ID);

        checker.validate(createFilmEvent(film1ID), film1EventDoc);
        verifyNoMoreInteractions(resultCollector);

        Document film2EventDoc = createFilmXmlDoc(film2ID);
        checker.validate(createFilmEvent(film2ID), film2EventDoc);
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void invalidFilmIDTest() {
        String filmID = "film-1";
        Document filmEventDoc = createFilmXmlDoc(filmID);
        AttributeParsingEvent event = createFilmEvent(filmID + "-1");
        checker.validate(event, filmEventDoc);
        verify(resultCollector).addFailure(eq(event.getName()), eq(FailureType.METADATA.value()),
                eq(FilmIDAgainstFilmNodenameChecker.class.getSimpleName()), anyString());
        verifyNoMoreInteractions(resultCollector);
    }

    private AttributeParsingEvent createFilmEvent(final String filmID) {
        return new AttributeParsingEvent("reelID/" + filmID + "/avisID-filmID.film.xml") {
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

    private Document createFilmXmlDoc(final String filmID) {
        final String filmXmlStructure =
                "<avis:reelMetadata xmlns:avis=\"http://www.statsbiblioteket.dk/avisdigitalisering/microfilm/1/0/\">\n" +
                        "    <avis:FilmId>" + filmID + "</avis:FilmId>\n" +
                        "</avis:reelMetadata>";
        return DOM.stringToDOM(filmXmlStructure);
    }
}
