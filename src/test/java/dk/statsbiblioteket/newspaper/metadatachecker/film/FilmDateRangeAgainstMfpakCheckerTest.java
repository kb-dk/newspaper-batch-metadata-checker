package dk.statsbiblioteket.newspaper.metadatachecker.film;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.newspaper.mfpakintegration.batchcontext.BatchContext;
import dk.statsbiblioteket.newspaper.mfpakintegration.batchcontext.BatchContextUtils;
import dk.statsbiblioteket.newspaper.metadatachecker.checker.XmlAttributeChecker;
import dk.statsbiblioteket.newspaper.metadatachecker.mockers.FilmMocker;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.MfPakDAO;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperBatchOptions;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperDateRange;
import dk.statsbiblioteket.util.xml.DOM;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

/**
 * Unit tests for FilmDateRangeAgainstMfpakChecker
 */
public class FilmDateRangeAgainstMfpakCheckerTest {

    Batch batch;

    @BeforeTest
    public void setUp() throws SQLException, ParseException {
        batch = new Batch();
        batch.setBatchID("400022028241");
        batch.setRoundTripNumber(1);

    }

    /**
     * Define a batch with four consecutive films.
     * @return
     * @throws ParseException
     */
    private List<NewspaperDateRange> getRanges() throws ParseException {
        List<NewspaperDateRange> ranges = new ArrayList<>();
        ranges.add(getNewspaperDateRange("1850-03-16", "1851-03-15"));
        ranges.add(getNewspaperDateRange("1851-03-16", "1852-03-15"));
        ranges.add(getNewspaperDateRange("1852-03-16", "1853-03-15"));
        ranges.add(getNewspaperDateRange("1853-03-16", "1854-03-15"));
        return ranges;
    }

