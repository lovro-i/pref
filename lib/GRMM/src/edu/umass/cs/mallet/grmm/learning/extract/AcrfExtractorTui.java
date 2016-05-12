/* Copyright (C) 2003 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */
package edu.umass.cs.mallet.grmm.learning.extract;
/**
 *
 * Created: Aug 23, 2005
 *
 * @author <A HREF="mailto:casutton@cs.umass.edu>casutton@cs.umass.edu</A>
 * @version $Id: AcrfExtractorTui.java,v 1.6 2006/09/25 16:17:40 casutton Exp $
 */

import bsh.EvalError;
import edu.umass.cs.mallet.base.extract.Extraction;
import edu.umass.cs.mallet.base.extract.ExtractionEvaluator;
import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.pipe.TokenSequence2FeatureVectorSequence;
import edu.umass.cs.mallet.base.pipe.iterator.LineGroupIterator;
import edu.umass.cs.mallet.base.pipe.iterator.PipeInputIterator;
import edu.umass.cs.mallet.base.types.InstanceList;
import edu.umass.cs.mallet.base.util.*;
import edu.umass.cs.mallet.grmm.inference.Inferencer;
import edu.umass.cs.mallet.grmm.learning.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class AcrfExtractorTui {

  private static final Logger logger = MalletLogger.getLogger (AcrfExtractorTui.class.getName ());

  private static CommandOption.File outputPrefix = new CommandOption.File
          (AcrfExtractorTui.class, "output-prefix", "FILENAME", true, null, 
             "Directory to write saved model to.", null);

  private static CommandOption.File modelFile = new CommandOption.File
          (AcrfExtractorTui.class, "model-file", "FILENAME", true, null, "Text file describing model structure.", null);

  private static CommandOption.File trainFile = new CommandOption.File
          (AcrfExtractorTui.class, "training", "FILENAME", true, null, "File containing training data.", null);

  private static CommandOption.File testFile = new CommandOption.File
          (AcrfExtractorTui.class, "testing", "FILENAME", true, null, "File containing testing data.", null);

  private static CommandOption.Integer numLabelsOption = new CommandOption.Integer
  (AcrfExtractorTui.class, "num-labels", "INT", true, -1,
          "If supplied, number of labels on each line of input file." +
                  "  Otherwise, the token ---- must separate labels from features.", null);

  private static CommandOption.String trainerOption = new CommandOption.String
          (AcrfExtractorTui.class, "trainer", "STRING", true, "ACRFExtractorTrainer",
                  "Specification of trainer type.", null);

  private static CommandOption.String inferencerOption = new CommandOption.String
          (AcrfExtractorTui.class, "inferencer", "STRING", true, "LoopyBP",
                  "Specification of inferencer.", null);

  private static CommandOption.String maxInferencerOption = new CommandOption.String
          (AcrfExtractorTui.class, "max-inferencer", "STRING", true, "LoopyBP.createForMaxProduct()",
                  "Specification of inferencer.", null);

  private static CommandOption.String evalOption = new CommandOption.String
          (AcrfExtractorTui.class, "eval", "STRING", true, "LOG",
                  "Evaluator to use.  Java code grokking performed.", null);

  private static CommandOption.String extractionEvalOption = new CommandOption.String
          (AcrfExtractorTui.class, "extraction-eval", "STRING", true, "PerDocumentF1",
                  "Evaluator to use.  Java code grokking performed.", null);

  private static CommandOption.Integer checkpointIterations = new CommandOption.Integer
    (AcrfExtractorTui.class, "checkpoint", "INT", true, -1, "Save a copy after every ___ iterations.", null);


  static CommandOption.Boolean cacheUnrolledGraph = new CommandOption.Boolean
          (AcrfExtractorTui.class, "cache-graphs", "true|false", true, true,
                  "Whether to use memory-intensive caching.", null);

  static CommandOption.Boolean perTemplateTrain = new CommandOption.Boolean
          (AcrfExtractorTui.class, "per-template-train", "true|false", true, false,
                  "Whether to pretrain templates before joint training.", null);

  static CommandOption.Integer pttIterations = new CommandOption.Integer
          (AcrfExtractorTui.class, "per-template-iterations", "INTEGER", false, 100,
                  "How many training iterations for each step of per-template-training.", null);

  static CommandOption.Integer randomSeedOption = new CommandOption.Integer
	(AcrfExtractorTui.class, "random-seed", "INTEGER", true, 0,
	 "The random seed for randomly selecting a proportion of the instance list for training", null);


  private static BshInterpreter interpreter = setupInterpreter ();

  public static void main (String[] args) throws IOException, EvalError
  {
    doProcessOptions (AcrfExtractorTui.class, args);
    Timing timing = new Timing ();

    Pipe p;
    if (!numLabelsOption.wasInvoked ()) {
      p = new GenericAcrfData2TokenSequence ();
    } else {
      p = new GenericAcrfData2TokenSequence (numLabelsOption.value);
    }

    PipeInputIterator trainSource = new LineGroupIterator (new FileReader (trainFile.value), Pattern.compile ("^\\s*$"), true);
    PipeInputIterator testSource;
    if (testFile.wasInvoked ()) {
      testSource = new LineGroupIterator (new FileReader (testFile.value), Pattern.compile ("^\\s*$"), true);
    } else {
      testSource = null;
    }

    ACRF.Template[] tmpls = parseModelFile (modelFile.value);

    ACRFExtractorTrainer trainer = createTrainer (trainerOption.value);
    ACRFEvaluator eval = createEvaluator (evalOption.value);
    ExtractionEvaluator extractionEval = createExtractionEvaluator (extractionEvalOption.value);

    Inferencer inf = createInferencer (inferencerOption.value);
    Inferencer maxInf = createInferencer (maxInferencerOption.value);

    trainer.setPipes (p, new TokenSequence2FeatureVectorSequence ())
            .setDataSource (trainSource, testSource)
            .setEvaluator (eval)
            .setTemplates (tmpls)
            .setInferencer (inf)
            .setViterbiInferencer (maxInf)
            .setCheckpointDirectory (outputPrefix.value)
            .setNumCheckpointIterations (checkpointIterations.value)
            .setCacheUnrolledGraphs (cacheUnrolledGraph.value)
            .setUsePerTemplateTrain (perTemplateTrain.value)
            .setPerTemplateIterations (pttIterations.value);

    logger.info ("Starting training...");
    ACRFExtractor extor = trainer.trainExtractor ();
    timing.tick ("Training");

    FileUtils.writeGzippedObject (new File (outputPrefix.value, "extor.ser.gz"), extor);
    timing.tick ("Serializing");

    InstanceList testing = trainer.getTestingData ();
    if (testing != null) {
      eval.test (extor.getAcrf (), testing, "Final results");
    }

    if ((extractionEval != null) && (testing != null)) {
      Extraction extraction = extor.extract (testing);
      extractionEval.evaluate (extraction);
      timing.tick ("Evaluting");
    }

    System.out.println ("Total time (ms) = " + timing.elapsedTime ());
  }

  private static BshInterpreter setupInterpreter ()
  {
    BshInterpreter interpreter = CommandOption.getInterpreter ();
    try {
      interpreter.eval ("import edu.umass.cs.mallet.base.extract.*");
      interpreter.eval ("import edu.umass.cs.mallet.grmm.inference.*");
      interpreter.eval ("import edu.umass.cs.mallet.grmm.learning.*");
      interpreter.eval ("import edu.umass.cs.mallet.grmm.learning.templates.*");
      interpreter.eval ("import edu.umass.cs.mallet.grmm.learning.extract.*");
    } catch (EvalError e) {
      throw new RuntimeException (e);
    }

    return interpreter;
  }

  public static ACRFEvaluator createEvaluator (String spec) throws EvalError
  {
    if (spec.indexOf ('(') >= 0) {
      // assume it's Java code, and don't screw with it.
      return (ACRFEvaluator) interpreter.eval (spec);
    } else {
      LinkedList toks = new LinkedList (Arrays.asList (spec.split ("\\s+")));
      return createEvaluator (toks);
    }
  }

  private static ExtractionEvaluator createExtractionEvaluator (String spec) throws EvalError
  {
    if (spec.indexOf ('(') >= 0) {
      // assume it's Java code, and don't screw with it.
      return (ExtractionEvaluator) interpreter.eval (spec);
    } else {
      spec = "new "+spec+"Evaluator ()";
      return (ExtractionEvaluator) interpreter.eval (spec);
    }
  }

  private static ACRFEvaluator createEvaluator (LinkedList toks)
  {
    String type = (String) toks.removeFirst ();

    if (type.equalsIgnoreCase ("SEGMENT")) {
      int slice = Integer.parseInt ((String) toks.removeFirst ());
      if (toks.size() % 2 != 0)
        throw new RuntimeException ("Error in --eval "+evalOption.value+": Every start tag must have a continue.");
      int numTags = toks.size () / 2;
      String[] startTags = new String [numTags];
      String[] continueTags = new String [numTags];

      for (int i = 0; i < numTags; i++) {
        startTags[i] = (String) toks.removeFirst ();
        continueTags[i] = (String) toks.removeFirst ();
      }

      return new MultiSegmentationEvaluatorACRF (startTags, continueTags, slice);

    } else if (type.equalsIgnoreCase ("LOG")) {
      return new ACRFTrainer.LogEvaluator ();

    } else if (type.equalsIgnoreCase ("SERIAL")) {
      List evals = new ArrayList ();
      while (!toks.isEmpty ()) {
        evals.add (createEvaluator (toks));
      }
      return new AcrfSerialEvaluator (evals);

    } else {
      throw new RuntimeException ("Error in --eval "+evalOption.value+": illegal evaluator "+type);
    }
  }

  private static ACRFExtractorTrainer createTrainer (String spec) throws EvalError
  {
    String cmd;
    if (spec.indexOf ('(') >= 0) {
      // assume it's Java code, and don't screw with it.
      cmd = spec;
    } else if (spec.endsWith ("Trainer")) {
      cmd = "new "+spec+"()";
    } else {
      cmd = "new "+spec+"Trainer()";
    }

    // Return whatever the Java code says to
    Object trainer = interpreter.eval (cmd);

    if (trainer instanceof ACRFExtractorTrainer)
      return (ACRFExtractorTrainer) trainer;
    else if (trainer instanceof ACRFTrainer)
      return new ACRFExtractorTrainer ().setTrainingMethod ((ACRFTrainer) trainer);
    else throw new RuntimeException ("Don't know what to do with trainer "+trainer);
  }

  private static Inferencer createInferencer (String spec) throws EvalError
  {
    String cmd;
    if (spec.indexOf ('(') >= 0) {
      // assume it's Java code, and don't screw with it.
      cmd = spec;
    } else {
      cmd = "new "+spec+"()";
    }

    // Return whatever the Java code says to
    Object inf = interpreter.eval (cmd);

    if (inf instanceof Inferencer)
      return (Inferencer) inf;

    else throw new RuntimeException ("Don't know what to do with inferencer "+inf);
  }


  public static void doProcessOptions (Class childClass, String[] args)
  {
    CommandOption.List options = new CommandOption.List ("", new CommandOption[0]);
    options.add (childClass);
    options.process (args);
    options.logOptions (Logger.getLogger (""));
  }

  private static ACRF.Template[] parseModelFile (File mdlFile) throws IOException, EvalError
  {
    BufferedReader in = new BufferedReader (new FileReader (mdlFile));

    List tmpls = new ArrayList ();
    String line = in.readLine ();
    while (line != null) {
      Object tmpl = interpreter.eval (line);
      if (!(tmpl instanceof ACRF.Template)) {
        throw new RuntimeException ("Error in "+mdlFile+" line "+in.toString ()+":\n  Object "+tmpl+" not a template");
      }
      tmpls.add (tmpl);
      line = in.readLine ();
    }

    return (ACRF.Template[]) tmpls.toArray (new ACRF.Template [0]);
  }

}
