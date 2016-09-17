package edu.drexel.cs.db.db4pref.test;

import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.MapPreferenceSet;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.data.PreferenceIO;
import edu.drexel.cs.db.db4pref.gm.GraphicalModel;
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


public class TestEleven {
  
  private static Random random = new Random();

  public static void main(String[] args) throws Exception {
    // query1();
    // ampTime();
    
    query4();
    // eeSS();
    // query2(5000, 12);
    // query2(20, 20);
    // query2(13);
    // query3();
    // threeItems(args);
  }
 
  
  private static void eeSS() throws IOException, TimeoutException, InterruptedException {
    PrintWriter out = FileUtils.append(new File("eleven.ee.ss.1.csv"));
    int m = 1000;
    ItemSet items = new ItemSet(m);
    items.tagOneBased();
    while (true) {
      MapPreferenceSet v = new MapPreferenceSet(items);
      int pairs = 10;
      while (v.size() < pairs) {
        Item item1 = items.get(random.nextInt(20));
        Item item2 = items.get(random.nextInt(20));
        if (item1.equals(item2)) continue;
        
        try { v.add(item1, item2); }
        catch (IllegalStateException e) {}
      }
      String vName = PreferenceIO.toString(v);
      
      Logger.info("Preference set: %s (%d out of %d items)", vName, v.getItems().size(), m);
      double phi = 0.8;
      MallowsModel model = new MallowsModel(items.getReferenceRanking(), phi);
      String line = String.format("%d,%.1f,%s,%d,%d", m, phi, vName, v.size(), v.getItems().size());

      // EE
      PreferenceExpander expander = new PreferenceExpander(model);
      long start = System.currentTimeMillis();
      double pEE = expander.getProbability(v);
      long timeEE = System.currentTimeMillis() - start;
      line += String.format(",%d,%d,%d,%d", expander.getMaxWidth(), expander.getSumWidth(), expander.getProductWidth(), expander.getSumStates());
      line += String.format(",%d,%f", timeEE, Math.log(pEE));
      Logger.info("EE done in %d ms: %f", timeEE, Math.log(pEE));

      // Logger.info("Estimated states: %.0f, real states: %d", expander.estimateStates(v), expander.getSumStates());
      // Logger.waitKey();
      
      {
        AMPInferator ampInferator = new AMPInferator(model, v);
        long startAMP = System.currentTimeMillis();
        double pAMP = ampInferator.sampleMillis(3000);
        long timeAMP = System.currentTimeMillis() - startAMP;
        line += String.format(",%d,%f", timeAMP, Math.log(pAMP));
        Logger.info("Sampled %d in %.1f sec: %f", ampInferator.getCount(), 0.001 * timeAMP, Math.log(pAMP));          
      }
      
      Logger.waitKey();
      
//      // GM
//      long startGM = System.currentTimeMillis();
//      GraphicalModel gm = new GraphicalModel(model, v);
//      gm.build();
//      long timeGM = System.currentTimeMillis() - startGM;
//      Logger.info("Graphical model built in %d ms. GM size: %d", timeGM, gm.getNetworkSize());
//      line += String.format(",%d,%d,%d", timeGM, gm.getNetworkSize(), gm.getVariables().size());
//
//      
//      // SS
//      SampleSearchInferator1 ss = new SampleSearchInferator1(gm);
//      int tSS = 1;
//      Double pSS = ss.exec(tSS);
//      long timeSS = ss.getTime();
//      Logger.info("SampleSearch done in %.1f sec (requested %d sec): %f", 0.001 * timeSS, tSS, pSS);
//      line += String.format(",%d,%d,%d,%f", tSS, timeSS, timeGM + timeSS, pSS);
        
      out.println(line);
      out.flush();
    }
  }
  
  public static void threeItems(String[] args) throws IOException, TimeoutException, InterruptedException {
    PrintWriter out = FileUtils.append(new File("eleven.three.csv"));
    while (true) {
      int m = 100 * (random.nextInt(5) + 1);
      int it = random.nextInt(2) + 2;
      shortRanking(out, m, it);
    }
  }
  
