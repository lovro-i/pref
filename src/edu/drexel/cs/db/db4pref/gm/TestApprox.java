package edu.drexel.cs.db.db4pref.gm;

import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.MapPreferenceSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.filter.Filter;
import edu.drexel.cs.db.db4pref.filter.MissingProbabilities;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.posterior.PreferenceExpander;
import edu.drexel.cs.db.db4pref.sampler.AMPSampler;
import edu.drexel.cs.db.db4pref.util.FileUtils;
import edu.drexel.cs.db.db4pref.util.Logger;
import edu.drexel.cs.db.db4pref.util.MathUtils;
import edu.drexel.cs.db.db4pref.util.Utils;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import java.util.concurrent.TimeoutException;


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
      eightFirst(out);
      // eightSecond(out);
    } 
  }
  
  public static void eightSecond(PrintWriter out) throws IOException, InterruptedException {
    Random random = new Random();
    int[] its = { 20, 25, 30, 35, 40, 45, 50, 60, 70 };
    double[] phis = { 0.2, 0.5, 0.8 };
    int[] amps = { 100, 1000, 10000 };
    double[] misses = { 0.9, 0.95 };
    
    for (int i = 0; i < 100000; i++) {
      int m = its[i % its.length];
      ItemSet items = new ItemSet(m);
      items.tagOneBased();
      
      double phi = phis[random.nextInt(phis.length)];
      double miss = misses[random.nextInt(misses.length)];
      
      Ranking reference = items.getReferenceRanking();
      MallowsModel model = new MallowsModel(reference, phi);
      
      Ranking r = items.getRandomRanking();
      MapPreferenceSet v = r.transitiveClosure();
      Filter.removePreferences(v, MissingProbabilities.uniformPairwise(items, miss));
      String vName = v.toString();
            
      Logger.info("Executing test %d: %d items (%d of %d pairs), phi = %.1f", i+1, m, v.size(), r.size(), phi);

      long timeout = 20 * Utils.ONE_MINUTE;
      
      
      // EXPANDER
      long starts = System.currentTimeMillis();
      PreferenceExpander expander = new PreferenceExpander(model);
      expander.setTimeout(timeout);
      Double pExpander;
      try { pExpander = expander.getProbability(v); }
      catch (TimeoutException te) { 
        Logger.info(te.getMessage());
        pExpander = -1d; 
      }
      long timeExpander = System.currentTimeMillis() - starts;
      Logger.info("PreferenceExpander done in %d sec: %f", timeExpander / 1000, Math.log(pExpander));
            

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
        Logger.info("%d AMPs done in %d sec: %f", as, timeAmps[k] / 1000, Math.log(pAmps[k]));
      }
      


      // SampleSearch
      long startGM = System.currentTimeMillis();
      GraphicalModel gm = new GraphicalModel(model, v);
      gm.build();
      long timeGM = System.currentTimeMillis() - startGM;
      Logger.info("Graphical model built in %d ms", timeGM);
      
      Double[] pSS = new Double[amps.length];
      Integer[] tSS = new Integer[amps.length];
      Long[] timeSS = new Long[amps.length];
      
      
      for (int k = 0; k < amps.length; k++) {
        int t = (int) Math.round(timeAmps[k] / 1000);
        t = Math.max(1, t);
        tSS[k] = t;
        Logger.info("Running SampleSearch for %d sec", t);
        SampleSearchInferator ss = new SampleSearchInferator(gm);
        ss.setTimeout(0);
        Double pss = ss.exec(t);
        pSS[k] = pss;
        timeSS[k] = ss.getTime();
        Logger.info("SampleSearch done in %d sec: %f", timeSS[k] / 1000, pss);
      }
      
      
      String line = String.format("%d,%.1f,%.1f,%s,%d", m, phi, miss, vName, v.size());
      line += String.format(",%f,%d,%d,%d,%d,%d", Math.log(pExpander), timeExpander, expander.getMaxWidth(), expander.getSumWidth(), expander.getMaxStates(), expander.getSumStates());
      line += String.format(",%d,%d", timeGM, gm.getNetworkSize());
      // line += String.format(",%d,%f,%d,%d,%d", jayesJunctionTreeWidth, Math.log(pJayes), timeGM, timeJayes, timeGMJayes);
      
      
      for (int k = 0; k < amps.length; k++) {
        line += String.format(",%d,%f,%d,%d,%f,%d", amps[k], Math.log(pAmps[k]), timeAmps[k], tSS[k], pSS[k], timeSS[k]);
      }
      
      out.println(line);
      out.flush();
      
    }
  }
  
  
  public static void eightFirst(PrintWriter out) throws IOException, InterruptedException {
    Random random = new Random();
    int[] its = { 50, 60 };
    double[] phis = { 0.9 };
    int[] amps = { 100, 1000, 10000 };
    
    for (int i = 0; i < 100000; i++) {
      int m = its[i % its.length];
      ItemSet items = new ItemSet(m);
      items.tagOneBased();
      
      double phi = phis[random.nextInt(phis.length)];
      
      Ranking reference = items.getReferenceRanking();
      MallowsModel model = new MallowsModel(reference, phi);
      
      Ranking v = items.getRandomRanking();
      v.remove(items.get(0));
      String vName = v.toString();
            
      Logger.info("Executing test %d: %d of %d items (%d pairs)", i+1, v.length(), m, v.size());

      long timeout = 20 * Utils.ONE_MINUTE;
      
      
      // EXPANDER
      long starts = System.currentTimeMillis();
      PreferenceExpander expander = new PreferenceExpander(model);
      expander.setTimeout(timeout);
      Double pExpander;
      try { pExpander = expander.getProbability(v); }
      catch (TimeoutException te) { 
        Logger.info(te.getMessage());
        pExpander = -1d; 
      }
      long timeExpander = System.currentTimeMillis() - starts;
      Logger.info("PreferenceExpander done in %d sec: %f", timeExpander / 1000, Math.log(pExpander));
            

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
        Logger.info("%d AMPs done in %d sec: %f", as, timeAmps[k] / 1000, Math.log(pAmps[k]));
      }
      


      // SampleSearch
      long startGM = System.currentTimeMillis();
      GraphicalModel gm = new GraphicalModel(model, v);
      gm.build();
      long timeGM = System.currentTimeMillis() - startGM;
      Logger.info("Graphical model built in %d ms", timeGM);
      
      Double[] pSS = new Double[amps.length];
      Integer[] tSS = new Integer[amps.length];
      Long[] timeSS = new Long[amps.length];
      
      
      for (int k = 0; k < amps.length; k++) {
        int t;
        if (k == 0) {
          t = (int) Math.round(timeAmps[k] / 1000);
        }
        else {
          t = (int) Math.round(2 * timeSS[k-1] / 1000);
        }
        
        t = Math.max(1, t);
        tSS[k] = t;
        Logger.info("Running SampleSearch for %d sec", t);
        SampleSearchInferator ss = new SampleSearchInferator(gm);
        ss.setTimeout(0);
        Double pss = ss.exec(t);
        pSS[k] = pss;
        timeSS[k] = ss.getTime();
        Logger.info("SampleSearch done in %d sec: %f", timeSS[k] / 1000, pss);
      }
      
      
      String line = String.format("%d,%.1f,%s,%d,%d", m, phi, vName, v.length(), v.size());
      line += String.format(",%f,%d,%d,%d,%d,%d", Math.log(pExpander), timeExpander, expander.getMaxWidth(), expander.getSumWidth(), expander.getMaxStates(), expander.getSumStates());
      line += String.format(",%d,%d", timeGM, gm.getNetworkSize());
      // line += String.format(",%d,%f,%d,%d,%d", jayesJunctionTreeWidth, Math.log(pJayes), timeGM, timeJayes, timeGMJayes);
      
      
      for (int k = 0; k < amps.length; k++) {
        line += String.format(",%d,%f,%d,%d,%f,%d", amps[k], Math.log(pAmps[k]), timeAmps[k], tSS[k], pSS[k], timeSS[k]);
      }
      
      out.println(line);
      out.flush();
      
    }
  }
  
  public static void sevenRank(PrintWriter out) throws IOException, InterruptedException {
    Random random = new Random();
    int[] its = { 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80 };
    double[] phis = { 0.2, 0.5, 0.8 };
    int[] amps = { 100, 500, 1000, 5000, 10000, 50000 };
    
    for (int i = 0; i < 100000; i++) {
      int m = its[i % its.length];
      ItemSet items = new ItemSet(m);
      items.tagOneBased();
      
      double miss = 0.85 * random.nextDouble();
      double phi = phis[random.nextInt(phis.length)];
      
      Ranking reference = items.getReferenceRanking();
      MallowsModel model = new MallowsModel(reference, phi);
      
      Ranking r = items.getRandomRanking();
//      MapPreferenceSet v = r.transitiveClosure();
//      Filter.removePreferences(v, MissingProbabilities.uniform(items, miss));
//      String vName = v.toString().replace(" ", "").replace(',', ' ');
      Ranking v = new Ranking(r);
      Filter.removeItems(v, miss);
      String vName = v.toString();
      
      
      Logger.info("Executing test %d: %d items, %d / %d pairs", i+1, m, v.size(), r.size());

      long timeout = 15 * Utils.ONE_MINUTE;
      
      // SPAN EXPANDER
//      long startSpan = System.currentTimeMillis();
//      SpanExpander sexpander = new SpanExpander(model);
//      sexpander.setTimeout(timeout);
//      Double pSexpander;
//      try { pSexpander = sexpander.getProbability(v); }
//      catch (TimeoutException te) { 
//        Logger.info(te.getMessage());
//        pSexpander = -1d; 
//      }
//      long timeSexpander = System.currentTimeMillis() - startSpan;
//      Logger.info("SpanExpander done in %d sec", timeSexpander / 1000);

      
      // EXPANDER
      long starts = System.currentTimeMillis();
      PreferenceExpander expander = new PreferenceExpander(model);
      expander.setTimeout(timeout);
      Double pExpander;
      try { pExpander = expander.getProbability(v); }
      catch (TimeoutException te) { 
        Logger.info(te.getMessage());
        pExpander = -1d; 
      }
      long timeExpander = System.currentTimeMillis() - starts;
      Logger.info("PreferenceExpander done in %d sec", timeExpander / 1000);
      
      
      
//      // JAYES
//      long startGM = System.currentTimeMillis();
//      long startJayes = 0;
//      int networkSize = -1;
//      double pJayes = -1;
//      int jayesJunctionTreeWidth = -1;
//      long timeGM = -1;
//      long timeJayes = -1;
//      long timeGMJayes = -1;
//
//      try {
//        // GM Common
//        GraphicalModel gm = new GraphicalModel(model, v);
//        gm.build();
//        networkSize = gm.getNetworkSize();
//        timeGM = System.currentTimeMillis() - startGM;
//        
//        // JAYES
//        startJayes = System.currentTimeMillis();
//        JayesInferator jayesInferator = new JayesInferator(gm);
//        pJayes = jayesInferator.getProbability();
//
//        jayesJunctionTreeWidth = jayesInferator.getJunctionTreeWidth();
//        timeJayes = System.currentTimeMillis() - startJayes;
//        timeGMJayes = timeGM + timeJayes;
//      }
//      catch (OutOfMemoryError e) {
//        Logger.info("Out of memory for Jayes. Turning off..."); 
//        pJayes = -1;
//        timeJayes = System.currentTimeMillis() - startJayes;
//        timeGMJayes = timeGM + timeJayes;
//      }
//      Logger.info("Jayes done in %d sec", timeGMJayes / 1000);
//      

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
      }
      Logger.info("%d AMPs done in %d sec", amps[amps.length-1], timeAmps[timeAmps.length-1] / 1000);


      // SampleSearch
      long startGM = System.currentTimeMillis();
      GraphicalModel gm = new GraphicalModel(model, v);
      gm.build();
      Logger.info("Graphical model built in %d ms", System.currentTimeMillis() - startGM);
      
      SampleSearchInferator ss10 = new SampleSearchInferator(gm);
      Double pss10 = ss10.exec(10);
      
      SampleSearchInferator ss60 = new SampleSearchInferator(gm);
      Double pss60 = ss60.exec(60);
      
      
      // String line = String.format("%d,%.1f,%s,%d", m, phi, vName, v.size());
      String line = String.format("%d,%.1f,%s,%d,%d", m, phi, vName, v.length(), v.size());
      line += String.format(",%f,%d,%d,%d,%d,%d", Math.log(pExpander), timeExpander, expander.getMaxWidth(), expander.getSumWidth(), expander.getMaxStates(), expander.getSumStates());
      // line += String.format(",%f,%d", Math.log(pSexpander), timeSexpander);      
      // line += String.format(",%d,%d,%f,%d,%d,%d", networkSize, jayesJunctionTreeWidth, Math.log(pJayes), timeGM, timeJayes, timeGMJayes);
      
      line += String.format(",%d,%f,%d", 10, pss10, ss10.getTime());
      line += String.format(",%d,%f,%d", 60, pss60, ss60.getTime());
      
      for (int k = 0; k < amps.length; k++) {
        line += String.format(",%d,%f,%d", amps[k], Math.log(pAmps[k]), timeAmps[k]);
      }
      
      // line += String.format(",%f,%d,%d", Math.log(pSexpander), timeSexpander, sexpander.getRankingCount());

      out.println(line);
      out.flush();
      
    }
  }
  
  public static void sevenPref(PrintWriter out) throws IOException, InterruptedException {
    Random random = new Random();
    int[] its = { 45, 50, 55, 60, 65, 70 };
    double[] phis = { 0.2, 0.5, 0.8 };
    double[] misses = { 0.5 };
    int[] amps = { 100, 500, 1000, 5000, 10000, 50000 };
    
    for (int i = 0; i < 100000; i++) {
      int m = its[i % its.length];
      ItemSet items = new ItemSet(m);
      items.tagOneBased();
      
      double miss = misses[random.nextInt(misses.length)];
      double phi = phis[random.nextInt(phis.length)];
      
      Ranking reference = items.getReferenceRanking();
      MallowsModel model = new MallowsModel(reference, phi);
      
      Ranking r = items.getRandomRanking();
      MapPreferenceSet v = r.transitiveClosure();
      Filter.removePreferences(v, MissingProbabilities.uniformPairwise(items, miss));
      String vName = v.toString().replace(" ", "").replace(',', ' ');
      // Ranking v = new Ranking(r);
      // Filter.removeItems(v, miss);
      // String vName = v.toString();
      
      
      Logger.info("Executing test %d: %d items, %d / %d pairs", i+1, m, v.size(), r.size());

      long timeout = 20 * Utils.ONE_MINUTE;
      
      // SPAN EXPANDER
//      long startSpan = System.currentTimeMillis();
//      SpanExpander sexpander = new SpanExpander(model);
//      sexpander.setTimeout(timeout);
//      Double pSexpander;
//      try { pSexpander = sexpander.getProbability(v); }
//      catch (TimeoutException te) { 
//        Logger.info(te.getMessage());
//        pSexpander = -1d; 
//      }
//      long timeSexpander = System.currentTimeMillis() - startSpan;
//      Logger.info("SpanExpander done in %d sec", timeSexpander / 1000);

      
      // EXPANDER
      long starts = System.currentTimeMillis();
      PreferenceExpander expander = new PreferenceExpander(model);
      expander.setTimeout(timeout);
      Double pExpander;
      try { pExpander = expander.getProbability(v); }
      catch (TimeoutException te) { 
        Logger.info(te.getMessage());
        pExpander = -1d; 
      }
      long timeExpander = System.currentTimeMillis() - starts;
      Logger.info("PreferenceExpander done in %d sec", timeExpander / 1000);
      
      
      
      // JAYES
      long startGM = System.currentTimeMillis();
      long startJayes = 0;
      int networkSize = -1;
      double pJayes = -1;
      int jayesJunctionTreeWidth = -1;
      long timeGM = -1;
      long timeJayes = -1;
      long timeGMJayes = -1;

//      try {
//        // GM Common
//        GraphicalModel gm = new GraphicalModel(model, v);
//        gm.build();
//        networkSize = gm.getNetworkSize();
//        timeGM = System.currentTimeMillis() - startGM;
//        
//        // JAYES
//        startJayes = System.currentTimeMillis();
//        JayesInferator jayesInferator = new JayesInferator(gm);
//        pJayes = jayesInferator.getProbability();
//
//        jayesJunctionTreeWidth = jayesInferator.getJunctionTreeWidth();
//        timeJayes = System.currentTimeMillis() - startJayes;
//        timeGMJayes = timeGM + timeJayes;
//      }
//      catch (OutOfMemoryError e) {
//        Logger.info("Out of memory for Jayes. Turning off..."); 
//        pJayes = -1;
//        timeJayes = System.currentTimeMillis() - startJayes;
//        timeGMJayes = timeGM + timeJayes;
//      }
//      Logger.info("Jayes done in %d sec", timeGMJayes / 1000);
      

      // AMP
      AMPSampler ampSampler = new AMPSampler(model);
      double[] b = new double[0];
      double[] pAmps = new double[amps.length];
      long[] timeAmps = new long[amps.length];
      long startAmp = System.currentTimeMillis();

      for (int k = 0; k < amps.length; k++) {
        int as = amps[k];
        if (as == 50000) {
          pAmps[k] = -1;
          timeAmps[k] = -1;
          continue;
        }
        int reps = as - b.length;
        double[] a = ampSampler.samplePosteriors(v, reps);
        b = MathUtils.concat(b, a);
        pAmps[k] = MathUtils.mean(b);
        timeAmps[k] = System.currentTimeMillis() - startAmp;
      }
      Logger.info("AMPs done in %d sec", timeAmps[timeAmps.length-1] / 1000);


      // SampleSearch
      GraphicalModel gm = new GraphicalModel(model, v);
      gm.build();
      
      SampleSearchInferator ss10 = new SampleSearchInferator(gm);
      Double pss10 = ss10.exec(10);
      
      SampleSearchInferator ss60 = new SampleSearchInferator(gm);
      Double pss60 = ss60.exec(60);
      
      // HEADER
      // items,phi,ranking,pref,size,log_p_exp,time_exp,exp_max_width,exp_sum_width,exp_max_states,exp_sum_states,jayes_tree_width,log_p_jayes,time_gn,time_jayes,time_gm_jayes,ss_10,log_p_ss_10,time_ss_10,ss_60,log_p_ss_60,time_ss_60,amp_100,log_p_amp_100,time_amp_100,amp_500,log_p_amp_500,time_amp_500,amp_1000,log_p_amp_1000,time_amp_1000,amp_5000,log_p_amp_5000,time_amp_5000,amp_10000,log_p_amp_10000,time_amp_10000,amp_50000,log_p_amp_50000,time_amp_50000
      
      String line = String.format("%d,%.1f,%s,%.1f,%d", m, phi, vName, miss, v.size());
      line += String.format(",%f,%d,%d,%d,%d,%d", Math.log(pExpander), timeExpander, expander.getMaxWidth(), expander.getSumWidth(), expander.getMaxStates(), expander.getSumStates());
      // line += String.format(",%f,%d,%d", Math.log(pSexpander), timeSexpander, sexpander.getRankingCount());  
      line += String.format(",%d,%d,%f,%d,%d,%d", networkSize, jayesJunctionTreeWidth, Math.log(pJayes), timeGM, timeJayes, timeGMJayes);
      
      line += String.format(",%d,%f,%d", 10, pss10, ss10.getTime());
      line += String.format(",%d,%f,%d", 60, pss60, ss60.getTime());
      
      for (int k = 0; k < amps.length; k++) {
        line += String.format(",%d,%f,%d", amps[k], Math.log(pAmps[k]), timeAmps[k]);
      }

      out.println(line);
      out.flush();
      
    }
  }
}
