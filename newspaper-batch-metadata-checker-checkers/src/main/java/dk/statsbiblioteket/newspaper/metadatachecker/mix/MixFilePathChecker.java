package dk.statsbiblioteket.newspaper.metadatachecker.mix;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.newspaper.metadatachecker.checker.FailureType;
import dk.statsbiblioteket.newspaper.metadatachecker.checker.XmlAttributeChecker;
import org.w3c.dom.Document;

public class MixFilePathChecker extends XmlAttributeChecker {
    public MixFilePathChecker(ResultCollector resultCollector) {
        super(resultCollector, FailureType.METADATA);
    }

    /** Validate that the objectIdentifier is of the form [Film-id]-[Billed-id] */
    public void validate(AttributeParsingEvent event, Document doc) {
        final String xpath2K1a = "/mix:mix/mix:BasicDigitalObjectInformation/mix:ObjectIdentifier"
                + "[mix:objectIdentifierType='Image Unique ID']/mix:objectIdentifierValue";
        final String xpath2K1b = "/mix:mix/mix:BasicDigitalObjectInformation/mix:ObjectIdentifier"
                + "[mix:objectIdentifierType='ISO Film Target Image ID']/mix:objectIdentifierValue";
        final String xpath2K12 = "/mix:mix/mix:ImageCaptureMetadata/mix:SourceInformation/mix:sourceType/text()";
        final String xpath2K13b = "/mix:mix/mix:ImageCaptureMetadata/mix:SourceInformation/mix:SourceID"
                + "[mix:sourceIDType='ISO Film Target ID']/mix:sourceIDValue";
        final String xpath2K13a = "/mix:mix/mix:ImageCaptureMetadata/mix:SourceInformation/mix:SourceID"
                + "[mix:sourceIDType='Microfilm reel barcode #']/mix:sourceIDValue";
        final String xpath2K14 = "/mix:mix/mix:ImageCaptureMetadata/mix:SourceInformation/mix:SourceID"
                + "[mix:sourceIDType='Location on microfilm']/mix:sourceIDValue";
        String mixSourceInformation = XPATH.selectString(doc, xpath2K12);
        String mixBilledeID = XPATH.selectString(doc, xpath2K14);
        String billedID = getBilledIDFromEvent(event);
        String objectIdentifier;
        String mixFilmID;
        String filmID = getFilmIDFromEvent(event);
        String identifierFromPath = getIdentifierFromEvent(event);

        if (event.getName().contains("/WORKSHIFT-ISO-TARGET/")) {
            if (!"iso-film-target".equals(mixSourceInformation)) {
                addFailure(
                        event,
                        "2K-12: SourceInformation does not match the expected value. "
                                + "Got '" + mixSourceInformation + "' expected 'iso-film-target'.");
            }
            objectIdentifier = XPATH.selectString(doc, xpath2K1b);
            mixFilmID = XPATH.selectString(doc, xpath2K13b);
        } else {
            if (!"microfilm".equals(mixSourceInformation)) {
                addFailure(
                        event,
                        "2K-12: SourceInformation does not match the expected value. "
                                + "Got '" + mixSourceInformation + "' expected 'microfilm'.");
            }
            objectIdentifier = XPATH.selectString(doc, xpath2K1a);
            mixFilmID = XPATH.selectString(doc, xpath2K13a);
        }
        if (!objectIdentifier.equals(identifierFromPath)) {
            addFailure(
                    event,
                    "2K-1: ObjectIdentifier does not match the location in the tree. "
                            + "Got '" + objectIdentifier + "' expected '" + identifierFromPath + "'.");
        }

        if (!mixFilmID.equals(filmID)) {
            addFailure(
                    event,
                    "2K-13: FilmID does not match the location in the tree. "
                            + "Got '" + mixFilmID + "' expected '" + filmID + "'.");
        }

        if (!mixBilledeID.equals(billedID)) {
            addFailure(
                    event,
                    "2K-14: Location on film does not match the location in the tree. "
                            + "Expected '" + mixBilledeID + "' got '" + billedID + "'.");
        }
    }

    private String getIdentifierFromEvent(AttributeParsingEvent event) {
        if (event.getName().contains("/WORKSHIFT-ISO-TARGET/")) {
              /* WORKSHIFT-ISO-TARGET is special in the sense that it does not belong to a film.
                 The 'filmID' is [batchID]-00. */
            String[] pathParts = event.getName().split("/");
            return  pathParts[0].substring(1,13) + "-00-" + getBilledIDFromEvent(event);
        } else {
            return getFilmIDFromEvent(event) + "-" + getBilledIDFromEvent(event);
        }
    }

    private String getBilledIDFromEvent(AttributeParsingEvent event) {
        String name = event.getName();
        String filename = name.substring(name.lastIndexOf("/") + 1, name.indexOf(".mix.xml"));
        if (filename.endsWith("brik")) {
            String[] splits = filename.split("-");
            return splits[splits.length - 2] + "-" + splits[splits.length - 1];
        } else {
            return filename.substring(
                    filename.lastIndexOf("-") + 1);
        }

    }

    private String getFilmIDFromEvent(AttributeParsingEvent event) {
        String filmID;
        if (event.getName().contains("/WORKSHIFT-ISO-TARGET/")) {
              /* WORKSHIFT-ISO-TARGET is special in the sense that it does not belong to a film.
                 The 'filmID' here is hard coded. */
            filmID = "iso-film-target-0001";
        } else {
              /* The event.getName should return something like: /batchID/filmID/dir/pagedir/page.xml
                 We want the filmID part, which should be the 2. (index 1) part */
            String[] pathParts = event.getName().split("/");
            filmID = pathParts[1];
        }
        return filmID;
    }

    @Override
    public boolean shouldCheckEvent(AttributeParsingEvent event) {
        return event.getName()
                    .endsWith(".mix.xml");
    }


}
