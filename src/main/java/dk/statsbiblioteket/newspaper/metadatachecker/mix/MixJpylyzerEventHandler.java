package dk.statsbiblioteket.newspaper.metadatachecker.mix;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.newspaper.metadatachecker.checker.FailureType;
import dk.statsbiblioteket.newspaper.metadatachecker.checker.XmlAttributeChecker;
import org.w3c.dom.Document;

/**
 * This class validates the mix information vs. the jpylyzer information
 */
public class MixJpylyzerEventHandler extends XmlAttributeChecker {
    private static final ThreadLocal<Integer> mixFileSize = new ThreadLocal<Integer>();
    private static final ThreadLocal<Integer> mixWidth = new ThreadLocal<Integer>();
    private static final ThreadLocal<Integer> mixHeight = new ThreadLocal<Integer>();
    private static final ThreadLocal<AttributeParsingEvent> foundMix = new ThreadLocal<AttributeParsingEvent>();


    /**
     * Constructor for this class.
     *
     * @param resultCollector   the result collector to collect errors in
     *
     *
     */
    public MixJpylyzerEventHandler(ResultCollector resultCollector) {
        super(resultCollector, FailureType.METADATA);
    }

    protected void handleJpylyzer(AttributeParsingEvent event, Document doc) {


        String jpylyzerFileSizeXpath = "/jpylyzer/fileInfo/fileSizeInBytes";
        Integer jpylyzerFileSize = XPATH.selectInteger(doc, jpylyzerFileSizeXpath);
        if (!jpylyzerFileSize.equals(mixFileSize.get())) {
            addFailure(
                    event,
                    "2K-2: The file size from jpylyzer does not match what is reported in the mix file"
                    );
        }

        String jpylyzerHeightXpath = "/jpylyzer/properties/jp2HeaderBox/imageHeaderBox/height";
        Integer jpylyzerHeight = XPATH.selectInteger(doc, jpylyzerHeightXpath);
        if (!jpylyzerHeight.equals(mixHeight.get())) {
            addFailure(
                    event,
                    "2K-10: The picture height from jpylyzer does not match what is reported in the mix file"
                    );
        }

        String jpylyzerWidthXpath = "/jpylyzer/properties/jp2HeaderBox/imageHeaderBox/width";
        Integer jpylyzerWidth = XPATH.selectInteger(doc, jpylyzerWidthXpath);
        if (!jpylyzerWidth.equals(mixWidth.get())) {
            addFailure(
                    event,
                    "2K-9: The picture width from jpylyzer does not match what is reported in the mix file"
                    );
        }
        foundMix.set(null);


    }

    protected void handleMix(AttributeParsingEvent event, Document doc) {
        if (foundMix.get() != null) {
            addFailure(foundMix.get(), "2K: No corresponding jpylyzer analysis found");
        }

        foundMix.set(event);

        final String mixFileSizeXpath = "/mix:mix/mix:BasicDigitalObjectInformation/mix:fileSize";
        this.mixFileSize.set(XPATH.selectInteger(doc, mixFileSizeXpath));


        String mixWidthXpath = "/mix:mix/mix:BasicImageInformation/mix:BasicImageCharacteristics/mix:imageWidth";
        this.mixWidth.set(XPATH.selectInteger(doc, mixWidthXpath));

        String mixHeightXpath = "/mix:mix/mix:BasicImageInformation/mix:BasicImageCharacteristics/mix:imageHeight";
        this.mixHeight.set(XPATH.selectInteger(doc, mixHeightXpath));
    }

    @Override
    public void validate(AttributeParsingEvent event, Document doc) {
        if (isMix(event)) {
            handleMix(event, doc);
        }
        if (isJpylyzer(event)) {
            handleJpylyzer(event, doc);
        }
    }

    private boolean isJpylyzer(AttributeParsingEvent event) {
        return event.getName()
                    .endsWith(".jpylyzer.xml");
    }

    private boolean isMix(AttributeParsingEvent event) {
        return event.getName()
                    .endsWith(".mix.xml");
    }

    @Override
    public boolean shouldCheckEvent(AttributeParsingEvent event) {
        return isMix(event) || isJpylyzer(event);
    }
}
