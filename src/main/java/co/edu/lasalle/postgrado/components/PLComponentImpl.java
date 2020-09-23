package co.edu.lasalle.postgrado.components;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import co.edu.lasalle.postgrado.beans.AParametro;
import co.edu.lasalle.postgrado.beans.PeticionPL;
import co.edu.lasalle.postgrado.beans.UsuarioCookie;
import co.edu.lasalle.postgrado.exceptions.PostgradoException;
import co.edu.lasalle.postgrado.properties.PostgradoProperties;
import co.edu.lasalle.postgrado.utils.guias.GuiasClient;
import co.edu.lasalle.postgrado.utils.guias.anulaciones.AnulacionGuiaClient;
import co.edu.lasalle.postgrado.utils.helpers.CriptoHelper;
import co.edu.lasalle.postgrado.utils.helpers.HttpClient;
import co.edu.lasalle.postgrado.utils.security.beans.Respuesta;

@Service
public class PLComponentImpl implements PLComponent {
	
	private static final Logger log = LoggerFactory.getLogger(PLComponentImpl.class);
	
	@Autowired
	private PostgradoProperties properties;
	@Autowired
	private HttpClient httpClient;
	@Autowired
	private CriptoHelper criptoHelper;
	@Autowired
	private GuiasClient guiasClient;
	@Autowired
	private AnulacionGuiaClient anulacionGuiaClient;
	
	@Override
	public String callPL(Authentication auth, PeticionPL peticion, int metodo, Cookie... cookies) {
		// FIXME: Validar identidad con el objeto Authentication
		try {
			String json = this.httpClient.obtenerURL(Integer.valueOf(peticion.getEsquema()), peticion.getProcedimiento(), metodo, peticion.getParametros(), cookies);
			log.debug(json);
			return json;
		} catch (PostgradoException ex) {
			Respuesta r = new Respuesta();
			r.setStatus(Respuesta.FAIL);
			r.setMensaje(ex.getMessage());
			return r.toString();
		}
	}

	@Override
//	@Cacheable("post5m")
	public String callPL5m(Authentication auth, PeticionPL peticion, Cookie... cookies) {
		return this.callPL(auth, peticion, HttpClient.PETICION_GET, cookies);
	}

	@Override
//	@Cacheable("post10m")
	public String callPL10m(Authentication auth, PeticionPL peticion, Cookie... cookies) {
		return this.callPL(auth, peticion, HttpClient.PETICION_GET, cookies);
	}

	@Override
//	@Cacheable("post15m")
	public String callPL15m(Authentication auth, PeticionPL peticion, Cookie... cookies) {
		return this.callPL(auth, peticion, HttpClient.PETICION_GET, cookies);
	}

	@Override
//	@Cacheable("post30m")
	public String callPL30m(Authentication auth, PeticionPL peticion, Cookie... cookies) {
		return this.callPL(auth, peticion, HttpClient.PETICION_GET, cookies);
	}

	@Override
//	@Cacheable("post1h")
	public String callPL1h(Authentication auth, PeticionPL peticion, Cookie... cookies) {
		return this.callPL(auth, peticion, HttpClient.PETICION_GET, cookies);
	}

	@Override
//	@Cacheable("post2h")
	public String callPL2h(Authentication auth, PeticionPL peticion, Cookie... cookies) {
		return this.callPL(auth, peticion, HttpClient.PETICION_GET, cookies);
	}

	@Override
//	@Cacheable("post1d")
	public String callPL1d(Authentication auth, PeticionPL peticion, Cookie... cookies) {
		return this.callPL(auth, peticion, HttpClient.PETICION_GET, cookies);
	}

	@Override
//	@Cacheable("post1h")
	public String getPerfiles(Authentication auth) throws PostgradoException {
		String documento = null;
		for (GrantedAuthority a : auth.getAuthorities()) {
			documento = a.getAuthority();
			break;
		}
		log.debug("[DOC] " + documento);
		documento = this.criptoHelper.crearToken(documento, this.properties.getKey());
		PeticionPL p = new PeticionPL();
		p.setCache(0);
		p.setEsquema(String.valueOf(this.properties.getEsquemaDefault()));
		p.setProcedimiento(this.properties.getUrls().get("perfiles"));
		Map<String, String> ps = new HashMap<>();
		ps.put("token", documento);
		p.setParametros(ps);
		return this.callPL(auth, p, HttpClient.PETICION_GET);
	}

