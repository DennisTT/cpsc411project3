package minijava.translate.implementation;

import minijava.ast.*;
import minijava.ir.frame.Frame;
import minijava.translate.Fragments;
import minijava.visitor.Visitor;

public class TranslateVisitor implements Visitor<TranslateExp>
{
  private Frame     frameFactory;
  private Fragments fragments;
  
  public TranslateVisitor(Frame frameFactory, Fragments fragments)
  {
    this.frameFactory = frameFactory;
    this.fragments    = fragments;
  }
  
  @Override
  public <T extends AST> TranslateExp visit(NodeList<T> ns)
  {
    // Iterate all nodes and generate IR
    for(int i = 0; i < ns.size(); ++i) { ns.elementAt(i).accept(this); }
    return null;
  }
  
  @Override
  public TranslateExp visit(Program n)
  {
    // TODO Auto-generated method stub
    return null;
  }
  
  @Override
  public TranslateExp visit(MainClass n)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TranslateExp visit(ClassDecl n)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TranslateExp visit(VarDecl n)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TranslateExp visit(MethodDecl n)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TranslateExp visit(IntArrayType n)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TranslateExp visit(BooleanType n)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TranslateExp visit(IntegerType n)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TranslateExp visit(ObjectType n)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TranslateExp visit(Block n)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TranslateExp visit(If n)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TranslateExp visit(While n)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TranslateExp visit(Print n)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TranslateExp visit(Assign n)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TranslateExp visit(ArrayAssign n)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TranslateExp visit(And n)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TranslateExp visit(LessThan n)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TranslateExp visit(Plus n)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TranslateExp visit(Minus n)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TranslateExp visit(Times n)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TranslateExp visit(ArrayLookup n)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TranslateExp visit(ArrayLength n)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TranslateExp visit(Call n)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TranslateExp visit(IntegerLiteral n)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TranslateExp visit(BooleanLiteral n)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TranslateExp visit(IdentifierExp n)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TranslateExp visit(This n)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TranslateExp visit(NewArray n)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TranslateExp visit(NewObject n)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TranslateExp visit(Not not)
  {
    // TODO Auto-generated method stub
    return null;
  }
}
