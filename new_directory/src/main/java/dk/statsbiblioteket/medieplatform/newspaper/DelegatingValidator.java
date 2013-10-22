package dk.statsbiblioteket.medieplatform.newspaper;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Delegating Validator. This class allows you to chain the invocation of several validators as a single validator
 */
public class DelegatingValidator
        implements Validator {

    List<Validator> delegates = new ArrayList<>();

    /**
     * Create a Delegating Validator with a list of validators to delegate to. They will be invoked in the given order
     * @param delegate the delegates
     */
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
}
