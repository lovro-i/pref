/* Copyright (C) 2006 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://mallet.cs.umass.edu/
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */
package edu.umass.cs.mallet.grmm.test;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import edu.umass.cs.mallet.grmm.types.Variable;
import edu.umass.cs.mallet.grmm.types.TableFactor;
import edu.umass.cs.mallet.grmm.types.AssignmentIterator;
import edu.umass.cs.mallet.grmm.types.Assignment;

/**
 * Created: Aug 11, 2004
 *
 * @author <A HREF="mailto:casutton@cs.umass.edu>casutton@cs.umass.edu</A>
 * @version $Id: TestAssignment.java,v 1.1 2006/03/14 22:01:35 casutton Exp $
 */
public class TestAssignment extends TestCase {

  /**
   * Constructs a test case with the given name.
   */
  public TestAssignment (String name)
  {
    super (name);
  }

  public void testSimple ()
  {
    Variable vars [] = {
      new Variable (2),
      new Variable (2),
    };

    Assignment assn = new Assignment (vars, new int[] { 1, 0 });
    assertEquals (1, assn.get (vars [0]));
    assertEquals (0, assn.get (vars [1]));
  }

  public static Test suite()
  {
    return new TestSuite (TestAssignment.class);
  }


  public static void main(String[] args) throws Exception
  {
    TestSuite theSuite;
    if (args.length > 0) {
      theSuite = new TestSuite();
      for (int i = 0; i < args.length; i++) {
        theSuite.addTest(new TestAssignment (args[i]));
      }
    } else {
      theSuite = (TestSuite) TestAssignment.suite ();
    }

    junit.textui.TestRunner.run(theSuite);
  }

}
