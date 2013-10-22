package dk.statsbiblioteket.newspaper.metadatachecker.jpylyzer;

import org.w3c.dom.Document;

/** Use this class to create new instances of the Validator */
public class ValidatorFactory {


    private final Document controlPolicies;

    /**
     * Construct a new factory from the given control policies
     * @param controlPolicies rdf scape control policies detailing the things to validate
     *
     */
    public ValidatorFactory(Document controlPolicies) {
        this.controlPolicies = controlPolicies;
        ;
    }

    /**
     * Create a validator
     *
     * @return a new validator
     */
    public Validator createValidator() {
        return new DelegatingValidator(new SchemaValidator("jpylizer.xsd"),
                                       new SchematronValidator("sb-jp2.sch"));

    }
}
