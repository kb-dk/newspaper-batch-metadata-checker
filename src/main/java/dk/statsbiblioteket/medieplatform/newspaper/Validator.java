package dk.statsbiblioteket.medieplatform.newspaper;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;

import java.io.InputStream;

public interface Validator {


    public boolean validate(String reference, String contents, ResultCollector resultCollector);

    public boolean validate(String reference, InputStream contents, ResultCollector resultCollector);
}
