package dk.statsbiblioteket.newspaper.metadatachecker.film;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Implements functionality for working with ISO format dates without full precision. This means the dates can be
 * instantiated with the follow formats: <ol>
 *     <li>Full precision: yyyy-MM-dd.</li>
 *     <li>Month precision: yyyy-MM-00.</li>
 *     <li>Year precision: yyyy-00-00</li>
 *      <li>Month precision: yyyy-MM</li>
 *     <li>Year precision: yyyy.</li>
 * </ol>
 */
public final class FuzzyDate implements Comparable<FuzzyDate> {
    private final String dateString;
    private final int myPrecision;

    public FuzzyDate(String dateString) {
        validateFormat(dateString);
        this.dateString = dateString;
        myPrecision = findPrecisionIndex(this.dateString);
    }

    /**
     * 0 if the argument Date is equal to this Date; a value less than 0 if this Date is before the Date argument and a
     * value greater than 0 if this Date is after the Date argument.<p>
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
        int minPrecisionIndex = Math.min(myPrecision, date.getPrecision());
        return asString(minPrecisionIndex).compareTo(date.asString(minPrecisionIndex));
    }


    /**
     * 0 if the argument Date is equal to this Date; a value less than 0 if this Date is before the Date argument and a
     * value greater than 0 if this Date is after the Date argument.<p>
     * @param date the date to compare this to
     */
    public int compareTo(Date date)  {
        DateFormat dateFormat = getDateFormat();
        Date thisAsDate = null;
        Date thatDate;
        try {
            thisAsDate = dateFormat.parse(asString());
            thatDate = dateFormat.parse(dateFormat.format(date));
        } catch (ParseException e) {
            throw new IllegalArgumentException("The datestring '"+dateString+"' is invalid",e);
        }
        return thisAsDate.compareTo(thatDate);
    }

    /**
     * return whether this fuzzy date is before the given fuzzy date.
     */
    public boolean before(FuzzyDate fuzzyDate) {
        return getMinDate().before(fuzzyDate.getMinDate());
    }

    /**
     * return whether this fuzzy date is after the given fuzzy date.
     */
    public boolean after(FuzzyDate fuzzyDate) {
        return getMaxDate().after(fuzzyDate.getMaxDate());
    }

    /**
     * Get the appropriate date format, according to the precision of the date string
     * @return a date format
     */
    private DateFormat getDateFormat() {
        if (getPrecision() > 9 ){
            return new SimpleDateFormat("yyyy-MM-dd");
        } else if (getPrecision() > 5 ){
            return new SimpleDateFormat("yyyy-MM");
        } else {
            return new SimpleDateFormat("yyyy");

        }
    }

    /**
     * Return the string value of this fuzzy date.
     */
    public String asString() {
        return dateString;
    }

    /**
     * Return the string value of this fuzzy date, where the characters outside of the indicated index are thrown away.
     * @param precisionIndex Specifies the precision the string should be trimmed to.
     */
    protected String asString(int precisionIndex) {
        return dateString.substring(0, precisionIndex);
    }

    /**
     * Will calculate the index for the date string, where the rest of the string is just padding '00's.
     */
    protected static int findPrecisionIndex(String date) {
        final int MONTH_START_INDEX = 4;
        int datePrecisionIndex = date.indexOf("00", MONTH_START_INDEX);
        return datePrecisionIndex != -1 ? datePrecisionIndex : date.length();
    }

    protected int getPrecision() {
        return myPrecision;
    }

    private void validateFormat(String dateString) {
        String[] dateParts= dateString.split("-");
        try {
        Integer.parseInt(dateParts[0]);
        if (Integer.parseInt(dateParts[0]) > 9999) {
            throw new IllegalArgumentException("Year part of date string can not be more than 9999, was " + dateParts[0]);
        }
        if (dateParts.length >= 2 && Integer.parseInt(dateParts[1]) > 12) {
            throw new IllegalArgumentException("Month part of date string can not be more than 12, was " + dateParts[1]);
        }
        if (dateParts.length >= 3 && Integer.parseInt(dateParts[2]) > 31) {
            throw new IllegalArgumentException("Month part of date string can not be more than 31, was " + dateParts[2]);
        }
        } catch (Throwable t) {
            throw new IllegalArgumentException("Invalid date format " + dateString +
                    ", the date must be of the format yyyy-MM-dd (-MM or -MM-dd are optional)");
        }
    }

    /**
     * Return the earliest date this fuzzy date can represent.
     * @return
     */
    public Date getMinDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            if (getPrecision() > 9 ){
                 return sdf.parse(dateString);
            } else if (getPrecision() > 5 ){
                //return sdf.parse(dateString.replace("-00", "-01"));
                return sdf.parse(dateString.substring(0,7) + "-01");
            } else {
                //return sdf.parse(dateString.replace("-00-00", "-01-01"));
                return sdf.parse(dateString.substring(0,4) + "-01-01");
            }
        } catch (ParseException e) {
            throw new IllegalArgumentException("Unparseable date", e);
        }
    }

    /**
     * Return the latest date this fuzzy date can represent. For non-precise dates, the algorithm is
     * to find a nextDate which is the date immediately after this fuzzy date and then roll it back 24 hours.
     * @return
     */
    public Date getMaxDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Long oneDayMs = 24*3600*1000L;
        try {
            if (getPrecision() > 9 ){
                return sdf.parse(dateString);
            } else if (getPrecision() > 5 ){
                if (getMonth() == 12) {
                    return sdf.parse(dateString.substring(0,4) + "-12-31"); //Special case in december
                } else {
                    Date nextDate = new Date(getYear() - 1900 , getMonth(), 1); //1st date of next month, same year. "getMonth" because of 0-month
                    Date actualDate = new Date(nextDate.getTime() - oneDayMs );
                    return actualDate;
                }
            } else {
                Date nextDate = new Date(getYear()+1-1900, 0, 1);  //1st January next year
                Date actualDate = new Date(nextDate.getTime() - oneDayMs );
                return actualDate;
            }
        } catch (ParseException e) {
            throw new IllegalArgumentException("Unparseable date", e);
        }
    }

    private int getYear() {
        return Integer.parseInt(dateString.substring(0,4));
    }

    private int getMonth() {
        if (myPrecision > 5) {
            return Integer.parseInt(dateString.substring(5,7));
        } else {
            return 0;
        }
    }

    private int getDate() {
        if (myPrecision > 9) {
        return Integer.parseInt(dateString.substring(9));
        } else {
            return 0;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FuzzyDate)) {
            return false;
        }

        FuzzyDate fuzzyDate = (FuzzyDate) o;

        if (myPrecision != fuzzyDate.myPrecision) {
            return false;
        }
        if (!dateString.equals(fuzzyDate.dateString)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = dateString.hashCode();
        result = 31 * result + myPrecision;
        return result;
    }
}
