package edu.drexel.cs.db.rank.test;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.values.Value;

@NonNullByDefault
public class DimpleExampleThreeBitXor extends FactorFunction
{
	@Override
	public final double evalEnergy(Value[] args)
	{
		int arg0 = args[0].getInt();
		int arg1 = args[1].getInt();
		int arg2 = args[2].getInt();
		
		return (arg0 ^ arg1 ^ arg2) == 0 ? 0 : Double.POSITIVE_INFINITY;
	}
}