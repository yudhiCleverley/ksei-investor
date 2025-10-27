package akses.ksei.co.id.investor.batch;

import org.springframework.stereotype.Service;

@Service
public class ChunkSizeCalculator {

	public int calculateChunkSize(int totalDataCount) {
		if (totalDataCount < 1_000) {
			return 100;
		} else if (totalDataCount < 10_000) {
			return 500;
		} else {
			return 1_000;
		}
	}

}
