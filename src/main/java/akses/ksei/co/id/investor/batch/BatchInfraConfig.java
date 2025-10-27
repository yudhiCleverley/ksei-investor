package akses.ksei.co.id.investor.batch;

import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BatchInfraConfig {

	@Bean
	public JobOperator jobOperator(JobExplorer jobExplorer, JobRepository jobRepository, JobLauncher jobLauncher,
			JobRegistry jobRegistry) {
		SimpleJobOperator jobOperator = new SimpleJobOperator();
		jobOperator.setJobExplorer(jobExplorer);
		jobOperator.setJobRepository(jobRepository);
		jobOperator.setJobLauncher(jobLauncher);
		jobOperator.setJobRegistry(jobRegistry);

		return jobOperator;
	}

}
