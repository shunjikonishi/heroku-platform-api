package jp.co.flect.heroku.json;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Type;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

public class JsonByJackson implements Json {
	
	public List<Map<String, Object>> parseArray(String str) {
		try {
			TypeReference type = new TypeReference<List<Map<String, Object>>>() {};
			return new ObjectMapper().readValue(str, type);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	public Map<String, Object> parse(String str) {
		try {
			TypeReference type = new TypeReference<Map<String, Object>>() {};
			return new ObjectMapper().readValue(str, type);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	public <T> T parse(String str, Class<T> clazz) {
		try {
			return new ObjectMapper().readValue(str, clazz);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	public String serialize(Object obj) {
		try {
			return new ObjectMapper().writeValueAsString(obj);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
}
