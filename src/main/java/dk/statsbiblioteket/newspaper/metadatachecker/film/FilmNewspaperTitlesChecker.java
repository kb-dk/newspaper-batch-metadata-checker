package dk.statsbiblioteket.newspaper.metadatachecker.film;

import java.io.IOException;
import java.sql.SQLException;
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
import org.w3c.dom.Document;

public class FilmNewspaperTitlesChecker extends XmlAttributeChecker {

    private MfPakDAO mfPakDAO;
    private Batch batch;
    private XPathSelector filmXPathSelector;
    private ResultCollector resultCollector;

    public FilmNewspaperTitlesChecker(ResultCollector resultCollector, FailureType failureType, MfPakDAO mfPakDAO, Batch batch) {
        super(resultCollector, failureType);
        this.batch = batch;
        this.mfPakDAO = mfPakDAO;
        this.resultCollector = resultCollector;
        this.filmXPathSelector = DOM.createXPathSelector("avis",
                        "http://www.statsbiblioteket.dk/avisdigitalisering/microfilm/1/0/");
    }

    @Override
    public void validate(AttributeParsingEvent event, Document doc) {
        if (event.getName().endsWith(".film.xml")) {
            Document filmMetaData;
            try {
                filmMetaData = DOM.streamToDOM(event.getData());
                if (filmMetaData == null) {
                    addFailure(event.getName(), "Could not parse xml");
                    return;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String filmStartdate = filmXPathSelector.selectString(filmMetaData, "/avis:reelMetadata/avis:startDate");
            String filmEnddate = filmXPathSelector.selectString(filmMetaData, "/avis:reelMetadata/avis:endDate");
            String filmID = event.getName().split("/")[1].replace(".film.xml","");
        }
        List<NewspaperEntity> possibleNewspaperEntities = null;
        try {
            possibleNewspaperEntities = mfPakDAO.getBatchNewspaperEntities(batch.getBatchID());
        } catch (SQLException e) {
            throw new RuntimeException("SQL exception: ", e);
        }
        for (NewspaperEntity newspaperEntity: possibleNewspaperEntities) {

        }


    }

    private void addFailure(String eventName, String description) {
            resultCollector.addFailure(
                    eventName, "metadata", getClass().getSimpleName(), description);
        }

}
