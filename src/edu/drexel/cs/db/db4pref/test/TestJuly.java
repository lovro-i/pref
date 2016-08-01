package edu.drexel.cs.db.db4pref.test;

import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.MapPreferenceSet;
import edu.drexel.cs.db.db4pref.core.MutablePreferenceSet;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.core.RankingSample;
import edu.drexel.cs.db.db4pref.core.Sample;
import edu.drexel.cs.db.db4pref.data.datasets.CrowdRank;
import edu.drexel.cs.db.db4pref.data.datasets.Sushi;
import edu.drexel.cs.db.db4pref.filter.Filter;
import edu.drexel.cs.db.db4pref.filter.MissingProbabilities;
import edu.drexel.cs.db.db4pref.gm.GraphicalModel;
import edu.drexel.cs.db.db4pref.gm.JayesInferator;
import edu.drexel.cs.db.db4pref.gm.SampleSearchInferator;
import edu.drexel.cs.db.db4pref.mixture.AMPxSMixtureReconstructor;
import edu.drexel.cs.db.db4pref.mixture.MallowsMixtureModel;
import edu.drexel.cs.db.db4pref.mixture.MallowsMixtureReconstructor;
import edu.drexel.cs.db.db4pref.mixture.PreferenceClusterer;
import edu.drexel.cs.db.db4pref.mixture.PreferenceClusters;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.posterior.PreferenceExpander;
import edu.drexel.cs.db.db4pref.sampler.AMPSampler;
import edu.drexel.cs.db.db4pref.util.FileUtils;
import edu.drexel.cs.db.db4pref.util.Logger;
import edu.drexel.cs.db.db4pref.util.MathUtils;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.TimeoutException;


public class TestJuly {

  public static void main(String[] args) throws Exception {
    sushi2(args, 2);
    sushi2(args, 5);
    sushi2(args, 8);
    // while (true) negative();
  }
  
  public static void negative() throws TimeoutException {
    int m = 10;
    ItemSet items = new ItemSet(m);
    items.tagOneBased();

    Random random = new Random();
    double miss = 0.85 * random.nextDouble();
    double phi = 0.5;
    
    Ranking reference = items.getRandomRanking();
    MallowsModel model = new MallowsModel(reference, phi);
    
    Ranking r = items.getRandomRanking(9);
    MapPreferenceSet v = r.transitiveClosure();
    // Filter.removePreferences(v, MissingProbabilities.uniform(items, miss));
    String vName = v.toString().replace(" ", "").replace(',', ' ');
    Logger.info("Executing %s [%d]", vName, m);
    
    PreferenceExpander expander = new PreferenceExpander(model);
    double pExp = expander.getProbability(r);
    
    
    
    AMPSampler ampSampler = new AMPSampler(model);
    double[] a = ampSampler.samplePosteriors(v, 100);
    double pAmp = MathUtils.mean(a);
    
    double lExp = Math.log(pExp);
    double lAmp = Math.log(pAmp);
    Logger.info("Expander: %f, AMP: %f", lExp, lAmp);
    if (lAmp < lExp) {
      Logger.info("!!!!!!!!!!!!!!!!!!!!!!!!!");
      Logger.waitKey();
    }
    System.out.println();
}
  
  
  public static void sushiPrepare() throws Exception {
    File data = new File("C:\\Projects\\Rank\\Data\\sushi\\sushi3a.csv");
    File folder = new File("C:\\Projects\\Rank\\Results\\2016.07");

    Sushi sushi = new Sushi(data);
    RankingSample sample = sushi.getSample();

    PreferenceClusterer clusterer = new PreferenceClusterer(6);
    PreferenceClusters clusters = clusterer.cluster(sample);
    
    Collection<Sample<PreferenceSet>> samples = clusters.getClusters();
    int i = 0;
    for (Sample<PreferenceSet> s: samples) {
      File file = new File(folder, "sushi.sample."+i+".txt");
      s.save(file);
      i++;
    }
    
    MallowsMixtureReconstructor rec = new AMPxSMixtureReconstructor();
    MallowsMixtureModel model = rec.model(clusters);
    
    System.out.println(model);
    PrintWriter out = FileUtils.write(new File(folder, "sushi.model.txt"));
    out.println(model);
    out.close();
    
    ItemSet items = model.getItemSet();
    items.save(new File(folder, "sushi.items.txt"));
  }
  
  
  public static void crowdRank(String[] args) throws IOException, TimeoutException {
    File folder;
    if (args.length == 0) { folder = new File("C:\\Projects\\Rank\\Results\\2016.07"); }
    else { folder = new File(args[0]); }
    
    ItemSet items = ItemSet.load(new File(folder, "crowdrank.items.txt"));
    
    Sample<PreferenceSet> sample = Sample.load(items, new File(folder, "crowdrank.sample.0.txt"));
    
    Ranking center = Ranking.fromStringByTag(items, "161-198-103-382-182-55-438-71-150-13-147-162-221-29-371-350-25-91-436");
    MallowsModel model = new MallowsModel(center, 0.779);
    
    int[] amps = { 100, 1000, 5000, 10000, 50000, 100000 };
    
    PrintWriter out = FileUtils.write(new File(folder, "crowdrank.out.csv"));
    
    for (Sample.PW<PreferenceSet> pw: sample) {
      
      // EXPANDER
      long starts = System.currentTimeMillis();
      PreferenceExpander expander = new PreferenceExpander(model);
      double pExpander = expander.getProbability(pw.p);
      long timeExpander = System.currentTimeMillis() - starts;
      
      // JAYES
      long startJayes = System.currentTimeMillis();
      GraphicalModel gm = new GraphicalModel(model, pw.p);
      gm.build();
      int networkSize = gm.getNetworkSize();
      JayesInferator jayesInferator = new JayesInferator(gm);
      double pJayes = jayesInferator.getProbability();
      int treeWidth = jayesInferator.getJunctionTreeWidth();
      long timeJayes = System.currentTimeMillis() - startJayes;
      
      
      // AMP
      AMPSampler ampSampler = new AMPSampler(model);
      double[] b = new double[0];
      double[] pAmps = new double[amps.length];
      long[] timeAmps = new long[amps.length];
      long startAmp = System.currentTimeMillis();

      for (int k = 0; k < amps.length; k++) {
        int as = amps[k];
        int reps = as - b.length;
        double[] a = ampSampler.samplePosteriors(pw.p, reps);
        b = MathUtils.concat(b, a);
        pAmps[k] = MathUtils.mean(b);
        timeAmps[k] = System.currentTimeMillis() - startAmp;
      }
      
      
      
      String line = String.format("%d,%.3f,%s,%d", items.size(), model.getPhi(), pw.p, pw.p.size());
      line += String.format(",%f,%d,%d,%d,%d,%d", Math.log(pExpander), timeExpander, expander.getMaxWidth(), expander.getSumWidth(), expander.getMaxStates(), expander.getSumStates());
      line += String.format(",%d,%d,%f,%d", networkSize, treeWidth, Math.log(pJayes), timeJayes);
      
      for (int k = 0; k < amps.length; k++) {
        line += String.format(",%d,%f,%d", amps[k], Math.log(pAmps[k]), timeAmps[k]);
      }
      
      Logger.info(line);
      out.println(line);
    }
    out.close();
  }
  
