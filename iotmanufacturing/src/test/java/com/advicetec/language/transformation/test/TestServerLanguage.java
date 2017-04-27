package com.advicetec.language.transformation.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.junit.BeforeClass;
import org.restlet.resource.ClientResource;
import org.junit.Test;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.restlet.ext.xml.DomRepresentation;

import com.advicetec.iot.rest.IotRestServer;

public class TestServerLanguage 
{

	  /** The port used for testing. */
	  private static int testPort = 8112;

	  private static final String EXTENSION = "properties";


	  /**
	   * Start up a test server before testing any of the operations on this resource.
	   * @throws Exception If problems occur starting up the server. 
	   */
	  @BeforeClass
	  public static void startServer () throws Exception {
	    IotRestServer.runServer(testPort);
	  }
	  
	  
	  /**
	   * Test the cycle of putting a new Contact on the server, retrieving it, then deleting it.
	   * @throws Exception If problems occur.
	   */
	  @Test
	  public void testSyntaxCheck() throws Exception {
	    // Construct the URL to test.
	    String uniqueID = "DS";
	    String testUrl = String.format("http://localhost:%s/languageserver/checker/%s", testPort,
	        uniqueID);
	    ClientResource client = new ClientResource(testUrl);
	    
	    // Construct the payload: an XML representation of a Contact.
		String program = "test/transformtest_errors." + EXTENSION;

		System.out.println("Syntax Check:" + program);
	    
	    
        // Create the Document instance representing this XML.
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
	        // Create and attach the root element <contact>.
        Element rootElement = doc.createElement("program");
        rootElement.setTextContent(program);
        doc.appendChild(rootElement);
	    
	    DomRepresentation representation = new DomRepresentation();
	    representation.setDocument(doc);
	    
	    // Now put the Contact to the server. 
	    DomRepresentation representation2 = new DomRepresentation(client.put(representation));
	    
	    // Let's now try to retrieve the Contact instance we just put on the server. 
	    //DomRepresentation representation2 = new DomRepresentation(client.get());
	    System.out.println("returned representation");

	    printDocument(representation2.getDocument(), System.out);
	    //assertEquals("Checking retrieved contact's ID", uniqueID, contact2.getUniqueID());
	    
	    // Now let's get rid of the sucker.
	    // client.delete();
	    
	  }	


	  private void printDocument(Document doc, OutputStream out) throws IOException, TransformerException {
		    TransformerFactory tf = TransformerFactory.newInstance();
		    Transformer transformer = tf.newTransformer();
		    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

		    transformer.transform(new DOMSource(doc), 
		         new StreamResult(new OutputStreamWriter(out, "UTF-8")));
		}
	  
}
