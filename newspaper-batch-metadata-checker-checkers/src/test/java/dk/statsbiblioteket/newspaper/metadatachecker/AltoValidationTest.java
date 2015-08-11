package dk.statsbiblioteket.newspaper.metadatachecker;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.newspaper.metadatachecker.caches.DocumentCache;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.MfPakDAO;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertFalse;

public class AltoValidationTest {

    Map<String, AttributeSpec> attributeConfigs;
    private ResultCollector resultCollector = null;

    @BeforeMethod
    public void initialise() {
        resultCollector = new ResultCollector("test", "test");
        attributeConfigs = new HashMap<>();
        attributeConfigs.put(".alto.xml",new AttributeSpec(".alto.xml", "alto-v2.0.xsd", "alto.sch","2J: ","metadata"));


    }

    /**
     * Test success for 2J3 - filepath in alto file matches actual file path
     */
    @Test
    public void testGood2J3() {
        DocumentCache documentCache = new DocumentCache();
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        Batch batch = new Batch();
        batch.setBatchID("400022028241");
        batch.setRoundTripNumber(10);
        MfPakDAO dao = mock(MfPakDAO.class);
        AltoXPathEventHandler handler = new AltoXPathEventHandler(resultCollector, documentCache);
        AttributeParsingEvent altoEvent = new AttributeParsingEvent("B400022028241-RT2/400022028241-14/1795-06-15-01/AdresseContoirsEfterretninger-1795-06-15-01-0012B.alto.xml") {
            @Override
            public InputStream getData() throws IOException {
                return Thread.currentThread().getContextClassLoader().getResourceAsStream("goodData/good.alto.xml");

            }

            @Override
            public String getChecksum() throws IOException {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        } ;
        handler.handleAttribute(altoEvent);
        assertTrue(resultCollector.isSuccess(), "Unexpected failure: " + resultCollector.toReport());
    }

    /**
     * Test failure for 2J3 - filepath in alto file matches actual file path
     */
    @Test
    public void testBad2J3() {
        DocumentCache documentCache = new DocumentCache();
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        Batch batch = new Batch();
        batch.setBatchID("400022028241");
        batch.setRoundTripNumber(10);
        MfPakDAO dao = mock(MfPakDAO.class);
        AltoXPathEventHandler handler = new AltoXPathEventHandler(resultCollector, documentCache);
        AttributeParsingEvent altoEvent = new AttributeParsingEvent("B400022028241-RT2/400022028241-14/1795-06-15-01/AdresseContoirsEfterretninger-1795-06-15-01-0012B.alto.xml") {
            @Override
            public InputStream getData() throws IOException {
                return Thread.currentThread().getContextClassLoader().getResourceAsStream("badData/bad1.alto.xml");
            }

            @Override
            public String getChecksum() throws IOException {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        } ;
        handler.handleAttribute(altoEvent);
        assertFalse("Unexpected success: " + resultCollector.toReport(), resultCollector.isSuccess());
        assertTrue(resultCollector.toReport().contains("2J-3"));
    }

    /**
     * Tests success for 2J16 - nested textblocks with the same language.
     */
    @Test
    public void shouldSucceed2J16() {
        DocumentCache documentCache = new DocumentCache();
        initialise();
        SchematronValidatorEventHandler handler = new SchematronValidatorEventHandler(resultCollector, documentCache,attributeConfigs);
        AttributeParsingEvent event = new AttributeParsingEvent("B400022028241-RT1/400022028241-14/1795-06-01/AdresseContoirsEfterretninger-1795-06-01-0006.alto.xml") {
            @Override
            public InputStream getData() throws IOException {
                return Thread.currentThread().getContextClassLoader().getResourceAsStream("goodData/good.alto.xml");
            }

            @Override
            public String getChecksum() throws IOException {
                return null;
            }
        };
        handler.handleAttribute(event);
        assertTrue(resultCollector.isSuccess());
    }

    /**
     * Tests failure for 2J16 - nested textblocks with different languages
     */
    @Test
    public void shouldFail2J16() {
        DocumentCache documentCache = new DocumentCache();
        initialise();
        SchematronValidatorEventHandler handler = new SchematronValidatorEventHandler(resultCollector, documentCache,attributeConfigs);
        AttributeParsingEvent event = new AttributeParsingEvent("B400022028241-RT1/400022028241-14/1795-06-01/AdresseContoirsEfterretninger-1795-06-01-0006.alto.xml") {
            @Override
            public InputStream getData() throws IOException {
                return Thread.currentThread().getContextClassLoader().getResourceAsStream("badData/bad1.alto.xml");
            }

            @Override
            public String getChecksum() throws IOException {
                return null;
            }
        };
        handler.handleAttribute(event);
        assertFalse(resultCollector.isSuccess());
        assertTrue(resultCollector.toReport().contains("2J-16"));
        assertTrue(resultCollector.toReport().contains("klingon"));
    }

    /**
      * Tests failure for 2J16 - special test that it all still works when we are nested two levels deep.
      */
     @Test
     public void shouldFail2J16DoubleNested() {
         DocumentCache documentCache = new DocumentCache();
         initialise();
         SchematronValidatorEventHandler handler = new SchematronValidatorEventHandler(resultCollector, documentCache,attributeConfigs);
         AttributeParsingEvent event = new AttributeParsingEvent("B400022028241-RT1/400022028241-14/1795-06-01/AdresseContoirsEfterretninger-1795-06-01-0006.alto.xml") {
             @Override
             public InputStream getData() throws IOException {
                 return Thread.currentThread().getContextClassLoader().getResourceAsStream("badData/bad2.alto.xml");
             }

             @Override
             public String getChecksum() throws IOException {
                 return null;
             }
         };
         handler.handleAttribute(event);
         assertFalse(resultCollector.isSuccess());
         assertTrue(resultCollector.toReport().contains("2J-16"));
         assertTrue(resultCollector.toReport().contains("klingon"));
         assertTrue(resultCollector.toReport().contains("worf"));
     }

    @Test
    public void shouldSucceed() {
        DocumentCache documentCache = new DocumentCache();
        initialise();
        SchematronValidatorEventHandler handler = new SchematronValidatorEventHandler(resultCollector, documentCache,attributeConfigs);
        AttributeParsingEvent event = new AttributeParsingEvent("B400022028241-RT1/400022028241-14/1795-06-01/AdresseContoirsEfterretninger-1795-06-01-0006.alto.xml") {
            @Override
            public InputStream getData() throws IOException {
                return Thread.currentThread().getContextClassLoader().getResourceAsStream(
                        "AdresseContoirsEfterretninger-1795-06-01-0006.alto.xml");
            }

            @Override
            public String getChecksum() throws IOException {
                return null;
            }
        };
        handler.handleAttribute(event);
        assertTrue(resultCollector.isSuccess(),resultCollector.toReport());
    }

    @Test
    public void shouldFailDueToMissingMeasurementUnit() {
        final String input = ""
                + "<?xml version='1.0' encoding='UTF-8'?>"
                + "<alto xmlns='http://www.loc.gov/standards/alto/ns-v2#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.loc.gov/standards/alto alto-v2.0.xsd'>"
                + "  <Description>"
                + "    <MeasurementUnit></MeasurementUnit>"
                + "  </Description>"
                + "</alto>";

        initialise();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToWrongMeasurementUnit() {
        final String input = ""
                + "<?xml version='1.0' encoding='UTF-8'?>"
                + "<alto xmlns='http://www.loc.gov/standards/alto/ns-v2#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.loc.gov/standards/alto alto-v2.0.xsd'>"
                + "  <Description>"
                + "    <MeasurementUnit>Wrong measurement unit</MeasurementUnit>"
                + "  </Description>"
                + "</alto>";

        initialise();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToMissingOCRProcessing() {
        final String input = ""
                + "<?xml version='1.0' encoding='UTF-8'?>"
                + "<alto xmlns='http://www.loc.gov/standards/alto/ns-v2#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.loc.gov/standards/alto alto-v2.0.xsd'>"
                + "  <Description>"
                + "    <MeasurementUnit>inch1200</MeasurementUnit>"
                + "      <ocrProcessingStep>"
                + "        <processingStepSettings>version:ABBYY Recognition Server 3.0</processingStepSettings>"
                + "        <processingSoftware>"
                + "          <softwareName>ABBYY Recognition Server</softwareName>"
                + "          <softwareVersion>3.0</softwareVersion>"
                + "        </processingSoftware>"
                + "      </ocrProcessingStep>"
                + "  </Description>"
                + "</alto>";

        initialise();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToMissingProcessingStepSettings() {
        final String input = ""
                + "<?xml version='1.0' encoding='UTF-8'?>"
                + "<alto xmlns='http://www.loc.gov/standards/alto/ns-v2#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.loc.gov/standards/alto alto-v2.0.xsd'>"
                + "  <Description>"
                + "    <MeasurementUnit>inch1200</MeasurementUnit>"
                + "    <OCRProcessing ID='OCR1'>"
                + "      <ocrProcessingStep>"
                + "        <processingStepSettings></processingStepSettings>"
                + "        <processingSoftware>"
                + "          <softwareName>ABBYY Recognition Server</softwareName>"
                + "          <softwareVersion>3.0</softwareVersion>"
                + "        </processingSoftware>"
                + "      </ocrProcessingStep>"
                + "    </OCRProcessing>"
                + "  </Description>"
                + "</alto>";

        initialise();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToMissingSoftwareName() {
        final String input = ""
                + "<?xml version='1.0' encoding='UTF-8'?>"
                + "<alto xmlns='http://www.loc.gov/standards/alto/ns-v2#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.loc.gov/standards/alto alto-v2.0.xsd'>"
                + "  <Description>"
                + "    <MeasurementUnit>inch1200</MeasurementUnit>"
                + "    <OCRProcessing ID='OCR1'>"
                + "      <ocrProcessingStep>"
                + "        <processingStepSettings>version:ABBYY Recognition Server 3.0</processingStepSettings>"
                + "        <processingSoftware>"
                + "          <softwareName></softwareName>"
                + "          <softwareVersion>3.0</softwareVersion>"
                + "        </processingSoftware>"
                + "      </ocrProcessingStep>"
                + "    </OCRProcessing>"
                + "  </Description>"
                + "</alto>";

        initialise();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToMissingSoftwareVersion() {
        final String input = ""
                + "<?xml version='1.0' encoding='UTF-8'?>"
                + "<alto xmlns='http://www.loc.gov/standards/alto/ns-v2#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.loc.gov/standards/alto alto-v2.0.xsd'>"
                + "  <Description>"
                + "    <MeasurementUnit>inch1200</MeasurementUnit>"
                + "    <OCRProcessing ID='OCR1'>"
                + "      <ocrProcessingStep>"
                + "        <processingStepSettings>version:ABBYY Recognition Server 3.0</processingStepSettings>"
                + "        <processingSoftware>"
                + "          <softwareName>ABBYY Recognition Server</softwareName>"
                + "          <softwareVersion></softwareVersion>"
                + "        </processingSoftware>"
                + "      </ocrProcessingStep>"
                + "    </OCRProcessing>"
                + "  </Description>"
                + "</alto>";

        initialise();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToMissingPageHeightAttribute() {
        final String input = ""
                + "<?xml version='1.0' encoding='UTF-8'?>"
                + "<alto xmlns='http://www.loc.gov/standards/alto/ns-v2#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.loc.gov/standards/alto alto-v2.0.xsd'>"
                + "  <Layout>"
                + "    <Page WIDTH='9304'>"
                + "    </Page>"
                + "  </Layout>"
                + "</alto>";

        initialise();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToEmptyPageHeightAttribute() {
        final String input = ""
                + "<?xml version='1.0' encoding='UTF-8'?>"
                + "<alto xmlns='http://www.loc.gov/standards/alto/ns-v2#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.loc.gov/standards/alto alto-v2.0.xsd'>"
                + "  <Layout>"
                + "    <Page HEIGHT='' WIDTH='9304'>"
                + "    </Page>"
                + "  </Layout>"
                + "</alto>";

        initialise();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToMissingPageWidthAttribute() {
        final String input = ""
                + "<?xml version='1.0' encoding='UTF-8'?>"
                + "<alto xmlns='http://www.loc.gov/standards/alto/ns-v2#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.loc.gov/standards/alto alto-v2.0.xsd'>"
                + "  <Layout>"
                + "    <Page HEIGHT='11408'>"
                + "    </Page>"
                + "  </Layout>"
                + "</alto>";

        initialise();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToEmptyPageWidthAttribute() {
        final String input = ""
                + "<?xml version='1.0' encoding='UTF-8'?>"
                + "<alto xmlns='http://www.loc.gov/standards/alto/ns-v2#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.loc.gov/standards/alto alto-v2.0.xsd'>"
                + "  <Layout>"
                + "    <Page HEIGHT='11408' WIDTH=''>"
                + "    </Page>"
                + "  </Layout>"
                + "</alto>";

        initialise();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToMissingStringHeightAttribute() {
        final String input = ""
                + "<?xml version='1.0' encoding='UTF-8'?>"
                + "<alto xmlns='http://www.loc.gov/standards/alto/ns-v2#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.loc.gov/standards/alto alto-v2.0.xsd'>"
                + "  <Layout>"
                + "    <Page HEIGHT='11408' WIDTH='9304'>"
                + "      <PrintSpace>"
                + "        <TextBlock language='dan'>"
                + "          <TextLine>"
                + "            <String CONTENT='Ao.' WIDTH='480' HPOS='584' VPOS='1000' />"
                + "          </TextLine>"
                + "        </TextBlock>"
                + "      </PrintSpace>"
                + "    </Page>"
                + "  </Layout>"
                + "</alto>";

        initialise();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToEmptyStringHeightAttribute() {
        final String input = ""
                + "<?xml version='1.0' encoding='UTF-8'?>"
                + "<alto xmlns='http://www.loc.gov/standards/alto/ns-v2#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.loc.gov/standards/alto alto-v2.0.xsd'>"
                + "  <Layout>"
                + "    <Page HEIGHT='11408' WIDTH='9304'>"
                + "      <PrintSpace>"
                + "        <TextBlock language='dan'>"
                + "          <TextLine>"
                + "            <String CONTENT='Ao.' HEIGHT='' WIDTH='480' HPOS='584' VPOS='1000' />"
                + "          </TextLine>"
                + "        </TextBlock>"
                + "      </PrintSpace>"
                + "    </Page>"
                + "  </Layout>"
                + "</alto>";

        initialise();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToMissingStringWidthAttribute() {
        final String input = ""
                + "<?xml version='1.0' encoding='UTF-8'?>"
                + "<alto xmlns='http://www.loc.gov/standards/alto/ns-v2#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.loc.gov/standards/alto alto-v2.0.xsd'>"
                + "  <Layout>"
                + "    <Page HEIGHT='11408' WIDTH='9304'>"
                + "      <PrintSpace>"
                + "        <TextBlock language='dan'>"
                + "          <TextLine>"
                + "            <String CONTENT='Ao.' HEIGHT='296' HPOS='584' VPOS='1000' />"
                + "          </TextLine>"
                + "        </TextBlock>"
                + "      </PrintSpace>"
                + "    </Page>"
                + "  </Layout>"
                + "</alto>";

        initialise();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToEmptyStringWidthAttribute() {
        final String input = ""
                + "<?xml version='1.0' encoding='UTF-8'?>"
                + "<alto xmlns='http://www.loc.gov/standards/alto/ns-v2#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.loc.gov/standards/alto alto-v2.0.xsd'>"
                + "  <Layout>"
                + "    <Page HEIGHT='11408' WIDTH='9304'>"
                + "      <PrintSpace>"
                + "        <TextBlock language='dan'>"
                + "          <TextLine>"
                + "            <String CONTENT='Ao.' HEIGHT='296' WIDTH='' HPOS='584' VPOS='1000' />"
                + "          </TextLine>"
                + "        </TextBlock>"
                + "      </PrintSpace>"
                + "    </Page>"
                + "  </Layout>"
                + "</alto>";

        initialise();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToMissingStringHPosAttribute() {
        final String input = ""
                + "<?xml version='1.0' encoding='UTF-8'?>"
                + "<alto xmlns='http://www.loc.gov/standards/alto/ns-v2#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.loc.gov/standards/alto alto-v2.0.xsd'>"
                + "  <Layout>"
                + "    <Page HEIGHT='11408' WIDTH='9304'>"
                + "      <PrintSpace>"
                + "        <TextBlock language='dan'>"
                + "          <TextLine>"
                + "            <String CONTENT='Ao.' HEIGHT='296' WIDTH='480' VPOS='1000' />"
                + "          </TextLine>"
                + "        </TextBlock>"
                + "      </PrintSpace>"
                + "    </Page>"
                + "  </Layout>"
                + "</alto>";

        initialise();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToEmptyStringHPosAttribute() {
        final String input = ""
                + "<?xml version='1.0' encoding='UTF-8'?>"
                + "<alto xmlns='http://www.loc.gov/standards/alto/ns-v2#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.loc.gov/standards/alto alto-v2.0.xsd'>"
                + "  <Layout>"
                + "    <Page HEIGHT='11408' WIDTH='9304'>"
                + "      <PrintSpace>"
                + "        <TextBlock language='dan'>"
                + "          <TextLine>"
                + "            <String CONTENT='Ao.' HEIGHT='296' WIDTH='480' HPOS='' VPOS='1000' />"
                + "          </TextLine>"
                + "        </TextBlock>"
                + "      </PrintSpace>"
                + "    </Page>"
                + "  </Layout>"
                + "</alto>";

        initialise();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToMissingStringVPosAttribute() {
        final String input = ""
                + "<?xml version='1.0' encoding='UTF-8'?>"
                + "<alto xmlns='http://www.loc.gov/standards/alto/ns-v2#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.loc.gov/standards/alto alto-v2.0.xsd'>"
                + "  <Layout>"
                + "    <Page HEIGHT='11408' WIDTH='9304'>"
                + "      <PrintSpace>"
                + "        <TextBlock language='dan'>"
                + "          <TextLine>"
                + "            <String CONTENT='Ao.' HEIGHT='296' WIDTH='480' HPOS='584' />"
                + "          </TextLine>"
                + "        </TextBlock>"
                + "      </PrintSpace>"
                + "    </Page>"
                + "  </Layout>"
                + "</alto>";

        initialise();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToEmptyStringVPosAttribute() {
        final String input = ""
                + "<?xml version='1.0' encoding='UTF-8'?>"
                + "<alto xmlns='http://www.loc.gov/standards/alto/ns-v2#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.loc.gov/standards/alto alto-v2.0.xsd'>"
                + "  <Layout>"
                + "    <Page HEIGHT='11408' WIDTH='9304'>"
                + "      <PrintSpace>"
                + "        <TextBlock language='dan'>"
                + "          <TextLine>"
                + "            <String CONTENT='Ao.' HEIGHT='296' WIDTH='480' HPOS='584' VPOS='' />"
                + "          </TextLine>"
                + "        </TextBlock>"
                + "      </PrintSpace>"
                + "    </Page>"
                + "  </Layout>"
                + "</alto>";

        initialise();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToMissingStringContentAttribute() {
        final String input = ""
                + "<?xml version='1.0' encoding='UTF-8'?>"
                + "<alto xmlns='http://www.loc.gov/standards/alto/ns-v2#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.loc.gov/standards/alto alto-v2.0.xsd'>"
                + "  <Layout>"
                + "    <Page HEIGHT='11408' WIDTH='9304'>"
                + "      <PrintSpace>"
                + "        <TextBlock language='dan'>"
                + "          <TextLine>"
                + "            <String HEIGHT='296' WIDTH='480' HPOS='584' VPOS='1000' />"
                + "          </TextLine>"
                + "        </TextBlock>"
                + "      </PrintSpace>"
                + "    </Page>"
                + "  </Layout>"
                + "</alto>";

        initialise();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToEmptyStringContentAttribute() {
        final String input = ""
                + "<?xml version='1.0' encoding='UTF-8'?>"
                + "<alto xmlns='http://www.loc.gov/standards/alto/ns-v2#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.loc.gov/standards/alto alto-v2.0.xsd'>"
                + "  <Layout>"
                + "    <Page HEIGHT='11408' WIDTH='9304'>"
                + "      <PrintSpace>"
                + "        <TextBlock language='dan'>"
                + "          <TextLine>"
                + "            <String CONTENT='' HEIGHT='296' WIDTH='480' HPOS='584' VPOS='1000' />"
                + "          </TextLine>"
                + "        </TextBlock>"
                + "      </PrintSpace>"
                + "    </Page>"
                + "  </Layout>"
                + "</alto>";

        initialise();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToStringContentAttributeContainingTwoWords() {
        final String input = ""
                + "<?xml version='1.0' encoding='UTF-8'?>"
                + "<alto xmlns='http://www.loc.gov/standards/alto/ns-v2#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.loc.gov/standards/alto alto-v2.0.xsd'>"
                + "  <Layout>"
                + "    <Page HEIGHT='11408' WIDTH='9304'>"
                + "      <PrintSpace>"
                + "        <TextBlock language='dan'>"
                + "          <TextLine>"
                + "            <String CONTENT='Ao. Ao.' HEIGHT='296' WIDTH='480' HPOS='584' VPOS='1000' />"
                + "          </TextLine>"
                + "        </TextBlock>"
                + "      </PrintSpace>"
                + "    </Page>"
                + "  </Layout>"
                + "</alto>";

        initialise();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailDueToStringContentAttributeContainingTwoWordsStillAllowingWhitespaceAsPrefixOrPostfix() {
        final String input = ""
                + "<?xml version='1.0' encoding='UTF-8'?>"
                + "<alto xmlns='http://www.loc.gov/standards/alto/ns-v2#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.loc.gov/standards/alto alto-v2.0.xsd'>"
                + "  <Layout>"
                + "    <Page HEIGHT='11408' WIDTH='9304'>"
                + "      <PrintSpace>"
                + "        <TextBlock language='dan'>"
                + "          <TextLine>"
                + "            <String CONTENT=' Ao. Ao. ' HEIGHT='296' WIDTH='480' HPOS='584' VPOS='1000' />"
                + "          </TextLine>"
                + "        </TextBlock>"
                + "      </PrintSpace>"
                + "    </Page>"
                + "  </Layout>"
                + "</alto>";

        initialise();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void shouldPassWhenStringContentAttributeContainsWhitespaceAsPrefixOrPostfix() {
        final String input = ""
                + "<?xml version='1.0' encoding='UTF-8'?>"
                + "<alto xmlns='http://www.loc.gov/standards/alto/ns-v2#' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.loc.gov/standards/alto alto-v2.0.xsd'>"
                + "  <Layout>"
                + "    <Page HEIGHT='11408' WIDTH='9304'>"
                + "      <PrintSpace>"
                + "        <TextBlock language='dan'>"
                + "          <TextLine>"
                + "            <String CONTENT=' Ao. ' HEIGHT='296' WIDTH='480' HPOS='584' VPOS='1000' />"
                + "          </TextLine>"
                + "        </TextBlock>"
                + "      </PrintSpace>"
                + "    </Page>"
                + "  </Layout>"
                + "</alto>";

        initialise();
        handleTestEvent(input, resultCollector);
        assertTrue(resultCollector.isSuccess());
    }

    private void handleTestEvent(final String input, ResultCollector resultCollector) {
        DocumentCache documentCache = new DocumentCache();
        SchematronValidatorEventHandler handler = new SchematronValidatorEventHandler(resultCollector, documentCache,attributeConfigs);
        AttributeParsingEvent event = new AttributeParsingEvent("test.alto.xml") {
            @Override
            public InputStream getData() throws IOException {
                return new ByteArrayInputStream(input.getBytes("UTF-8"));
            }

            @Override
            public String getChecksum() throws IOException {
                return null;
            }
        };
        handler.handleAttribute(event);
    }
}
