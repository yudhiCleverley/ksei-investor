package akses.ksei.co.id.investor.i18n;

import java.util.Locale;

import lombok.Data;

@Data
public class LocalHolder {

	private Locale currentLocale;

	public Locale getCurrentLocale() {
		return currentLocale;
	}

	public void setCurrentLocale(Locale currentLocale) {
		this.currentLocale = currentLocale;
	}

}
