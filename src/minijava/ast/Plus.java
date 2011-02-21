package minijava.ast;

import minijava.visitor.Visitor;

public class Plus extends Expression {

	public final Expression e1;
	public final Expression e2;

	public Plus(Expression e1, Expression e2) {
		super();
		this.e1 = e1;
		this.e2 = e2;
	}

	@Override
	public <R> R accept(Visitor<R> v) {
		return v.visit(this);
	}

}