    private NewspaperDateRange getNewspaperDateRange(String start, String end) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return new NewspaperDateRange(sdf.parse(start), sdf.parse(end) );
    }

    /**
     * Tests that a single film event validates if it has the precise dates.
     * @throws IOException
     * @throws SQLException
     * @throws ParseException
     */
    @Test
    public void testGoodPreciseData() throws IOException, SQLException, ParseException {
        MfPakDAO dao = getMockDao(batch);
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        BatchContext context = BatchContextUtils.buildBatchContext(dao, batch);
        FilmDateRangeAgainstMfpakChecker checker = new FilmDateRangeAgainstMfpakChecker(resultCollector, context);
        checkEvent(checker, "1851-03-16", "1852-03-15");
        assertTrue(resultCollector.isSuccess(), resultCollector.toReport());
    }

    /**
     * Tests film dates for a complete batch with precise data - ie that all expected films are found.
     * @throws IOException
     * @throws SQLException
     * @throws ParseException
     */
    @Test
    public void testGoodPreciseDataComplete() throws IOException, SQLException, ParseException {
        MfPakDAO dao = getMockDao(batch);
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        BatchContext context = BatchContextUtils.buildBatchContext(dao, batch);
        FilmDateRangeAgainstMfpakChecker checker = new FilmDateRangeAgainstMfpakChecker(resultCollector, context);
        checkEvent(checker, "1850-03-16", "1851-03-15");
        checkEvent(checker, "1851-03-16", "1852-03-15");
        checkEvent(checker, "1852-03-16", "1853-03-15");
        checkEvent(checker, "1853-03-16", "1854-03-15");
        checker.finish();
        assertTrue(resultCollector.isSuccess(), resultCollector.toReport());
    }

    /**
     * Checks that we can validate that all films in a batch are present when some of the dates are fuzzy.
     * @throws IOException
     * @throws SQLException
     * @throws ParseException
     */
    @Test
    public void testGoodFuzzyDataComplete() throws IOException, SQLException, ParseException {
        MfPakDAO dao = getMockDao(batch);
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        BatchContext context = BatchContextUtils.buildBatchContext(dao, batch);
        FilmDateRangeAgainstMfpakChecker checker = new FilmDateRangeAgainstMfpakChecker(resultCollector, context);
        checkEvent(checker, "1850-03-16", "1851-03-15");
        checkEvent(checker, "1851-03-00", "1852-03-15");
        checkEvent(checker, "1852-03-16", "1853");
        checkEvent(checker, "1853-03-16", "1854-03-15");
        checker.finish();
        assertTrue(resultCollector.isSuccess(), resultCollector.toReport());
    }

    /**
     * Checks that we can identify a missing film in a batch when some of the film dates are fuzzy.
     * @throws IOException
     * @throws SQLException
     * @throws ParseException
     */
    @Test
    public void testBadFuzzyDataIncomplete() throws IOException, SQLException, ParseException {
        MfPakDAO dao = getMockDao(batch);
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        BatchContext context = BatchContextUtils.buildBatchContext(dao, batch);
        FilmDateRangeAgainstMfpakChecker checker = new FilmDateRangeAgainstMfpakChecker(resultCollector, context);
        checkEvent(checker, "1851-03-00", "1852-03-15");
        checkEvent(checker, "1852-03-16", "1853-00");
        checkEvent(checker, "1853-03-16", "1854-03-15");
        checker.finish();
        assertFalse(resultCollector.isSuccess(), resultCollector.toReport());
        assertTrue(resultCollector.toReport().contains("2E-2"), resultCollector.toReport());
    }


     /**
     * Checks that we can identify a missing film in a batch with non-fuzzy data.
     * @throws IOException
     * @throws SQLException
     * @throws ParseException
     */
    @Test
    public void testBadPreciseDataIncomplete() throws IOException, SQLException, ParseException {
        MfPakDAO dao = getMockDao(batch);
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        BatchContext context = BatchContextUtils.buildBatchContext(dao, batch);
        FilmDateRangeAgainstMfpakChecker checker = new FilmDateRangeAgainstMfpakChecker(resultCollector, context);
        checkEvent(checker, "1852-03-16", "1853-03-15");
        checker.finish();
        assertFalse(resultCollector.isSuccess(), resultCollector.toReport());
        assertTrue(resultCollector.toReport().contains("2E-2"), resultCollector.toReport());
    }

    /**
     * Checks that we can recognise an unexpected film in a batch via fuzzy data.
     * @throws IOException
     * @throws SQLException
     * @throws ParseException
     */
    @Test
    public void testBadDataUnknownFilm() throws IOException, SQLException, ParseException {
        MfPakDAO dao = getMockDao(batch);
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        BatchContext context = BatchContextUtils.buildBatchContext(dao, batch);
        FilmDateRangeAgainstMfpakChecker checker = new FilmDateRangeAgainstMfpakChecker(resultCollector, context);
        checkEvent(checker, "1852-03-16", "1853-06-00");
        assertFalse(resultCollector.isSuccess(), resultCollector.toReport());
        assertTrue(resultCollector.toReport().contains("2E-2"), resultCollector.toReport());
    }

    private MfPakDAO getMockDao(Batch batch) throws SQLException, ParseException {
        MfPakDAO dao  = mock(MfPakDAO.class);
        when(dao.getBatchDateRanges(batch.getBatchID())).thenReturn(getRanges());
        NewspaperBatchOptions options = new NewspaperBatchOptions();
        options.setOptionB1(false);
        options.setOptionB2(false);
        options.setOptionB9(false);
        when(dao.getBatchOptions(eq(batch.getBatchID()))).thenReturn(options);
        when(dao.getNewspaperID(eq(batch.getBatchID()))).thenReturn("foobar");
        when(dao.getBatchShipmentDate(eq(batch.getBatchID()))).thenReturn(new Date(0));
        
        return dao;
    }
    
    /**
     * Utility method to create and check a film.xml instance with specified start/end dates.
     * @param checker
     * @param start
     * @param end
     * @throws IOException
     */
    private void checkEvent(XmlAttributeChecker checker, String start, String end) throws IOException {
            AttributeParsingEvent event = FilmMocker.getFilmXmlAttributeParsingEvent(
                            "14",
                            "Titleavis",
                            "title",
                            "1999-01-01",
                            start,
                    end,
                    batch,
                    300
            );
          checker.validate(event, DOM.streamToDOM(event.getData()));
      }


}
