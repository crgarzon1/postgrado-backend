package co.edu.lasalle.postgrado.utils.guias;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.transport.WebServiceMessageSender;

import co.edu.lasalle.postgrado.beans.AParametro;
import co.edu.lasalle.postgrado.exceptions.PostgradoException;
import co.edu.lasalle.postgrado.properties.PostgradoProperties;
import co.edu.lasalle.postgrado.utils.security.beans.Respuesta;
import co.edu.unisalle.cti.guiaspago.servicios.BeanParametros;
import co.edu.unisalle.cti.guiaspago.servicios.GenerarGuiaPago;
import co.edu.unisalle.cti.guiaspago.servicios.GenerarGuiaPagoResponse;

public class GuiasClient extends WebServiceGatewaySupport {

	private static final Logger log = LoggerFactory.getLogger(GuiasClient.class);
	
	public static final String CONTEXT_PATH = "co.edu.unisalle.cti.guiaspago.servicios";
	private static final String GUIA_OK = "OK";
	
	public GuiasClient(PostgradoProperties properties, WebServiceMessageSender sender) {
		super();
		GuiasMarshaller marshaller = new GuiasMarshaller();
		this.setMarshaller(marshaller);
		this.setUnmarshaller(marshaller);
		this.setMessageSender(sender);
		this.setDefaultUri(properties.getWs().get("guias"));
	}

	public Respuesta liquidar(AParametro pm, int periodo) throws PostgradoException {
		//TODO: Agregar bloqueos.
		try {
			if (log.isDebugEnabled()) {
				log.debug("[GUIA]");
				log.debug(pm.toString());
				log.debug("----------------------");
			}
			BeanParametros p = new BeanParametros();
			p.setAnio(pm.getAnio());
			p.setAnioIngreso(pm.getAnioIngreso());
			p.setAnioreintegro(pm.getAnioreintegro());
			p.setAplicaIndicador(pm.getAplicaIndicador());
			p.setCobrarRecargo(pm.getCobrarRecargo());
			p.setCod2DoPrograma(pm.getCod2DoPrograma());
			p.setCodigo(pm.getCodigo());
			p.setCorreoElectronico(pm.getCorreoElectronico());
			p.setCreditosInscritos(pm.getCreditosInscritos());
			p.setCreditosSemestre(pm.getCreditosSemestre());
			p.setCredsInscritos2DoProg(pm.getCredsInscritos2DoProg());
			p.setCredsSemestre2DoProg(pm.getCredsSemestre2DoProg());
			p.setDepartamento(pm.getDepartamento() == null ? -1 : pm.getDepartamento().shortValue());
			p.setDireccion(pm.getDireccion());
			if (pm.getFechaNacimiento() == null || "".equals(pm.getFechaNacimiento())) {
				pm.setFechaNacimiento("1900/01/01");
			}
			Date d = new SimpleDateFormat("yyyy/MM/dd").parse(pm.getFechaNacimiento());
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(d);
			p.setFechaNacimiento(DatatypeFactory.newInstance().newXMLGregorianCalendar(cal));
			if (pm.getFechaAjuste() != null && !pm.getFechaAjuste().isEmpty() && !"1900/01/01".equals(pm.getFechaAjuste())) {
				GregorianCalendar cal2 = new GregorianCalendar();
				cal2.setTime(new SimpleDateFormat("yyyy/MM/dd").parse(pm.getFechaAjuste()));
				p.setFechaPazo(DatatypeFactory.newInstance().newXMLGregorianCalendar(cal2));
			}
			p.setGenero(pm.getGenero());
			p.setGuiaAcademica(pm.getGuiaAcademica());
			p.setIdentificacion(pm.getIdentificacion());
			p.setJornada(pm.getJornada());
			p.setNombre2DoPrograma(pm.getNombre2DoPrograma());
			p.setNombrePrograma(pm.getNombrePrograma());
			p.setPais(pm.getPais() == null ? -1 : pm.getPais().shortValue());
			p.setPeriodo(pm.getPeriodo());
			p.setPoblacion(pm.getPoblacion() == null ? -1 : pm.getPoblacion().shortValue());
			p.setPorcentajeRecargo(pm.getPorcentajeRecargo() == null ? 0 : pm.getPorcentajeRecargo().doubleValue());
			p.setPrimerApellido(pm.getPrimerApellido());
			p.setPrimerNombre(pm.getPrimerNombre());
			p.setPrimerSemestre(pm.getPrimerSemestre());
			p.setPrograma(pm.getPrograma());
			p.setSegundoApellido(pm.getSegundoApellido());
			p.setSegundoNombre(pm.getSegundoNombre());
			p.setSemestre(pm.getSemestre());
			p.setTelefono(pm.getTelefono());
			p.setTipoIdentificacion(pm.getTipoIdentificacion());
			p.setTipoMatricula(pm.getTipoMatricula());
			p.setTipoPrograma(pm.getTipoPrograma());
			p.setTotalCreditos(pm.getTotalCreditos());
			p.setTotalSemestres(pm.getTotalSemestres());
			p.setCobrarcreditosadicionales(pm.getCobrarcreditosadicionales());
			p.setCreditosadescontar(pm.getCreditosadescontar());
			GenerarGuiaPago ggp = new GenerarGuiaPago();
			ggp.setParametros(p);
			GenerarGuiaPagoResponse response = (GenerarGuiaPagoResponse) this.getWebServiceTemplate().marshalSendAndReceive(ggp, new GuiasMessageCallback());
			Respuesta r = new Respuesta();
			if (response != null && response.getReturn() != null) {
				if (GUIA_OK.equals(response.getReturn().getCodigo())) {
					r.setStatus(Respuesta.OK);
					log.info("[GUIA LIQUIDADA] " + response.getReturn().getNumGuia());
					r.setMensaje(pm.getGuiaAcademica() + ";" + response.getReturn().getMensaje() + ";" + response.getReturn().getNumGuia());
				} else {
					r.setStatus(Respuesta.FAIL);
					log.debug("[GUIA NO LIQUIDADA] " + response.getReturn().getMensaje());
					r.setMensaje(response.getReturn().getMensaje());
				}
			}
			return r;
		} catch (ParseException ex) {
			log.debug("No se puede hacer el parseo de la fecha.", ex);
			throw new PostgradoException("No se logro transformar la fecha: " + ex.getMessage());
		} catch (DatatypeConfigurationException ex) {
			log.debug("No se puede crear el DatatypeConfigurator", ex);
			throw new PostgradoException("No se puede crear el DatatypeConfigurator: " + ex.getMessage());
		}
	}
	
}
