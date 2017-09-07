package com.advicetec.language.ast;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

/**
 * This class is used to collect errors during the syntax checking 
 * 
 * @author Andres Marentes
 *
 */
public class CollectionErrorListener extends BaseErrorListener {

    /**
     *  List of errors collected during syntax checking.
     */
    private final List<SyntaxError> errors = new ArrayList<SyntaxError>();

    /**
     * Gets the list of errors collected.
     * 
     * @return list of errors collected.
     */
    public List<SyntaxError> getErrors() {
        return errors;
    }

    /**
     * Registers an error in the list, it takes as parameters the context where it occurs.
     * 
     * @param offendingSymbol  		symbol generating the error.
     * @param line 					line in the text where the errors occurs.
     * @param charPositionInLine	Position within the line where the error occurs
     * @param msg					Error Message
     */
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        if (e == null) {
            // e is null when the parser was able to recover in line without exiting the surrounding rule.
            e = new InlineRecognitionException(msg, recognizer, ((Parser)recognizer).getInputStream(), ((Parser)recognizer).getContext(), (Token) offendingSymbol);
        }
        this.errors.add(new SyntaxError(msg, e));
    }
}