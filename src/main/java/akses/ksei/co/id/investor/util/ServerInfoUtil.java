package akses.ksei.co.id.investor.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ServerInfoUtil {

	Logger _log = LoggerFactory.getLogger(ServerInfoUtil.class);

	@Value("${server.port}")
	private String serverPort;

	@Autowired
	private EnvironmentChecker _environmentChecker;

	@Autowired
	private KubernetesPodUtil _kubernetesPodUtil;

	public String getServerIp() {
		_log.info("isRunningInLocal : " + _environmentChecker.isRunningInLocal());

		try {
			if (_environmentChecker.isRunningInLocal()) {

				return InetAddress.getLocalHost().getHostAddress();
			} else {

				return _kubernetesPodUtil.getPodIp();
			}
		} catch (UnknownHostException e) {
			_log.error(e.getMessage());
			return "unknown";
		}
	}

	public String getHostName() {
		try {
			if (_environmentChecker.isRunningInLocal()) {

				return serverPort;
			} else {

				return _kubernetesPodUtil.getServerPort();
			}
		} catch (Exception e) {
			_log.error(e.getMessage());
			return "unknown";
		}
	}

}
