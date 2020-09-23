package co.edu.lasalle.postgrado.utils.auth.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.lasalle.postgrado.utils.auth.IdentityClient;
import co.edu.lasalle.postgrado.utils.auth.IdentityHelper;
import co.edu.unisalle.cti.identity.UsuarioSalle;

@Service
@Transactional
public class UserDetailsServiceImpl implements UserDetailsService {
	
	private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);
	
	private IdentityClient identityClient;
	
	public UserDetailsServiceImpl(IdentityClient identityClient) {
		this.identityClient = identityClient;
	}
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		if (username == null || username.isEmpty()) {
			throw new UsernameNotFoundException("Usuario no valido");
		}
		log.debug("[USR] buscando usuario: " + username);
		UsuarioSalle usr = this.identityClient.obtenerUsuario(username).getReturn();
		if (usr == null) {
			log.debug("[USR] FAIL " + username);
			throw new UsernameNotFoundException("El usuario no existe: " + username);
		}
		log.debug("[USR] OK " + username);
		return IdentityHelper.userBuilder(usr);
	}


}
