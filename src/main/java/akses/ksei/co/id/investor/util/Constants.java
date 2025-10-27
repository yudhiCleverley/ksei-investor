package akses.ksei.co.id.investor.util;

import java.util.Arrays;
import java.util.List;

public class Constants {

	// investor params
	public static final String INVESTOR_TABLE_NAME = "ksei_investor";
	public static final String INVESTOR_COLUMN_ID = "id";
	public static final String INVESTOR_SEQUENCE_NAME = "KSEI_INVESTOR_SEQ";

	// audit params
	public static final String AUDIT_TABLE_NAME = "ksei_audittrail";
	public static final String AUDIT_COLUMN_ID = "audit_id";
	public static final String AUDIT_SEQUENCE_NAME = "KSEI_AUDITTRAIL_SEQ";

	// notification
	public static final String NOTIFICATION_TABLE_NAME = "ksei_notification";
	public static final String NOTIFICATION_COLUMN_ID = "notification_id";
	public static final String NOTIFICATION_SEQUENCE_NAME = "KSEI_NOTIFICATION_SEQ";

	// history email
	public static final String HISTORY_EMAIL_SEQUENCE_NAME = "KSEI_HISTORY_EMAIL_SEQ";

	// scheduler management params
	public static final String SCHEDULER_MANAGEMENT_TABLE_NAME = "ksei_scheduler_management";

	// scheduler history params
	public static final String SCHEDULER_HISTORY_TABLE_NAME = "ksei_scheduler_history";
	public static final String SCHEDULER_HISTORY_COLUMN_ID = "history_id";
	public static final String SCHEDULER_HISTORY_SEQUENCE_NAME = "KSEI_SCHEDULER_HISTORY_SEQ";

	public static final List<String> VALID_TABLES = Arrays.asList(INVESTOR_TABLE_NAME, AUDIT_TABLE_NAME,
			SCHEDULER_HISTORY_TABLE_NAME, NOTIFICATION_TABLE_NAME);
	public static final List<String> VALID_COLUMNS = Arrays.asList(INVESTOR_COLUMN_ID, AUDIT_COLUMN_ID,
			SCHEDULER_HISTORY_COLUMN_ID, NOTIFICATION_COLUMN_ID);
	public static final List<String> VALID_SEQUENCES = Arrays.asList(INVESTOR_SEQUENCE_NAME, AUDIT_SEQUENCE_NAME,
			SCHEDULER_HISTORY_SEQUENCE_NAME, HISTORY_EMAIL_SEQUENCE_NAME, NOTIFICATION_SEQUENCE_NAME);

	public static final String MODULE_NAME = "ksei-investor-service";
	public static final String CLIENT_IP_KEY = "clientIp";
	public static final String SERVER_PORT_KEY = "serverPort";

	public static final String PULL_DATA_INVESTOR_CODE = "PULL_DATA_INVESTOR";
	public static final String PULL_INVESTOR_DATA = "TARIK DATA INVESTOR";
	public static final String JOBS_NAME = "JOBS_TARIK_DATA_INVESTOR";

	public static final String EMAIL_SETTINGS_GROUP_CODE = "EMAIL_SETTINGS";
	public static final String SITE_SETTINGS_GROUP_CODE = "SITE_SETTINGS";
	public static final String EMAIL_LOGO_CODE = "email.logo";
	public static final String DEFAULT_PORTAL_WEB_CODE = "default.portal.web";

	// parameter
	public static final String SCHEDULER_SETTINGS_GROUP_CODE = "SCHEDULER_SETTINGS";
	public static final String TRANSFER_DATA_CRON_EXPRESSION_CODE = "transfer.data.cron.expression";
	public static final String TRANSFER_DATA_IVESTOR_DATE_CODE = "transfer.data.investor.date";
	public static final String TRANSFER_DATA_IVESTOR_DATE_DEFAULT_VALUE = "sysdate";
	public static final String TRANSFER_DATA_DATE_FORMAT = "DD/MM/YYYY";

}
