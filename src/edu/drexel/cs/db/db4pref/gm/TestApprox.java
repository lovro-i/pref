package edu.drexel.cs.db.db4pref.gm;

import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.MapPreferenceSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.filter.Filter;
import edu.drexel.cs.db.db4pref.filter.MissingProbabilities;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.posterior.PreferenceExpander;
import edu.drexel.cs.db.db4pref.util.FileUtils;
import edu.drexel.cs.db.db4pref.util.Logger;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;


public class TestApprox {

  public static void main(String[] args) throws IOException, InterruptedException {
    PrintWriter out;
    if (args.length > 0) {
      File file = new File(args[0]);
      Logger.info("Writing to %s", file);
      out = FileUtils.append(file);
    }
    else {
      Logger.warn("No file specified, writing to console");
      out = new PrintWriter(System.out);
    }
    
    while (true) {
      seven(out);
    } 
  }
  
  public static void seven(PrintWriter out) throws IOException, InterruptedException {
    Random random = new Random();
    int[] its = { 40, 60, 80, 100 }; //, 60, 70, 80, 90, 100 };
    double[] phis = { 0.2, 0.5, 0.8 };
    int[] amps = { 100, 500, 1000, 5000, 10000, 50000 };
    
    for (int i = 1; i < 100000; i++) {
      int m = its[random.nextInt(its.length)];
      ItemSet items = new ItemSet(m);
      items.tagOneBased();
      
      double miss = 0.85 * random.nextDouble();
      double phi = phis[random.nextInt(phis.length)];
      
      Ranking reference = items.getReferenceRanking();
      MallowsModel model = new MallowsModel(reference, phi);
      
      Ranking r = items.getRandomRanking();
      MapPreferenceSet v = r.transitiveClosure();
      Filter.removePreferences(v, MissingProbabilities.uniform(items, miss));
      String vName = v.toString().replace(" ", "").replace(',', ' ');
      
      
      Logger.info("Executing test %d: %d/%d items", i, r.length(), m);

      // EXPANDER
      long starts = System.currentTimeMillis();
      PreferenceExpander expander = new PreferenceExpander(model);
      double pExpander = expander.getProbability(v);
      long timeExpander = System.currentTimeMillis() - starts;
      Logger.info("Expander done in %d sec", timeExpander / 1000);

      /*
      // JAYES
      long startGM = System.currentTimeMillis();
      long startJayes = 0;
      int networkSize = -1;
      double pJayes = -1;
      int jayesJunctionTreeWidth = -1;
      long timeGM = -1;
      long timeJayes = -1;
      long timeGMJayes = -1;

      try {
        // GM Common
        GraphicalModel gm = new GraphicalModel(model, v);
        gm.build();
        networkSize = gm.getNetworkSize();
        timeGM = System.currentTimeMillis() - startGM;
        
        // JAYES
        startJayes = System.currentTimeMillis();
        JayesInferator jayesInferator = new JayesInferator(gm);
        System.out.println("aaaaacc");
        pJayes = jayesInferator.getProbability();
        System.out.println("aaaaab");

        jayesJunctionTreeWidth = jayesInferator.getJunctionTreeWidth();
        timeJayes = System.currentTimeMillis() - startJayes;
        timeGMJayes = timeGM + timeJayes;
      }
      catch (OutOfMemoryError e) {
        Logger.info("Out of memory for Jayes. Turning off..."); 
        pJayes = -1;
        timeJayes = System.currentTimeMillis() - startJayes;
        timeGMJayes = timeGM + timeJayes;
      }
      Logger.info("Jayes done in %d sec", timeGMJayes / 1000);
      

      // AMP
      AMPSampler ampSampler = new AMPSampler(model);
      double[] b = new double[0];
      double[] pAmps = new double[amps.length];
      long[] timeAmps = new long[amps.length];
      long startAmp = System.currentTimeMillis();

      for (int k = 0; k < amps.length; k++) {
        int as = amps[k];
        int reps = as - b.length;
        double[] a = ampSampler.samplePosteriors(v, reps);
        b = MathUtils.concat(b, a);
        pAmps[k] = MathUtils.mean(b);
        timeAmps[k] = System.currentTimeMillis() - startAmp;
        // Logger.info("AMP %d done in %d sec", as, timeAmps[k] / 1000);
      }
      */


      // SampleSearch
      GraphicalModel gm = new GraphicalModel(model, v);
      gm.build();
      
      SampleSearchInferator ss10 = new SampleSearchInferator(gm);
      double pss10 = ss10.exec(10);
      pss10 = pss10 / Math.log10(Math.E);
      
      SampleSearchInferator ss60 = new SampleSearchInferator(gm);
      double pss60 = ss60.exec(60);
      pss60 = pss60 / Math.log10(Math.E);
      
      
      String line = String.format("%d,%.1f,%s,%d", m, phi, vName, v.size());
      line += String.format(",%f,%d,%d,%d,%d,%d", Math.log(pExpander), timeExpander, expander.getMaxWidth(), expander.getSumWidth(), expander.getMaxStates(), expander.getSumStates());
      line += String.format(",%f,%d,%d,%d", pss10, ss10.getCount(), ss10.getTime(), ss10.getTotalTime());
      line += String.format(",%f,%d,%d,%d", pss60, ss60.getCount(), ss60.getTime(), ss60.getTotalTime());
      // line += String.format(",%d,%d,%f,%d,%d,%d", networkSize, jayesJunctionTreeWidth, Math.log(pJayes), timeGM, timeJayes, timeGMJayes);
      // for (int k = 0; k < amps.length; k++) {
      //   line += String.format(",%d,%f,%d", amps[k], Math.log(pAmps[k]), timeAmps[k]);
      // }

      out.println(line);
      out.flush();
      
    }
  }
}
