package edu.drexel.cs.db.db4pref.test;

import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.MapPreferenceSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.data.PreferenceIO;
import edu.drexel.cs.db.db4pref.filter.Filter;
import edu.drexel.cs.db.db4pref.filter.MissingProbabilities;
import edu.drexel.cs.db.db4pref.gm.GraphicalModel;
import edu.drexel.cs.db.db4pref.gm.SampleSearchInferator;
import edu.drexel.cs.db.db4pref.gm.SampleSearchInferator1;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.posterior.AMPInferator;
import edu.drexel.cs.db.db4pref.posterior.PreferenceExpander;
import edu.drexel.cs.db.db4pref.util.FileUtils;
import edu.drexel.cs.db.db4pref.util.Logger;
import edu.drexel.cs.db.db4pref.util.Utils;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import java.util.concurrent.TimeoutException;


public class TestTen {

  public static void main(String[] args) throws Exception {
    // ssExample();
    // generate(args);
    // generateAmp(args);
    // verySpecific(args);
    // specificShifted(args);
    everyTen(args);
  }
  
 
  
  
  /** An example that should run SampleSearch for 10 seconds, but instead it runs much longer. */
  public static void ssExample() throws IOException, InterruptedException {
    int m = 30;
    double miss = 0.8;
    double phi = 0.8;
    
    ItemSet items = new ItemSet(m);
    items.tagOneBased();
    Ranking reference = items.getReferenceRanking();
    MallowsModel model = new MallowsModel(reference, phi);

    Ranking r = items.getRandomRanking();
    MapPreferenceSet v = r.transitiveClosure();
    Filter.removePreferences(v, MissingProbabilities.uniformPairwise(items, miss));
    String vName = PreferenceIO.toString(v);
    
    GraphicalModel gm = new GraphicalModel(model, v);
    gm.build();
    
    int reqTime = 10;
    SampleSearchInferator ss = new SampleSearchInferator(gm);
    Double pss = ss.exec(reqTime);
    long timeSS = ss.getTime();
    
    Logger.info("%d,%.1f,%.1f,%s,%d,%d", m, phi, miss, vName, reqTime, timeSS);
  }
  
  
  public static void generate(String[] args) throws IOException, TimeoutException {
    PrintWriter out = FileUtils.append(new File("dataset.ten.csv"));
    Random random = new Random();
    
    double[] misses = { 0.2, 0.5, 0.8 };
    int[] limits = { 85, 56, 22 };
    double[] phis = { 0.8 }; // 0.8
    
    
    int it = 10;
    int idx = 0;
    if (args.length > 0) idx = Integer.parseInt(args[0]) % misses.length;
    
    for (int i = 0; i < 100000; i++) {
      System.gc();
      int m = it;
      it += 1;
      double miss = misses[idx];
      int limit = limits[idx];
      double phi = phis[random.nextInt(phis.length)];
      
      ItemSet items = new ItemSet(m);
      items.tagOneBased();
      Ranking reference = items.getReferenceRanking();
      MallowsModel model = new MallowsModel(reference, phi);
      
      Ranking r = items.getRandomRanking();
      MapPreferenceSet v = r.transitiveClosure();
      Filter.removePreferences(v, MissingProbabilities.uniformPairwise(items, miss));
      String vName = PreferenceIO.toString(v);
            
      Logger.info("Executing test %d: %d items (%d of %d pairs), phi = %.1f, miss = %.1f", i+1, m, v.size(), r.size(), phi, miss);

      
      // EXPANDER
      long start = System.currentTimeMillis();
      Double pExpander;
      PreferenceExpander expander = new PreferenceExpander(model);
      int result = 0;
      try {
        pExpander = expander.getProbability(v);
      }
      catch (OutOfMemoryError me) {
        pExpander = 0d;
        result = -1;
      }
      long timeExpander = System.currentTimeMillis() - start;
      pExpander = Math.log(pExpander);
      Logger.info("PreferenceExpander done in %d sec: %f", timeExpander / 1000, pExpander);
      
      String line = String.format("%d,%.1f,%.1f,%s,%d,%d", m, phi, miss, vName, v.size(), v.transitiveClosure().size());
      line += String.format(",%f,%d,%d,%d,%d,%d", pExpander, timeExpander, expander.getMaxWidth(), expander.getSumWidth(), expander.getMaxStates(), expander.getSumStates());
      line += String.format(",%d,%d,%d,%d", result, Runtime.getRuntime().freeMemory(), Runtime.getRuntime().maxMemory(), Runtime.getRuntime().totalMemory());
      out.println(line);
      out.flush();
      
      if (it > limit) {
        it = 10;
        idx = (idx + 1) % misses.length;
      }
    }
    out.close();
  }
  