	protected AParametro getParametro(Authentication auth, UsuarioCookie usuario, int periodo, int adicionales) throws PostgradoException {
		try {
			PeticionPL p = new PeticionPL();
			p.setCache(0);
			p.setEsquema(String.valueOf(this.properties.getEsquemaDefault()));
			p.setProcedimiento(this.properties.getUrls().get("guia"));
			Map<String, String> ps = new HashMap<>();
			ps.put("p_codigo", usuario.getCodigo());
			ps.put("p_periodo", String.valueOf(periodo));
			if (adicionales > 0) {
				ps.put("p_creditos_add", String.valueOf(adicionales));
			}
			p.setParametros(ps);
			String json = this.callPL(auth, p, HttpClient.PETICION_GET);
			AParametro pm = new ObjectMapper().readValue(json, AParametro.class);
			if (pm.getCodigo() == null) {
				Respuesta r = new ObjectMapper().readValue(json, Respuesta.class);
				if (Respuesta.FAIL.equals(r.getStatus())) {
					throw new PostgradoException(r.getMensaje());
				}
			}
			log.debug("[PARAMETRO] " + pm.toString());
			return pm;
		} catch (IOException ex) {
			throw new PostgradoException("No se pudo liquidar desde SIA.", ex);
		}
	}

	@Override
	public Respuesta liquidar(Authentication auth, UsuarioCookie usuario, int periodo) throws PostgradoException {
		return this.liquidar(auth, usuario, periodo, 0);
	}

	@Override
	public Respuesta liquidar(Authentication auth, UsuarioCookie usuario, int periodo, int adicionales)
			throws PostgradoException {
		//Busco la guía anterior antes de liquidar.
		Respuesta ug = this.consultarUltimaGuia(auth, usuario.getCodigo());
		log.debug("[LIQUIDANDO]");
		AParametro pm = this.getParametro(auth, usuario, periodo, adicionales);
		if (pm != null && pm.getGuiaAcademica() != null && Respuesta.OK.equals(ug.getStatus()) && !pm.getGuiaAcademica().equals(ug.getMensaje())) {
			Respuesta anulacion = this.anulacionGuiaClient.anular(Integer.valueOf(ug.getMensaje()));
			if (Respuesta.OK.equals(anulacion.getStatus())) {
				log.info("[GUIA ANULADA] " + usuario.getCodigo() + ": " + ug.getMensaje() + "," + pm.getGuiaAcademica());
			} else {
				log.warn("[GUIA NO ANULADA] " + usuario.getCodigo() + ": " + ug.getMensaje() + "," + pm.getGuiaAcademica());
			}
		} else {
			log.debug("[GUIA ANULADA] No es necesario anular guia: " + ug.getMensaje() + "," + pm.getGuiaAcademica());
		}
		Respuesta r = this.guiasClient.liquidar(pm, periodo);
		if (Respuesta.FAIL.equals(r.getStatus())) {
			PeticionPL pl = new PeticionPL();
			pl.setCache(0);
			pl.setEsquema(String.valueOf(this.properties.getEsquemaDefault()));
			pl.setProcedimiento(this.properties.getUrls().get("desactivar-guia"));
			Map<String, String> params = new HashMap<>();
			params.put("p_codigo_guia", pm.getGuiaAcademica());
			pl.setParametros(params);
			this.callPL(auth, pl, HttpClient.PETICION_GET);
			throw new PostgradoException(r.getMensaje());
		}
		return r;
	}

	@Override
	public Respuesta consultarGuiaCreditosAdicionales(Authentication auth, String token) throws PostgradoException {
		PeticionPL pl = new PeticionPL();
		pl.setCache(2);
		pl.setEsquema(String.valueOf(this.properties.getEsquemaDefault()));
		pl.setProcedimiento(this.properties.getUrls().get("consulta-creditos-adicionales"));
		Map<String, String> params = new HashMap<>();
		params.put("P_CRITERIO_BUSQUEDA", token);
		pl.setParametros(params);
		String json = this.callPL(auth, pl, HttpClient.PETICION_GET);
		log.debug("[ADICIONALES EST] " + json);
		try {
			ObjectNode node = new ObjectMapper().readValue(json, ObjectNode.class);
			JsonNode values = node.get("values");
			if (values != null && values.isArray()) {
				for (final JsonNode n : values) {
					JsonNode guia = n.get("guiaFinanciera");
					if (guia != null) {
						Respuesta r = new Respuesta();
						r.setStatus(Respuesta.OK);
						r.setMensaje(guia.textValue());
						return r;
					}
				}
			}
			throw new PostgradoException("Sin creditos adicionales.");
		} catch (IOException ex) {
			throw new PostgradoException("[ADICIONALES EST] " + ex.getMessage());
		}
	}

	@Override
	public Respuesta consultarUltimaGuia(Authentication auth, String codigo) throws PostgradoException {
		try {
			PeticionPL p = new PeticionPL();
			p.setCache(0);
			p.setEsquema(String.valueOf(this.properties.getEsquemaDefault()));
			p.setProcedimiento(this.properties.getUrls().get("ultima-guia"));
			Map<String, String> ps = new HashMap<>();
			ps.put("p_codigo_estudiante", codigo);
			p.setParametros(ps);
			String json = this.callPL(auth, p, HttpClient.PETICION_GET);
			return new ObjectMapper().readValue(json, Respuesta.class);
		} catch (IOException ex) {
			throw new PostgradoException("No se pudo liquidar desde SIA.", ex);
		}
	}

}
