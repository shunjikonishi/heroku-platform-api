package jp.co.flect.heroku.platformapi.model;

import jp.co.flect.heroku.transport.HttpRequest;

public class Range {
	
	private String field = "id";
	private Boolean sortAsc = null;
	private String start = null;
	private String end = null;
	private int max = 0;
	
	private String[] sortableFields;
	private Range nextRange;
	
	public Range() {
	}
	
	public Range(String header) {
		parse(header);
	}
	
	private void parse(String str) {
		int idx = str.indexOf(' ');
System.out.println("parse1: " + str + ", " + idx);
		if (idx == -1) {
			this.field = str;
			return;
		}
		this.field = str.substring(0, idx);
		int idx2 = str.indexOf("..", idx);
System.out.println("parse2: " + field + ", " + idx2);
		if (idx2 == -1) {
			return;
		}
		this.start = str.substring(idx, idx2).trim();
		if (this.start.length() == 0) {
			this.start = null;
		}
		int idx3 = str.indexOf(';', idx2 + 2);
System.out.println("parse3: " + start + ", " + idx3);
		if (idx3 == -1) {
			this.end = str.substring(idx2 + 2).trim();
		} else {
			this.end = str.substring(idx2 + 2, idx3).trim();
		}
		if (this.end.length() == 0) {
			this.end = null;
		}
System.out.println("parse4: " + end + ", " + idx3);
		if (idx3 == -1) {
			return;
		}
		int idx4 = str.indexOf("max=", idx3);
System.out.println("parse5: " + idx4);
		if (idx4 != -1) {
			int spos = idx4 + "max=".length();
			int epos = spos;
			for (int i=spos; i<str.length(); i++) {
				char c = str.charAt(i);
System.out.println("parse6: " + c);
				if (c >= '0' && c <= '9') {
					epos++;
				} else {
					break;
				}
			}
System.out.println("parse7: " + spos + ", " + epos);
			if (epos > spos) {
				this.max = Integer.parseInt(str.substring(spos, epos));
			}
		}
		if (str.indexOf("order=desc", idx3) != -1) {
System.out.println("parse8: sortAsc");
			this.sortAsc = false;
		} else if (str.indexOf("order=asc", idx3) != -1) {
System.out.println("parse8: sortDesc");
			this.sortAsc = true;
		}
	}
	
	public void setSortOrder(String fieldName, boolean asc) {
		this.field = fieldName;
		this.sortAsc = asc;
	}
	
	public String getField() { return this.field;}
	public boolean isSortAsc() { return this.sortAsc;}
	
	public String getStart() { return this.start;}
	public void setStart(String s) { this.start = s;}
	
	public String getEnd() { return this.end;}
	public void setEnd(String s) { this.end = s;}
	
	public int getMax() { return this.max;}
	public void setMax(int n) { this.max = n;}
	
	public void apply(HttpRequest request) {
		if (sortAsc == null && start == null && end == null && max <= 0) {
			return;
		}
		request.setHeader("Range", toString());
	}
	
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append(field).append(" ");
		
		if (start != null) {
			buf.append(start);
		}
		buf.append("..");
		if (end != null) {
			buf.append(end);
		}
		if (max > 0 || sortAsc != null) {
			boolean bNext = false;
			buf.append("; ");
			if (max > 0) {
				buf.append("max=").append(max);
				bNext = true;
			}
			if (sortAsc != null) {
				if (bNext) {
					buf.append(", ");
				}
				buf.append("order=").append(sortAsc ? "asc" : "desc");
			}
		}
		return buf.toString();
	}
	
	public String[] getSortableFields() { return this.sortableFields;}
	public void setSortableFields(String[] v) { this.sortableFields = v;}
	
	public boolean isSortableField(String s) {
		if (this.sortableFields == null) {
			return false;
		}
		for (String sf : this.sortableFields) {
			if (sf.equals(s)) {
				return true;
			}
		}
		return false;
	}
	
	public Range getNextRange() { return this.nextRange;}
	public void setNextRange(Range v) { this.nextRange = v;}
}
