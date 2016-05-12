/* Copyright (C) 2003 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */
package edu.umass.cs.mallet.grmm.test;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import edu.umass.cs.mallet.grmm.types.Variable;
import edu.umass.cs.mallet.grmm.types.TableFactor;
import edu.umass.cs.mallet.grmm.types.*;

/**
 * Created: Aug 11, 2004
 *
 * @author <A HREF="mailto:casutton@cs.umass.edu>casutton@cs.umass.edu</A>
 * @version $Id: TestAssignmentIterator.java,v 1.3 2006/01/05 20:44:35 casutton Exp $
 */
public class TestAssignmentIterator extends TestCase {

  /**
   * Constructs a test case with the given name.
   */
  public TestAssignmentIterator (String name)
  {
    super (name);
  }

  public void testSum ()
  {
    Variable vars [] = {
      new Variable (2),
      new Variable (2),
    };
    double[] probs = { 0.1, 10.3, 17, 0.5 };
    TableFactor ptl = new TableFactor (vars, probs);

    AssignmentIterator it = ptl.assignmentIterator ();
    double total = 0;
    while (it.hasNext ()) {
      total += ptl.value (it);
      it.advance ();
    }

    assertEquals (27.9, total, 0.01);
  }


  public void testLazyAssignment ()
  {
    Variable vars [] = {
      new Variable (2),
      new Variable (2),
    };
    double[] probs = { 0.1, 10.3, 17, 0.5 };
    TableFactor ptl = new TableFactor (vars, probs);

    AssignmentIterator it = ptl.assignmentIterator ();
    it.advance ();
    it.advance ();

    Assignment assn = it.assignment ();
    assertEquals (2, assn.size ());   
    assertEquals (1, assn.get (vars [0]));
    assertEquals (0, assn.get (vars [1]));
  }

  public static Test suite()
  {
    return new TestSuite(TestAssignmentIterator.class);
  }


  public static void main(String[] args) throws Exception
  {
    TestSuite theSuite;
    if (args.length > 0) {
      theSuite = new TestSuite();
      for (int i = 0; i < args.length; i++) {
        theSuite.addTest(new TestAssignmentIterator(args[i]));
      }
    } else {
      theSuite = (TestSuite) suite();
    }

    junit.textui.TestRunner.run(theSuite);
  }

}
