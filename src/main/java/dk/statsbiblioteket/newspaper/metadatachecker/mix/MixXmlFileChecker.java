package dk.statsbiblioteket.newspaper.metadatachecker.mix;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.w3c.dom.Document;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.newspaper.mfpakintegration.batchcontext.BatchContext;
import dk.statsbiblioteket.newspaper.metadatachecker.checker.XmlAttributeChecker;
import dk.statsbiblioteket.newspaper.metadatachecker.checker.XmlFileChecker;

public class MixXmlFileChecker extends XmlFileChecker {
    private final Document batchXmlStructure;
    private final Date shipmentDate;


    public MixXmlFileChecker(ResultCollector resultCollector, BatchContext context, Document batchXmlStructure) {
        super(resultCollector);
        this.batchXmlStructure = batchXmlStructure;
        this.shipmentDate = context.getShipmentDate();
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
