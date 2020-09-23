package co.edu.lasalle.postgrado.utils.auth;

import java.io.IOException;
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.soap.SoapEnvelope;
import org.springframework.ws.soap.saaj.SaajSoapMessage;

public class IdentityMessageCallback implements WebServiceMessageCallback {
	
	private static final Logger log = LoggerFactory.getLogger(IdentityMessageCallback.class);

	@Override
	public void doWithMessage(WebServiceMessage message) throws IOException, TransformerException {
		SaajSoapMessage saajSoapMessage = (SaajSoapMessage)message;
		SoapEnvelope envelope = saajSoapMessage.getEnvelope();
		envelope.addNamespaceDeclaration("iden", "http://identity.cti.unisalle.edu.co/");
		if (log.isDebugEnabled()) {
			try {
				Transformer t = TransformerFactory.newInstance().newTransformer();
				t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
				t.setOutputProperty(OutputKeys.INDENT, "yes");
				StringWriter sw = new StringWriter();
				t.transform(new DOMSource(saajSoapMessage.getDocument()), new StreamResult(sw));
				log.debug("[SOAP MSG]\n" + sw.toString());
			} catch (TransformerException ex) {
				log.error("No transformo...", ex);
			}
		}
	}

}
