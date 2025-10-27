package akses.ksei.co.id.investor.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class JobHealthServiceImpl {

	Logger _log = LoggerFactory.getLogger(this.getClass());

	// Map untuk menyimpan waktu eksekusi terakhir setiap job
	private final Map<String, LocalDateTime> lastRunTime = new ConcurrentHashMap<>();

	// Menyimpan waktu terakhir job berhasil dijalankan
	public void updateLastRunTime(String jobName) {
		lastRunTime.put(jobName, LocalDateTime.now());
	}

	// Mengevaluasi apakah job berjalan sesuai jadwal
	public boolean isJobHealthy(String jobName, Duration threshold) {
		LocalDateTime lastRun = lastRunTime.get(jobName);
		// _log.info("Job {} last run time: {}", jobName, lastRun);

		if (lastRun == null) {
			_log.info("Job {} is running for the first time. Skipping health check.", jobName);
			return true; // Anggap sehat pada eksekusi pertama
		}
		// Hitung durasi sejak terakhir job dijalankan
		Duration durationSinceLastRun = Duration.between(lastRun, LocalDateTime.now());
		return durationSinceLastRun.compareTo(threshold) <= 0;
	}

	// Untuk debugging atau monitoring, mendapatkan waktu eksekusi terakhir semua
	// job
	public Map<String, LocalDateTime> getAllJobStatus() {
		return Collections.unmodifiableMap(lastRunTime);
	}

}
