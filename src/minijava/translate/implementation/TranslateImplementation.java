package minijava.translate.implementation;

import minijava.ir.frame.Frame;
import minijava.translate.Fragments;
import minijava.typechecker.TypeChecked;
import minijava.typechecker.implementation.TypeCheckedImplementation;

public class TranslateImplementation
{
  private Frame                     factory;
  private TypeCheckedImplementation t;
  private Fragments                 fragments;
  
  public TranslateImplementation(Frame frameFactory, TypeChecked typechecked)
  {
    this.factory    = frameFactory;
    this.t          = (TypeCheckedImplementation) typechecked;
    this.fragments  = new Fragments(this.factory);
  }
  
  public Fragments translate()
  {
    this.t.program.accept(new TranslateVisitor(this.factory, this.fragments));
    return this.fragments;
  }
}
