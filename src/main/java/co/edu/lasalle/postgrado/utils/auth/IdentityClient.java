package co.edu.lasalle.postgrado.utils.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.transport.WebServiceMessageSender;

import co.edu.lasalle.postgrado.beans.UsuarioPortal;
import co.edu.lasalle.postgrado.exceptions.PostgradoException;
import co.edu.lasalle.postgrado.properties.PostgradoProperties;
import co.edu.lasalle.postgrado.utils.helpers.CriptoHelper;
import co.edu.unisalle.cti.identity.ObtenerUsuarioSalle;
import co.edu.unisalle.cti.identity.ObtenerUsuarioSalleByCedula;
import co.edu.unisalle.cti.identity.ObtenerUsuarioSalleByCedulaResponse;
import co.edu.unisalle.cti.identity.ObtenerUsuarioSalleByUser;
import co.edu.unisalle.cti.identity.ObtenerUsuarioSalleByUserResponse;
import co.edu.unisalle.cti.identity.ObtenerUsuarioSallePortal;
import co.edu.unisalle.cti.identity.ObtenerUsuarioSallePortalResponse;
import co.edu.unisalle.cti.identity.ObtenerUsuarioSalleResponse;
import co.edu.unisalle.cti.identity.UsuarioSalle;

public final class IdentityClient extends WebServiceGatewaySupport  {

	private static final Logger log = LoggerFactory.getLogger(IdentityClient.class);
	
	public static final String CONTEXT_PATH = "co.edu.unisalle.cti.identity";
	private final String COOKIE_TOKEN;
	private final long VENCIMIENTO;
	private int idApp = 186;
	
	public IdentityClient(int idApp, PostgradoProperties properties, WebServiceMessageSender sender) {
		this.idApp = idApp;
		IdentityMarshaller jaxbm = new IdentityMarshaller();
		jaxbm.setContextPath(CONTEXT_PATH);
		this.setMarshaller(jaxbm);
		this.setUnmarshaller(jaxbm);
		this.setMessageSender(sender);
		this.setDefaultUri(properties.getWs().get("identity"));
		this.COOKIE_TOKEN = properties.getCookieToken();
		this.VENCIMIENTO = properties.getCookieVencimiento();
	}
	
	public UsuarioSalle autenticar(String usr, String pws) {
		ObtenerUsuarioSalle ous = new ObtenerUsuarioSalle();
		ous.setAplicacion(this.idApp);
		ous.setFiltrarPerfilesPorApp(false);
		ous.setObtenerRolesSga(false);
		ous.setPassword(pws);
		ous.setRetornarDatosBasicos(false);
		ous.setUsuario(usr);
		((IdentityMarshaller)this.getMarshaller()).setOperacion(IdentityRootElementFactory.OBTENER_USUARIO_SALLE);
		((IdentityMarshaller)this.getMarshaller()).setRootName("obtenerUsuarioSalle");
		log.debug("[LOGIN] " + this.idApp + " " + usr);
		ObtenerUsuarioSalleResponse response = (ObtenerUsuarioSalleResponse) this.getWebServiceTemplate().marshalSendAndReceive(ous, new IdentityMessageCallback());
		UsuarioSalle us = response != null ? response.getReturn() : null;
		log.debug("[LOGIN] " + (us != null ? "OK" : "FAIL"));
		return us;
	}
	
	public ObtenerUsuarioSalleByUserResponse obtenerUsuario(String usr) {
		ObtenerUsuarioSalleByUser ous = new ObtenerUsuarioSalleByUser();
		ous.setUsuario(usr);
		ous.setDatosBasicos(false);
		((IdentityMarshaller)this.getMarshaller()).setOperacion(IdentityRootElementFactory.OBTENER_USUARIO_SALLE_BY_USER);
		((IdentityMarshaller)this.getMarshaller()).setRootName("obtenerUsuarioSalleByUser");
		log.debug("[USR] " + this.idApp + " " + usr);
		ObtenerUsuarioSalleByUserResponse response = (ObtenerUsuarioSalleByUserResponse) getWebServiceTemplate().marshalSendAndReceive(ous, new IdentityMessageCallback());
		log.debug("[USR] " + (response.getReturn() != null ? " OK" : " FAIL"));
		return response;
	}
	
	public ObtenerUsuarioSalleByCedulaResponse obtenerUsuarioXDocumento(String documento) {
		ObtenerUsuarioSalleByCedula ous = new ObtenerUsuarioSalleByCedula();
		ous.setCedula(documento);
		ous.setDatosBasicos(false);
		((IdentityMarshaller)this.getMarshaller()).setOperacion(IdentityRootElementFactory.OBTENER_USUARIO_SALLE_BY_CEDULA);
		((IdentityMarshaller)this.getMarshaller()).setRootName("obtenerUsuarioSalleByCedula");
		log.debug("[USR] " + this.idApp + " " + documento);
		ObtenerUsuarioSalleByCedulaResponse response = (ObtenerUsuarioSalleByCedulaResponse) getWebServiceTemplate().marshalSendAndReceive(ous, new IdentityMessageCallback());
		log.debug("[USR] " + (response.getReturn() != null ? " OK" : " FAIL"));
		return response;
	}
	
	public UsuarioSalle obtenerUsuarioPortal(String usr, String psw) {
		if (COOKIE_TOKEN.equals(usr)) {
			try {
				UsuarioPortal up = new CriptoHelper().leerCookiePortal(psw, this.VENCIMIENTO);
				ObtenerUsuarioSalleByUserResponse r = obtenerUsuario(up.getUsuario());
				return r.getReturn();
			} catch (PostgradoException ex) {
				log.debug("[LOGIN COOKIE] " + ex.getMessage());
				return null;
			}
		}
		ObtenerUsuarioSallePortal ous = new ObtenerUsuarioSallePortal();
		ous.setAplicacion(this.idApp);
		ous.setUsuario(usr);
		ous.setPassword(psw);
		((IdentityMarshaller)this.getMarshaller()).setOperacion(IdentityRootElementFactory.OBTENER_USUARIO_SALLE_PORTAL);
		((IdentityMarshaller)this.getMarshaller()).setRootName("obtenerUsuarioSallePortal");
		log.debug("[USR] " + this.idApp + " " + usr);
		ObtenerUsuarioSallePortalResponse response = (ObtenerUsuarioSallePortalResponse) getWebServiceTemplate().marshalSendAndReceive(ous, new IdentityMessageCallback());
		UsuarioSalle us = response != null ? response.getReturn() : null;
		log.debug("[LOGIN] " + (us != null ? "OK" : "FAIL"));
		return us;
	}

}
