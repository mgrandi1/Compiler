package edu.citadel.cprl;


import edu.citadel.compiler.ErrorHandler;
import edu.citadel.compiler.Position;
import edu.citadel.compiler.ScannerException;
import edu.citadel.compiler.Source;

import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;


public class Scanner
  {
    private Source source;
    private Token  currentToken;
    private StringBuilder scanBuffer;


    /**
     * Initialize scanner with its associated source and advance
     * to the first token.
     */
    public Scanner(Source source) throws IOException
      {
        this.source  = source;
        scanBuffer   = new StringBuilder(100);
        currentToken = new Token();

        advance();           // advance to the first token
      }


    /**
     * Returns a copy of the current token in the source file.
     */
    public Token getToken()
      {
        return new Token(currentToken);
      }


    /**
     * Returns a reference to the current symbol in the source file.
     */
    public Symbol getSymbol()
      {
        return currentToken.getSymbol();
      }


    /**
     * Returns a reference to the position of the current
     * symbol in the source file.
     */
    public Position getPosition()
      {
        return currentToken.getPosition();
      }


    /**
     * Advance to the next token in the source file.
     */
    public void advance() throws IOException
      {
        try
          {
            //System.out.println(source.getCharPosition());

            skipWhiteSpace();

            // currently at starting character of next token
            currentToken.setPosition(source.getCharPosition());
          //  System.out.println(currentToken.getPosition());
            currentToken.setText(null);

            if (source.getChar() == Source.EOF)
              {
                // set symbol but don't advance
                currentToken.setSymbol(Symbol.EOF);
              }
            else if (Character.isLetter((char) source.getChar()))
              {
                String idString = scanIdentifier();
                Symbol scannedSymbol = getIdentifierSymbol(idString);
                currentToken.setSymbol(scannedSymbol);

                if (scannedSymbol == Symbol.identifier)
                    currentToken.setText(idString);
              }
            else if (Character.isDigit((char) source.getChar()))
              {
                currentToken.setText(scanIntegerLiteral());
                currentToken.setSymbol(Symbol.intLiteral);
              }
        
            else
              {
                switch((char) source.getChar())
                  {
                // Arithmetic Operators
                    case '+':
                        currentToken.setSymbol(Symbol.plus);
                        source.advance();
                        break;
                    case '-':
                        currentToken.setSymbol(Symbol.minus);
                        source.advance();
                        break;
                    case '*':
                        currentToken.setSymbol(Symbol.times);
                        source.advance();
                        break;
                    case '/':
                    	source.advance();
                    	if(source.getChar() == '/') {
                    		skipComment();
                    		break;
                    	}else {
                    		currentToken.setSymbol(Symbol.divide);
                    		break;
                    	}
                        
                    case '\n':               
                    	break;
                    	
                //relational operators
                    case '=':
                    	currentToken.setSymbol(Symbol.equals);
                    	source.advance();
                    	break;
                    case '!':
                    	source.advance();
                    	if ((char) source.getChar() == '=') {
                    		currentToken.setSymbol(Symbol.notEqual);
                    		source.advance();
                    		break;
                    	} else
                    		throw new IllegalArgumentException("Exclamation without an equals?!");
                    	
                    case '<':
                    	source.advance();
                    	if ((char) source.getChar() == '=')
                    	  {
                    		currentToken.setSymbol(Symbol.lessOrEqual);
                    		source.advance();
                    	  }
                    	else
                    		currentToken.setSymbol(Symbol.lessThan);
                    	break;
                    case '>':
                        source.advance();
                        if ((char) source.getChar() == '=')
                          {
                            currentToken.setSymbol(Symbol.greaterOrEqual);
                            source.advance();
                          }
                        else
                            currentToken.setSymbol(Symbol.greaterThan);
                        break;
                    //Assignment and other punctuation symbols
                    case '(':
                    	currentToken.setSymbol(Symbol.equals);
                    	source.advance();
                    	break;
                    case ')':
                    	currentToken.setSymbol(Symbol.equals);
                    	source.advance();
                    	break;
                    case '[':
                    	currentToken.setSymbol(Symbol.equals);
                    	source.advance();
                    	break;
                    case ']':
                    	currentToken.setSymbol(Symbol.equals);
                    	source.advance();
                    	break;
                    case ',':
                    	currentToken.setSymbol(Symbol.equals);
                    	source.advance();
                    	break;
                    case ';':
                    	currentToken.setSymbol(Symbol.equals);
                    	source.advance();
                    	break;
                    case '.':
                    	currentToken.setSymbol(Symbol.equals);
                    	source.advance();
                    	break;
                    case ':':
                    	source.advance();
                    	if ((char) source.getChar() == '=') {
                    		currentToken.setSymbol(Symbol.assign);
                    		source.advance();
                    		break;
                    	}else
                    		currentToken.setSymbol(Symbol.semicolon);
                    	break;
                    case '\'':
                    	currentToken.setText(scanCharLiteral());
                        currentToken.setSymbol(Symbol.charLiteral);
                        break;
                    case '"':
                    	currentToken.setText(scanStringLiteral());
                    	currentToken.setSymbol(Symbol.stringLiteral);
                    	break;
                    

// ...

                    default:           // error:  invalid character
                      {
                        String errorMsg = "Invalid character \'"
                                        + ((char) source.getChar()) + "\'";
                        source.advance();
                        throw error(errorMsg);
                      }
                  }
              }
          }
        catch (ScannerException e)
          {
            ErrorHandler.getInstance().reportError(e);

            // set token to either EOF or unknown
            if (source.getChar() == Source.EOF)
              {
                if (getSymbol() != Symbol.EOF)
                    currentToken.setSymbol(Symbol.EOF);
              }
            else
                currentToken.setSymbol(Symbol.unknown);
            	System.out.print("boopboopboop");
          }
      }


    /**
     * Returns the symbol associated with an identifier
     * (Symbol.arrayRW, Symbol.ifRW, Symbol.identifier, etc.)
     */
    private Symbol getIdentifierSymbol(String idString)
      {
    	//Create an empty HashMap 
    	HashMap<String, String> symbols = new HashMap<String, String>();
    	//Fill the HashMap with key/value pairs taken from the Symbol enum class.
    	for (Symbol s : Symbol.values()) {
    		symbols.put(s.name(), s.toString());
    	}
    	
    	/**
    	 * Check the HashMap for a symbol matching the provided String, 
    	 * If one exists, return it. Otherwise, return the identifier symbol. 
    	 */
    	if(symbols.containsValue(idString)) {
    		return Symbol.fromString(idString);
    	}else
    		return Symbol.identifier;
    	
      }


    /**
     *  Skip over a comment.
     */
    private void skipComment() throws ScannerException, IOException
      { 
    		skipToEndOfLine();
    		this.advance();    	
      }


    /**
     * Advance until the symbol in the source file matches the symbol
     * specified in the parameter or until end of file is encountered.
     */
    public void advanceTo(Symbol symbol) throws IOException
      {
        while (true)
          {
            if (getSymbol() == symbol || source.getChar() == Source.EOF)
                return;
            else
                advance();
          }
      }


    /**
     * Advance until the symbol in the source file matches one of the
     * symbols in the given array or until end of file is encountered.
     */
    public void advanceTo(Symbol[] symbols) throws IOException
      {
        while (true)
          {
            if (search(symbols, currentToken.getSymbol()) >= 0
                || source.getChar() == Source.EOF)
                return;
            else
                advance();
          }

      }


    /**
     * Performs a linear search of the array for the given value.
     *
     * @return the index of the value in the array if found, otherwise -1.
     */
    private int search(Symbol[] symbols, Symbol value)
      {
        for (int i = 0;  i < symbols.length;  ++i)
          {
            if (symbols[i].equals(value))
                return i;
          }
        return -1;
      }


    /**
     * Clear the scan buffer (makes it empty).
     */
    private void clearScanBuffer()
      {
        scanBuffer.delete(0, scanBuffer.length());
      }


    /**
     * Scans characters in the source file for a valid identifier using the
     * lexical rule: identifier = letter ( letter | digit)* .
     *
     * @return the string of letters and digits for the identifier.
     */
    private String scanIdentifier() throws IOException
      {
    	assert Character.isLetter((char) source.getChar()) :
            "scanIdentifier(): check identifier start for letter at position "
            + getPosition();
    	
    	clearScanBuffer();
    	do
        {
          scanBuffer.append((char) source.getChar());
          source.advance();
        }
      while (Character.isLetterOrDigit((char) source.getChar()));

      return scanBuffer.toString();
    
      }


    /**
     * Scans characters in the source file for a valid integer literal.
     * Assumes that source.getChar() is the first character of the Integer literal.
     *
     * @return the string of digits for the integer literal.
     */
    private String scanIntegerLiteral() throws ScannerException, IOException
      {
        // assumes that source.getChar() is the first digit of the integer literal
        assert Character.isDigit((char) source.getChar()) :
            "scanIntegerLiteral(): check integer literal start for digit at position "
            + getPosition();

        clearScanBuffer();

        do
          {
            scanBuffer.append((char) source.getChar());
            source.advance();
          }
        while (Character.isDigit((char) source.getChar()));

        return scanBuffer.toString();
      }


    /**
     * Scan characters in the source file for a String literal.  Escaped
     * characters are not converted; e.g., '\t' is not converted to the tab
     * character since the assembler performs the conversion.  Assumes that
     * source.getChar() is the opening double quote (") of the String literal.
     *
     * @return the string of characters for the string literal, including
     *         opening and closing quotes
     */
    private String scanStringLiteral() throws ScannerException, IOException
      {
    	assert (char) source.getChar() == '"' :
            "scanCharLiteral(): check for opening quote (\") at position "
            + getPosition();
    	
    	clearScanBuffer();
    	//append the opening double quote
    	char c = (char) source.getChar();
    	scanBuffer.append(c);
    	source.advance();
    	
    	c = (char) source.getChar();

    	while(c != '\"') {
    		checkGraphicChar(source.getChar());
            c = (char) source.getChar();
            if (c != '\\')
            	scanBuffer.append(c);
            if(c == '\\') {
            	
            	try {
            		scanBuffer.append(scanEscapedChar());
            	}catch (ScannerException e)
                {
                    checkGraphicChar(source.getChar());
                    c = (char) source.getChar();

                  }
            	continue;    
            }
            
            
            source.advance();
    	}
    	
    	c = (char) source.getChar();
    	scanBuffer.append(c);
    	source.advance();
    	return scanBuffer.toString();
// ...
// Note: This one is a little tricky!
      }


    /**
     * Scan characters in the source file for a Char literal.  Escaped
     * characters are not converted; e.g., '\t' is not converted to the tab
     * character since the assembler performs that conversion.  Assumes that
     * source.getChar() is the opening single quote (') of the Char literal.
     *
     * @return the string of characters for the char literal, including
     *         opening and closing single quotes.
     */
    private String scanCharLiteral() throws ScannerException, IOException
      {
        // assumes that source.getChar() is the opening single quote for the char literal
        assert (char) source.getChar() == '\'' :
            "scanCharLiteral(): check for opening quote (\') at position "
            + getPosition();

        String errorMsg = "Invalid Char literal.";
        clearScanBuffer();

        // append the opening single quote
        char c = (char) source.getChar();
        scanBuffer.append(c);
        source.advance();

        checkGraphicChar(source.getChar());
        c = (char) source.getChar();

        if (c == '\\')                      // escaped character
          {
            try
              {
                scanBuffer.append(scanEscapedChar());
              }
            catch (ScannerException e)
              {
                checkGraphicChar(source.getChar());
                c = (char) source.getChar();

                if (c == '\'')              // assume bad escape similar to '\x'
                  {
                    source.advance();       // move past closing single quote
                    throw e;                // rethrow the exception
                  }
              }
          }
        else if (c == '\'')                 // either '' (empty) or '''; both are invalid
          {
            source.advance();
            c = (char) source.getChar();

            if (c == '\'')                  // three single quotes in a row
                source.advance();

            throw error(errorMsg);
          }
        else
          {
            scanBuffer.append(c);
            source.advance();
          }

        c = (char) source.getChar();        // should be the closing single quote
        checkGraphicChar(c);

        if (c == '\'')                      // should be the closing single quote
          {
            scanBuffer.append(c);           // append the closing quote
            source.advance();
          }
        else
            throw error(errorMsg);

        return scanBuffer.toString();
      }


    /**
     * Scans characters in the source file for an escaped character; i.e.,
     * a character preceded by a backslash.  This method checks escape
     * characters \b, \t, \n, \f, \r, \", \', and \\.  If the character
     * following a backslash is anything other than one of these characters,
     * then an exception is thrown.  Note that the escaped character sequence
     * is returned unmodified; i.e., \t returns "\t", not the tab character.
     * Assumes that source.getChar() is the escape character (\).
     *
     * @return the escaped character sequence unmodified.
     */
    private String scanEscapedChar() throws ScannerException, IOException
      {
        // assumes that source.getChar() is the backslash for the escaped char
        assert (char) source.getChar() == '\\' :
            "scanEscapedChar(): check for escape character ('\\') at position "
            + getPosition();

        // Need to save current position for error reporting.
        Position backslashPosition = source.getCharPosition();

        source.advance();
        checkGraphicChar(source.getChar());
        char c = (char) source.getChar();

        source.advance();  // leave source at second character following the backslash

        switch (c)
          {
            case 'b'  : return "\\b";   // backspace
            case 't'  : return "\\t";   // tab
            case 'n'  : return "\\n";   // linefeed (a.k.a. newline)
            case 'f'  : return "\\f";   // form feed
            case 'r'  : return "\\r";   // carriage return
            case '\"' : return "\\\"";   // double quote
            case '\'' : return "\\\'";   // single quote
            case '\\' : return "\\\\";   // backslash
            default   : throw new ScannerException(backslashPosition, "Illegal escape character.");
          }
      }


    /**
     * Fast skip over white space.
     */
    private void skipWhiteSpace() throws IOException
      {
        while (Character.isWhitespace((char) source.getChar()))
          {
            source.advance();
          }
      }


    /**
     * Advances over source characters to the end of the current line.
     */
    private void skipToEndOfLine() throws ScannerException, IOException
      {
        while ((char) source.getChar() != '\n')
          {
            source.advance();
            checkEOF();
          }
      }


    /**
     * Checks that the integer represents a graphic character in the Unicode
     * Basic Multilingual Plane (BMP).
     *
     * @throws ScannerException if the integer does not represent a BMP graphic
     *         character.
     */
    private void checkGraphicChar(int n) throws ScannerException
      {
        if (n == Source.EOF)
            throw error("End of file reached before closing quote for Char or String literal.");
        else if (n > 0xffff)
            throw error("Character not in Unicode Basic Multilingual Pane (BMP)");
        else
          {
            char c = (char) n;
            if (c == '\r' || c == '\n')   // special check for end of line
                throw error("Char and String literals can not extend past end of line.");
            else if (Character.isISOControl(c))    // Sorry.  No ISO control characters.
                throw new ScannerException(source.getCharPosition(),
                    "Control characters not allowed in Char or String literal.");
          }
      }


    /**
     * Returns a ScannerException with the specified error message.
     */
    private ScannerException error(String message)
      {
        return new ScannerException(getPosition(), message);
      }


    /**
     * Used to check for EOF in the middle of scanning tokens that
     * require closing characters such as strings and comments.
     *
     * @throws ScannerException if source is at end of file.
     */
    private void checkEOF() throws ScannerException
      {
        if (source.getChar() == Source.EOF)
            throw new ScannerException(getPosition(), "Unexpected end of file");
      }
  }
