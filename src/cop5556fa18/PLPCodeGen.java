/**
 * Name: Yihao Wu
 * Assignment Number: Project#6
 * Date Due: November 20, 2018
 */

package cop5556fa18;

import cop5556fa18.PLPAST.AssignmentStatement;
import cop5556fa18.PLPAST.Block;
import cop5556fa18.PLPAST.ExpressionBinary;
import cop5556fa18.PLPAST.ExpressionBooleanLiteral;
import cop5556fa18.PLPAST.ExpressionCharLiteral;
import cop5556fa18.PLPAST.ExpressionConditional;
import cop5556fa18.PLPAST.ExpressionFloatLiteral;
import cop5556fa18.PLPAST.ExpressionIdentifier;
import cop5556fa18.PLPAST.ExpressionIntegerLiteral;
import cop5556fa18.PLPAST.ExpressionStringLiteral;
import cop5556fa18.PLPAST.ExpressionUnary;
import cop5556fa18.PLPAST.FunctionWithArg;
import cop5556fa18.PLPAST.IfStatement;
import cop5556fa18.PLPAST.LHS;
import cop5556fa18.PLPAST.PLPASTNode;
import cop5556fa18.PLPAST.PLPASTVisitor;
import cop5556fa18.PLPAST.PrintStatement;
import cop5556fa18.PLPAST.Program;
import cop5556fa18.PLPAST.SleepStatement;
import cop5556fa18.PLPAST.VariableDeclaration;
import cop5556fa18.PLPAST.VariableListDeclaration;
import cop5556fa18.PLPAST.WhileStatement;
import cop5556fa18.PLPScanner.Kind;
import cop5556fa18.PLPTypes.Type;

