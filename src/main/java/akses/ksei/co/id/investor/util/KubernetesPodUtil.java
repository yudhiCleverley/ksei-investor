package akses.ksei.co.id.investor.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class KubernetesPodUtil {

	@Autowired
	private Environment environment;

	public String getPodIp() {

		return environment.getProperty("POD_IP", "unknown");
	}

	public String getPodName() {

		return environment.getProperty("POD_NAME", "unknown");
	}

	public String getNodeName() {

		return environment.getProperty("NODE_NAME", "unknown");
	}

	public String getServerPort() {

		return environment.getProperty("SERVER_PORT", "8080");
	}

}
