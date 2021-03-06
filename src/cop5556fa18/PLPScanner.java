/**
 * Name: Yihao Wu
 * Assignment Number: Project#6
 * Date Due: November 20, 2018
 */

package cop5556fa18;

import java.util.ArrayList;
import java.util.Arrays;

public class PLPScanner {
	
	@SuppressWarnings("serial")
	public static class LexicalException extends Exception {

		int pos;

		public LexicalException(String message, int pos) {
			super(message);
			this.pos = pos;
		}

		public int getPos() {
			return pos;
		}
	}
	
	public static enum Kind {
		IDENTIFIER, INTEGER_LITERAL, BOOLEAN_LITERAL, FLOAT_LITERAL,
		STRING_LITERAL, CHAR_LITERAL,
		KW_print        /* print       */,
		KW_sleep        /* sleep       */,
		KW_int          /* int         */,
		KW_float        /* float       */,
		KW_boolean      /* boolean     */,
		KW_if           /* if          */,
		KW_while 		/* while 	   */,
		KW_char         /* char        */,
		KW_string       /* string      */,
		KW_abs			/* abs 		   */,
		KW_sin			/* sin 		   */,
		KW_cos			/* cos 		   */, 
		KW_atan			/* atan        */,
		KW_log			/* log 		   */,
		OP_ASSIGN       /* =           */, 
		OP_EXCLAMATION  /* !           */,
		OP_QUESTION		/* ? 		   */,
		OP_EQ           /* ==          */,
		OP_NEQ          /* !=          */, 
		OP_GE           /* >=          */,
		OP_LE           /* <=          */,
		OP_GT           /* >           */,
		OP_LT           /* <           */,
		OP_AND			/* & 		   */, 
		OP_OR			/* | 		   */,
		OP_PLUS         /* +           */,
		OP_MINUS        /* -           */,
		OP_TIMES        /* *           */,
		OP_DIV          /* /           */,
		OP_MOD          /* %           */,
		OP_POWER        /* **          */, 
		LPAREN          /* (           */,
		RPAREN          /* )           */,
		LBRACE          /* {           */, 
		RBRACE          /* }           */,
		LSQUARE			/* [           */, 
		RSQUARE			/* ]           */, 
		SEMI            /* ;           */,
		OP_COLON		/* : 		   */,
		COMMA           /* ,           */,
		DOT             /* .           */,
		EOF				/* end of file */,
	}
	
	/**
	 * Class to represent Tokens.
	 *
	 */
	public class Token {
		public final Kind kind;
		public final int pos; // position of first character of this token in the input. Counting starts at 0
								// and is incremented for every character.
		public final int length; // number of characters in this token

		public Token(Kind kind, int pos, int length) {
			super();
			this.kind = kind;
			this.pos = pos;
			this.length = length;
		}
		
		/**
		 * Calculates and returns the line on which this token resides. The first line
		 * in the source code is line 1.
		 * 
		 * @return line number of this Token in the input.
		 */
		public int line() {
			return PLPScanner.this.line(pos) + 1;
		}

		/**
		 * Returns position in line of this token.
		 * 
		 * @param line.
		 *            The line number (starting at 1) for this token, i.e. the value
		 *            returned from Token.line()
		 * @return
		 */
		public int posInLine(int line) {
			return PLPScanner.this.posInLine(pos, line - 1) + 1;
		}

		/**
		 * Returns the position in the line of this Token in the input. Characters start
		 * counting at 1. Line termination characters belong to the preceding line.
		 * 
		 * @return
		 */
		public int posInLine() {
			return PLPScanner.this.posInLine(pos) + 1;
		}

		public String toString() {
			int line = line();
			return "[" + kind + "," +
			       String.copyValueOf(chars, pos, length) + "," +
			       pos + "," +
			       length + "," +
			       line + "," +
			       posInLine(line) + "]";
		}

