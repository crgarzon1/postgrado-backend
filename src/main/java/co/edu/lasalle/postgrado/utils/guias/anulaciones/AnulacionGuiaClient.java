package co.edu.lasalle.postgrado.utils.guias.anulaciones;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.transport.WebServiceMessageSender;

import co.edu.lasalle.postgrado.exceptions.PostgradoException;
import co.edu.lasalle.postgrado.properties.PostgradoProperties;
import co.edu.lasalle.postgrado.utils.security.beans.Respuesta;
import co.edu.unisalle.cti.guiaspago.servicios.anulacion.AnularGuiaAcademica;
import co.edu.unisalle.cti.guiaspago.servicios.anulacion.AnularGuiaAcademicaResponse;
import co.edu.unisalle.cti.guiaspago.servicios.anulacion.BeanParametrosAnulacion;

public class AnulacionGuiaClient extends WebServiceGatewaySupport {

	private static final Logger log = LoggerFactory.getLogger(AnulacionGuiaClient.class);
	
	public static final String CONTEXT_PATH = "co.edu.unisalle.cti.guiaspago.servicios.anulacion";
	private static final String GUIA_OK = "OK";
	private final String TOKEN;
	
	public AnulacionGuiaClient(PostgradoProperties properties, WebServiceMessageSender sender) {
		super();
		AnulacionGuiaMarshaller marshaller = new AnulacionGuiaMarshaller();
		this.setMarshaller(marshaller);
		this.setUnmarshaller(marshaller);
		this.setMessageSender(sender);
		this.setDefaultUri(properties.getWs().get("anular-guia"));
		this.TOKEN = properties.getTokenAnulacionGuia();
	}
	
	public Respuesta anular(Integer idGuiaAcademica) throws PostgradoException {
		try {
			BeanParametrosAnulacion bp = new BeanParametrosAnulacion();
			bp.setGuiaAcademica(String.valueOf(idGuiaAcademica));
			bp.setToken(TOKEN);
			AnularGuiaAcademica aga = new AnularGuiaAcademica();
			aga.setParametrosAnulacion(bp);
			AnularGuiaAcademicaResponse response = (AnularGuiaAcademicaResponse) this.getWebServiceTemplate().marshalSendAndReceive(aga, new AnulacionGuiaMessageCallback());
			Respuesta r = new Respuesta();
			if (response != null && response.getReturn() != null) {
				if (GUIA_OK.equals(response.getReturn().getCodigo())) {
					r.setStatus(Respuesta.OK);
					log.info("[GUIA ANULADA] " + response.getReturn().getNumGuia());
					r.setMensaje(Respuesta.OK);
				} else {
					r.setStatus(Respuesta.FAIL);
					log.info("[GUIA NO ANULADA] " + response.getReturn().getMensaje());
					r.setMensaje(response.getReturn().getMensaje());
				}
			}
			return r;
		} catch (Exception ex) {
			log.error("No se logro anular la guia: " + idGuiaAcademica, ex);
			throw new PostgradoException("No se logro anular la guia: " + idGuiaAcademica, ex);
		}
	}
	
}
