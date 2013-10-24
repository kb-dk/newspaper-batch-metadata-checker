package dk.statsbiblioteket.newspaper.metadatachecker.jpylyzer;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;

/**
 * This is the validator interface. It should be used to validate a file against a set check
 */
public interface AttributeValidator {

    /**
     * Validate the contents of the inputstream. Any problems are reported to the resultCollector. The reference
     * is the name of the file or resource. It will be used for reporting failures, but is not part of the thing
     * being validated.
     * The method returns true if no errors were found.
     * @param reference the "name" of the resource being validated
     * @param contents the contents to validate
     * @param resultCollector the result collector to collect the validation errors
     * @return true of no errors were found
     */
    public boolean validate(String reference, byte[] contents, ResultCollector resultCollector);
}
