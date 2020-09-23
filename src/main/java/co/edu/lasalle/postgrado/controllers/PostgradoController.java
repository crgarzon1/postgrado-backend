package co.edu.lasalle.postgrado.controllers;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.lasalle.postgrado.beans.PeticionPL;
import co.edu.lasalle.postgrado.beans.UsuarioAuditoriaSIA;
import co.edu.lasalle.postgrado.beans.UsuarioCookie;
import co.edu.lasalle.postgrado.components.PLComponent;
import co.edu.lasalle.postgrado.exceptions.PostgradoException;
import co.edu.lasalle.postgrado.properties.PostgradoProperties;
import co.edu.lasalle.postgrado.utils.helpers.CriptoHelper;
import co.edu.lasalle.postgrado.utils.helpers.HttpClient;
import co.edu.lasalle.postgrado.utils.security.beans.Respuesta;

@PreAuthorize("authenticated")
@RestController
@RequestMapping("/api")
public class PostgradoController {
	
	private static final Logger log = LoggerFactory.getLogger(PostgradoController.class);
	
	@Autowired
	private PostgradoProperties properties;
	@Autowired
	private PLComponent plComponent;
	@Autowired
	private CriptoHelper criptoHelper;
	
	private ResponseEntity<String> hacerPeticion(PeticionPL peticion, Authentication authentication, int tipoPeticion, String ip, Cookie... cookies) {
		log.debug("[PETICION] " + peticion.toString());
		String json = null;
		try {
			if (peticion.getUsuario() > 0) {
				Map<String, String> params = peticion.getParametros();
				if (params != null) {
					UsuarioAuditoriaSIA ua = new UsuarioAuditoriaSIA();
					ua.setIp(ip);
					UsernamePasswordAuthenticationToken at = (UsernamePasswordAuthenticationToken) authentication;
					List<GrantedAuthority> gas = new ArrayList<GrantedAuthority>(at.getAuthorities());
					ua.setPerfil(gas.get(1).getAuthority());
					ua.setUsuario(at.getName());
					try {
						params.put(
								this.properties.getParametroUsuario(),
								UriUtils.encodeQueryParam(
										new ObjectMapper().writeValueAsString(ua),
										Charset.defaultCharset())
								);
					} catch (IOException ex) {
						throw new PostgradoException("No se logro procesar el usuario de auditoria: " + ex.getMessage(), ex);
					}
				}
			}
			switch (peticion.getCache()) {
			case 0:
				json = this.plComponent.callPL(authentication, peticion, tipoPeticion, cookies);
				break;
			case 1:
				json = this.plComponent.callPL5m(authentication, peticion, cookies);
				break;
			case 2:
				json = this.plComponent.callPL10m(authentication, peticion, cookies);
				break;
			case 3:
				json = this.plComponent.callPL15m(authentication, peticion, cookies);
				break;
			case 4:
				json = this.plComponent.callPL30m(authentication, peticion, cookies);
				break;
			case 5:
				json = this.plComponent.callPL1h(authentication, peticion, cookies);
				break;
			case 6:
				json = this.plComponent.callPL2h(authentication, peticion, cookies);
				break;
			case 7:
				json = this.plComponent.callPL1d(authentication, peticion, cookies);
				break;
			default:
				throw new PostgradoException("Opcion no valida");
			}
			try {
				ObjectMapper om = new ObjectMapper();
				Respuesta r = om.readValue(json, Respuesta.class);
				if (Respuesta.FAIL.equals(r.getStatus())) {
					throw new PostgradoException(om.writeValueAsString(r));
				}
			} catch (IOException ex) {
				log.debug(ex.getMessage());
			}
		} catch (PostgradoException ex) {
			return new ResponseEntity<String>(json, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<String>(json, HttpStatus.OK);
	}

	@GetMapping("/get")
	public ResponseEntity<String> get(@RequestParam String peticion, Authentication authentication, HttpServletRequest request) {
		log.debug("[GET]");
		try {
			PeticionPL p = new ObjectMapper().readValue(peticion, PeticionPL.class);
			log.debug("[IN] " + p.toString());
			return this.hacerPeticion(p, authentication, HttpClient.PETICION_GET, request.getRemoteHost(), request.getCookies());
		} catch (IOException ex) {
			Respuesta r = new Respuesta();
			r.setStatus(Respuesta.FAIL);
			r.setMensaje(ex.getMessage());
			return new ResponseEntity<String>(r.toString(), HttpStatus.BAD_REQUEST);
		}
	}
	
	@RequestMapping(value = {"/post", "/put", "/delete"}, method = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
	public ResponseEntity<String> post(@RequestBody PeticionPL peticion, Authentication authentication, HttpServletRequest request) {
		log.debug("[OTRO]");
		peticion.setCache(0);
		return this.hacerPeticion(peticion, authentication, HttpClient.PETICION_POST, request.getRemoteHost(), request.getCookies());
	}

	@GetMapping("/perfiles")
	public ResponseEntity<String> getPerfiles(Authentication authentication) {
		try {
			return new ResponseEntity<String>(this.plComponent.getPerfiles(authentication), HttpStatus.OK);
		} catch (PostgradoException ex) {
			Respuesta r = new Respuesta();
			r.setStatus(Respuesta.FAIL);
			r.setMensaje(ex.getMessage());
			return new ResponseEntity<String>(r.toString(), HttpStatus.BAD_REQUEST);
		}
	}
	
	@GetMapping("/liquidar/{periodo}")
	public ResponseEntity<Respuesta> liquidar(Authentication authentication, @CookieValue(value = "wUFAnew4", defaultValue = "fail") String token, @PathVariable Integer periodo) {
		try {
			if (Respuesta.FAIL.equals(token)) {
				throw new PostgradoException("No autenticado");
			}
			UsuarioCookie user = this.criptoHelper.leerCookieSIA(token, this.properties.getCookieVencimiento(), this.properties.getKey());
			return new ResponseEntity<Respuesta>(this.plComponent.liquidar(authentication, user, periodo), HttpStatus.OK);
		} catch (PostgradoException ex) {
			Respuesta r = new Respuesta();
			r.setStatus(Respuesta.FAIL);
			r.setMensaje(ex.getMessage());
			return new ResponseEntity<Respuesta>(r, HttpStatus.BAD_REQUEST);
		}
	}
	
	@PreAuthorize("hasRole('ADMINISTRATIVO')")
	@GetMapping("/liquidar/{codigo}/{periodo}")
	public ResponseEntity<Respuesta> liquidar(Authentication authentication, @CookieValue(value = "wUFAnew4", defaultValue = "fail") String token, @PathVariable String codigo, @PathVariable Integer periodo, @RequestParam(defaultValue = "0") Integer adicionales) {
		try {
			if (Respuesta.FAIL.equals(token)) {
				throw new PostgradoException("No autenticado");
			}
			UsuarioCookie user = this.criptoHelper.leerCookieSIA(token, this.properties.getCookieVencimiento(), this.properties.getKey());
			if (log.isDebugEnabled()) {
				log.debug("[USUARIO COOKIE] " + user.getNombre());
				log.debug("[     * CODIGO ] " + codigo);
				log.debug("[     * PERIODO] " + periodo);
				log.debug("[   ADICIONALES] " + adicionales);
			}
			user.setCodigo(codigo);
			return new ResponseEntity<Respuesta>(this.plComponent.liquidar(authentication, user, periodo, adicionales), HttpStatus.OK);
		} catch (PostgradoException ex) {
			Respuesta r = new Respuesta();
			r.setStatus(Respuesta.FAIL);
			r.setMensaje(ex.getMessage());
			return new ResponseEntity<Respuesta>(r, HttpStatus.BAD_REQUEST);
		}
	}
	
	@GetMapping("/guia/adicionales")
	public ResponseEntity<Respuesta> consultarGuia(Authentication authentication, @CookieValue(value = "wUFAnew4", defaultValue = "fail") String token) {
		try {
			if (Respuesta.FAIL.equals(token)) {
				throw new PostgradoException("No autenticado");
			}
			return new ResponseEntity<Respuesta>(this.plComponent.consultarGuiaCreditosAdicionales(authentication, token), HttpStatus.OK);
		} catch (PostgradoException ex) {
			Respuesta r = new Respuesta();
			r.setStatus(Respuesta.FAIL);
			r.setMensaje(ex.getMessage());
			return new ResponseEntity<Respuesta>(r, HttpStatus.BAD_REQUEST);
		}
	}
	
}
