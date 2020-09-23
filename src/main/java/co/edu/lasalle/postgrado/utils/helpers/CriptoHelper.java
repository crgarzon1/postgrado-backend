package co.edu.lasalle.postgrado.utils.helpers;

import java.io.IOException;
import java.security.Key;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.edu.lasalle.postgrado.beans.UsuarioCookie;
import co.edu.lasalle.postgrado.beans.UsuarioPortal;
import co.edu.lasalle.postgrado.exceptions.PostgradoException;
import co.edu.lasalle.utils.cripto.CodecFactory;
import co.edu.lasalle.utils.cripto.CodecHelper;
import co.edu.lasalle.utils.cripto.key.KeyGenFactory;

public final class CriptoHelper {
	
	private static final Logger log = LoggerFactory.getLogger(CriptoHelper.class);
	private Key key;

	public CriptoHelper() {
		try {
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			this.key = (Key) CodecHelper.loadKeyFile(loader.getResourceAsStream(KeyGenFactory.PRIVATE_KEY_FILE_NAME));
			log.debug("llave privada cargada...");
		} catch (IOException ex) {
			log.debug("No cargo la llave privada.", ex);
		}
	}
	
	/**
	 * Lee un token bajo el estandar SIA [fecha formato ssmmHHddMMyyyy]#[token]#[aleatorio] y retorna [token]
	 * si no han pasado los [vencimiento] milisegundos desde [fecha].
	 * @param token
	 * @param key
	 * @param vencimiento
	 * @return
	 * @throws PostgradoException
	 */
	public String leerToken(String token, String key, long vencimiento) throws PostgradoException {
		try {
			String[] valores = CodecFactory.instanceCodec(CodecFactory.DES).decode(token, key).split("#");
			log.debug("Token desencriptado: " + Arrays.toString(valores));
			if (valores.length != 3) {
				throw new PostgradoException("token no valido");
			} else if (new Date().getTime() > new SimpleDateFormat("ssmmHHddMMyyyy").parse(valores[0]).getTime() + vencimiento) {
				throw new PostgradoException("token vencido");
			}
			return valores[1];
		} catch (PostgradoException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new PostgradoException("Fallo inesperado al desencriptar: " + ex.getMessage(), ex);
		}
	}
	
	/**
	 * Crea un token tipo SIA.
	 * @param token
	 * @param key
	 * @return
	 * @throws PostgradoException
	 */
	public String crearToken(String token, String key) throws PostgradoException {
		try {
			log.debug("Token a encriptar: " + token);
			return CodecFactory.instanceCodec(CodecFactory.DES).encode(
					new SimpleDateFormat("ssmmHHddMMyyyy").format(new Date()) + "#" +
					token + "#" + (Math.random() * 1e6), key);
		} catch (Exception ex) {
			throw new PostgradoException("Fallo inesperado encriptando: " + ex.getMessage(), ex);
		}
	}
	
	/**
	 * Lee la cookie que genera el SIA al realizar la autenticación.
	 * @param request
	 * @param nombreCookie
	 * @param vencimiento
	 * @param key
	 * @return
	 * @throws PostgradoException
	 */
	public UsuarioCookie leerCookieSIA(HttpServletRequest request, String nombreCookie, long vencimiento, String key) throws PostgradoException {
		try {
			for (Cookie c : request.getCookies()) {
				if (nombreCookie.equals(c.getName())) {
					return this.leerCookieSIA(c.getValue(), vencimiento, key);
				}
			}
		} catch (Exception ex) {
			log.debug("No se logro autenticar.", ex);
			throw new PostgradoException("No se logro autenticar: " + ex.getMessage(), ex);
		}
		throw new PostgradoException("Usuario no autenticado o sesión vencida. Ingrese de nuevo su usuario y clave del SIA por el portal institucional");
	}

	/**
	 * Lee la cookie que genera el SIA al realizar la autenticación, a partir del valor.
	 * @param token
	 * @param vencimiento
	 * @param key
	 * @return
	 * @throws PostgradoException
	 */
	public UsuarioCookie leerCookieSIA(String token, long vencimiento, String key) throws PostgradoException {
		String txt = this.leerToken(token, key, vencimiento);
		log.debug("Cookie: " + txt);
		String[] u = txt.split(";");
		UsuarioCookie user = new UsuarioCookie();
		user.setUsuario(u[0]);
		user.setClave(u[1]);
		user.setCodigo(u[2]);
		user.setNombre(u[3]);
		if (u.length < 5) {
			throw new PostgradoException("Usuario sin documento registrado.");
		}
		user.setDocumento(u[4]);
		return user;
	}
	
	public UsuarioPortal leerCookiePortal(HttpServletRequest request, String nombreCookie, long vencimiento) throws PostgradoException {
		try {
			for (Cookie c : request.getCookies()) {
				if (nombreCookie.equals(c.getName())) {
					return this.leerCookiePortal(c.getValue(), vencimiento);
				}
			}
		} catch (Exception ex) {
			log.debug("No se logro autenticar.", ex);
			throw new PostgradoException("No se logro autenticar: " + ex.getMessage(), ex);
		}
		throw new PostgradoException("Usuario no autenticado o sesión vencida. Ingrese de nuevo su usuario y clave por el portal institucional");
	}
	
	public UsuarioPortal leerCookiePortal(String tokenEnc, long vencimiento) throws PostgradoException {
		try {
			String token = CodecFactory.instanceCodec(CodecFactory.RSAPKCS1Padding).decode(tokenEnc, this.key);
			String[] datos = token.split(";");
			if (datos.length != 4) {
				throw new PostgradoException("Cookie mal formada.");
			}
			Date fecha = new SimpleDateFormat("yyyyMMddHHmmss").parse(datos[3]);
			if (new Date().getTime() > fecha.getTime() + vencimiento) {
				throw new PostgradoException("cookie vencida");
			}
			UsuarioPortal up = new UsuarioPortal();
			up.setUsuario(datos[0]);
			up.setNombre(datos[1]);
			up.setEmail(datos[2]);
			up.setFecha(fecha);
			return up;
		} catch (Exception ex) {
			log.debug("No se logro autenticar.", ex);
			throw new PostgradoException("No se logro autenticar: " + ex.getMessage(), ex);
		}
	}
	
	
	
}