		/**
		 * Since we override equals, we need to override hashCode, too.
		 * 
		 * See
		 * https://docs.oracle.com/javase/9/docs/api/java/lang/Object.html#hashCode--
		 * where it says, "If two objects are equal according to the equals(Object)
		 * method, then calling the hashCode method on each of the two objects must
		 * produce the same integer result."
		 * 
		 * This method, along with equals, was generated by eclipse
		 * 
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((kind == null) ? 0 : kind.hashCode());
			result = prime * result + length;
			result = prime * result + pos;
			return result;
		}

		/**
		 * Override equals so that two Tokens are equal if they have the same Kind, pos,
		 * and length.
		 * 
		 * This method, along with hashcode, was generated by eclipse.
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Token other = (Token) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (kind != other.kind)
				return false;
			if (length != other.length)
				return false;
			if (pos != other.pos)
				return false;
			return true;
		}

		/**
		 * used in equals to get the Scanner object this Token is associated with.
		 * 
		 * @return
		 */
		private PLPScanner getOuterType() {
			return PLPScanner.this;
		}
	}
	
	/**
	 * Array of positions of beginning of lines. lineStarts[k] is the pos of the
	 * first character in line k (starting at 0).
	 * 
	 * If the input is empty, the chars array will have one element, the synthetic
	 * EOFChar token and lineStarts will have size 1 with lineStarts[0] = 0;
	 */
	int[] lineStarts;

	int[] initLineStarts() {
		ArrayList<Integer> lineStarts = new ArrayList<Integer>();
		int pos = 0;

		for (pos = 0; pos < chars.length; pos++) {
			lineStarts.add(pos);
			char ch = chars[pos];
			while (ch != EOFChar && ch != '\n' && ch != '\r') {
				pos++;
				ch = chars[pos];
			}
			if (ch == '\r' && chars[pos + 1] == '\n') {
				pos++;
			}
		}
		// convert arrayList<Integer> to int[]
		return lineStarts.stream().mapToInt(Integer::valueOf).toArray();
	}
	
	int line(int pos) {
		int line = Arrays.binarySearch(lineStarts, pos);
		if (line < 0) {
			line = -line - 2;
		}
		return line;
	}

	public int posInLine(int pos, int line) {
		return pos - lineStarts[line];
	}

	public int posInLine(int pos) {
		int line = line(pos);
		return posInLine(pos, line);
	}
	
	/**
	 * Sentinal character added to the end of the input characters.
	 */
	static final char EOFChar = 128;

	/**
	 * The list of tokens created by the scan method.
	 */
	final ArrayList<Token> tokens;

	/**
	 * An array of characters representing the input. These are the characters from
	 * the input string plus an additional EOFchar at the end.
	 */
	final char[] chars;

	/**
	 * position of the next token to be returned by a call to nextToken
	 */
	private int nextTokenPos = 0;
	
	PLPScanner(String inputString) {
		int numChars = inputString.length();
		this.chars = Arrays.copyOf(inputString.toCharArray(), numChars + 1); // input string terminated with null char
		chars[numChars] = EOFChar;
		tokens = new ArrayList<Token>();
		lineStarts = initLineStarts();
	}
	
	private enum State {START, START_IDENT, IN_IDENT, IN_INT, IN_FLOAT, IN_STRING, IN_CHAR,
		END_CHAR, IN_COMM, IN_COMM_START, IN_COMM_END, IN_POWER, START_ZERO, IN_EQ, IN_NEQ, IN_GE, IN_LE};
	
