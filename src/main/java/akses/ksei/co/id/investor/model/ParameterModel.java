package akses.ksei.co.id.investor.model;

public class ParameterModel {

	private String code;

	private String paramValue;

	public ParameterModel(String code, String paramValue) {
		this.code = code;
		this.paramValue = paramValue;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getParamValue() {
		return paramValue;
	}

	public void setParamValue(String paramValue) {
		this.paramValue = paramValue;
	}

}
