package co.edu.lasalle.postgrado.utils.auth.filters;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import co.edu.lasalle.postgrado.utils.auth.IdentityHelper;
import co.edu.lasalle.postgrado.utils.security.SecurityProperties;
import io.jsonwebtoken.Jwts;

public class JWTAuthorizationFilter extends BasicAuthenticationFilter {
	
	private static final Logger log = LoggerFactory.getLogger(JWTAuthorizationFilter.class);
	
	private SecurityProperties securityProperties;

	public JWTAuthorizationFilter(AuthenticationManager authenticationManager, SecurityProperties securityProperties) {
		super(authenticationManager);
		this.securityProperties = securityProperties;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		String header = request.getHeader(this.securityProperties.getHeaderAuthorizacionKey());
		if (header == null || !header.startsWith(this.securityProperties.getTokenBearerPrefix())) {
			log.debug("[HEADER TOKEN] FAIL");
			chain.doFilter(request, response);
			return;
		}
		log.debug("[HEADER TOKEN] " + header);
		UsernamePasswordAuthenticationToken authentication = this.getAuthentication(request);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		if (log.isDebugEnabled()) {
			log.debug("[HEADER TOKEN] OK " + authentication.getName());
			for (GrantedAuthority ga : authentication.getAuthorities()) {
				log.debug(" * " + ga.getAuthority());
			}
			log.debug("----------[HEADER TOKEN]");
		}
		chain.doFilter(request, response);
	}
	
	private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
		String token = request.getHeader(this.securityProperties.getHeaderAuthorizacionKey());
		if (token != null) {
			String userJson = Jwts.parser()
					.setSigningKey(this.securityProperties.getSuperSecretKey())
					.parseClaimsJws(token.replace(this.securityProperties.getTokenBearerPrefix(), ""))
					.getBody()
					.getSubject();
			if (userJson == null) {
				return null;
			}
			try {
				UserDetails usr = IdentityHelper.jsonToUser(userJson);
				return new UsernamePasswordAuthenticationToken(usr.getUsername(), usr.getPassword(), usr.getAuthorities());
			} catch (IOException ex) {
				log.warn("No se logro decodificar el usuario: " + ex.getMessage());
				return null;
			}
		}
		return null;
	}

}
