package hmi.flipper2.sax;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import hmi.flipper2.FlipperException;

public class SimpleSAXParser {
	
	public static SimpleElement parseString(String path, String xml_str) throws FlipperException {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			SimpleHandler handler = new SimpleHandler();
			saxParser.parse(new InputSource(new StringReader(xml_str)), handler);
			return handler.root;
		} catch (SAXException | ParserConfigurationException e) {
			throw new FlipperException(e, "# Parsing XML File: "+path);
		} catch(IOException e) {
			throw new FlipperException(e);
		}
	}

	public static SimpleElement parseFile(String path) throws FlipperException {
		try {
			return parseString(path, readFile(path));
		} catch (IOException e) {
			throw new FlipperException(e);
		}
	}
	
	public final static String readFile(String path) throws IOException {
		return readFile(path, StandardCharsets.UTF_8);
	}

	public final static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
}
