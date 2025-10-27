package akses.ksei.co.id.investor.batch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobInterruptedException;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import akses.ksei.co.id.investor.entity.Investor;
import akses.ksei.co.id.investor.service.InvestorImpl;
import akses.ksei.co.id.investor.service.JobStopFlagService;
import akses.ksei.co.id.investor.util.Validator;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InvestorItemWriter implements ItemWriter<Investor>, StepExecutionListener {

	private final Logger _log = LoggerFactory.getLogger(this.getClass());

	private final InvestorImpl _investorImpl;

	private final JobResultHolder _jobResultHolder;

	private final JobStopFlagService jobStopFlagService;

	private final AtomicInteger chunkCounter = new AtomicInteger(0);

	private StepExecution stepExecution;

	@Override
	public void beforeStep(StepExecution stepExecution) {
		this.stepExecution = stepExecution;
		jobStopFlagService.reset();
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		return ExitStatus.COMPLETED;
	}

	@Override
	public void write(List<? extends Investor> items) throws Exception {
		if (Thread.currentThread().isInterrupted()) {
			_log.warn("Writer thread was interrupted. Stopping writer...");
			throw new JobInterruptedException("Thread interrupted in writer");
		}

		if (jobStopFlagService.shouldStop()) {
			_log.warn("Job stop flag detected in writer. Throwing JobInterruptedException...");
			throw new JobInterruptedException("Job stop flag set in writer");
		}

		if (stepExecution != null && stepExecution.getJobExecution().isStopping()) {
			_log.warn("Job is stopping. Throwing JobInterruptedException in writer...");
			throw new JobInterruptedException("Job is stopping in writer");
		}

		long startTime = System.currentTimeMillis();

		if (items == null || items.isEmpty()) {
			_log.warn("No items to process.");
			return;
		}

		int chunkNumber = chunkCounter.incrementAndGet();

		Map<String, Investor> existingData = _investorImpl.fetchBySids(items);
		// _log.info("Fetched existing: {}", existingMembers.keySet());

		List<Investor> inserts = new ArrayList<>();
		List<Investor> updates = new ArrayList<>();
		classifyItems(items, existingData, inserts, updates);

		// save new data to database
		int inserted = processInserts(inserts);

		// update existing records
		int updated = processUpdates(updates);

		// Update ExecutionContext with processed data counts
		int duplicated = items.size() - (inserted + updated);
		updateExecutionContext(inserted, updated, duplicated);

		_log.info("Chunk #{} write complete: total={}, insert={}, update={}, duplicates={}, duration={} ms",
				chunkNumber, items.size(), inserts.size(), updates.size(), duplicated,
				(System.currentTimeMillis() - startTime));
	}

	public int classifyItems(List<? extends Investor> items, Map<String, Investor> existingData, List<Investor> inserts,
			List<Investor> updates) {
		int duplicateCount = 0;

		for (Investor item : items) {
			if (Validator.isNotNull(item.getSid())) {
				String key = item.getSid();
				Investor existing = existingData.get(key);

				if (existing == null) {
					inserts.add(item);
				} else {
					if (_investorImpl.hasChanged(item, existing)) {
						updates.add(item);
					} else {
						duplicateCount++;
					}
				}
			}
		}

		if (duplicateCount > 0 && _log.isDebugEnabled()) {
			_log.debug("Found {} duplicate records. These will not be processed.", duplicateCount);
		}

		return duplicateCount;
	}

	public int processInserts(List<Investor> inserts) {
		int inserted = 0;

		if (inserts.isEmpty()) {
			return inserted;
		}

		try {
			inserted = _investorImpl.insertBatch(inserts);
		} catch (Exception e) {
			_log.warn("Batch insert failed. Falling back to single inserts.");

			List<List<Investor>> batches = Lists.partition(inserts, 50);

			for (List<Investor> batch : batches) {
				try {
					inserted = _investorImpl.insertBatch(batch);
				} catch (Exception ex) {
					_log.warn("Small batch insert failed. Falling back to individual inserts.");

					try {
						inserted = _investorImpl.addInvestors(batch);
					} catch (Exception exc) {
						_log.error("Failed to individual inserts: {}", exc.getMessage(), exc);
					}
				}
			}
		}

		return inserted;
	}

	public int processUpdates(List<Investor> updates) {
		int updated = 0;

		if (updates.isEmpty()) {
			return updated;
		}

		try {
			updated = _investorImpl.updateBatch(updates);
		} catch (Exception e) {
			_log.error("Failed to update records: {}", e.getMessage(), e);
		}

		return updated;
	}

	public void updateExecutionContext(int insertCount, int updateCount, int duplicateCount) {
		StepExecution se = Optional.ofNullable(StepSynchronizationManager.getContext())
				.map(StepContext::getStepExecution)
				.orElseThrow(() -> new IllegalStateException("StepContext or StepExecution is null"));
		ExecutionContext executionContext = se.getExecutionContext();

		executionContext.putInt("insertCount", executionContext.getInt("insertCount", 0) + insertCount);
		executionContext.putInt("updateCount", executionContext.getInt("updateCount", 0) + updateCount);
		executionContext.putInt("duplicateCount", executionContext.getInt("duplicateCount", 0) + duplicateCount);

		// Simpan juga ke JobResultHolder
		String jobId = stepExecution.getJobExecution().getJobId().toString();
		_jobResultHolder.addInsert(jobId, insertCount);
		_jobResultHolder.addUpdate(jobId, updateCount);
		_jobResultHolder.addDuplicate(jobId, duplicateCount);
	}

}
