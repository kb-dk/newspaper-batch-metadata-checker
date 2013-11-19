package dk.statsbiblioteket.newspaper.metadatachecker;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.DefaultTreeEventHandler;
import dk.statsbiblioteket.util.xml.DOM;
import dk.statsbiblioteket.util.xml.XPathSelector;
import org.w3c.dom.Document;

import java.io.IOException;
import java.sql.SQLException;

import static dk.statsbiblioteket.util.Strings.getStackTrace;

public class MixJpylyzerEventHandler extends DefaultTreeEventHandler {

    private static final XPathSelector XPATH = DOM.createXPathSelector("mix", "http://www.loc.gov/mix/v20");
    private ResultCollector resultCollector;
    private Batch batch;
    private Integer mixFileSize = null;


    /**
     * Constructor for this class.
     *
     * @param resultCollector the result collector to collect errors in
     * @param batch           a batch object representing the batch being analysed.
     */
    public MixJpylyzerEventHandler(ResultCollector resultCollector, Batch batch) {
        this.resultCollector = resultCollector;
        this.batch = batch;


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
        } else if (event.getName()
                        .endsWith(".jpylyzer.xml")) {
            try {
                validateJP2FileSize(event);
            } catch (Exception e) {

            }
        }
    }

    private void validateJP2FileSize(AttributeParsingEvent event) {
        String jpylyzerFileSizeXpath = "/jpylyzer/fileInfo/fileSizeInBytes";

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
        Integer jpylyzerFileSize = XPATH.selectInteger(doc, jpylyzerFileSizeXpath);
        if (!jpylyzerFileSize.equals(mixFileSize)) {
            addFailure(
                    event.getName(),
                    "The file size from jpylyzer does not match what is reported in the mix file",
                    null);
        }

        mixFileSize = null;


    }

    /**
     * Validate the mix file
     *
     * @param event the attribute corresponding to the mix file
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


        validateAgainstJpylyzer(doc, XPATH, event);


    }

    private void validateAgainstJpylyzer(Document doc, XPathSelector xpath, AttributeParsingEvent event) {

        final String mixFileSizeXpath = "/mix:mix/mix:BasicDigitalObjectInformation/mix:fileSize";

        if (mixFileSize != null) {
            addFailure(event.getName(), "We found this two mix files without a jpylyzer file", null);
        }
        this.mixFileSize = xpath.selectInteger(doc, mixFileSizeXpath);


        /*
        File size
        integer
        53153644
        Size of the file in bytes
        NR
        M
        mix:mix/mix:BasicDigitalObjectInformation/mix:fileSize
        J – skal sammenlignes med det der ligger i filsystemet
        – ikke relevant




mix:mix/mix:BasicImageInformation/mix:BasicImageCharacteristics/mix:imageWidth
J – skal sammenlignes med en karakterisering af filen

mix:mix/mix:BasicImageInformation/mix:BasicImageCharacteristics/mix:imageHeigh
J – skal sammenlignes med en karakterisering af filen


         */


    }

    private void addFailure(String reference, String description, String details) {
        resultCollector.addFailure(reference, "metadata", getClass().getName(), description, details);
    }

}
