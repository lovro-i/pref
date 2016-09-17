package edu.drexel.cs.db.db4pref.test;

import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.MapPreferenceSet;
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
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.concurrent.TimeoutException;


public class TestTwelve {
  
  private static Random random = new Random();


  public static void ampTestOptimized() throws TimeoutException {
    long sumN = 0;
    long sumO = 0;
    while (true) {
      MapPreferenceSet v = generate(25, 4, 5);
      ItemSet items = v.getItemSet();
      MallowsModel model = new MallowsModel(items.getReferenceRanking(), 0.8);
      
      double p1, p2;
      
      
      // EE
      PreferenceExpander expander = new PreferenceExpander(model);
      double pEE = expander.getProbability(v);
        
      {
        AMPInferator ampInferator = new AMPInferator(model, v);
        long s1 = System.currentTimeMillis();
        p1 = ampInferator.sampleCount(10000);
        sumN += System.currentTimeMillis() - s1;
      }
      
      {
        AMPInferator ampInferator = new AMPInferator(model, v);
        long s1 = System.currentTimeMillis();
        p2 = ampInferator.sampleCountOptimized(10000);
        sumO += System.currentTimeMillis() - s1;
      }
      
      double speedup = 1d * sumN / sumO;
      Logger.info("Non-optimized: %.1f sec, Optimized: %.1f sec, Speedup: %.2fx: %f, %f, %f ", 0.001 * sumN, 0.001 * sumO, speedup, Math.log(p1), Math.log(p2), Math.log(pEE));
    }
  }
  
  public static MapPreferenceSet generate(int m, int type, int pairs) {
    int box = Math.min(10, m/2);
    ItemSet items = new ItemSet(m);
    items.tagOneBased();
    MapPreferenceSet pref = new MapPreferenceSet(items);
 
    if (type == 0) {
      while (pref.size() < pairs) {
        Item item1 = items.get(random.nextInt(box));
        Item item2 = items.get(random.nextInt(box));
        
        try { pref.add(item1, item2); }
        catch (IllegalStateException e) {}
      }
    }
    else if (type == 1) {
      while (pref.size() < pairs) {
        Item item1 = items.get(m - box + random.nextInt(box));
        Item item2 = items.get(m - box + random.nextInt(box));
        
        try { pref.add(item1, item2); }
        catch (IllegalStateException e) {}
      }
    }
    else if (type == 2) {
      while (pref.size() < pairs) {
        Item item1 = items.get(random.nextInt(box));
        Item item2 = items.get(m - box + random.nextInt(box));
        
        try { pref.add(item1, item2); }
        catch (IllegalStateException e) {}
      }
    }
    else if (type == 3) {
      int left = 0;
      while (pref.size() < pairs) {
        Item item1 = items.get(left + random.nextInt(box));
        Item item2 = items.get((left + (m - box) / 2 + random.nextInt(box)) % m);
        
        try { 
          pref.add(item1, item2);
          left = (left + (m-box)/2) % m;
        }
        catch (IllegalStateException e) {}
        
      }
    }
    else if (type == 4) {
      while (pref.size() < pairs) {
        Item item1 = items.get(random.nextInt(m));
        Item item2 = items.get(random.nextInt(m));
        
        try { pref.add(item1, item2); }
        catch (IllegalStateException e) {}
      }
    }
    else if (type == 5) {
      while (pref.size() < pairs) {
        Item item1 = items.get(m - box + random.nextInt(box));
        Item item2 = items.get(random.nextInt(box));
        
        try { pref.add(item1, item2); }
        catch (IllegalStateException e) {}
      }
    }
    else if (type == 6) {
      while (pref.size() < pairs) {
        Item item1 = items.get(m - box + random.nextInt(box));
        Item item2 = items.get(random.nextInt(box));
        
        if (random.nextInt(2) == 0) {
          Item temp = item1;
          item1 = item2;
          item2 = temp;
        }
        
        try { pref.add(item1, item2); }
        catch (IllegalStateException e) {}
      }
    }
    
    return pref;
  }
  
