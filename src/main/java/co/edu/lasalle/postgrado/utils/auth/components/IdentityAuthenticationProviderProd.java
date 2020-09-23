package co.edu.lasalle.postgrado.utils.auth.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import co.edu.lasalle.postgrado.utils.auth.IdentityClient;
import co.edu.lasalle.postgrado.utils.auth.IdentityHelper;
import co.edu.unisalle.cti.identity.UsuarioSalle;

@Component
@Profile("prod")
public class IdentityAuthenticationProviderProd implements IdentityAuthenticationProvider {
	
	private static final Logger log = LoggerFactory.getLogger(IdentityAuthenticationProviderProd.class);
	
	@Autowired
	private IdentityClient identityClient;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String usr = authentication.getName();
		String psw = authentication.getCredentials().toString();
		log.debug("[LOGIN] " + usr);
		UsuarioSalle us = this.identityClient.obtenerUsuarioPortal(usr, psw);
		if (us != null) {
			log.debug(" * OK se genera token.");
			UserDetails u = IdentityHelper.userBuilder(us);
			return new UsernamePasswordAuthenticationToken(u.getUsername(), u.getPassword(), u.getAuthorities());
		} else {
			log.debug(" * FAIL usuario no valido.");
			throw new BadCredentialsException("Usuario no valido");
		}
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}

}
