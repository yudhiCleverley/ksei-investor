package akses.ksei.co.id.investor.service;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.stereotype.Service;

@Service
public class JobStopFlagService {

	private final AtomicBoolean stopFlag = new AtomicBoolean(false);

	public void setStop() {
		stopFlag.set(true);
	}

	public boolean shouldStop() {
		return stopFlag.get();
	}

	public void reset() {
		stopFlag.set(false);
	}

}
