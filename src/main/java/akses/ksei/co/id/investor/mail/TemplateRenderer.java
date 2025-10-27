package akses.ksei.co.id.investor.mail;

import java.util.Map;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

public class TemplateRenderer {

	private static final TemplateEngine templateEngine;

	private TemplateRenderer() {
		throw new UnsupportedOperationException("Utility class");
	}

	static {
		StringTemplateResolver templateResolver = new StringTemplateResolver();
		templateResolver.setTemplateMode(TemplateMode.HTML);
		templateResolver.setCacheable(false);
		templateResolver.setCheckExistence(true);

		templateEngine = new TemplateEngine();
		templateEngine.setTemplateResolver(templateResolver);
	}

	public static String render(String templateContent, Map<String, Object> variables) {
		Context context = new Context();
		context.setVariables(variables);
		return templateEngine.process(templateContent, context);
	}

}
