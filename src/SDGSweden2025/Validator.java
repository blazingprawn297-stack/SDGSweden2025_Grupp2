package SDGSweden2025;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public class Validator {

    // ---------- Grund ----------

    public static boolean isEmpty(String input) {
        return input == null || input.trim().isEmpty();
    }

    public static boolean isNotEmpty(String input) {
        return !isEmpty(input);
    }

    public static boolean hasValidLength(String input, int min, int max) {
        return input != null && input.length() >= min && input.length() <= max;
    }

    public static boolean hasMinLength(String input, int min) {
        return isNotEmpty(input) && input.length() >= min;
    }

    public static boolean hasMaxLength(String input, int max) {
        return isNotEmpty(input) && input.length() <= max;
    }

    // ---------- Namn / text ----------

    // Bokstäver (inkl åäö) + mellanslag + bindestreck
    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[a-zA-ZåäöÅÄÖ\\s\\-]+$");

    public static boolean isValidName(String name) {
        return isNotEmpty(name) && NAME_PATTERN.matcher(name.trim()).matches();
    }

    // Fritext som får vara tom, men om den är ifylld ska den hålla maxlängd
    public static boolean isValidOptionalText(String text, int maxLength) {
        return isEmpty(text) || text.length() <= maxLength;
    }

    // Fritext som måste vara ifylld och hålla maxlängd
    public static boolean isValidRequiredText(String text, int maxLength) {
        return isNotEmpty(text) && text.length() <= maxLength;
    }

    // ---------- E-post ----------

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public static boolean isValidEmail(String email) {
        return isNotEmpty(email) && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    // ---------- Telefon ----------

    // Tillåt siffror, mellanslag, bindestreck och + (t.ex. +46)
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[0-9\\s\\-\\+]{6,20}$");

    // Telefon som är frivillig: tomt är OK, annars måste format vara OK
    public static boolean isValidOptionalPhone(String phone) {
        return isEmpty(phone) || PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    // Telefon som är obligatorisk
    public static boolean isValidPhone(String phone) {
        return isNotEmpty(phone) && PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    // ---------- Status ----------

    public static boolean isValidStatus(String input) {
        if (isEmpty(input)) return false;

        String[] statuses = {"pågående", "avslutat", "planerat"};
        for (String s : statuses) {
            if (s.equalsIgnoreCase(input.trim())) {
                return true;
            }
        }
        return false;
    }

    // ---------- Tal ----------

    public static boolean isPositiveInteger(String input) {
        try {
            int number = Integer.parseInt(input.trim());
            return number > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isNonNegativeInteger(String input) {
        try {
            int number = Integer.parseInt(input.trim());
            return number >= 0;
        } catch (Exception e) {
            return false;
        }
    }

    // ---------- Datum (YYYY-MM-DD) ----------

    public static boolean isValidIsoDate(String input) {
        if (isEmpty(input)) return false;
        try {
            LocalDate.parse(input.trim()); // yyyy-MM-dd
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    // Frivilligt datum: tomt är OK, annars måste det vara giltigt
    public static boolean isValidOptionalIsoDate(String input) {
        return isEmpty(input) || isValidIsoDate(input);
    }

    // Slutdatum får inte vara före startdatum
    public static boolean isValidDateRange(String startDate, String endDate) {
        if (!isValidIsoDate(startDate) || !isValidIsoDate(endDate)) return false;

        LocalDate start = LocalDate.parse(startDate.trim());
        LocalDate end = LocalDate.parse(endDate.trim());
        return !end.isBefore(start);
    }
}
