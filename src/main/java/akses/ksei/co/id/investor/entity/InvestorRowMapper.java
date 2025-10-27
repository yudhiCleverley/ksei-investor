package akses.ksei.co.id.investor.entity;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class InvestorRowMapper implements RowMapper<Investor> {

	@Override
	public Investor mapRow(ResultSet rs, int rowNum) throws SQLException {
		Investor investor = new Investor();
		investor.setSid(rs.getString("inv_id"));
		investor.setName(rs.getString("inv_name"));
		investor.setLastUpdate(rs.getDate("lst_upd_ts"));
		return investor;
	}

}