  public static void specificShifted(String[] args) throws Exception {
    PrintWriter out = FileUtils.append(new File("specific.shifted.csv"));
    
    int[] items = { 120, 140, 160, 180, 220, 240, 260, 280 };
    for (int it: items) {
      specificShiftedOne(out, it);
    }
  }
  
  
  private static void specificShiftedOne(PrintWriter out, int m) throws TimeoutException, IOException, InterruptedException {
    ItemSet items = new ItemSet(m);
    items.tagOneBased();
    double[] phis = { 0.2, 0.5, 0.8 };
    MapPreferenceSet v = new MapPreferenceSet(items);
    v.addByTag(m, m-20);
    v.addByTag(m, m-10);
    v.addByTag(m-20, m-25);
    v.addByTag(m-10, m-23);
    String vName = PreferenceIO.toString(v);
    
    for (double phi: phis) {
      
      // EE
      MallowsModel model = new MallowsModel(items.getReferenceRanking(), phi);
      PreferenceExpander expander = new PreferenceExpander(model);
      long start = System.currentTimeMillis();
      double pEE = expander.getProbability(v);
      long time = System.currentTimeMillis() - start;
      
      String line = String.format("%d,%.1f,%s,%d,%d,%f,%d", m, phi, vName, v.size(), v.getItems().size(), Math.log(pEE), time);
      
      
      // AMP
      {
        AMPInferator ampInferator = new AMPInferator(model, v);
        long timeAMP = System.currentTimeMillis();
        double pAMP = ampInferator.sampleMillis(time / 10);
        timeAMP = System.currentTimeMillis() - timeAMP;
        line += String.format(",%d,%f", timeAMP, Math.log(pAMP));
      }
      
      {
        AMPInferator ampInferator = new AMPInferator(model, v);
        long timeAMP = System.currentTimeMillis();
        double pAMP = ampInferator.sampleMillis(time / 2);
        timeAMP = System.currentTimeMillis() - timeAMP;
        line += String.format(",%d,%f", timeAMP, Math.log(pAMP));
      }
      
      // SS
      long startGM = System.currentTimeMillis();
      GraphicalModel gm = new GraphicalModel(model, v);
      gm.build();
      long timeGM = System.currentTimeMillis() - startGM;
      Logger.info("Graphical model built in %d ms", timeGM);
      line += String.format(",%d,%d,%d", timeGM, gm.getNetworkSize(), gm.getVariables().size());
      
//      {
//        SampleSearchInferator1 ss = new SampleSearchInferator1(gm);
//        int tSS = 1;
//        Double pSS = ss.exec(tSS);
//        long timeSS = ss.getTime();
//        Logger.info("SampleSearch done in %d sec (requested %d sec): %f", timeSS / 1000, tSS, pSS);
//        line += String.format(",%d,%d,%f", tSS, timeSS, pSS);
//      }
      line += String.format(",%d,%d,%f", 1, -1, Double.NaN);
      
//      { // t/2
//        SampleSearchInferator ss = new SampleSearchInferator(gm);
//        int tSS = (int) Math.round(0.5 * time / 1000);
//        tSS = Math.max(tSS, 1);
//        Double pSS = ss.exec(tSS);
//        long timeSS = ss.getTime();
//        Logger.info("SampleSearch done in %d sec (requested %d sec): %f", timeSS / 1000, tSS, pSS);
//        line += String.format(",%d,%d,%f", tSS, timeSS, pSS);
//      }
      
      Logger.info("%d,%.1f,%d,%f", m, phi, time, Math.log(pEE));
      out.println(line);
      out.flush();
    }
  }
  
  public static void everyTen(String[] args) throws Exception {
    PrintWriter out = FileUtils.append(new File("specific.ten.csv"));
    int[] items = { 300, 320, 350, 400, 450, 500, 550, 600, 650, 700, 750, 800, 850, 900, 950, 1000 };
    for (int it: items) {
      everyTenOne(out, it);
    }
  }
  
