package akses.ksei.co.id.investor.model;

import java.util.Date;

public record AuditRequest(int auditType, String moduleName, String className, String auditAction, Date auditActionDate,
		String clientIp, String serverPort, String additionalInfo) {
}
