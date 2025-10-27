package akses.ksei.co.id.investor.mail;

import org.springframework.web.multipart.MultipartFile;

public class MailModel {

	private String[] recipients;

	private String subject;

	private String body;

	private Boolean isHtml;

	private String base64Logo;

	private String fileExtention;

	private MultipartFile[] attachments;

	public MailModel(String[] recipients, String subject, String body, Boolean isHtml, String base64Logo,
			String fileExtention, MultipartFile[] attachments) {
		this.recipients = recipients;
		this.subject = subject;
		this.body = body;
		this.isHtml = isHtml;
		this.base64Logo = base64Logo;
		this.fileExtention = fileExtention;
		this.attachments = attachments;
	}

	public String[] getRecipients() {
		return recipients;
	}

	public void setRecipients(String[] recipients) {
		this.recipients = recipients;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Boolean getIsHtml() {
		return isHtml;
	}

	public void setIsHtml(Boolean isHtml) {
		this.isHtml = isHtml;
	}

	public String getBase64Logo() {
		return base64Logo;
	}

	public void setBase64Logo(String base64Logo) {
		this.base64Logo = base64Logo;
	}

	public String getFileExtention() {
		return fileExtention;
	}

	public void setFileExtention(String fileExtention) {
		this.fileExtention = fileExtention;
	}

	public MultipartFile[] getAttachments() {
		return attachments;
	}

	public void setAttachments(MultipartFile[] attachments) {
		this.attachments = attachments;
	}

}
