package dk.statsbiblioteket.newspaper.metadatachecker;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import dk.statsbiblioteket.newspaper.metadatachecker.caches.DocumentCache;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;


public class FilmValidationTest {
    private ResultCollector resultCollector = null;
    Map<String, AttributeSpec> attributeConfigs = new HashMap<>();


    @BeforeMethod
    public void initialise() {
        resultCollector = new ResultCollector("test", "test");
        attributeConfigs.put(".film.xml",new AttributeSpec(".film.xml", "film.xsd", "film.sch","2E: ","metadata"));
    }

    @Test
    public void shouldSucceed() {
        final String input = "" +
                "<avis:reelMetadata xmlns:avis=\"http://www.statsbiblioteket.dk/avisdigitalisering/microfilm/1/0/\">\n" +
                "    <avis:titles>Adresse Contoirs Efterretninger</avis:titles>\n" +
                "    <avis:startDate>1795-06</avis:startDate>\n" +
                "    <avis:endDate>1795-06-15</avis:endDate>\n" +
                "    <avis:FilmId>400022028241-1</avis:FilmId>\n" +
                "    <avis:numberOfPictures>14</avis:numberOfPictures>\n" +
                "    <avis:reductionRatio>15x</avis:reductionRatio>\n" +
                "    <avis:captureResolutionOriginal measurement=\"pixels/inch\">400</avis:captureResolutionOriginal>\n" +
                "    <avis:captureResolutionFilm measurement=\"pixels/inch\">6000</avis:captureResolutionFilm>\n" +
                "    <avis:dateMicrofilmCreated>1970-06-05</avis:dateMicrofilmCreated>\n" +
                "    <avis:resolutionOfNegative>6.3</avis:resolutionOfNegative>\n" +
                "    <avis:resolutionCommentNegative>No comments</avis:resolutionCommentNegative>\n" +
                "    <avis:densityReadingNegative>0.11</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.13</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:averageDensityNegative>0.12</avis:averageDensityNegative>\n" +
                "    <avis:dminNegative>0.12</avis:dminNegative>\n" +
                "</avis:reelMetadata>\n";
        handleTestEvent(input, resultCollector);
        assertTrue(resultCollector.isSuccess(), resultCollector.toReport());
    }

    @Test
    public void shouldSucceedEmptyCreationDate() {
        final String input = "" +
                "<avis:reelMetadata xmlns:avis=\"http://www.statsbiblioteket.dk/avisdigitalisering/microfilm/1/0/\">\n" +
                "    <avis:titles>Adresse Contoirs Efterretninger</avis:titles>\n" +
                "    <avis:startDate>1795-06</avis:startDate>\n" +
                "    <avis:endDate>1795-06-15</avis:endDate>\n" +
                "    <avis:FilmId>400022028241-1</avis:FilmId>\n" +
                "    <avis:numberOfPictures>14</avis:numberOfPictures>\n" +
                "    <avis:reductionRatio>15x</avis:reductionRatio>\n" +
                "    <avis:captureResolutionOriginal measurement=\"pixels/inch\">400</avis:captureResolutionOriginal>\n" +
                "    <avis:captureResolutionFilm measurement=\"pixels/inch\">6000</avis:captureResolutionFilm>\n" +
                "    <avis:dateMicrofilmCreated></avis:dateMicrofilmCreated>\n" +
                "    <avis:resolutionOfNegative>6.3</avis:resolutionOfNegative>\n" +
                "    <avis:resolutionCommentNegative>No comments</avis:resolutionCommentNegative>\n" +
                "    <avis:densityReadingNegative>0.11</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.13</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:averageDensityNegative>0.12</avis:averageDensityNegative>\n" +
                "    <avis:dminNegative>0.12</avis:dminNegative>\n" +
                "</avis:reelMetadata>\n";
        handleTestEvent(input, resultCollector);
        assertTrue(resultCollector.isSuccess(), resultCollector.toReport());
    }

