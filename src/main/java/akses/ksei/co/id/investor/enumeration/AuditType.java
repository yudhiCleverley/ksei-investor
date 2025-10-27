package akses.ksei.co.id.investor.enumeration;

import java.util.HashMap;
import java.util.Map;

public enum AuditType {
	
	ROLE_AUDIT(1, "lbl-role-audit"),
	USER_AUDIT(2, "lbl-user-audit"), 
	CONTACT_AUDIT(3, "lbl-contact-audit"),
	PASSWORD_AUDIT(4, "lbl-password-audit"),
	PASSWORD_POLICY_AUDIT(5, "lbl-password-policy-audit"),
	ADDRESS_AUDIT(6, "lbl-address-audit"),
	NOTIFICATION_AUDIT(7, "lbl-notification-audit"),
	MAIL_TEMPLATE_AUDIT(8, "lbl-mail-template-audit"),
	PARAMETER_AUDIT(9, "lbl-parameter-audit"),
	GROUP_AUDIT(10, "lbl-group-audit"),
	LAYOUT_AUDIT(11, "lbl-layout-audit"),
	SHORTCUT_AUDIT(12, "lbl-shortcut-audit"),
	OJK_AUDIT(13, "lbl-ojk-audit"),
	SCHEDULER_AUDIT(14, "lbl-scheduler-audit");

	private Integer id;
	private String type;

	private AuditType(Integer id, String type) {
		this.id = id;
		this.type = type;
	}

	public Integer getId() {
		return id;
	}

	public String getType() {
		return type;
	}

	private static final Map<Integer, AuditType> lookup = new HashMap<>();
	static {
		for (AuditType d : AuditType.values())
			lookup.put(d.getId(), d);
	}

	public static String getMsgKey(int id) {
		return lookup.get(id).getType();
	}

}