  private static void ampTest1() throws IOException, InterruptedException {
    PrintWriter out = FileUtils.append(new File("twelve.amp.csv"));
    int types = 7;
    while (true) {
      int pairs = 2 + random.nextInt(4);
      int type = random.nextInt(types);
      MapPreferenceSet v = generate(40, type, pairs);
      ItemSet items = v.getItemSet();
      String vName = PreferenceIO.toString(v);
      Logger.info("Preference set: %s (%d out of %d items), type %d", vName, v.getItems().size(), items.size(), type);
      
      double phi = 0.8;
      MallowsModel model = new MallowsModel(items.getReferenceRanking(), phi);
      String line = String.format("%d,%.1f,%s,%d,%d,%d", items.size(), phi, vName, v.size(), v.getItems().size(), type);
      
      // MAX ITEM
      int maxItem = 0;
      int minItem = items.size();
      for (Item it: v.getItems()) {
        maxItem = Math.max(maxItem, (Integer) it.getTag());
        minItem = Math.min(minItem, (Integer) it.getTag());
      }
      line += String.format(",%d,%d,%d", minItem, maxItem, maxItem - minItem);
      
      // EE
      PreferenceExpander expander = new PreferenceExpander(model);
      long timeout = Utils.ONE_HOUR;
      expander.setTimeout(timeout);
      long start = System.currentTimeMillis();
      double pEE;
      long timeEE;
      try {
        pEE = expander.getProbability(v);
        timeEE = System.currentTimeMillis() - start;
      }
      catch (TimeoutException te) {
        pEE = -1;
        timeEE = -timeout;
      }
      line += String.format(",%d,%d,%d,%d", expander.getMaxWidth(), expander.getSumWidth(), expander.getProductWidth(), expander.getSumStates());
      line += String.format(",%d,%f", timeEE, Math.log(pEE));
      Logger.info("EE done in %d ms: %f", timeEE, Math.log(pEE));


      // AMP
      int[] secs = { 1, 2, 3, 4, 6, 8, 10, 15, 20 };
      AMPInferator ampInferator = new AMPInferator(model, v);
      long totalTime = 0;
      for (int sec: secs) {
        long time = (sec - totalTime) * 1000;
        totalTime = sec;
        double pAMP = ampInferator.sampleMillis(time);
        line += String.format(",%d,%f", totalTime, Math.log(pAMP));
        Logger.info("Sampled %d in %d sec: %f", ampInferator.getCount(), totalTime, Math.log(pAMP));          
      }
      
      // Logger.info("================== " + line);
      out.println(line);
      out.flush();
    }
  }
  
  
  
