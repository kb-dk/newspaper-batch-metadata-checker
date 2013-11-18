package dk.statsbiblioteket.newspaper.metadatachecker;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

import static org.testng.Assert.assertTrue;

public class MixXPathEventHandlerTest {


    /** Test that we can validate a valid edition xml (mods) file. */
      @Test
      public void testEditionModsGood() throws SQLException {
          ResultCollector resultCollector = new ResultCollector("foo", "bar");
          SchematronValidatorEventHandler handler = new SchematronValidatorEventHandler(resultCollector, null);
          TreeEventHandler mixXPathEventHandler = new MixXPathEventHandler(resultCollector, MFPakMocker.getMFPak(), getBatch());
          AttributeParsingEvent editionEvent = new AttributeParsingEvent(
                  "B400022028241-RT1/400022028241-1/1795-06-13-01/adresseavisen1759-1795-06-13-01-0006.mix.xml") {
              @Override
              public InputStream getData() throws IOException {
                  return Thread.currentThread()
                               .getContextClassLoader()
                               .getResourceAsStream(
                                       "goodData/good.mix.xml");
              }

              @Override
              public String getChecksum() throws IOException {
                  return null;
              }
          };
          handler.handleAttribute(editionEvent);
          mixXPathEventHandler.handleAttribute(editionEvent);

          if ( ! resultCollector.isSuccess()){
              System.out
                    .println(resultCollector.toReport());
          }
          assertTrue(resultCollector.isSuccess());
      }



    private Batch getBatch() {
        return new Batch("400022028241");
    }



}
