package jp.co.flect.heroku.platformapi.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public abstract class AbstractModel implements Serializable {
	
	private static final long serialVersionUID = -281893291198955622L;;
	
	private static final String DATE_FORMAT = "yyyy-MM-ddTHH:mm:ssZ";
	
	private Map<String, Object> _map;
	
	public AbstractModel() {
		this(new LinkedHashMap<String, Object>());
	}
	
	public AbstractModel(Map<String, Object> map) {
		this._map = map;
	}
	
	public void init(Map<String, Object> map) {
		this._map.putAll(map);
	}
	
	public List<String> keys() {
		return new ArrayList<String>(this._map.keySet());
	}
	
	public Object get(String name) { return this._map.get(name);}
	public void set(String name, Object value) { this._map.put(name, value);}
	
	public String getAsString(String name) {
		Object o = get(name);
		return o == null ? null : o.toString();
	}
	
	public Date getAsDate(String name) {
		Object o = get(name);
		if (o instanceof Date) {
			return (Date)o;
		} else if (o instanceof String) {
			try {
				Date d = new SimpleDateFormat(DATE_FORMAT).parse((String)o);
				set(name, d);
				return d;
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public int getAsInt(String name) {
		Object o = get(name);
		if (o instanceof Number) {
			return ((Number)o).intValue();
		} else if (o instanceof String) {
			try {
				int n = Integer.parseInt((String)o);
				set(name, n);
				return n;
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		return 0;
	}
	
	public boolean getAsBoolean(String name) {
		Object o = get(name);
		if (o instanceof Boolean) {
			return ((Boolean)o).booleanValue();
		} else if (o != null) {
			Boolean b = Boolean.valueOf(o.toString());
			set(name, b);
			return b;
		}
		return false;
	}
	
	public String toString() { return this._map.toString();}
}

