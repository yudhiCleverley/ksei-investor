package akses.ksei.co.id.investor.util;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class CommonServiceUtil {

	private final Logger _log = LoggerFactory.getLogger(this.getClass());

	private static String dfPattern = "dd/MM/yyyy";

	private static String oraclePattern = "dd-MMM-yyyy";

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public Long getNextId(String sequenceName) {
		String sql = String.format("SELECT %s.NEXTVAL FROM DUAL", sequenceName);
		return jdbcTemplate.queryForObject(sql, Long.class);
	}

	public void setNullableField(PreparedStatement ps, int index, Object value, int sqlType) throws SQLException {
		if (value != null) {
			ps.setObject(index, value, sqlType);
		} else {
			ps.setNull(index, sqlType);
		}
	}

	public String trimToNull(String value) {
		if (value == null)
			return null;
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	public boolean compareBigDecimal(BigDecimal bd1, BigDecimal bd2) {
		if (bd1 == null && bd2 == null) {
			return true;
		}

		if (bd1 == null || bd2 == null) {
			return false;
		}
		// Menggunakan compareTo() untuk mengabaikan perbedaan scale
		return bd1.compareTo(bd2) == 0;
	}

	public boolean compareDate(Date d1, Date d2) {
		if (d1 == null && d2 == null) {
			return true;
		}

		if (d1 == null || d2 == null) {
			return false;
		}

		return d1.getTime() == d2.getTime(); // ✅ Bandingkan dengan getTime()
	}

	public LocalDate parseInputDate(String input) {
		DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(dfPattern);
		return LocalDate.parse(input, inputFormatter);
	}

	public String convertToOracleFormat(String inputDate) {
		LocalDate localDate = parseInputDate(inputDate);
		DateTimeFormatter oracleFormatter = DateTimeFormatter.ofPattern(oraclePattern, Locale.ENGLISH);
		return localDate.format(oracleFormatter).toUpperCase();
	}

	public String convertToPostgresFormat(String inputDate) {
		LocalDate localDate = parseInputDate(inputDate);
		return localDate.toString(); // yyyy-MM-dd
	}

	public String getDateMinusOne(String inputDate) {
		try {
			// Format tanggal input
			SimpleDateFormat sdf = new SimpleDateFormat(dfPattern);

			// Parse string ke Date
			Date date = sdf.parse(inputDate);

			// Gunakan Calendar untuk manipulasi tanggal
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.add(Calendar.DATE, -1); // Kurangi satu hari

			// Kembalikan tanggal hasil sebagai string
			return sdf.format(cal.getTime());
		} catch (Exception e) {
			_log.error(e.getMessage(), e);

			return null;
		}
	}

	public List<String> getDateAndMinusOne(String inputDate) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dfPattern);

		// Parse string input menjadi LocalDate
		LocalDate date = LocalDate.parse(inputDate, formatter);

		// Ambil tanggal H-1
		LocalDate dayBefore = date.minusDays(1);

		// Format kembali ke string dan masukkan ke dalam list
		return List.of(dayBefore.format(formatter), date.format(formatter));
	}

	public boolean toBoolean(Object value, boolean defaultValue) {
		if (value instanceof Boolean) {
			return (Boolean) value;
		}
		if (value instanceof Number) {
			return ((Number) value).intValue() != 0;
		}
		if (value instanceof String) {
			return Boolean.parseBoolean((String) value);
		}
		return defaultValue;
	}

	public String toString(Object value) {
		return value != null ? value.toString().trim() : null;
	}

	public Date toCreatedDate(String date) {
		LocalDate localDate = convertToLocalDate(date);
		return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

	public LocalDate convertToLocalDate(String dateStr) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		return LocalDate.parse(dateStr, formatter);
	}

}
