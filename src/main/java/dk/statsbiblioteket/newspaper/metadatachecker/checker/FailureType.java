package dk.statsbiblioteket.newspaper.metadatachecker.checker;

/**
 * Defines the types of failures which can be reported through the <code>ResultCollector</code>.
 */
public enum FailureType {
    METADATA("metadata");

    private final String value;

    FailureType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static FailureType fromValue(String v) {
        for (FailureType c: FailureType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
