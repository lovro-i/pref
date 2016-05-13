/* Copyright (C) 2003 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */
package edu.umass.cs.mallet.grmm.test;

import edu.umass.cs.mallet.base.types.MatrixOps;
import edu.umass.cs.mallet.base.types.SparseMatrixn;
import edu.umass.cs.mallet.base.types.tests.TestSerializable;
import edu.umass.cs.mallet.base.util.ArrayUtils;
import edu.umass.cs.mallet.base.util.Maths;
import edu.umass.cs.mallet.base.util.Random;
import edu.umass.cs.mallet.grmm.types.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.IOException;
import java.util.Set;

/**
 * Created: Aug 17, 2004
 *
 * @author <A HREF="mailto:casutton@cs.umass.edu>casutton@cs.umass.edu</A>
 * @version $Id: TestTableFactor.java,v 1.2 2006/05/30 23:09:59 casutton Exp $
 */
public class TestTableFactor extends TestCase {

  public TestTableFactor (String name)
  {
    super (name);
  }

  public void testMultiplyMultiplyBy ()
  {
    Variable var = new Variable (4);
    double[] vals = new double[]{ 2.0, 4.0, 6.0, 8.0 };
    double[] vals2 = new double [] { 0.5, 0.5, 0.5, 0.5 };

    double[] vals3 = new double [] { 1, 2, 3, 4, };
    TableFactor ans = new TableFactor (var, vals3);

    TableFactor ptl1 = new TableFactor (var, vals);
    TableFactor ptl2 = new TableFactor (var, vals2);
    Factor ptl3 = ptl1.multiply (ptl2);
    ptl1.multiplyBy (ptl2);

    assertTrue (ans.almostEquals (ptl1));
    assertTrue (ans.almostEquals (ptl3));
  }

  public void testEntropy ()
  {
    Variable v1 = new Variable (2);
    TableFactor ptl = new TableFactor (v1, new double[] { 0.3, 0.7 });

    double entropy = ptl.entropy ();
    assertEquals (0.61086, entropy, 1e-3);

    LogTableFactor logFactor = LogTableFactor.makeFromValues (v1, new double[] { 0.3, 0.7 });
    double entropy2 = logFactor.entropy ();
    assertEquals (0.61086, entropy2, 1e-3);
  }

  public void testSerialization () throws IOException, ClassNotFoundException
  {
    Variable v1 = new Variable (2);
    Variable v2 = new Variable (3);
    Variable[] vars = { v1, v2 };
    double[] vals = new double[]{ 2.0, 4.0, 6.0, 3, 5, 7 };
    TableFactor ptl = new TableFactor (vars, vals);
    TableFactor ptl2 = (TableFactor) TestSerializable.cloneViaSerialization (ptl);

    Set varset1 = ptl.varSet();
    Set varset2 = ptl2.varSet();
    assertTrue (!varset1.contains (varset2)); // Variables deep-cloned

    // There's not way to get directly at the matrices...!
    comparePotentialValues (ptl, ptl2);

    TableFactor marg1 = (TableFactor) ptl.marginalize (v1);
    TableFactor marg2 = (TableFactor) ptl2.marginalize (ptl2.findVariable (v1.getLabel ()));
    comparePotentialValues (marg1, marg2);
  }

  private void comparePotentialValues (TableFactor ptl, TableFactor ptl2)
  {
    AssignmentIterator it1 = ptl.assignmentIterator ();
    AssignmentIterator it2 = ptl2.assignmentIterator ();
    while (it1.hasNext ()) {
      assertTrue (ptl.value (it1) == ptl.value (it2));
      it1.advance (); it2.advance ();
    }
  }

