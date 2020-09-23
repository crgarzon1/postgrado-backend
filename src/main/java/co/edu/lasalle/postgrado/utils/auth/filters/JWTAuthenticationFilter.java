package co.edu.lasalle.postgrado.utils.auth.filters;

import java.io.IOException;
import java.util.Date;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.lasalle.postgrado.utils.auth.IdentityHelper;
import co.edu.lasalle.postgrado.utils.security.SecurityProperties;
import co.edu.lasalle.postgrado.utils.security.beans.Respuesta;
import co.edu.lasalle.postgrado.utils.security.beans.Usuario;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
	
	private static final Logger log = LoggerFactory.getLogger(JWTAuthenticationFilter.class);
	
	private AuthenticationManager authenticationManager;
	private SecurityProperties securityProperties;
	
	public JWTAuthenticationFilter(AuthenticationManager authenticationManager, SecurityProperties securityProperties) {
		this.authenticationManager = authenticationManager;
		this.securityProperties = securityProperties;
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		try {
			Usuario credenciales = new ObjectMapper().readValue(request.getInputStream(), Usuario.class);
			log.debug("[AUTENTICANDO] " + credenciales.getUsuario());
			return this.authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(credenciales.getUsuario(), credenciales.getContrasenia())
				);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {
		UserDetails usr = new User(authResult.getName(), "GnLlRyVI9vm7ECfQ", authResult.getAuthorities());
		String token = Jwts.builder().setIssuedAt(new Date()).setIssuer(this.securityProperties.getIssuerInfo())
				.setSubject(IdentityHelper.userToJSON(usr))
				.setExpiration(new Date(System.currentTimeMillis() + this.securityProperties.getTokenExpirationTime()))
				.signWith(SignatureAlgorithm.HS512, this.securityProperties.getSuperSecretKey()).compact();
		log.debug("[TOKEN] " + token);
		response.addHeader(this.securityProperties.getHeaderAuthorizacionKey(), this.securityProperties.getTokenBearerPrefix() + " " + token);
		response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
		Respuesta r = new Respuesta();
		r.setStatus(Respuesta.OK);
		r.setMensaje(token);
		ServletOutputStream out = response.getOutputStream();
		out.print(new ObjectMapper().writeValueAsString(r));
	}

}
