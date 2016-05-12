/* Copyright (C) 2003 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */

package edu.umass.cs.mallet.grmm.inference;

import edu.umass.cs.mallet.grmm.types.Factor;
import edu.umass.cs.mallet.grmm.types.TableFactor;
import edu.umass.cs.mallet.grmm.types.FactorGraph;
import edu.umass.cs.mallet.grmm.types.Variable;
import edu.umass.cs.mallet.grmm.inference.AbstractInferencer;

import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import java.util.Collection;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;



/**
 * The variable elimination algorithm for inference in graphical
 *  models.
 *
 * Created: Mon Sep 22 17:34:00 2003
 *
 * @author <a href="mailto:casutton@cs.umass.edu">Charles Sutton</a>
 * @version $Id: VariableElimination.java,v 1.5 2006/02/03 04:25:32 casutton Exp $
 */

public class VariableElimination 
	extends AbstractInferencer
{

	private Factor eliminate (Collection allPhi, Variable node) {
			
		HashSet phiSet = new HashSet();	    
		
		/* collect the potentials that include this variable */
		for (Iterator j = allPhi.iterator(); j.hasNext(); ) {
			Factor cpf = (Factor) j.next ();
			if (cpf.containsVar (node)) {
				phiSet.add (cpf);
				j.remove ();
			}
		}

		return TableFactor.multiplyAll (phiSet);
	}

	/**
	 * The bulk of the variable-elimination algorithm. Returns the
	 *  marginal density of the variable QUERY in the undirected
	 *  model MODEL, except that the density is un-normalized.
	 *  The normalization is done in a separate function to make
	 *  computeNormalizationFactor easier.
	 */
	private Factor unnormalizedQuery
	                             (FactorGraph model, Variable query)
	{
		/* here the elimination order is random */
		/* note that using buckets would make this more efficient as
			 well. */

		/* make a copy of potentials */
		HashSet allPhi = new HashSet();
		for (Iterator i = model.factorsIterator (); i.hasNext(); ){
			TableFactor cpf = (TableFactor) i.next ();
			allPhi.add(cpf.clone());
		}

		Set nodes = model.variablesSet ();

		/* Eliminate each node in turn */
		for (Iterator i = nodes.iterator(); i.hasNext(); ) {
	    Variable node = (Variable) i.next();
			if (node == query) continue; // Eliminate the query variable last!

			Factor newCPF = eliminate (allPhi, node);

	    /* Extract (marginalize) over this variables */
			Factor singleCPF;
			if(newCPF.varSet().size() == 1) {
				singleCPF = newCPF;
			} else {
				singleCPF = newCPF.marginalizeOut (node);  				
			}
				
			/* add it back to the list of potentials */
			allPhi.add(singleCPF); 
			
		}

		/* Now, all the potentials that are left should contain only the
		 * query variable.... UNLESS the graph is disconnected.  So just
		 * eliminate the query var. 
		 */
		Factor marginal = eliminate (allPhi, query);
		assert marginal.containsVar (query);
		assert marginal.varSet().size() == 1;

		return marginal;
	}

  /** 
   * Computes the normalization constant for a model.
	 */
	public double computeNormalizationFactor (FactorGraph m) {
		/* What we'll do is get the unnormalized marginal of an arbitrary
		 *  node; then sum the marginal to get the normalization factor. */
		Variable var = (Variable) m.variablesSet ().iterator().next();
		Factor marginal = unnormalizedQuery (m, var);
		return marginal.sum ();
	}

	transient FactorGraph mdlCurrent;

	// Inert. All work done in lookupMarginal().
	public void computeMarginals (FactorGraph m)
	{
		mdlCurrent = m;
	}

	public Factor lookupMarginal (Variable var)
	{
		Factor marginal = unnormalizedQuery (mdlCurrent, var);
		marginal.normalize();
		return marginal;
	}

  // Serialization
  private static final long serialVersionUID = 1;

  // If seralization-incompatible changes are made to these classes,
  //  then smarts can be added to these methods for backward compatibility.
  private void writeObject (ObjectOutputStream out) throws IOException {
     out.defaultWriteObject ();
   }

  private void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException {
     in.defaultReadObject ();
  }

}

