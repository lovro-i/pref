/* Copyright (C) 2003 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */
package edu.umass.cs.mallet.grmm.inference;

import edu.umass.cs.mallet.grmm.types.*;
import edu.umass.cs.mallet.base.types.MatrixOps;

import java.util.Iterator;

/**
 * A bunch of static utilities useful for dealing with Inferencers.
 * Created: Jun 1, 2005
 *
 * @author <A HREF="mailto:casutton@cs.umass.edu>casutton@cs.umass.edu</A>
 * @version $Id: Utils.java,v 1.5 2006/02/03 04:25:32 casutton Exp $
 */
public class Utils {

  /**
   * Returns ths value of -log Z in mdl according to the given inferencer.
   * If inf is exact, the answer will be exact; otherwise the answer will be
   * approximation
   *
   * @param mdl
   * @param inf An inferencer.  <tt>inf.computeMarginals (mdl)</tt> must already have
   *            been called.
   * @return The value of -logZ
   */
  public static double lookupMinusLogZ (FactorGraph mdl, Inferencer inf)
  {
    Assignment assn = new Assignment (mdl, new int[mdl.numVariables ()]);
    double prob = inf.lookupLogJoint (assn);
    double energy = mdl.logProduct (assn);
    return prob - energy;
  }

  public static double localMagnetization (Inferencer inferencer, Variable var)
  {
    if (var.getNumOutcomes () != 2)
      throw new IllegalArgumentException ();

    Factor marg = inferencer.lookupMarginal (var);
    AssignmentIterator it = marg.assignmentIterator ();
    double v1 = marg.value (it); it.advance ();
    double v2 = marg.value (it);
    return v1 - v2;
  }

  public static double[] allL1MarginalDistance (FactorGraph mdl, Inferencer inf1, Inferencer inf2)
  {
    double[] dist = new double [mdl.numVariables ()];

    int i = 0;
    for (Iterator it = mdl.variablesIterator (); it.hasNext();) {
      Variable var = (Variable) it.next ();
      Factor bel1 = inf1.lookupMarginal (var);
      Factor bel2 = inf2.lookupMarginal (var);
      dist[i++] = Factors.oneDistance (bel1, bel2);
    }

    return dist;
  }

  public static double avgL1MarginalDistance (FactorGraph mdl, Inferencer inf1, Inferencer inf2)
  {
    double[] dist = allL1MarginalDistance (mdl, inf1, inf2);
    return MatrixOps.mean (dist);
  }

  public static double maxL1MarginalDistance (FactorGraph mdl, Inferencer inf1, Inferencer inf2)
  {
    double[] dist = allL1MarginalDistance (mdl, inf1, inf2);
    return MatrixOps.max (dist);
  }

}
