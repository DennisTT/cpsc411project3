package minijava.translate.implementation;

import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

import junit.framework.Assert;

import minijava.ast.*;
import minijava.ir.frame.Access;
import minijava.ir.frame.Frame;
import minijava.ir.temp.Label;
import minijava.ir.temp.Temp;
import minijava.ir.tree.CJUMP.RelOp;
import minijava.ir.tree.IR;
import minijava.ir.tree.IRExp;
import minijava.ir.tree.IRStm;
import minijava.ir.tree.BINOP.Op;
import minijava.translate.Fragment;
import minijava.translate.Fragments;
import minijava.translate.ProcFragment;
import minijava.translate.Translator;
import minijava.util.List;
import minijava.visitor.Visitor;

public class TranslateVisitor implements Visitor<TranslateExp>
{
  private Fragments     fragments,
                        classFragments;
  
  private Stack<Frame>  frames = new Stack<Frame>();
  private SymbolTable   symbols = new SymbolTable();
  private String        currentClass,
                        currentMethod;
  
  public TranslateVisitor(Frame frameFactory, Fragments fragments)
  {
    this.frames.push(frameFactory);
    this.fragments = fragments;
    this.classFragments = new Fragments(frameFactory);
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
    // Make sure class declarations are defined before mainClass is visited
    n.classes.accept(this);
    n.mainClass.accept(this);
    
    // Make sure mainClass Fragment from is defined before class Fragments
    Iterator<Fragment> it = this.classFragments.iterator();
    while(it.hasNext())
    {
      this.fragments.add(it.next());
    }
    
    return null;
  }
  
  @Override
  public TranslateExp visit(MainClass n)
  {
    List<Boolean> frameParams = List.empty();
    this.frames.push(this.createNewFrame(Translator.L_MAIN, frameParams));
    this.currentClass = n.className;
    this.currentMethod = "main";
    this.fragments.add(new ProcFragment(this.frames.peek(),
                                        this.procEntryExit(n.statement.accept(this))));
    this.currentClass = this.currentMethod = null;
    this.frames.pop();
    
    return null;
  }

  @Override
  public TranslateExp visit(ClassDecl n)
  {
    this.currentClass = n.name;
    
    n.vars.accept(this);
    n.methods.accept(this);

    this.currentClass = null;

    return null;
  }

  @Override
  public TranslateExp visit(VarDecl n)
  {
    Access var;
    
    if(this.currentMethod != null)
    {
      // Allocate local method variable on current frame
      var = this.frames.peek().allocLocal(false);
      this.symbols.addClassMethodVar( this.currentClass,
                                      this.currentMethod,
                                      n.name,
                                      var);
      return new TranslateEx(var.exp(this.frames.peek().FP()));
    }

    // Allocate class variable
    var = this.frames.peek().alloc(this.symbols.numClassVars(this.currentClass) * this.frames.peek().wordSize());
    this.symbols.addClassVar(this.currentClass, n.name, var);
    return null;
  }

  @Override
  public TranslateExp visit(MethodDecl n)
  {
    this.currentMethod = n.name;
    
    this.addClassMethod(n.name);
    
    // Include implicit argument for "this" in method frame
    List<Boolean> frameParams = List.list(true);
    int numFormals = n.formals.size();
    for(int i = 0; i < numFormals; ++i)
    {
      frameParams.add(true);
    }
    this.frames.push(this.createNewFrame(Label.get(n.name), frameParams));
    
    // Keep track of formal variables
    this.addFormal(0, "this");
    for(int j = 0; j < numFormals; ++j)
    {
      this.addFormal(j + 1, n.formals.elementAt(j).name);
    }
    
    // Allocate space for variables
    n.vars.accept(this);
    
    IRExp e = null;
    
    // Check if block is empty, a single statement, or multiple statements
    int numStatements = n.statements.size();
    if(numStatements > 0)
    {
      IRStm s = n.statements.elementAt(0).accept(this).unNx();
      
      // Generate SEQ instructions chain if method contains multiple statements
      for(int i = 1; i < numStatements; ++i)
      {
        s = IR.SEQ(s, n.statements.elementAt(i).accept(this).unNx());
      }
      
      e = IR.ESEQ(s, n.returnExp.accept(this).unEx());
    }
    else
    {
      e = n.returnExp.accept(this).unEx();
    }
    
    this.classFragments.add(new ProcFragment( this.frames.peek(),
                                              this.procEntryExit(new TranslateEx(e))));
    this.currentMethod = null;
    this.frames.pop();
    
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
    return new IfThenElseExp( n.tst.accept(this), 
                              n.thn.accept(this), 
                              n.els.accept(this));
  }