  private static void ampTime() throws IOException {
    PrintWriter out = FileUtils.append(new File("eleven.amp.time.csv"));
    int[] ms = { 100, 200, 500, 1000, 1500, 2000, 300, 4000, 5000, 6000, 8000, 10000, 15000, 20000, 25000, 30000 };
    int[] its = { 2, 4, 6, 8, 10, 15, 20, 30, 40, 50 };
    while (true) {
      for (int m: ms) {
        ItemSet items = new ItemSet(m);
        items.tagOneBased();
        int it = its[random.nextInt(its.length)];
        Ranking v = new Ranking(items);
        Item minItem = null;
        Item maxItem = null;
        while (v.length() < it) {
          Item item = items.get(random.nextInt(m));
          if (v.contains(item)) continue;
          v.add(item);
          if (minItem == null || item.getId() < minItem.getId()) minItem = item;
          if (maxItem == null || item.getId() > maxItem.getId()) maxItem = item;
        }
        
        Logger.info("Ranking: %s (%d out of %d items)", v.toString(), it, m);
        
        double phi = 0.85;
        MallowsModel model = new MallowsModel(items.getReferenceRanking(), phi);
        String line = String.format("%d,%.1f,%s,%d,%d", m, phi, v, v.size(), v.getItems().size());
        
        {
          AMPInferator ampInferator = new AMPInferator(model, v);
          long startAMP = System.currentTimeMillis();
          double pAMP = ampInferator.sampleCount(1);
          long timeAMP = System.currentTimeMillis() - startAMP;
          line += String.format(",%d,%f", timeAMP, Math.log(pAMP));
          Logger.info("Sampled 1 in %.1f sec: %f", 0.001 * timeAMP, Math.log(pAMP));          
        }
        
        out.println(line);
        out.flush();
      }
    }
  }
  
