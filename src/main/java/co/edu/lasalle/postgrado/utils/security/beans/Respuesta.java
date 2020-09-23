package co.edu.lasalle.postgrado.utils.security.beans;

import java.io.IOException;
import java.io.Serializable;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class Respuesta implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private static final String regexpOracleError = "(ORA-)[0-9]+(:)[ ]+";
	public final static String OK = "ok";
	public final static String FAIL = "fail";
	
	private String status;
	private String mensaje;
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getMensaje() {
		return mensaje;
	}
	public void setMensaje(String mensaje) {
		this.mensaje = mensaje != null ? mensaje.replaceAll(regexpOracleError, "") : null;
	}
	@Override
	public String toString() {
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (IOException ex) {
			return super.toString();
		}
	}
	
}
