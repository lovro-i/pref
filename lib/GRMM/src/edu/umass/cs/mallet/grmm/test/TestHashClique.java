/* Copyright (C) 2003 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */
package edu.umass.cs.mallet.grmm.test;

import junit.framework.*;
import edu.umass.cs.mallet.grmm.types.Variable;
import edu.umass.cs.mallet.grmm.types.HashVarSet;

/**
 * Created: Aug 22, 2005
 *
 * @author <A HREF="mailto:casutton@cs.umass.edu>casutton@cs.umass.edu</A>
 * @version $Id: TestHashClique.java,v 1.2 2006/02/03 04:25:32 casutton Exp $
 */
public class TestHashClique extends TestCase {

  public TestHashClique (String name)
  {
    super (name);
  }

  public void testEqualsHashCode ()
  {
    Variable[] vars = new Variable [4];
    for (int i = 0; i < vars.length; i++) {
      vars[i] = new Variable(3);
    }

    HashVarSet c1 = new HashVarSet (vars);
    HashVarSet c2 = new HashVarSet (vars);

    assertTrue(c1.equals (c2));
    assertTrue(c2.equals (c1));
    assertEquals (c1.hashCode(), c2.hashCode ());
  }

  public static Test suite ()
  {
    return new TestSuite (TestHashClique.class);
  }

  public static void main (String[] args) throws Throwable
  {
    TestSuite theSuite;
    if (args.length > 0) {
      theSuite = new TestSuite ();
      for (int i = 0; i < args.length; i++) {
        theSuite.addTest (new TestHashClique (args[i]));
      }
    } else {
      theSuite = (TestSuite) suite ();
    }

    junit.textui.TestRunner.run (theSuite);
  }

}
