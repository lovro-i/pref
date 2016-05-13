/* Copyright (C) 2003 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */
package edu.umass.cs.mallet.grmm.learning.templates;

import edu.umass.cs.mallet.base.types.*;
import edu.umass.cs.mallet.grmm.types.Variable;
import edu.umass.cs.mallet.grmm.learning.ACRF;
import edu.umass.cs.mallet.grmm.util.THashMultiMap;
import gnu.trove.THashMap;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


/**
 * Template for adding "skip edges" as in
 *
 * @author Charles Sutton
 * @version $Id: SimilarTokensTemplate.java,v 1.5 2006/05/30 18:12:39 casutton Exp $
 */
// Copied from TUIacrf
public class SimilarTokensTemplate extends ACRF.SequenceTemplate {

  private static final boolean debug = true;

  private static class TokenInfo {

    String featureName;
    FeatureVector fv;
    int pos;

    public TokenInfo (String featureName, FeatureVector fv, int pos)
    {
      this.featureName = featureName;
      this.fv = fv;
      this.pos = pos;
    }
  }

  private int factor;
  private boolean distinguishEndpts = true;
  private boolean wordFeaturesOnly = false;
  private boolean excludeAdjacent = true;

  private FeatureVectorBinner binner;

  // Maps FeatureVectorSequence ==> THashMultiMap<String,TokenInfo>
  private transient THashMap instanceCache = new THashMap ();


  public SimilarTokensTemplate (int factor)
  {
    this (factor, false);
  }

  public SimilarTokensTemplate (int factor, boolean distinguishEndpoints)
  {
    this (factor, distinguishEndpoints, new CapWordsBinner ());
  }

  public SimilarTokensTemplate (int factor, boolean distinguishEndpoints, FeatureVectorBinner binner)
  {
    this.factor = factor;
    this.distinguishEndpts = distinguishEndpoints;
    this.binner = binner;
  }

  public void addInstantiatedCliques (ACRF.UnrolledGraph graph,
                                      FeatureVectorSequence fvs,
                                      LabelsSequence lblseq)
  {
    THashMultiMap fvByWord = constructFvByWord (fvs);

    int numSkip = 0;

    for (Iterator it = fvByWord.keySet ().iterator (); it.hasNext ();) {
      String wordFeature = (String) it.next ();
      List infoList = (List) fvByWord.get (wordFeature);
      int N = infoList.size ();

      if (debug && N > 1) System.err.print ("Processing list of size "+N+" ("+wordFeature+")");

      for (int i = 0; i < N; i++) {
        for (int j = i + 1; j < N; j++) {
          if (excludeAdjacent && (i + 1 == j)) continue;

          TokenInfo info1 = (TokenInfo) infoList.get (i);
          TokenInfo info2 = (TokenInfo) infoList.get (j);

          Variable v1 = graph.getVarForLabel (info1.pos, factor);
          Variable v2 = graph.getVarForLabel (info2.pos, factor);

          Variable[] vars = new Variable[]{v1, v2};
          assert v1 != null : "Couldn't get label factor " + factor + " time " + i;
          assert v2 != null : "Couldn't get label factor " + factor + " time " + j;

          FeatureVector fv = combineFv (wordFeature, info1.fv, info2.fv);
          ACRF.UnrolledVarSet clique = new ACRF.UnrolledVarSet (graph, this, vars, fv);
          graph.addClique (clique);
          numSkip++;

          /* Insanely verbose
          if (debug) {
            System.err.println ("Combining:\n   "+info1.fv+"\n   "+info2.fv);
          }
          */
        }
      }
      if (debug && N > 1) System.err.println ("...done.");
    }

    System.err.println ("SimilarTokensTemplate: Total skip edges = "+numSkip);
  }

  private THashMultiMap constructFvByWord (FeatureVectorSequence fvs)
  {
    THashMultiMap fvByWord = new THashMultiMap (fvs.size ());
    int N = fvs.size ();
    for (int t = 0; t < N; t++) {
      FeatureVector fv = fvs.getFeatureVector (t);
      String wordFeature = binner.computeBin (fv);
      if (wordFeature != null) {  // could happen if the current word has been excluded
        fvByWord.put (wordFeature, new TokenInfo (wordFeature, fv, t));
      }
    }
    return fvByWord;
  }

