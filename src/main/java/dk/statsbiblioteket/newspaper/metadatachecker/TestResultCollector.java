package dk.statsbiblioteket.newspaper.metadatachecker;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;

import java.util.HashSet;
import java.util.Set;

/** Test result collector that remembers information */
public class TestResultCollector extends ResultCollector {
    Set<String> failures = new HashSet<>();

    public TestResultCollector(String componentName, String componentVersion) {
        super(componentName, componentVersion);
    }

    @Override
    public void addFailure(String reference, String type, String component, String description) {
        super.addFailure(reference, type, component,
                         description);    //To change body of overridden methods use File | Settings | File Templates.
        failures.add(description);
    }

    @Override
    public void addFailure(String reference, String type, String component, String description, String... details) {
        super.addFailure(reference, type, component, description,
                         details);    //To change body of overridden methods use File | Settings | File Templates.
        failures.add(description);
    }
}
