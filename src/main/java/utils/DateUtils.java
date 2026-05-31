package utils;

import java.time.LocalDate;

public class DateUtils {

    public static String today() {
        return LocalDate.now().toString();
    }

}
