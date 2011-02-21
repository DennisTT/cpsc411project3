package minijava.typechecker.implementation;

import minijava.ast.Type;
import minijava.typechecker.TypeChecked;

public class TypeCheckedImplementation extends TypeChecked
{
  public Type type;
  
  public TypeCheckedImplementation(Type type)
  {
    this.type = type;
  }
}
