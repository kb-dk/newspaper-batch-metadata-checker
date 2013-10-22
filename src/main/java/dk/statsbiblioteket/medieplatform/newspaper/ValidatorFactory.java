package dk.statsbiblioteket.medieplatform.newspaper;

import org.w3c.dom.Document;

import java.util.Properties;

/** Use this class to create new instances of the Validator */
public class ValidatorFactory {


    private final Document controlPolicies;
    private final Properties properties;

    /**
     * Construct a new factory from the given control policies
     * @param controlPolicies rdf scape control policies detailing the things to validate
     * @param properties
     */
    public ValidatorFactory(Document controlPolicies,
                            Properties properties) {
        this.controlPolicies = controlPolicies;
        this.properties = properties;
    }

    /**
     * Create a validator
     *
     * @return a new validator
     */
    public Validator createValidator() {
        return new DelegatingValidator(new SchemaValidator("jpylizer.xsd",properties),
                                       new SchematronValidator("sb-jp2.sch",properties));

    }
}