  private static void ampTest2() throws IOException, InterruptedException {
    PrintWriter out = FileUtils.append(new File("twelve.two.long.csv"));
    int types = 7;
    while (true) {
      int pairs = 2 + random.nextInt(4);
      int type = 4; // random.nextInt(types + 4);
      // if (type >= types) type = 4;
      int its = 50;
      MapPreferenceSet v = generate(its, type, pairs);
      ItemSet items = v.getItemSet();
      String vName = PreferenceIO.toString(v);
      
      
      double phi = 0.8;
      MallowsModel model = new MallowsModel(items.getReferenceRanking(), phi);
      String line = String.format("%d,%.1f,%s,%d,%d,%d", items.size(), phi, vName, v.size(), v.getItems().size(), type);
      
      // MAX ITEM
      int maxItem = 0;
      int minItem = items.size();
      for (Item it: v.getItems()) {
        maxItem = Math.max(maxItem, (Integer) it.getTag());
        minItem = Math.min(minItem, (Integer) it.getTag());
      }
      line += String.format(",%d,%d,%d", minItem, maxItem, maxItem - minItem);
      
      Logger.info("Preference set: %s (%d out of %d items), max %d", vName, v.getItems().size(), items.size(), maxItem);
      
      // EE
      PreferenceExpander expander = new PreferenceExpander(model);
      long timeout = Utils.ONE_HOUR / 2;
      expander.setTimeout(timeout);
      long start = System.currentTimeMillis();
      double pEE;
      long timeEE;
      boolean eeDone = true;
      try {
        pEE = expander.getProbability(v);
        timeEE = System.currentTimeMillis() - start;
      }
      catch (TimeoutException te) {
        pEE = -1;
        timeEE = -timeout;
        eeDone = false;
      }
      line += String.format(",%d,%d,%d,%d", expander.getMaxWidth(), expander.getSumWidth(), expander.getProductWidth(), expander.getSumStates());
      line += String.format(",%d,%f", timeEE, Math.log(pEE));
      Logger.info("EE done in %d ms: %f", timeEE, Math.log(pEE));


      // AMP
      long amp5 = -1;
      long amp3 = -1;
      long amp1 = -1;
      if (eeDone) {
        AMPInferator ampInferator = new AMPInferator(model, v);
        long ampTime = 0;
        double iterTime = 5;
        long startAmpTime = System.currentTimeMillis();
        while (ampTime < Utils.ONE_MINUTE) {
          double pAMP = ampInferator.sampleMillisOptimized((long) iterTime);
          ampTime = System.currentTimeMillis() - startAmpTime;
          double err = Math.abs(pEE - pAMP) / pEE;
          if (amp5 == -1 && err <= 0.05) amp5 = ampTime;
          if (amp3 == -1 && err <= 0.03) amp3 = ampTime;
          if (amp1 == -1 && err <= 0.01) {
            amp1 = ampTime;
            break;
          }
          iterTime *= 1.1;
        }
        Logger.info("AMP done in %.1f sec", 0.001 * amp1);
      }
      line += String.format(",%d,%d,%d", amp5, amp3, amp1);
      
      
      // GM
      long startGM = System.currentTimeMillis();
      GraphicalModel gm = new GraphicalModel(model, v);
      gm.build();
      long timeGM = System.currentTimeMillis() - startGM;
      Logger.info("Graphical model built in %d ms. GM size: %d", timeGM, gm.getNetworkSize());
      line += String.format(",%d,%d,%d", timeGM, gm.getNetworkSize(), gm.getVariables().size());

      /*
      // SS
      long ss5 = -1;
      long ss3 = -1;
      long ss1 = -1;
      if (eeDone) {
        int maxTime = 60;
        if (amp1 != -1) maxTime = (int) Math.min(maxTime, (amp1 / 1000) + 1);
        int tSS = 0;
        while (tSS <= maxTime) {
          SampleSearchInferator1 ss = new SampleSearchInferator1(gm);
          Double pSS = ss.exec(tSS);
          long timeSS = ss.getTime();
          Logger.info("SS required %d, done in %.1f", tSS, 0.001 * timeSS);
          if (pSS != null) {
            double err = Math.abs(pEE - Math.exp(pSS)) / pEE;
            if (ss5 == -1 && err <= 0.05) ss5 = timeSS;
            if (ss3 == -1 && err <= 0.03) ss3 = timeSS;
            if (ss1 == -1 && err <= 0.01) {
              ss1 = timeSS;
              break;
            }
          }

          int last = (int) (timeSS / 1000);
          tSS = Math.max(last, tSS) + 1;
        }
        Logger.info("SampleSearch done in %d sec", tSS-1);
      }
      line += String.format(",%d,%d,%d", ss5, ss3, ss1);
      Logger.info("AMP 3%%: %d ms, SS 3%%: %d ms", amp3, ss3);
      */
      
      out.println(line);
      out.flush();
    }
  }
  
  
  public static void gm() throws IOException {
    PrintWriter out = FileUtils.append(new File("twelve.gm.csv"));
    int types = 7;
    int itss[] = {30, 40, 50 };
    while (true) {
      int pairs = 2 + random.nextInt(4);
      int type = 4;
      int its = 35 + random.nextInt(16);
      MapPreferenceSet v = generate(its, type, pairs);
      ItemSet items = v.getItemSet();
      String vName = PreferenceIO.toString(v);
      
      
      double phi = 0.8;
      MallowsModel model = new MallowsModel(items.getReferenceRanking(), phi);
      String line = String.format("%d,%.1f,%s,%d,%d,%d", items.size(), phi, vName, v.size(), v.getItems().size(), type);
      
      // MAX ITEM
      int maxItem = 0;
      int minItem = items.size();
      for (Item it: v.getItems()) {
        maxItem = Math.max(maxItem, (Integer) it.getTag());
        minItem = Math.min(minItem, (Integer) it.getTag());
      }
      line += String.format(",%d,%d,%d", minItem, maxItem, maxItem - minItem);
      
      Logger.info("Preference set: %s (%d out of %d items), max %d", vName, v.getItems().size(), items.size(), maxItem);
      
      // GM
      long startGM = System.currentTimeMillis();
      GraphicalModel gm = new GraphicalModel(model, v);
      gm.build();
      long timeGM = System.currentTimeMillis() - startGM;
      Logger.info("Graphical model built in %d ms. GM size: %d", timeGM, gm.getNetworkSize());
      line += String.format(",%d,%d,%d,%d", timeGM, gm.getNetworkSize(), gm.getVariables().size(), gm.getNonZeroNetworkSize());
      
      
      out.println(line);
      out.flush();
    }
  }
  
  public static void convertNine(File inFile, File outFile) throws IOException {
    List<String> lines = FileUtils.readLines(inFile);
    PrintWriter out = FileUtils.write(outFile);
    out.println(lines.get(0)+",max_item");
    
    for (int i = 1; i < lines.size(); i++) {
      String line = lines.get(i);
      StringTokenizer tokenizer = new StringTokenizer(line, ",");
      String items = tokenizer.nextToken();
      String phi = tokenizer.nextToken();
      String miss = tokenizer.nextToken();
      String pref = tokenizer.nextToken();
      
      StringTokenizer tok = new StringTokenizer(pref, "[] >");
      int maxItem = -1;
      while (tok.hasMoreTokens()) {
        String t = tok.nextToken();
        int it = Integer.parseInt(t);
        maxItem = Math.max(maxItem, it);
      }
      
      out.println(line+","+maxItem);
    }
    out.close();
  }
  

  public static void main(String[] args) throws Exception {
    gm();
    // ampTest2();
    
//    File folder = new File("C:\\Projects\\Rank\\Results\\2016.08b");
//    
//    convertNine(new File(folder, "nine.four.csv"), new File(folder, "nine.four.plus.csv"));
//    convertNine(new File(folder, "nine.five.csv"), new File(folder, "nine.five.plus.csv"));
  }

}
