package com.stimasoft.obiectivecva.utils;

import android.util.Log;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class used to validate text input
 */

public class Validator {

    /**
     * CNP Validation. Checks the blocks of the CNP and does the math.
     *
     * @param unCnp The CNP to check
     * @return Returns <b>true</b> if the CNP is valid.
     * <p>Returns <b>false</b> if the CNP is invalid
     */
    public boolean cnpText(String unCnp) {
        Log.d("DBG", "Read CNP: " + unCnp);

        //Regular expression setup
        //Sex   |Year|    Month     |          Zi          |               Judet             | Ultimele 4
        String regEx = "\\b[1-9]\\d{2}(0[1-9]|1[0-2])(0[1-9]|[1-2]\\d|3[0-1])(0[1-9]|[1-4]\\d|5[0-2]|99|88|77)\\d{4}\\b";
        Pattern pattern = Pattern.compile(regEx);
        Matcher match = pattern.matcher(unCnp);

        Log.d("DBG", "CNP matcher returned " + match.matches());

        if (match.matches()) //CNP regex was ok
        {
            int verification[] = {2, 7, 9, 1, 4, 6, 3, 5, 8, 2, 7, 9}; //Numbers courtesy of http://ro.wikipedia.org/wiki/Cod_numeric_personal#C
            //Control number calculation variables
            int sum = 0;
            int rest;
            int control;

            //Control number calculation//
            for (int i = 0; i < verification.length; i++) {
                sum += verification[i] * Character.getNumericValue(unCnp.charAt(i));
            }

            rest = sum % 11;

            if (rest == 10)
                control = 1;
            else
                control = rest;

            Log.d("DBG", "Control a dat: " + Integer.toString(control));
            Log.d("DBG", "Ce e la sfarsitu string-ului: " + unCnp.charAt(unCnp.length() - 1));

            if (control == Character.getNumericValue(unCnp.charAt(unCnp.length() - 1))) {
                Log.d("DBG", "CNP-u e bun");
                return true;
            } else
                return false;
            //////////////////////////////
        }
        return false;
    }

    /**
     * General name validation. Checks if name starts with upper case and other minor elements.
     *
     * @param name The name to check
     * @return Returns <b>true</b> if the name is valid.
     * <p>Returns <b>false</b> if the name is invalid.
     */
    public boolean nameText(String name) {
        //1 name minimum|one or more extra names separated by dashes|
        String regEx = "([A-Z][a-z]+)(\\s?-\\s?([A-Z][a-z]+))?";
        Pattern pattern = Pattern.compile(regEx);
        Matcher match = pattern.matcher(name);

        Log.d("DBG", "Name matcher returned " + match.matches());

        return match.matches();

    }

    /**
     * General telephone number validation.
     *
     * @param phone The phone number to check
     * @return Returns <b>true</b> if the phone number is valid.
     * <p>Returns <b>false</b> if the phone number is invalid.
     */
    public boolean phoneText(String phone) {
        //Max 1 plus sign| Any symbol |x or ext with max 1 space |  Any symbol  |
        String regEx = "[+]?([\\d\\s()./-])*(\\s?(x|ext)?)?([\\d()./-])*";

        Pattern pattern = Pattern.compile(regEx);
        Matcher match = pattern.matcher(phone);

        Log.d("DBG", "Phone matcher returned " + match.matches());

        return match.matches();
    }

    /**
     * General age validation. Doesn't allow people to live thousands of years
     *
     * @param age The age to check
     * @return Returns <b>true</b> if the age is valid.
     * <p>Returns <b>false</b> if the age is invalid.
     */
    public boolean ageText(String age) {
        String regEx = "\\d{1,3}";

        Pattern pattern = Pattern.compile(regEx);
        Matcher match = pattern.matcher(age);

        Log.d("DBG", "Age matcher returned " + match.matches());

        return match.matches();
    }

    /**
     * General income validation. Allows for values of "boolean" format
     *
     * @param income The income to check
     * @return Returns <b>true</b> if the income is valid.
     * <p>Returns <b>false</b> if the income is invalid.
     */
    public boolean salaryText(String income) {
        String regEx = "\\d+[,.]?\\d*";

        Pattern pattern = Pattern.compile(regEx);
        Matcher match = pattern.matcher(income);

        Log.d("DBG", "Salary matcher returned " + match.matches());

        return match.matches();
    }

    /**
     * General ethnicity validation.
     *
     * @param ethnicity The ethnicity to check
     * @return Returns <b>true</b> if the ethnicity is valid.
     * <p>Returns <b>false</b> if the ethnicity is invalid.
     */
    public boolean ethnicityText(String ethnicity) {
        //1 name minimum|one or more extra names separated by dashes|
        String regEx = "([A-Z][a-z]+)(\\s?-\\s?([A-Z][a-z]+))?";
        Pattern pattern = Pattern.compile(regEx);
        Matcher match = pattern.matcher(ethnicity);

        Log.d("DBG", "Ethnicity matcher returned " + match.matches());

        return match.matches();

    }

