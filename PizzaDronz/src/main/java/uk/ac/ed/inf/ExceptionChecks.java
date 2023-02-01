package uk.ac.ed.inf;

public class ExceptionChecks {
    /**
     * The function that check if the given date is valid.
     * @param Date the data we need to process data with.
     * @return weather the date is right or wrong
     */
    public static boolean checkDate(String Date){
        if (Date.length() != 10){
            System.err.println("The date provided is wrong");
            return false;
        }
        if (!(Character.isDigit(Date.charAt(8)) && Character.isDigit(Date.charAt(9)))){
            System.err.println("The date provided is wrong");
            return false;
        }
        if ((Date.startsWith("2023-01-")) || (Date.startsWith("2023-03-")) || (Date.startsWith("2023-05-"))){
            if ((Date.charAt(8) - '0' > 3)){
                System.err.println("The date provided is wrong");
                return false;
            }
            if ((Date.charAt(8) - '0' == 3) && ( Date.charAt(9) - '0' > 1)){
                System.err.println("The date provided is wrong");
                return false;
            }
            return true;
        }
        if ((Date.startsWith("2023-02-"))){
            if (Date.charAt(8) - '0' > 2){
                System.err.println("The date provided is wrong");
                return false;
            }
            if (( Date.charAt(8) - '0' == 2) && ( Date.charAt(9) - '0' > 8)){
                System.err.println("The date provided is wrong");
                return false;
            }
            return true;
        }
        if ((Date.startsWith("2023-04-"))){
            if (Date.charAt(8) - '0' > 3){
                System.err.println("The date provided is wrong");
                return false;
            }
            if ((Date.charAt(8) - '0' == 3) && ( Date.charAt(9) - '0' > 0)){
                System.err.println("The date provided is wrong");
                return false;
            }
            return true;
        }
        System.err.println("We only have the data from 2023-01-01 to 2023-05-31");
        return false;
    }
}
