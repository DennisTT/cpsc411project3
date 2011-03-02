package minijava.ir.frame.x86;

import minijava.ir.frame.Access;
import minijava.ir.tree.IRExp;

public class X86InFrame extends Access
{
  private int offset;
  
  protected X86InFrame(int offset)
  {
    this.offset = offset;
  }
  
  @Override
  public String toString()
  {
    // TODO Auto-generated method stub
    return null;
  }
  
  @Override
  public IRExp exp(IRExp fp)
  {
    // TODO Auto-generated method stub
    return null;
  }
  
}