    /**
     * General password validation. Password rules are in development.
     * <p>As of 19.12.2014 this validation only checks for strings larger than 8 characters</p>
     *
     * @param password The password to check
     * @return Returns <b>true</b> if the password is valid.
     * <p>Returns <b>false</b> if the password is invalid.
     */
    public boolean passwordText(String password) {
        return password.length() >= 8;
    }

    /**
     * General username validation. username rules are in development.
     * <p>As of 19.12.2014 this validation only checks for strings larger than 0 characters</p>
     *
     * @param username The username to check
     * @return Returns <b>true</b> if the username is valid.
     * <p>Returns <b>false</b> if the username is invalid.
     */
    public boolean userNameText(String username) {
        return username.length() != 0;
    }

    /**
     * Checks if any text exists.
     *
     * @param text The text to check
     * @return Returns <b>true</b> if the text exists.
     * <p>Returns <b>false</b> if the text does not exist.
     */
    public boolean existsText(String text) {
        return text.length() != 0;

    }

    public boolean isNumeric(String input) {

        try {
            int num = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    /**
     * Checks if a CUI is valid or not
     *
     * @param cui The CUI code to be validated
     * @return <code>true</code> if CUI is valid <br> <code>false</code> if it is not
     */
    public boolean cuiIsValid(String cui) {
        // Algorithm used: https://ro.wikipedia.org/wiki/Cod_de_Identificare_Fiscal%C4%83

        String testKey = "753217532";

        // Check if cui matches pattern
        String regEx = "([A-Z][A-Z])*([0-9]{1,9})([0-9])";
        Pattern pattern = Pattern.compile(regEx);
        Matcher match = pattern.matcher(cui);

        if (!match.matches()) {
            return false;
        }

        // Check if cui math is ok
        // Split cui in the [ |ZZZZZZZZZ| ] |C| format
        ArrayList<String> strings = new ArrayList<String>();
        match.reset();
        if (match.find())
            for (int i = 1; i <= match.groupCount(); i++) {
                strings.add(match.group(i));
            }

        // Reverse the ZZZZZZZZZ code and the test key in order to start calculating
        String reversedOrderNumber = new StringBuilder(strings.get(1)).reverse().toString();
        String reversedTestKey = new StringBuilder(testKey).reverse().toString();

        int sum = 0;

        // Each figure from the ZZZZZZZZZ code and the corresponding testkey figure is added after multiplication
        for(int i = 0; i < reversedOrderNumber.length(); i++){
            sum += Character.getNumericValue(reversedOrderNumber.charAt(i)) *
                    Character.getNumericValue(reversedTestKey.charAt(i));
        }

        // Sum is multiplied by 10
        sum *= 10;

        // The rest of the division to 11
        int verificationChar = sum % 11;

        // The figure 10 doesn't exist so we will use 0
        if(verificationChar == 10) verificationChar = 0;

        // Final check, if the calculated verification figure matches the given one, CIF is valid
        return verificationChar == Integer.parseInt(strings.get(2));

    }

    /**
     * Checks if a NR_RC is valid or not
     *
     * @param nrRc The NR_RC code to be validated
     * @return <code>true</code> if NR_RC is valid <br> <code>false</code> if it is not
     */
    public boolean nrRcIsValid(String nrRc){
        //Check is based on the following nrRc format: J12/34567.../dd.mm.yyyy

        String regEx = "(?:(?:[FJ][0-9]{2})\\/(?:[0-9]+)\\/)(?:(?:(?:31(\\/|-|\\.)(?:0?[13578]|1[02]))\\1|(?:(?:29|30)(\\/|-|\\.)(?:0?[1,3-9]|1[0-2])\\2))(?:(?:1[6-9]|[2-9]\\d)?\\d{2})$|(?:29(\\/|-|\\.)0?2\\3(?:(?:(?:1[6-9]|[2-9]\\d)?(?:0[48]|[2468][048]|[13579][26])|(?:(?:16|[2468][048]|[3579][26])00))))$|(?:0?[1-9]|1\\d|2[0-8])(\\/|-|\\.)(?:(?:0?[1-9])|(?:1[0-2]))\\4(?:(?:1[6-9]|[2-9]\\d)?\\d{2})|(?:[12][0-9]{3}))";
        Pattern pattern = Pattern.compile(regEx);
        Matcher match = pattern.matcher(nrRc);

        return match.matches();

    }
}
