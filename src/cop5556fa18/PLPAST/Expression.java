package cop5556fa18.PLPAST;

import cop5556fa18.PLPTypes.Type;
import cop5556fa18.PLPScanner.Token;

public abstract class Expression extends PLPASTNode {
	
	public Type type;
	
	public Type getType() { 
		return type; 
	}

	public Expression(Token firstToken) {
		super(firstToken);
	}	

}
