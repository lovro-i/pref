/* Copyright (C) 2003 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */
package edu.umass.cs.mallet.grmm.inference;

//import edu.umass.cs.mallet.users.casutton.util.Timing;

import edu.umass.cs.mallet.base.util.Timing;
import edu.umass.cs.mallet.base.util.Random;
import edu.umass.cs.mallet.grmm.types.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

/**
 * Created: Mar 28, 2005
 *
 * @author <A HREF="mailto:casutton@cs.umass.edu>casutton@cs.umass.edu</A>
 * @version $Id: GibbsSampler.java,v 1.9 2006/02/03 04:25:32 casutton Exp $
 */
public class GibbsSampler implements Sampler {

  private int burnin;

  private Factor[] allCpts;

  private Random r = new Random (324231);

  public GibbsSampler () {}

  public GibbsSampler (int burnin)
  {
    this.burnin = burnin;
  }

  public GibbsSampler (Random r, int burnin)
  {
    this.burnin = burnin;
    this.r = r;
  }

  public void setBurnin (int burnin)
  {
    this.burnin = burnin;
  }

  public void setRandom (Random r)
  {
    this.r = r;
  }

  public List sample (FactorGraph mdl, int N)
  {
//    initForGraph (mdl);
    Assignment assn = initialAssignment (mdl);

    Timing timing = new Timing ();
    for (int i = 0; i < burnin; i++) {
      assn = doOnePass (mdl, assn);
    }
    timing.tick ("Burnin");

    List ret = new ArrayList (N);
    for (int i = 0; i < N; i++) {
      assn = doOnePass (mdl, assn);
      ret.add (assn);
    }
    timing.tick ("Sampling");

    return ret;
  }

  private Assignment initialAssignment (FactorGraph mdl)
  {
    int size = mdl.numVariables ();
    return new Assignment (mdl, new int [size]);
  }

  private Assignment doOnePass (FactorGraph mdl, Assignment initial)
  {
    Assignment ret = initial.duplicate ();
    for (int vidx = 0; vidx < ret.size (); vidx++) {
      Variable var = mdl.get (vidx);
      DiscreteFactor subcpt = constructConditionalCpt (mdl, var, ret);
      int value = subcpt.sampleLocation (r);
      ret.setValue (var, value);
    }

    return ret;
  }

   // Warning: destructively modifies ret's assignment to fullAssn (I could save and restore, but I don't care
  private DiscreteFactor constructConditionalCpt (FactorGraph mdl, Variable var, Assignment fullAssn)
  {
    List ptlList = mdl.allFactorsOfVar (var);
    LogTableFactor ptl = new LogTableFactor (var);
    for (AssignmentIterator it = ptl.assignmentIterator (); it.hasNext(); it.advance ()) {
      Assignment varAssn = it.assignment ();
      fullAssn.setValue (var, varAssn.get (var));
      ptl.setRawValue (varAssn, sumValues (ptlList, fullAssn));
    }
    ptl.normalize ();
    return ptl;
  }

  private double sumValues (List ptlList, Assignment assn)
  {
    double sum = 0;
    for (Iterator it = ptlList.iterator (); it.hasNext ();) {
      Factor ptl = (Factor) it.next ();
      sum += ptl.logValue (assn);
    }
    return sum;
  }

}