  public void testSample ()
  {
    Variable v = new Variable (3);
    double[] vals = new double[] { 1, 3, 2 };
    TableFactor ptl = new TableFactor (v, vals);
    int[] sampled = new int [100];

    Random r = new Random (32423);
    for (int i = 0; i < sampled.length; i++) {
      sampled[i] = ptl.sampleLocation (r);
    }

    double sum = MatrixOps.sum (vals);
    double[] counts = new double [vals.length];
    for (int i = 0; i < vals.length; i++) {
      counts[i] = ArrayUtils.count (sampled, i);
    }

    MatrixOps.print (counts);
    for (int i = 0; i < vals.length; i++) {
      double prp = counts[i] / ((double) sampled.length);
      assertEquals (vals[i] / sum, prp, 0.1);
    }
  }

  public void testMarginalize ()
  {
    Variable[] vars = new Variable[] { new Variable (2), new Variable (2) };
    TableFactor ptl = new TableFactor (vars, new double[] { 1, 2, 3, 4});
    TableFactor ptl2 = (TableFactor) ptl.marginalize (vars[1]);
    assertEquals ("FAILURE: Potential has too many vars.\n  "+ptl2, 1, ptl2.varSet ().size ());
    assertTrue ("FAILURE: Potential does not contain "+vars[1]+":\n  "+ptl2, ptl2.varSet ().contains (vars[1]));

    double[] expected = new double[] { 4, 6 };
    assertTrue ("FAILURE: Potential has incorrect values.  Expected "+ArrayUtils.toString (expected)+"was "+ptl2,
          Maths.almostEquals (ptl2.toValueArray (), expected, 1e-5));
  }

  public void testMarginalizeOut ()
  {
    Variable[] vars = new Variable[] { new Variable (2), new Variable (2) };
    TableFactor ptl = new TableFactor (vars, new double[] { 1, 2, 3, 4});
    TableFactor ptl2 = (TableFactor) ptl.marginalizeOut (vars[0]);
    assertEquals ("FAILURE: Potential has too many vars.\n  "+ptl2, 1, ptl2.varSet ().size ());
    assertTrue ("FAILURE: Potential does not contain "+vars[1]+":\n  "+ptl2, ptl2.varSet ().contains (vars[1]));

    double[] expected = new double[] { 4, 6 };
    assertTrue ("FAILURE: Potential has incorrect values.  Expected "+ArrayUtils.toString (expected)+"was "+ptl2,
          Maths.almostEquals (ptl2.toValueArray (), expected, 1e-5));
  }

  public void testSparseMultiply ()
  {
    Variable[] vars = new Variable[] { new Variable (2), new Variable (2) };
    int[] szs = { 2, 2 };

    int[] idxs1 = new int[] { 0, 1, 3 };
    double[] vals1 = new double[]{ 2.0, 4.0, 8.0 };

    int[] idxs2 = new int[] { 0, 3 };
    double[] vals2 = new double [] { 0.5, 0.5 };

    double[] vals3 = new double [] { 1.0, 0, 4.0 };

    TableFactor ptl1 = new TableFactor (vars);
    ptl1.setValues (new SparseMatrixn (szs, idxs1, vals1));

    TableFactor ptl2 = new TableFactor (vars);
    ptl2.setValues (new SparseMatrixn (szs, idxs2, vals2));

    TableFactor ans = new TableFactor (vars);
    ans.setValues (new SparseMatrixn (szs, idxs1, vals3));

    Factor ptl3 = ptl1.multiply (ptl2);

    assertTrue ("Tast failed! Expected: "+ans+" Actual: "+ptl3, ans.almostEquals (ptl3));
  }

  public void testSparseDivide ()
  {
    Variable[] vars = new Variable[] { new Variable (2), new Variable (2) };
    int[] szs = { 2, 2 };

    int[] idxs1 = new int[] { 0, 1, 3 };
    double[] vals1 = new double[]{ 2.0, 4.0, 8.0 };

    int[] idxs2 = new int[] { 0, 3 };
    double[] vals2 = new double [] { 0.5, 0.5 };

    double[] vals3 = new double [] { 4.0, 0, 16.0 };

    TableFactor ptl1 = new TableFactor (vars);
    ptl1.setValues (new SparseMatrixn (szs, idxs1, vals1));

    TableFactor ptl2 = new TableFactor (vars);
    ptl2.setValues (new SparseMatrixn (szs, idxs2, vals2));

    TableFactor ans = new TableFactor (vars);
    ans.setValues (new SparseMatrixn (szs, idxs1, vals3));

    ptl1.divideBy (ptl2);

    assertTrue ("Tast failed! Expected: "+ans+" Actual: "+ptl1, ans.almostEquals (ptl1));
  }

