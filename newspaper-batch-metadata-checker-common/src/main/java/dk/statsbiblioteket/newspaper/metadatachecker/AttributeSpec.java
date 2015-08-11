package dk.statsbiblioteket.newspaper.metadatachecker;

/**
 * This is the config for the more generic checkers, namely the SchematronValidatorEventHandler and the SchemaValidatorEventHandler
 * @see SchematronValidatorEventHandler
 * @see SchemaValidatorEventHandler
 */
public class AttributeSpec {

    private String postfix;
    private String xsdFile;
    private String schematronFile;
    private String messagePrefix;
    private String type;

    /**
     * Create a new attribute spec
     * @param postfix Every file with this postfix will be validated with the event handlers
     * @param xsdFile the Xml Schema to validate the file against. If null, no schema validation will be done
     * @param schematronFile the Schematron document to validate the file against. If null, no schematron validation will be done
     * @param messagePrefix For schema validation, this is the prefix to use when reporting errors.
     * @param type The type of error, usually "metadata" or "jp2file", but can be whatever
     */
    public AttributeSpec(String postfix, String xsdFile, String schematronFile, String messagePrefix, String type) {
        this.postfix = postfix;
        this.xsdFile = xsdFile;
        this.schematronFile = schematronFile;
        this.messagePrefix = messagePrefix;
        this.type = type;
    }

    /**
     * The postfix for the files to be validated with the specification in this AttributeSpec
     * @return
     */
    public String getPostfix() {
        return postfix;
    }

    /**
     * The Xml Schema file path to the schema to validate against
     * @return
     */
    public String getXsdFile() {
        return xsdFile;
    }

    /**
     * The Schematron file path to the schematron document to validate against
     * @return
     */
    public String getSchematronFile() {
        return schematronFile;
    }

    /**
     * The prefix for the schema, but not schematron, errors reported
     * @return
     */
    public String getMessagePrefix() {
        return messagePrefix;
    }

    /**
     * The type of errors reported. Should correspond to the type of file being validated.
     * @return
     */
    public String getType() {
        return type;
    }
}
