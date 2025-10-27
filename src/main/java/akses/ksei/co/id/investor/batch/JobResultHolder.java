package akses.ksei.co.id.investor.batch;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

@Component
public class JobResultHolder {

	private final Map<String, String> jobResults = new ConcurrentHashMap<>();

	private final Map<String, AtomicInteger> insertCounts = new ConcurrentHashMap<>();
	private final Map<String, AtomicInteger> updateCounts = new ConcurrentHashMap<>();
	private final Map<String, AtomicInteger> duplicateCounts = new ConcurrentHashMap<>();
	private final Map<String, AtomicInteger> totalCounts = new ConcurrentHashMap<>();

	// ==== Success message ====
	public void setSuccessMessage(String jobId, String message) {
		jobResults.put(jobId, message);
	}

	public String getSuccessMessage(String jobId) {
		return jobResults.getOrDefault(jobId, "");
	}

	// ==== Total pulled data ====
	public void setTotalCount(String jobId, int count) {
		totalCounts.put(jobId, new AtomicInteger(count));
	}

	public int getTotalCount(String jobId) {
		return totalCounts.getOrDefault(jobId, new AtomicInteger(0)).get();
	}

	// ==== Insert count ====
	public void addInsert(String jobId, int count) {
		insertCounts.computeIfAbsent(jobId, k -> new AtomicInteger(0)).addAndGet(count);
	}

	public int getInsertCount(String jobId) {
		return insertCounts.getOrDefault(jobId, new AtomicInteger(0)).get();
	}

	// ==== Update count ====
	public void addUpdate(String jobId, int count) {
		updateCounts.computeIfAbsent(jobId, k -> new AtomicInteger(0)).addAndGet(count);
	}

	public int getUpdateCount(String jobId) {
		return updateCounts.getOrDefault(jobId, new AtomicInteger(0)).get();
	}

	// ==== Duplicate count ====
	public void addDuplicate(String jobId, int count) {
		duplicateCounts.computeIfAbsent(jobId, k -> new AtomicInteger(0)).addAndGet(count);
	}

	public int getDuplicateCount(String jobId) {
		return duplicateCounts.getOrDefault(jobId, new AtomicInteger(0)).get();
	}

	// ==== Format summary message ====
	public String formatSuccessMessage(String jobId, String jobName, String status, long durationMillis) {
		return String.format(
				"Job: [%s] completed with status: [%s], duration: %dms, Pulled: %d, Inserted: %d, Updated: %d, Duplicate: %d",
				jobName, status, durationMillis, getTotalCount(jobId), getInsertCount(jobId), getUpdateCount(jobId),
				getDuplicateCount(jobId));
	}

	public void clear(String jobId) {
		jobResults.remove(jobId);
		insertCounts.remove(jobId);
		updateCounts.remove(jobId);
		duplicateCounts.remove(jobId);
		totalCounts.remove(jobId);
	}

}
