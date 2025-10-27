package akses.ksei.co.id.investor.enumeration;

import java.util.HashMap;
import java.util.Map;

public enum JobStatusEnum {

	RUNNING(0, "Running"), 
	SUCCESS(1, "Success"), 
	FAILED(2, "Failed"), 
	STOPPED(3, "Canceled");

	private Integer id;
	private String type;

	private JobStatusEnum(Integer id, String type) {
		this.id = id;
		this.type = type;
	}

	public Integer getId() {
		return id;
	}

	public String getType() {
		return type;
	}

	private static final Map<Integer, JobStatusEnum> lookup = new HashMap<>();
	static {
		for (JobStatusEnum d : JobStatusEnum.values())
			lookup.put(d.getId(), d);
	}

	public static String getMsgKey(int id) {
		return lookup.get(id).getType();
	}

	public static int fromBatchStatus(String batchStatus) {
		if (batchStatus == null || batchStatus.trim().isEmpty()) {
			return FAILED.getId();
		}

		try {
			switch (batchStatus.toUpperCase()) {
			case "COMPLETED":
				return SUCCESS.getId();
			case "FAILED":
				return FAILED.getId();
			case "STOPPED":
			case "STOPPING":
				return STOPPED.getId();
			case "STARTING":
			case "STARTED":
			case "RUNNING":
				return RUNNING.getId();
			default:
				return FAILED.getId();
			}
		} catch (Exception e) {
			return FAILED.getId();
		}
	}

}
