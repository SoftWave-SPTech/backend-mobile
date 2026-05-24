package softwave.backend.backend_mobile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SoftWaveFinanceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SoftWaveFinanceApplication.class, args);
	}

}
