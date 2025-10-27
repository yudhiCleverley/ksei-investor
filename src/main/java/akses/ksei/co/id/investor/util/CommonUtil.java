package akses.ksei.co.id.investor.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class CommonUtil {

	private static Logger log = LoggerFactory.getLogger(CommonUtil.class);

	private static Pattern REMOVE_TAGS = Pattern.compile("<.+?>");

	private static String regPhone = "(0|\\+62|62)([2-9])?[0-9]+";// "^(0|\\+62|62)([1-9]{2,3})([0-9]{6,44})$";

	private static String regMobilePhone = "(0|\\+62|62)+(8)?[0-9]+"; // "^(0|\\+62|62)8([1-9]{1}?[0-9]{7,45})$";

	private static String emailRegexPattern = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";

	private static Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
			Pattern.CASE_INSENSITIVE);

	private static final String LOCALHOST_IPV4 = "127.0.0.1";

	private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";

	private static final int NOT_FOUND = -1;

	private static final String EMPTY_STRING = "";

	public static final char EXTENSION_SEPARATOR = '.';

	private static final char SYSTEM_NAME_SEPARATOR = File.separatorChar;

	/**
	 * The Windows separator character.
	 */
	private static final char WINDOWS_NAME_SEPARATOR = '\\';

	/**
	 * The Unix separator character.
	 */
	private static final char UNIX_NAME_SEPARATOR = '/';

	/**
	 * The separator character that is the opposite of the system separator.
	 */
	private static final char OTHER_SEPARATOR = flipSeparator(SYSTEM_NAME_SEPARATOR);

	private CommonUtil() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static String getTimestamp() {
		String ts = "";

		try {
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			Instant instant = timestamp.toInstant();
			ts = instant.toString();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return ts;
	}

	public static void deleteFiles(String path) {
		if (path == null) {
			return;
		}

		try {
			File directory = new File(path);
			File[] files = directory.listFiles();

			if (files != null) {
				for (File file : files) {
					if (file.isFile()) {
						try {
							Files.delete(file.toPath());
							log.debug("Deleted file: {}", file.getAbsolutePath());
						} catch (IOException e) {
							log.error("Failed to delete file: {}", file.getAbsolutePath(), e);
						}
					}
				}
			}
		} catch (Exception e) {
			log.error("Error while deleting files in path: {}", path, e);
		}
	}

	public static String removeTags(String string) {
		if (string == null || string.isEmpty()) {
			return string;
		}

		Matcher m = REMOVE_TAGS.matcher(string);

		String test = m.replaceAll("");
		return test.replace("<", "");
	}

	public static boolean isValidPhone(String phone, boolean isMobile) {
		return isMobile ? phone.matches(regMobilePhone) : phone.matches(regPhone);
	}

	public static boolean validateEmail(String emailAddress) {
		Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailAddress);
		return matcher.find();
	}

	public static boolean patternMatches(String emailAddress, String regexPattern) {
		return Pattern.compile(regexPattern, Pattern.CASE_INSENSITIVE).matcher(emailAddress).matches();
	}

	public static boolean isValidEmail(String email) {
		try {
			if (patternMatches(email, emailRegexPattern)) {
				return true;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return false;
	}

	public static boolean specialCharCheck(String value) {
		boolean isContainsSC = false;

		if (null == value)
			return false;

		Matcher m = Pattern.compile("[^A-Za-z0-9\\s+]").matcher(value.trim());
		while (m.find()) {
			isContainsSC = true;
		}

		return isContainsSC;
	}

	public static String fetchClientIpAddr() {
		try {
			ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
			if (attrs == null) {
				log.warn(
						"RequestContextHolder does not contain any request attributes (possibly called outside HTTP context)");
				return "UNKNOWN";
			}

			HttpServletRequest request = attrs.getRequest();
			String ip = Optional.ofNullable(request.getHeader("X-FORWARDED-FOR")).orElse(request.getRemoteAddr());
			if (ip.equals("0:0:0:0:0:0:0:1"))
				ip = "127.0.0.1";
			Assert.isTrue(ip.chars().filter($ -> $ == '.').count() == 3, "Illegal IP: " + ip);

			return ip;
		} catch (Exception e) {
			log.error(e.getMessage(), e);

			return null;
		}
	}

	public static String getClientIp(HttpServletRequest request) {
		String ipAddress = request.getHeader("X-Forwarded-For");
		if (StringUtils.isEmpty(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getHeader("Proxy-Client-IP");
		}

		if (StringUtils.isEmpty(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getHeader("WL-Proxy-Client-IP");
		}

		if (StringUtils.isEmpty(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getRemoteAddr();
			if (LOCALHOST_IPV4.equals(ipAddress) || LOCALHOST_IPV6.equals(ipAddress)) {
				try {
					InetAddress inetAddress = InetAddress.getLocalHost();
					ipAddress = inetAddress.getHostAddress();
				} catch (UnknownHostException e) {
					log.error(e.getMessage(), e);
				}
			}
		}

		if (!StringUtils.isEmpty(ipAddress) && ipAddress.length() > 15 && ipAddress.indexOf(",") > 0) {
			ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
		}

		return ipAddress;
	}

	public static String getClientIpAddr(HttpServletRequest request) {

		String ip = request.getHeader("X-FORWARDED-FOR");
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}

	public static String getClientPort(HttpServletRequest request) {

		return String.valueOf(request.getServerPort());
	}

	public static String getClientMACAddress(String clientIp) {
		String str = StringPool.BLANK;
		String macAddress = StringPool.BLANK;

		try {
			Process p = Runtime.getRuntime().exec("nbtstat -A " + clientIp);
			InputStreamReader ir = new InputStreamReader(p.getInputStream());
			LineNumberReader input = new LineNumberReader(ir);
			for (int i = 1; i < 100; i++) {
				str = input.readLine();
				if (str != null && str.indexOf("MAC Address") > 1) {
					macAddress = str.substring(str.indexOf("MAC Address") + 14, str.length());
					break;
				}

			}
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}

		return macAddress;
	}

	public static String getMacAddress(String clientIp) {
		String macAddress = StringPool.BLANK;

		InetAddress ip = null;

		try {
			if (Validator.isNotNull(clientIp)) {
				ip = InetAddress.getByName(clientIp);

				NetworkInterface network = NetworkInterface.getByInetAddress(ip);

				if (network != null) {
					byte[] mac = network.getHardwareAddress();

					StringBuilder sb = new StringBuilder();
					for (int i = 0; i < mac.length; i++) {
						sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
					}

					macAddress = sb.toString();
				}
			}
		} catch (UnknownHostException | SocketException e) {

			log.error(e.getMessage(), e);
		}

		return macAddress;
	}

	public static String getMacAddress() {
		String macAddress = StringPool.BLANK;

		InetAddress ip = null;

		try {
			ip = InetAddress.getLocalHost();

			NetworkInterface network = NetworkInterface.getByInetAddress(ip);

			if (network != null) {
				byte[] mac = network.getHardwareAddress();

				if (mac != null) {
					StringBuilder sb = new StringBuilder();
					for (int i = 0; i < mac.length; i++) {
						sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
					}

					macAddress = sb.toString();
				}
			}
		} catch (UnknownHostException | SocketException e) {

			log.error(e.getMessage(), e);
		}

		return macAddress;
	}

	public static String getSessionId() {
		try {
			return RequestContextHolder.currentRequestAttributes().getSessionId();
		} catch (Exception e) {
			log.error(e.getMessage(), e);

			return null;
		}
	}

	public static String getClientSessionId() {
		try {
			ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
			HttpSession session = attr.getRequest().getSession(true);

			return session.getId();
		} catch (Exception e) {
			log.error(e.getMessage(), e);

			return null;
		}
	}

	public static String getClientOS(HttpServletRequest request) {
		String userAgent = getUserAgent(request);
		String userAgentLwr = userAgent.toLowerCase();

		// =================OS=======================
		if (userAgentLwr.contains("windows")) {
			String version = userAgent.substring(userAgent.indexOf("Windows NT ") + 11, userAgent.indexOf(";"));

			return "Windows " + version;
		} else if (userAgentLwr.contains("mac")) {
			String version = userAgent.substring(userAgent.indexOf("Mac OS ") + 7, userAgent.indexOf(")"));

			return "Mac " + version;
		} else if (userAgentLwr.contains("x11")) {
			return "Unix";
		} else if (userAgentLwr.contains("android")) {
			return "Android";
		} else if (userAgentLwr.contains("iphone")) {
			return "IPhone";
		} else {
			return "UnKnown, More-Info: " + userAgent;
		}
	}

	public static String getClientBrowser(HttpServletRequest request) {
		String userAgent = getUserAgent(request);
		String userAgentLwr = userAgent.toLowerCase();

		String browser = "";

		// ===============Browser===========================
		if (userAgentLwr.contains("msie")) {
			String substring = userAgent.substring(userAgent.indexOf("MSIE")).split(";")[0];
			browser = substring.split(" ")[0].replace("MSIE", "IE") + "-" + substring.split(" ")[1];
		} else if (userAgentLwr.contains("safari") && userAgentLwr.contains("version")) {
			browser = (userAgent.substring(userAgent.indexOf("Safari")).split(" ")[0]).split("/")[0] + "-"
					+ (userAgent.substring(userAgent.indexOf("Version")).split(" ")[0]).split("/")[1];
		} else if (userAgentLwr.contains("opr") || userAgentLwr.contains("opera")) {
			if (userAgentLwr.contains("opera"))
				browser = (userAgent.substring(userAgent.indexOf("Opera")).split(" ")[0]).split("/")[0] + "-"
						+ (userAgent.substring(userAgent.indexOf("Version")).split(" ")[0]).split("/")[1];
			else if (userAgentLwr.contains("opr"))
				browser = ((userAgent.substring(userAgent.indexOf("OPR")).split(" ")[0]).replace("/", "-"))
						.replace("OPR", "Opera");
		} else if (userAgentLwr.contains("chrome")) {
			browser = (userAgent.substring(userAgent.indexOf("Chrome")).split(" ")[0]).replace("/", "-");
		} else if ((userAgentLwr.indexOf("mozilla/7.0") > -1) || (userAgentLwr.indexOf("netscape6") != -1)
				|| (userAgentLwr.indexOf("mozilla/4.7") != -1) || (userAgentLwr.indexOf("mozilla/4.78") != -1)
				|| (userAgentLwr.indexOf("mozilla/4.08") != -1) || (userAgentLwr.indexOf("mozilla/3") != -1)) {
			// browser=(userAgent.substring(userAgent.indexOf("MSIE")).split("
			// ")[0]).replace("/", "-");
			browser = "Netscape-?";
		} else if (userAgentLwr.contains("firefox")) {
			browser = (userAgent.substring(userAgent.indexOf("Firefox")).split(" ")[0]).replace("/", "-");
		} else if (userAgentLwr.contains("rv")) {
			browser = "IE";
		} else {
			browser = "UnKnown, More-Info: " + userAgent;
		}

		return browser;
	}

	public static String getDeviceType(HttpServletRequest request) {
		String userAgent = getUserAgent(request);
		String userAgentLwr = userAgent.toLowerCase();

		// =================Device Type=======================
		if (userAgentLwr.contains("windows") || userAgentLwr.contains("mac") || userAgentLwr.contains("x11")) {
			return "Desktop";
		} else if (userAgentLwr.contains("android")) {
			return "Mobile";
		} else if (userAgentLwr.contains("iphone")) {
			if (userAgentLwr.indexOf("iphone") >= 0) {
				return "iPhone";
			} else if (userAgentLwr.indexOf("ipad") >= 0) {
				return "iPad";
			}
		}

		return "UnKnown, More-Info: " + userAgent;
	}

	public static String getUserAgent(HttpServletRequest request) {
		return request.getHeader("User-Agent");
	}

	public static String getFileExtension(String fileName) {
		if (fileName == null) {
			throw new IllegalArgumentException("fileName must not be null!");
		}

		String extension = "";

		int index = fileName.lastIndexOf('.');
		if (index > 0) {
			extension = fileName.substring(index + 1);
		}

		return extension;

	}

	public static String getExtension(final String fileName) throws IllegalArgumentException {
		if (fileName == null) {
			return null;
		}
		final int index = indexOfExtension(fileName);
		if (index == NOT_FOUND) {
			return EMPTY_STRING;
		}
		return fileName.substring(index + 1);
	}

	public static int indexOfExtension(final String fileName) throws IllegalArgumentException {
		if (fileName == null) {
			return NOT_FOUND;
		}
		if (isSystemWindows()) {
			// Special handling for NTFS ADS: Don't accept colon in the fileName.
			final int offset = fileName.indexOf(':', getAdsCriticalOffset(fileName));
			if (offset != -1) {
				throw new IllegalArgumentException("NTFS ADS separator (':') in file name is forbidden.");
			}
		}
		final int extensionPos = fileName.lastIndexOf(EXTENSION_SEPARATOR);
		final int lastSeparator = indexOfLastSeparator(fileName);
		return lastSeparator > extensionPos ? NOT_FOUND : extensionPos;
	}

	/**
	 * Determines if Windows file system is in use.
	 *
	 * @return true if the system is Windows
	 */
	static boolean isSystemWindows() {
		return SYSTEM_NAME_SEPARATOR == WINDOWS_NAME_SEPARATOR;
	}

	/**
	 * Special handling for NTFS ADS: Don't accept colon in the fileName.
	 *
	 * @param fileName a file name
	 * @return ADS offsets.
	 */
	private static int getAdsCriticalOffset(final String fileName) {
		// Step 1: Remove leading path segments.
		final int offset1 = fileName.lastIndexOf(SYSTEM_NAME_SEPARATOR);
		final int offset2 = fileName.lastIndexOf(OTHER_SEPARATOR);
		if (offset1 == -1) {
			if (offset2 == -1) {
				return 0;
			}
			return offset2 + 1;
		}
		if (offset2 == -1) {
			return offset1 + 1;
		}
		return Math.max(offset1, offset2) + 1;
	}

	/**
	 * Flips the Windows name separator to Linux and vice-versa.
	 *
	 * @param ch The Windows or Linux name separator.
	 * @return The Windows or Linux name separator.
	 */
	static char flipSeparator(final char ch) {
		if (ch == UNIX_NAME_SEPARATOR) {
			return WINDOWS_NAME_SEPARATOR;
		}
		if (ch == WINDOWS_NAME_SEPARATOR) {
			return UNIX_NAME_SEPARATOR;
		}
		throw new IllegalArgumentException(String.valueOf(ch));
	}

	/**
	 * Returns the index of the last directory separator character.
	 * <p>
	 * This method will handle a file in either Unix or Windows format. The position
	 * of the last forward or backslash is returned.
	 * <p>
	 * The output will be the same irrespective of the machine that the code is
	 * running on.
	 *
	 * @param fileName the fileName to find the last path separator in, null returns
	 *                 -1
	 * @return the index of the last separator character, or -1 if there is no such
	 *         character
	 */
	public static int indexOfLastSeparator(final String fileName) {
		if (fileName == null) {
			return NOT_FOUND;
		}
		final int lastUnixPos = fileName.lastIndexOf(UNIX_NAME_SEPARATOR);
		final int lastWindowsPos = fileName.lastIndexOf(WINDOWS_NAME_SEPARATOR);
		return Math.max(lastUnixPos, lastWindowsPos);
	}

	public static void doLongRunningTask() {
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
			Thread.currentThread().interrupt();
		}
	}

}
