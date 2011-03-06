package minijava.translate.implementation;

import java.util.Stack;

import minijava.ast.*;
import minijava.ir.frame.Frame;
import minijava.ir.temp.Label;
import minijava.ir.temp.Temp;
import minijava.ir.tree.IR;
import minijava.ir.tree.IRExp;
import minijava.ir.tree.IRStm;
import minijava.ir.tree.BINOP.Op;
import minijava.translate.Fragments;
import minijava.translate.ProcFragment;
import minijava.translate.Translator;
import minijava.util.List;
import minijava.visitor.Visitor;

public class TranslateVisitor implements Visitor<TranslateExp>
{
  private Stack<Frame>  frames = new Stack<Frame>();
  private Fragments     fragments;
  
  public TranslateVisitor(Frame frameFactory, Fragments fragments)
  {
    this.frames.push(frameFactory);
    this.fragments = fragments;
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
    n.mainClass.accept(this);
    n.classes.accept(this);
    
    return null;
  }
  
  @Override
  public TranslateExp visit(MainClass n)
  {
    List<Boolean> frameParams = List.empty();
    this.frames.push(this.createNewFrame(Translator.L_MAIN, frameParams));
    this.fragments.add(new ProcFragment(this.frames.peek(),
                                        this.procEntryExit(n.statement.accept(this))));
    this.frames.pop();
    
    return null;
  }

  @Override
  public TranslateExp visit(ClassDecl n)
  {
    n.vars.accept(this);
    n.methods.accept(this);
    
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
    // No translation required
    return null;
  }

  @Override
  public TranslateExp visit(BooleanType n)
  {
    // No translation required
    return null;
  }

  @Override
  public TranslateExp visit(IntegerType n)
  {
    // No translation required
    return null;
  }

  @Override
  public TranslateExp visit(ObjectType n)
  {
    // No translation required
    return null;
  }

  @Override
  public TranslateExp visit(Block n)
  {
    IRStm s     = IR.NOP;
    int length  = n.statements.size();
    
    // Check if block is empty, a single statement, or multiple statements
    // An empty block defaults to a No-Op
    if(length > 0)
    {
      s = n.statements.elementAt(0).accept(this).unNx();
      
      // Generate SEQ instructions chain if block contains multiple statements
      for(int i = 1; i < length; ++i)
      {
        s = IR.SEQ(s, n.statements.elementAt(i).accept(this).unNx());
      }
    }
    
    return new TranslateNx(s);
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
    return new TranslateNx(IR.MOVE( IR.TEMP(new Temp()),
                                    IR.CALL(Translator.L_PRINT,
                                            n.exp.accept(this).unEx())));
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
    return new TranslateEx(IR.PLUS( n.e1.accept(this).unEx(),
                                    n.e2.accept(this).unEx()));
  }

  @Override
  public TranslateExp visit(Minus n)
  {
    return new TranslateEx(IR.BINOP(Op.MINUS,
                                    n.e1.accept(this).unEx(),
                                    n.e2.accept(this).unEx()));
  }

  @Override
  public TranslateExp visit(Times n)
  {
    return new TranslateEx(IR.BINOP(Op.MUL,
                                    n.e1.accept(this).unEx(),
                                    n.e2.accept(this).unEx()));
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
    return new TranslateEx(IR.CONST(n.value));
  }

  @Override
  public TranslateExp visit(BooleanLiteral n)
  {
    return new TranslateEx((n.value) ? IR.TRUE : IR.FALSE);
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
    // Subtracting 1 by a boolean bit results in the bit being flipped
    return new TranslateEx(IR.BINOP(Op.MINUS, IR.CONST(1), not.e.accept(this).unEx()));
  }
  
  private Frame createNewFrame(Label name, List<Boolean> formalsEscape)
  {
    return this.frames.peek().newFrame(name, formalsEscape);
  }
  
  // Helper method for updating the current frame's state upon exiting a method
  private IRStm procEntryExit(TranslateExp body)
  {
    Frame currentFrame = this.frames.peek();
    
    // Treat empty method blocks as No-Ops
    IRStm s = IR.NOP;
    if(body != null)
    {
      // Set method return value (if necessary)
      IRExp e = body.unEx();
      s = (e != null) ? IR.MOVE(currentFrame.RV(), e) : body.unNx();
    }
    
    return currentFrame.procEntryExit1(s);
  }
}
