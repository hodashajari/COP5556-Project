/**
 * Name: Yihao Wu
 * Assignment Number: Project#6
 * Date Due: November 20, 2018
 */

package cop5556fa18;

import cop5556fa18.PLPAST.*;
import cop5556fa18.PLPScanner.Kind;
import cop5556fa18.PLPScanner.Token;
import cop5556fa18.PLPTypes.Type;

public class PLPTypeChecker implements PLPASTVisitor {
	
	private PLPSymbolTable symbolTable;
	
	PLPTypeChecker() {
		symbolTable = new PLPSymbolTable();
	}
	
	@SuppressWarnings("serial")
	public static class SemanticException extends Exception {
		Token t;

		public SemanticException(Token t, String message) {
			super(message);
			this.t = t;
		}
	}

	// Name is only used for naming the output file. 
	// Visit the child block to type check program.
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		program.block.visit(this, arg);
		return null;
	}
		
	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		symbolTable.enterScope();
		for (PLPASTNode declarationsAndStatement : block.declarationsAndStatements) {
			// System.out.println(1);
			declarationsAndStatement.visit(this, arg);
		}
		symbolTable.closeScope();
		return null;
	}

	@Override
	public Object visitVariableDeclaration(VariableDeclaration declaration, Object arg) throws Exception {
		
		if (declaration.expression != null) {
			declaration.expression.visit(this, arg);
			if (declaration.type != declaration.expression.type) {
				throw new SemanticException(declaration.firstToken, "SemanticException: Type of expression doesn't match type of identifier in declaration");
			}
		}
		if (!symbolTable.insert(declaration.name, declaration)) {
			throw new SemanticException(declaration.firstToken, "SemanticException: Repeat declaration!");
		}
		
		return null;
	}

	@Override
	public Object visitVariableListDeclaration(VariableListDeclaration declaration, Object arg) throws Exception {
		
		for (String name : declaration.names) {
			if (!symbolTable.insert(name, declaration)) {
				throw new SemanticException(declaration.firstToken, "SemanticException: Repeat declaration!");
			}
		}
		return null;
	}

	@Override
	public Object visitExpressionBooleanLiteral(ExpressionBooleanLiteral expressionBooleanLiteral, Object arg) throws Exception {
		
		expressionBooleanLiteral.type = Type.BOOLEAN;
		return null;
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws Exception {
		expressionBinary.leftExpression.visit(this, arg);
		expressionBinary.rightExpression.visit(this, arg);
		if (expressionBinary.op == Kind.OP_EQ || expressionBinary.op == Kind.OP_NEQ || expressionBinary.op == Kind.OP_GT || 
				expressionBinary.op == Kind.OP_GE || expressionBinary.op == Kind.OP_LT || expressionBinary.op == Kind.OP_LE) {
			
			if ((expressionBinary.leftExpression.type == Type.INTEGER && expressionBinary.rightExpression.type == 
					Type.INTEGER) || (expressionBinary.leftExpression.type == Type.FLOAT && 
					expressionBinary.rightExpression.type == Type.FLOAT) || (expressionBinary.leftExpression.type == 
					Type.BOOLEAN && expressionBinary.rightExpression.type == Type.BOOLEAN)) {
				
				expressionBinary.type = Type.BOOLEAN;
			}
			else {
				throw new SemanticException(expressionBinary.firstToken, "SemanticException: Type of leftExpression isn't "
						+ "compatible with type of right Expression, or using unsupported operation!");
			}
		}
		else if (expressionBinary.op == Kind.OP_AND || expressionBinary.op == Kind.OP_OR) {
			if (expressionBinary.leftExpression.type == Type.BOOLEAN && 
					expressionBinary.rightExpression.type == Type.BOOLEAN) {
				
				expressionBinary.type = Type.BOOLEAN;
			}
			else if (expressionBinary.leftExpression.type == Type.INTEGER && 
					expressionBinary.rightExpression.type == Type.INTEGER) {
				
				expressionBinary.type = Type.INTEGER;
			}
			else {
				throw new SemanticException(expressionBinary.firstToken, "SemanticException: Type of leftExpression isn't "
						+ "compatible with type of right Expression, or using unsupported operation!");
			}
		}
		else if (expressionBinary.op == Kind.OP_MINUS || expressionBinary.op == Kind.OP_TIMES || 
				expressionBinary.op == Kind.OP_DIV || expressionBinary.op == Kind.OP_POWER) {
			
			if (expressionBinary.leftExpression.type == Type.INTEGER && expressionBinary.rightExpression.type == Type.INTEGER) {
				expressionBinary.type = Type.INTEGER;
			}
			else if (expressionBinary.leftExpression.type == Type.FLOAT || expressionBinary.rightExpression.type == Type.FLOAT) {
				expressionBinary.type = Type.FLOAT;
			}
			else {
				throw new SemanticException(expressionBinary.firstToken, "SemanticException: Type of leftExpression isn't "
						+ "compatible with type of right Expression, or using unsupported operation!");
			}
		}
		else if (expressionBinary.op == Kind.OP_PLUS) {
			if (expressionBinary.leftExpression.type == Type.INTEGER && expressionBinary.rightExpression.type == Type.INTEGER) {
				expressionBinary.type = Type.INTEGER;
			}
			else if (expressionBinary.leftExpression.type == Type.STRING && expressionBinary.rightExpression.type == Type.STRING) {
				expressionBinary.type = Type.STRING;
			}
			else if (expressionBinary.leftExpression.type == Type.FLOAT || expressionBinary.rightExpression.type == Type.FLOAT) {
				expressionBinary.type = Type.FLOAT;
			}
			else {
				throw new SemanticException(expressionBinary.firstToken, "SemanticException: Type of leftExpression isn't "
						+ "compatible with type of right Expression, or using unsupported operation!");
			}
		}
		else if (expressionBinary.op == Kind.OP_MOD) {
			if (expressionBinary.leftExpression.type == Type.INTEGER && expressionBinary.rightExpression.type == Type.INTEGER) {
				expressionBinary.type = Type.INTEGER;
			}
			else {
				throw new SemanticException(expressionBinary.firstToken, "SemanticException: Type of leftExpression isn't "
						+ "compatible with type of right Expression, or using unsupported operation!");
			}
		}
		else {
			throw new SemanticException(expressionBinary.firstToken, "SemanticException: Invalid operation!");
		}
		
		return null;
	}

	@Override
	public Object visitExpressionConditional(ExpressionConditional expressionConditional, Object arg) throws Exception {
		expressionConditional.condition.visit(this, arg);
		expressionConditional.trueExpression.visit(this, arg);
		expressionConditional.falseExpression.visit(this, arg);
		if (expressionConditional.condition.type != PLPTypes.Type.BOOLEAN) {
			throw new SemanticException(expressionConditional.firstToken, "SemanticException: Type of expression as condition "
					+ "of ExpressionConditional must be boolean!");
		}
		else if (expressionConditional.trueExpression.type != expressionConditional.falseExpression.type) {
			throw new SemanticException(expressionConditional.firstToken, "SemanticException: Type of trueExpression and falseExpression "
					+ "of ExpressionConditional must be equal!");
		}
		else {
			expressionConditional.type = expressionConditional.trueExpression.type;
		}
		return null;
	}

	@Override
	public Object visitExpressionFloatLiteral(ExpressionFloatLiteral expressionFloatLiteral, Object arg)
			throws Exception {
		
		expressionFloatLiteral.type = PLPTypes.Type.FLOAT;
		return null;
	}

	@Override
	public Object visitFunctionWithArg(FunctionWithArg FunctionWithArg, Object arg) throws Exception {
		FunctionWithArg.expression.visit(this, arg);
		if (FunctionWithArg.functionName == Kind.KW_abs) {
			if (FunctionWithArg.expression.type == PLPTypes.Type.INTEGER) {
				FunctionWithArg.type = PLPTypes.Type.INTEGER;
			}
			else if (FunctionWithArg.expression.type == PLPTypes.Type.FLOAT) {
				FunctionWithArg.type = PLPTypes.Type.FLOAT;
			}
			else {
				throw new SemanticException(FunctionWithArg.firstToken, "SemanticException: Type of parameters aren't "
						+ "compatible with type requirement of this function!");
			}
		}
		else if (FunctionWithArg.functionName == Kind.KW_sin || FunctionWithArg.functionName == Kind.KW_cos || 
				FunctionWithArg.functionName == Kind.KW_atan || FunctionWithArg.functionName == Kind.KW_log) {
			
			if (FunctionWithArg.expression.type == PLPTypes.Type.FLOAT) {
				FunctionWithArg.type = PLPTypes.Type.FLOAT;
			}
			else {
				throw new SemanticException(FunctionWithArg.firstToken, "SemanticException: Type of parameters aren't "
						+ "compatible with type requirement of this function!");
			}
		}
		else if (FunctionWithArg.functionName == Kind.KW_float) {
			if (FunctionWithArg.expression.type == PLPTypes.Type.FLOAT || FunctionWithArg.expression.type == PLPTypes.Type.INTEGER) {
				FunctionWithArg.type = PLPTypes.Type.FLOAT;
			}
			else {
				throw new SemanticException(FunctionWithArg.firstToken, "SemanticException: Type of parameters aren't "
						+ "compatible with type requirement of this function!");
			}
		}
		else if (FunctionWithArg.functionName == Kind.KW_int) {
			if (FunctionWithArg.expression.type == PLPTypes.Type.FLOAT || FunctionWithArg.expression.type == PLPTypes.Type.INTEGER) {
				FunctionWithArg.type = PLPTypes.Type.INTEGER;
			}
			else {
				throw new SemanticException(FunctionWithArg.firstToken, "SemanticException: Type of parameters aren't "
						+ "compatible with type requirement of this function!");
			}
		}
		else {
			throw new SemanticException(FunctionWithArg.firstToken, "SemanticException: Invalid function name!");
		}
		
		return null;
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdentifier expressionIdent, Object arg) throws Exception {
		
		expressionIdent.dec = symbolTable.lookup(expressionIdent.name);
		if (expressionIdent.dec == null) {
			throw new SemanticException(expressionIdent.firstToken, "SemanticException: Identifier isn't declared");
		}
		else {
			expressionIdent.type = expressionIdent.dec.type;
		}
		return null;
	}

	@Override
	public Object visitExpressionIntegerLiteral(ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
			throws Exception {
		
		expressionIntegerLiteral.type = PLPTypes.Type.INTEGER;
		return null;
	}

	@Override
	public Object visitExpressionStringLiteral(ExpressionStringLiteral expressionStringLiteral, Object arg)
			throws Exception {
		
		expressionStringLiteral.type = PLPTypes.Type.STRING;
		return null;
	}

	@Override
	public Object visitExpressionCharLiteral(ExpressionCharLiteral expressionCharLiteral, Object arg) throws Exception {
		
		expressionCharLiteral.type = PLPTypes.Type.CHAR;
		return null;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws Exception {
		
		statementAssign.lhs.visit(this, arg);
		statementAssign.expression.visit(this, arg);
		if (statementAssign.lhs.type != statementAssign.expression.type) {
			throw new SemanticException(statementAssign.firstToken, "SemanticException: Type of expresssion as are not equal to type of LHS!");
		}
		return null;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		ifStatement.condition.visit(this, arg);
		ifStatement.block.visit(this, arg);
		if (ifStatement.condition.type != PLPTypes.Type.BOOLEAN) {
			throw new SemanticException(ifStatement.firstToken, "SemanticException: Type of expresssion as condition of IfStatement must be boolean!");
		}
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		whileStatement.condition.visit(this, arg);
		if (whileStatement.condition.type != PLPTypes.Type.BOOLEAN) {
			throw new SemanticException(whileStatement.firstToken, "SemanticException: Type of expresssion as condition of WhileStatement must be boolean!");
		}
		whileStatement.b.visit(this, arg);
		return null;
	}

	@Override
	public Object visitPrintStatement(PrintStatement printStatement, Object arg) throws Exception {
		printStatement.expression.visit(this, arg);
		if (printStatement.expression.type != PLPTypes.Type.BOOLEAN && printStatement.expression.type != PLPTypes.Type.INTEGER && 
				printStatement.expression.type != PLPTypes.Type.FLOAT && printStatement.expression.type != PLPTypes.Type.CHAR && 
				printStatement.expression.type != PLPTypes.Type.STRING) {
			throw new SemanticException(printStatement.firstToken, "SemanticException: Type of expresssion of PrintStatement must "
					+ "be boolean, int, float, char or string!");
		}
		return null;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		sleepStatement.time.visit(this, arg);
		if (sleepStatement.time.type != PLPTypes.Type.INTEGER) {
			throw new SemanticException(sleepStatement.firstToken, "SemanticException: Type of expresssion of SleepStatement must be int!");
		}
		return null;
	}

	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary, Object arg) throws Exception {
		expressionUnary.expression.visit(this, arg);
		if (expressionUnary.op == Kind.OP_EXCLAMATION && expressionUnary.expression.type != PLPTypes.Type.INTEGER && 
				expressionUnary.expression.type != PLPTypes.Type.BOOLEAN) {
			
			throw new SemanticException(expressionUnary.firstToken, "SemanticException: ! can be used only if Expression.type is integer or Boolean!");
		}
		else if ((expressionUnary.op == Kind.OP_PLUS || expressionUnary.op == Kind.OP_MINUS) && 
				expressionUnary.expression.type != PLPTypes.Type.INTEGER && expressionUnary.expression.type != PLPTypes.Type.FLOAT) {
			
			throw new SemanticException(expressionUnary.firstToken, "SemanticException: +, -  can be used only if Expression.type is integer or float!");
		}
		else {
			expressionUnary.type = expressionUnary.expression.type;
		}
		return null;
	}

	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		
		lhs.declaration = symbolTable.lookup(lhs.identifier);
		if (lhs.declaration == null) {
			throw new SemanticException(lhs.firstToken, "SemanticException: Identifier isn't declared!");
		}
		lhs.type = lhs.declaration.type;
		return null;
	}

}
