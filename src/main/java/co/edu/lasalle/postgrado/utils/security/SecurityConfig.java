package co.edu.lasalle.postgrado.utils.security;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.ws.transport.WebServiceMessageSender;

import co.edu.lasalle.postgrado.properties.PostgradoProperties;
import co.edu.lasalle.postgrado.utils.auth.IdentityClient;
import co.edu.lasalle.postgrado.utils.auth.components.IdentityAuthenticationProvider;
import co.edu.lasalle.postgrado.utils.auth.filters.JWTAuthenticationFilter;
import co.edu.lasalle.postgrado.utils.auth.filters.JWTAuthorizationFilter;
import co.edu.lasalle.postgrado.utils.auth.services.UserDetailsServiceImpl;

@Configuration
@EnableConfigurationProperties(SecurityProperties.class)
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
	private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

	@Autowired
	private IdentityAuthenticationProvider authProvider;
	@Autowired
	private SecurityProperties properties;
	@Autowired
	private PostgradoProperties propertiesPostgrado;
	@Autowired
	private WebServiceMessageSender webServiceMessageSender;
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(authProvider);
		log.debug("Configurando autenticacion.");
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			.and()
			.cors()
			.and()
			.csrf().disable()
			.authorizeRequests().antMatchers(HttpMethod.POST, this.properties.getLoginUrl()).permitAll()
			.anyRequest().authenticated()
			.and()
			.addFilter(new JWTAuthenticationFilter(this.authenticationManager(), this.properties))
			.addFilter(new JWTAuthorizationFilter(this.authenticationManager(), this.properties));
		log.debug("Configurando seguridad http.");
	}

	@Bean
	@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public IdentityClient getIdentityClient() {
		log.debug("Creando cliente IdentityService.");
		return new IdentityClient(186, this.propertiesPostgrado, webServiceMessageSender);
	}
	
	@Bean
	protected UserDetailsService getUserDetailsService() {
		return new UserDetailsServiceImpl(this.getIdentityClient());
	}
	
	@Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
	
	@Bean
	protected CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList("http://zeus.lasalle.edu.co","http://estctiedarevalo.lasalle.edu.co:4200","http://estctijdrojas.lasalle.edu.co:9000"));
		configuration.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE","OPTIONS"));
		configuration.setAllowedHeaders(Arrays.asList("Authorization"));
		configuration.setAllowCredentials(Boolean.TRUE);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
	
}
