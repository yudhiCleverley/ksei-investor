package akses.ksei.co.id.investor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAutoConfiguration
@EnableScheduling
public class AksesKseiInvestorServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AksesKseiInvestorServiceApplication.class, args);
	}

}
