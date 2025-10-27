package akses.ksei.co.id.investor.config;

import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

@KeycloakConfiguration
@EnableWebSecurity
@ConditionalOnProperty(name = "keycloak.enabled", havingValue = "true", matchIfMissing = true)
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends KeycloakWebSecurityConfigurerAdapter {
	
	@Bean
	@Override
	protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
		// return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());

		return new NullAuthenticatedSessionStrategy();
	}

	/*
	 * re-configure Spring Security to use registers the
	 * KeycloakAuthenticationProvider with the authentication manager
	 */
	@Autowired
	void configureGlobal(AuthenticationManagerBuilder auth) {
		// auth.authenticationProvider(getKeycloakAuthenticationProvider());

		KeycloakAuthenticationProvider keycloakAuthenticationProvider = keycloakAuthenticationProvider();
		auth.authenticationProvider(keycloakAuthenticationProvider);
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		super.configure(http);

		configureApiSecurity(http);
	}

	public void configureApiSecurity(HttpSecurity http) throws Exception {
		http.csrf().disable().authorizeRequests()
				// we can set up authorization here alternatively to @Secured methods
				.antMatchers(HttpMethod.POST).permitAll()
				.antMatchers(HttpMethod.GET).permitAll()
				.antMatchers(HttpMethod.DELETE).permitAll()
				.antMatchers("/v2/api-docs/**").permitAll()
				.antMatchers("/v3/api-docs/**").permitAll()
				.antMatchers("/swagger-ui/**").permitAll()
				.antMatchers("/swagger-resources/**").permitAll()
				.antMatchers("/swagger-ui.html").permitAll()
				.antMatchers("/webjars/**").permitAll()
				.anyRequest().authenticated()
				.and()
				.sessionManagement()
				.sessionAuthenticationStrategy(sessionAuthenticationStrategy())
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
	}

}
