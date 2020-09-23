package co.edu.lasalle.postgrado.utils.guias;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
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
import org.springframework.oxm.MarshallingFailureException;
import org.springframework.oxm.XmlMappingException;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.oxm.mime.MimeContainer;

import co.edu.unisalle.cti.guiaspago.servicios.GenerarGuiaPago;
import co.edu.unisalle.cti.guiaspago.servicios.GenerarGuiaPagoResponse;

public class GuiasMarshaller extends Jaxb2Marshaller {
	
	private static final Logger log = LoggerFactory.getLogger(GuiasMarshaller.class);
	
	public static final String PREFIJO = "ser";

	public GuiasMarshaller() {
		super();
		this.setContextPath(GuiasClient.CONTEXT_PATH);
	}

	@Override
	public void marshal(Object graph, Result result, MimeContainer mimeContainer) throws XmlMappingException {
		log.debug("[JAXB MARSHAL] iniciando...");
		try {
			JAXBContext contexto = this.getJaxbContext();
			Marshaller jaxbMarshaller = contexto.createMarshaller();
			QName qName = new QName(PREFIJO + ":generarGuiaPago");
			jaxbMarshaller.marshal(new JAXBElement<GenerarGuiaPago>(qName, GenerarGuiaPago.class, (GenerarGuiaPago) graph), result);
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

	@Override
	public Object unmarshal(Source source, MimeContainer mimeContainer) throws XmlMappingException {
		try {
			Unmarshaller jaxbUnmarshaller = getJaxbContext().createUnmarshaller();
			JAXBElement<GenerarGuiaPagoResponse> root = jaxbUnmarshaller.unmarshal(source, GenerarGuiaPagoResponse.class);
			return root.getValue();
		} catch (JAXBException ex) {
			throw new MarshallingFailureException("No se logro leer la respuesta", ex);
		}
	}
	
}