  private static void everyTenOne(PrintWriter out, int m) throws TimeoutException, IOException, InterruptedException {
    ItemSet items = new ItemSet(m);
    items.tagOneBased();
    double[] phis = { 0.2, 0.5, 0.8 };
    MapPreferenceSet v = new MapPreferenceSet(items);
    for (int i = 10; i <= m-10; i += 10) {
      v.addByTag(i, i+10);
    }
    String vName = PreferenceIO.toString(v);
    
    for (double phi: phis) {
      
      // EE
      MallowsModel model = new MallowsModel(items.getReferenceRanking(), phi);
      PreferenceExpander expander = new PreferenceExpander(model);
      long start = System.currentTimeMillis();
      double pEE = expander.getProbability(v);
      long time = System.currentTimeMillis() - start;
      
      String line = String.format("%d,%.1f,%s,%d,%d,%f,%d", m, phi, vName, v.size(), v.getItems().size(), Math.log(pEE), time);
      
      
      // AMP t/10
      { 
        AMPInferator ampInferator = new AMPInferator(model, v);
        long timeAMP = System.currentTimeMillis();
        double pAMP = ampInferator.sampleMillis(time / 10);
        timeAMP = System.currentTimeMillis() - timeAMP;
        line += String.format(",%d,%f", timeAMP, Math.log(pAMP));
      }
      
      // AMP t/2
      {
        AMPInferator ampInferator = new AMPInferator(model, v);
        long timeAMP = System.currentTimeMillis();
        double pAMP = ampInferator.sampleMillis(time / 2);
        timeAMP = System.currentTimeMillis() - timeAMP;
        line += String.format(",%d,%f", timeAMP, Math.log(pAMP));
      }
      
      // SS
      long startGM = System.currentTimeMillis();
      GraphicalModel gm = new GraphicalModel(model, v);
      gm.build();
      long timeGM = System.currentTimeMillis() - startGM;
      Logger.info("Graphical model built in %d ms", timeGM);
      line += String.format(",%d,%d,%d", timeGM, gm.getNetworkSize(), gm.getVariables().size());
      
      // SS 1 sec
//      {
//        SampleSearchInferator1 ss = new SampleSearchInferator1(gm);
//        int tSS = 1;
//        Double pSS = ss.exec(tSS);
//        long timeSS = ss.getTime();
//        Logger.info("SampleSearch done in %d sec (requested %d sec): %f", timeSS / 1000, tSS, pSS);
//        line += String.format(",%d,%d,%f", tSS, timeSS, pSS);
//      }
      line += String.format(",%d,%d,%f", 1, -1, Double.NaN);
      
      // SS t/2
//      {
//        SampleSearchInferator ss = new SampleSearchInferator(gm);
//        int tSS = (int) Math.round(0.5 * time / 1000);
//        tSS = Math.max(tSS, 1);
//        Double pSS = ss.exec(tSS);
//        long timeSS = ss.getTime();
//        Logger.info("SampleSearch done in %d sec (requested %d sec): %f", timeSS / 1000, tSS, pSS);
//        line += String.format(",%d,%d,%f", tSS, timeSS, pSS);
//      }
      
      Logger.info("%d,%.1f,%d,%f", m, phi, time, Math.log(pEE));
      out.println(line);
      out.flush();
    }
  }
  
  public static void verySpecific(String[] args) throws TimeoutException, IOException, InterruptedException {
    PrintWriter out = FileUtils.append(new File("specific.one.csv"));
    // PrintWriter out = new PrintWriter(System.out);
    // int[] items = { 30, 50, 100, 200, 500, 1000, 2000, 5000, 10000, 20000, 50000, 100000, 200000, 500000, 1000000 };
    int[] items = { 50 };
    for (int m: items) {
      verySpecificOne(out, m);
    }
  }
  
