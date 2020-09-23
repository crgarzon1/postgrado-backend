package co.edu.lasalle.postgrado.utils.security.beans;

import java.io.Serializable;
import java.util.List;

public final class Usuario implements Serializable {

	private static final long serialVersionUID = 1L;
	private String usuario;
	private String contrasenia;
	private List<String> perfiles;
	
	public String getUsuario() {
		return usuario;
	}
	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}
	public String getContrasenia() {
		return contrasenia;
	}
	public void setContrasenia(String contrasenia) {
		this.contrasenia = contrasenia;
	}
	public List<String> getPerfiles() {
		return perfiles;
	}
	public void setPerfiles(List<String> perfiles) {
		this.perfiles = perfiles;
	}
	
}
