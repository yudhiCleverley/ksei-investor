package akses.ksei.co.id.investor.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import akses.ksei.co.id.investor.entity.Investor;

@Repository
public interface InvestorRepository extends JpaRepository<Investor, Long> {

	Investor findById(long id);

	Investor findBySid(String sid);

	List<Investor> findListBySid(String sid);

}