  @Override
  public TranslateExp visit(While n)
  {
    Label done = Label.generate("done");
    Label test = Label.generate("test");
    TranslateExp loop = new IfThenElseExp(n.tst.accept(this), 
                                          new TranslateNx(IR.SEQ( n.body.accept(this).unNx(),
                                                                  IR.JUMP(test))), 
                                          new TranslateNx(IR.JUMP(done)));
    return new TranslateNx(IR.SEQ(IR.LABEL(test),
                                  loop.unNx(),
                                  IR.LABEL(done)));
  }

  @Override
  public TranslateExp visit(Print n)
  {
    return new TranslateNx(IR.EXP(IR.CALL(Translator.L_PRINT,
                                          n.exp.accept(this).unEx())));
  }

  @Override
  public TranslateExp visit(Assign n)
  {
    IRExp m = this.getVarLocation(n.name),
          v = n.value.accept(this).unEx();
    return (this.currentMethod != null) ? new TranslateNx(IR.MOVE(m, v)) :
                                          new TranslateNx(IR.MOVE(IR.MEM(m), v));
  }

  @Override
  public TranslateExp visit(ArrayAssign n)
  {
    IRExp m = this.getVarLocation(n.name);
    if(m == null) { return null; }
    
    int wordSize  = this.frames.peek().wordSize();
    Label l1      = Label.gen(),
          l2      = Label.gen(),
          l3      = Label.gen(),
          l4      = Label.gen();
    IRExp i       = n.index.accept(this).unEx(),
          r       = IR.TEMP(new Temp());
    
    // Perform index bounds checking before assigning value to array memory
    // Return IR.FALSE on error
    return new TranslateNx(IR.SEQ(IR.CJUMP(RelOp.LT, IR.CONST(-1), i, l2, l1),
                                  IR.LABEL(l1),
                                  IR.MOVE(r, IR.FALSE),
                                  IR.JUMP(l4),
                                  IR.LABEL(l2),
                                  IR.CJUMP(RelOp.LT, i, IR.MEM(IR.MINUS(m, wordSize)), l3, l1),
                                  IR.LABEL(l3),
                                  IR.MOVE(IR.MEM(IR.PLUS(m, IR.MUL(i, wordSize))),
                                          n.value.accept(this).unEx()),
                                  IR.LABEL(l4)));
  }

  @Override
  public TranslateExp visit(And n)
  {
    return new AndCx( n.e1.accept(this),
                      n.e2.accept(this));
  }

  @Override
  public TranslateExp visit(LessThan n)
  {
    return new RelCx( RelOp.LT,  
                      n.e1.accept(this).unEx(), 
                      n.e2.accept(this).unEx());
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
    int wordSize  = this.frames.peek().wordSize();
    Label l1      = Label.gen(),
          l2      = Label.gen(),
          l3      = Label.gen(),
          l4      = Label.gen();
    IRExp p       = n.array.accept(this).unEx(),
          i       = n.index.accept(this).unEx(),
          r       = IR.TEMP(new Temp());
    
    // Perform index bounds checking before accessing array memory
    // Return IR.FALSE on error
    return new TranslateEx(IR.ESEQ(IR.SEQ(IR.CJUMP(RelOp.LT, IR.CONST(-1), i, l2, l1),
                                          IR.LABEL(l1),
                                          IR.MOVE(r, IR.FALSE),
                                          IR.JUMP(l4),
                                          IR.LABEL(l2),
                                          IR.CJUMP(RelOp.LT, i, IR.MEM(IR.MINUS(p, wordSize)), l3, l1),
                                          IR.LABEL(l3),
                                          IR.MOVE(r, IR.MEM(IR.PLUS(p, IR.MUL(i, wordSize)))),
                                          IR.LABEL(l4)),
                                  r));
  }

  @Override
  public TranslateExp visit(ArrayLength n)
  {
    // Array IR gives us the pointer to the 0 element of the array
    // Length is one offset before this pointer. 
    return new TranslateEx(IR.MEM(IR.MINUS( n.array.accept(this).unEx(), 
                                            this.frames.peek().wordSize())));
  }

