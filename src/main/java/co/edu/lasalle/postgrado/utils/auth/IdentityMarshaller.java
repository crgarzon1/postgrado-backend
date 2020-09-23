package co.edu.lasalle.postgrado.utils.auth;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.oxm.MarshallingFailureException;
import org.springframework.oxm.XmlMappingException;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.oxm.mime.MimeContainer;

import co.edu.unisalle.cti.identity.ObtenerUsuarioSalleByCedulaResponse;
import co.edu.unisalle.cti.identity.ObtenerUsuarioSalleByUserResponse;
import co.edu.unisalle.cti.identity.ObtenerUsuarioSallePortalResponse;
import co.edu.unisalle.cti.identity.ObtenerUsuarioSalleResponse;

public class IdentityMarshaller extends Jaxb2Marshaller {
	
	private static final Logger log = LoggerFactory.getLogger(IdentityMarshaller.class);
	
	public static final String PREFIJO = "iden:";
	
	private int operacion;
	private String rootName;

	public IdentityMarshaller() {
		super();
		this.setContextPath(IdentityClient.CONTEXT_PATH);
	}
	
	public void setOperacion(int operacion) {
		this.operacion = operacion;
	}

	public void setRootName(String rootName) {
		this.rootName = rootName;
	}

	@Override
	public void marshal(Object graph, Result result, @Nullable MimeContainer mimeContainer) throws XmlMappingException {
		log.debug("[JAXB MARSHAL] iniciando...");
		try {
			JAXBContext contexto = this.getJaxbContext();
			Marshaller jaxbMarshaller = contexto.createMarshaller();
			jaxbMarshaller.marshal(IdentityRootElementFactory.getElementRoot(this.operacion, this.rootName, graph), result);
			if (log.isDebugEnabled()) {
				try {
					Transformer t = TransformerFactory.newInstance().newTransformer();
					t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
					t.setOutputProperty(OutputKeys.INDENT, "yes");
					StringWriter sw = new StringWriter();
					t.transform(new DOMSource(((DOMResult)result).getNode()), new StreamResult(sw));
					log.debug("[MARSHAL]\n" + sw.toString());
				} catch (TransformerException ex) {
					log.error("No transformo...", ex);
				}
			}
		} catch (JAXBException ex) {
			throw new MarshallingFailureException("No se logro crear el XML", ex);
		}
	}
	
	private <T> T getObjeto(Unmarshaller jaxbUnmarshaller, Source source, Class<T> clazz) throws JAXBException {
		JAXBElement<T> root = jaxbUnmarshaller.unmarshal(source, clazz);
		T obj = root.getValue();
		log.debug("[JAXB UNMARSHALL] Objeto creado: " + clazz.getName());
		return obj;
	}

	@Override
	public Object unmarshal(Source source, @Nullable MimeContainer mimeContainer) throws XmlMappingException {
		log.debug("[JAXB UNMARSHALL] iniciando...");
		try {
			Unmarshaller jaxbUnmarshaller = getJaxbContext().createUnmarshaller();
			switch(this.operacion) {
			case IdentityRootElementFactory.OBTENER_USUARIO_SALLE:
				return this.getObjeto(jaxbUnmarshaller, source, ObtenerUsuarioSalleResponse.class);
			case IdentityRootElementFactory.OBTENER_USUARIO_SALLE_BY_USER:
				return this.getObjeto(jaxbUnmarshaller, source, ObtenerUsuarioSalleByUserResponse.class);
			case IdentityRootElementFactory.OBTENER_USUARIO_SALLE_BY_CEDULA:
				return this.getObjeto(jaxbUnmarshaller, source, ObtenerUsuarioSalleByCedulaResponse.class);
			case IdentityRootElementFactory.OBTENER_USUARIO_SALLE_PORTAL:
				return this.getObjeto(jaxbUnmarshaller, source, ObtenerUsuarioSallePortalResponse.class);
			default:
				throw new MarshallingFailureException("Operacion no definida.");
			}
		} catch (JAXBException ex) {
			throw new MarshallingFailureException("No se logro leer la respuesta", ex);
		}
	}

}
