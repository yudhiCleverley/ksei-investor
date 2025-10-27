package akses.ksei.co.id.investor.i18n;

import java.util.Locale;

import javax.annotation.Resource;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class I18nUtil {

	private final MessageSource messageSource;

	@Resource(name = "localHolder")
	private LocalHolder localHolder;

	public Locale getCurrentLocale() {
		return LocaleContextHolder.getLocale();
	}

	public String getMessage(String code, String... args) {
		return messageSource.getMessage(code, args, localHolder.getCurrentLocale());
	}

	public String getMessage(String code, Locale locale) {
		return messageSource.getMessage(code, null, locale);
	}

}
