package dk.statsbiblioteket.newspaper.metadatachecker.mix;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.newspaper.metadatachecker.checker.FailureType;
import dk.statsbiblioteket.newspaper.metadatachecker.checker.XmlAttributeChecker;
import org.w3c.dom.Document;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This checker verifies that the dateTimeCreated in the mix file is after the batch was actually shipped from SB
 */
public class MixShipmentDateChecker extends XmlAttributeChecker{
    private Date shipmentDate;

    public MixShipmentDateChecker(ResultCollector resultCollector, Date shipmentDate) {
        super(resultCollector, FailureType.METADATA);
        this.shipmentDate = shipmentDate;
    }

    @Override
    public void validate(AttributeParsingEvent event, Document doc) {
        final String xpath2K1 = "/mix:mix/mix:ImageCaptureMetadata/mix:GeneralCaptureInformation/mix:dateTimeCreated";
        final String mixDateFormat = "yyyy-MM-dd'T'HH:mm:ss";
        final SimpleDateFormat formatter = new SimpleDateFormat(mixDateFormat);
        String scannedDateInMix = XPATH.selectString(doc, xpath2K1);
        if (scannedDateInMix == null) {
            addFailure(
                    event, "2K-15: Could not find scanned date in MIX file with XPath: '" + xpath2K1 + "'");
        }

        Date scannedDate;

        try {
            scannedDate = formatter.parse(scannedDateInMix);
        } catch (ParseException e) {
            addFailure(
                    event,
                    "2K-15: Could not parse the scanned date '" + scannedDateInMix + "' found in the MIX file." + "Expected the form '" + mixDateFormat + "'.");
            return;
        }

        if (shipmentDate == null) {
            addFailure(event, "2K-15: Shipment date for this batch not found in MFPak");

        } else {

            if (scannedDate.before(shipmentDate)) {
                addFailure(
                        event,
                        "2K-15: The scanned '" + scannedDate + "' is before " + "the batch was shipped from SB '" + shipmentDate + "'.");
            }
        }

    }
    @Override
    public boolean shouldCheckEvent(AttributeParsingEvent event) {
        return event.getName()
                    .endsWith(".mix.xml");
    }


}
