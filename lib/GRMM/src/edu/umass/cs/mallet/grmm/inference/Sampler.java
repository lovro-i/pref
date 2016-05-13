/* Copyright (C) 2003 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */
package edu.umass.cs.mallet.grmm.inference;

import edu.umass.cs.mallet.grmm.types.FactorGraph;
import edu.umass.cs.mallet.base.util.Random;

import java.util.List;

/**
 * Interface for methods from sampling the distribution given by a graphical
 *  model.
 *
 * Created: Mar 28, 2005
 *
 * @author <A HREF="mailto:casutton@cs.umass.edu>casutton@cs.umass.edu</A>
 * @version $Id: Sampler.java,v 1.3 2006/01/04 21:25:56 casutton Exp $
 */
public interface Sampler {

  /**
   * Samples from the distribution of a given undirected model.
   * @param mdl Model to sample from
   * @param N Number of samples to generate
   * @return A list of assignments to the model.
   */
  public List sample (FactorGraph mdl, int N);

  /**
   * Sets the random seed used by this sampler.
   * @param r Random object to be used by this sampler.
   */
  public void setRandom (Random r);
  
}
