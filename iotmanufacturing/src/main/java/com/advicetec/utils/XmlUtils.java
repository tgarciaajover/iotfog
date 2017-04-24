package com.advicetec.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public final class XmlUtils {

	public static void attachElement(Document doc, Element parent, 
			String childName, String childValue){
		Element childElement  = doc.createElement(childName);
		childElement.setTextContent(childValue);
		parent.appendChild(childElement);
	}
	
	public static String getElementTextContent(Document doc, String elementName){
		Element element = (Element) doc.getElementsByTagName(elementName).item(0);
		return element.getTextContent();
	}
}