	public PLPScanner scan() throws LexicalException {
		int pos = 0;
		State state = State.START;
		int startPos = 0;

		while (pos < chars.length) {
			char ch = chars[pos];
				switch(state) {
				case START: {
					startPos = pos;
					switch (ch) {
						case EOFChar: {
							tokens.add(new Token(Kind.EOF, startPos, 0));
							pos++; // next iteration will terminate loop
						}
						break;
						case ';': {
							tokens.add(new Token(Kind.SEMI, startPos, pos - startPos + 1));
							pos++;
						}
						break;
						case ':': {
							tokens.add(new Token(Kind.OP_COLON, startPos, pos - startPos + 1));
							pos++;
						}
						break;
						case ',': {
							tokens.add(new Token(Kind.COMMA, startPos, pos - startPos + 1));
							pos++;
						}
						break;
						case '?': {
							tokens.add(new Token(Kind.OP_QUESTION, startPos, pos - startPos + 1));
							pos++;
						}
						break;
						case '|': {
							tokens.add(new Token(Kind.OP_OR, startPos, pos - startPos + 1));
							pos++;
						}
						break;
						case '&': {
							tokens.add(new Token(Kind.OP_AND, startPos, pos - startPos + 1));
							pos++;
						}
						break;
						case '+': {
							tokens.add(new Token(Kind.OP_PLUS, startPos, pos - startPos + 1));
							pos++;
						}
						break;
						case '-': {
							tokens.add(new Token(Kind.OP_MINUS, startPos, pos - startPos + 1));
							pos++;
						}
						break;
						case '/': {
							tokens.add(new Token(Kind.OP_DIV, startPos, pos - startPos + 1));
							pos++;
						}
						break;
						case '(': {
							tokens.add(new Token(Kind.LPAREN, startPos, pos - startPos + 1));
							pos++;
						}
						break;
						case ')': {
							tokens.add(new Token(Kind.RPAREN, startPos, pos - startPos + 1));
							pos++;
						}
						break;
						case '[': {
							tokens.add(new Token(Kind.LSQUARE, startPos, pos - startPos + 1));
							pos++;
						}
						break;
						case ']': {
							tokens.add(new Token(Kind.RSQUARE, startPos, pos - startPos + 1));
							pos++;
						}
						break;
						case '{': {
							tokens.add(new Token(Kind.LBRACE, startPos, pos - startPos + 1));
							pos++;
						}
						break;
						case '}': {
							tokens.add(new Token(Kind.RBRACE, startPos, pos - startPos + 1));
							pos++;
						}
						break;
						case '0': {
							state = State.START_ZERO;
							pos++;
						}
						break;
						case '=': {
							state = State.IN_EQ;
							pos++;
						}
						break;
						case '!': {
							state = State.IN_NEQ;
							pos++;
						}
						break;
						case '>': {
							state = State.IN_GE;
							pos++;
						}
						break;
						case '<': {
							state = State.IN_LE;
							pos++;
						}
						break;
						case '*': {
							state = State.IN_POWER;
							pos++;
						}
						break;
						case '%': {
							state = State.IN_COMM_START;
							pos++;
						}
						break;
						case '\'':{
							state = State.IN_CHAR;
							pos++;
						}
						break;
						case '"': {
							state = State.IN_STRING;
							pos++;
						}
						break;
						case '_': {
							state = State.START_IDENT;
							pos++;
						}
						break;
						case ' ': {
							pos++;
						}
						break;
						case '\n': {
							pos++;
						}
						break;
						case '\r': {
							pos++;
						}
						break;
						case '\t': {
							pos++;
						}
						break;
						case '\f': {
							pos++;
						}
						break;
						default: {
							if ((ch >= 'A' && ch <='Z') || (ch >= 'a' && ch <= 'z')) {
								state = State.IN_IDENT;
								pos++;
							}
							else if (ch >= '1' && ch <= '9') {
								state = State.IN_INT;
								pos++;
							}
							else error(pos, line(pos), posInLine(pos), "illegal char");
						}
					}//switch ch
				}
				break;
				case START_IDENT: {
					if (ch == '_') {
						pos++;
					}
					else if ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z')) {
						state = State.IN_IDENT;
						pos++;
					}
					else {
						error(pos, line(pos), posInLine(pos), "identifier must contain at least one alphabet");
					}
				}
				break;
				case IN_IDENT: {
					if ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z')
							|| ch == '_' || (ch >= '0' && ch <= '9')) {
						pos++;
					}
					else {
						String ident = new String(chars, startPos, pos - startPos);
						switch (ident) {
						case "print": {
							tokens.add(new Token(Kind.KW_print, startPos, pos - startPos));
						}
						break;
						case "sleep": {
							tokens.add(new Token(Kind.KW_sleep, startPos, pos - startPos));
						}
						break;
						case "int": {
							tokens.add(new Token(Kind.KW_int, startPos, pos - startPos));
						}
						break;
						case "float": {
							tokens.add(new Token(Kind.KW_float, startPos, pos - startPos));
						}
						break;
						case "boolean": {
							tokens.add(new Token(Kind.KW_boolean, startPos, pos - startPos));
						}
						break;
						case "char": {
							tokens.add(new Token(Kind.KW_char, startPos, pos - startPos));
						}
						break;
						case "string": {
							tokens.add(new Token(Kind.KW_string, startPos, pos - startPos));
						}
						break;
						case "if": {
							tokens.add(new Token(Kind.KW_if, startPos, pos - startPos));
						}
						break;
						case "true": {
							tokens.add(new Token(Kind.BOOLEAN_LITERAL, startPos, pos - startPos));
						}
						break;
						case "false": {
							tokens.add(new Token(Kind.BOOLEAN_LITERAL, startPos, pos - startPos));
						}
						break;
						case "while": {
							tokens.add(new Token(Kind.KW_while, startPos, pos - startPos));
						}
						break;
						case "sin": {
							tokens.add(new Token(Kind.KW_sin, startPos, pos - startPos));
						}
						break;
						case "cos": {
							tokens.add(new Token(Kind.KW_cos, startPos, pos - startPos));
						}
						break;
						case "atan": {
							tokens.add(new Token(Kind.KW_atan, startPos, pos - startPos));
						}
						break;
						case "abs": {
							tokens.add(new Token(Kind.KW_abs, startPos, pos - startPos));
						}
						break;
						case "log": {
							tokens.add(new Token(Kind.KW_log, startPos, pos - startPos));
						}
						break;
						default: {
							tokens.add(new Token(Kind.IDENTIFIER, startPos, pos - startPos));
						}
						}
						state = State.START;
					}
				}
				break;
				case IN_INT: {
					if (ch >= '0' && ch <= '9') {
						pos++;
					}
					else if (ch == '.') {
						state = State.IN_FLOAT;
						pos++;
					}
					else if(pos - startPos > 10) {
						error(pos, line(pos), posInLine(pos), "int value is beyond upper limitation of JAVA");
					}
					else {
						String integer = new String(chars, startPos, pos - startPos);
						if(Long.valueOf(integer) > Integer.MAX_VALUE) {
							error(pos, line(pos), posInLine(pos), "int value is beyond upper limitation of JAVA");
						}
						else {
							tokens.add(new Token(Kind.INTEGER_LITERAL, startPos, pos - startPos));
							state = State.START;
						}
					}
				}
				break;
				case IN_FLOAT: {
					if (ch >= '0' && ch <= '9') {
						pos++;
					}
					else if (chars[pos - 1] == '.') {
						error(pos, line(pos), posInLine(pos), "float is ended illegally");
					}
					else if (pos - startPos > 48) {
						error(pos, line(pos), posInLine(pos), "float value is beyond upper limitation of JAVA");
					}
					else {
						try {
							Float.parseFloat(new String(chars, startPos, pos - startPos));
							tokens.add(new Token(Kind.FLOAT_LITERAL, startPos, pos - startPos));
							state = State.START;
						} catch (NumberFormatException e) {
							error(pos, line(pos), posInLine(pos), "float value is beyond upper limitation of JAVA");
						}
					}
				}
				break;
				case IN_STRING: {
					if(ch == '"') {
						tokens.add(new Token(Kind.STRING_LITERAL, startPos, pos - startPos + 1));
						state = State.START;
					}
					else if (ch == EOFChar || ch == '\n' || ch == '\r' || ch == '\f') {
						error(pos, line(pos), posInLine(pos), "String is not ended legally");
					}
					pos++;
				}
				break;
				case IN_CHAR: {
					if(ch == '\'') {
						tokens.add(new Token(Kind.CHAR_LITERAL, startPos, pos - startPos + 1));
						state = State.START;
					}
					else {
						state = State.END_CHAR;
					}
					pos++;
				}
				break;
				case END_CHAR: {
					if (ch == '\'') {
						tokens.add(new Token(Kind.CHAR_LITERAL, startPos, pos - startPos + 1));
						state = State.START;
						pos++;
					}
					else {
						error(pos, line(pos), posInLine(pos), "illegal char formate");
					}
				}
				break;
				case IN_COMM_START: {
					if (ch == '{') {
						state = State.IN_COMM;
						pos++;
					}
					else {
						tokens.add(new Token(Kind.OP_MOD, startPos, pos - startPos));
						state = State.START;
					}
				}
				break;
				case IN_COMM: {
					if (ch == '%') {
						state = State.IN_COMM_END;
					}
					else if (ch == EOFChar) {
						error(pos, line(pos), posInLine(pos), "illegally end comment");
					}
					pos++;
				}
				break;
				case IN_COMM_END: {
					if (ch == '}') {
						state = State.START;
					}
					else if (ch == '{') {
						error(pos, line(pos), posInLine(pos), "there mustn't be a \"%{\" in comments");
					}
					else if (ch == EOFChar) {
						error(pos, line(pos), posInLine(pos), "illegally end comment");
					}
					else if (ch != '%') {
						state = State.IN_COMM;
					}
					pos++;
				}
				break;
				case IN_POWER: {
					if (ch == '*') {
						tokens.add(new Token(Kind.OP_POWER, startPos, pos - startPos + 1));
						state = State.START;
						pos++;
					}
					else {
						tokens.add(new Token(Kind.OP_TIMES, startPos, pos - startPos));
						state = State.START;
					}
				}
				break;
				case START_ZERO: {
					if (ch == '.') {
						state = State.IN_FLOAT;
						pos++;
					}
					else {
						tokens.add(new Token(Kind.INTEGER_LITERAL, startPos, pos - startPos));
						state = State.START;
					}
				}
				break;
				case IN_EQ: {
					if (ch == '=') {
						tokens.add(new Token(Kind.OP_EQ, startPos, pos - startPos + 1));
						state = State.START;
						pos++;
					}
					else {
						tokens.add(new Token(Kind.OP_ASSIGN, startPos, pos - startPos));
						state = State.START;
					}
				}
				break;
				case IN_NEQ: {
					if (ch == '=') {
						tokens.add(new Token(Kind.OP_NEQ, startPos, pos - startPos + 1));
						state = State.START;
						pos++;
					}
					else {
						tokens.add(new Token(Kind.OP_EXCLAMATION, startPos, pos - startPos));
						state = State.START;
					}
				}
				break;
				case IN_GE: {
					if (ch == '=') {
						tokens.add(new Token(Kind.OP_GE, startPos, pos - startPos + 1));
						state = State.START;
						pos++;
					}
					else {
						tokens.add(new Token(Kind.OP_GT, startPos, pos - startPos));
						state = State.START;
					}
				}
				break;
				case IN_LE: {
					if (ch == '=') {
						tokens.add(new Token(Kind.OP_LE, startPos, pos - startPos + 1));
						state = State.START;
						pos++;
					}
					else {
						tokens.add(new Token(Kind.OP_LT, startPos, pos - startPos));
						state = State.START;
					}
				}
				break;
				default: {
					error(pos, 0, 0, "undefined state");
				}
			}// switch state
		} // while
			
		return this;
	}
	
	private void error(int pos, int line, int posInLine, String message) throws LexicalException {
		String m = (line + 1) + ":" + (posInLine + 1) + " " + message;
		throw new LexicalException(m, pos);
	}

	/**
	 * Returns true if the internal iterator has more Tokens
	 * 
	 * @return
	 */
	public boolean hasTokens() {
		return nextTokenPos < tokens.size();
	}

	/**
	 * Returns the next Token and updates the internal iterator so that the next
	 * call to nextToken will return the next token in the list.
	 * 
	 * It is the callers responsibility to ensure that there is another Token.
	 * 
	 * Precondition: hasTokens()
	 * 
	 * @return
	 */
	public Token nextToken() {
		return tokens.get(nextTokenPos++);
	}

	/**
	 * Returns the next Token, but does not update the internal iterator. This means
	 * that the next call to nextToken or peek will return the same Token as
	 * returned by this methods.
	 * 
	 * It is the callers responsibility to ensure that there is another Token.
	 * 
	 * Precondition: hasTokens()
	 * 
	 * @return next Token.
	 */
	public Token peek() {
		return tokens.get(nextTokenPos);
	}

	/**
	 * Resets the internal iterator so that the next call to peek or nextToken will
	 * return the first Token.
	 */
	public void reset() {
		nextTokenPos = 0;
	}

	/**
	 * Returns a String representation of the list of Tokens and line starts
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Tokens:\n");
		for (int i = 0; i < tokens.size(); i++) {
			sb.append(tokens.get(i)).append('\n');
		}
		sb.append("Line starts:\n");
		for (int i = 0; i < lineStarts.length; i++) {
			sb.append(i).append(' ').append(lineStarts[i]).append('\n');
		}
		return sb.toString();
	}

}
