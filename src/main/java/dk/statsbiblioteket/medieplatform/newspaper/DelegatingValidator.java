package dk.statsbiblioteket.medieplatform.newspaper;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DelegatingValidator
        implements Validator {

    List<Validator> delegates = new ArrayList<>();

    public DelegatingValidator(Validator... delegate) {
        delegates.addAll(Arrays.asList(delegate));
    }


    @Override
    public boolean validate(String reference,
                            String contents,
                            ResultCollector resultCollector) {
        boolean result = true;
        for (Validator delegate : delegates) {
            result = delegate.validate(reference, contents, resultCollector) & result;
        }
        return result;
    }

    @Override
    public boolean validate(String reference,
                            InputStream contents,
                            ResultCollector resultCollector) {
        boolean result = true;
        for (Validator delegate : delegates) {
            result = delegate.validate(reference, contents, resultCollector) & result;
        }
        return result;
    }
}
