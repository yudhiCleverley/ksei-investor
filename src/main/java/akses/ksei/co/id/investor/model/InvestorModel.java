package akses.ksei.co.id.investor.model;

import java.util.Date;

public class InvestorModel {

	private String sid;

	private String name;

	private Date lastUpdate;

	public InvestorModel() {
	}

	public InvestorModel(String sid, String name, Date lastUpdate) {
		this.sid = sid;
		this.name = name;
		this.lastUpdate = lastUpdate;
	}

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

}
