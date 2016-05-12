/* Copyright (C) 2003 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */
package edu.umass.cs.mallet.grmm.types;

import java.util.Collection;
import java.util.HashSet;
import java.util.Random;

/**
 * Created: Sep 12, 2005
 *
 * @author <A HREF="mailto:casutton@cs.umass.edu>casutton@cs.umass.edu</A>
 * @version $Id: AbstractFactor.java,v 1.6 2006/02/03 04:25:32 casutton Exp $
 */
public abstract class AbstractFactor implements Factor {

  VarSet vars;

  protected AbstractFactor (VarSet vars)
  {
    this.vars = vars;
  }

  protected abstract Factor extractMaxInternal (VarSet varSet);

  protected abstract double lookupValueInternal (int i);

  protected abstract Factor marginalizeInternal (VarSet varsToKeep);

  public double value (Assignment assn)
  {
    return lookupValueInternal (assn.singleIndex ());
  }


  public double phi (DenseAssignmentIterator it)
  {
    return lookupValueInternal (it.indexOfCurrentAssn ());
  }

  public Factor marginalize (Variable vars[])
  {
    return marginalizeInternal (new HashVarSet (vars));
  }

  public Factor marginalize (Collection vars)
  {
    return marginalizeInternal (new HashVarSet (vars));
  }

  public Factor marginalize (Variable var)
  {
    return marginalizeInternal (new HashVarSet (new Variable[] { var }));
  }

  public Factor marginalizeOut (Variable var)
  {
    HashSet vars = new HashSet (this.vars);
    vars.remove (var);
    return marginalizeInternal (new HashVarSet (new Variable[] { var }));
  }

  public Factor extractMax (Variable vars[])
  {
    return extractMaxInternal (new HashVarSet (vars));
  }

  public Factor extractMax (Collection vars)
  {
    return extractMaxInternal (new HashVarSet (vars));
  }

  public Factor extractMax (Variable var)
  {
    return extractMaxInternal (new HashVarSet (new Variable[] { var }));
  }


  // xxx should return an Assignment
  public int argmax ()
  {
    return 0;
  }

  //xxx Should return an Assigment
  public int sample (Random r)
  {
    return 0;
  }

  public double sum ()
  {
    return 0;
  }

  public double entropy ()
  {
    return 0;
  }

  public Factor multiply (Factor dist)
  {
    return null;
  }

  public void multiplyBy (Factor pot)
  {

  }

  public void exponentiate (double power)
  {

  }

  public void divideBy (Factor pot)
  {

  }

  public boolean isInLogSpace ()
  {
    return false;
  }

  public void logify ()
  {

  }

  public void delogify ()
  {

  }

  public Factor log ()
  {
    return null;
  }

  public boolean containsVar (Variable var)
  {
    return vars.contains (var);
  }

  public VarSet varSet ()
  {
    return vars;
  }

  public AssignmentIterator assignmentIterator ()
  {
    return null;
  }

  public boolean almostEquals (Factor p)
  {
    return false;
  }

  public boolean almostEquals (Factor p, double epsilon)
  {
    return false;
  }

  public Factor duplicate ()
  {
    return null;
  }

  public boolean isNaN ()
  {
    return false;
  }

  public double[] toArray ()
  {
    return new double[0];
  }

  public double logphi (DenseAssignmentIterator it)
  {
    return 0;
  }

  public double logValue (Assignment assn)
  {
    return 0;
  }

  public double logValue (int loc)
  {
    return 0;
  }

  public double valueAtLocation (int loc)
  {
    return 0;
  }

  public int indexAtLocation (int loc)
  {
    return 0;
  }

  public int numLocations ()
  {
    return 0;
  }

  public Variable getVariable (int i)
  {
    return null;
  }
}
