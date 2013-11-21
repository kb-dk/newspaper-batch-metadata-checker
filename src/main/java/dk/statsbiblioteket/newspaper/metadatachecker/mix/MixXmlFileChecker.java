package dk.statsbiblioteket.newspaper.metadatachecker.mix;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.newspaper.metadatachecker.checker.XmlAttributeChecker;
import dk.statsbiblioteket.newspaper.metadatachecker.checker.XmlFileChecker;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.MfPakDAO;
import dk.statsbiblioteket.util.Strings;

import org.w3c.dom.Document;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MixXmlFileChecker extends XmlFileChecker {
    private final Document batchXmlStructure;
    private final Date shipmentDate;


    public MixXmlFileChecker(ResultCollector resultCollector, MfPakDAO mfPakDAO, Batch batch,
                             Document batchXmlStructure) {
        super(resultCollector);
        this.batchXmlStructure = batchXmlStructure;


        try {
            shipmentDate = mfPakDAO.getBatchShipmentDate(batch.getBatchID());
        } catch (SQLException e) {
            resultCollector.addFailure(
                    batch.getFullID(),
                    "exception",
                    getClass().getSimpleName(),
                    "Could not connect to MFPak: " + e.toString(),
                    Strings.getStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    @Override
    protected List<XmlAttributeChecker> createCheckers() {
        return Arrays.asList(
                new MixFilePathChecker(resultCollector),
                new MixShipmentDateChecker(resultCollector, shipmentDate),
                new MixChecksumChecker(resultCollector, batchXmlStructure),
                new MixJpylyzerEventHandler(resultCollector));
    }

}