  private static void shortRanking(PrintWriter out, int m, int its) throws IOException, TimeoutException {
    ItemSet items = new ItemSet(m);
    Ranking v = new Ranking(items);
    Item minItem = null;
    Item maxItem = null;
    while (v.length() < its) {
      Item item = items.get(random.nextInt(m));
      if (v.contains(item)) continue;
      v.add(item);
      if (minItem == null || item.getId() < minItem.getId()) minItem = item;
      if (maxItem == null || item.getId() > maxItem.getId()) maxItem = item;
    }
    
    items.tagOneBased();
    double[] phis = { 0.5 };
    String vName = v.toString();
    Logger.info("Ranking: %s (%d out of %d items)", vName, its, m);

    for (double phi: phis) {
      String line = String.format("%d,%.1f,%s,%d,%d", m, phi, vName, v.size(), v.getItems().size());
      line += String.format(",%s,%s", minItem, maxItem);
      
      // EE
      MallowsModel model = new MallowsModel(items.getReferenceRanking(), phi);
      PreferenceExpander expander = new PreferenceExpander(model);
      long start = System.currentTimeMillis();
      double pEE = expander.getProbability(v);
      long timeEE = System.currentTimeMillis() - start;
      line += String.format(",%f,%d", Math.log(pEE), timeEE);
      Logger.info("EE done in %d ms", timeEE);


      // AMP
      {
        AMPInferator ampInferator = new AMPInferator(model, v);
        long timeAMP = System.currentTimeMillis();
        double pAMP = ampInferator.sampleMillis(timeEE / 10);
        timeAMP = System.currentTimeMillis() - timeAMP;
        line += String.format(",%d,%f", timeAMP, Math.log(pAMP));
      }

      {
        AMPInferator ampInferator = new AMPInferator(model, v);
        long timeAMP = System.currentTimeMillis();
        double pAMP = ampInferator.sampleMillis(timeEE / 2);
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

      {
        SampleSearchInferator1 ss = new SampleSearchInferator1(gm);
        int tSS = 1;
        try {
          ss.setTimeout(20 * Utils.ONE_MINUTE);
          Double pSS = ss.exec(tSS);
          long timeSS = ss.getTime();
          Logger.info("SampleSearch done in %d sec (requested %d sec): %f", timeSS / 1000, tSS, pSS);
          line += String.format(",%d,%d,%d,%f", tSS, timeSS, timeGM + timeSS, pSS);
        }
        catch (InterruptedException e) {
          Logger.info("SampleSearch stopped after 20 minutes");
          line += String.format(",%d,%d,%d,%f", tSS, -1, -1, Double.NaN);
        }
      }
      // line += String.format(",%d,%d,%f", 1, -1, Double.NaN);


      Logger.info("Done %s: %f", v, Math.log(pEE));
      out.println(line);
      out.flush();
    }
  }
  
  
  private static void query1() throws IOException, TimeoutException {
    PrintWriter out = new PrintWriter(System.out); // FileUtils.append(new File("eleven.queries.csv"));
    ItemSet items = new ItemSet(20);
    items.tagOneBased();
    MapPreferenceSet pref = new MapPreferenceSet(items);
    pref.addByTag(15, 1);
    pref.addByTag(15, 3);
    pref.addByTag(15, 5);
    pref.addByTag(15, 7);
    pref.addByTag(15, 9);
    query(out, pref);
  }
  
  private static void query2(int m, int top) throws IOException, TimeoutException {
    PrintWriter out = FileUtils.append(new File("eleven.queries.csv"));
    ItemSet items = new ItemSet(m);
    items.tagOneBased();
    MapPreferenceSet pref = new MapPreferenceSet(items);
    for (int i = 1; i <= 10; i++) {
      pref.addByTag(top, i);
    }
    query(out, pref);
  }
  
  private static void query3() throws IOException, TimeoutException {
    PrintWriter out = FileUtils.append(new File("eleven.queries.csv"));
    ItemSet items = new ItemSet(100);
    items.tagOneBased();
    MapPreferenceSet pref = new MapPreferenceSet(items);
    for (int i = 1; i <= 10; i++) {
      pref.addByTag(100, i);
    }
    query(out, pref);
  }
  
  
  private static void query4() throws IOException, TimeoutException, InterruptedException {
    PrintWriter out = new PrintWriter(System.out);
    int m = 100;
    ItemSet items = new ItemSet(m);
    items.tagOneBased();
    MapPreferenceSet pref = new MapPreferenceSet(items);
    pref.addByTag(1, 10);
    pref.addByTag(10, 20);
    pref.addByTag(20, 30);
    pref.addByTag(30, 40);
    queryConvergence(out, pref);
  }
  
  
   
  private static void queryConvergence(PrintWriter out, PreferenceSet pref) throws IOException, TimeoutException, InterruptedException {
    ItemSet items = pref.getItemSet();
    double[] phis = { 0.5 };
    String vName = pref.toString();

    for (double phi: phis) {
      Logger.info("Testing %s", pref);
      String line = String.format("%d,%.1f,%s,%d,%d", items.size(), phi, vName, pref.size(), pref.getItems().size());
      MallowsModel model = new MallowsModel(items.getReferenceRanking(), phi);
      
      // EE
      PreferenceExpander expander = new PreferenceExpander(model);
      long start = System.currentTimeMillis();
      double pEE = expander.getProbability(pref);
      long timeEE = System.currentTimeMillis() - start;
      line += String.format(",%f,%d,%d,%d", Math.log(pEE), timeEE, expander.getMaxWidth(), expander.getSumWidth());
      Logger.info("EE done in %d ms: %f", timeEE, Math.log(pEE));

      long startGM = System.currentTimeMillis();
      GraphicalModel gm = new GraphicalModel(model, pref);
      gm.build();
      long timeGM = System.currentTimeMillis() - startGM;
      Logger.info("Graphical model built in %d ms. GM size: %d", timeGM, gm.getNetworkSize());
      line += String.format(",%d,%d,%d", timeGM, gm.getNetworkSize(), gm.getVariables().size());

      
      
      long time = 0;
      while (time < 600000) {
        // SS
        SampleSearchInferator1 ss = new SampleSearchInferator1(gm);
        int sec = (int) (time / 1000);
        Double logpSS = ss.exec(sec);
        long timeSS = ss.getTime();
        double errSS = Math.abs(pEE - Math.exp(logpSS)) / pEE;
        Logger.info("SampleSearch done in %d ms (requested %d sec): %f. Error: %f", timeSS, sec, logpSS, errSS);
        // line += String.format(",%d,%d,%d,%f", sec, timeSS, timeGM + timeSS, pSS);
        
        // AMP
        AMPInferator ampInferator = new AMPInferator(model, pref);
        double pAMP = ampInferator.sampleMillis(timeSS);
        double errAMP = Math.abs(pAMP - pEE) / pEE;
        Logger.info("AMP after %d ms: %f; err: %f", timeSS, Math.log(pAMP), errAMP);
        if (errSS < errAMP) Logger.info("=========== FOUND!");
        
        double logErrorSS = Math.abs(logpSS - Math.log(pEE));
        double logErrorAMP = Math.abs(Math.log(pAMP) - Math.log(pEE));
        if (logErrorSS < logErrorAMP) Logger.info("=========== FOUND: EE %f, SS %f, AMP %f", Math.log(pEE), logpSS, Math.log(pAMP));
        
        time = (long) (Math.max(time, timeSS) * 1.2);
      }




      // Logger.info("Done %s: %f", pref, Math.log(pEE));
      //out.println(line);
      // out.flush();
    }
  }
  
   
  private static void query(PrintWriter out, PreferenceSet pref) throws IOException, TimeoutException {
    ItemSet items = pref.getItemSet();
    double[] phis = { 0.5 };
    String vName = pref.toString();

    for (double phi: phis) {
      Logger.info("Testing %s", pref);
      String line = String.format("%d,%.1f,%s,%d,%d", items.size(), phi, vName, pref.size(), pref.getItems().size());
      MallowsModel model = new MallowsModel(items.getReferenceRanking(), phi);
      
      // EE
      PreferenceExpander expander = new PreferenceExpander(model);
      long start = System.currentTimeMillis();
      double pEE = expander.getProbability(pref);
      long timeEE = System.currentTimeMillis() - start;
      line += String.format(",%f,%d,%d,%d", Math.log(pEE), timeEE, expander.getMaxWidth(), expander.getSumWidth());
      Logger.info("EE done in %d ms: %f", timeEE, Math.log(pEE));



      // SS
      long startGM = System.currentTimeMillis();
      GraphicalModel gm = new GraphicalModel(model, pref);
      gm.build();
      long timeGM = System.currentTimeMillis() - startGM;
      Logger.info("Graphical model built in %d ms. GM size: %d", timeGM, gm.getNetworkSize());
      line += String.format(",%d,%d,%d", timeGM, gm.getNetworkSize(), gm.getVariables().size());

      
      int[] secs = { 1 };
      for (int sec: secs) {
        SampleSearchInferator1 ss = new SampleSearchInferator1(gm);
        int tSS = sec;
        try {
          Double pSS = ss.exec(tSS);
          long timeSS = ss.getTime();
          Logger.info("SampleSearch done in %d sec (requested %d sec): %f", timeSS / 1000, tSS, pSS);
          line += String.format(",%d,%d,%d,%f", tSS, timeSS, timeGM + timeSS, pSS);
        }
        catch (InterruptedException e) {
          Logger.info("SampleSearch stopped after 20 minutes");
          line += String.format(",%d,%d,%d,%f", tSS, -1, -1, Double.NaN);
        }
      }
      
      // line += String.format(",%d,%d,%f", 1, -1, Double.NaN);

      
      // AMP
      int[] ampTimes = { 1 };
      // for (int ampTime: ampTimes) {
      AMPInferator ampInferator = new AMPInferator(model, pref);
      long timeAMP = System.currentTimeMillis();
      
      
      Logger.info("pEE: %f", Math.log(pEE));
      while (true) {
        double pAMP = ampInferator.sampleCount(1000);
        double epEE = Math.exp(pEE);
        
        double err = Math.abs(pAMP - epEE) / epEE;
        Logger.info("AMP iteration: %f; err: %f", Math.log(pAMP), err);
        if (err < 0.05) break;
        
        // line += String.format(",%d,%f", timeAMP, Math.log(pAMP));
      }
      Logger.info("AMP: %d ms, %d samples", System.currentTimeMillis() - timeAMP, ampInferator.getCount());
      

      // Logger.info("Done %s: %f", pref, Math.log(pEE));
      out.println(line);
      out.flush();
    }
  }
  
}
