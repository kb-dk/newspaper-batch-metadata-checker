package dk.statsbiblioteket.newspaper.metadatachecker;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

public class AltoMixCrossCheckEventHandlerTest {

    @Test
    public void goodTest() {
        ResultCollector resultCollector = new ResultCollector("foo", "bar");

        AttributeParsingEvent eventMix = new AttributeParsingEvent(
                "goodData/good.mix.xml") {
            @Override
            public InputStream getData() throws IOException {
                return Thread.currentThread()
                             .getContextClassLoader()
                             .getResourceAsStream(getName());
            }

            @Override
            public String getChecksum() throws IOException {
                return null;
            }
        };
        AttributeParsingEvent eventAlto = new AttributeParsingEvent(
                "goodData/good.alto.xml") {
            @Override
            public InputStream getData() throws IOException {
                return Thread.currentThread()
                             .getContextClassLoader()
                             .getResourceAsStream(getName());
            }

            @Override
            public String getChecksum() throws IOException {
                return null;
            }
        };
        AltoMixCrossCheckEventHandler handler = new AltoMixCrossCheckEventHandler(resultCollector);
        handler.handleNodeBegin(new NodeBeginsParsingEvent("good"));
        handler.handleAttribute(eventMix);
        handler.handleAttribute(eventAlto);
        handler.handleNodeEnd(new NodeEndParsingEvent("good"));


        assertTrue(resultCollector.isSuccess());


    }

    @Test
    public void badTest() {
        ResultCollector resultCollector = new ResultCollector("foo", "bar");

        AttributeParsingEvent eventMix = new AttributeParsingEvent(
                "badData/badAltoMix.mix.xml") {
            @Override
            public InputStream getData() throws IOException {
                return Thread.currentThread()
                             .getContextClassLoader()
                             .getResourceAsStream(getName());
            }

            @Override
            public String getChecksum() throws IOException {
                return null;
            }
        };
        AttributeParsingEvent eventAlto = new AttributeParsingEvent(
                "badData/badAltoMix.alto.xml") {
            @Override
            public InputStream getData() throws IOException {
                return Thread.currentThread()
                             .getContextClassLoader()
                             .getResourceAsStream(getName());
            }

            @Override
            public String getChecksum() throws IOException {
                return null;
            }
        };
        AltoMixCrossCheckEventHandler handler = new AltoMixCrossCheckEventHandler(resultCollector);
        handler.handleNodeBegin(new NodeBeginsParsingEvent("bad"));
        handler.handleAttribute(eventMix);
        handler.handleAttribute(eventAlto);
        handler.handleNodeEnd(new NodeEndParsingEvent("bad"));


        assertFalse(resultCollector.isSuccess());



    }


}
