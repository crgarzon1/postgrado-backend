package co.edu.lasalle.postgrado.beans;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UsuarioAuditoriaSIA implements Serializable{

	private static final long serialVersionUID = 1L;
	
	@JsonProperty("i")
	private String ip;
	@JsonProperty("id")
	private String usuario;
	@JsonProperty("pe")
	private String perfil;
	
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getUsuario() {
		return usuario;
	}
	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}
	public String getPerfil() {
		return perfil;
	}
	public void setPerfil(String perfil) {
		this.perfil = perfil;
	}

}
