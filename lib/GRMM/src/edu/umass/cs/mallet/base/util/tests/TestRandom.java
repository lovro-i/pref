/* Copyright (C) 2006 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://mallet.cs.umass.edu/
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */
package edu.umass.cs.mallet.base.util.tests;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import edu.umass.cs.mallet.base.util.Strings;
import edu.umass.cs.mallet.base.util.Random;
import edu.umass.cs.mallet.base.types.MatrixOps;

/**
 * Created: Jan 19, 2005
 *
 * @author <A HREF="mailto:casutton@cs.umass.edu>casutton@cs.umass.edu</A>
 * @version $Id: TestRandom.java,v 1.1 2006/03/20 23:07:07 casutton Exp $
 */
public class TestRandom extends TestCase {

  public TestRandom (String name)
  {
    super (name);
  }

  public static Test suite ()
  {
    return new TestSuite (TestRandom.class);
  }

  public static void testAsJava ()
  {
    Random mRand = new Random ();
    java.util.Random jRand = mRand.asJavaRandom ();

    int size = 10000;
    double[] vals = new double [size];
    for (int i = 0; i < size; i++) {
      vals[i] = jRand.nextGaussian ();
    }

    assertEquals (0.0, MatrixOps.mean (vals), 0.01);
    assertEquals (1.0, MatrixOps.stddev (vals), 0.01);
  }

  public static void main (String[] args) throws Throwable
  {
    TestSuite theSuite;
    if (args.length > 0) {
      theSuite = new TestSuite ();
      for (int i = 0; i < args.length; i++) {
        theSuite.addTest (new TestRandom (args[i]));
      }
    } else {
      theSuite = (TestSuite) TestRandom.suite ();
    }

    junit.textui.TestRunner.run (theSuite);
  }

}