import org.objectweb.asm.Opcodes;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public class PLPCodeGen implements PLPASTVisitor, Opcodes {
	
	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;
	
	private int slotNum;
	
	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	public PLPCodeGen(String sourceFileName, boolean dEVEL, boolean gRADE) {
		super();
		this.sourceFileName = sourceFileName;
		DEVEL = dEVEL;
		GRADE = gRADE;
		slotNum = 0;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		for (PLPASTNode node : block.declarationsAndStatements) {
			node.visit(this, null);
		}
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		// cw = new ClassWriter(0); 
		// If the call to mv.visitMaxs(1, 1) crashes, it is sometimes helpful 
		// to temporarily run it without COMPUTE_FRAMES. You probably won't 
		// get a completely correct classfile, but you will be able to see the 
		// code that was generated.
		
		className = program.name;
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", null);
		cw.visitSource(sourceFileName, null);
		
		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();
		
		// add label before first instruction
		Label mainStart = new Label();
		mv.visitLabel(mainStart);

		PLPCodeGenUtils.genLog(DEVEL, mv, "entering main");
		
		program.block.visit(this, arg);

		// generates code to add string to log
		PLPCodeGenUtils.genLog(DEVEL, mv, "leaving main");
		
		// adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);

		// adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		
		// Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the
		// constructor, asm will calculate this itself and the parameters are ignored.
		// If you have trouble with failures in this routine, it may be useful
		// to temporarily change the parameter in the ClassWriter constructor
		// from COMPUTE_FRAMES to 0.
		// The generated classfile will not be correct, but you will at least be
		// able to see what is in it.
		mv.visitMaxs(0, 0);

		// terminate construction of main method
		mv.visitEnd();

		// terminate class construction
		cw.visitEnd();

		// generate classfile as byte array and return
		return cw.toByteArray();			
	}

	@Override
	public Object visitVariableDeclaration(VariableDeclaration declaration, Object arg) throws Exception {
		declaration.slotNum = slotNum++;
		if (declaration.expression != null) {
			declaration.expression.visit(this, null);
			switch(declaration.type) {
			case INTEGER: {
				mv.visitVarInsn(ISTORE, declaration.slotNum);
			}
			break;
			case FLOAT: {
				mv.visitVarInsn(FSTORE, declaration.slotNum);
			}
			break;
			case BOOLEAN: {
				mv.visitVarInsn(ISTORE, declaration.slotNum);
			}
			break;
			case CHAR: {
				mv.visitVarInsn(ISTORE, declaration.slotNum);
			}
			break;
			case STRING: {
				mv.visitVarInsn(ASTORE, declaration.slotNum);
			}
			break;
			default: {
				throw new UnsupportedOperationException();
			}
			}
		}
		
		return null;
	}

	@Override
	public Object visitVariableListDeclaration(VariableListDeclaration declaration, Object arg) throws Exception {
		declaration.slotNum = slotNum;
		slotNum += declaration.names.size();
		return null;
	}

	@Override
	public Object visitExpressionBooleanLiteral(ExpressionBooleanLiteral expressionBooleanLiteral, Object arg)
			throws Exception {
		mv.visitLdcInsn(expressionBooleanLiteral.value);
		return null;
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws Exception {
		expressionBinary.leftExpression.visit(this, null);
		expressionBinary.rightExpression.visit(this, null);
		Label trueLabel = new Label();
		Label endLabel = new Label();
		if (expressionBinary.op == Kind.OP_OR) {
			mv.visitInsn(IOR);
		}
		else if (expressionBinary.op == Kind.OP_AND) {
			mv.visitInsn(IAND);
		}
		else if (expressionBinary.op == Kind.OP_EQ) {
			if (expressionBinary.leftExpression.type == Type.INTEGER || expressionBinary.leftExpression.type == Type.BOOLEAN) {
				mv.visitJumpInsn(IF_ICMPEQ, trueLabel);
				mv.visitLdcInsn(false);
				mv.visitJumpInsn(GOTO, endLabel);
				mv.visitLabel(trueLabel);
				mv.visitLdcInsn(true);
				mv.visitLabel(endLabel);
			}
			else {
				mv.visitInsn(FCMPL);
				mv.visitJumpInsn(IFEQ, trueLabel);
				mv.visitLdcInsn(false);
				mv.visitJumpInsn(GOTO, endLabel);
				mv.visitLabel(trueLabel);
				mv.visitLdcInsn(true);
				mv.visitLabel(endLabel);
			}
		}
		else if (expressionBinary.op == Kind.OP_NEQ) {
			if (expressionBinary.leftExpression.type == Type.INTEGER || expressionBinary.leftExpression.type == Type.BOOLEAN) {
				mv.visitJumpInsn(IF_ICMPNE, trueLabel);
				mv.visitLdcInsn(false);
				mv.visitJumpInsn(GOTO, endLabel);
				mv.visitLabel(trueLabel);
				mv.visitLdcInsn(true);
				mv.visitLabel(endLabel);
			}
			else {
				mv.visitInsn(FCMPL);
				mv.visitJumpInsn(IFNE, trueLabel);
				mv.visitLdcInsn(false);
				mv.visitJumpInsn(GOTO, endLabel);
				mv.visitLabel(trueLabel);
				mv.visitLdcInsn(true);
				mv.visitLabel(endLabel);
			}
		}
		else if (expressionBinary.op == Kind.OP_LT) {
			if (expressionBinary.leftExpression.type == Type.INTEGER || expressionBinary.leftExpression.type == Type.BOOLEAN) {
				mv.visitJumpInsn(IF_ICMPLT, trueLabel);
				mv.visitLdcInsn(false);
				mv.visitJumpInsn(GOTO, endLabel);
				mv.visitLabel(trueLabel);
				mv.visitLdcInsn(true);
				mv.visitLabel(endLabel);
			}
			else {
				mv.visitInsn(FCMPL);
				mv.visitJumpInsn(IFLT, trueLabel);
				mv.visitLdcInsn(false);
				mv.visitJumpInsn(GOTO, endLabel);
				mv.visitLabel(trueLabel);
				mv.visitLdcInsn(true);
				mv.visitLabel(endLabel);
			}
		}
		else if (expressionBinary.op == Kind.OP_GT) {
			if (expressionBinary.leftExpression.type == Type.INTEGER || expressionBinary.leftExpression.type == Type.BOOLEAN) {
				mv.visitJumpInsn(IF_ICMPGT, trueLabel);
				mv.visitLdcInsn(false);
				mv.visitJumpInsn(GOTO, endLabel);
				mv.visitLabel(trueLabel);
				mv.visitLdcInsn(true);
				mv.visitLabel(endLabel);
			}
			else {
				mv.visitInsn(FCMPL);
				mv.visitJumpInsn(IFGT, trueLabel);
				mv.visitLdcInsn(false);
				mv.visitJumpInsn(GOTO, endLabel);
				mv.visitLabel(trueLabel);
				mv.visitLdcInsn(true);
				mv.visitLabel(endLabel);
			}
		}
		else if (expressionBinary.op == Kind.OP_LE) {
			if (expressionBinary.leftExpression.type == Type.INTEGER || expressionBinary.leftExpression.type == Type.BOOLEAN) {
				mv.visitJumpInsn(IF_ICMPLE, trueLabel);
				mv.visitLdcInsn(false);
				mv.visitJumpInsn(GOTO, endLabel);
				mv.visitLabel(trueLabel);
				mv.visitLdcInsn(true);
				mv.visitLabel(endLabel);
			}
			else {
				mv.visitInsn(FCMPL);
				mv.visitJumpInsn(IFLE, trueLabel);
				mv.visitLdcInsn(false);
				mv.visitJumpInsn(GOTO, endLabel);
				mv.visitLabel(trueLabel);
				mv.visitLdcInsn(true);
				mv.visitLabel(endLabel);
			}
		}
		else if (expressionBinary.op == Kind.OP_GE) {
			if (expressionBinary.leftExpression.type == Type.INTEGER || expressionBinary.leftExpression.type == Type.BOOLEAN) {
				mv.visitJumpInsn(IF_ICMPGE, trueLabel);
				mv.visitLdcInsn(false);
				mv.visitJumpInsn(GOTO, endLabel);
				mv.visitLabel(trueLabel);
				mv.visitLdcInsn(true);
				mv.visitLabel(endLabel);
			}
			else {
				mv.visitInsn(FCMPL);
				mv.visitJumpInsn(IFGE, trueLabel);
				mv.visitLdcInsn(false);
				mv.visitJumpInsn(GOTO, endLabel);
				mv.visitLabel(trueLabel);
				mv.visitLdcInsn(true);
				mv.visitLabel(endLabel);
			}
		}
		else if (expressionBinary.op == Kind.OP_PLUS) {
			if (expressionBinary.leftExpression.type == Type.INTEGER && expressionBinary.rightExpression.type == Type.INTEGER) {
				mv.visitInsn(IADD);
			}
			else if (expressionBinary.leftExpression.type == Type.FLOAT && expressionBinary.rightExpression.type == Type.INTEGER) {
				mv.visitInsn(I2F);
				mv.visitInsn(FADD);
			}
			else if (expressionBinary.leftExpression.type == Type.INTEGER && expressionBinary.rightExpression.type == Type.FLOAT) {
				mv.visitInsn(SWAP);
				mv.visitInsn(I2F);
				mv.visitInsn(FADD);
			}
			else if (expressionBinary.leftExpression.type == Type.FLOAT && expressionBinary.rightExpression.type == Type.FLOAT) {
				mv.visitInsn(FADD);
			}
			else {
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "concat", "(Ljava/lang/String;)Ljava/lang/String;", false);
			}
		}
		else if (expressionBinary.op == Kind.OP_MINUS) {
			if (expressionBinary.leftExpression.type == Type.INTEGER && expressionBinary.rightExpression.type == Type.INTEGER) {
				mv.visitInsn(ISUB);
			}
			else if (expressionBinary.leftExpression.type == Type.FLOAT && expressionBinary.rightExpression.type == Type.INTEGER) {
				mv.visitInsn(I2F);
				mv.visitInsn(FSUB);
			}
			else if (expressionBinary.leftExpression.type == Type.INTEGER && expressionBinary.rightExpression.type == Type.FLOAT) {
				mv.visitInsn(SWAP);
				mv.visitInsn(I2F);
				mv.visitInsn(SWAP);
				mv.visitInsn(FSUB);
			}
			else {
				mv.visitInsn(FSUB);
			}
		}
		else if (expressionBinary.op == Kind.OP_TIMES) {
			if (expressionBinary.leftExpression.type == Type.INTEGER && expressionBinary.rightExpression.type == Type.INTEGER) {
				mv.visitInsn(IMUL);
			}
			else if (expressionBinary.leftExpression.type == Type.FLOAT && expressionBinary.rightExpression.type == Type.INTEGER) {
				mv.visitInsn(I2F);
				mv.visitInsn(FMUL);
			}
			else if (expressionBinary.leftExpression.type == Type.INTEGER && expressionBinary.rightExpression.type == Type.FLOAT) {
				mv.visitInsn(SWAP);
				mv.visitInsn(I2F);
				mv.visitInsn(FMUL);
			}
			else {
				mv.visitInsn(FMUL);
			}
		}
		else if (expressionBinary.op == Kind.OP_DIV) {
			if (expressionBinary.leftExpression.type == Type.INTEGER && expressionBinary.rightExpression.type == Type.INTEGER) {
				mv.visitInsn(IDIV);
			}
			else if (expressionBinary.leftExpression.type == Type.FLOAT && expressionBinary.rightExpression.type == Type.INTEGER) {
				mv.visitInsn(I2F);
				mv.visitInsn(FDIV);
			}
			else if (expressionBinary.leftExpression.type == Type.INTEGER && expressionBinary.rightExpression.type == Type.FLOAT) {
				mv.visitInsn(SWAP);
				mv.visitInsn(I2F);
				mv.visitInsn(SWAP);
				mv.visitInsn(FDIV);
			}
			else {
				mv.visitInsn(FDIV);
			}
		}
		else if (expressionBinary.op == Kind.OP_MOD) {
			mv.visitInsn(IREM);
		}
		else if (expressionBinary.op == Kind.OP_POWER) {
			if (expressionBinary.leftExpression.type == Type.INTEGER && expressionBinary.rightExpression.type == Type.INTEGER) {
				mv.visitInsn(I2D);
				mv.visitVarInsn(DSTORE, slotNum);
				mv.visitInsn(I2D);
				mv.visitVarInsn(DLOAD, slotNum);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
				mv.visitInsn(D2I);
			}
			else if (expressionBinary.leftExpression.type == Type.FLOAT && expressionBinary.rightExpression.type == Type.INTEGER) {
				mv.visitInsn(I2D);
				mv.visitVarInsn(DSTORE, slotNum);
				mv.visitInsn(F2D);
				mv.visitVarInsn(DLOAD, slotNum);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
				mv.visitInsn(D2F);
			}
			else if (expressionBinary.leftExpression.type == Type.INTEGER && expressionBinary.rightExpression.type == Type.FLOAT) {
				mv.visitInsn(F2D);
				mv.visitVarInsn(DSTORE, slotNum);
				mv.visitInsn(I2D);
				mv.visitVarInsn(DLOAD, slotNum);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
				mv.visitInsn(D2F);
			}
			else {
				mv.visitInsn(F2D);
				mv.visitVarInsn(DSTORE, slotNum);
				mv.visitInsn(F2D);
				mv.visitVarInsn(DLOAD, slotNum);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
				mv.visitInsn(D2F);
			}
		}
		
		return null;
	}

	@Override
	public Object visitExpressionConditional(ExpressionConditional expressionConditional, Object arg) throws Exception {
		Label trueLabel = new Label();
		Label endLabel = new Label();
		
		expressionConditional.condition.visit(this, null);
		mv.visitLdcInsn(true);
		mv.visitJumpInsn(IF_ICMPEQ, trueLabel);
		expressionConditional.falseExpression.visit(this, null);
		mv.visitJumpInsn(GOTO, endLabel);
		mv.visitLabel(trueLabel);
		expressionConditional.trueExpression.visit(this, null);
		mv.visitLabel(endLabel);
		
		return null;
	}

	@Override
	public Object visitExpressionFloatLiteral(ExpressionFloatLiteral expressionFloatLiteral, Object arg)
			throws Exception {
		mv.visitLdcInsn(expressionFloatLiteral.value);
		return null;
	}

	@Override
	public Object visitFunctionWithArg(FunctionWithArg FunctionWithArg, Object arg) throws Exception {
		FunctionWithArg.expression.visit(this, null);
		if (FunctionWithArg.functionName == Kind.KW_int) {
			if (FunctionWithArg.expression.type == Type.FLOAT) {
				mv.visitInsn(F2I);
			}
		}
		else if (FunctionWithArg.functionName == Kind.KW_float) {
			if (FunctionWithArg.expression.type == Type.INTEGER) {
				mv.visitInsn(I2F);
			}
		}
		else if (FunctionWithArg.functionName == Kind.KW_sin) {
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "sin", "(D)D", false);
			mv.visitInsn(D2F);
		}
		else if (FunctionWithArg.functionName == Kind.KW_cos) {
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "cos", "(D)D", false);
			mv.visitInsn(D2F);
		}
		else if (FunctionWithArg.functionName == Kind.KW_atan) {
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "atan", "(D)D", false);
			mv.visitInsn(D2F);
		}
		else if (FunctionWithArg.functionName == Kind.KW_abs) {
			if (FunctionWithArg.expression.type == Type.INTEGER) {
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "abs", "(I)I", false);
			}
			else {
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "abs", "(F)F", false);
			}
		}
		else {
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "log", "(D)D", false);
			mv.visitInsn(D2F);
		}
		return null;
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdentifier expressionIdent, Object arg) throws Exception {
		int curSlotNum;
		if (expressionIdent.dec.getClass() == VariableDeclaration.class) {
			curSlotNum = expressionIdent.dec.slotNum;
		}
		else {
			VariableListDeclaration dec = (VariableListDeclaration)expressionIdent.dec;
			curSlotNum = dec.slotNum + dec.names.indexOf(expressionIdent.name);
		}
		switch(expressionIdent.dec.type) {
		case INTEGER: {
			mv.visitVarInsn(ILOAD, curSlotNum);
		}
		break;
		case FLOAT: {
			mv.visitVarInsn(FLOAD, curSlotNum);
		}
		break;
		case BOOLEAN: {
			mv.visitVarInsn(ILOAD, curSlotNum);
		}
		break;
		case CHAR: {
			mv.visitVarInsn(ILOAD, curSlotNum);
		}
		break;
		case STRING: {
			mv.visitVarInsn(ALOAD, curSlotNum);
		}
		break;
		default: {
			throw new UnsupportedOperationException();
		}
		}
		return null;
	}

	@Override
	public Object visitExpressionIntegerLiteral(ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
			throws Exception {
		mv.visitLdcInsn(expressionIntegerLiteral.value);
		return null;
	}

	@Override
	public Object visitExpressionStringLiteral(ExpressionStringLiteral expressionStringLiteral, Object arg)
			throws Exception {
		mv.visitLdcInsn(expressionStringLiteral.text);
		return null;
	}

	@Override
	public Object visitExpressionCharLiteral(ExpressionCharLiteral expressionCharLiteral, Object arg) throws Exception {
		mv.visitLdcInsn(expressionCharLiteral.text);
		return null;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws Exception {
		statementAssign.expression.visit(this, null);
		int curSlotNum;
		if (statementAssign.lhs.declaration.getClass() == VariableDeclaration.class) {
			curSlotNum = statementAssign.lhs.declaration.slotNum;
			System.out.println(curSlotNum);
		}
		else {
			VariableListDeclaration dec = (VariableListDeclaration)statementAssign.lhs.declaration;
			curSlotNum = statementAssign.lhs.declaration.slotNum + dec.names.indexOf(statementAssign.lhs.identifier);
		}
		switch(statementAssign.lhs.type) {
		case INTEGER: {
			mv.visitVarInsn(ISTORE, curSlotNum);
		}
		break;
		case FLOAT: {
			mv.visitVarInsn(FSTORE, curSlotNum);
		}
		break;
		case BOOLEAN: {
			mv.visitVarInsn(ISTORE, curSlotNum);
		}
		break;
		case CHAR: {
			mv.visitVarInsn(ISTORE, curSlotNum);
		}
		break;
		case STRING: {
			mv.visitVarInsn(ASTORE, curSlotNum);
		}
		break;
		default: {
			throw new UnsupportedOperationException();
		}
		}
		
		return null;
	}

	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		
		return null;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		ifStatement.condition.visit(this, null);
		Label trueLabel = new Label();
		Label endLabel = new Label();
		mv.visitLdcInsn(true);
		mv.visitJumpInsn(IF_ICMPEQ, trueLabel);
		mv.visitJumpInsn(GOTO, endLabel);
		mv.visitLabel(trueLabel);
		ifStatement.block.visit(this, null);
		mv.visitLabel(endLabel);
		
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		Label trueLabel = new Label();
		Label startLabel = new Label();
		Label endLabel = new Label();
		mv.visitLabel(startLabel);
		whileStatement.condition.visit(this, null);
		mv.visitLdcInsn(true);
		mv.visitJumpInsn(IF_ICMPEQ, trueLabel);
		mv.visitJumpInsn(GOTO, endLabel);
		mv.visitLabel(trueLabel);
		whileStatement.b.visit(this, null);
		mv.visitJumpInsn(GOTO, startLabel);
		mv.visitLabel(endLabel);
		
		return null;
	}

	@Override
	public Object visitPrintStatement(PrintStatement printStatement, Object arg) throws Exception {
		/**
		 * TODO refactor and complete implementation.
		 * 
		 * In all cases, invoke CodeGenUtils.genLogTOS(GRADE, mv, type); before
		 * consuming top of stack.
		 */
		printStatement.expression.visit(this, arg);
		Type type = printStatement.expression.getType();
		switch (type) {
		case INTEGER : {
			PLPCodeGenUtils.genLogTOS(GRADE, mv, type);
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
			mv.visitInsn(Opcodes.SWAP);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
					"println", "(I)V", false);
		}
		break;
		case BOOLEAN : {
			PLPCodeGenUtils.genLogTOS(GRADE, mv, type);
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
			mv.visitInsn(Opcodes.SWAP);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
					"println", "(Z)V", false);
		}
		break;
		case FLOAT : {
			PLPCodeGenUtils.genLogTOS(GRADE, mv, type);
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
			mv.visitInsn(Opcodes.SWAP);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
					"println", "(F)V", false);
		}
		break;
		case CHAR : {
			PLPCodeGenUtils.genLogTOS(GRADE, mv, type);
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
			mv.visitInsn(Opcodes.SWAP);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
					"println", "(C)V", false);
		}
		break;
		case STRING : {
			PLPCodeGenUtils.genLogTOS(GRADE, mv, type);
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
			mv.visitInsn(Opcodes.SWAP);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
					"println", "(Ljava/lang/String;)V", false);
		}
		break;
		default: {
			throw new UnsupportedOperationException();
		}
		}
		return null;
		
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		sleepStatement.time.visit(this, null);
		mv.visitInsn(I2L);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
		return null;
	}

	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary, Object arg) throws Exception {
		expressionUnary.expression.visit(this, null);
		if (expressionUnary.op == Kind.OP_EXCLAMATION) {
			if (expressionUnary.expression.type == Type.INTEGER) {
				mv.visitLdcInsn(0xFFFFFFFF);
			}
			else {
				mv.visitLdcInsn(1);
			}
			mv.visitInsn(IXOR);
		}
		else if (expressionUnary.op == Kind.OP_MINUS) {
			if (expressionUnary.expression.type == Type.INTEGER) {
				mv.visitInsn(INEG);
			}
			else {
				mv.visitInsn(FNEG);
			}
		}
		return null;
	}

}
