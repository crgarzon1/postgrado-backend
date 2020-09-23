package co.edu.lasalle.postgrado.beans;

import java.io.IOException;
import java.io.Serializable;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class JSONBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Override
	public String toString() {
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (IOException ex) {
			return super.toString();
		}
	}
	
}
