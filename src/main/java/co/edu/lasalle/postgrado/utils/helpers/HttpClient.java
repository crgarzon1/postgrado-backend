package co.edu.lasalle.postgrado.utils.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.edu.lasalle.postgrado.exceptions.PostgradoException;
import co.edu.lasalle.postgrado.properties.PostgradoProperties;

public final class HttpClient {

	private static final Logger log = LoggerFactory.getLogger(HttpClient.class);
	public static final int PETICION_POST = 0;
	public static final int PETICION_GET = 1;
	private PostgradoProperties properties;
	
	public HttpClient(PostgradoProperties properties) {
		this.properties = properties;
	}
	
	public String urlencode(String texto) throws UnsupportedEncodingException {
		return URLEncoder.encode(texto, java.nio.charset.StandardCharsets.UTF_8.toString());
	}
	
	public String obtenerURL(int esquema, String procedimiento, int tipoPeticion, Map<String, String> parametros, Cookie... cookies) throws PostgradoException {
		long init = System.currentTimeMillis();
		String url = this.properties.getUrlsEsquemas().get(String.valueOf(esquema)) + procedimiento;
		CloseableHttpClient client = HttpClients
				.custom()
                .build();
		log.debug("URL: " + url);
		BufferedReader br = null;
		try {
			HttpRequestBase peticion = null;
			if (PETICION_POST == tipoPeticion) {
				peticion = new HttpPost(url);
				peticion.setHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0");
				peticion.setHeader(HttpHeaders.ACCEPT_CHARSET, "utf-8");
				if (parametros != null && !parametros.isEmpty()) {
					List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
					for (String key : parametros.keySet()) {
						String value = parametros.get(key);
						log.debug("\t-> parametro: (" + key + "-" + value + ")");
						urlParameters.add(new BasicNameValuePair(key, value));
					}
					((HttpPost)peticion).setEntity(new UrlEncodedFormEntity(urlParameters));				
				}
			} else if (PETICION_GET == tipoPeticion) {
				if (parametros != null && !parametros.isEmpty()) {
					StringBuffer sb = new StringBuffer();
					sb.append(url);
					sb.append("?");
					for (String key : parametros.keySet()) {
						sb.append(key);
						sb.append("=");
						sb.append(parametros.get(key));
						sb.append("&");
					}
					url = sb.toString();
					url = url.substring(0, url.length() - 1);
					log.debug("[GET PARAMETERS] " + url);
				}
				peticion = new HttpGet(url);
			}
			RequestConfig requestConfig = RequestConfig.custom()
					  .setSocketTimeout(this.properties.getHttpTimeout())
					  .setConnectTimeout(this.properties.getHttpTimeout())
					  .setConnectionRequestTimeout(this.properties.getHttpTimeout())
					  //.setCookieSpec(CookieSpecs.STANDARD)
					  .build();
			peticion.setConfig(requestConfig);
			// 23/02/2020 Alejandro Arevalo
			if (cookies != null && cookies.length > 0) {
				String value = "";
				for (Cookie c : cookies) {
					// peticion.setHeader("Cookie", c.getName() + "=" + c.getValue());
					value += c.getName() + "=" + c.getValue() + ";";
				}
				peticion.addHeader("Cookie", value);
			}
			HttpResponse res = client.execute(peticion);
			if (res.getStatusLine().getStatusCode() == 200) {
				br = new BufferedReader(new InputStreamReader(res.getEntity().getContent()));
				StringBuffer sb = new StringBuffer();
				String line = "";
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				String json = sb.toString();
				log.debug("HTTP: " + json);
				return json;
			}
			throw new PostgradoException("Http error: " + res.getStatusLine().getStatusCode());
		} catch (ClientProtocolException ex) {
			if (log.isDebugEnabled()) {
				throw new PostgradoException("Http: error de protocolo (" + url + ") -> " + ex.getMessage());
			} else {
				throw new PostgradoException("Http: error de protocolo -> " + ex.getMessage());
			}
		} catch (IOException ex) {
			if (log.isDebugEnabled()) {
				throw new PostgradoException("Http: error de IO -> (" + url + ") " + ex.getMessage());
			} else {
				throw new PostgradoException("Http: error de IO -> " + ex.getMessage());
			}
		} catch (Exception ex) {
			if (log.isDebugEnabled()) {
				throw new PostgradoException("Http: error (" + url + ") -> " + ex.getMessage(), ex);
			} else {
				throw new PostgradoException("Http: error -> " + ex.getMessage(), ex);
			}
		} finally {
			try {
				if (br != null) {
					br.close();
				}
				client.close();
			} catch (IOException ex) {
				log.error("Cx no cerrada: " + url, ex);
			}
			long t = System.currentTimeMillis() - init;
			if (t >= Math.ceil(this.properties.getHttpTimeout() * .75)) {
				log.warn("http >> " + t + "ms: " + url);
			} else if (log.isDebugEnabled()) {
				log.debug("http >> " + t + "ms: " + url);
			}
		}
	}
	
}
