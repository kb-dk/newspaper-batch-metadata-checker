package dk.statsbiblioteket.newspaper.metadatachecker;

import dk.statsbiblioteket.newspaper.mfpakintegration.database.MfPakDAO;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperDateRange;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperEntity;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperTitle;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MFPakMocker {

    public static MfPakDAO getMFPak() throws SQLException {

        MfPakDAO mfPakDAO = mock(MfPakDAO.class);
        when(mfPakDAO.getNewspaperID(anyString())).thenReturn("adresseavisen1759");
        NewspaperTitle title = new NewspaperTitle();
        title.setTitle("Kiøbenhavns Kongelig alene priviligerede Adresse-Contoirs Efterretninger");
        title.setDateRange(new NewspaperDateRange(new Date(Long.MIN_VALUE), new Date()));
        when(mfPakDAO.getBatchNewspaperTitles(anyString())).thenReturn(Arrays.asList(title));
        NewspaperEntity entity = new NewspaperEntity();
        entity.setPublicationLocation("København");
        entity.setNewspaperID("adresseavisen1759");
        entity.setNewspaperTitle("Kiøbenhavns Kongelig alene priviligerede Adresse-Contoirs Efterretninger");
        when(mfPakDAO.getNewspaperEntity(anyString(), any(Date.class))).thenReturn(entity);
        when(mfPakDAO.getBatchShipmentDate(anyString())).thenReturn(new Date(0));
        return mfPakDAO;

    }
}
