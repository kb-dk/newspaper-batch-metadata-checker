package dk.statsbiblioteket.newspaper.metadatachecker.film;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.newspaper.metadatachecker.checker.FailureType;
import dk.statsbiblioteket.newspaper.metadatachecker.checker.XmlAttributeChecker;
import dk.statsbiblioteket.util.xml.XPathSelector;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * Checks that the editions contained in a film for a batch structure are inside of the start/end dates specified in the
 * film.xml.
 */
public class FilmDateVsEditionsChecker extends XmlAttributeChecker {
    private final XPathSelector xPathSelector;
    private final Document batchXmlStructure;

    public FilmDateVsEditionsChecker(ResultCollector resultCollector, XPathSelector xPathSelector,
                                     Document batchXmlStructure) {
        super(resultCollector, FailureType.METADATA);
        this.xPathSelector = xPathSelector;
        this.batchXmlStructure = batchXmlStructure;
    }

    @Override
    public void validate(AttributeParsingEvent event, Document filmMetaData) {
        String filmStartdate = xPathSelector.selectString(filmMetaData, "/avis:reelMetadata/avis:startDate");
        String filmEnddate = xPathSelector.selectString(filmMetaData, "/avis:reelMetadata/avis:endDate");
        FuzzyDate filmStart = new FuzzyDate(filmStartdate);
        FuzzyDate filmEnd = new FuzzyDate(filmEnddate);
        FuzzyDate editionsStart = null;
        FuzzyDate editionsEnd = null;
        String filmID = event.getName()
                             .split("/")[1].replace(".film.xml", "");
        NodeList editions = xPathSelector.selectNodeList(
                batchXmlStructure,
                "/node/node[@shortName='" + filmID + "']/node[@shortName!='FILM-ISO-target'][@shortName!='UNMATCHED']");
        for (int index = 0; index < editions.getLength(); index++) {
            String editionID = editions.item(index).getAttributes().getNamedItem("shortName").getNodeValue();
            FuzzyDate editionDate = new FuzzyDate(editionID.substring(0, editionID.lastIndexOf('-')));
            if (editionsStart == null || editionDate.before(editionsStart)) {
                editionsStart = editionDate;
            }
            if (editionsEnd == null || editionDate.after(editionsEnd)) {
                editionsEnd = editionDate;
            }
        }
        if (editionsStart == null || editionsEnd == null) {
            // No editions in film, do not check mix dates
            return;
        }
        if (!filmStart.equals(editionsStart)) {
            addFailure(
                    event,
                    "2E-2: Earliest edition date " + editionsStart.asString()
                            + " not the same as film start date " + filmStart.asString());
        }
        if (!filmEnd.equals(editionsEnd)) {
            addFailure(
                    event,
                    "2E-3: Latest edition date " + editionsEnd.asString()
                            + " not the same as film end date " + filmEnd.asString());
        }
    }

    @Override
    public boolean shouldCheckEvent(AttributeParsingEvent event) {
        return event.getName().endsWith(".film.xml");
    }
}
