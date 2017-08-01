package vcf;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Created by uichuimi on 11/11/16.
 */
public class ValueUtils {

	private static final String NULL_VALUE = ".";
	private static final String VALUE_SEPARATOR = ",";
	private static final DecimalFormat DECIMAL_FORMAT =
			new DecimalFormat("0.0##",
					DecimalFormatSymbols.getInstance(Locale.US));

	/**
	 * Get the most precise value for the String passed by argument.
	 * This is equivalent to <code>getValue(value, type, ",", ".")</code>
	 *
	 * @param value a string with the raw representation of the value
	 * @param type  integer, int, long, float, double, boolean, flag, string, text, character or char
	 * @return an object or array of objects with a more precise representation (list, tpye) of the value
	 */
	public static Object getValue(String value, String type) {
		return getValue(value, type, VALUE_SEPARATOR, NULL_VALUE);
	}

	/**
	 * Get the most precise value for the String passed by argument. Depending on the type (case insensitive) it will
	 * create:
	 * <p>
	 * <table border=1>
	 * <caption>Types table</caption>
	 * <tr>
	 * <th>Source type</th>
	 * <th>Java type</th>
	 * </tr>
	 * <tr>
	 * <td>integer, int</td>
	 * <td>Integer</td>
	 * </tr>
	 * <tr>
	 * <td>long</td>
	 * <td>Long</td>
	 * </tr>
	 * <tr>
	 * <td>double, float</td>
	 * <td>Double</td>
	 * </tr>
	 * <tr>
	 * <td>boolean, flag</td>
	 * <td>Boolean</td>
	 * </tr>
	 * <tr>
	 * <td>text, string, character, char</td>
	 * <td>String</td>
	 * </tr>
	 * </table>
	 * <p>
	 * If type is not recognized, String or String[] will be returned.
	 * <p>
	 * An array is returned when sep is found in value. value is split by sep and each element is converted with
	 * <code>getValue()</code>
	 *
	 * @param value a string with the raw representation og the value
	 * @param type  integer, int, long, float, double, boolean, flag, string, text, character or char
	 * @return an object or array of objects with a more precise representation (list, tpye) of the value
	 */
	public static Object getValue(String value, String type, String sep, String nullValue) {
		if (value.equals(nullValue)) return null;
		if (value.contains(sep))
			return Arrays.stream(value.split(sep))
					.map(val -> getValue(val, type, sep, nullValue)).collect(Collectors.toList()).toArray();
		else
			try {
				switch (type.toLowerCase()) {
					case "integer":
					case "int":
						return Integer.valueOf(value);
					case "long":
						return Long.valueOf(value);
					case "float":
					case "double":
						return Double.valueOf(value);
					case "boolean":
					case "flag":
						return value.matches("(?iu)yes|true|ok");
					case "string":
					case "text":
					case "character":
					case "char":
					default:
						return value;
				}
			} catch (Exception ex) {
				return value;
			}

	}

	/**
	 * Returns the String representation of the value passed by argument.
	 * <p>
	 * If value is an array, it is returned as <code>String.join(sep, value)</code>, and each object inside value is
	 * then transformed to String.
	 * <p>
	 * The method used to transform to String is <code>String.valueOf(object)</code>.
	 *
	 * @param value any object or array
	 * @return the string representation of value
	 */
	public static String getString(Object value, String sep) {
		return value == null ? NULL_VALUE
				: isArray(value)
				? String.join(sep, Arrays.stream((Object[]) value).map(String::valueOf).collect(Collectors.toList()))
				: Double.class.isAssignableFrom(value.getClass())
				? DECIMAL_FORMAT.format(value)
				: String.valueOf(value);
	}

	/**
	 * Returns the String representation of the value passed by argument. This is the same as
	 * <code>getString(value, ",")</code>.
	 *
	 * @param value any object or array
	 * @return the string representation of value
	 */
	public static String getString(Object value) {
		return getString(value, VALUE_SEPARATOR);
	}


	/**
	 * Returns <code>Object[].class.isAssignableFrom(object.getClass())</code>
	 */
	public static boolean isArray(Object object) {
		return Object[].class.isAssignableFrom(object.getClass());
	}

}
