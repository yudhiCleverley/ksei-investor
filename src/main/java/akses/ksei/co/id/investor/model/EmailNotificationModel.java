package akses.ksei.co.id.investor.model;

public class EmailNotificationModel {

	private boolean sentEmailSuccess;
	private boolean sentEmailFailed;
	private String emailSuccess;
	private String emailFailed;

	public EmailNotificationModel() {
	}

	public EmailNotificationModel(boolean sentEmailSuccess, boolean sentEmailFailed, String emailSuccess,
			String emailFailed) {
		this.sentEmailSuccess = sentEmailSuccess;
		this.sentEmailFailed = sentEmailFailed;
		this.emailSuccess = emailSuccess;
		this.emailFailed = emailFailed;
	}

	public boolean isSentEmailSuccess() {
		return sentEmailSuccess;
	}

	public boolean isSentEmailFailed() {
		return sentEmailFailed;
	}

	public String getEmailSuccess() {
		return emailSuccess;
	}

	public String getEmailFailed() {
		return emailFailed;
	}

}
