package minijava.ast;

import minijava.visitor.Visitor;

public class ObjectType extends Type {
	
	public final String name;

	public ObjectType(String name) {
		super();
		this.name = name;
	}

	@Override
	public <R> R accept(Visitor<R> v) {
		return v.visit(this);
	}

	@Override
	public boolean equals(Object other) {
		if (this.getClass()==other.getClass()) {
			return this.name.equals(((ObjectType)other).name);
		}
		return false;
	}
}
