package akses.ksei.co.id.investor.batch;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobInterruptedException;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;
import org.springframework.stereotype.Component;

@Component
public class JobFailureListener extends StepExecutionListenerSupport {

	private final Logger _log = LoggerFactory.getLogger(this.getClass());

	@Override
	public void beforeStep(StepExecution stepExecution) {
		// No implementation required
	}

	@Override
	@AfterStep
	public ExitStatus afterStep(StepExecution stepExecution) {
		List<Throwable> failures = stepExecution.getFailureExceptions();

		String stoppedBy = stepExecution.getExecutionContext().containsKey("stoppedBy")
				? stepExecution.getExecutionContext().getString("stoppedBy")
				: "";
		boolean manuallyStopped = stoppedBy.equalsIgnoreCase("USER");

		if (manuallyStopped) {
			_log.warn("Step [{}] dihentikan oleh user. JobInterruptedException diabaikan.",
					stepExecution.getStepName());
			return ExitStatus.STOPPED;
		}

		if (failures.size() == 1 && isCausedByJobInterrupted(failures.get(0))) {
			_log.warn("Step [{}] dihentikan (JobInterruptedException terdeteksi dalam NonSkippableReadException).",
					stepExecution.getStepName());
			return ExitStatus.STOPPED;
		}

		if (!failures.isEmpty()) {
			_log.error("Step [{}] gagal dengan {} exception(s):", stepExecution.getStepName(), failures.size());
			for (int i = 0; i < failures.size(); i++) {
				Throwable t = failures.get(i);
				_log.error("Exception #{}: {} - {}", i + 1, t.getClass().getSimpleName(), t.getMessage(), t);
			}
		}

		return super.afterStep(stepExecution);
	}

	private boolean isCausedByJobInterrupted(Throwable ex) {
		while (ex != null) {
			if (ex instanceof JobInterruptedException) {
				return true;
			}
			ex = ex.getCause();
		}
		return false;
	}

}
