/**
 * Name: Yihao Wu
 * Assignment Number: Project#6
 * Date Due: November 20, 2018
 */

package cop5556fa18;

import cop5556fa18.PLPAST.*;
import cop5556fa18.PLPScanner.Token;
import cop5556fa18.PLPScanner.Kind;

import java.util.ArrayList;
import java.util.List;

public class PLPParser {
	
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}
	}
	
	PLPScanner scanner;
	Token t;

	PLPParser(PLPScanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}
	
	public Program parse() throws SyntaxException {
		Program p = program();
		matchEOF();
		return p;
	}
	
	/*
	 * Program -> Identifier Block
	 */
	public Program program() throws SyntaxException {
		Token firstToken = t;
		match(Kind.IDENTIFIER);
		String name = new String(scanner.chars, firstToken.pos, firstToken.length);
		Block block = block();
		Program p = new Program(firstToken, name, block);
		return p;
	}
	
	Kind[] firstDec = { Kind.KW_int, Kind.KW_boolean, Kind.KW_float, Kind.KW_char, Kind.KW_string /* Complete this */ };
	Kind[] firstStatement = {Kind.KW_if, Kind.KW_while, Kind.KW_print, Kind.KW_sleep, Kind.IDENTIFIER /* Complete this */  };
	Kind[] functionName = { Kind.KW_sin, Kind.KW_cos, Kind.KW_atan, Kind.KW_abs, Kind.KW_log, Kind.KW_int, Kind.KW_float };

	/*
	 * Block ->  { (  (Declaration | Statement) ; )* }
	 */
	public Block block() throws SyntaxException {
		
		match(Kind.LBRACE);
		List<PLPASTNode> declarationsAndStatements  = new ArrayList<PLPASTNode>();
		Token firstToken = t;
		while (checkKind(firstDec) | checkKind(firstStatement)) {
			if (checkKind(firstDec)) {
				Declaration declaration = declaration();
				declarationsAndStatements.add(declaration);
			}
			else if (checkKind(firstStatement)) {
				Statement statement = statement();
				declarationsAndStatements.add(statement);
			}
			match(Kind.SEMI);
		}
		match(Kind.RBRACE);
		
		Block block = new Block(firstToken, declarationsAndStatements);
		
		return block;
	}
	
	/*
	 * Declaration → Type Identifier ( = Expression | ε ) | Type IDENTIFIERLIST
	 */
	public Declaration declaration() throws SyntaxException {
		Token firstToken = t;
		Kind type = t.kind;
		consume();
		Token identifier = t;
		match(Kind.IDENTIFIER);
		String name = new String(scanner.chars, identifier.pos, identifier.length);
		
		if (checkKind(Kind.OP_ASSIGN)) {
			consume();
			Expression expression = expression();
			Declaration declaration = new VariableDeclaration(firstToken, type, name, expression);
			return declaration;
		}
		else if (checkKind(Kind.SEMI)) {
			Declaration declaration = new VariableDeclaration(firstToken, type, name, null);
			return declaration;
		}
		List<String> names = new ArrayList<String>();
		names.add(name);
		while (checkKind(Kind.COMMA)) {
			consume();
			identifier = t;
			match(Kind.IDENTIFIER);
			name = new String(scanner.chars, identifier.pos, identifier.length);
			names.add(name);
		}
		Declaration declaration = new VariableListDeclaration(firstToken, type, names);
		
		return declaration;
		//throw new UnsupportedOperationException();
	}
	
	/*
	 * Expression → OrExpression ? Expression : Expression | OrExpression
	 */
	public Expression expression() throws SyntaxException {
		Token firstToken = t;
		Expression condition = orExpression();
		if(checkKind(Kind.OP_QUESTION)) {
			consume();
			Expression trueExpression = expression();
			match(Kind.OP_COLON);
			Expression falseExpression = expression();
			ExpressionConditional expression = new ExpressionConditional(firstToken, condition, trueExpression, falseExpression);
			return expression;
		}
		return condition;
		//throw new UnsupportedOperationException();
	}
	
	/*
	 * OrExpression → AndExpression ( | AndExpression )*
	 */
	public Expression orExpression() throws SyntaxException {
		Token firstToken = t;
		Expression leftExpression = andExpression();
		while (checkKind(Kind.OP_OR)) {
			Kind op = t.kind;
			consume();
			Expression rightExpression = andExpression();
			leftExpression = new ExpressionBinary(firstToken, leftExpression, op, rightExpression);
		}
		return leftExpression;
	}
	
	/*
	 * AndExpression → EqExpression ( & EqExpression )*
	 */
	public Expression andExpression() throws SyntaxException {
		Token firstToken = t;
		Expression leftExpression = eqExpression();
		while (checkKind(Kind.OP_AND)) {
			Kind op = t.kind;
			consume();
			Expression rightExpression = eqExpression();
			leftExpression = new ExpressionBinary(firstToken, leftExpression, op, rightExpression);
		}
		return leftExpression;
	}
	
	/*
	 * EqExpression → RelExpression ( ( == | != ) RelExpression )*
	 */
	public Expression eqExpression() throws SyntaxException {
		Token firstToken = t;
		Expression leftExpression = relExpression();
		while (checkKind(Kind.OP_EQ) || checkKind(Kind.OP_NEQ)) {
			Kind op = t.kind;
			consume();
			Expression rightExpression = relExpression();
			leftExpression = new ExpressionBinary(firstToken, leftExpression, op, rightExpression);
		}
		return leftExpression;
	}
	
	/*
	 * RelExpression → AddExpression ( ( < | > | <= | >= ) AddExpression )*
	 */
	public Expression relExpression() throws SyntaxException {
		Token firstToken = t;
		Expression leftExpression = addExpression();
		while (checkKind(Kind.OP_GE) || checkKind(Kind.OP_GT) || checkKind(Kind.OP_LE) || checkKind(Kind.OP_LT)) {
			Kind op = t.kind;
			consume();
			Expression rightExpression = addExpression();
			leftExpression = new ExpressionBinary(firstToken, leftExpression, op, rightExpression);
		}
		return leftExpression;
	}
	
	/*
	 * AddExpression → MultExpression ( ( + | - ) MultExpression )*
	 */
	public Expression addExpression() throws SyntaxException {
		Token firstToken = t;
		Expression leftExpression = multExpression();
		while (checkKind(Kind.OP_PLUS) || checkKind(Kind.OP_MINUS)) {
			Kind op = t.kind;
			consume();
			Expression rightExpression = multExpression();
			leftExpression = new ExpressionBinary(firstToken, leftExpression, op, rightExpression);
		}
		return leftExpression;
	}
	
	/*
	 * MultExpression → PowerExpression ( ( * | / | % ) PowerExpression )*
	 */
	public Expression multExpression() throws SyntaxException {
		Token firstToken = t;
		Expression leftExpression = powerExpression();
		while (checkKind(Kind.OP_MOD) || checkKind(Kind.OP_TIMES) || checkKind(Kind.OP_DIV)) {
			Kind op = t.kind;
			consume();
			Expression rightExpression = powerExpression();
			leftExpression = new ExpressionBinary(firstToken, leftExpression, op, rightExpression);
		}
		return leftExpression;
	}
	
	/*
	 * PowerExpression → UnaryExpression ( ** PowerExpression |  ε )
	 */
	public Expression powerExpression() throws SyntaxException {
		Token firstToken = t;
		Expression leftExpression = unaryExpression();
		if (checkKind(Kind.OP_POWER)) {
			Kind op = t.kind;
			consume();
			Expression rightExpression = powerExpression();
			leftExpression = new ExpressionBinary(firstToken, leftExpression, op, rightExpression);
		}
		return leftExpression;
	}
	
	/*
	 * UnaryExpression → + UnaryExpression | - UnaryExpression | ! UnaryExpression | Primary
	 */
	public Expression unaryExpression() throws SyntaxException {
		Token firstToken = t;
		if (checkKind(Kind.OP_PLUS) || checkKind(Kind.OP_MINUS) || checkKind(Kind.OP_EXCLAMATION)) {
			Kind op = t.kind;
			consume();
			Expression expression = unaryExpression();
			Expression unaryExpression = new ExpressionUnary(firstToken, op, expression);
			return unaryExpression;
		}
		else {
			Expression expression = primary();
			return expression;
		}
		
	}
	
	/*
	 * Primary → INTEGER_LITERAL | BOOLEAN_LITERAL | FLOAT_LITERAL | CHAR_LITERAL 
	 * | STRING_LITERAL | ( Expression ) | IDENTIFIER | Function
	 */
	public Expression primary() throws SyntaxException {
		Token firstToken = t;
		Expression primary = null;
		if (checkKind(Kind.IDENTIFIER)) {
			String name = new String(scanner.chars, t.pos, t.length);
			primary = new ExpressionIdentifier(firstToken, name);
			consume();
		}
		else if (checkKind(Kind.INTEGER_LITERAL)) {
			String integer_literal = new String(scanner.chars, t.pos, t.length);
			int value = Integer.parseInt(integer_literal);
			primary = new ExpressionIntegerLiteral(firstToken, value);
			consume();
		}
		else if (checkKind(Kind.BOOLEAN_LITERAL)) {
			boolean value = false;
			String boolean_literal = new String(scanner.chars, t.pos, t.length);
			if (boolean_literal.equals("true")) {
				value = true;
			}
			primary = new ExpressionBooleanLiteral(firstToken, value);
			consume();
		}
		else if (checkKind(Kind.FLOAT_LITERAL)) {
			String float_literal = new String(scanner.chars, t.pos, t.length);
			float value = Float.parseFloat(float_literal);
			primary = new ExpressionFloatLiteral(firstToken, value);
			consume();
		}
		else if (checkKind(Kind.CHAR_LITERAL)) {
			char text = scanner.chars[t.pos + 1];
			primary = new ExpressionCharLiteral(firstToken, text);
			consume();
		}
		else if (checkKind(Kind.STRING_LITERAL)) {
			String text = new String(scanner.chars, t.pos + 1, t.length - 2);
			primary = new ExpressionStringLiteral(firstToken, text);
			consume();
		}
		else if (checkKind(Kind.LPAREN)) {
			consume();
			primary = expression();
			match(Kind.RPAREN);
		}
		else {
			primary = function();
		}
		return primary;
	}
	
	/*
	 * Function → FunctionName ( Expression )
	 */
	public Expression function() throws SyntaxException {
		Token firstToken = t;
		Kind name = null;
		if (checkKind(functionName)) {
			name = t.kind;
			consume();
		}
		else {
			throw new SyntaxException(t, "Syntax Error: Expect a \"sin\", \"cos\", \"atan\","
					+ " \"abs\", \"log\", \"int\" or \"float\" as a function name but not match");
		}
		match(Kind.LPAREN);
		Expression expression = expression();
		match(Kind.RPAREN);
		Expression function = new FunctionWithArg(firstToken, name, expression);
		return function;
	}
	
	/*
	 * Statement → IfStatement | AssignmentStatement | SleepStatement 
	 * | PrintStatement | WhileStatment
	 */
	public Statement statement() throws SyntaxException {
		
		Token firstToken = t;
		if (checkKind(Kind.KW_if)) {
			consume();
			match(Kind.LPAREN);
			Expression expression = expression();
			match(Kind.RPAREN);
			Block block = block();
			IfStatement ifStatement = new IfStatement(firstToken, expression, block);
			return ifStatement;
		}
		else if (checkKind(Kind.KW_while)) {
			consume();
			match(Kind.LPAREN);
			Expression expression = expression();
			match(Kind.RPAREN);
			Block block = block();
			WhileStatement whileStatement = new WhileStatement(firstToken, expression, block);
			return whileStatement;
		}
		else if (checkKind(Kind.IDENTIFIER)) {
			String identifier = new String(scanner.chars, t.pos, t.length);
			LHS lhs = new LHS(firstToken, identifier);
			consume();
			match(Kind.OP_ASSIGN);
			Expression expression = expression();
			AssignmentStatement assignmentStatement = new AssignmentStatement(firstToken, lhs, expression);
			return assignmentStatement;
		}
		else if (checkKind(Kind.KW_sleep)) {
			consume();
			Expression expression = expression();
			SleepStatement sleepStatement = new SleepStatement(firstToken, expression);
			return sleepStatement;
		}
		else if (checkKind(Kind.KW_print)) {
			consume();
			Expression expression = expression();
			PrintStatement printStatement = new PrintStatement(firstToken, expression);
			return printStatement;
		}
		else {
			throw new SyntaxException(t,"Syntax Error: Expect \"if\", \"while\", \"sleep\","
					+ " \"print\" or a identifier as first of statement, but not match");
		}
	}

	protected boolean checkKind(Kind kind) {
		return t.kind == kind;
	}

	protected boolean checkKind(Kind... kinds) {
		for (Kind k : kinds) {
			if (k == t.kind)
				return true;
		}
		return false;
	}
	
	private Token matchEOF() throws SyntaxException {
		if (checkKind(Kind.EOF)) {
			return t;
		}
		throw new SyntaxException(t,"Syntax Error: Expect EOF but not match!"); //TODO  give a better error message!
	}
	
	/**
	 * @param kind
	 * @return 
	 * @return 
	 * @return
	 * @throws SyntaxException
	 */
	private void match(Kind kind) throws SyntaxException {
		if (checkKind(kind)) {
			t = scanner.nextToken();
			return;
		}
		throw new SyntaxException(t,"Syntax Error: Token doesn't match!");
	}
	
	private void consume() {
		t = scanner.nextToken();
	}

}
