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
 *  Class to handle MIX crosschecks
 */
public class MixXPathEventHandler extends DefaultTreeEventHandler {

    private ResultCollector resultCollector;
    private MfPakDAO mfPakDAO;
    private Batch batch;

    /**
     * Constructor for this class.
     * @param resultCollector the result collector to collect errors in
     * @param mfPakDAO a DAO object from which one can read relevant external properties of a batch.
     * @param batch a batch object representing the batch being analysed.
     */
    public MixXPathEventHandler(ResultCollector resultCollector, MfPakDAO mfPakDAO, Batch batch) {
        this.resultCollector = resultCollector;
        this.mfPakDAO = mfPakDAO;
        this.batch = batch;
    }

    @Override
    public void handleAttribute(AttributeParsingEvent event) {
        if (event.getName().endsWith(".mix.xml")) {
            try {
                doValidate(event);
            } catch (Exception e) {    //Fault Barrier
                addFailure(event.getName(), "Error processing MIX metadata.", getStackTrace(e));
            }
        }
    }

    private void doValidate(AttributeParsingEvent event) {
        XPathSelector xpath = DOM.createXPathSelector("mix", "http://www.loc.gov/mix/v20");
        Document doc;
        try {
            doc = DOM.streamToDOM(event.getData(), true);
            if (doc == null) {
                addFailure(event.getName(), 
                        "Could not parse xml from " + event.getName(),
                        event.getName());
                return;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        Date shipmentDate = null;
        try {
            shipmentDate = mfPakDAO.getBatchShipmentDate(batch.getBatchID());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        final String xpath2K1 = "/mix:mix/mix:ImageCaptureMetadata/mix:GeneralCaptureInformation/mix:dateTimeCreated";
        final String mixDateFormat = "yyyy-MM-dd'T'HH:mm:ss";
        final SimpleDateFormat formatter = new SimpleDateFormat(mixDateFormat);
        String scannedDateInMix = xpath.selectString(doc, xpath2K1);
        if(scannedDateInMix == null) {
            addFailure(event.getName(),
                    "2K-1: Could not find scanned date in MIX file with XPath: '" + xpath2K1 + "'",
                    event.getName());
        }
        
        Date scannedDate = null;
        
        try {
            scannedDate = formatter.parse(scannedDateInMix);
        } catch (ParseException e) {
            addFailure(event.getName(),
                    "2K-1: Could not parse the scanned date '" + scannedDateInMix + "' found in the MIX file."
                            + "Expected the form '" + mixDateFormat + "'.",
                    event.getName());
        }
        
        if(scannedDate.before(shipmentDate)) {
            addFailure(event.getName(),
                    "2K-1: The scanned '" + scannedDate + "' is before "
                            + "the batch was shipped from SB '" + shipmentDate + "'.",
                    event.getName());
        }

    }
    
    private void addFailure(String reference, String description, String details) {
        resultCollector.addFailure(reference, "metadata", getClass().getName(), description, details);
    }

}
