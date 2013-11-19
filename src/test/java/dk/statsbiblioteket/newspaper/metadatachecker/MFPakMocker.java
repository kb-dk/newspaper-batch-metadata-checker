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
        NewspaperEntity entity = new NewspaperEntity();
        entity.setNewspaperTitle("Kiøbenhavns Kongelig alene priviligerede Adresse-Contoirs Efterretninger");
        entity.setNewspaperID("adresseavisen1759");
        entity.setPublicationLocation("København");
        entity.setNewspaperDateRange(new NewspaperDateRange(new Date(Long.MIN_VALUE), new Date()));
        when(mfPakDAO.getBatchNewspaperEntities(anyString())).thenReturn(Arrays.asList(entity));
        NewspaperEntity entity2 = new NewspaperEntity();
        entity2.setPublicationLocation("København");
        entity2.setNewspaperID("adresseavisen1759");
        entity2.setNewspaperTitle("Kiøbenhavns Kongelig alene priviligerede Adresse-Contoirs Efterretninger");
        when(mfPakDAO.getNewspaperEntity(anyString(), any(Date.class))).thenReturn(entity2);
        when(mfPakDAO.getBatchShipmentDate(anyString())).thenReturn(new Date(0));
        return mfPakDAO;

    }
}