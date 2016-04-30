package es.dmoral.tappic.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by grender on 29/04/16.
 */
public class TooltipperUtils {

    public static String getStringFromRegex(String fullString, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(fullString);
        return matcher.find() ? matcher.group(0) : "";
    }

}
