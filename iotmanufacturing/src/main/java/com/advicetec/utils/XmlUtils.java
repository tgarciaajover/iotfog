package com.advicetec.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Utility class for XML handling.
 * @author advicetec
 *
 */
public final class XmlUtils {

	/**
	 * Attaches an element to a given document.
	 * @param doc Document to attach an element.
	 * @param parent Element parent.
	 * @param childName Child name to be attached.
	 * @param childValue Value for the child.
	 */
	public static void attachElement(Document doc, Element parent, 
			String childName, String childValue){
		Element childElement  = doc.createElement(childName);
		childElement.setTextContent(childValue);
		parent.appendChild(childElement);
	}
	
	/**
	 * Returns the value of a given child name and document. 
	 * @param doc xml document.
	 * @param elementName child's name to get the value.
	 * @return the value of a given child name and document. 
	 */
	public static String getElementTextContent(Document doc, String elementName){
		Element element = (Element) doc.getElementsByTagName(elementName).item(0);
		return element.getTextContent();
	}
}
