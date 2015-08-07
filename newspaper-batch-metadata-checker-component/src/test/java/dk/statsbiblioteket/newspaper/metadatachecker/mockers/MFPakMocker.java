package dk.statsbiblioteket.newspaper.metadatachecker.mockers;

import dk.statsbiblioteket.newspaper.mfpakintegration.database.MfPakDAO;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperBatchOptions;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperDateRange;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperEntity;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperTitle;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MFPakMocker {

    public static MfPakDAO getMFPak() throws SQLException, ParseException {

        MfPakDAO mfPakDAO = mock(MfPakDAO.class);
        when(mfPakDAO.getNewspaperID(anyString())).thenReturn("adresseavisen1759");
        NewspaperEntity entity = new NewspaperEntity();
        entity.setNewspaperTitle("Kiøbenhavns Kongelig alene priviligerede Adresse-Contoirs Efterretninger");
        entity.setNewspaperID("adresseavisen1759");
        entity.setPublicationLocation("København");
        entity.setNewspaperDateRange(new NewspaperDateRange(new SimpleDateFormat("yyyy").parse("1600"), new Date()));
        when(mfPakDAO.getBatchNewspaperEntities(anyString())).thenReturn(Arrays.asList(entity));
        NewspaperEntity entity2 = new NewspaperEntity();
        entity2.setPublicationLocation("København");
        entity2.setNewspaperID("adresseavisen1759");
        entity2.setNewspaperTitle("Kiøbenhavns Kongelig alene priviligerede Adresse-Contoirs Efterretninger");
        when(mfPakDAO.getNewspaperEntity(anyString(), any(Date.class))).thenReturn(entity2);
        when(mfPakDAO.getBatchShipmentDate(anyString())).thenReturn(new Date(0));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        NewspaperDateRange filmDateRange = new NewspaperDateRange(sdf.parse("1795-06-01"), sdf.parse("1795-06-15"));
        List<NewspaperDateRange> ranges = new ArrayList<>();
        ranges.add(filmDateRange);
        when(mfPakDAO.getBatchDateRanges(anyString())).thenReturn(ranges);
        NewspaperBatchOptions options = mock(NewspaperBatchOptions.class);
        when(options.isOptionB7()).thenReturn(true);
        when(mfPakDAO.getBatchOptions(anyString())).thenReturn(options);
        return mfPakDAO;

    }
}
