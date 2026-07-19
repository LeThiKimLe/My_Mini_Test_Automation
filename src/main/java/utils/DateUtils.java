package utils;

import java.time.LocalDate;

public class DateUtils {

    public static String today() {
        return LocalDate.now().toString();
    }

    public static String getCurrentTimeWithSpecificFormat(String format) {
        return java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern(format));
    }

}
