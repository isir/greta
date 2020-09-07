package hmi.flipper2.dataflow;

import java.util.HashSet;
import java.util.Set;

public interface DataFlow {

	public Set<String> flowIn();
	
	public Set<String> flowOut();
	
	public static final String isp = "is.";
	public static final int isp_length = isp.length();
	
	public static Set<String> extractRefs(String js_expr) {
		Set<String> res = new HashSet<String>();

		int jsp_length = js_expr.length();
		int start = js_expr.indexOf(isp);
		while (start >= 0) {
			int p = start + isp_length;
			if (!(start > 0
					&& (js_expr.charAt(start - 1) == '.' || Character.isJavaIdentifierPart(js_expr.charAt(start - 1)))))
				while (true) {
					p++;
					if (p >= jsp_length) {
						res.add(js_expr.substring(start, p));
						break;
					} else {
						char c = js_expr.charAt(p);
						if (!(c == '.' || Character.isJavaIdentifierPart(c))) {
							res.add(js_expr.substring(start, p));
							break;
						}
					}
				}
			start = js_expr.indexOf(isp, p);
		}
		return res;
	}
	
	public static final Set<String> EMPTY = new HashSet<String>();
	
	public static Set<String> union(Set<String> l, Set<String> r) {
		if ( l == EMPTY )
			return r;
		else if ( r == EMPTY )
			return l;
		else {
			HashSet<String> res = new HashSet<String>();
			res.addAll(l);
			res.addAll(r);
			return res;
		}
	}
	
//	public static void main(String[] args) {
//		Set<String> res = extractRefs("is.a  \"xis.\" en is.y.v._z  is.x");
//		System.out.println("RES:" + res);
//	}
	
}
