package akses.ksei.co.id.investor.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateUtil {

	private static Logger _log = LoggerFactory.getLogger(DateUtil.class);

	private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();

	/** Date format: dd-MM-yyyy */
	private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd-MM-yyyy");
	/** Date format: dd-MMM-yyyy (e.g., 25-Jan-2025) */
	private static final DateTimeFormatter SDF = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
	/** Date format: yyyy-MM-dd (ISO format) */
	private static final DateTimeFormatter SDF2 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	/** Date format: yyyyMMdd (compact) */
	private static final DateTimeFormatter SDF3 = DateTimeFormatter.ofPattern("yyyyMMdd");
	/** Date format: dd/MM/yyyy (slash separator) */
	private static final DateTimeFormatter SDF4 = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private DateUtil() {
		throw new UnsupportedOperationException("Utility class should not be instantiated");
	}

	public static LocalDateTime stringToLocalDateTime(String stringDate) {
		if (Validator.isNull(stringDate)) {
			return null;
		}

		// Define the date-time format
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

		// Convert the String to LocalDateTime
		return LocalDateTime.parse(stringDate, formatter);
	}

	public static LocalDateTime getLocalDateTime(Calendar calendar) {

		return calendar == null ? null : LocalDateTime.ofInstant(calendar.toInstant(), DEFAULT_ZONE);
	}

	public static LocalDateTime startDate(String stringDate) {

		return Validator.isNotNull(stringDate) ? getLocalDateTime(setStartDate(strToDate(stringDate))) : null;
	}

	public static LocalDateTime endDate(String stringDate) {

		return Validator.isNotNull(stringDate) ? getLocalDateTime(setEndDate(strToDate(stringDate))) : null;
	}

	public static Date dateBefore(Date date, int minusDays) {
		if (date == null) {
			return null;
		}

		Instant instant = Instant.ofEpochMilli(date.getTime());
		LocalDate localDate = instant.atZone(DEFAULT_ZONE).toLocalDate().minusDays(minusDays);
		return Date.from(localDate.atStartOfDay(DEFAULT_ZONE).toInstant());
	}

	// fmt: dd-MM-yyyy
	public static Date strToDate(String stringDate) {
		if (Validator.isNull(stringDate)) {
			return null;
		}

		LocalDate localDate = LocalDate.parse(stringDate, DF);
		return Date.from(localDate.atStartOfDay(DEFAULT_ZONE).toInstant());
	}

	// fmt: dd/MM/yyyy
	public static Date stringToDate(String stringDate) {
		if (Validator.isNull(stringDate)) {
			return null;
		}

		LocalDate localDate = LocalDate.parse(stringDate, SDF4);
		return Date.from(localDate.atStartOfDay(DEFAULT_ZONE).toInstant());
	}

	public static String dateToStr(Date date) {
		if (date == null)
			return null;
		Instant instant = Instant.ofEpochMilli(date.getTime());
		LocalDate localDate = instant.atZone(DEFAULT_ZONE).toLocalDate();
		return DF.format(localDate);
	}

	public static Calendar setStartDate(Date date) {
		if (date == null)
			return null;

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);

		return calendar;
	}

	public static Calendar setEndDate(Date date) {
		if (date == null)
			return null;

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 00);

		return calendar;
	}

	public static String getSettlementDate(String input) {
		if (Validator.isNull(input)) {
			return StringPool.BLANK;
		}

		LocalDate date = LocalDate.parse(input, SDF);
		return SDF2.format(date);
	}

	public static String getEffectiveStartDate(String input) {
		if (Validator.isNull(input)) {
			return StringPool.BLANK;
		}

		LocalDate date = LocalDate.parse(input, SDF).minusDays(1);
		return SDF.format(date);
	}

	public static String getBalanceStartDate(String input) {
		if (Validator.isNull(input)) {
			return StringPool.BLANK;
		}

		LocalDate date = LocalDate.parse(input, SDF).minusDays(1);
		return SDF3.format(date);
	}

	public static String getBalanceEndDate(String input) {
		if (Validator.isNull(input)) {
			return StringPool.BLANK;
		}

		LocalDate date = LocalDate.parse(input, SDF);
		return SDF3.format(date);
	}

	public static String lastUpdate(Date date) {
		if (date == null) {
			return null;
		}

		Instant instant = Instant.ofEpochMilli(date.getTime());
		LocalDate localDate = instant.atZone(DEFAULT_ZONE).toLocalDate();
		return SDF.format(localDate);
	}

	public static Date strTolastUpdate(String stringDate) {
		if (Validator.isNull(stringDate)) {
			return null;
		}

		LocalDate localDate = LocalDate.parse(stringDate, SDF);
		return Date.from(localDate.atStartOfDay(DEFAULT_ZONE).toInstant());
	}

	/**
	 * Validasi apakah string tanggal sesuai dengan format yang diberikan.
	 * 
	 * @param dateStr string tanggal
	 * @param pattern pola format, misal "dd/MM/yyyy"
	 * @return true jika valid
	 */
	public static boolean isValid(String dateStr, String pattern) {
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
			LocalDate.parse(dateStr, formatter);
			return true;
		} catch (DateTimeParseException e) {
			return false;
		}
	}

	/**
	 * Validasi string tanggal terhadap daftar pola format.
	 * 
	 * @param dateStr  string tanggal
	 * @param patterns list format seperti ["yyyy-MM-dd", "dd-MM-yyyy"]
	 * @return true jika salah satu format cocok
	 */
	public static boolean isValid(String dateStr, List<String> patterns) {
		for (String pattern : patterns) {
			if (isValid(dateStr, pattern)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Parse string menjadi LocalDate jika format cocok.
	 * 
	 * @param dateStr string tanggal
	 * @param pattern pola format
	 * @return LocalDate jika valid, null jika gagal parse
	 */
	public static LocalDate parse(String dateStr, String pattern) {
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
			return LocalDate.parse(dateStr, formatter);
		} catch (DateTimeParseException e) {
			return null;
		}
	}

	public static java.sql.Date safeDate(Date date) {
		if (date == null) {
			_log.warn("DateUtil.safeDate called with null date");
			return null;
		}

		try {
			Date truncated = DateUtils.truncate(date, Calendar.DATE);
			return new java.sql.Date(truncated.getTime());
		} catch (Exception e) {
			_log.error("Error truncating date: {}", e.getMessage(), e);
			return null;
		}
	}

}
