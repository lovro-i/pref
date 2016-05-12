/* Copyright (C) 2003 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */
package edu.umass.cs.mallet.grmm.learning.extract;

import edu.umass.cs.mallet.base.extract.*;
import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.pipe.iterator.PipeInputIterator;
import edu.umass.cs.mallet.base.pipe.iterator.InstanceListIterator;
import edu.umass.cs.mallet.base.types.*;
import edu.umass.cs.mallet.grmm.util.SliceLabelsSequence;
import edu.umass.cs.mallet.grmm.learning.ACRF;

/**
 * Created: Mar 1, 2005
 *
 * @author <A HREF="mailto:casutton@cs.umass.edu>casutton@cs.umass.edu</A>
 * @version $Id: ACRFExtractor.java,v 1.1 2006/02/03 17:14:10 casutton Exp $
 */
public class ACRFExtractor implements Extractor {

  private ACRF acrf;
  private Pipe tokPipe;
  private Pipe featurePipe;
  private int slice = 0;

  private String backgroundTag = "O";
  private TokenizationFilter filter;

  public ACRFExtractor (ACRF acrf, Pipe tokPipe, Pipe featurePipe)
  {
    this.acrf = acrf;
    this.tokPipe = tokPipe;
    this.featurePipe = featurePipe;
    this.filter = new BIOTokenizationFilter ();
  }

  public Extraction extract (Object o)
  {
    throw new UnsupportedOperationException ("Not yet implemented");
  }

  public Extraction extract (Tokenization toks)
  {
    throw new UnsupportedOperationException ("Not yet implemented");
  }

  public Extraction extract (PipeInputIterator source)
  {
    Extraction extraction = new Extraction (this, getTargetAlphabet ());
     // Put all the instances through both pipes, then get viterbi path
     InstanceList tokedList = new InstanceList (tokPipe);
     tokedList.add (source);
     InstanceList pipedList = new InstanceList (getFeaturePipe ());
     pipedList.add (new InstanceListIterator (tokedList));

     InstanceList.Iterator it1 = tokedList.iterator ();
     InstanceList.Iterator it2 = pipedList.iterator ();
     while (it1.hasNext()) {
       Instance toked = it1.nextInstance();
       Instance piped = it2.nextInstance ();

       Tokenization tok = (Tokenization) toked.getData();
       String name = piped.getName().toString();
       Sequence target = (Sequence) piped.getTarget ();
       LabelsSequence output = acrf.getBestLabels (piped);
       LabelSequence ls = SliceLabelsSequence.sliceLabelsSequence (output, slice);
       LabelSequence lsTarget = SliceLabelsSequence.sliceLabelsSequence
              ((LabelsSequence) target, slice);

       DocumentExtraction docseq = new DocumentExtraction (name, getTargetAlphabet (), tok,
                                                           ls, lsTarget, backgroundTag, filter);
       extraction.addDocumentExtraction (docseq);
     }
     return extraction;
  }

  // Experimental: Extract from training lists
  public Extraction extract (InstanceList testing)
  {
    Extraction extraction = new Extraction (this, getTargetAlphabet ());
    for (int i = 0; i < testing.size(); i++) {
      Instance instance = testing.getInstance (i);
      Tokenization tok = (Tokenization) instance.getProperty ("TOKENIZATION");
      if (tok == null)
        throw new IllegalArgumentException
         ("To use extract(InstanceList), must save the Tokenization!");

      String name = instance.getName ().toString ();
      Sequence target = (Sequence) instance.getTarget ();
      Sequence output = acrf.getBestLabels (instance);

      DocumentExtraction docseq = new DocumentExtraction (name, getTargetAlphabet (), tok,
                                                          output, target, backgroundTag, filter);
      extraction.addDocumentExtraction (docseq);
    }
    return extraction;
  }

  public Pipe getFeaturePipe ()
  {
    return featurePipe;
  }

  public Pipe getTokenizationPipe ()
  {
    return tokPipe;
  }

  public void setTokenizationPipe (Pipe pipe)
  {
    tokPipe = pipe;
  }

  public Alphabet getInputAlphabet ()
  {
    return acrf.getInputAlphabet ();
  }

  public LabelAlphabet getTargetAlphabet ()
  {
    return (LabelAlphabet) acrf.getInputPipe ().getTargetAlphabet ();
  }

  public ACRF getAcrf ()
  {
    return acrf;
  }

  public void setSlice (int sl) { slice = sl; }

  public void setTokenizationFilter (TokenizationFilter filter)
  {
    this.filter = filter;
  }
}
