/* Copyright (C) 2003 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */
package edu.umass.cs.mallet.grmm.inference;

import edu.umass.cs.mallet.grmm.types.*;
import edu.umass.cs.mallet.base.util.Random;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Computes an exact sample from the distribution of a given factor graph by forming
 *  a junction tree.
 *
 * Created: Nov 9, 2005
 *
 * @author <A HREF="mailto:casutton@cs.umass.edu>casutton@cs.umass.edu</A>
 * @version $Id: ExactSampler.java,v 1.5 2006/02/03 04:25:32 casutton Exp $
 */
public class ExactSampler implements Sampler {

  Random r;

  public ExactSampler ()
  {
    this (new Random ());
  }

  public ExactSampler (Random r)
  {
    this.r = r;
  }

  public List sample (FactorGraph mdl, int N)
  {
    JunctionTreeInferencer jti = new JunctionTreeInferencer ();
    jti.computeMarginals (mdl);
    JunctionTree jt = jti.lookupJunctionTree ();

    List assns = new ArrayList (N);
    for (int i = 0; i < N; i++) {
      assns.add (sampleOneAssn (jt));
    }

    return assns;
  }

  private Assignment sampleOneAssn (JunctionTree jt)
  {
    Assignment assn = new Assignment ();
    VarSet root = (VarSet) jt.getRoot ();
    sampleAssignmentRec (jt, assn, root);
    return assn;
  }

  private void sampleAssignmentRec (JunctionTree jt, Assignment assn, VarSet varSet)
  {
    DiscreteFactor marg = (DiscreteFactor) jt.getCPF (varSet);
    DiscreteFactor slice = (DiscreteFactor) Factors.slice (marg, assn);
    Assignment sampled = slice.sample (r);
    assn.setValues (sampled);
    for (Iterator it = jt.getChildren (varSet).iterator(); it.hasNext();) {
      VarSet child = (VarSet) it.next ();
      sampleAssignmentRec (jt, assn, child);
    }
  }

  public void setRandom (Random r)
  {
    this.r = r;
  }
}
