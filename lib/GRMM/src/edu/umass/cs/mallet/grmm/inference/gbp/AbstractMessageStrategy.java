/* Copyright (C) 2003 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */
package edu.umass.cs.mallet.grmm.inference.gbp;

import edu.umass.cs.mallet.grmm.types.Factor;
import edu.umass.cs.mallet.grmm.types.TableFactor;
import edu.umass.cs.mallet.grmm.types.LogTableFactor;

import java.util.Iterator;

/**
 * Created: May 29, 2005
 *
 * @author <A HREF="mailto:casutton@cs.umass.edu>casutton@cs.umass.edu</A>
 * @version $Id: AbstractMessageStrategy.java,v 1.3 2006/01/05 20:44:35 casutton Exp $
 */
public abstract class AbstractMessageStrategy implements MessageStrategy {

  protected MessageArray oldMessages;
  protected MessageArray newMessages;

  public void setMessageArray (MessageArray oldMessages, MessageArray newMessages)
  {
    this.oldMessages = oldMessages;
    this.newMessages = newMessages;
  }

  public MessageArray getOldMessages ()
  {
    return oldMessages;
  }

  public MessageArray getNewMessages ()
  {
    return newMessages;
  }

  Factor msgProduct (RegionEdge edge)
  {
    Factor product = new LogTableFactor (edge.from.vars);

    for (Iterator it = edge.neighboringParents.iterator (); it.hasNext ();) {
      RegionEdge otherEdge = (RegionEdge) it.next ();
      Factor otherMsg = oldMessages.getMessage (otherEdge.from, otherEdge.to);

      product.multiplyBy (otherMsg);
    }

    for (Iterator it = edge.loopingMessages.iterator (); it.hasNext ();) {
      RegionEdge otherEdge = (RegionEdge) it.next ();
      Factor otherMsg = newMessages.getMessage (otherEdge.from, otherEdge.to);
      product.divideBy (otherMsg);
    }

    return product;
  }
}
