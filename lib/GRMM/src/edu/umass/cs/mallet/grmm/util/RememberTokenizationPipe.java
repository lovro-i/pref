/* Copyright (C) 2003 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */
package edu.umass.cs.mallet.grmm.util;

import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.types.Instance;
import edu.umass.cs.mallet.base.extract.Tokenization;

/**
 * Created: Mar 17, 2005
 *
 * @author <A HREF="mailto:casutton@cs.umass.edu>casutton@cs.umass.edu</A>
 * @version $Id: RememberTokenizationPipe.java,v 1.1 2006/02/03 17:14:10 casutton Exp $
 */
public class RememberTokenizationPipe extends Pipe {

  public Instance pipe (Instance carrier)
  {
    Tokenization tok = (Tokenization) carrier.getData ();
    carrier.setProperty ("TOKENIZATION", tok);
    return carrier;
  }
}
