package com.codeninja.mfa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class MfaApplication {

	public static void main(String[] args) {
		SpringApplication.run(MfaApplication.class, args);
	}

}
