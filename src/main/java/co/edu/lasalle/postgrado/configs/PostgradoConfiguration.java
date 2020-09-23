package co.edu.lasalle.postgrado.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.ws.transport.WebServiceMessageSender;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;

import co.edu.lasalle.postgrado.properties.PostgradoProperties;
import co.edu.lasalle.postgrado.utils.guias.GuiasClient;
import co.edu.lasalle.postgrado.utils.guias.anulaciones.AnulacionGuiaClient;
import co.edu.lasalle.postgrado.utils.helpers.CriptoHelper;
import co.edu.lasalle.postgrado.utils.helpers.HttpClient;

@Configuration
@EnableConfigurationProperties(PostgradoProperties.class)
public class PostgradoConfiguration {
	
	@Autowired
	private PostgradoProperties properties;
	
	@Bean
	public HttpClient getHttpClient() {
		return new HttpClient(properties);
	}
	
	@Bean
	public CriptoHelper getCriptoHelper() {
		return new CriptoHelper();
	}
	
	@Bean
	public WebServiceMessageSender webServiceMessageSender() {
		HttpComponentsMessageSender httpComponentsMessageSender = new HttpComponentsMessageSender();
		httpComponentsMessageSender.setConnectionTimeout(this.properties.getWsTimeout());
		httpComponentsMessageSender.setReadTimeout(this.properties.getWsTimeout());
		return httpComponentsMessageSender;
	}
	
	@Bean
	@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public GuiasClient getGuiasClient(WebServiceMessageSender webServiceMessageSender) {
		return new GuiasClient(this.properties, webServiceMessageSender);
	}
	
	@Bean
	@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public AnulacionGuiaClient getAnulacionGuiaClient(WebServiceMessageSender webServiceMessageSender) {
		return new AnulacionGuiaClient(this.properties, webServiceMessageSender);
	}

}
