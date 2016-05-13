/* Copyright (C) 2006 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://mallet.cs.umass.edu/
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */
package edu.umass.cs.mallet.share.extractors;

import edu.umass.cs.mallet.base.pipe.iterator.AbstractPipeInputIterator;
import edu.umass.cs.mallet.base.pipe.iterator.PipeInputIterator;
import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.types.Instance;

/**
 * Passes instances from some other iterator through a given pipe.
 *  This is a hacky way of chaining pipes without using SerialPipes.
 *
 * Created: Mar 3, 2005
 *
 * @author <A HREF="mailto:casutton@cs.umass.edu>casutton@cs.umass.edu</A>
 * @version $Id: PipedIterator.java,v 1.1 2006/02/15 06:33:40 casutton Exp $
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
