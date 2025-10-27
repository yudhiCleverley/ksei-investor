package akses.ksei.co.id.investor.batch;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

import akses.ksei.co.id.investor.enumeration.AuditType;
import akses.ksei.co.id.investor.model.AuditRequest;
import akses.ksei.co.id.investor.service.AuditTrailImpl;
import akses.ksei.co.id.investor.util.Constants;
import akses.ksei.co.id.investor.util.ServerInfoUtil;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InvestorStepExecutionListener implements StepExecutionListener {

	private final Logger _log = LoggerFactory.getLogger(this.getClass());

	private final AuditTrailImpl _auditTrailImpl;

	private final ServerInfoUtil _serverInfoUtil;

	@Override
	public void beforeStep(StepExecution stepExecution) {
		String serverIp = _serverInfoUtil.getServerIp();
		String serverPort = _serverInfoUtil.getHostName();

		ExecutionContext executionContext = stepExecution.getExecutionContext();
		executionContext.put(Constants.CLIENT_IP_KEY, serverIp);
		executionContext.put(Constants.SERVER_PORT_KEY, serverPort);
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		try {
			String stepName = stepExecution.getStepName();
			int readCount = stepExecution.getReadCount();
			int writeCount = stepExecution.getWriteCount();

			// retrieve insertCount and updateCount from ExecutionContext
			ExecutionContext executionContext = stepExecution.getExecutionContext();
			int insertCount = executionContext.getInt("insertCount", 0);
			int updateCount = executionContext.getInt("updateCount", 0);
			int duplicateCount = executionContext.getInt("duplicateCount", 0);

			String auditMessage = String.format(
					"Step [%s] completed - Read: %d, Write: %d, Insert: %d, Update: %d, Duplicate: %d", stepName,
					readCount, writeCount, insertCount, updateCount, duplicateCount);

			String clientIp = (String) executionContext.get(Constants.CLIENT_IP_KEY);
			String serverPort = (String) executionContext.get(Constants.SERVER_PORT_KEY);

			clientIp = (clientIp == null) ? "unknown" : clientIp;
			serverPort = (serverPort == null) ? "unknown" : serverPort;

			// _log.info("_clientIp: " + clientIp);
			// _log.info("_serverPort: " + serverPort);

			// add audit trail
			AuditRequest request = new AuditRequest(AuditType.SCHEDULER_AUDIT.getId(), Constants.MODULE_NAME,
					this.getClass().getName(), Constants.PULL_INVESTOR_DATA, new Date(), clientIp, serverPort,
					auditMessage);
			_auditTrailImpl.createAuditTrail(request);
		} catch (Exception e) {
			_log.error("failed add audit trail : " + e.getMessage(), e);
		}

		return stepExecution.getExitStatus();
	}

}
