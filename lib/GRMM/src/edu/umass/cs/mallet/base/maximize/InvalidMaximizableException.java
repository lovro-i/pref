/* Copyright (C) 2006 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://mallet.cs.umass.edu/
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */
package edu.umass.cs.mallet.base.maximize;

/**
 * Exception thrown by optimization algorithms, when the problem is usually
 *  due to a problem with the given Maximizable instance.
 * <p>
 * If the optimizer throws this in your code, usually there are two possible
 *  causes: (a) you are computing the gradients approximately, (b) your value
 *  function and gradient do not match (this can be checking using
 *  @link{edu.umass.cs.mallet.base.maximize.tests.TestMaximizable}.
 *
 * Created: Feb 1, 2006
 *
 * @author <A HREF="mailto:casutton@cs.umass.edu>casutton@cs.umass.edu</A>
 * @version $Id: InvalidMaximizableException.java,v 1.1 2006/02/01 19:23:15 casutton Exp $
 */
public class InvalidMaximizableException extends OptimizationException {

  public InvalidMaximizableException ()
  {
  }

  public InvalidMaximizableException (String message)
  {
    super (message);
  }

  public InvalidMaximizableException (String message, Throwable cause)
  {
    super (message, cause);
  }

  public InvalidMaximizableException (Throwable cause)
  {
    super (cause);
  }
}
