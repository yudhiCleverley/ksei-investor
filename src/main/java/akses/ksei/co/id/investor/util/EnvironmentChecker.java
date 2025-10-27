package akses.ksei.co.id.investor.util;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentChecker {

	@Autowired
	private Environment environment;

	public boolean isRunningInKubernetes() {
		// Cek KUBERNETES_SERVICE_HOST
		if (environment.getProperty("KUBERNETES_SERVICE_HOST") != null) {

			return true;
		}

		// Cek file serviceaccount
		if (Files.exists(Paths.get("/var/run/secrets/kubernetes.io/serviceaccount/"))) {

			return true;
		}

		return false;
	}

	public boolean isRunningInLocal() {

		return !isRunningInKubernetes();
	}

}
