package dk.statsbiblioteket.newspaper.metadatachecker.crosscheck;

public class FuzzyDate implements Comparable<FuzzyDate> {
    private final String dateString;

    public FuzzyDate(String dateString) {
        validateFormat(dateString);
        this.dateString = dateString;
    }

    public int compareTo(FuzzyDate date) {
        int myPrecision = findPrecisionIndex(this.dateString);
        int theirPrecision = findPrecisionIndex(date.asString());
        int minPrecisionIndex = Math.min(myPrecision, theirPrecision);
        return this.dateString.substring(0, minPrecisionIndex).compareTo(date.asString().substring(0, minPrecisionIndex));
    }

    public String asString() {
        return dateString;
    }

    private static int findPrecisionIndex(String date) {
        final int MONTH_START_INDEX = 4;
        final int FULL_PRECISION_INDEX = 9;
        int datePrecisionIndex = date.indexOf("00", MONTH_START_INDEX);
        return datePrecisionIndex != -1 ? datePrecisionIndex : FULL_PRECISION_INDEX;
    }

    private void validateFormat(String dateString) {
        //ToDo implement
    }
}
