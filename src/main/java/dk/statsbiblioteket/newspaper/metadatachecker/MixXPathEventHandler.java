package dk.statsbiblioteket.newspaper.metadatachecker;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.DefaultTreeEventHandler;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.MfPakDAO;
import dk.statsbiblioteket.util.xml.DOM;
import dk.statsbiblioteket.util.xml.XPathSelector;

import org.w3c.dom.Document;

import java.io.IOException;
import java.util.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import static dk.statsbiblioteket.util.Strings.getStackTrace;

/**
 * Class to handle MIX crosschecks
 * Checks that:
 * 1. The scanned date is after the date that it was shipped from SB.
 */
public class MixXPathEventHandler extends DefaultTreeEventHandler {

    private static final XPathSelector XPATH = DOM.createXPathSelector("mix", "http://www.loc.gov/mix/v20");
    private ResultCollector resultCollector;
    private MfPakDAO mfPakDAO;
    private Batch batch;
    private Date shipmentDate;

    /**
     * Constructor for this class.
     *
     * @param resultCollector the result collector to collect errors in
     * @param mfPakDAO        a DAO object from which one can read relevant external properties of a batch.
     * @param batch           a batch object representing the batch being analysed.
     */
    public MixXPathEventHandler(ResultCollector resultCollector, MfPakDAO mfPakDAO, Batch batch) {
        this.resultCollector = resultCollector;
        this.mfPakDAO = mfPakDAO;
        this.batch = batch;

        try {
            shipmentDate = mfPakDAO.getBatchShipmentDate(batch.getBatchID());
        } catch (SQLException e) {
            resultCollector.addFailure(
                    batch.getFullID(),
                    "metadata",
                    getClass().getName(),
                    "Could not connect to MFPak",
                    batch.getFullID());
            throw new RuntimeException(e);
        }
    }

    /**
     * Hooks on files ending in ".mix.xml"
     *
     * @param event
     */
    @Override
    public void handleAttribute(AttributeParsingEvent event) {
        if (event.getName()
                 .endsWith(".mix.xml")) {
            try {
                doValidate(event);
            } catch (SQLException e) {
                resultCollector.addFailure(
                        event.getName(),
                        "metadata",
                        getClass().getName(),
                        "Could not connect to MFPak",
                        event.getName());

            } catch (Exception e) {    //Fault Barrier
                addFailure(event.getName(), "Error processing MIX metadata.", getStackTrace(e));
            }
        }
    }