    @Test
    public void shouldFailBcozEmptyComment() {
        final String input = "" +
                "<avis:reelMetadata xmlns:avis=\"http://www.statsbiblioteket.dk/avisdigitalisering/microfilm/1/0/\">\n" +
                "    <avis:titles>Adresse Contoirs Efterretninger</avis:titles>\n" +
                "    <avis:startDate>1795-06</avis:startDate>\n" +
                "    <avis:endDate>1795-06-15</avis:endDate>\n" +
                "    <avis:FilmId>400022028241-1</avis:FilmId>\n" +
                "    <avis:numberOfPictures>14</avis:numberOfPictures>\n" +
                "    <avis:reductionRatio>15x</avis:reductionRatio>\n" +
                "    <avis:captureResolutionOriginal measurement=\"pixels/inch\">400</avis:captureResolutionOriginal>\n" +
                "    <avis:captureResolutionFilm measurement=\"pixels/inch\">6000</avis:captureResolutionFilm>\n" +
                "    <avis:dateMicrofilmCreated>1970-06-05</avis:dateMicrofilmCreated>\n" +
                "    <avis:resolutionOfNegative>3.0</avis:resolutionOfNegative>\n" +
                "    <avis:resolutionCommentNegative></avis:resolutionCommentNegative>\n" +
                "    <avis:densityReadingNegative>0.11</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.13</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:averageDensityNegative>0.12</avis:averageDensityNegative>\n" +
                "    <avis:dminNegative>0.12</avis:dminNegative>\n" +
                "</avis:reelMetadata>\n";
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess(), resultCollector.toReport());
    }

    @Test
    public void shouldFailBcozWrongCaptureResolutionOriginalUnit() {
        final String input = "" +
                "<avis:reelMetadata xmlns:avis=\"http://www.statsbiblioteket.dk/avisdigitalisering/microfilm/1/0/\">\n" +
                "    <avis:titles>Adresse Contoirs Efterretninger</avis:titles>\n" +
                "    <avis:startDate>1795-06</avis:startDate>\n" +
                "    <avis:endDate>1795-06-15</avis:endDate>\n" +
                "    <avis:FilmId>400022028241-1</avis:FilmId>\n" +
                "    <avis:numberOfPictures>14</avis:numberOfPictures>\n" +
                "    <avis:reductionRatio>15x</avis:reductionRatio>\n" +
                "    <avis:captureResolutionOriginal measurement=\"pixels/alen\">400</avis:captureResolutionOriginal>\n" +
                "    <avis:captureResolutionFilm measurement=\"pixels/inch\">6000</avis:captureResolutionFilm>\n" +
                "    <avis:dateMicrofilmCreated>1970-06-05</avis:dateMicrofilmCreated>\n" +
                "    <avis:resolutionOfNegative>6.3</avis:resolutionOfNegative>\n" +
                "    <avis:resolutionCommentNegative>No comments</avis:resolutionCommentNegative>\n" +
                "    <avis:densityReadingNegative>0.11</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.13</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:averageDensityNegative>0.12</avis:averageDensityNegative>\n" +
                "    <avis:dminNegative>0.12</avis:dminNegative>\n" +
                "</avis:reelMetadata>\n";
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess(), resultCollector.toReport());
    }

    @Test
    public void shouldFailBcozWrongCaptureResolutionFilmUnit() {
        final String input = "" +
                "<avis:reelMetadata xmlns:avis=\"http://www.statsbiblioteket.dk/avisdigitalisering/microfilm/1/0/\">\n" +
                "    <avis:titles>Adresse Contoirs Efterretninger</avis:titles>\n" +
                "    <avis:startDate>1795-06</avis:startDate>\n" +
                "    <avis:endDate>1795-06-15</avis:endDate>\n" +
                "    <avis:FilmId>400022028241-1</avis:FilmId>\n" +
                "    <avis:numberOfPictures>14</avis:numberOfPictures>\n" +
                "    <avis:reductionRatio>15x</avis:reductionRatio>\n" +
                "    <avis:captureResolutionOriginal measurement=\"pixels/inch\">400</avis:captureResolutionOriginal>\n" +
                "    <avis:captureResolutionFilm measurement=\"pixels/alen\">6000</avis:captureResolutionFilm>\n" +
                "    <avis:dateMicrofilmCreated>1970-06-05</avis:dateMicrofilmCreated>\n" +
                "    <avis:resolutionOfNegative>6.3</avis:resolutionOfNegative>\n" +
                "    <avis:resolutionCommentNegative>No comments</avis:resolutionCommentNegative>\n" +
                "    <avis:densityReadingNegative>0.11</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.13</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:averageDensityNegative>0.12</avis:averageDensityNegative>\n" +
                "    <avis:dminNegative>0.12</avis:dminNegative>\n" +
                "</avis:reelMetadata>\n";
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess(), resultCollector.toReport());
    }

    @Test
    public void shouldFailBcozWrongScanningResolutionFilm() {
        final String input = "" +
                "<avis:reelMetadata xmlns:avis=\"http://www.statsbiblioteket.dk/avisdigitalisering/microfilm/1/0/\">\n" +
                "    <avis:titles>Adresse Contoirs Efterretninger</avis:titles>\n" +
                "    <avis:startDate>1795-06</avis:startDate>\n" +
                "    <avis:endDate>1795-06-15</avis:endDate>\n" +
                "    <avis:FilmId>400022028241-1</avis:FilmId>\n" +
                "    <avis:numberOfPictures>14</avis:numberOfPictures>\n" +
                "    <avis:reductionRatio>15x</avis:reductionRatio>\n" +
                "    <avis:captureResolutionOriginal measurement=\"pixels/inch\">400</avis:captureResolutionOriginal>\n" +
                "    <avis:captureResolutionFilm measurement=\"pixels/inch\">1024</avis:captureResolutionFilm>\n" +
                "    <avis:dateMicrofilmCreated>1970-06-05</avis:dateMicrofilmCreated>\n" +
                "    <avis:resolutionOfNegative>6.3</avis:resolutionOfNegative>\n" +
                "    <avis:resolutionCommentNegative>No comments</avis:resolutionCommentNegative>\n" +
                "    <avis:densityReadingNegative>0.11</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.13</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:averageDensityNegative>0.12</avis:averageDensityNegative>\n" +
                "    <avis:dminNegative>0.12</avis:dminNegative>\n" +
                "</avis:reelMetadata>\n";
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess(), resultCollector.toReport());
    }

    @Test
    public void shouldFailBcozWrongSequenceDates() {
        final String input = "" +
                "<avis:reelMetadata xmlns:avis=\"http://www.statsbiblioteket.dk/avisdigitalisering/microfilm/1/0/\">\n" +
                "    <avis:titles>Adresse Contoirs Efterretninger</avis:titles>\n" +
                "    <avis:startDate>1795-06</avis:startDate>\n" +
                "    <avis:endDate>1790-06-15</avis:endDate>\n" +
                "    <avis:FilmId>400022028241-1</avis:FilmId>\n" +
                "    <avis:numberOfPictures>14</avis:numberOfPictures>\n" +
                "    <avis:reductionRatio>15x</avis:reductionRatio>\n" +
                "    <avis:captureResolutionOriginal measurement=\"pixels/inch\">400</avis:captureResolutionOriginal>\n" +
                "    <avis:captureResolutionFilm measurement=\"pixels/inch\">6000</avis:captureResolutionFilm>\n" +
                "    <avis:dateMicrofilmCreated>1970-06-05</avis:dateMicrofilmCreated>\n" +
                "    <avis:resolutionOfNegative>6.3</avis:resolutionOfNegative>\n" +
                "    <avis:resolutionCommentNegative>No comments</avis:resolutionCommentNegative>\n" +
                "    <avis:densityReadingNegative>0.11</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.13</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:averageDensityNegative>0.12</avis:averageDensityNegative>\n" +
                "    <avis:dminNegative>0.12</avis:dminNegative>\n" +
                "</avis:reelMetadata>\n";
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess(), resultCollector.toReport());
    }

    @Test
    public void shouldFailBcozWrongDateFormat() {
        final String input = "" +
                "<avis:reelMetadata xmlns:avis=\"http://www.statsbiblioteket.dk/avisdigitalisering/microfilm/1/0/\">\n" +
                "    <avis:titles>Adresse Contoirs Efterretninger</avis:titles>\n" +
                "    <avis:startDate>1795/06/13</avis:startDate>\n" +
                "    <avis:endDate>1795-06-15</avis:endDate>\n" +
                "    <avis:FilmId>400022028241-1</avis:FilmId>\n" +
                "    <avis:numberOfPictures>14</avis:numberOfPictures>\n" +
                "    <avis:reductionRatio>15x</avis:reductionRatio>\n" +
                "    <avis:captureResolutionOriginal measurement=\"pixels/inch\">400</avis:captureResolutionOriginal>\n" +
                "    <avis:captureResolutionFilm measurement=\"pixels/inch\">6000</avis:captureResolutionFilm>\n" +
                "    <avis:dateMicrofilmCreated>1970-06-05</avis:dateMicrofilmCreated>\n" +
                "    <avis:resolutionOfNegative>6.3</avis:resolutionOfNegative>\n" +
                "    <avis:resolutionCommentNegative>No comments</avis:resolutionCommentNegative>\n" +
                "    <avis:densityReadingNegative>0.11</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.13</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:averageDensityNegative>0.12</avis:averageDensityNegative>\n" +
                "    <avis:dminNegative>0.12</avis:dminNegative>\n" +
                "</avis:reelMetadata>\n";
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess(), resultCollector.toReport());
    }

    @Test
    public void shouldFailBcozWrongMicrofilmCreatedDateFormat() {
        final String input = "" +
                "<avis:reelMetadata xmlns:avis=\"http://www.statsbiblioteket.dk/avisdigitalisering/microfilm/1/0/\">\n" +
                "    <avis:titles>Adresse Contoirs Efterretninger</avis:titles>\n" +
                "    <avis:startDate>1795-06</avis:startDate>\n" +
                "    <avis:endDate>1795-06-15</avis:endDate>\n" +
                "    <avis:FilmId>400022028241-1</avis:FilmId>\n" +
                "    <avis:numberOfPictures>14</avis:numberOfPictures>\n" +
                "    <avis:reductionRatio>15x</avis:reductionRatio>\n" +
                "    <avis:captureResolutionOriginal measurement=\"pixels/inch\">400</avis:captureResolutionOriginal>\n" +
                "    <avis:captureResolutionFilm measurement=\"pixels/inch\">6000</avis:captureResolutionFilm>\n" +
                "    <avis:dateMicrofilmCreated>1970/06/05</avis:dateMicrofilmCreated>\n" +
                "    <avis:resolutionOfNegative>6.3</avis:resolutionOfNegative>\n" +
                "    <avis:resolutionCommentNegative>No comments</avis:resolutionCommentNegative>\n" +
                "    <avis:densityReadingNegative>0.11</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.13</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:averageDensityNegative>0.12</avis:averageDensityNegative>\n" +
                "    <avis:dminNegative>0.12</avis:dminNegative>\n" +
                "</avis:reelMetadata>\n";
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess(), resultCollector.toReport());
    }

    @Test
    public void shouldFailBcozWrongMicrofilmCreatedDateSequence() {
        final String input = "" +
                "<avis:reelMetadata xmlns:avis=\"http://www.statsbiblioteket.dk/avisdigitalisering/microfilm/1/0/\">\n" +
                "    <avis:titles>Adresse Contoirs Efterretninger</avis:titles>\n" +
                "    <avis:startDate>1795-06</avis:startDate>\n" +
                "    <avis:endDate>1795-06-15</avis:endDate>\n" +
                "    <avis:FilmId>400022028241-1</avis:FilmId>\n" +
                "    <avis:numberOfPictures>14</avis:numberOfPictures>\n" +
                "    <avis:reductionRatio>15x</avis:reductionRatio>\n" +
                "    <avis:captureResolutionOriginal measurement=\"pixels/inch\">400</avis:captureResolutionOriginal>\n" +
                "    <avis:captureResolutionFilm measurement=\"pixels/inch\">6000</avis:captureResolutionFilm>\n" +
                "    <avis:dateMicrofilmCreated>1670-06-05</avis:dateMicrofilmCreated>\n" +
                "    <avis:resolutionOfNegative>6.3</avis:resolutionOfNegative>\n" +
                "    <avis:resolutionCommentNegative>No comments</avis:resolutionCommentNegative>\n" +
                "    <avis:densityReadingNegative>0.11</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.13</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:densityReadingNegative>0.12</avis:densityReadingNegative>\n" +
                "    <avis:averageDensityNegative>0.12</avis:averageDensityNegative>\n" +
                "    <avis:dminNegative>0.12</avis:dminNegative>\n" +
                "</avis:reelMetadata>\n";
        handleTestEvent(input, resultCollector);
        assertFalse(resultCollector.isSuccess(), resultCollector.toReport());
    }

    private void handleTestEvent(final String input, ResultCollector resultCollector) {
        DocumentCache documentCache = new DocumentCache();
        SchematronValidatorEventHandler handler = new SchematronValidatorEventHandler(resultCollector, documentCache,attributeConfigs);
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
