package dk.statsbiblioteket.newspaper.metadatachecker.film;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.newspaper.metadatachecker.checker.FailureType;
import dk.statsbiblioteket.newspaper.metadatachecker.checker.XmlAttributeChecker;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.MfPakDAO;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperDateRange;
import dk.statsbiblioteket.util.xml.DOM;
import dk.statsbiblioteket.util.xml.XPathSelector;
import org.w3c.dom.Document;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 *
 */
public class FilmDateRangeAgainstMfpakChecker extends XmlAttributeChecker {

    private List<NewspaperDateRange> dateRanges;

    private Batch batch;
    private ResultCollector resultCollector;
    private XPathSelector filmXPathSelector;

    public FilmDateRangeAgainstMfpakChecker(ResultCollector resultCollector, FailureType failureType, MfPakDAO mfPakDAO, Batch batch) {
        super(resultCollector, failureType);
        this.batch = batch;
        this.resultCollector = resultCollector;
        this.filmXPathSelector = DOM.createXPathSelector("avis",
                "http://www.statsbiblioteket.dk/avisdigitalisering/microfilm/1/0/");
        try {
            dateRanges = mfPakDAO.getBatchDateRanges(batch.getBatchID());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void validate(AttributeParsingEvent event, Document filmMetaData) {
        String filmStartdate = filmXPathSelector.selectString(filmMetaData, "/avis:reelMetadata/avis:startDate");
        String filmEnddate = filmXPathSelector.selectString(filmMetaData, "/avis:reelMetadata/avis:endDate");
        FuzzyDate filmStart = new FuzzyDate(filmStartdate);
        FuzzyDate filmEnd = new FuzzyDate(filmEnddate);
        boolean foundRange = false;
        NewspaperDateRange theActualRange = null;
        for (NewspaperDateRange range: dateRanges) {
            if (
                    filmStart.compareTo(range.getFromDate()) == 0
                            &&
                            filmEnd.compareTo(range.getToDate()) == 0
                    ) {
                foundRange = true;
                theActualRange = range;
            }
        }
        if (!foundRange) {
            addFailure(event, "2E-2, 2E-3: Did not find any range in batch corresponding to the film with" +
                    " date range (" + filmStartdate + ","+filmEnddate + ")");
        } else {
            dateRanges.remove(theActualRange);
        }
    }

    @Override
    public boolean shouldCheckEvent(AttributeParsingEvent event) {
        return event.getName().endsWith(".film.xml");
    }

    @Override
    protected void finish() {
        if (!dateRanges.isEmpty()) {
            for (NewspaperDateRange range: dateRanges) {
                 resultCollector.addFailure(
                      batch.getFullID(),
                         FailureType.METADATA.name(),
                         getClass().getName(),
                         "2E-2, 2E-3: No film.xml file was found for the film with date range " +
                                 "(" + range.getFromDate() + "," + range.getToDate() + ")."
                 );
            }
        }
    }
}
