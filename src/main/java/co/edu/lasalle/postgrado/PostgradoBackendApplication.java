package co.edu.lasalle.postgrado;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class PostgradoBackendApplication extends SpringBootServletInitializer {
	
	private static final Logger log = LoggerFactory.getLogger(PostgradoBackendApplication.class);

	public static void main(String[] args) {
		log.debug("Iniciando Postgrado-Backend");
		SpringApplication.run(PostgradoBackendApplication.class, args);
	}

}
