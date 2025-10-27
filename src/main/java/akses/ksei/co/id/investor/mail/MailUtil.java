package akses.ksei.co.id.investor.mail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import akses.ksei.co.id.investor.util.Validator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MailUtil {

	private final Logger _log = LoggerFactory.getLogger(this.getClass());

	private String LOGO = "logo";

	private final MailProperties _mailProperties;

	private final JavaMailSender _mailSender;

	public void sendMail(MailModel model) throws MessagingException, IOException {

		MimeMessage message = _mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
				StandardCharsets.UTF_8.name());
		helper.setFrom(_mailProperties.getSender(), "AKSes KSEI");
		helper.setTo(model.getRecipients());
		helper.setSubject(model.getSubject());
		helper.setText(model.getBody(), model.getIsHtml());

		if (Validator.isNotNull(model.getBase64Logo())) {
			byte[] decoder = Base64.getDecoder().decode(model.getBase64Logo());
			InputStreamSource dataSource = new ByteArrayResource(decoder);
			helper.addInline(LOGO, dataSource, model.getFileExtention());
		}

		_mailSender.send(message);

		if (_log.isInfoEnabled()) {
			_log.info("Sending mail {} to {} success", model.getSubject(), String.join(", ", model.getRecipients()));
		}
	}

	public void sendMailWithAttachment(MailModel model) throws MessagingException, IOException {

		MimeMessage message = _mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
				StandardCharsets.UTF_8.name());
		helper.setFrom(_mailProperties.getSender(), "AKSes KSEI");
		helper.setTo(model.getRecipients());
		helper.setSubject(model.getSubject());
		helper.setText(model.getBody(), model.getIsHtml());

		if (Validator.isNotNull(model.getBase64Logo())) {
			byte[] decoder = Base64.getDecoder().decode(model.getBase64Logo());
			InputStreamSource dataSource = new ByteArrayResource(decoder);
			helper.addInline(LOGO, dataSource, model.getFileExtention());
		}

		if (model.getAttachments() != null) {
			for (MultipartFile file : model.getAttachments()) {
				if (file != null) {
					String filename = Objects.requireNonNull(file.getOriginalFilename(),
							"Attachment filename cannot be null");
					helper.addAttachment(filename, file);
				}
			}
		}

		_mailSender.send(message);

		if (_log.isInfoEnabled()) {
			_log.info("Sending mail {} to {} success", model.getSubject(), String.join(", ", model.getRecipients()));
		}
	}

	public void validate(String[] recipients, String body) {
		if (Validator.isNull(recipients)) {
			throw new IllegalArgumentException("Email recipient can't be empty!");
		}

		if (Validator.isNull(body)) {
			throw new IllegalArgumentException("Body can't be empty!");
		}
	}

	public String[] cleanEmailList(String raw) {
		if (raw == null || raw.isBlank())
			return new String[0];

		return Arrays.stream(raw.replace(";", ",").split(",")).map(String::trim).filter(s -> !s.isEmpty())
				.toArray(String[]::new);
	}

}