  private static void verySpecificOne(PrintWriter out, int m) throws TimeoutException, IOException, InterruptedException {
    ItemSet items = new ItemSet(m);
    items.tagOneBased();
    double[] phis = { 0.2, 0.5, 0.8 };
    MapPreferenceSet v = new MapPreferenceSet(items);
    v.addByTag(30, 10);
    v.addByTag(30, 20);
    v.addByTag(10, 5);
    v.addByTag(20, 7);
    String vName = PreferenceIO.toString(v);
    
    for (double phi: phis) {
      MallowsModel model = new MallowsModel(items.getReferenceRanking(), phi);
      PreferenceExpander expander = new PreferenceExpander(model);
      long start = System.currentTimeMillis();
      double p = expander.getProbability(v);
      long time = System.currentTimeMillis() - start;
      String line = String.format("%d,%.1f,%s,%d,%d,%f,%d", m, phi, vName, v.size(), v.getItems().size(), Math.log(p), time);
      Logger.info("%d,%.1f,%d,%f", m, phi, time, Math.log(p));
      
      
      // SS
      long startGM = System.currentTimeMillis();
      GraphicalModel gm = new GraphicalModel(model, v);
      gm.build();
      long timeGM = System.currentTimeMillis() - startGM;
      Logger.info("Graphical model built in %d ms", timeGM);
      line += String.format(",%d,%d,%d", timeGM, gm.getNetworkSize(), gm.getVariables().size());
      
      SampleSearchInferator ss = new SampleSearchInferator(gm);
      int tSS = 60;
      Double pSS = ss.exec(tSS);
      long timeSS = ss.getTime();
      Logger.info("SampleSearch done in %d sec (requested %d sec): %f", timeSS / 1000, tSS, pSS);
      line += String.format(",%d,%d,%f", tSS, timeSS, pSS);
      
      out.println(line);
      out.flush();
    }
  }
  
  
  public static void generateAmp(String[] args) throws IOException, TimeoutException {
    PrintWriter out = FileUtils.append(new File("dataset.amp.csv"));
    Random random = new Random();
    
    double[] misses = { 0.2, 0.5, 0.8 };
    int[] from = { 80, 50, 20 };
    int[] to = { 120, 80, 50 };
    double[] phis = { 0.8 }; // 0.8
    
    
    
    int idx = 0;
    if (args.length > 0) idx = Integer.parseInt(args[0]) % misses.length;
    int it = from[idx];
    
    for (int i = 0; i < 100000; i++) {
      System.gc();
      double miss = misses[idx];
      double phi = phis[random.nextInt(phis.length)];
      
      ItemSet items = new ItemSet(it);
      items.tagOneBased();
      Ranking reference = items.getReferenceRanking();
      MallowsModel model = new MallowsModel(reference, phi);
      
      Ranking r = items.getRandomRanking();
      MapPreferenceSet v = r.transitiveClosure();
      Filter.removePreferences(v, MissingProbabilities.uniformPairwise(items, miss));
      String vName = PreferenceIO.toString(v);
            
      Logger.info("Executing test %d: %d items (%d of %d pairs), phi = %.1f, miss = %.1f", i+1, it, v.size(), r.size(), phi, miss);

      
      // AMP
      AMPInferator ampInferator = new AMPInferator(model, v);
      double pAmp = -1;
      double pAmp2 = -1;
      long start = System.currentTimeMillis();
      while (true) {
        double p = ampInferator.sampleMillis(Utils.ONE_MINUTE);
        if (Double.isInfinite(p)) {
          pAmp = p;
          break;
        }
        if (pAmp == -1 || pAmp2 == -1) {
          pAmp2 = pAmp;
          pAmp = p;
          continue;
        }
        double err1 = Math.abs(pAmp - p) / p;
        double err2 = Math.abs(pAmp2 - p) / p;
        if (err1 < 0.01 && err2 < 0.01) {
          pAmp = p;
          break;
        }
        pAmp2 = pAmp;
        pAmp = p;
        Logger.info("AMP after %d min: %f", (System.currentTimeMillis() - start) / Utils.ONE_MINUTE, Math.log(pAmp));
      }
      pAmp = Math.log(pAmp);
      long timeAmp = System.currentTimeMillis() - start;
      Logger.info("AMP done in %d sec: %f", timeAmp / 1000, pAmp);
      String line = String.format("%d,%.1f,%.1f,%s,%d,%d", it, phi, miss, vName, v.size(), v.transitiveClosure().size());
      line += String.format(",%f,%d", pAmp, timeAmp);
      
      out.println(line);
      out.flush();
      
      int limit = to[idx];
      if (it >= limit) {
        idx = (idx + 1) % misses.length;
        it = from[idx];
      }
      else {
        it++;
      }
    }
    out.close();
  }
  
}
