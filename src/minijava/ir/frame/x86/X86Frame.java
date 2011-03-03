package minijava.ir.frame.x86;

import minijava.ir.frame.Access;
import minijava.ir.frame.Frame;
import minijava.ir.interp.Interp;
import minijava.ir.interp.Word;
import minijava.ir.interp.X86SimFrame;
import minijava.ir.temp.Label;
import minijava.ir.temp.Temp;
import minijava.ir.tree.IRExp;
import minijava.ir.tree.IRStm;
import minijava.ir.tree.TEMP;
import minijava.util.IndentingWriter;
import minijava.util.List;

public class X86Frame extends Frame {
  public static final int FIRST_FORMAL_OFFSET = 4;
  public static final int FORMAL_INCREMENT = 4;
  
  public static X86Frame factory = new X86Frame(null, null);
  
  protected int localCount = 0;

	protected X86Frame(Label label, List<Access> formals)
	{
		super(label, formals);
	}
	
	public int numLocals()
	{
	  return this.localCount;
	}

	@Override
	public void dump(IndentingWriter out)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public Frame newFrame(Label name, List<Boolean> formalsEscape)
	{
	  int offset = FIRST_FORMAL_OFFSET;
	  
	  List<Access> newFormals = List.empty();
	  for(Boolean b : formalsEscape)
	  {
	    if(b)
	    {
	      newFormals.add(new X86InFrame(offset));
	      offset += this.wordSize();
	    }
	    else
	    {
	      newFormals.add(new X86InReg(new Temp()));
	    }
	  }
	  
	  return new X86Frame(name, newFormals);
	}

	@Override
	public Access allocLocal(boolean escapes)
	{
		return (escapes) ?  new X86InFrame(-(this.wordSize() * this.localCount++)) :
		                    new X86InReg(new Temp());
	}

	@Override
	public IRExp FP()
	{
		return new TEMP(new Temp("ebp"));
	}

	@Override
	public IRExp RV()
	{
    return new TEMP(new Temp("eax"));
	}

	@Override
	public int wordSize()
	{
	  // x86 supports 32-bit addressing (4 bytes)
		return 4;
	}

	@Override
	public IRStm procEntryExit1(IRStm body)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public X86SimFrame newSimFrame(Interp interp, List<Word> args)
	{
		return new X86SimFrame(interp, factory, args);
	}
}
