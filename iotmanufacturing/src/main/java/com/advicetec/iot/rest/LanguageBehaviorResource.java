package com.advicetec.iot.rest;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import com.advicetec.language.behavior.BehaviorSyntaxChecking;

/**
 * This class exposes the behavior language checker. By this interface, users can verify if a program written in 
 * the production behavior language is correct or not. 
 * 
 * When the program has errors, the the system sends an error list briefly explaining the causes. 
 *  
 * @author Andres Marentes
 *
 */
public class LanguageBehaviorResource extends ServerResource 
{

	static Logger logger = LogManager.getLogger(LanguageBehaviorResource.class.getName());
	
	/**
	   * Returns the list of errors found.
	   *  
	   * @return A JSON array representation with the list of errors, or CLIENT_ERROR_BAD_REQUEST if an invalid call was made.
	   * 
	   * @throws Exception If problems occur reading the representation.
	   * Shouldn't occur in practice but if it does, Restlet will set the Status code. 
	   */
	  @Put
	  public Representation checkSyntax(Representation representation) throws Exception {
		  
		  logger.debug("En Syntax Cheching");  

		  // Creates an empty JSON representation.
		  DomRepresentation input = new DomRepresentation(representation);
		  DomRepresentation result = new DomRepresentation();

		  BehaviorSyntaxChecking sintaxChecking = new BehaviorSyntaxChecking();
		  		  
		  // Gets the program text from the JSON representation.
		  String program = sintaxChecking.getProgram(input.getDocument());
		  
		  logger.debug("text:" + program);
		  
		  if (program != null){

			  List<SyntaxError> errorList = sintaxChecking.process(program);

			  // Creates the Document instance representing this JSON.
			  DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			  DocumentBuilder builder = factory.newDocumentBuilder();
			  Document doc = builder.newDocument();

			  Element rootElement = doc.createElement("errors");
			  doc.appendChild(rootElement);

			  for (SyntaxError error : errorList){
				  error.toXml(doc, rootElement);
			  }

			  // The request was successfully processed, so we can add the JSON array to the response.
			  result.setDocument(doc);

			  // Returns the representation. The Status code reports a valid processing.
			  return result;
			  
		  } else {

			  // The request language JSON has not a proper format, so we set the status to indicate this fact.
			  getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			  return result;
		  } 	
	  }
	
}
