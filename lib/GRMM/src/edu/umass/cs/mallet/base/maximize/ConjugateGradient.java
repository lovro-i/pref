/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */




/** 
   @author Andrew McCallum <a href="mailto:mccallum@cs.umass.edu">mccallum@cs.umass.edu</a>
 */

package edu.umass.cs.mallet.base.maximize;

import edu.umass.cs.mallet.base.maximize.LineMaximizer;
import edu.umass.cs.mallet.base.maximize.Maximizable;
import edu.umass.cs.mallet.base.maximize.tests.TestMaximizable;
import edu.umass.cs.mallet.base.types.MatrixOps;
import edu.umass.cs.mallet.base.util.MalletLogger;
import java.util.logging.*;

// Conjugate Gradient, Polak and Ribiere version
// from "Numeric Recipes in C", Section 10.6.

public class ConjugateGradient implements Maximizer.ByGradient
{
	private static Logger logger = MalletLogger.getLogger(ConjugateGradient.class.getName());

	// xxx If this is too big, we can get inconsistent value and gradient in MaxEntTrainer
	// Investigate!!!
	double initialStepSize = 0.01;
	double tolerance = 0.0001;
	int maxIterations = 1000;

	//	LineMaximizer lineMaximizer = new GradientBracketLineMaximizer ();
	LineMaximizer.ByGradient lineMaximizer = new BackTrackLineSearch ();

	// "eps" is a small number to recitify the special case of converging
	// to exactly zero function value
	final double eps = 1.0e-10;

	public ConjugateGradient (double initialStepSize)
	{
		this.initialStepSize = initialStepSize;
	}

	public ConjugateGradient ()
	{
	}

	public void setInitialStepSize (double initialStepSize) { this.initialStepSize = initialStepSize; }
	public double getInitialStepSize () { return this.initialStepSize; }
  public double getStepSize () { return step; }

  // The state of a conjugate gradient search
	double fp, gg, gam, dgg, step, fret;
	double[] xi, g, h;
	int j, iterations;

	public boolean maximize (Maximizable.ByGradient maxable)
	{
		return maximize (maxable, maxIterations);
	}

	public void setTolerance(double t) {
		tolerance = t;
	}

	public boolean maximize (Maximizable.ByGradient maxable, int numIterations)
	{
    int n = maxable.getNumParameters();
    double prevStepSize = initialStepSize;
    boolean searchingGradient = true;
    if (xi == null) {
			fp = maxable.getValue ();
			xi = new double[n];
			g = new double[n];
			h = new double[n];
			maxable.getValueGradient (xi);
			System.arraycopy (xi, 0, g, 0, n);
			System.arraycopy (xi, 0, h, 0, n);
			step = initialStepSize;
      iterations = 0;
		}

		for (int iterationCount = 0; iterationCount < numIterations; iterationCount++) {
			logger.info ("ConjugateGradient: At iteration "+iterations+", cost = "+fp);
			try {
        prevStepSize = step;
        step = lineMaximizer.maximize (maxable, xi, step);
			} catch (IllegalArgumentException e) {
				System.out.println ("ConjugateGradient caught "+e.toString());
        TestMaximizable.testValueAndGradientCurrentParameters(maxable);
        TestMaximizable.testValueAndGradientInDirection(maxable, xi);
				//System.out.println ("Trying ConjugateGradient restart.");
				//return this.maximize (maxable, numIterations);
			}
      if (step == 0) {
        if (searchingGradient) {
          System.err.println ("ConjugateGradient converged: Line maximizer got step 0 in gradient direction.  "
                              +"Gradient absNorm="+MatrixOps.absNorm(xi));
          return true;
        } else
          System.err.println ("Line maximizer got step 0.  Probably pointing up hill.  Resetting to gradient.  "
                              +"Gradient absNorm="+MatrixOps.absNorm(xi));
        // Copied from above (how to code this better?  I want GoTo)
        fp = maxable.getValue();
        maxable.getValueGradient (xi);
        searchingGradient = true;
        System.arraycopy (xi, 0, g, 0, n);
        System.arraycopy (xi, 0, h, 0, n);
        step = prevStepSize;
        continue;
      }
      fret = maxable.getValue();
			// This termination provided by "Numeric Recipes in C".
			if (2.0*Math.abs(fret-fp) <= tolerance*(Math.abs(fret)+Math.abs(fp)+eps)) {
        System.out.println ("ConjugateGradient converged: old value= "+fp+" new value= "+fret+" tolerance="+tolerance);
        return true;
      }
      fp = fret;
			maxable.getValueGradient(xi);
			
			logger.info ("Gradient infinityNorm = "+MatrixOps.infinityNorm(xi));
			// This termination provided by McCallum
			if (MatrixOps.infinityNorm(xi) < tolerance) {
        System.err.println ("ConjugateGradient converged: maximum gradient component "+MatrixOps.infinityNorm(xi)
                            +", less than "+tolerance);
        return true;
      }

      dgg = gg = 0.0;
			double gj, xj;
			for (j = 0; j < xi.length; j++) {
				gj = g[j];
				gg += gj * gj;
				xj = -xi[j];
				dgg = (xj + gj) * xj;
			}
			if (gg == 0.0) {
        System.err.println ("ConjugateGradient converged: gradient is exactly zero.");
        return true; // In unlikely case that gradient is exactly zero, then we are done
      }
      gam = dgg/gg;

			double hj;
			for (j = 0; j < xi.length; j++) {
				xj = xi[j];
				g[j] = xj;
				hj = h[j];
				hj = xj + gam * hj;
				h[j] = hj;
			}
			assert (!MatrixOps.isNaN(h));
			MatrixOps.set (xi, h);
      searchingGradient = false;

      iterations++;
			if (iterations > maxIterations) {
				System.err.println("Too many iterations in ConjugateGradient.java");
				return true;
				//throw new IllegalStateException ("Too many iterations.");
			}
		}
		return false;
	}
	
}
