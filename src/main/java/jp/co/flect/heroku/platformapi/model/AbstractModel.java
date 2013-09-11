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
	
	private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	
	private Map<String, Object> map;
	
	private String requestId;
	
	public AbstractModel() {
		this(new LinkedHashMap<String, Object>());
	}
	
	public AbstractModel(Map<String, Object> map) {
		this.map = map;
	}
	
	public void init(Map<String, Object> map) {
		this.map.putAll(map);
	}
	
	public String getRequestId() { return this.requestId;}
	public void setRequestId(String s) { this.requestId = s;}
	
	public List<String> keys() {
		List<String> list = new ArrayList<String>();
		doKeys(this.map, list, null);
		return list;
	}
	
	private static void doKeys(Map<String, Object> m, List<String> list, String prefix) {
		for (Map.Entry<String, Object> entry : m.entrySet()) {
			String key = entry.getKey();
			if (entry.getValue() instanceof Map) {
				doKeys((Map<String, Object>)entry.getValue(), list, key);
			} else {
				list.add(prefix == null ? key : prefix + "." + key);
			}
		}
	}
	
	public Object get(String name) { 
		return doGet(this.map, name);
	}
	
	private static Object doGet(Map<String, Object> m, String name) {
		int idx = name.indexOf(".");
		if (idx == -1) {
			return m.get(name);
		} else {
			String prefix = name.substring(0, idx);
			String suffix = name.substring(idx+1);
			Map<String, Object> childMap = (Map<String, Object>)m.get(prefix);
			if (childMap == null) {
				return null;
			}
			return doGet(childMap, suffix);
		}
	}
	
	public void set(String name, Object value) { 
		doSet(this.map, name, value);
	}
	
	private static void doSet(Map<String, Object> m, String name, Object value) {
		int idx = name.indexOf(".");
		if (idx == -1) {
			m.put(name, value);
		} else {
			String prefix = name.substring(0, idx);
			String suffix = name.substring(idx+1);
			Map<String, Object> childMap = (Map<String, Object>)m.get(prefix);
			if (childMap == null) {
				childMap = new LinkedHashMap<String, Object>();
				m.put(prefix, childMap);
			}
			doSet(childMap, suffix, value);
		}
	}
	
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
		if (o instanceof Integer) {
			return ((Integer)o).intValue();
		} else if (o instanceof Number) {
			int n = ((Number)o).intValue();
			set(name, n);
			return n;
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
	
	public long getAsLong(String name) {
		Object o = get(name);
		if (o instanceof Long) {
			return ((Long)o).longValue();
		} else if (o instanceof Number) {
			long n = ((Number)o).longValue();
			set(name, n);
			return n;
		} else if (o instanceof String) {
			try {
				long n = Long.parseLong((String)o);
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
	
	public String toString() { return this.map.toString();}
}

