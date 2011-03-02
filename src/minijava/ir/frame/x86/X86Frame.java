package minijava.ir.frame.x86;

import minijava.ir.frame.Access;
import minijava.ir.frame.Frame;
import minijava.ir.interp.Interp;
import minijava.ir.interp.Word;
import minijava.ir.interp.X86SimFrame;
import minijava.ir.temp.Label;
import minijava.ir.tree.IRExp;
import minijava.ir.tree.IRStm;
import minijava.util.IndentingWriter;
import minijava.util.List;

public class X86Frame extends Frame {

	protected X86Frame(Label label, List<Access> formals) {
		super(label, formals);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void dump(IndentingWriter out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Frame newFrame(Label name, List<Boolean> formalsEscape) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Access allocLocal(boolean escapes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IRExp FP() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IRExp RV() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int wordSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public IRStm procEntryExit1(IRStm body) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public X86SimFrame newSimFrame(Interp interp, List<Word> args) {
		// TODO Auto-generated method stub
		return null;
	}

}
