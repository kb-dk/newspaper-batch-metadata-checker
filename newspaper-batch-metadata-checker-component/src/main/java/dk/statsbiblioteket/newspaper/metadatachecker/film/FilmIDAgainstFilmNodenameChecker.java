package dk.statsbiblioteket.newspaper.metadatachecker.film;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.newspaper.metadatachecker.checker.FailureType;
import dk.statsbiblioteket.newspaper.metadatachecker.checker.XmlAttributeChecker;
import dk.statsbiblioteket.util.xml.XPathSelector;
import org.w3c.dom.Document;

/**
 * Check whether the filmId specified in the film.xml (2E-4) corresponds to the name of the film node.
 */
public class FilmIDAgainstFilmNodenameChecker extends XmlAttributeChecker {
    private final XPathSelector xPathSelector;

    public FilmIDAgainstFilmNodenameChecker(
            ResultCollector resultCollector, XPathSelector xPathSelector) {
        super(resultCollector, FailureType.METADATA);
        this.xPathSelector = xPathSelector;
    }

    public void validate(AttributeParsingEvent event, Document doc) {
        String filmIdFromXml = xPathSelector.selectString(doc, "/avis:reelMetadata/avis:FilmId");
        String filmIDFromEvent = event.getName().split("/")[1];
        if (!filmIdFromXml.equals(filmIDFromEvent)) {
            addFailure(event, "2E-4: FilmID in film.xml '" + filmIdFromXml +
                    "' doesn't correspond to the node name '" + filmIDFromEvent + "'.");
        }
    }
    @Override
    public boolean shouldCheckEvent(AttributeParsingEvent event) {
        return event.getName().endsWith(".film.xml");
    }
}