  @Override
  public TranslateExp visit(Call n)
  {
    // Build list of arguments to pass to the method
    // Include implicit argument to "this"
    List<IRExp> args = List.list(n.receiver.accept(this).unEx());
    int length = n.rands.size();
    for(int i = 0; i < length; ++i)
    {
      args.add(n.rands.elementAt(i).accept(this).unEx());
    }
    
    return new TranslateEx(IR.CALL(Label.get(n.name), args));
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
    IRExp m = this.getVarLocation(n.name);
    return (m != null) ? new TranslateEx(m) : null;
  }
  
  @Override
  public TranslateExp visit(This n)
  {
    Access var = this.lookupVar("this");
    Assert.assertNotNull("'This' not found in lookup.", var);
    return (var != null) ? new TranslateEx(var.exp(this.frames.peek().FP())) : null;
  }
  
  @Override
  public TranslateExp visit(NewArray n)
  {
    IRExp r = IR.TEMP(new Temp()),
          c = IR.CALL(Translator.L_NEW_ARRAY, List.list(n.size.accept(this).unEx()));
    
    return new TranslateEx(IR.ESEQ( IR.MOVE(r, c), r));
  }

  @Override
  public TranslateExp visit(NewObject n)
  {
    int         s = Math.max(this.symbols.numClassVars(n.typeName) * this.frames.peek().wordSize(), this.frames.peek().wordSize());
    IRExp       r = IR.TEMP(new Temp()),
                c = IR.CALL(Translator.L_NEW_OBJECT, List.list(IR.CONST(s)));
    IRStm seq = IR.MOVE(r, c);
    for(int i = 0; i < this.symbols.numClassVars(n.typeName); ++i)
    {
      seq = IR.SEQ(seq, IR.MOVE(IR.MEM(IR.PLUS(r, i * this.frames.peek().wordSize())), IR.CONST(0)));
    }
    return new TranslateEx(IR.ESEQ(seq, r));
  }

  @Override
  public TranslateExp visit(Not not)
  {
    // Subtracting 1 by a boolean bit results in the bit being flipped
    return new TranslateEx(IR.BINOP(Op.MINUS, IR.CONST(1), not.e.accept(this).unEx()));
  }
  
  // ---------------------------------------------------------------------------
  
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
  
  // Adds the specified method as a symbol entry to the current class
  private void addClassMethod(String methodName)
  {
    this.symbols.addClassMethod(this.currentClass, methodName);
  }
  
  // Adds the specified formal as a symbol entry to the current class method
  private Access addFormal(int i, String id)
  {
    Assert.assertNotNull(this.currentMethod);
    
    Access var = this.frames.peek().getFormal(i);
    this.symbols.addClassMethodVar( this.currentClass,
                                    this.currentMethod,
                                    id,
                                    var);
    return var;
  }
  
  // Returns the appropriate IRExp representing the memory location for the
  // specified identifier
  private IRExp getVarLocation(String id)
  {
    IRExp p;
    
    // Check to see if identifier is a local method variable
    Access var = this.lookupMethodVar(id);
    if(var != null)
    {
      // Method variable found
      p = this.frames.peek().FP();
    }
    else
    {
      // Check to see if identifier is a class variable
      var = this.lookupClassVar(id);
      if(var == null) { return null; }
      
      // Class variable found
      Access thisVar = this.lookupVar("this");
      p = thisVar.exp(this.frames.peek().FP());
    }
    
    return var.exp(p);
  }
  
  // Attempts to find the specified class in the symbol table
  private Set<String> lookupClassVars(String className)
  {
    return this.symbols.lookupClassVars(className);
  }
  
  private Access lookupClassVar(String id)
  {
    return this.symbols.lookupClassVar(this.currentClass, id);
  }
  
  private Access lookupMethodVar(String id)
  {
    return this.symbols.lookupMethodVar(this.currentClass, this.currentMethod, id);
  }
  
  // Attempts to find the specified variable in the symbol table
  private Access lookupVar(String id)
  {
    Access var = null;
    if(this.currentMethod != null)
    {
      // Check for local method variable
      var = this.lookupMethodVar(id);
    }
    
    if(var == null)
    {
      // Check for class variable
      var = this.lookupClassVar(id);
    }
    
    return var;
  }
}
