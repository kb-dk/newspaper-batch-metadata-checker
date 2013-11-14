package dk.statsbiblioteket.newspaper.metadatachecker;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.DefaultTreeEventHandler;
import dk.statsbiblioteket.util.xml.DOM;
import dk.statsbiblioteket.util.xml.XPathSelector;
import org.w3c.dom.Document;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;

import static dk.statsbiblioteket.util.Strings.getStackTrace;

/** This class tests that the mix and alto files agree on the image size */
public class AltoMixCrossCheckEventHandler extends DefaultTreeEventHandler {


    private final ResultCollector resultCollector;
    private XPathSelector xpath;
    private ArrayDeque<Sizes> sizesStack = new ArrayDeque<>();

    public AltoMixCrossCheckEventHandler(ResultCollector resultCollector) {
        this.resultCollector = resultCollector;

        xpath = DOM.createXPathSelector(
                "a", "http://www.loc.gov/standards/alto/ns-v2#", "mix", "http://www.loc.gov/mix/v20");

    }

    /**
     * Push a sizes object onto the stack
     *
     * @param event
     */
    @Override
    public void handleNodeBegin(NodeBeginsParsingEvent event) {
        sizesStack.push(new Sizes(event.getName()));
    }

    /**
     * Pop the stack and verify that the sizes match for that folder
     *
     * @param event
     */
    @Override
    public void handleNodeEnd(NodeEndParsingEvent event) {
        Sizes sizes = sizesStack.pop();
        verify(sizes, resultCollector);
    }

    /**
     * Compare the alto sizes and the mix sizes, if both are > 0. If less than 0, we assume that they have not been
     * set (ie. there was no alto file or something)
     *
     * @param sizes           the store of sizes
     * @param resultCollector the result collector
     */
    private void verify(Sizes sizes, ResultCollector resultCollector) {
        if (sizes.getAltoHeight() > 0 && sizes.getMixHeight() > 0) {
            if (sizes.getAltoHeight() != sizes.getMixHeight()) {
                resultCollector.addFailure(
                        sizes.getFolder(),
                        "metadata",
                        getClass().getName(),
                        "2J-7: The file '"
                        + sizes.getFolder()
                        + ".mix.xml' and file '"
                        + sizes.getFolder()
                        + ".alto.xml' should agree on height");
            }
        }
        if (sizes.getAltoWidth() > 0 && sizes.getMixWidth() > 0) {
            if (sizes.getAltoWidth() != sizes.getMixWidth()) {
                resultCollector.addFailure(
                        sizes.getFolder(),
                        "metadata",
                        getClass().getName(),
                        "2J-7: The file '"
                        + sizes.getFolder()
                        + ".mix.xml' and file '"
                        + sizes.getFolder()
                        + ".alto.xml' should agree on width");
            }
        }

    }

    @Override
    public void handleAttribute(AttributeParsingEvent event) {

        Sizes sizes = sizesStack.peek();
        try {
            if (event.getName()
                     .endsWith(".alto.xml")) {

                extractAltoSizes(asDom(event.getData()), sizes, event.getName());

            } else if (event.getName()
                            .endsWith(".mix.xml")) {
                extractMixSizes(asDom(event.getData()), sizes, event.getName());

            }
        } catch (IOException e) {
            resultCollector.addFailure(
                    event.getName(), "metadata", getClass().getName(), "Error processing metadata.", getStackTrace(e));
        }
    }

    private Document asDom(InputStream data) {
        return DOM.streamToDOM(data, true);
    }

    private void extractMixSizes(Document data, Sizes sizes, String name) {
        Integer width = xpath.selectInteger(
                data, "/mix:mix/mix:BasicImageInformation/mix:BasicImageCharacteristics/mix:imageWidth");
        if (width == null) {
            resultCollector.addFailure(
                    name, "metadata", getClass().getName(), "Failed to read page width from mix file");
        } else {
            sizes.setMixWidth(width);
        }

        Integer height = xpath.selectInteger(
                data, "/mix:mix/mix:BasicImageInformation/mix:BasicImageCharacteristics/mix:imageHeight");
        if (height == null) {
            resultCollector.addFailure(
                    name, "metadata", getClass().getName(), "Failed to read page height from mix file");
        } else {

            sizes.setMixHeight(height);
        }
    }

    private void extractAltoSizes(Document data, Sizes sizes, String reference) {

        Integer height = xpath.selectInteger(data, "/a:alto/a:Layout/a:Page/@HEIGHT");
        if (height == null) {
            resultCollector.addFailure(
                    reference, "metadata", getClass().getName(), "Failed to read page height from alto");

        } else {
            sizes.setAltoHeight(height);
        }
        Integer width = xpath.selectInteger(data, "/a:alto/a:Layout/a:Page/@WIDTH");
        if (width == null) {
            resultCollector.addFailure(
                    reference, "metadata", getClass().getName(), "Failed to read page width from alto");
        } else {

            sizes.setAltoWidth(width);
        }
    }

    private class Sizes {

        private int altoWidth;
        private int altoHeight;
        private int mixWidth;
        private int mixHeight;
        private String folder;

        private Sizes(String folder) {
            this.folder = folder;
        }

        private void reset() {
            altoWidth = -1;
            altoHeight = -1;
            mixWidth = -1;
            mixHeight = -1;
        }

        private int getAltoWidth() {
            return altoWidth;
        }

        private void setAltoWidth(int altoWidth) {
            this.altoWidth = altoWidth;
        }

        private int getAltoHeight() {
            return altoHeight;
        }

        private void setAltoHeight(int altoHeight) {
            this.altoHeight = altoHeight;
        }

        private int getMixWidth() {
            return mixWidth;
        }

        private void setMixWidth(int mixWidth) {
            this.mixWidth = mixWidth;
        }

        private int getMixHeight() {
            return mixHeight;
        }

        private void setMixHeight(int mixHeight) {
            this.mixHeight = mixHeight;
        }

        private String getFolder() {
            return folder;
        }
    }
}
