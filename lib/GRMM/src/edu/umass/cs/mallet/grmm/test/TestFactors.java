/* Copyright (C) 2003 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */
package edu.umass.cs.mallet.grmm.test;

import junit.framework.*;
import edu.umass.cs.mallet.grmm.types.Variable;
import edu.umass.cs.mallet.grmm.types.TableFactor;
import edu.umass.cs.mallet.grmm.types.Factors;
import edu.umass.cs.mallet.grmm.types.*;

/**
 * Created: Mar 28, 2005
 *
 * @author <A HREF="mailto:casutton@cs.umass.edu>casutton@cs.umass.edu</A>
 * @version $Id: TestFactors.java,v 1.4 2006/05/25 05:53:48 casutton Exp $
 */
public class TestFactors extends TestCase {

  public TestFactors (String name)
  {
    super (name);
  }

  public void testNormalizeAsCpt ()
  {
    double[] vals = { 1, 4, 2, 6 };
    Variable v1 = new Variable (2);
    Variable v2 = new Variable (2);
    TableFactor ptl = new TableFactor (new Variable[] {v1, v2}, vals);
    Factors.normalizeAsCpt (ptl, v1);

    comparePotentials (ptl, new double[] { 0.3333, 0.4, 0.6666, 0.6 });
  }

  public void testNormalizeAsCptLogSpace ()
  {
    double[] vals = { 0.0, 1.3862943611198906, 0.6931471805599453, 1.791759469228055 };
    Variable v1 = new Variable (2);
    Variable v2 = new Variable (2);
    AbstractTableFactor ptl = LogTableFactor.makeFromLogValues(new Variable[] { v1, v2 }, vals);

    System.out.println (ptl);
    Factors.normalizeAsCpt (ptl, v1);
    System.out.println (ptl);

    comparePotentials (ptl, new double[] { 0.3333, 0.4, 0.6666, 0.6 });
//    comparePotentials (ptl, new double[] { -1.098712293668443, -0.916290731874155, -0.4055651131084978, -0.5108256237659907 });
  }

  private void comparePotentials (DiscreteFactor ptl, double[] expected)
  {
    double[] actual = ptl.toValueArray ();
    assertEquals (expected.length, actual.length);
    for (int i = 0; i < expected.length; i++) {
      assertEquals (expected[i], actual[i], 0.001);
    }
  }

  public void testRetainMass ()
  {
    Variable v = new Variable (4);
    LogTableFactor ptl = LogTableFactor.makeFromValues (v, new double[] { 0.75, 0, 0.05, 0.2 });

    TableFactor actual = Factors.retainMass (ptl, 0.9);
    System.out.println (actual);

//    comparePotentials (actual, new double[] { Math.log (0.75), Math.log (0.2) });
  }

  public void testOneVarSlice ()
  {
    double[] vals = { 0.0, 1.3862943611198906, 0.6931471805599453, 1.791759469228055 };
    Variable v1 = new Variable (2);
    Variable v2 = new Variable (2);
    Factor ptl = LogTableFactor.makeFromLogValues(new Variable[] { v1, v2 }, vals);

    Assignment assn = new Assignment (v1, 0);
    Factor sliced = Factors.slice (v2, ptl, assn);

    comparePotentials ((AbstractTableFactor) sliced, new double[] { 1.0, 4.0 });
//    comparePotentials ((AbstractTableFactor) sliced, new double[] { 0, 1.386294 });
  }

  public void testTwoVarSlice ()
  {
    double[] vals = { 0.0, 1, 2, 3, 4, 5, 6, 7 };
    Variable v1 = new Variable (2);
    Variable v2 = new Variable (2);
    Variable v3 = new Variable (2);
    Factor ptl = LogTableFactor.makeFromValues(new Variable[] { v1, v2, v3 }, vals);

    Assignment assn = new Assignment (v3, 0);
    Factor sliced = Factors.slice (new Variable[] { v1, v2 }, ptl, assn);

    comparePotentials ((AbstractTableFactor) sliced, new double[] { 0, 2, 4, 6 });
  }

  public void testMultiVarSlice ()
  {
    double[] vals = { 0.0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 };
    Variable v1 = new Variable (2);
    Variable v2 = new Variable (2);
    Variable v3 = new Variable (2);
    Variable v4 = new Variable (2);
    Factor ptl = LogTableFactor.makeFromValues(new Variable[] { v1, v2, v3, v4 }, vals);

    System.out.println (ptl);
    Assignment assn = new Assignment (v4, 0);
    Factor sliced = Factors.slice (new Variable[] { v1, v2, v3 }, ptl, assn);
    System.out.println (new TableFactor ((AbstractTableFactor) sliced));

    comparePotentials ((AbstractTableFactor) sliced, new double[] { 0, 2, 4, 6, 8, 10, 12, 14 });
  }

  public void testMutualInfo1 ()
  {
    Factor ptl1 = new TableFactor (new Variable (2), new double[] { 0.7, 0.3 });
    Factor ptl2 = new TableFactor (new Variable (2), new double[] { 0.2, 0.8 });
    Factor joint = ptl1.multiply (ptl2);
    assertEquals (0.0, Factors.mutualInformation (joint), 1e-5);
  }

  public void testMutualInfo2 ()
  {
    Variable[] vars = new Variable[]  { new Variable (2), new Variable (2) };
    Factor joint = new TableFactor (vars, new double[] { 0.3, 0.2, 0.1, 0.4 });
    System.out.println (joint.dump ());
    assertEquals (0.08630462, Factors.mutualInformation (joint), 1e-5);
  }

  public void testMix ()
  {
    Variable var = new Variable (2);
    AbstractTableFactor tf = new TableFactor (var, new double[] { 0.3, 0.7 });
    AbstractTableFactor ltf = LogTableFactor.makeFromValues (var, new double[] { 0.5, 0.5 });

    Factor mix = Factors.mix (tf, ltf, 0.5);
    AbstractTableFactor ans = new TableFactor (var, new double[] { 0.4, 0.6 });
    assertTrue (ans.almostEquals (mix));
  }

  public static Test suite ()
  {
    return new TestSuite (TestFactors.class);
  }

  public static void main (String[] args) throws Throwable
  {
    TestSuite theSuite;
    if (args.length > 0) {
      theSuite = new TestSuite ();
      for (int i = 0; i < args.length; i++) {
        theSuite.addTest (new TestFactors (args[i]));
      }
    } else {
      theSuite = (TestSuite) suite ();
    }

    junit.textui.TestRunner.run (theSuite);
  }

}
