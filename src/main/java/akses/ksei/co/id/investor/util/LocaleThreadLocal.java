package akses.ksei.co.id.investor.util;

import java.util.Locale;

public class LocaleThreadLocal {

	public static Locale getDefaultLocale() {
		return _defaultLocale.get();
	}

	public static Locale getSiteDefaultLocale() {
		return _siteDefaultLocale.get();
	}

	public static Locale getThemeDisplayLocale() {
		return _themeDisplayLocale.get();
	}

	public static void setDefaultLocale(Locale locale) {
		_defaultLocale.set(locale);
	}

	public static void setSiteDefaultLocale(Locale locale) {
		_siteDefaultLocale.set(locale);
	}

	public static void setThemeDisplayLocale(Locale locale) {
		_themeDisplayLocale.set(locale);
	}

	public static void unloadDefault() {
		_defaultLocale.remove();
	}

	public static void unloadSiteDefaul() {
		_siteDefaultLocale.remove();
	}

	public static void unloadthemeDisplay() {
		_themeDisplayLocale.remove();
	}

	private static final ThreadLocal<Locale> _defaultLocale = new CentralizedThreadLocal<>(
			LocaleThreadLocal.class + "._defaultLocale");
	private static final ThreadLocal<Locale> _siteDefaultLocale = new CentralizedThreadLocal<>(
			LocaleThreadLocal.class + "._siteDefaultLocale");
	private static final ThreadLocal<Locale> _themeDisplayLocale = new CentralizedThreadLocal<>(
			LocaleThreadLocal.class + "._themeDisplayLocale");

}