  public static void sushi(String[] args) throws IOException, TimeoutException {
    File folder;
    if (args.length == 0) { folder = new File("C:\\Projects\\Rank\\Results\\2016.07\\sushi.3"); }
    else { folder = new File(args[0]); }
    
    ItemSet items = ItemSet.load(new File(folder, "sushi.items.txt"));
    
    Sample<PreferenceSet> sample = Sample.load(items, new File(folder, "sushi.sample.1.txt"));
    
    Ranking center = Ranking.fromStringByTag(items, "7-0-2-3-8-1-6-9-5-4");
    MallowsModel model = new MallowsModel(center, 0.739);
    
    int[] amps = { 5, 10, 50, 100, }; //1000, 5000, 10000, 50000, 100000 };
    
    PrintWriter out = FileUtils.write(new File(folder, "sushi.out.csv"));
    
    for (Sample.PW<PreferenceSet> pw: sample) {
      
      // EXPANDER
      long starts = System.currentTimeMillis();
      PreferenceExpander expander = new PreferenceExpander(model);
      double pExpander = expander.getProbability(pw.p);
      long timeExpander = System.currentTimeMillis() - starts;
      
      // JAYES
      long startJayes = System.currentTimeMillis();
      GraphicalModel gm = new GraphicalModel(model, pw.p);
      gm.build();
      int networkSize = gm.getNetworkSize();
      JayesInferator jayesInferator = new JayesInferator(gm);
      double pJayes = jayesInferator.getProbability();
      int treeWidth = jayesInferator.getJunctionTreeWidth();
      long timeJayes = System.currentTimeMillis() - startJayes;
      
      
      // AMP
      AMPSampler ampSampler = new AMPSampler(model);
      double[] b = new double[0];
      double[] pAmps = new double[amps.length];
      long[] timeAmps = new long[amps.length];
      long startAmp = System.currentTimeMillis();

      for (int k = 0; k < amps.length; k++) {
        int as = amps[k];
        int reps = as - b.length;
        double[] a = ampSampler.samplePosteriors(pw.p, reps);
        b = MathUtils.concat(b, a);
        pAmps[k] = MathUtils.mean(b);
        timeAmps[k] = System.currentTimeMillis() - startAmp;
      }
      
      
      
      String line = String.format("%d,%.3f,%s,%d", items.size(), model.getPhi(), pw.p, pw.p.size());
      line += String.format(",%f,%d,%d,%d,%d,%d", Math.log(pExpander), timeExpander, expander.getMaxWidth(), expander.getSumWidth(), expander.getMaxStates(), expander.getSumStates());
      line += String.format(",%d,%d,%f,%d", networkSize, treeWidth, Math.log(pJayes), timeJayes);
      
      for (int k = 0; k < amps.length; k++) {
        line += String.format(",%d,%f,%d", amps[k], Math.log(pAmps[k]), timeAmps[k]);
      }
      
      Logger.info(line);
      out.println(line);
    }
    out.close();
  }
  
