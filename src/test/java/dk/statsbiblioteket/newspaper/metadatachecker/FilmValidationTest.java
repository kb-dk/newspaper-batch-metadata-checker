package dk.statsbiblioteket.newspaper.metadatachecker;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import static org.mockito.Mockito.*;


public class FilmValidationTest {
    private ResultCollector resultCollector = null;

    @BeforeTest
    public void setUp() {
        resultCollector = new ResultCollector("test", "test");
    }

    @Test
    public void shouldSucceed() {
        final String input = "" +
                "<avis:reelMetadata xmlns:avis=\"http://www.statsbiblioteket.dk/avisdigitalisering/microfilm/1/0/\">\n" +
                "    <avis:titles>Adresse Contoirs Efterretninger</avis:titles>\n" +
                "    <avis:startDate>1795-06-13</avis:startDate>\n" +
                "    <avis:endDate>1795-06-15</avis:endDate>\n" +
                "    <avis:batchIdFilmId>400022028241-1</avis:batchIdFilmId>\n" +
                "    <avis:numberOfPictures>14</avis:numberOfPictures>\n" +
                "    <avis:reductionRatio>15x</avis:reductionRatio>\n" +
                "    <avis:captureResolutionOriginal measurement=\"pixels/inch\">400</avis:captureResolutionOriginal>\n" +
                "    <avis:captureResolutionFilm measurement=\"pixels/inch\">6000</avis:captureResolutionFilm>\n" +
                "    <avis:dateMicrofilmCreated>1970-06-05</avis:dateMicrofilmCreated>\n" +
                "    <avis:looseLeavesFlag>true</avis:looseLeavesFlag>\n" +
                "    <avis:boundVolumeFlag>false</avis:boundVolumeFlag>\n" +
                "    <avis:resolutionOfDuplicateNegative>6.3</avis:resolutionOfDuplicateNegative>\n" +
                "    <avis:resolutionCommentDuplicateNegative>No comments</avis:resolutionCommentDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.11</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.12</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.12</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.12</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.12</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.13</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.12</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.12</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.12</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.12</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:averageDensityDuplicateNegative>0.12</avis:averageDensityDuplicateNegative>\n" +
                "    <avis:dminDuplicateNegative>0.12</avis:dminDuplicateNegative>\n" +
                "</avis:reelMetadata>\n";
        setUp();
        handleTestEvent(input, resultCollector);
        assertTrue(resultCollector.isSuccess());
    }

