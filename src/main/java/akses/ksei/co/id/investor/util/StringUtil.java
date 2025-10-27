package akses.ksei.co.id.investor.util;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class StringUtil {

	private StringUtil() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static String removeChars(String s, char... oldSubs) {
		if (s == null) {
			return null;
		}

		if (oldSubs == null) {
			return s;
		}

		StringBuilder sb = new StringBuilder(s.length());

		iterate: for (int i = 0; i < s.length(); i++) {
			char c1 = s.charAt(i);

			for (char c2 : oldSubs) {
				if (c1 == c2) {
					continue iterate;
				}
			}

			sb.append(c1);
		}

		if (s.length() == sb.length()) {
			return s;
		}

		return sb.toString();
	}

	public static String[] split(String s) {
		return split(s, CharPool.COMMA);
	}

	public static boolean[] split(String s, boolean x) {
		return split(s, StringPool.COMMA, x);
	}

	public static String[] split(String s, char delimiter) {
		if (Validator.isNull(s)) {
			return _EMPTY_STRING_ARRAY;
		}

		s = s.trim();

		if (s.isEmpty()) {
			return _EMPTY_STRING_ARRAY;
		}

		List<String> nodeValues = new ArrayList<>();

		_split(nodeValues, s, 0, delimiter);

		return nodeValues.toArray(new String[0]);
	}

	private static void _split(Collection<String> values, String s, int offset, char delimiter) {

		int pos = s.indexOf(delimiter, offset);

		while (pos != -1) {
			values.add(s.substring(offset, pos));

			offset = pos + 1;

			pos = s.indexOf(delimiter, offset);
		}

		if (offset < s.length()) {
			values.add(s.substring(offset));
		}
	}

	public static double[] split(String s, double x) {
		return split(s, StringPool.COMMA, x);
	}

	public static float[] split(String s, float x) {
		return split(s, StringPool.COMMA, x);
	}

	public static int[] split(String s, int x) {
		return split(s, StringPool.COMMA, x);
	}

	public static long[] split(String s, long x) {
		return split(s, StringPool.COMMA, x);
	}

	public static short[] split(String s, short x) {
		return split(s, StringPool.COMMA, x);
	}

	public static String[] split(String s, String delimiter) {
		if (Validator.isNull(s) || (delimiter == null) || delimiter.equals(StringPool.BLANK)) {

			return _EMPTY_STRING_ARRAY;
		}

		s = s.trim();

		if (s.equals(delimiter)) {
			return _EMPTY_STRING_ARRAY;
		}

		if (delimiter.length() == 1) {
			return split(s, delimiter.charAt(0));
		}

		List<String> nodeValues = new ArrayList<>();

		int offset = 0;

		int pos = s.indexOf(delimiter, offset);

		while (pos != -1) {
			nodeValues.add(s.substring(offset, pos));

			offset = pos + delimiter.length();

			pos = s.indexOf(delimiter, offset);
		}

		if (offset < s.length()) {
			nodeValues.add(s.substring(offset));
		}

		return nodeValues.toArray(new String[0]);
	}

	public static boolean[] split(String s, String delimiter, boolean x) {
		String[] array = split(s, delimiter);

		boolean[] newArray = new boolean[array.length];

		for (int i = 0; i < array.length; i++) {
			boolean value = x;

			try {
				Boolean booleanValue = Boolean.valueOf(array[i]);

				value = booleanValue.booleanValue();
			} catch (Exception e) {
			}

			newArray[i] = value;
		}

		return newArray;
	}

	public static double[] split(String s, String delimiter, double x) {
		String[] array = split(s, delimiter);

		double[] newArray = new double[array.length];

		for (int i = 0; i < array.length; i++) {
			double value = x;

			try {
				value = Double.parseDouble(array[i]);
			} catch (Exception e) {
			}

			newArray[i] = value;
		}

		return newArray;
	}

	public static float[] split(String s, String delimiter, float x) {
		String[] array = split(s, delimiter);

		float[] newArray = new float[array.length];

		for (int i = 0; i < array.length; i++) {
			float value = x;

			try {
				value = Float.parseFloat(array[i]);
			} catch (Exception e) {
			}

			newArray[i] = value;
		}

		return newArray;
	}

	public static int[] split(String s, String delimiter, int x) {
		String[] array = split(s, delimiter);

		int[] newArray = new int[array.length];

		for (int i = 0; i < array.length; i++) {
			int value = x;

			try {
				value = Integer.parseInt(array[i]);
			} catch (Exception e) {
			}

			newArray[i] = value;
		}

		return newArray;
	}

	public static long[] split(String s, String delimiter, long x) {
		String[] array = split(s, delimiter);

		long[] newArray = new long[array.length];

		for (int i = 0; i < array.length; i++) {
			long value = x;

			try {
				value = Long.parseLong(array[i]);
			} catch (Exception e) {
			}

			newArray[i] = value;
		}

		return newArray;
	}

	public static short[] split(String s, String delimiter, short x) {
		String[] array = split(s, delimiter);

		short[] newArray = new short[array.length];

		for (int i = 0; i < array.length; i++) {
			short value = x;

			try {
				value = Short.parseShort(array[i]);
			} catch (Exception e) {
			}

			newArray[i] = value;
		}

		return newArray;
	}

	public static String replace(String s, char oldSub, char newSub) {
		if (s == null) {
			return null;
		}

		return s.replace(oldSub, newSub);
	}

	public static String replace(String s, char oldSub, String newSub) {
		if ((s == null) || (newSub == null)) {
			return null;
		}

		int index = s.indexOf(oldSub);

		if (index == -1) {
			return s;
		}

		int previousIndex = index;

		StringBundler sb = new StringBundler();

		if (previousIndex != 0) {
			sb.append(s.substring(0, previousIndex));
		}

		sb.append(newSub);

		while ((index = s.indexOf(oldSub, previousIndex + 1)) != -1) {
			sb.append(s.substring(previousIndex + 1, index));
			sb.append(newSub);

			previousIndex = index;
		}

		index = previousIndex + 1;

		if (index < s.length()) {
			sb.append(s.substring(index));
		}

		return sb.toString();
	}

	public static String replace(String s, char[] oldSubs, char[] newSubs) {
		if ((s == null) || (oldSubs == null) || (newSubs == null)) {
			return null;
		}

		if (oldSubs.length != newSubs.length) {
			return s;
		}

		StringBuilder sb = new StringBuilder(s.length());

		sb.append(s);

		boolean modified = false;

		for (int i = 0; i < sb.length(); i++) {
			char c = sb.charAt(i);

			for (int j = 0; j < oldSubs.length; j++) {
				if (c == oldSubs[j]) {
					sb.setCharAt(i, newSubs[j]);

					modified = true;

					break;
				}
			}
		}

		if (modified) {
			return sb.toString();
		}

		return s;
	}

	public static String replace(String s, char[] oldSubs, String[] newSubs) {
		if ((s == null) || (oldSubs == null) || (newSubs == null)) {
			return null;
		}

		if (oldSubs.length != newSubs.length) {
			return s;
		}

		StringBundler sb = null;

		int lastReplacementIndex = 0;

		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);

			for (int j = 0; j < oldSubs.length; j++) {
				if (c == oldSubs[j]) {
					if (sb == null) {
						sb = new StringBundler();
					}

					if (i > lastReplacementIndex) {
						sb.append(s.substring(lastReplacementIndex, i));
					}

					sb.append(newSubs[j]);

					lastReplacementIndex = i + 1;

					break;
				}
			}
		}

		if (sb == null) {
			return s;
		}

		if (lastReplacementIndex < s.length()) {
			sb.append(s.substring(lastReplacementIndex));
		}

		return sb.toString();
	}

	public static String replace(String s, String oldSub, String newSub) {
		return replace(s, oldSub, newSub, 0);
	}

	public static String replace(String s, String oldSub, String newSub, int fromIndex) {

		if (s == null) {
			return null;
		}

		if ((oldSub == null) || oldSub.equals(StringPool.BLANK)) {
			return s;
		}

		if (newSub == null) {
			newSub = StringPool.BLANK;
		}

		int y = s.indexOf(oldSub, fromIndex);

		if (y >= 0) {
			StringBundler sb = new StringBundler();

			int length = oldSub.length();
			int x = 0;

			while (x <= y) {
				sb.append(s.substring(x, y));
				sb.append(newSub);

				x = y + length;

				y = s.indexOf(oldSub, x);
			}

			sb.append(s.substring(x));

			return sb.toString();
		}

		return s;
	}

	public static String replace(String s, String begin, String end, Map<String, String> values) {

		StringBundler sb = replaceToStringBundler(s, begin, end, values);

		return sb.toString();
	}

	public static String replace(String s, String[] oldSubs, String[] newSubs) {
		if ((s == null) || (oldSubs == null) || (newSubs == null)) {
			return null;
		}

		if (oldSubs.length != newSubs.length) {
			return s;
		}

		for (int i = 0; i < oldSubs.length; i++) {
			s = replace(s, oldSubs[i], newSubs[i]);
		}

		return s;
	}

	public static String replace(String s, String[] oldSubs, String[] newSubs, boolean exactMatch) {

		if ((s == null) || (oldSubs == null) || (newSubs == null)) {
			return null;
		}

		if (oldSubs.length != newSubs.length) {
			return s;
		}

		if (!exactMatch) {
			return replace(s, oldSubs, newSubs);
		}

		for (int i = 0; i < oldSubs.length; i++) {
			s = s.replaceAll("\\b" + oldSubs[i] + "\\b", newSubs[i]);
		}

		return s;
	}

	public static String replaceFirst(String s, char oldSub, char newSub) {
		if (s == null) {
			return null;
		}

		return replaceFirst(s, String.valueOf(oldSub), String.valueOf(newSub));
	}

	public static String replaceFirst(String s, char oldSub, String newSub) {
		if ((s == null) || (newSub == null)) {
			return null;
		}

		return replaceFirst(s, String.valueOf(oldSub), newSub);
	}

	public static String replaceFirst(String s, String oldSub, String newSub) {
		return replaceFirst(s, oldSub, newSub, 0);
	}

	public static String replaceFirst(String s, String oldSub, String newSub, int fromIndex) {

		if ((s == null) || (oldSub == null) || (newSub == null)) {
			return null;
		}

		if (oldSub.equals(newSub)) {
			return s;
		}

		int y = s.indexOf(oldSub, fromIndex);

		if (y >= 0) {
			return s.substring(0, y).concat(newSub).concat(s.substring(y + oldSub.length()));
		}

		return s;
	}

	public static String replaceFirst(String s, String[] oldSubs, String[] newSubs) {

		if ((s == null) || (oldSubs == null) || (newSubs == null)) {
			return null;
		}

		if (oldSubs.length != newSubs.length) {
			return s;
		}

		for (int i = 0; i < oldSubs.length; i++) {
			s = replaceFirst(s, oldSubs[i], newSubs[i]);
		}

		return s;
	}

	public static String replaceLast(String s, char oldSub, char newSub) {
		if (s == null) {
			return null;
		}

		return replaceLast(s, String.valueOf(oldSub), String.valueOf(newSub));
	}

	public static String replaceLast(String s, char oldSub, String newSub) {
		if ((s == null) || (newSub == null)) {
			return null;
		}

		return replaceLast(s, String.valueOf(oldSub), newSub);
	}

	public static String replaceLast(String s, String oldSub, String newSub) {
		if ((s == null) || (oldSub == null) || (newSub == null)) {
			return null;
		}

		if (oldSub.equals(newSub)) {
			return s;
		}

		int y = s.lastIndexOf(oldSub);

		if (y >= 0) {
			return s.substring(0, y).concat(newSub).concat(s.substring(y + oldSub.length()));
		}

		return s;
	}

	public static String replaceLast(String s, String[] oldSubs, String[] newSubs) {

		if ((s == null) || (oldSubs == null) || (newSubs == null)) {
			return null;
		}

		if (oldSubs.length != newSubs.length) {
			return s;
		}

		for (int i = 0; i < oldSubs.length; i++) {
			s = replaceLast(s, oldSubs[i], newSubs[i]);
		}

		return s;
	}

	public static StringBundler replaceToStringBundler(String s, String begin, String end, Map<String, String> values) {

		if (Validator.isBlank(s) || Validator.isBlank(begin) || Validator.isBlank(end) || MapUtil.isEmpty(values)) {

			return new StringBundler(s);
		}

		StringBundler sb = new StringBundler(values.size() * 2 + 1);

		int pos = 0;

		while (true) {
			int x = s.indexOf(begin, pos);

			int y = s.indexOf(end, x + begin.length());

			if ((x == -1) || (y == -1)) {
				sb.append(s.substring(pos));

				break;
			}

			sb.append(s.substring(pos, x));

			String oldValue = s.substring(x + begin.length(), y);

			String newValue = values.get(oldValue);

			if (newValue == null) {
				newValue = oldValue;
			}

			sb.append(newValue);

			pos = y + end.length();
		}

		return sb;
	}

	public static StringBundler replaceWithStringBundler(String s, String begin, String end,
			Map<String, StringBundler> values) {

		if (Validator.isBlank(s) || Validator.isBlank(begin) || Validator.isBlank(end) || MapUtil.isEmpty(values)) {

			return new StringBundler(s);
		}

		int size = values.size() + 1;

		for (StringBundler valueSB : values.values()) {
			size += valueSB.index();
		}

		StringBundler sb = new StringBundler(size);

		int pos = 0;

		while (true) {
			int x = s.indexOf(begin, pos);

			int y = s.indexOf(end, x + begin.length());

			if ((x == -1) || (y == -1)) {
				sb.append(s.substring(pos));

				break;
			}

			sb.append(s.substring(pos, x));

			String oldValue = s.substring(x + begin.length(), y);

			StringBundler newValueSB = values.get(oldValue);

			if (newValueSB == null) {
				sb.append(oldValue);
			} else {
				sb.append(newValueSB);
			}

			pos = y + end.length();
		}

		return sb;
	}

	public static String extractDigits(String s) {
		if (s == null) {
			return StringPool.BLANK;
		}

		StringBundler sb = new StringBundler();

		char[] chars = s.toCharArray();

		for (char c : chars) {
			if (Validator.isDigit(c)) {
				sb.append(c);
			}
		}

		return sb.toString();
	}

	public static String toLowerCase(String s) {
		return toLowerCase(s, null);
	}

	public static String toLowerCase(String s, Locale locale) {
		if (s == null) {
			return null;
		}

		StringBuilder sb = null;

		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);

			if (c > 127) {

				// Found non-ascii char, fallback to the slow unicode detection

				if (locale == null) {
					locale = LocaleUtil.getDefault();
				}

				return s.toLowerCase(locale);
			}

			if ((c >= 'A') && (c <= 'Z')) {
				if (sb == null) {
					sb = new StringBuilder(s);
				}

				sb.setCharAt(i, (char) (c + 32));
			}
		}

		if (sb == null) {
			return s;
		}

		return sb.toString();
	}

	public static String toUpperCase(String s) {
		return toUpperCase(s, null);
	}

	public static String toUpperCase(String s, Locale locale) {
		if (s == null) {
			return null;
		}

		StringBuilder sb = null;

		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);

			if (c > 127) {

				// Found non-ascii char, fallback to the slow unicode detection

				if (locale == null) {
					locale = LocaleUtil.getDefault();
				}

				return s.toUpperCase(locale);
			}

			if ((c >= 'a') && (c <= 'z')) {
				if (sb == null) {
					sb = new StringBuilder(s);
				}

				sb.setCharAt(i, (char) (c - 32));
			}
		}

		if (sb == null) {
			return s;
		}

		return sb.toString();
	}

	public static boolean equalsIgnoreCase(char c1, char c2) {
		if (c1 == c2) {
			return true;
		}

		// Fast fallback for non-acsii code.

		if ((c1 > 127) || (c2 > 127)) {

			// Georgian alphabet needs to check both upper and lower case

			if ((Character.toLowerCase(c1) == Character.toLowerCase(c2))
					|| (Character.toUpperCase(c1) == Character.toUpperCase(c2))) {

				return true;
			}

			return false;
		}

		// Fast fallback for non-letter ascii code

		if ((c1 < CharPool.UPPER_CASE_A) || (c1 > CharPool.LOWER_CASE_Z) || (c2 < CharPool.UPPER_CASE_A)
				|| (c2 > CharPool.LOWER_CASE_Z)) {

			return false;
		}

		int delta = c1 - c2;

		if ((delta != 32) && (delta != -32)) {
			return false;
		}

		return true;
	}

	public static boolean equalsIgnoreCase(String s1, String s2) {
		if (Objects.equals(s1, s2)) {
			return true;
		}

		if ((s1 == null) || (s2 == null)) {
			return false;
		}

		if (s1.length() != s2.length()) {
			return false;
		}

		for (int i = 0; i < s1.length(); i++) {
			if (!equalsIgnoreCase(s1.charAt(i), s2.charAt(i))) {
				return false;
			}
		}

		return true;
	}

	public static String appendParentheticalSuffix(String s, int suffix) {
		if (Pattern.matches(".* \\(" + (suffix - 1) + "\\)", s)) {
			int pos = s.lastIndexOf(" (");

			s = s.substring(0, pos);
		}

		return appendParentheticalSuffix(s, String.valueOf(suffix));
	}

	public static String appendParentheticalSuffix(String s, String suffix) {
		StringBundler sb = new StringBundler(5);

		sb.append(s);
		sb.append(StringPool.SPACE);
		sb.append(StringPool.OPEN_PARENTHESIS);
		sb.append(suffix);
		sb.append(StringPool.CLOSE_PARENTHESIS);

		return sb.toString();
	}

	public static String merge(boolean[] array) {
		return merge(array, StringPool.COMMA);
	}

	public static String merge(boolean[] array, String delimiter) {
		if (array == null) {
			return null;
		}

		if (array.length == 0) {
			return StringPool.BLANK;
		}

		if (array.length == 1) {
			return String.valueOf(array[0]);
		}

		StringBundler sb = new StringBundler(2 * array.length - 1);

		for (int i = 0; i < array.length; i++) {
			if (i != 0) {
				sb.append(delimiter);
			}

			sb.append(String.valueOf(array[i]));
		}

		return sb.toString();
	}

	public static String merge(char[] array) {
		return merge(array, StringPool.COMMA);
	}

	public static String merge(char[] array, String delimiter) {
		if (array == null) {
			return null;
		}

		if (array.length == 0) {
			return StringPool.BLANK;
		}

		if (array.length == 1) {
			return String.valueOf(array[0]);
		}

		StringBundler sb = new StringBundler(2 * array.length - 1);

		for (int i = 0; i < array.length; i++) {
			if (i != 0) {
				sb.append(delimiter);
			}

			sb.append(String.valueOf(array[i]));
		}

		return sb.toString();
	}

	public static String merge(Collection<?> col) {
		return merge(col, StringPool.COMMA);
	}

	public static String merge(Collection<?> col, String delimiter) {
		if (col == null) {
			return null;
		}

		if (col.isEmpty()) {
			return StringPool.BLANK;
		}

		StringBundler sb = new StringBundler(2 * col.size());

		for (Object object : col) {
			String objectString = String.valueOf(object);

			sb.append(objectString.trim());

			sb.append(delimiter);
		}

		if (!delimiter.isEmpty()) {
			sb.setIndex(sb.index() - 1);
		}

		return sb.toString();
	}

	public static String merge(double[] array) {
		return merge(array, StringPool.COMMA);
	}

	public static String merge(double[] array, String delimiter) {
		if (array == null) {
			return null;
		}

		if (array.length == 0) {
			return StringPool.BLANK;
		}

		if (array.length == 1) {
			return String.valueOf(array[0]);
		}

		StringBundler sb = new StringBundler(2 * array.length - 1);

		for (int i = 0; i < array.length; i++) {
			if (i != 0) {
				sb.append(delimiter);
			}

			sb.append(String.valueOf(array[i]));
		}

		return sb.toString();
	}

	public static String merge(float[] array) {
		return merge(array, StringPool.COMMA);
	}

	public static String merge(float[] array, String delimiter) {
		if (array == null) {
			return null;
		}

		if (array.length == 0) {
			return StringPool.BLANK;
		}

		if (array.length == 1) {
			return String.valueOf(array[0]);
		}

		StringBundler sb = new StringBundler(2 * array.length - 1);

		for (int i = 0; i < array.length; i++) {
			if (i != 0) {
				sb.append(delimiter);
			}

			sb.append(String.valueOf(array[i]));
		}

		return sb.toString();
	}

	public static String merge(int[] array) {
		return merge(array, StringPool.COMMA);
	}

	public static String merge(int[] array, String delimiter) {
		if (array == null) {
			return null;
		}

		if (array.length == 0) {
			return StringPool.BLANK;
		}

		if (array.length == 1) {
			return String.valueOf(array[0]);
		}

		StringBundler sb = new StringBundler(2 * array.length - 1);

		for (int i = 0; i < array.length; i++) {
			if (i != 0) {
				sb.append(delimiter);
			}

			sb.append(String.valueOf(array[i]));
		}

		return sb.toString();
	}

	public static String merge(long[] array) {
		return merge(array, StringPool.COMMA);
	}

	public static String merge(long[] array, String delimiter) {
		if (array == null) {
			return null;
		}

		if (array.length == 0) {
			return StringPool.BLANK;
		}

		if (array.length == 1) {
			return String.valueOf(array[0]);
		}

		StringBundler sb = new StringBundler(2 * array.length - 1);

		for (int i = 0; i < array.length; i++) {
			if (i != 0) {
				sb.append(delimiter);
			}

			sb.append(String.valueOf(array[i]));
		}

		return sb.toString();
	}

	public static String merge(Object[] array) {
		return merge(array, StringPool.COMMA);
	}

	public static String merge(Object[] array, String delimiter) {
		if (array == null) {
			return null;
		}

		if (array.length == 0) {
			return StringPool.BLANK;
		}

		if (array.length == 1) {
			return String.valueOf(array[0]);
		}

		StringBundler sb = new StringBundler(2 * array.length - 1);

		for (int i = 0; i < array.length; i++) {
			if (i != 0) {
				sb.append(delimiter);
			}

			String value = String.valueOf(array[i]);

			sb.append(value.trim());
		}

		return sb.toString();
	}

	public static String merge(short[] array) {
		return merge(array, StringPool.COMMA);
	}

	public static String merge(short[] array, String delimiter) {
		if (array == null) {
			return null;
		}

		if (array.length == 0) {
			return StringPool.BLANK;
		}

		if (array.length == 1) {
			return String.valueOf(array[0]);
		}

		StringBundler sb = new StringBundler(2 * array.length - 1);

		for (int i = 0; i < array.length; i++) {
			if (i != 0) {
				sb.append(delimiter);
			}

			sb.append(String.valueOf(array[i]));
		}

		return sb.toString();
	}

	public static String[] splitLines(String s) {
		if (Validator.isNull(s)) {
			return _EMPTY_STRING_ARRAY;
		}

		s = s.trim();

		List<String> lines = new ArrayList<>();

		_splitLines(s, lines);

		return lines.toArray(new String[0]);
	}

	private static void _splitLines(String s, Collection<String> lines) {
		int lastIndex = 0;

		while (true) {
			int returnIndex = s.indexOf(CharPool.RETURN, lastIndex);

			if (returnIndex == -1) {
				_split(lines, s, lastIndex, CharPool.NEW_LINE);

				return;
			}

			int newLineIndex = s.indexOf(CharPool.NEW_LINE, lastIndex);

			if (newLineIndex == -1) {
				_split(lines, s, lastIndex, CharPool.RETURN);

				return;
			}

			if (newLineIndex < returnIndex) {
				lines.add(s.substring(lastIndex, newLineIndex));

				lastIndex = newLineIndex + 1;
			} else {
				lines.add(s.substring(lastIndex, returnIndex));

				lastIndex = returnIndex + 1;

				if (lastIndex == newLineIndex) {
					lastIndex++;
				}
			}
		}
	}

	public static String randomString(int length) {
		SecureRandom secureRandom = new SecureRandom();

		char[] chars = new char[length];

		for (int i = 0; i < length; i++) {
			int index = secureRandom.nextInt(_RANDOM_STRING_CHAR_TABLE.length);

			chars[i] = _RANDOM_STRING_CHAR_TABLE[index];
		}

		return new String(chars);
	}

	public static String trim(String s) {
		if (s == null) {
			return null;
		}

		int len = s.length();

		if (len == 0) {
			return s;
		}

		int x = 0;

		while (x < len) {
			char c = s.charAt(x);

			if (((c > CharPool.SPACE) && (c < 128)) || !Character.isWhitespace(c)) {

				break;
			}

			x++;
		}

		if (x == len) {
			return StringPool.BLANK;
		}

		int y = len - 1;

		while (x < y) {
			char c = s.charAt(y);

			if (((c > CharPool.SPACE) && (c < 128)) || !Character.isWhitespace(c)) {

				break;
			}

			y--;
		}

		y++;

		if ((x > 0) || (y < len)) {
			return s.substring(x, y);
		}

		return s;
	}

	public static String replaceNewlineWithBr(String value) {
		if (value == null)
			return "";
		return value.replaceAll("\\\\n|\\n", "<br/>");
	}

	private static final String[] _EMPTY_STRING_ARRAY = new String[0];

	private static final char[] _RANDOM_STRING_CHAR_TABLE = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A',
			'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V',
			'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
			'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

}
