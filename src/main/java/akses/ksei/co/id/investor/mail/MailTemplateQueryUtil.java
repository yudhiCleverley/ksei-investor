package akses.ksei.co.id.investor.mail;

public class MailTemplateQueryUtil {

	private MailTemplateQueryUtil() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static String sqlMailTemplates() {

		StringBuilder query = new StringBuilder();
		query.append(" SELECT ");
		query.append(" mt.code, ");
		query.append(" mt.subject, ");
		query.append(" mt.content ");
		query.append(" FROM ksei_mail_template mt ");
		query.append(" WHERE mt.code = :code ");

		return query.toString();
	}

	public static String sqlMailTemplate() {

		StringBuilder query = new StringBuilder();
		query.append(" SELECT ");
		query.append(" mt.subject, ");
		query.append(" mt.content ");
		query.append(" FROM ksei_mail_template mt ");
		query.append(" WHERE mt.code = :code ");

		return query.toString();
	}

}
