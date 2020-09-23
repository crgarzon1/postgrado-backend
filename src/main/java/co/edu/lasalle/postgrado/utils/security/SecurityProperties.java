package co.edu.lasalle.postgrado.utils.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

	private String loginUrl;
	private String headerAuthorizacionKey;
	private String tokenBearerPrefix;
	private String issuerInfo;
	private String superSecretKey;
	private Long tokenExpirationTime;
	public String getLoginUrl() {
		return loginUrl;
	}
	public void setLoginUrl(String loginUrl) {
		this.loginUrl = loginUrl;
	}
	public String getHeaderAuthorizacionKey() {
		return headerAuthorizacionKey;
	}
	public void setHeaderAuthorizacionKey(String headerAuthorizacionKey) {
		this.headerAuthorizacionKey = headerAuthorizacionKey;
	}
	public String getTokenBearerPrefix() {
		return tokenBearerPrefix;
	}
	public void setTokenBearerPrefix(String tokenBearerPrefix) {
		this.tokenBearerPrefix = tokenBearerPrefix;
	}
	public String getIssuerInfo() {
		return issuerInfo;
	}
	public void setIssuerInfo(String issuerInfo) {
		this.issuerInfo = issuerInfo;
	}
	public String getSuperSecretKey() {
		return superSecretKey;
	}
	public void setSuperSecretKey(String superSecretKey) {
		this.superSecretKey = superSecretKey;
	}
	public Long getTokenExpirationTime() {
		return tokenExpirationTime;
	}
	public void setTokenExpirationTime(Long tokenExpirationTime) {
		this.tokenExpirationTime = tokenExpirationTime;
	}
	
}
