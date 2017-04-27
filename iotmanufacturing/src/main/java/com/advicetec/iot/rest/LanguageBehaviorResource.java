package com.advicetec.iot.rest;

import java.util.List;

import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.advicetec.language.ast.SyntaxError;
import com.advicetec.language.behavior.SyntaxChecking;

public class LanguageBehaviorResource extends ServerResource 
{

	  /**
	   * Returns the Status instance requested by the URL. 
	   * @return The XML representation of the status, or CLIENT_ERROR_NOT_ACCEPTABLE if the unique ID is not present.
	   * 
	   * @throws Exception If problems occur making the representation.
	   * Shouldn't occur in practice but if it does, Restlet will set the Status code. 
	   */
	  @Put
	  public Representation checkSyntax(Representation representation) throws Exception {
		  
		  System.out.println("En Syntax Cheching");  
		  // Create an empty XML representation.
		  DomRepresentation input = new DomRepresentation(representation);
		  DomRepresentation result = new DomRepresentation();
		  // Get the contact's uniqueID from the URL.

		  SyntaxChecking sintaxChecking = new SyntaxChecking();
		  		  
		  // Convert the XML representation to the Java representation.
		  String program = sintaxChecking.getProgram(input.getDocument());
		  
		  System.out.println("text:" + program);
		  
		  if (program != null){

			  List<SyntaxError> errorList = sintaxChecking.process(program);

			  // Create the Document instance representing this XML.
			  DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			  DocumentBuilder builder = factory.newDocumentBuilder();
			  Document doc = builder.newDocument();

			  Element rootElement = doc.createElement("errors");
			  doc.appendChild(rootElement);

			  for (SyntaxError error : errorList){
				  error.toXml(doc, rootElement);
			  }

			  // The requested contact was found, so add the Contact's XML representation to the response.
			  result.setDocument(doc);

			  // Return the representation.  The Status code tells the client if the representation is valid.
			  return result;
		  } else {

			  // The requested language xml has not a proper formar, so set the Status to indicate this.
			  getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			  return result;
		  } 	
	  }
	
}
