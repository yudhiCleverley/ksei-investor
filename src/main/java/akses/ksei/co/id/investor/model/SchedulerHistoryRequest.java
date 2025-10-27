package akses.ksei.co.id.investor.model;

import java.util.Date;

public record SchedulerHistoryRequest(long schedulerId, Date createdDate, Date startProcess, Date endProcess,
		String batchStatus, String failedMessage, String successMessage, String parameterInput) {
}
