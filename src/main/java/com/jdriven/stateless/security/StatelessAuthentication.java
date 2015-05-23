package com.jdriven.stateless.security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.social.SocialWebAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@EnableAutoConfiguration(exclude = { SocialWebAutoConfiguration.class })
@Configuration
@ComponentScan
public class StatelessAuthentication {

	public static void main(String[] args) {
		SpringApplication.run(StatelessAuthentication.class, args);
	}

}
