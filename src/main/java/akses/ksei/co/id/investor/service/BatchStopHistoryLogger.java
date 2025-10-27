package akses.ksei.co.id.investor.service;

import java.util.Date;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import akses.ksei.co.id.investor.i18n.I18nUtil;
import akses.ksei.co.id.investor.util.StringPool;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BatchStopHistoryLogger {

	private final Logger _log = LoggerFactory.getLogger(this.getClass());

	private final SchedulerMgmtServiceImpl schedulerMgmtServiceImpl;

	private final I18nUtil i18nUtil;

	private final PlatformTransactionManager transactionManager;

	public void logStopHistory(Long schedulerId, String date) {
		try {
			TransactionTemplate template = new TransactionTemplate(transactionManager);
			template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

			template.executeWithoutResult(status -> {
				Date now = new Date();
				Locale defaultLocale = new Locale("id", "ID");

				String message = StringPool.BLANK;
				try {
					message = i18nUtil.getMessage("success.stop.batch.job", defaultLocale);
				} catch (Exception e) {
					_log.error(e.getMessage(), e);
				}

				schedulerMgmtServiceImpl.insertSchedulerHistory(schedulerId, now, now, BatchStatus.STOPPED.name(), null,
						message, date);

				// _log.info("STOPPED history inserted for schedulerId: {}", schedulerId);
			});
		} catch (Exception e) {
			_log.error("Failed insert STOPPED history: " + e.getMessage(), e);
		}
	}

}