    /**
     * Validate the mix file
     *
     * @param event the attribute corresponding to the mix file
     *
     * @see #validateAgainstFilepath(org.w3c.dom.Document, dk.statsbiblioteket.util.xml.XPathSelector,
     *      dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent)
     * @see #validateScannedDate(org.w3c.dom.Document, dk.statsbiblioteket.util.xml.XPathSelector,
     *      dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent)
     */
    private void doValidate(AttributeParsingEvent event) throws SQLException {
        Document doc;
        try {
            doc = DOM.streamToDOM(event.getData(), true);
            if (doc == null) {
                addFailure(event.getName(), "Could not parse xml from " + event.getName(), event.getName());
                return;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        validateScannedDate(doc, XPATH, event);
        validateAgainstFilepath(doc, XPATH, event);

    }

    /** Checkst that  the scanned date is after the batch was sent from SB. */
    private void validateScannedDate(Document doc, XPathSelector xpath, AttributeParsingEvent event) throws
                                                                                                     SQLException {
        final String xpath2K1 = "/mix:mix/mix:ImageCaptureMetadata/mix:GeneralCaptureInformation/mix:dateTimeCreated";
        final String mixDateFormat = "yyyy-MM-dd'T'HH:mm:ss";
        final SimpleDateFormat formatter = new SimpleDateFormat(mixDateFormat);
        String scannedDateInMix = xpath.selectString(doc, xpath2K1);
        if (scannedDateInMix == null) {
            addFailure(
                    event.getName(),
                    "2K-1: Could not find scanned date in MIX file with XPath: '" + xpath2K1 + "'",
                    event.getName());
        }

        Date scannedDate = null;

        try {
            scannedDate = formatter.parse(scannedDateInMix);
        } catch (ParseException e) {
            addFailure(
                    event.getName(),
                    "2K-1: Could not parse the scanned date '"
                    + scannedDateInMix
                    + "' found in the MIX file."
                    + "Expected the form '"
                    + mixDateFormat
                    + "'.",
                    event.getName());
            return;
        }

        if (shipmentDate == null) {
            addFailure(batch.getFullID(), "2K-1: Shipment date for this batch not found in MFPak", batch.getBatchID());

        } else {

            if (scannedDate.before(shipmentDate)) {
                addFailure(
                        event.getName(),
                        "2K-1: The scanned '"
                        + scannedDate
                        + "' is before "
                        + "the batch was shipped from SB '"
                        + shipmentDate
                        + "'.",
                        event.getName());
            }
        }

    }

    /** Validate that the objectIdentifier is of the form [Film-id]-[Billed-id] */
    private void validateAgainstFilepath(Document doc, XPathSelector xpath, AttributeParsingEvent event) {
        final String xpath2K2 = "/mix:mix/mix:BasicDigitalObjectInformation/mix:ObjectIdentifier"
                                + "[mix:objectIdentifierType='Image Unique ID']/mix:objectIdentifierValue";

        final String xpath2K3 = "/mix:mix/mix:ImageCaptureMetadata/mix:SourceInformation/mix:SourceID"
                                + "[mix:sourceIDType='Microfilm reel barcode #']/mix:sourceIDValue";

        final String xpath2K4 = "/mix:mix/mix:ImageCaptureMetadata/mix:SourceInformation/mix:SourceID"
                                + "[mix:sourceIDType='Location on microfilm']/mix:sourceIDValue";

        String objectIdentifier = xpath.selectString(doc, xpath2K2);
        String mixFilmID = xpath.selectString(doc, xpath2K3);
        String mixBilledeID = xpath.selectString(doc, xpath2K4);
        String filmID = getFilmIDFromEvent(event);
        String billedID = getBilledIDFromEvent(event);
        String identifierFromPath = filmID + "-" + billedID;
        if (!objectIdentifier.equals(identifierFromPath)) {
            addFailure(
                    event.getName(),
                    "2K-2: ObjectIdentifier does not match the location in the tree. "
                    + "Expected '"
                    + objectIdentifier
                    + "' got '"
                    + identifierFromPath
                    + "'.",
                    event.getName());
        }

        if (!mixFilmID.equals(filmID)) {
            addFailure(
                    event.getName(),
                    "2K-3: FilmID does not match the location in the tree. "
                    + "Expected '"
                    + mixFilmID
                    + "' got '"
                    + filmID
                    + "'.",
                    event.getName());
        }

        if (!mixBilledeID.equals(billedID)) {
            addFailure(
                    event.getName(),
                    "2K-4: Location on film does not match the location in the tree. "
                    + "Expected '"
                    + mixBilledeID
                    + "' got '"
                    + billedID
                    + "'.",
                    event.getName());
        }


    }

    private String getBilledIDFromEvent(AttributeParsingEvent event) {
        String name = event.getName();
        String filename = name.substring(name.lastIndexOf("/") + 1, name.indexOf(".mix.xml"));
        if (filename.endsWith("brik") || filename.matches("^.*-ISO-[0-9]+$")) {
            String[] splits = filename.split("-");
            return splits[splits.length-2]+"-"+splits[splits.length-1];
        } else {
            return filename.substring(
                    filename.lastIndexOf("-") + 1);
        }

    }

    private String getFilmIDFromEvent(AttributeParsingEvent event) {
        String filmID = null;
        if (event.getName()
                 .contains("WORKSHIFT-ISO-TARGET")) {
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

    private void addFailure(String reference, String description, String details) {
        resultCollector.addFailure(reference, "metadata", getClass().getName(), description, details);
    }

}
