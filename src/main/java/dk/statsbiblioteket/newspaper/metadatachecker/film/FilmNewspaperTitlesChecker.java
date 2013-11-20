package dk.statsbiblioteket.newspaper.metadatachecker.film;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.newspaper.metadatachecker.checker.FailureType;
import dk.statsbiblioteket.newspaper.metadatachecker.checker.XmlAttributeChecker;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.MfPakDAO;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperEntity;
import dk.statsbiblioteket.util.xml.DOM;
import dk.statsbiblioteket.util.xml.XPathSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * This class checks the requirement of Appendix 2E-1, that the names of the newspaper titles found on a film are
 * in agreement with the date-range of the film and the known titles of the given newspaper within that date range.
 */
public class FilmNewspaperTitlesChecker extends XmlAttributeChecker {

    private static Logger log = LoggerFactory.getLogger(FilmNewspaperTitlesChecker.class);


    private MfPakDAO mfPakDAO;
    private Batch batch;
    private XPathSelector filmXPathSelector;

    public FilmNewspaperTitlesChecker(ResultCollector resultCollector, FailureType failureType, MfPakDAO mfPakDAO, Batch batch) {
        super(resultCollector, failureType);
        this.batch = batch;
        this.mfPakDAO = mfPakDAO;
        this.filmXPathSelector = DOM.createXPathSelector("avis",
                "http://www.statsbiblioteket.dk/avisdigitalisering/microfilm/1/0/");
    }

    /**
     * The logic of this algorithm is as follows. Because the actual start and end date of the file are fuzzy,
     * we take first the minimum extent of the film, then the maximum extent. The minimum defines the titles which
     * must be on the film, the maximum defines the titles which may be on the film.
     * @param event The event to base the check on.
     * @param filmMetaData  the xml to check.
     */
    @Override
    public void validate(AttributeParsingEvent event, Document filmMetaData) {
        String filmStartdate = filmXPathSelector.selectString(filmMetaData, "/avis:reelMetadata/avis:startDate");
        String filmEnddate = filmXPathSelector.selectString(filmMetaData, "/avis:reelMetadata/avis:endDate");
        FuzzyDate filmStart = new FuzzyDate(filmStartdate);
        FuzzyDate filmEnd = new FuzzyDate(filmEnddate);
        List<String> titlesOnFilm = getTitlesOnFilm(filmMetaData);
        List<NewspaperEntity> possibleNewspaperEntities;
        try {
            possibleNewspaperEntities = mfPakDAO.getBatchNewspaperEntities(batch.getBatchID());
        } catch (SQLException e) {
            throw new RuntimeException("SQL exception: ", e);
        }
        List<String> requiredTitles = findIncludedTitles(possibleNewspaperEntities, filmStart.getMaxDate(), filmEnd.getMinDate());
        List<String> allowedTitles  = findIncludedTitles(possibleNewspaperEntities, filmStart.getMinDate(), filmEnd.getMaxDate());
        for (String requiredTitle: requiredTitles) {
            if (!titlesOnFilm.contains(requiredTitle)) {
                addFailure(event, "2E-1: expected title '" + requiredTitle + "' not found.");
            }
        }
        for (String title: titlesOnFilm) {
            if (!allowedTitles.contains(title)) {
                addFailure(event, "2E-1: unexpected title '" + title + "' found");
            }
        }
    }


    private List<String> getTitlesOnFilm(Document filmMetaData) {
        NodeList titleNodes = filmXPathSelector.selectNodeList(filmMetaData, "/avis:reelMetadata/avis:titles");
        List<String> titlesOnFilm = new ArrayList<>();
        for (int nodeNumber = 0; nodeNumber < titleNodes.getLength(); nodeNumber++) {
            titlesOnFilm.add(titleNodes.item(nodeNumber).getTextContent().trim());
        }
        return titlesOnFilm;
    }

    /**
     * The logic is that if the start-date of the film title or the end date of the film title lies
     * within the film date-range then we include it. Also if the film title is valid for the entire film
     * range then we include it.
     * @param possibleNewspaperEntities all the known entities for this batch.
     * @param startLimit  assumed start date of the film.
     * @param endLimit    assumed end date of the film.
     */
    private List<String> findIncludedTitles(List<NewspaperEntity> possibleNewspaperEntities, Date startLimit, Date endLimit) {
        List<String> includedTitles = new ArrayList<>();
        for (NewspaperEntity newspaperEntity: possibleNewspaperEntities) {
            Date fromDate = newspaperEntity.getNewspaperDateRange().getFromDate();
            Date toDate = newspaperEntity.getNewspaperDateRange().getToDate();
            if (
                    (startLimit.before(fromDate) && endLimit.after(fromDate))
                            ||
                            (startLimit.before(toDate) && endLimit.after(toDate))
                            ||
                            (startLimit.after(fromDate) && endLimit.before(toDate))
                    ) {
                includedTitles.add(newspaperEntity.getNewspaperTitle().trim());
            }
        }
        return includedTitles;
    }

    @Override
    public boolean shouldCheckEvent(AttributeParsingEvent event) {
        return event.getName().endsWith(".film.xml");
    }
}
