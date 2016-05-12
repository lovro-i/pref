/* Copyright (C) 2003 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */
package edu.umass.cs.mallet.grmm.inference;

import edu.umass.cs.mallet.base.types.MatrixOps;
import edu.umass.cs.mallet.grmm.types.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.List;

/**
 * Approximate inferencer for graphical models using sampling.
 *  A general inference engine that takes any Sampler engine, and performs
 *  approximate inference using its samples.
 * Created: Mar 28, 2005
 *
 * @author <A HREF="mailto:casutton@cs.umass.edu>casutton@cs.umass.edu</A>
 * @version $Id: SamplingInferencer.java,v 1.5 2006/02/03 04:25:32 casutton Exp $
 */
public class SamplingInferencer extends AbstractInferencer {

  private int N;

  private Sampler sampler;

  // Could save only sufficient statistics to save on memory
  transient List samples;

  public SamplingInferencer (Sampler sampler, int n)
  {
    this.sampler = sampler;
    N = n;
  }

  public void computeMarginals (FactorGraph mdl)
  {
    samples = sampler.sample (mdl, N);
  }

  public Factor lookupMarginal (Variable var)
  {
    double[] counts = new double [var.getNumOutcomes ()];
    Iterator it = samples.iterator ();
    while (it.hasNext()) {
      Assignment assn = (Assignment) it.next ();
      int value = assn.get (var);
      counts[value]++;
    }
    double sum = MatrixOps.sum (counts);
    MatrixOps.timesEquals (counts, 1 / sum);
    return new TableFactor (var, counts);
  }

  // don't try this for large cliques
  public Factor lookupMarginal (VarSet varSet)
  {
    double[] counts = new double [varSet.weight ()];
    Iterator it = samples.iterator ();
    while (it.hasNext()) {
      Assignment assn = (Assignment) it.next ();
      int value = Assignment.restriction (assn, varSet).singleIndex ();
      counts[value]++;
    }
    double sum = MatrixOps.sum (counts);
    MatrixOps.timesEquals (counts, 1 / sum);
    return new TableFactor (varSet, counts);
  }
  
  // Serialization garbage

  private static final long serialVersionUID = 1;
  private static final int CURRENT_SERIAL_VERSION = 1;

  private void writeObject (ObjectOutputStream out) throws IOException
  {
    out.writeInt (CURRENT_SERIAL_VERSION);
    out.writeInt (N);
    out.writeObject (sampler);
  }


  private void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException
  {
    in.readInt ();  // read version
    N = in.readInt ();
    sampler = (Sampler) in.readObject ();
  }

}
