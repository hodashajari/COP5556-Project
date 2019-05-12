/**
 * Name: Yihao Wu
 * Assignment Number: Project#6
 * Date Due: November 20, 2018
 */

package cop5556fa18;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556fa18.PLPScanner;
import cop5556fa18.PLPTypeChecker.SemanticException;
import cop5556fa18.PLPAST.PLPASTVisitor;
import cop5556fa18.PLPAST.Program;

public class PLPTypeCheckerTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	/**
	 * Prints objects in a way that is easy to turn on and off
	 */
	static final boolean doPrint = true;

	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}

	/**
	 * Scan, parse, and type check an input string
	 * 
	 * @param input
	 * @throws Exception
	 */
	void typeCheck(String input) throws Exception {
		show(input);
		// instantiate a Scanner and scan input
		PLPScanner scanner = new PLPScanner(input).scan();
		show(scanner);
		// instantiate a Parser and parse input to obtain and AST
		Program ast = new PLPParser(scanner).parse();
		show(ast);
		// instantiate a TypeChecker and visit the ast to perform type checking and
		// decorate the AST.
		PLPASTVisitor v = new PLPTypeChecker();
		ast.visit(v, null);
	}
	
	
	@Test
	public void emptyProg() throws Exception {
		String input = "emptyProg{}";
		typeCheck(input);
	}

	@Test
	public void expression1() throws Exception {
		String input = "prog {print 1+2;}";
		typeCheck(input);
	}

	@Test
	public void expression2_fail() throws Exception {
		String input = "prog { print true+4; }"; //should throw an error due to incompatible types in binary expression
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void testScope1() throws Exception {
		String input = "p{int var;if(true) {float var;var=5.0;};var=5;}";
		typeCheck(input);
	}
	
	@Test
	public void testExpressionBinary5() throws Exception {
		String input = "prog { int i,j,k; i = j % k;}";
		typeCheck(input);
	}

}
