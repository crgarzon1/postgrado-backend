package co.edu.lasalle.postgrado.components;

import javax.servlet.http.Cookie;

import org.springframework.security.core.Authentication;

import co.edu.lasalle.postgrado.beans.PeticionPL;
import co.edu.lasalle.postgrado.beans.UsuarioCookie;
import co.edu.lasalle.postgrado.exceptions.PostgradoException;
import co.edu.lasalle.postgrado.utils.security.beans.Respuesta;

public interface PLComponent {
	
	/**
	 * Hace el llamado a un PL determinado según la petición.
	 * Este es el metodo general, sin cache.
	 * FIXME: Se debe validar que quien lo llama realmente puede hacerlo, por ejemplo: Un estudiante con otro código no debería, o una UA consultando cosas de otra UA.
	 * @param auth
	 * @param peticion
	 * @return
	 */
	String callPL(Authentication auth, PeticionPL peticion, int metodo, Cookie... cookies);
	
	//FIXME: Esto se puede hacer mejor.
	
	/**
	 * [5 minutos] Estos métodos llaman al primero, pero cada uno tiene un TTL diferente dentro del cache.
	 * Solo aplica llamados GET
	 * @param auth
	 * @param peticion
	 * @return
	 */
	String callPL5m(Authentication auth, PeticionPL peticion, Cookie... cookies);
	/**
	 * [10 minutos] Estos métodos llaman al primero, pero cada uno tiene un TTL diferente dentro del cache.
	 * Solo aplica llamados GET
	 * @param auth
	 * @param peticion
	 * @return
	 */
	String callPL10m(Authentication auth, PeticionPL peticion, Cookie... cookies);
	/**
	 * [15 minutos] Estos métodos llaman al primero, pero cada uno tiene un TTL diferente dentro del cache.
	 * Solo aplica llamados GET
	 * @param auth
	 * @param peticion
	 * @return
	 */
	String callPL15m(Authentication auth, PeticionPL peticion, Cookie... cookies);
	/**
	 * [30 minutos] Estos métodos llaman al primero, pero cada uno tiene un TTL diferente dentro del cache.
	 * Solo aplica llamados GET
	 * @param auth
	 * @param peticion
	 * @return
	 */
	String callPL30m(Authentication auth, PeticionPL peticion, Cookie... cookies);
	/**
	 * [1 hora] Estos métodos llaman al primero, pero cada uno tiene un TTL diferente dentro del cache.
	 * Solo aplica llamados GET
	 * @param auth
	 * @param peticion
	 * @return
	 */
	String callPL1h(Authentication auth, PeticionPL peticion, Cookie... cookies);
	/**
	 * [2 horas] Estos métodos llaman al primero, pero cada uno tiene un TTL diferente dentro del cache.
	 * Solo aplica llamados GET
	 * @param auth
	 * @param peticion
	 * @return
	 */
	String callPL2h(Authentication auth, PeticionPL peticion, Cookie... cookies);
	/**
	 * [1 dia] Estos métodos llaman al primero, pero cada uno tiene un TTL diferente dentro del cache.
	 * Solo aplica llamados GET
	 * @param auth
	 * @param peticion
	 * @return
	 */
	String callPL1d(Authentication auth, PeticionPL peticion, Cookie... cookies);

	/**
	 * 
	 * @param auth
	 * @return
	 */
	String getPerfiles(Authentication auth) throws PostgradoException;
	
	/**
	 * 
	 * @param auth
	 * @param usuario
	 * @param periodo
	 * @return
	 * @throws PostgradoException
	 */
	Respuesta liquidar(Authentication auth, UsuarioCookie usuario, int periodo) throws PostgradoException;
	
	/**
	 * 
	 * @param auth
	 * @param usuario
	 * @param periodo
	 * @param adicionales
	 * @return
	 * @throws PostgradoException
	 */
	Respuesta liquidar(Authentication auth, UsuarioCookie usuario, int periodo, int adicionales) throws PostgradoException;
	
	/**
	 * 
	 * @param auth
	 * @return
	 * @throws PostgradoException
	 */
	Respuesta consultarGuiaCreditosAdicionales(Authentication auth, String token) throws PostgradoException;
	
	/**
	 * Consulta la ultima guia no paga
	 * @param auth
	 * @param codigo
	 * @return
	 * @throws PostgradoException
	 */
	Respuesta consultarUltimaGuia(Authentication auth, String codigo) throws PostgradoException;

}
