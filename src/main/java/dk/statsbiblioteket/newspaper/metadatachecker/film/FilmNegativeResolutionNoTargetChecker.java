package dk.statsbiblioteket.newspaper.metadatachecker.film;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.newspaper.metadatachecker.checker.FailureType;
import dk.statsbiblioteket.newspaper.metadatachecker.checker.XmlAttributeChecker;
import dk.statsbiblioteket.util.xml.XPathSelector;

/**
 * Check whether the value of resolutionOfNegative in the film xml (2E-14) is 0.0 when no FILM-ISO-target is present.
 */
public class FilmNegativeResolutionNoTargetChecker extends XmlAttributeChecker {
    private final XPathSelector xPathSelector;
    private final Document batchXmlStructure;

    public FilmNegativeResolutionNoTargetChecker(ResultCollector resultCollector, XPathSelector xPathSelector,
                                                 Document batchXmlStructure) {
        super(resultCollector, FailureType.METADATA);
        this.xPathSelector = xPathSelector;
        this.batchXmlStructure = batchXmlStructure;
    }

    public void validate(AttributeParsingEvent event, Document doc) {
        double resolutionOfNegative = xPathSelector.selectDouble(
                        doc, "avis:reelMetadata/avis:resolutionOfNegative");
        String filmID = event.getName()
                             .split("/")[1].replace(".film.xml", "");
        NodeList pageImages = xPathSelector.selectNodeList(
                batchXmlStructure,
                "/node/node[@shortName='" + filmID + "']/node[@shortName='FILM-ISO-target']/node");
        if (resolutionOfNegative == 0.0d && pageImages.getLength() > 0) {
            addFailure(
                    event,
                    "2E-14: When a FILM-ISO-target is present, resolutionOfNegative must not be 0.0");
        }
        if (resolutionOfNegative != 0.0d && pageImages.getLength() == 0) {
            addFailure(
                    event,
                    "2E-14: When no FILM-ISO-target is present, resolutionOfNegative must be 0.0");
        }
    }

    @Override
    public boolean shouldCheckEvent(AttributeParsingEvent event) {
        return event.getName().endsWith(".film.xml");
    }
}
