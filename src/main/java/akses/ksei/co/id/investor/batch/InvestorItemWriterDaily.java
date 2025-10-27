package akses.ksei.co.id.investor.batch;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import akses.ksei.co.id.investor.entity.Investor;
import akses.ksei.co.id.investor.util.QueryUtil;
import akses.ksei.co.id.investor.util.StringPool;
import akses.ksei.co.id.investor.util.Validator;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InvestorItemWriterDaily implements ItemWriter<Investor> {

	private final Logger _log = LoggerFactory.getLogger(this.getClass());

	private final EntityManager _entityManager;

	private Set<String> existingSids = new HashSet<>();

	@SuppressWarnings("unchecked")
	public void loadExistingSids(String date) {
		String query = StringPool.BLANK;

		try {
			if (Validator.isNotNull(date)) {
				query = QueryUtil.sqlExistingInvestor(date);
			}
			// _log.info("Executing query to load existing SIDs: {}", query);

			if (Validator.isNotNull(query)) {
				List<String> resultList = _entityManager.createNativeQuery(query).getResultList();
				_log.info("resultList: {}", resultList.size());

				existingSids.clear();
				existingSids.addAll(resultList);
				// _log.info("Loaded existing SIDs for date {}: {}", date, existingSids.size());
			} else {
				existingSids.clear();
			}
		} catch (Exception e) {
			_log.error("Error loading existing SIDs: " + e.getMessage(), e);
			existingSids.clear();
		}
	}

	@Override
	public void write(List<? extends Investor> items) throws Exception {
		List<Investor> toPersist = new ArrayList<>();
		List<Investor> toMerge = new ArrayList<>();

		for (Investor investor : items) {
			if (existingSids.contains(investor.getSid())) {
				Investor existingInvestor = (Investor) _entityManager
						.createNativeQuery(QueryUtil.sqlExistingInvestorBySid(investor.getSid()), Investor.class)
						.getSingleResult();
				// _log.info("existingInvestor : " + existingInvestor.getId());

				if (existingInvestor != null && (!existingInvestor.getName().equals(investor.getName())
						|| !existingInvestor.getLastUpdate().equals(investor.getLastUpdate()))) {
					existingInvestor.setName(investor.getName());
					existingInvestor.setLastUpdate(investor.getLastUpdate());
					toMerge.add(existingInvestor);
				}
			} else {
				toPersist.add(investor);
			}
		}

		_log.info("toPersist: {}", toPersist.size());
		_log.info("toMerge: {}", toMerge.size());

		// do batch insert
		if (!toPersist.isEmpty()) {
			for (Investor investor : toPersist) {
				_entityManager.persist(investor);
			}
		}

		// do batch update
		if (!toMerge.isEmpty()) {
			for (Investor investor : toMerge) {
				_entityManager.merge(investor);
			}
		}

		// ensure all changes are saved in the batch
		_entityManager.flush();
	}

}