  private FeatureVector combineFv (String word, FeatureVector fv1, FeatureVector fv2)
  {
// 			System.out.println("combineFv:");
// 			System.out.println("FV1 values "+fv1.getValues()+" indices "+fv1.getIndices());
// 			System.out.println("FV1: "+fv1.toString (true));
// 			System.out.println("FV2 values "+fv2.getValues()+" indices "+fv2.getIndices());
// 			System.out.println("FV2:"+fv2.toString (true));
    Alphabet dict = fv1.getAlphabet ();
    AugmentableFeatureVector afv = new AugmentableFeatureVector (dict, true);
    if (wordFeaturesOnly) {
      int idx = dict.lookupIndex (word);
      afv.add (idx, 1.0);
    } else if (distinguishEndpts) {
      afv.add (fv1, "S:");
      afv.add (fv2, "E:");
    } else {
      afv.add (fv1);
      afv.add (fv2);
    }

//			System.out.println("AFV: "+afv.toString (true));
    return afv;
  }

  // Customization

  /** Interface for classes that ssigns each features vector to a String-valued bin.
   *   Feature vectors is the same bin are assumed to be similar, so that they need a skip edge.
   *   In this way the similarity metric used for generating skip edges can be completely customized.
   */
  public static interface FeatureVectorBinner {
    String computeBin (FeatureVector fv);
  }

  public static class WordFeatureBinner implements FeatureVectorBinner, Serializable {

    private Pattern findWordPtn1 = Pattern.compile("WORD=(.*)");
    private Pattern findWordPtn2 = Pattern.compile("W=(.*)");
    private Pattern findWordExcludePtn = Pattern.compile (".*(?:@-?\\d+|_&_).*");

    private Pattern wordIncludePattern = null;

    public WordFeatureBinner () { }

    public WordFeatureBinner (Pattern wordIncludePattern)
    {
      this.wordIncludePattern = wordIncludePattern;
    }

    public String computeBin (FeatureVector fv)
    {
      String text = intuitTokenText (fv);
      if (text != null) {
        if (wordIncludePattern == null || wordIncludePattern.matcher(text).matches ()) {
          return text;
        }
      }

      return null;
    }

    private String intuitTokenText (FeatureVector fv)
    {
      Alphabet dict = fv.getAlphabet ();
      for (int loc = 0; loc < fv.numLocations (); loc++) {
        int idx = fv.indexAtLocation (loc);
        String fname = String.valueOf (dict.lookupObject (idx));

        Matcher matcher;
        if ((matcher = findWordPtn1.matcher (fname)).matches ()) {
          if (!findWordExcludePtn.matcher (fname).matches ()) {
            return matcher.group (1);
          }
        } else if ((findWordPtn2 != null) && (matcher = findWordPtn2.matcher (fname)).matches ()) {
          if (!findWordExcludePtn.matcher (fname).matches ()) {
            return matcher.group (1);
          }
        }
      }

      return null;
    }

    // Serialization garbage

    private static final long serialVersionUID = 1;
    private static final int CURRENT_SERIAL_VERSION = 2;

    private void writeObject (ObjectOutputStream out) throws IOException
    {
      out.defaultWriteObject ();
      out.writeInt (CURRENT_SERIAL_VERSION);
    }


    private void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException
    {
      in.defaultReadObject ();
      int version = in.readInt ();
      if (version == 1) {
        throw new RuntimeException ();
      }
    }

  }

  public static class CapWordsBinner extends WordFeatureBinner {

    public CapWordsBinner ()
    {
      super (Pattern.compile ("[A-Z][A-Za-z]*"));
    }

  }

  public void setBinner (FeatureVectorBinner binner)
  {
    this.binner = binner;
  }

  public boolean isExcludeAdjacent ()
  {
    return excludeAdjacent;
  }

  public void setExcludeAdjacent (boolean excludeAdjacent)
  {
    this.excludeAdjacent = excludeAdjacent;
  }
  // Serialization garbage

  private static final long serialVersionUID = 1;
  private static final int CURRENT_SERIAL_VERSION = 1;

  private void writeObject (ObjectOutputStream out) throws IOException
  {
    out.defaultWriteObject ();
    out.writeInt (CURRENT_SERIAL_VERSION);
  }


  private void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException
  {
    in.defaultReadObject ();
    int version = in.readInt ();
    instanceCache = new THashMap ();
  }

}
