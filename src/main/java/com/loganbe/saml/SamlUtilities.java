package com.loganbe.saml;

import org.opensaml.saml2.core.Response;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.w3c.dom.Element;

public class SamlUtilities {

	public static Response parseResponse(Element rootElement) throws UnmarshallingException {
		UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
		Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(rootElement);
		XMLObject responseXmlObj = unmarshaller.unmarshall(rootElement);

		return (Response) responseXmlObj;
	}
	
}