  public void testSparseMarginalize ()
  {
    Variable[] vars = new Variable[] { new Variable (2), new Variable (2) };
    int[] szs = { 2, 2 };

    int[] idxs1 = new int[] { 0, 1, 3 };
    double[] vals1 = new double[]{ 2.0, 4.0, 8.0 };

    TableFactor ptl1 = new TableFactor (vars);
    ptl1.setValues (new SparseMatrixn (szs, idxs1, vals1));

    TableFactor ans = new TableFactor (vars[0], new double[] { 6, 8 });

    Factor ptl2 = ptl1.marginalize (vars[0]);

    assertTrue ("Tast failed! Expected: "+ans+" Actual: "+ptl2+" Orig: "+ptl1, ans.almostEquals (ptl2));
  }

  public void testSparseExtractMax ()
  {
    Variable[] vars = new Variable[] { new Variable (2), new Variable (2) };
    int[] szs = { 2, 2 };

    int[] idxs1 = new int[] { 0, 1, 3 };
    double[] vals1 = new double[]{ 2.0, 4.0, 8.0 };

    TableFactor ptl1 = new TableFactor (vars);
    ptl1.setValues (new SparseMatrixn (szs, idxs1, vals1));

    TableFactor ans = new TableFactor (vars[0], new double[] { 4, 8 });

    Factor ptl2 = ptl1.extractMax (vars[0]);

    assertTrue ("Tast failed! Expected: "+ans+" Actual: "+ptl2+ "Orig: "+ptl1, ans.almostEquals (ptl2));
  }

  public void testLogSample ()
  {
    Variable v = new Variable (2);
    double[] vals = new double[] { -30, 0 };
    LogTableFactor ptl = LogTableFactor.makeFromLogValues (v, vals);
    int idx = ptl.sampleLocation (new Random (43));
    assertEquals (1, idx);
  }

  public void testExp ()
  {
    Variable var = new Variable (4);
    double[] vals = new double[] {2.0, 4.0, 6.0, 8.0};
    double[] vals3 = new double [] { 4.0, 16.0, 36.0, 64.0 };
    TableFactor ans = new TableFactor (var, vals3);

    TableFactor ptl1 = new TableFactor (var, vals);
    ptl1.exponentiate (2.0);

    assertTrue ("Error: expected "+ans.dump ()+" but was "+ptl1.dump (), ptl1.almostEquals (ans));
  }

  public void testPlusEquals ()
  {
    Variable var = new Variable (4);
    double[] vals = new double[]{ 2.0, 4.0, 6.0, 8.0 };

    TableFactor factor = new TableFactor (var, vals);
    factor.plusEquals (0.1);

    double[] expected = new double[] { 2.1, 4.1, 6.1, 8.1 };
    TableFactor ans = new TableFactor (var, expected);

    assertTrue ("Error: expected "+ans.dump()+" but was "+factor.dump(), factor.almostEquals (ans));
  }

  public static Test suite ()
  {
    return new TestSuite (TestTableFactor.class);
  }

  public static void main (String[] args) throws Throwable
  {
    TestSuite theSuite;
    if (args.length > 0) {
      theSuite = new TestSuite ();
      for (int i = 0; i < args.length; i++) {
        theSuite.addTest (new TestTableFactor (args[i]));
      }
    } else {
      theSuite = (TestSuite) suite ();
    }

    junit.textui.TestRunner.run (theSuite);
  }

}
