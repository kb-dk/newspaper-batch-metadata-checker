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
 * This class checks the date range for a film in the film.xml metadata against the list of known
 * date ranges for films in the current batch (from mfpak).
 */
public class FilmDateRangeAgainstMfpakChecker extends XmlAttributeChecker {

    /**
     * The sorted list of known date ranges. From mfpak.
     */
    private List<NewspaperDateRange> dateRanges;

    private Batch batch;
    private ResultCollector resultCollector;
    private XPathSelector filmXPathSelector;

    /**
     * Constructor for this class.
     * @param resultCollector
     * @param mfPakDAO
     * @param batch
     */
    public FilmDateRangeAgainstMfpakChecker(ResultCollector resultCollector, MfPakDAO mfPakDAO, Batch batch) {
        super(resultCollector, FailureType.METADATA);
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

    /**
     * The algorithm is that each time we find a matching film/date-range we remove it from the dateRanges
     * list. At the end of the batch the list should be empty.
     * @param event The event to base the check on.
     * @param filmMetaData  the xml representation of the film metadata.
     */
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
                         getClass().getSimpleName(),
                         "2E-2, 2E-3: No film.xml file was found for the film with date range " +
                                 "(" + range.getFromDate() + "," + range.getToDate() + ")."
                 );
            }
        }
    }
}
