package com.loganbe.xml;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XmlUtilities {

	public static Document parseStringToXml(String xml) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true); // Qname exception otherwise, probably invalid xml
		
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource is = new InputSource(new StringReader(xml));
		return builder.parse(is);
	}

	public static Document parseFileToXml(String path) throws SAXException, IOException, ParserConfigurationException {
		File testFile = new File(path);

		//System.out.println("File Exists? " + fXmlFile.exists());
		//System.out.println("File Readable? " + fXmlFile.canRead());
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true); // Qname exception otherwise, probably invalid xml
		
		DocumentBuilder builder = factory.newDocumentBuilder();
		return builder.parse(testFile);
	}
	
}