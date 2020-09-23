package co.edu.lasalle.postgrado.properties;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "postgrado")
public class PostgradoProperties {

	private int httpTimeout;
	private int wsTimeout;
	private Map<String, String> urlsEsquemas;
	private String key;
	private int esquemaDefault;
	private Map<String, String> urls = new HashMap<>();
	private Map<String, String> ws = new HashMap<>();
	private String cookieSia;
	private long cookieVencimiento;
	private String parametroUsuario;
	private String regexpErrorOracle;
	private String tokenAnulacionGuia;
	private String cookieToken;
	
	public Map<String, String> getWs() {
		return ws;
	}

	public void setWs(Map<String, String> ws) {
		this.ws = ws;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public int getEsquemaDefault() {
		return esquemaDefault;
	}

	public void setEsquemaDefault(int esquemaDefault) {
		this.esquemaDefault = esquemaDefault;
	}

	public Map<String, String> getUrlsEsquemas() {
		return urlsEsquemas;
	}

	public void setUrlsEsquemas(Map<String, String> urlsEsquemas) {
		this.urlsEsquemas = urlsEsquemas;
	}

	public int getHttpTimeout() {
		return httpTimeout;
	}

	public void setHttpTimeout(int httpTimeout) {
		this.httpTimeout = httpTimeout;
	}

	public Map<String, String> getUrls() {
		return urls;
	}

	public void setUrls(Map<String, String> urls) {
		this.urls = urls;
	}

	public String getCookieSia() {
		return cookieSia;
	}

	public void setCookieSia(String cookieSia) {
		this.cookieSia = cookieSia;
	}

	public long getCookieVencimiento() {
		return cookieVencimiento;
	}

	public void setCookieVencimiento(long cookieVencimiento) {
		this.cookieVencimiento = cookieVencimiento;
	}

	public String getParametroUsuario() {
		return parametroUsuario;
	}

	public void setParametroUsuario(String parametroUsuario) {
		this.parametroUsuario = parametroUsuario;
	}

	public int getWsTimeout() {
		return wsTimeout;
	}

	public void setWsTimeout(int wsTimeout) {
		this.wsTimeout = wsTimeout;
	}

	public String getRegexpErrorOracle() {
		return regexpErrorOracle;
	}

	public void setRegexpErrorOracle(String regexpErrorOracle) {
		this.regexpErrorOracle = regexpErrorOracle;
	}

	public String getTokenAnulacionGuia() {
		return tokenAnulacionGuia;
	}

	public void setTokenAnulacionGuia(String tokenAnulacionGuia) {
		this.tokenAnulacionGuia = tokenAnulacionGuia;
	}

	public String getCookieToken() {
		return cookieToken;
	}

	public void setCookieToken(String cookieToken) {
		this.cookieToken = cookieToken;
	}
	
}
