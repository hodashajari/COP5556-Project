/**
 * Name: Yihao Wu
 * Assignment Number: Project#6
 * Date Due: November 20, 2018
 */

package cop5556fa18;

import java.util.HashMap;
import java.util.Stack;
import cop5556fa18.PLPAST.Declaration;

public class PLPSymbolTable {
	
	private Stack<Integer> scope_stack;
	private HashMap<String, Entry> symbolTable;
	
	public int current_scope;
	public int next_scope;
	
	PLPSymbolTable() {
		scope_stack = new Stack<Integer>();
		symbolTable = new HashMap<String, Entry>();
		current_scope = 0;
		next_scope = 1;
	}
	
	public void enterScope() {
		current_scope = next_scope++;
		scope_stack.push(current_scope);
	}
	
	public void closeScope() {
		scope_stack.pop();
		if (!scope_stack.empty()) {
			current_scope = scope_stack.peek();
		}
		else {
			current_scope = 0;
			next_scope = 1;
		}
	}
	
	public boolean insert(String name, Declaration declaration) {
		if (symbolTable.containsKey(name)) {
			Entry cur_entry = symbolTable.get(name);
			while (cur_entry != null) {
				if (cur_entry.scope_number == current_scope) {
					return false;
				}
				else {
					cur_entry = cur_entry.next;
				}
			}
			System.out.println("Insert " + name + current_scope + declaration.type);
			Entry entry = new Entry(declaration, current_scope, symbolTable.get(name));
			symbolTable.put(name, entry);
			return true;
		}
		else {
			System.out.println("Insert " + name + current_scope + declaration.type);
			Entry entry = new Entry(declaration, current_scope, null);
			symbolTable.put(name, entry);
			return true;
		}
	}
	
	public Declaration lookup(String name) {
		Entry entry = symbolTable.get(name);
		if (entry != null) {
			Stack<Integer> tmp = new Stack<Integer>();
			while (!scope_stack.empty()) {
				Entry cur_entry = entry;
				while (cur_entry != null) {
					if (cur_entry.scope_number == scope_stack.peek().intValue()) {
						while (!tmp.empty()) {
							scope_stack.push(tmp.pop());
						}
						return cur_entry.declaration;
					}
					else cur_entry = cur_entry.next;
				}
				tmp.push(scope_stack.pop());
			}
			while (!tmp.empty()) {
				scope_stack.push(tmp.pop());
			}
		}
		return null;
	}
	
	public class Entry {
		
		private Declaration declaration;
		private int scope_number;
		private Entry next;
		
		Entry(Declaration declaration, int scope_number, Entry next) {
			this.declaration = declaration;
			this.scope_number = scope_number;
			this.next = next;
		}
		
		public Declaration getDeclaration() {
			return declaration;
		}
	}

}
