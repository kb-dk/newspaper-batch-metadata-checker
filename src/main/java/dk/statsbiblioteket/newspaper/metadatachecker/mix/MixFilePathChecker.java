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
        final String xpath2K2
                = "/mix:mix/mix:BasicDigitalObjectInformation/mix:ObjectIdentifier" + "[mix:objectIdentifierType='Image Unique ID']/mix:objectIdentifierValue";

        final String xpath2K3
                = "/mix:mix/mix:ImageCaptureMetadata/mix:SourceInformation/mix:SourceID" + "[mix:sourceIDType='Microfilm reel barcode #']/mix:sourceIDValue";

        final String xpath2K4
                = "/mix:mix/mix:ImageCaptureMetadata/mix:SourceInformation/mix:SourceID" + "[mix:sourceIDType='Location on microfilm']/mix:sourceIDValue";

        String objectIdentifier = XPATH.selectString(doc, xpath2K2);
        String mixFilmID = XPATH.selectString(doc, xpath2K3);
        String mixBilledeID = XPATH.selectString(doc, xpath2K4);
        String filmID = getFilmIDFromEvent(event);
        String infix = getInfixFromEvent(event);
        String billedID = getBilledIDFromEvent(event);
        String identifierFromPath = filmID + infix + billedID;
        if (!objectIdentifier.equals(identifierFromPath)) {
            addFailure(
                    event,
                    "2K-1: ObjectIdentifier does not match the location in the tree. " + "Expected '" + objectIdentifier + "' got '" + identifierFromPath + "'.");
        }

        if (!mixFilmID.equals(filmID)) {
            addFailure(
                    event,
                    "2K-13: FilmID does not match the location in the tree. " + "Expected '" + mixFilmID + "' got '" + filmID + "'.");
        }

        if (!mixBilledeID.equals(billedID)) {
            addFailure(
                    event,
                    "2K-14: Location on film does not match the location in the tree. " + "Expected '" + mixBilledeID + "' got '" + billedID + "'.");
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

    private String getInfixFromEvent(AttributeParsingEvent event) {
        if (event.getName()
                 .contains("/FILM-ISO-target/")) {
            return "-ISO-";
        } else {
            return "-";
        }
    }

    private String getFilmIDFromEvent(AttributeParsingEvent event) {
        String filmID;
        if (event.getName()
                 .contains("/WORKSHIFT-ISO-TARGET/")) {
              /* WORKSHIFT-ISO-TARGET is special in the sense that it does not belong to a film.
                 The 'filmID' here is based on the running number that is in the filename just
                 before the 'billedID' */
            String filename = event.getName()
                                   .substring(
                                           event.getName()
                                                .lastIndexOf("/"));
            filmID = filename.split("-")[1];
        } else {
              /* The event.getName should return something like: /batchID/filmID/dir/pagedir/page.xml
                 We want the filmID part, which should be the 2. (index 1) part */
            String[] pathParts = event.getName()
                                      .split("/");
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
