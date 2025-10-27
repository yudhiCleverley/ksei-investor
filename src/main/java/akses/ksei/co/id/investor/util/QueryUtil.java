package akses.ksei.co.id.investor.util;

public class QueryUtil {

	private QueryUtil() {
		throw new UnsupportedOperationException("Utility class should not be instantiated");
	}

	// SIDGEN Investor
	public static String sqlInvestor(String date, String dateFormat) {

		StringBuilder query = new StringBuilder();
		query.append(" SELECT ");
		query.append(" inv_id, ");
		query.append(" inv_name, ");
		query.append(" lst_upd_ts ");
		query.append(" FROM data_inv_detail ");
		query.append(" WHERE code_sta = 0 ");
		query.append(" AND inv_id IS NOT NULL ");
		if (Validator.isNotNull(date)) {
			query.append(" AND TRUNC(lst_upd_ts) = TO_DATE('" + date + "', '" + dateFormat + "') ");
		}
		query.append(" ORDER BY lst_upd_ts DESC ");

		return query.toString();
	}

	public static String _sqlInvestor(String date) {

		StringBuilder query = new StringBuilder();
		query.append(" WITH ranked_investors AS ( ");
		query.append(" SELECT ");
		query.append(" inv_id, ");
		query.append(" inv_name, ");
		query.append(" lst_upd_ts, ");
		query.append(" ROW_NUMBER() OVER (PARTITION BY inv_id ORDER BY lst_upd_ts DESC) AS row_num ");
		query.append(" FROM data_inv_detail ");
		query.append(" WHERE code_sta = 0 ");
		query.append(" AND inv_id IS NOT NULL ");
		if (Validator.isNotNull(date)) {
			query.append(" AND TRUNC(lst_upd_ts) = TO_DATE(:date, :fmt) ");
		}
		query.append(" ) ");
		query.append(" SELECT inv_id, inv_name, lst_upd_ts ");
		query.append(" FROM ranked_investors ");
		query.append(" WHERE row_num = 1 ");
		query.append(" ORDER BY lst_upd_ts DESC ");

		return query.toString();
	}

	public static String sqlCountInvestor(String date, String dateFormat) {

		StringBuilder query = new StringBuilder();
		query.append(" SELECT COUNT(*) ");
		query.append(" FROM data_inv_detail ");
		query.append(" WHERE code_sta = 0 ");
		query.append(" AND inv_id IS NOT NULL ");
		if (Validator.isNotNull(date)) {
			query.append(" AND TRUNC(lst_upd_ts) = TO_DATE('" + date + "', '" + dateFormat + "') ");
		}

		return query.toString();
	}

	public static String _sqlCountInvestor(String date) {

		StringBuilder query = new StringBuilder();
		query.append(" WITH ranked_investors AS ( ");
		query.append(" SELECT ");
		query.append(" inv_id, ");
		query.append(" inv_name, ");
		query.append(" lst_upd_ts, ");
		query.append(" ROW_NUMBER() OVER (PARTITION BY inv_id ORDER BY lst_upd_ts DESC) AS row_num ");
		query.append(" FROM data_inv_detail ");
		query.append(" WHERE code_sta = 0 ");
		query.append(" AND inv_id IS NOT NULL ");
		if (Validator.isNotNull(date)) {
			query.append(" AND TRUNC(lst_upd_ts) = TO_DATE(:date, :fmt) ");
		}
		query.append(" ) ");
		query.append(" SELECT COUNT(*) ");
		query.append(" FROM ranked_investors ");
		query.append(" WHERE row_num = 1 ");

		return query.toString();
	}

	// public static String sqlInvestor(String date, String dateFormat) throws
	// Exception {
	//
	// StringBuilder query = new StringBuilder();
	// query.append(" SELECT ");
	// query.append(" inv_id AS sid, ");
	// query.append(" inv_name AS name, ");
	// query.append(" lst_upd_ts AS last_update ");
	// query.append(" FROM data_inv_detail ");
	// query.append(" WHERE code_sta = 0 ");
	// query.append(" AND TRUNC(lst_upd_ts) = TO_DATE('" + date + "', '" +
	// dateFormat + "') ");
	// query.append(" ORDER BY inv_id ASC ");
	//
	// String sql = query.toString();
	//
	// return sql;
	// }

