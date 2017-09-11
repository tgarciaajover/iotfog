package com.advicetec.language.ast;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.IntStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Class to represents a syntax error occuring in the language. 
 * 
 * @author Andres Marentes
 */
public class SyntaxError extends RecognitionException 
{

	/** 
	 * The line number name where the error occurs. 
	 */
	private static final String lineNumberElementName = "Line-number";
	
	/** 
	 * Character number within the line number where the error occurs. 
	 */
	private static final String positionInLineElementName = "position-in-line";
	
	/** 
	 * Message returned for the error. 
	 */
	private static final String messageElementName = "message";
	
	/** 
	 * Offending Token name. 
	 */
	private static final String offendingTokenElementName = "offending-token";
	
	/** 
	 * Offending Token Verbose name. 
	 */
	private static final String offendingTokenVerboseElementName = "offending-verbose-token";
	
	/** 
	 * Error String underlined name. 
	 */
	private static final String errorStringUnderlinedElementName = "error-string-underlined";
	
	
    /**
     * Constructor for the class
     * 
     * @param message  message error
     * @param e		   Exception triggered by the language parser 
     */
    public SyntaxError(String message, RecognitionException e) {
        super(message, e.getRecognizer(), e.getInputStream(), (ParserRuleContext) e.getCtx());
        this.setOffendingToken(e.getOffendingToken());
        this.initCause(e);
    }
    
    /**
     * Constructor for the class
     * 
     * @param message		message error
     * @param t				token generating the error 
     * @param recognizer	Recognizer object to get error information  
     * @param inputStream	string stream with the program being executed 
     * @param ctx			parser context where occurs the error. 
     */
    public SyntaxError(String message, Token t, Recognizer<?,?> recognizer, IntStream inputStream, ParserRuleContext ctx){
    	super(message, recognizer, inputStream, ctx);
    	this.setOffendingToken(t);
    }
    

    /**
     * Returns this syntax error as an XML Document instance. 
     * For example:
     * <pre>
     * {@code
     * <syntaxerror>
     *   <first-name>Philip</first-name>
     *   <last-name>Johnson</last-name>
     *   <info>Professor</info>
     *   <unique-ID>pmj</unique-ID>
     * </syntaxerror>
     * }
     * </pre>
     * @return This contact as XML.
     * @throws Exception If problems occur creating the XML.
     */
    public void toXml(Document doc, Element parent) throws Exception {
       // Create and attach the root element <error>.
       Element errorElement = doc.createElement("error");
       parent.appendChild(errorElement);

      // Now create and attach the fields for this contact.
      attachElement(doc, errorElement, lineNumberElementName, RecognitionExceptionUtil.getLineNumberString(this));
      attachElement(doc, errorElement, positionInLineElementName, RecognitionExceptionUtil.getCharPositionInLineString(this));
      attachElement(doc, errorElement, messageElementName, getMessage());
      attachElement(doc, errorElement, offendingTokenElementName, RecognitionExceptionUtil.getOffendingTokenString(this));
      attachElement(doc, errorElement, errorStringUnderlinedElementName, RecognitionExceptionUtil.getErrorLineStringUnderlined(this).replaceAll("(?m)^|$", "|"));
    }    

    /**
     * Helper function that creates a child element and attaches it to the passed parent element.
     * @param doc The document for creating elements. 
     * @param parent The parent element. 
     * @param childName The name of the child element.
     * @param childValue The text value for the child element. 
     */
    private void attachElement(Document doc, Element parent, String childName, String childValue) {
      Element childElement = doc.createElement(childName);
      childElement.setTextContent(childValue);
      parent.appendChild(childElement);
    }    
}