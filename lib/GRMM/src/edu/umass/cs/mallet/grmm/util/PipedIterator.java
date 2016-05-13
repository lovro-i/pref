/* Copyright (C) 2003 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */
package edu.umass.cs.mallet.grmm.util;

import edu.umass.cs.mallet.base.pipe.iterator.AbstractPipeInputIterator;
import edu.umass.cs.mallet.base.pipe.iterator.PipeInputIterator;
import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.types.Instance;

/**
 * Created: Mar 3, 2005
 *
 * @author <A HREF="mailto:casutton@cs.umass.edu>casutton@cs.umass.edu</A>
 * @version $Id: PipedIterator.java,v 1.1 2006/02/03 17:14:10 casutton Exp $
 */
public class PipedIterator extends AbstractPipeInputIterator {

  PipeInputIterator subIt;
  Pipe pipe;

  public PipedIterator (PipeInputIterator subIt, Pipe pipe)
  {
    this.subIt = subIt;
    this.pipe = pipe;
  }

  // The PipeInputIterator interface
  public Instance nextInstance ()
  {
    Instance inst = subIt.nextInstance ();
    inst = pipe.pipe (inst);
    return new Instance (inst.getData (), inst.getTarget (), inst.getName (), inst.getSource ());
  }

  public boolean hasNext ()
  {
    return subIt.hasNext ();
  }
}
