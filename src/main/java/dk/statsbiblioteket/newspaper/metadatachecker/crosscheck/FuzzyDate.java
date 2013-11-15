package dk.statsbiblioteket.newspaper.metadatachecker.crosscheck;

/**
 * Implements functionality for working with ISO format dates without full precision. This means the dates can be
 * instantiate with the follow formats: <ol>
 *     <li>Full precision: yyyy-MM-dd.</li>
 *     <li>Month precision: yyyy-MM-00.</li>
 *     <li>Year precision: yyyy-00-00.</li>
 * </ol>
 */
public class FuzzyDate implements Comparable<FuzzyDate> {
    private final String dateString;

    public FuzzyDate(String dateString) {
        validateFormat(dateString);
        this.dateString = dateString;
    }

    /**
     * Return 1 if the supplied date is before this date, 0 if equal to and -1 if after. This is done by
     * converting the dates to dates with the least precision of the two dates and comparing these.<p>
     *     Examples: <ol>
     *        <li>FuzzyDate(2012-03-03).compareTo(FuzzyDate(2012-03-02)) will return 1.</li>
     *        <li>FuzzyDate(2012-03-00).compareTo(FuzzyDate(2012-03-02)) will return 0.</li>
     *        <li>FuzzyDate(2012-03-03).compareTo(FuzzyDate(2012-03-00)) will return 0.</li>
     *        <li>FuzzyDate(2012-03-00).compareTo(FuzzyDate(2012-00-00)) will return -1.</li>
     *     </ol>
     * </p>
     * @param date The date to compare this date to.
     */
    public int compareTo(FuzzyDate date) {
        int myPrecision = findPrecisionIndex(this.dateString);
        int theirPrecision = findPrecisionIndex(date.asString());
        int minPrecisionIndex = Math.min(myPrecision, theirPrecision);
        return this.dateString.substring(0, minPrecisionIndex).compareTo(
                date.asString().substring(0, minPrecisionIndex));
    }

    /**
     * Return the string value of this fuzzy date.
     */
    public String asString() {
        return dateString;
    }

    /**
     * Will calculate the index for the date string, where the rest of the string is just padding '00's.
     */
    private static int findPrecisionIndex(String date) {
        final int MONTH_START_INDEX = 4;
        final int FULL_PRECISION_INDEX = 10;
        int datePrecisionIndex = date.indexOf("00", MONTH_START_INDEX);
        return datePrecisionIndex != -1 ? datePrecisionIndex : FULL_PRECISION_INDEX;
    }

    private void validateFormat(String dateString) {
        String[] dateParts= dateString.split("-");
        try {
        assert dateParts.length == 3;
        Integer.parseInt(dateParts[0]);
        assert Integer.parseInt(dateParts[1]) <= 12;
        assert Integer.parseInt(dateParts[2]) <= 31;
        } catch (Throwable t) {
            throw new RuntimeException("Invalide date format " + dateString +
                    ", the date must be of the format yyyy-MM-dd");
        }
    }
}
