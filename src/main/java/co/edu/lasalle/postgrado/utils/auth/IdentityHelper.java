package co.edu.lasalle.postgrado.utils.auth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.lasalle.postgrado.utils.security.beans.Usuario;
import co.edu.unisalle.cti.identity.UsuarioSalle;

public final class IdentityHelper {
	
	private static final Logger log = LoggerFactory.getLogger(IdentityHelper.class);
	
	public static final String ROL_ESTUDIANTE = "ROLE_ESTUDIANTE";
	public static final String ROL_PROFESOR = "ROLE_PROFESOR";
	public static final String ROL_ADMINISTRATIVO = "ROLE_ADMINISTRATIVO";

	public static UserDetails userBuilder(UsuarioSalle usr) {
		if (usr == null) {
			log.warn("Usuario vacio.");
			return null;
		}
		List<GrantedAuthority> authorities = new ArrayList<>();
		String username = null;
		if (usr.getDatosLdapAdministrativo() != null) {
			log.debug("[USR] Administrativo " + usr.getDatosLdapAdministrativo().getSAMAccountName());
			authorities.add(new SimpleGrantedAuthority(usr.getDatosLdapAdministrativo().getDescription()));
			username = usr.getDatosLdapAdministrativo().getSAMAccountName();
			authorities.add(new SimpleGrantedAuthority(ROL_ADMINISTRATIVO));
		}
		if (usr.getDatosLdapDocente() != null) {
			log.debug("[USR] Docente " + usr.getDatosLdapDocente().getSAMAccountName());
			authorities.add(new SimpleGrantedAuthority(usr.getDatosLdapDocente().getDescription()));
			username = usr.getDatosLdapDocente().getSAMAccountName();
			authorities.add(new SimpleGrantedAuthority(ROL_PROFESOR));
		}
		if (usr.getDatosLdapEstudiante() != null) {
			log.debug("[USR] Estudiante " + usr.getDatosLdapEstudiante().getSAMAccountName());
			authorities.add(new SimpleGrantedAuthority(usr.getDatosLdapEstudiante().getDescription()));
			username = usr.getDatosLdapEstudiante().getSAMAccountName();
			authorities.add(new SimpleGrantedAuthority(ROL_ESTUDIANTE));
		}
		return new User(username, username + "-" + System.currentTimeMillis(), authorities);
	}
	
	public static String userToJSON(UserDetails user) throws JsonProcessingException {
		Usuario u = new Usuario();
		u.setUsuario(user.getUsername());
		u.setContrasenia(user.getPassword());
		List<String> perfiles = new ArrayList<>();
		for (GrantedAuthority a : user.getAuthorities()) {
			perfiles.add(a.getAuthority());
		}
		u.setPerfiles(perfiles);
		return new ObjectMapper().writeValueAsString(u);
	}
	
	public static UserDetails jsonToUser(String json) throws IOException {
		Usuario u = new ObjectMapper().readValue(json, Usuario.class);
		List<GrantedAuthority> p = new ArrayList<>();
		for (String perfil : u.getPerfiles()) {
			p.add(new SimpleGrantedAuthority(perfil));
		}
		return new User(u.getUsuario(), u.getContrasenia(), p);
	}
	
}
