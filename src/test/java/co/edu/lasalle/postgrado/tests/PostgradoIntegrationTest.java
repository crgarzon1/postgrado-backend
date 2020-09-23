package co.edu.lasalle.postgrado.tests;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;

import co.edu.lasalle.postgrado.beans.PeticionPL;
import co.edu.lasalle.postgrado.properties.PostgradoProperties;
import co.edu.lasalle.postgrado.tests.annotation.ApiTestConfig;
import co.edu.lasalle.postgrado.tests.utils.RestBuilder;
import co.edu.lasalle.postgrado.utils.helpers.CriptoHelper;
import co.edu.lasalle.postgrado.utils.security.beans.Respuesta;
import co.edu.lasalle.postgrado.utils.security.beans.Usuario;

@ApiTestConfig
public class PostgradoIntegrationTest {
	
	private static final Logger log = LoggerFactory.getLogger(PostgradoIntegrationTest.class);
	
	@Autowired
	private CriptoHelper criptoHelper;
	@Autowired
	private PostgradoProperties properties;
	
	@Value("${local.server.port}")
	private int port = 8080;

	@Test
	void testAllUserOK() {
		try {
			Usuario usr = new Usuario();
			usr.setUsuario("hvelasco");
			usr.setContrasenia("123456789");
			Respuesta r = new RestBuilder<Respuesta>(port)
					.clazz(Respuesta.class)
					.accept(MediaType.APPLICATION_JSON_UTF8)
					.body(usr)
					.path("/login")
					.post()
					.build();
			PeticionPL p = new PeticionPL();
			p.setCache(0);
			p.setEsquema("2");
			p.setProcedimiento("PKG_MENU.CALL_FACADE");
			Map<String, String> params = new HashMap<String, String>();
			params.put("P_OPTION_ID", "9");
			p.setParametros(params);
			String credenciales = "XSYW;EDHS;d94;VELASCO PEÑA HUGO FERNANDO;79731831";
			String json = new RestBuilder<String>(port)
					.clazz(String.class)
					.accept(MediaType.APPLICATION_JSON_UTF8)
					.bearerAuth(r.getMensaje())
					.header("Cookie", this.properties.getCookieSia() + "=" + this.criptoHelper.crearToken(credenciales, this.properties.getKey()))
					.path("/api/liquidar/94182214/1")
					//.param("peticion", new ObjectMapper().writeValueAsString(p))
					.get()
					.build();
			log.info("[1] " + json);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}

}
