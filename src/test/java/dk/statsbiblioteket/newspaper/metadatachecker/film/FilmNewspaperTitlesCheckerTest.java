package dk.statsbiblioteket.newspaper.metadatachecker.film;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.newspaper.metadatachecker.checker.FailureType;
import dk.statsbiblioteket.newspaper.metadatachecker.mockers.FilmMocker;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.MfPakDAO;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperDateRange;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperEntity;
import dk.statsbiblioteket.util.xml.DOM;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 *
 * The test data is as follows:
 * A newspaper with avisId: Titleavis
 * and titles
 * title1: up to 1920-03-15
 * title2: up to 1920-04-15
 * title3: up to 1920-05-15
 * title4: up to 1930-03-15
 * title5: up to 1930:04.15
 * title6: thereafter
 *
 * Then we test with various film-xml structures with both precise and fuzzy dates for the ends.
 */
public class FilmNewspaperTitlesCheckerTest {

    Batch batch;
    MfPakDAO dao;

    @BeforeTest
    public void setUp() throws SQLException, ParseException {
        batch = new Batch();
        batch.setBatchID("400022028241");
                batch.setRoundTripNumber(1);
        dao = mock(MfPakDAO.class);
        when(dao.getBatchNewspaperEntities(batch.getBatchID())).thenReturn(getEntities());
    }

    /**
     * Basic test that we can find the right titles if we have the exact dates of the film.
     * @throws Exception
     */
    @Test
    public void testGoodDataPrecise() throws Exception {
        AttributeParsingEvent event = FilmMocker.getFilmXmlAttributeParsingEvent(
                "14",
                "Titleavis",
                "title2</avis:titles>" +
                        "<avis:titles>title3</avis:titles>" +
                        "<avis:titles>title4</avis:titles>" +
                        "<avis:titles>title5</avis:titles>" +
                        "<avis:titles>title6",
                "1999-01-01",
                "1920-04-01",
                "1935-01-01",
                batch,
                300
        );
        Document filmDocument = DOM.streamToDOM(event.getData());
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        FilmNewspaperTitlesChecker checker = new FilmNewspaperTitlesChecker(resultCollector, FailureType.METADATA, dao, batch);
        checker.validate(event, filmDocument);
        assertTrue(resultCollector.isSuccess(), resultCollector.toReport());
    }

    /**
     * Basic test that we can find the right titles if we have the exact dates of the film. This
     * tests the corner case when the film exactly matches start/end dates for the titles.
     * @throws Exception
     */
    @Test
    public void testGoodDataPreciseCornerCase() throws IOException {
        AttributeParsingEvent event = FilmMocker.getFilmXmlAttributeParsingEvent(
                "14",
                "Titleavis",
                "title2</avis:titles>" +
                        "<avis:titles>title3</avis:titles>" +
                        "<avis:titles>title4</avis:titles>" +
                        "<avis:titles>title5</avis:titles>" +
                        "<avis:titles>title6",
                "1999-01-01",
                "1920-04-15",
                "1930-04-16",
                batch,
                300
        );
        Document filmDocument = DOM.streamToDOM(event.getData());
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        FilmNewspaperTitlesChecker checker = new FilmNewspaperTitlesChecker(resultCollector, FailureType.METADATA, dao, batch);
        checker.validate(event, filmDocument);
        assertTrue(resultCollector.isSuccess(), resultCollector.toReport());
    }

    /**
     * Fuzzy-date test:
     * from 1920-04 to 1930 so
     * title2, title5, title6 are optional
     * title3, title4 required
     * title1 forbidden
     * Test data has some but not all of the optional titles.
     * @throws Exception
     */
    @Test
    public void testGoodDataFuzzy() throws Exception {
        AttributeParsingEvent event = FilmMocker.getFilmXmlAttributeParsingEvent(
                "14",
                "Titleavis",
                "title3</avis:titles>" +
                        "<avis:titles>title4</avis:titles>" +
                        "<avis:titles>title5</avis:titles>" +
                        "<avis:titles>title6",
                "1999-01-01",
                "1920-04-00",
                "1930-00-00",
                batch,
                300
        );
        Document filmDocument = DOM.streamToDOM(event.getData());
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        FilmNewspaperTitlesChecker checker = new FilmNewspaperTitlesChecker(resultCollector, FailureType.METADATA, dao, batch);
        checker.validate(event, filmDocument);
        assertTrue(resultCollector.isSuccess(), resultCollector.toReport());
    }

    /**
     * Disallowed title: title1
     * @throws Exception
     */
    @Test
    public void testBadDataExtraTitlePrecise() throws Exception {
        AttributeParsingEvent event = FilmMocker.getFilmXmlAttributeParsingEvent(
                "14",
                "Titleavis",
                "title1</avis:titles>" +
                        "<avis:titles>title2</avis:titles>" +
                        "<avis:titles>title3</avis:titles>" +
                        "<avis:titles>title4</avis:titles>" +
                        "<avis:titles>title5</avis:titles>" +
                        "<avis:titles>title6",

                "1999-01-01",
                "1920-04-01",
                "1935-01-01",
                batch,
                300
        );
        Document filmDocument = DOM.streamToDOM(event.getData());
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        FilmNewspaperTitlesChecker checker = new FilmNewspaperTitlesChecker(resultCollector, FailureType.METADATA, dao, batch);
        checker.validate(event, filmDocument);
        final String report = resultCollector.toReport();
        assertFalse(resultCollector.isSuccess(), report);
        assertTrue(report.contains("2E-1"), report);
        assertTrue(report.contains("title1"), report);
    }

