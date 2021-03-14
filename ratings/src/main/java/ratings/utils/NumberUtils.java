package ratings.utils;

import java.util.regex.Pattern;

public class NumberUtils {

	private static final Pattern PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");

	public static boolean isNumeric(String str) {
	    return str == null ? false : PATTERN.matcher(str).matches();
	}

	public static float toFixed(float number, int scale) {
		int pow = (int) Math.pow(10, scale);
		float tmp = number * pow;
		return (float)(int) ((tmp - (int) tmp) >= 0.5f ? tmp + 1 : tmp) / pow;
	}

	public static int getInteger(String value) {
    	try {
    		return Integer.valueOf(value);
    	} catch (NumberFormatException e) {
    		return 0;
    	}
    }
}
