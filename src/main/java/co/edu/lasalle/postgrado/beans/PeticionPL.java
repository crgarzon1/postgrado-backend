package co.edu.lasalle.postgrado.beans;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * @author jdrojas
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public final class PeticionPL extends JSONBean {

	private static final long serialVersionUID = 1L;
	@JsonProperty("e")
	private String esquema;
	@JsonProperty("p")
	private String procedimiento;
	@JsonProperty("ps")
	private Map<String, String> parametros;
	@JsonProperty("c")
	private int cache;
	@JsonProperty(value = "u", required = false)
	private int usuario = 0;
	
	public String getEsquema() {
		return esquema;
	}
	public void setEsquema(String esquema) {
		this.esquema = esquema;
	}
	public String getProcedimiento() {
		return procedimiento;
	}
	public void setProcedimiento(String procedimiento) {
		this.procedimiento = procedimiento;
	}
	public Map<String, String> getParametros() {
		return parametros;
	}
	public void setParametros(Map<String, String> parametros) {
		this.parametros = parametros;
	}
	public int getCache() {
		return cache;
	}
	public void setCache(int cache) {
		this.cache = cache;
	}
	public int getUsuario() {
		return usuario;
	}
	public void setUsuario(int usuario) {
		this.usuario = usuario;
	}
	
}