  public static void sushi2(String[] args, Integer iParam) throws IOException, TimeoutException, InterruptedException {
    File folder;
    if (args.length == 0) { folder = new File("C:\\Projects\\Rank\\Results\\2016.07\\sushi.3"); }
    else { folder = new File(args[0]); }
    
    ItemSet items = ItemSet.load(new File(folder, "sushi.items.txt"));
    
    Sample<PreferenceSet> sample = Sample.load(items, new File(folder, "sushi.sample.1.txt"));
    
    Ranking center = Ranking.fromStringByTag(items, "7-0-2-3-8-1-6-9-5-4");
    MallowsModel model = new MallowsModel(center, 0.739);
    
    int[] amps = { 10, 50, 100, 1000, 5000, 10000, 50000 };
    double[] misses = { 0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8 };
    
    PrintWriter out = FileUtils.append(new File(folder, "sushi.out.csv"));
    
    for (double miss: misses) {
      MissingProbabilities missing = MissingProbabilities.uniformPairwise(items, miss);
      
      for (Sample.PW<PreferenceSet> pw: sample) {

        MutablePreferenceSet v = pw.p.transitiveClosure();
        Filter.removePreferences(v, missing);
        String vName = v.toString().replace(" ", "").replace(',', ' ');
        
        // EXPANDER
        long starts = System.currentTimeMillis();
        PreferenceExpander expander = new PreferenceExpander(model);
        double pExpander = expander.getProbability(v);
        long timeExpander = System.currentTimeMillis() - starts;

        // JAYES
        long startJayes = System.currentTimeMillis();
        GraphicalModel gm = new GraphicalModel(model, v);
        gm.build();
        long timeGM = System.currentTimeMillis() - startJayes;
        int networkSize = gm.getNetworkSize();
        JayesInferator jayesInferator = new JayesInferator(gm);
        double pJayes = jayesInferator.getProbability();
        int treeWidth = jayesInferator.getJunctionTreeWidth();
        long timeJayes = System.currentTimeMillis() - startJayes;


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

        // SampleSearch
        SampleSearchInferator ss1 = new SampleSearchInferator(gm);
        ss1.setI(iParam);
        Double pss1 = ss1.exec(1);


        String line = String.format("%d,%.3f,%s,%.1f,%s,%d", items.size(), model.getPhi(), pw.p, miss, vName, v.size());
        line += String.format(",%f,%d,%d,%d,%d,%d", Math.log(pExpander), timeExpander, expander.getMaxWidth(), expander.getSumWidth(), expander.getMaxStates(), expander.getSumStates());
        line += String.format(",%d,%d,%f,%d,%d,%d", networkSize, treeWidth, Math.log(pJayes), timeGM, timeJayes, timeGM+timeJayes);

        for (int k = 0; k < amps.length; k++) {
          line += String.format(",%d,%f,%d", amps[k], Math.log(pAmps[k]), timeAmps[k]);
        }
        
        line += String.format(",%d,%f,%d,%d", 1, pss1, ss1.getTime(), ss1.getTime() + timeGM);
        line += String.format(",%d", iParam);

        Logger.info(line);
        out.println(line);
        out.flush();
      }
    }
    out.close();
  }
  
  public static void crowdRankPrepare(String[] args) throws Exception {
    File folder, data;
    if (args.length == 0) {
      data = new File("C:\\Projects\\Rank\\Papers\\prefaggregation\\Mallows_Model\\datasets\\crowdrank\\hit_uid_ranking.csv");
      folder = new File("C:\\Projects\\Rank\\Results\\2016.07");
    }
    else {
      data = new File(args[0]);
      folder = data.getParentFile();
    }
    Logger.info("Data file: %s", data);
    
    CrowdRank crowdRank = new CrowdRank(data);
    RankingSample sample = crowdRank.getHitSample(43);

    PreferenceClusterer clusterer = new PreferenceClusterer(5);
    PreferenceClusters clusters = clusterer.cluster(sample);
    
    Collection<Sample<PreferenceSet>> samples = clusters.getClusters();
    int i = 0;
    for (Sample<PreferenceSet> s: samples) {
      File file = new File(folder, "crowdrank.sample."+i+".txt");
      s.save(file);
      i++;
    }
    
    MallowsMixtureReconstructor rec = new AMPxSMixtureReconstructor();
    MallowsMixtureModel model = rec.model(clusters);
    
    System.out.println(model);
    PrintWriter out = FileUtils.write(new File(folder, "crowdrank.model.txt"));
    out.println(model);
    out.close();
    
    ItemSet items = model.getItemSet();
    items.save(new File(folder, "crowdrank.items.txt"));
  }
}
