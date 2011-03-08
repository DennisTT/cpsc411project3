package minijava.translate.implementation;

import junit.framework.Assert;
import minijava.ir.temp.Label;
import minijava.ir.temp.Temp;
import minijava.ir.tree.IR;
import minijava.ir.tree.IRExp;
import minijava.ir.tree.IRStm;
import minijava.ir.tree.CJUMP.RelOp;

public class IfThenElseExp extends TranslateExp
{
  TranslateExp test, thenExp, elseExp;
  
  Label t = Label.generate("t");
  Label f = Label.generate("f");
  Label join = Label.generate("join");
  
  public IfThenElseExp(TranslateExp test, TranslateExp thenExp, TranslateExp elseExp)
  {
    this.test = test;
    this.thenExp = thenExp;
    this.elseExp = elseExp;
  }

  @Override
  public IRExp unEx()
  {
    Temp res = new Temp();
    return IR.ESEQ(IR.SEQ(test.unCx(t, f), 
                          IR.SEQ( IR.LABEL(t), 
                                  IR.MOVE(IR.TEMP(res), 
                                          thenExp.unEx())),
                          IR.SEQ( IR.LABEL(f), 
                                  IR.MOVE(IR.TEMP(res), 
                                          elseExp.unEx()))),
                   IR.TEMP(res));
  }

  @Override
  public IRStm unNx()
  {
    return IR.SEQ(test.unCx(t, f), 
                  IR.SEQ(IR.LABEL(t), thenExp.unNx(), IR.JUMP(join)),
                  IR.SEQ(IR.LABEL(f), elseExp.unNx(), IR.JUMP(join)),
                  IR.LABEL(join));
  }

  @Override
  public IRStm unCx(Label t, Label f)
  {
    // This method should never be called
    // Do nothing
    Assert.assertTrue("unCx should not be called for IfThenElseExp.", false);
    return null;
  }
}