    @Test
    public void shouldFailBcozEmptyComment() {
        final String input = "" +
                "<avis:reelMetadata xmlns:avis=\"http://www.statsbiblioteket.dk/avisdigitalisering/microfilm/1/0/\">\n" +
                "    <avis:titles>Adresse Contoirs Efterretninger</avis:titles>\n" +
                "    <avis:startDate>1795-06-13</avis:startDate>\n" +
                "    <avis:endDate>1795-06-15</avis:endDate>\n" +
                "    <avis:batchIdFilmId>400022028241-1</avis:batchIdFilmId>\n" +
                "    <avis:numberOfPictures>14</avis:numberOfPictures>\n" +
                "    <avis:reductionRatio>15x</avis:reductionRatio>\n" +
                "    <avis:captureResolutionOriginal measurement=\"pixels/inch\">400</avis:captureResolutionOriginal>\n" +
                "    <avis:captureResolutionFilm measurement=\"pixels/inch\">6000</avis:captureResolutionFilm>\n" +
                "    <avis:dateMicrofilmCreated>1970-06-05</avis:dateMicrofilmCreated>\n" +
                "    <avis:looseLeavesFlag>true</avis:looseLeavesFlag>\n" +
                "    <avis:boundVolumeFlag>false</avis:boundVolumeFlag>\n" +
                "    <avis:resolutionOfDuplicateNegative>3.0</avis:resolutionOfDuplicateNegative>\n" +
                "    <avis:resolutionCommentDuplicateNegative></avis:resolutionCommentDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.11</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.12</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.12</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.12</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.12</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.13</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.12</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.12</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.12</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.12</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:averageDensityDuplicateNegative>0.12</avis:averageDensityDuplicateNegative>\n" +
                "    <avis:dminDuplicateNegative>0.12</avis:dminDuplicateNegative>\n" +
                "</avis:reelMetadata>\n";
        setUp();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess(), resultCollector.toReport());
    }

    @Test
    public void shouldFailBcozWrongCaptureResolutionOriginalUnit() {
        final String input = "" +
                "<avis:reelMetadata xmlns:avis=\"http://www.statsbiblioteket.dk/avisdigitalisering/microfilm/1/0/\">\n" +
                "    <avis:titles>Adresse Contoirs Efterretninger</avis:titles>\n" +
                "    <avis:startDate>1795-06-13</avis:startDate>\n" +
                "    <avis:endDate>1795-06-15</avis:endDate>\n" +
                "    <avis:batchIdFilmId>400022028241-1</avis:batchIdFilmId>\n" +
                "    <avis:numberOfPictures>14</avis:numberOfPictures>\n" +
                "    <avis:reductionRatio>15x</avis:reductionRatio>\n" +
                "    <avis:captureResolutionOriginal measurement=\"pixels/alen\">400</avis:captureResolutionOriginal>\n" +
                "    <avis:captureResolutionFilm measurement=\"pixels/inch\">6000</avis:captureResolutionFilm>\n" +
                "    <avis:dateMicrofilmCreated>1970-06-05</avis:dateMicrofilmCreated>\n" +
                "    <avis:looseLeavesFlag>true</avis:looseLeavesFlag>\n" +
                "    <avis:boundVolumeFlag>false</avis:boundVolumeFlag>\n" +
                "    <avis:resolutionOfDuplicateNegative>6.3</avis:resolutionOfDuplicateNegative>\n" +
                "    <avis:resolutionCommentDuplicateNegative>No comments</avis:resolutionCommentDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.11</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.12</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.12</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.12</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.12</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.13</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.12</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.12</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.12</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.12</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:averageDensityDuplicateNegative>0.12</avis:averageDensityDuplicateNegative>\n" +
                "    <avis:dminDuplicateNegative>0.12</avis:dminDuplicateNegative>\n" +
                "</avis:reelMetadata>\n";
        setUp();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess(), resultCollector.toReport());
    }

    @Test
    public void shouldFailBcozWrongCaptureResolutionFilmUnit() {
        final String input = "" +
                "<avis:reelMetadata xmlns:avis=\"http://www.statsbiblioteket.dk/avisdigitalisering/microfilm/1/0/\">\n" +
                "    <avis:titles>Adresse Contoirs Efterretninger</avis:titles>\n" +
                "    <avis:startDate>1795-06-13</avis:startDate>\n" +
                "    <avis:endDate>1795-06-15</avis:endDate>\n" +
                "    <avis:batchIdFilmId>400022028241-1</avis:batchIdFilmId>\n" +
                "    <avis:numberOfPictures>14</avis:numberOfPictures>\n" +
                "    <avis:reductionRatio>15x</avis:reductionRatio>\n" +
                "    <avis:captureResolutionOriginal measurement=\"pixels/inch\">400</avis:captureResolutionOriginal>\n" +
                "    <avis:captureResolutionFilm measurement=\"pixels/alen\">6000</avis:captureResolutionFilm>\n" +
                "    <avis:dateMicrofilmCreated>1970-06-05</avis:dateMicrofilmCreated>\n" +
                "    <avis:looseLeavesFlag>true</avis:looseLeavesFlag>\n" +
                "    <avis:boundVolumeFlag>false</avis:boundVolumeFlag>\n" +
                "    <avis:resolutionOfDuplicateNegative>6.3</avis:resolutionOfDuplicateNegative>\n" +
                "    <avis:resolutionCommentDuplicateNegative>No comments</avis:resolutionCommentDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.11</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.12</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.12</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.12</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.12</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.13</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.12</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.12</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.12</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.12</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:averageDensityDuplicateNegative>0.12</avis:averageDensityDuplicateNegative>\n" +
                "    <avis:dminDuplicateNegative>0.12</avis:dminDuplicateNegative>\n" +
                "</avis:reelMetadata>\n";
        setUp();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess(), resultCollector.toReport());
    }

    @Test
    public void shouldFailBcozWrongScanningResolutionFilm() {
        final String input = "" +
                "<avis:reelMetadata xmlns:avis=\"http://www.statsbiblioteket.dk/avisdigitalisering/microfilm/1/0/\">\n" +
                "    <avis:titles>Adresse Contoirs Efterretninger</avis:titles>\n" +
                "    <avis:startDate>1795-06-13</avis:startDate>\n" +
                "    <avis:endDate>1795-06-15</avis:endDate>\n" +
                "    <avis:batchIdFilmId>400022028241-1</avis:batchIdFilmId>\n" +
                "    <avis:numberOfPictures>14</avis:numberOfPictures>\n" +
                "    <avis:reductionRatio>15x</avis:reductionRatio>\n" +
                "    <avis:captureResolutionOriginal measurement=\"pixels/inch\">400</avis:captureResolutionOriginal>\n" +
                "    <avis:captureResolutionFilm measurement=\"pixels/inch\">1024</avis:captureResolutionFilm>\n" +
                "    <avis:dateMicrofilmCreated>1970-06-05</avis:dateMicrofilmCreated>\n" +
                "    <avis:looseLeavesFlag>true</avis:looseLeavesFlag>\n" +
                "    <avis:boundVolumeFlag>false</avis:boundVolumeFlag>\n" +
                "    <avis:resolutionOfDuplicateNegative>6.3</avis:resolutionOfDuplicateNegative>\n" +
                "    <avis:resolutionCommentDuplicateNegative>No comments</avis:resolutionCommentDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.11</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.12</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.12</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.12</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.12</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.13</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.12</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.12</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.12</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:densityReadingDuplicateNegative>0.12</avis:densityReadingDuplicateNegative>\n" +
                "    <avis:averageDensityDuplicateNegative>0.12</avis:averageDensityDuplicateNegative>\n" +
                "    <avis:dminDuplicateNegative>0.12</avis:dminDuplicateNegative>\n" +
                "</avis:reelMetadata>\n";
        setUp();
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess(), resultCollector.toReport());
    }

    private void handleTestEvent(final String input, ResultCollector resultCollector) {
        SchematronValidatorEventHandler handler = new SchematronValidatorEventHandler(resultCollector, null);
        AttributeParsingEvent event = new AttributeParsingEvent("test.film.xml") {
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
