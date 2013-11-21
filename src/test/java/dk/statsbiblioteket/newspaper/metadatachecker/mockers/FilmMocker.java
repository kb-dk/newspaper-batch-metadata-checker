package dk.statsbiblioteket.newspaper.metadatachecker.mockers;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class FilmMocker {


    public static AttributeParsingEvent getFilmXmlAttributeParsingEvent(final String film, final String avisID, final String title,
                                                                    final String createdDate, final String startDate,
                                                                    final String endDate, final Batch batch,
                                                                    final int resolution) {
           return new AttributeParsingEvent(
                   batch.getFullID() + "/" +
                   batch.getBatchID() + "-" + film + "/" +
                   avisID + "-" + batch.getBatchID() + "-" + film + ".film.xml") {
               @Override
               public InputStream getData() throws IOException {
                   return new ByteArrayInputStream(
                           getFilmXml(film, title, startDate, endDate, createdDate, batch, resolution));
               }

               @Override
               public String getChecksum() throws IOException {
                   return null;
               }
           };
       }


    public static byte[] getFilmXml(final String film, final String title, final String startDate, final String endDate, 
                                    final String createdDate, final Batch batch,
                                    final int resolution) {
        try {
            
            String filmXml = "<avis:reelMetadata xmlns:avis=\"http://www.statsbiblioteket.dk/avisdigitalisering/microfilm/1/0/\"> \n" + 
                                 "<avis:titles>"+title+"</avis:titles> \n" + 
                                 "<avis:startDate>"+startDate+"</avis:startDate> \n" +
                                 "<avis:endDate>"+endDate+"</avis:endDate> \n" +
                                 "<avis:batchIdFilmId>" + batch.getBatchID() + "-" + film + "</avis:batchIdFilmId> \n" + 
                                 "<avis:numberOfPictures>14</avis:numberOfPictures> \n" + 
                                 "<avis:reductionRatio>15x</avis:reductionRatio> \n" +
                                 "<avis:captureResolutionOriginal measurement=\"pixels/inch\">"+resolution+"</avis:captureResolutionOriginal> \n" +
                                 "<avis:captureResolutionFilm measurement=\"pixels/inch\">6000</avis:captureResolutionFilm> \n" +
                                 "<avis:dateMicrofilmCreated>"+createdDate+"</avis:dateMicrofilmCreated> \n" +
                                 "<avis:resolutionOfNegative>6.3</avis:resolutionOfNegative> \n" +
                                 "<avis:resolutionCommentNegative>No comments</avis:resolutionCommentNegative> \n" +
                                 "<avis:densityReadingNegative>0.11</avis:densityReadingNegative> \n" +
                                 "<avis:densityReadingNegative>0.12</avis:densityReadingNegative> \n" +
                                 "<avis:densityReadingNegative>0.12</avis:densityReadingNegative> \n" +
                                 "<avis:densityReadingNegative>0.12</avis:densityReadingNegative> \n" +
                                 "<avis:densityReadingNegative>0.12</avis:densityReadingNegative> \n" +
                                 "<avis:densityReadingNegative>0.13</avis:densityReadingNegative> \n" +
                                 "<avis:densityReadingNegative>0.12</avis:densityReadingNegative> \n" +
                                 "<avis:densityReadingNegative>0.12</avis:densityReadingNegative> \n" +
                                 "<avis:densityReadingNegative>0.12</avis:densityReadingNegative> \n" +
                                 "<avis:densityReadingNegative>0.12</avis:densityReadingNegative> \n" +
                                 "<avis:averageDensityNegative>0.12</avis:averageDensityNegative> \n" +
                                 "<avis:dminNegative>0.12</avis:dminNegative> \n" +
                             "</avis:reelMetadata>";
            
            return filmXml.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }
    }
}
