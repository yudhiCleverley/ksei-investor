package akses.ksei.co.id.investor.mail;

public class MailTemplateModel {

	private String code;

	private String content;

	private String subject;

	public MailTemplateModel() {
	}

	public MailTemplateModel(String code, String content, String subject) {
		this.code = code;
		this.content = content;
		this.subject = subject;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

}
