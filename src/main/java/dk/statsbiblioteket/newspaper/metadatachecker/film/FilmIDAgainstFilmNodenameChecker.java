package dk.statsbiblioteket.newspaper.metadatachecker.film;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.newspaper.metadatachecker.checker.MetadataFailureType;
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
        super(resultCollector, MetadataFailureType.METADATA);
        this.xPathSelector = xPathSelector;
    }

    public void validate(AttributeParsingEvent event, Document doc) {
        String filmIdFromXml = xPathSelector.selectString(doc, "/avis:reelMetadata/avis:batchIdFilmId");
        String filmIDFromEvent = event.getName().split("/")[1].replace(".film.xml","");
        if (filmIdFromXml != filmIDFromEvent) {
            addFailure(event, "2E-4: FildID in film.xml '" + filmIDFromEvent +
                    "' doesn't correspond to the node name '" + filmIDFromEvent + "'.");
        }
    }
}