    /**
     * title3 should be present but is missing
     * @throws Exception
     */
    @Test
    public void testBadDataMissingTitlePrecise() throws Exception {
        AttributeParsingEvent event = FilmMocker.getFilmXmlAttributeParsingEvent(
                "14",
                "Titleavis",
                "title2</avis:titles>" +
                        "<avis:titles>title4</avis:titles>" +
                        "<avis:titles>title5</avis:titles>" +
                        "<avis:titles>title6",

                "1999-01-01",
                "1920-04-01",
                "1935-01-01",
                batch,
                300
        );
        Document filmDocument = DOM.streamToDOM(event.getData());
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        FilmNewspaperTitlesChecker checker = new FilmNewspaperTitlesChecker(resultCollector, FailureType.METADATA, dao, batch);
        checker.validate(event, filmDocument);
        final String report = resultCollector.toReport();
        assertFalse(resultCollector.isSuccess(), report);
        assertTrue(report.contains("2E-1"), report);
        assertTrue(report.contains("title3"), report);
    }

    /**
     * from 1920-04 to 1930 so
     * title2, title5, title6 are optional
     * title3, title4 required
     * title1 forbidden
     * Test data has forbidden title.
     * @throws Exception
     */
    @Test
    public void testBadDataExtraTitleFuzzy() throws Exception {
        AttributeParsingEvent event = FilmMocker.getFilmXmlAttributeParsingEvent(
                "14",
                "Titleavis",
                "title1</avis:titles>" +
                        "<avis:titles>title3</avis:titles>" +
                        "<avis:titles>title4</avis:titles>" +
                        "<avis:titles>title5</avis:titles>" +
                        "<avis:titles>title6",
                "1999-01-01",
                "1920-04",
                "1930",
                batch,
                300
        );
        Document filmDocument = DOM.streamToDOM(event.getData());
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        FilmNewspaperTitlesChecker checker = new FilmNewspaperTitlesChecker(resultCollector, FailureType.METADATA, dao, batch);
        checker.validate(event, filmDocument);
        final String report = resultCollector.toReport();
        assertFalse(resultCollector.isSuccess(), report);
        assertTrue(report.contains("2E-1"), report);
        assertTrue(report.contains("title1"));
    }

    /**
     * from 1920-04 to 1930 so
     * title2, title5, title6 are optional
     * title3, title4 required
     * title1 forbidden
     * Test data missing title3
     * @throws Exception
     */
    @Test
    public void testBadDataMissingTitleFuzzy() throws Exception {
        AttributeParsingEvent event = FilmMocker.getFilmXmlAttributeParsingEvent(
                "14",
                "Titleavis",
                "title1</avis:titles>" +
                        "<avis:titles>title2</avis:titles>" +
                        "<avis:titles>title4</avis:titles>" +
                        "<avis:titles>title5</avis:titles>" +
                        "<avis:titles>title6",
                "1999-01-01",
                "1920-04-00",
                "1930-00-00",
                batch,
                300
        );
        Document filmDocument = DOM.streamToDOM(event.getData());
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        FilmNewspaperTitlesChecker checker = new FilmNewspaperTitlesChecker(resultCollector, FailureType.METADATA, dao, batch);
        checker.validate(event, filmDocument);
        final String report = resultCollector.toReport();
        assertFalse(resultCollector.isSuccess(), report);
        assertTrue(report.contains("2E-1"), report);
        assertTrue(report.contains("title3"));
    }


    private List<NewspaperEntity> getEntities() throws ParseException {
        List<NewspaperEntity> entities = new ArrayList<>();
        entities.add(getNewspaperEntity("1900-06-01", "1920-03-15", 1));
        entities.add(getNewspaperEntity("1920-03-16", "1920-04-15", 2));
        entities.add(getNewspaperEntity("1920-04-16", "1920-05-15", 3));
        entities.add(getNewspaperEntity("1920-05-16", "1930-03-15", 4));
        entities.add(getNewspaperEntity("1930-03-16", "1930-04-15", 5));
        entities.add(getNewspaperEntity("1930-04-16", "3050-06-01", 6));
        return entities;
    }

    private NewspaperEntity getNewspaperEntity(String start, String end, int title) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        NewspaperDateRange r1 = new NewspaperDateRange(sdf.parse(start), sdf.parse(end) );
        NewspaperEntity e1 = new NewspaperEntity();
        e1.setNewspaperDateRange(r1);
        e1.setNewspaperID("Titleavis");
        e1.setNewspaperTitle("title" + title);
        return e1;
    }

}