	// public static String sqlCountInvestor(String date, String dateFormat) throws
	// Exception {
	//
	// StringBuilder query = new StringBuilder();
	// query.append(" SELECT COUNT(*) ");
	// query.append(" FROM data_inv_detail ");
	// query.append(" WHERE code_sta = 0 ");
	// query.append(" AND TRUNC(lst_upd_ts) = TO_DATE('" + date + "', '" +
	// dateFormat + "') ");
	//
	// String sql = query.toString();
	//
	// return sql;
	// }

	// KSEI Investor
	public static String sqlCountExistingInvestor() {

		StringBuilder query = new StringBuilder();
		query.append(" SELECT COUNT(*) ");
		query.append(" FROM ksei_investor ");

		return query.toString();
	}

	public static String sqlExistingInvestor() {

		StringBuilder query = new StringBuilder();
		query.append(" SELECT i.sid FROM ksei_investor i ");

		return query.toString();
	}

	public static String sqlExistingInvestor(String date) {

		StringBuilder query = new StringBuilder();
		query.append(" SELECT i.sid FROM ksei_investor i WHERE TRUNC(i.last_update) = '" + date + "' ");

		return query.toString();
	}

	public static String sqlExistingInvestorBySid(String sid) {

		StringBuilder query = new StringBuilder();
		query.append(" SELECT * FROM ksei_investor i WHERE i.sid = '" + sid + "' ");

		return query.toString();
	}

	public static String sqlExistingInvestors() {

		StringBuilder query = new StringBuilder();
		query.append(" SELECT ");
		query.append(" SID, ");
		query.append(" NAME, ");
		query.append(" LAST_UPDATE ");
		query.append(" FROM ksei_investor ");
		query.append(" WHERE SID IN (:sids) ");

		return query.toString();
	}

	public static String sqlInsertInvestor() {

		StringBuilder query = new StringBuilder();
		query.append(" INSERT INTO ksei_investor ");
		query.append(" (id, sid, name, last_update) ");
		query.append(" VALUES (?, ?, ?, ?) ");

		return query.toString();
	}

	public static String sqlUpdateInvestor() {

		StringBuilder query = new StringBuilder();
		query.append(" UPDATE ksei_investor ");
		query.append(" SET name = ?, last_update = ? WHERE sid = ? ");

		return query.toString();
	}

	public static String sqlParameter(String groupCode, String paramCode) {

		StringBuilder query = new StringBuilder();
		query.append(" SELECT ");
		query.append(" p.param_code AS code, ");
		query.append(" p.param_value ");
		query.append(" FROM ksei_parameter p ");
		query.append(" LEFT JOIN ksei_parameter_group pg on pg.parameter_group_id = p.parameter_group_id ");
		query.append(" WHERE ");
		query.append(" pg.group_code = '" + groupCode + "' AND ");
		query.append(" p.param_code = '" + paramCode + "' AND ");
		query.append(" p.status = '1' AND ");
		query.append(" p.reserved = '1' ");

		return query.toString();
	}

	/**
	 * query for insert audit trail
	 * 
	 * @return
	 * @throws Exception
	 */
	public static String sqlInserAuditTrail() {

		StringBuilder query = new StringBuilder();
		query.append(" INSERT INTO ksei_audittrail ( ");
		query.append(" audit_id, ");
		query.append(" audit_type, ");
		query.append(" module_name, ");
		query.append(" class_name, ");
		query.append(" prim_key, ");
		query.append(" name, ");
		query.append(" login_id, ");
		query.append(" email, ");
		query.append(" link_email, ");
		query.append(" version, ");
		query.append(" audit_action, ");
		query.append(" audit_action_by, ");
		query.append(" audit_action_date, ");
		query.append(" client_ip, ");
		query.append(" otp_number, ");
		query.append(" mobile_number, ");
		query.append(" session_id, ");
		query.append(" server_port, ");
		query.append(" additional_info, ");
		query.append(" created_by, ");
		query.append(" created_date, ");
		query.append(" modified_by, ");
		query.append(" modified_date ");
		query.append(" ) VALUES ( ");
		query.append(
				" :auditId, :auditType, :moduleName, :className, :primKey, :name, :loginId, :email, :linkEmail, :version, ");
		query.append(
				" :auditAction, :auditActionBy, :auditActionDate, :clientIp, :otpNumber, :mobileNumber, :sessionId, :serverPort, ");
		query.append(" :additionalInfo, :createdBy, :createdDate, :modifiedBy, :modifiedDate ) ");

		return query.toString();
	}

}
