package co.edu.lasalle.postgrado.utils.auth;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import co.edu.unisalle.cti.identity.ObtenerUsuarioSalle;
import co.edu.unisalle.cti.identity.ObtenerUsuarioSalleByCedula;
import co.edu.unisalle.cti.identity.ObtenerUsuarioSalleByUser;
import co.edu.unisalle.cti.identity.ObtenerUsuarioSallePortal;

public final class IdentityRootElementFactory {
	
	public final static int OBTENER_USUARIO_SALLE = 1;
	public final static int OBTENER_USUARIO_SALLE_BY_USER = 2;
	public final static int OBTENER_USUARIO_SALLE_BY_CEDULA = 3;
	public final static int OBTENER_USUARIO_SALLE_PORTAL = 4;
	
	public static JAXBElement<?> getElementRoot(int operacion, String rootName, Object graph) {
		QName qName = new QName(IdentityMarshaller.PREFIJO + rootName);
		switch (operacion) {
		case OBTENER_USUARIO_SALLE:
			return new JAXBElement<ObtenerUsuarioSalle>(qName, ObtenerUsuarioSalle.class, (ObtenerUsuarioSalle) graph);
		case OBTENER_USUARIO_SALLE_BY_USER:
			return new JAXBElement<ObtenerUsuarioSalleByUser>(qName, ObtenerUsuarioSalleByUser.class, (ObtenerUsuarioSalleByUser) graph);
		case OBTENER_USUARIO_SALLE_BY_CEDULA:
			return new JAXBElement<ObtenerUsuarioSalleByCedula>(qName, ObtenerUsuarioSalleByCedula.class, (ObtenerUsuarioSalleByCedula) graph);
		case OBTENER_USUARIO_SALLE_PORTAL:
			return new JAXBElement<ObtenerUsuarioSallePortal>(qName, ObtenerUsuarioSallePortal.class, (ObtenerUsuarioSallePortal) graph);
		}
		return null;
	}

}
