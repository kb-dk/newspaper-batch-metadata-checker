package dk.statsbiblioteket.newspaper.metadatachecker;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.DataFileNodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.DataFileNodeEndsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.DefaultTreeEventHandler;
import dk.statsbiblioteket.newspaper.metadatachecker.caches.DocumentCache;
import dk.statsbiblioteket.util.Strings;
import dk.statsbiblioteket.util.xml.DOM;
import dk.statsbiblioteket.util.xml.XPathSelector;
import org.w3c.dom.Document;

import java.io.IOException;

import static dk.statsbiblioteket.util.Strings.getStackTrace;

/** This class tests that the mix and alto files agree on the image size */
public class AltoMixCrossCheckEventHandler extends DefaultTreeEventHandler {

    private final ResultCollector resultCollector;
    private final XPathSelector xpath;


    private static final ThreadLocal<SizeSet> sizes = new ThreadLocal<>();
    private final DocumentCache documentCache;

    public AltoMixCrossCheckEventHandler(ResultCollector resultCollector, DocumentCache documentCache) {
        this.resultCollector = resultCollector;
        this.documentCache = documentCache;

        xpath = DOM.createXPathSelector(
                "a", "http://www.loc.gov/standards/alto/ns-v2#", "mix", "http://www.loc.gov/mix/v20");

    }

    /**
     * Push a sizes object onto the stack
     *
     * @param event
     */
    @Override
    public  void handleNodeBegin(NodeBeginsParsingEvent event) {
        if (!(event instanceof DataFileNodeBeginsParsingEvent)) {
            sizes.set(new SizeSet(event.getName()));
        }
    }

    /**
     * Pop the stack and verify that the sizes match for that folder
     *
     * @param event
     */
    @Override
    public  void handleNodeEnd(NodeEndParsingEvent event) {
        if (!(event instanceof DataFileNodeEndsParsingEvent)) {
            verify(sizes.get(), resultCollector);
        }
    }


    /**
     * Compare the alto sizes and the mix sizes, if both are > 0. If less than 0, we assume that they have not been
     * set (ie. there was no alto file or something)
     *
     * @param sizes           the store of sizes
     * @param resultCollector the result collector
     */
    private void verify(SizeSet sizes, ResultCollector resultCollector) {
        if (!sizes.getMixSize()
                  .equalWidth(sizes.getAltoSize())) {
            resultCollector.addFailure(
                    sizes.getFolder(),
                    "metadata",
                    getClass().getSimpleName(),
                    "2J-7: The file '" + sizes.getFolder() + ".mix.xml' and file '" + sizes.getFolder() + ".alto.xml' should agree on height");

        }
        if (!sizes.getMixSize()
                  .equalHeight(sizes.getAltoSize())) {
            resultCollector.addFailure(
                    sizes.getFolder(),
                    "metadata",
                    getClass().getSimpleName(),
                    "2J-7: The file '" + sizes.getFolder() + ".mix.xml' and file '" + sizes.getFolder() + ".alto.xml' should agree on width");
        }
    }

    @Override
    public  void handleAttribute(AttributeParsingEvent event) {

        try {
            if (event.getName()
                     .endsWith(".alto.xml")) {

                extractAltoSizes(documentCache.getDocument(event), sizes.get().getAltoSize(), event.getName());

            } else if (event.getName()
                            .endsWith(".mix.xml")) {
                extractMixSizes(documentCache.getDocument(event), sizes.get().getMixSize(), event.getName());
            }


        } catch (IOException e) {
            resultCollector.addFailure(
                    event.getName(), "exception", getClass().getSimpleName(), "Error processing metadata: " + e.toString(),
                    getStackTrace(e));

        } catch (NumberFormatException e) {
            resultCollector.addFailure(
                    event.getName(),
                    "metadata",
                    getClass().getSimpleName(),
                    "2J-7 / 2K-9 - 2K-10: Failed to parse integer: " + e.toString(),
                    Strings.getStackTrace(e));
        }

    }

    private void extractMixSizes(Document data, Size size, String name) {
        Integer xResolution = xpath.selectInteger(
                data, "/mix:mix/mix:ImageAssessmentMetadata/mix:SpatialMetrics/mix:xSamplingFrequency/mix:numerator");
        Integer width = xpath.selectInteger(
                data, "/mix:mix/mix:BasicImageInformation/mix:BasicImageCharacteristics/mix:imageWidth");
        if (width == null) {
            resultCollector.addFailure(
                    name, "metadata", getClass().getSimpleName(), "2K-9: Failed to read page width from mix file");
        } else {
            size.setWidth(width * 1200 / xResolution);
        }

        Integer yResolution = xpath.selectInteger(
                data, "/mix:mix/mix:ImageAssessmentMetadata/mix:SpatialMetrics/mix:ySamplingFrequency/mix:numerator");

        Integer height = xpath.selectInteger(
                data, "/mix:mix/mix:BasicImageInformation/mix:BasicImageCharacteristics/mix:imageHeight");
        if (height == null) {
            resultCollector.addFailure(
                    name, "metadata", getClass().getSimpleName(), "2K-10: Failed to read page height from mix file");
        } else {

            size.setHeight(height * 1200 / yResolution);
        }

    }

    private void extractAltoSizes(Document data, Size size, String reference) {

        Integer height = xpath.selectInteger(data, "/a:alto/a:Layout/a:Page/@HEIGHT");
        if (height == null) {
            resultCollector.addFailure(
                    reference, "metadata", getClass().getSimpleName(), "2J-7: Failed to read page height from alto");

        } else {
            size.setHeight(height);
        }
        Integer width = xpath.selectInteger(data, "/a:alto/a:Layout/a:Page/@WIDTH");
        if (width == null) {
            resultCollector.addFailure(
                    reference, "metadata", getClass().getSimpleName(), "2J-7: Failed to read page width from alto");
        } else {

            size.setWidth(width);
        }
    }

    private class Size {

        private int width = -1;
        private int height = -1;

        public boolean equalHeight(Size that) {
            if (this.getHeight() > 0 && that.getHeight() > 0) {
                return this.getHeight() == that.getHeight();
            } else {
                return true;
            }
        }

        public boolean equalWidth(Size that) {
            if (this.getWidth() > 0 && that.getWidth() > 0) {
                return this.getWidth() == that.getWidth();
            } else {
                return true;
            }
        }

        private int getWidth() {
            return width;
        }

        private void setWidth(int width) {
            this.width = width;
        }

        private int getHeight() {
            return height;
        }

        private void setHeight(int height) {
            this.height = height;
        }

    }

    private class SizeSet {

        private Size altoSize;
        private Size mixSize;
        private Size jpylyzerSize;
        private String folder;

        private SizeSet(String folder) {
            this.altoSize = new Size();
            this.mixSize = new Size();
            this.folder = folder;
        }

        private Size getAltoSize() {
            return altoSize;
        }

        private Size getMixSize() {
            return mixSize;
        }

        private String getFolder() {
            return folder;
        }

        private Size getJpylyzerSize() {
            return jpylyzerSize;
        }
    }
}