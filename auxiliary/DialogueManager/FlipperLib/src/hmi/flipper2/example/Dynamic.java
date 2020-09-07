package hmi.flipper2.example;

import java.io.StringReader;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

public class Dynamic {

	private final static boolean verbose = false;

	private Double base;

	public Dynamic() {
		if (verbose)
			System.out.println("Dynamic() called");
		this.base = 0.0;
	}

	public Dynamic(Double base) {
		if (verbose)
			System.out.println("Dynamic(Double(" + base + ")) called");
		this.base = base;
	}

	public static Boolean alwaysTrue() {
		if (verbose)
			System.out.println("Dynamic:method:alwaysTrue(" + "" + ") called.");
		return Boolean.TRUE;
	}

	public static void f(String s) {
		if (verbose)
			System.out.println("Dynamic:static method:f(String(" + s + ")) called.");
	}

	public static void f(Double d) {
		if (verbose)
			System.out.println("Dynamic:static method:f(Double(" + d + ")) called.");
	}

	public Double fplus(Double dl, Double dr) {
		if (verbose)
			System.out.println("Dynamic:static method:f(Double(" + dl + "," + dr + ")) called.");
		return base + dl + dr;
	}
	
	public Double recur(Double dl, Dynamic dyn) {
		if (verbose)
			System.out.println("Dynamic: method:recur(" + dl + ", obj.base=" + dyn.base  + ")) called.");
		return base + dl;
	}

	public String fjson(String json) {
		if (verbose)
			System.out.println("Dynamic:static method:fjson(json=" + json + ") called.");
		JsonObject jso = string2json(json);
		JsonObject jso_counter = jso.getJsonObject("counter");
		JsonValue jso_value = jso_counter.get("value");

		JsonObject returnObject = Json.createObjectBuilder().add("extraction", "example").add("value", jso_value)
				.build();
		return returnObject.toString();
	}

	public static JsonObject string2json(String s) {
		JsonReader reader = Json.createReader(new StringReader(s));
		JsonObject result = reader.readObject();
		reader.close();
		return result;
	}

}
