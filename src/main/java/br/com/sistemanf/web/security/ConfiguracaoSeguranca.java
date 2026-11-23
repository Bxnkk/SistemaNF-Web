package br.com.sistemanf.web.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ConfiguracaoSeguranca {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new SenhaNFPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain filtroSeguranca(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(autorizacao -> autorizacao.requestMatchers("/css/**").permitAll()
				.requestMatchers("/professor", "/professor/**").hasRole("PROFESSOR")
				.requestMatchers("/aluno", "/aluno/**").hasRole("ALUNO").anyRequest().authenticated())
				.formLogin(login -> login.loginPage("/login").permitAll())
				.logout(logout -> logout.logoutSuccessUrl("/login?logout").permitAll());

		return http.build();
	}
}