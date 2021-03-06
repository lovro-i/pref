package edu.drexel.cs.db.db4pref.data.datasets;

import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.core.RankingSample;
import edu.drexel.cs.db.db4pref.mixture.AMPxSMixtureReconstructor;
import edu.drexel.cs.db.db4pref.mixture.PreferenceClusters;
import edu.drexel.cs.db.db4pref.mixture.PreferenceClusterer;
import edu.drexel.cs.db.db4pref.mixture.MallowsMixtureModel;
import edu.drexel.cs.db.db4pref.util.FileUtils;
import edu.drexel.cs.db.db4pref.util.Logger;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/** Class used for loading and accessing CrowdRank dataset */
public class CrowdRank {

  private File data;
  private Map<Integer, RankingSample> hits;
  private RankingSample fullSample;
  
  /** Load CrowdRank dataset from the file */
  public CrowdRank(File data) throws IOException {
    this.data = data;
    loadHitSamples();
    loadFullSample();
  }
  
  private void loadHitSamples() throws IOException {
    hits = new HashMap<Integer, RankingSample>();
    List<String> lines = FileUtils.readLines(data);
    Set<Integer> hitIds = getHits(lines);
    for (Integer hit: hitIds) {
      ItemSet items = getItems(lines, hit);
      RankingSample sample = new RankingSample(items);
      hits.put(hit, sample);
      loadSample(sample, lines, hit);
    }
  }
  
  public RankingSample getHitSample(int hit) throws IOException {
    if (hits == null) loadHitSamples();
    return hits.get(hit);
  }
  
  public int getHitCount() {
    return hits.size();
  }
  
  
  private void loadFullSample() throws IOException {
    List<String> lines = FileUtils.readLines(data);
    ItemSet items = getItems(lines);
    fullSample = new RankingSample(items);
    for (String line: lines) {
      try {
        StringTokenizer t = new StringTokenizer(line, ", -;\"");
        Integer hid = Integer.valueOf(t.nextToken()); // skip hit
        int uid = Integer.parseInt(t.nextToken()); // skip uid
        Ranking r = new Ranking(items);
        while (t.hasMoreTokens()) {
          String itemId = t.nextToken();
          Item e = items.getItemByTag(itemId);
          r.add(e);
        }
        fullSample.add(r);
      }
      catch (NumberFormatException skip) {}
    }
  }
  
  public RankingSample getFullSample() throws IOException {
    if (fullSample == null) loadFullSample();
    return fullSample;
  }
  
  private void loadSample(RankingSample sample, List<String> lines, Integer hit) {
    ItemSet items = sample.getItemSet();
    for (String line: lines) {
      try {
        StringTokenizer t = new StringTokenizer(line, ", -;\"");
        Integer hid = Integer.valueOf(t.nextToken());
        if (!hid.equals(hit)) continue;
        
        int uid = Integer.parseInt(t.nextToken()); // skip uid
        Ranking r = new Ranking(items);
        while (t.hasMoreTokens()) {
          String itemId = t.nextToken();
          Item e = items.getItemByTag(itemId);
          r.add(e);
        }
        sample.add(r);
      }
      catch (NumberFormatException skip) {}
    }
  }
  
  
  public Set<Integer> getHits(List<String> lines) {
    Set<Integer> hits = new HashSet<Integer>();
    for (String line: lines ) {
      try {
        StringTokenizer t = new StringTokenizer(line, ", ");
        Integer hit = Integer.valueOf(t.nextToken());
        hits.add(hit);
      }
      catch (NumberFormatException skip) {}
    }
    return hits;
  }
  
  
  private ItemSet getItems(List<String> lines) {
    Set<String> ids = new HashSet<String>();
    for (String line: lines) {
      try {
        StringTokenizer t = new StringTokenizer(line, ", -;\"");
        Integer hid = Integer.valueOf(t.nextToken()); // skip hit        
        int uid = Integer.parseInt(t.nextToken()); // skip uid
        
        while (t.hasMoreTokens()) {
          String itemId = t.nextToken();
          ids.add(itemId);
        }
      }
      catch (NumberFormatException skip) {}
    }
    return new ItemSet(ids.toArray());
  }
  
  
  private ItemSet getItems(List<String> lines, Integer hit) {
    Set<String> ids = new HashSet<String>();
    for (String line: lines) {
      try {
        StringTokenizer t = new StringTokenizer(line, ", -;\"");
        Integer hid = Integer.valueOf(t.nextToken());
        if (!hid.equals(hit)) continue;
        
        int uid = Integer.parseInt(t.nextToken()); // skip uid
        
        while (t.hasMoreTokens()) {
          String itemId = t.nextToken();
          ids.add(itemId);
        }
      }
      catch (NumberFormatException skip) {}
    }
    return new ItemSet(ids.toArray());
  }
  
//  /** Model the whole hit with a mixture of mallows */
//  public MallowsMixtureModel reconstructFull(int hit) throws Exception {
//    RankingSample sample = getHitSample(hit);
//    MallowsReconstructor single = new EMIncompleteReconstructor(10);
//    MallowsMixtureReconstructor reconstructor = new MallowsMixtureReconstructor(single, 10);        
//    return reconstructor.reconstruct(sample);
//  }
//  
//  
//  public MallowsMixtureModel reconstructFullSample() throws Exception {
//    MallowsReconstructor single = new EMIncompleteReconstructor(10);
//    MallowsMixtureReconstructor reconstructor = new MallowsMixtureReconstructor(single, 10);        
//    return reconstructor.reconstruct(fullSample);
//  }
//  
//  public static void main(String[] args) throws Exception {
//    File data = new File("C:\\Projects\\Rank\\Papers\\prefaggregation\\Mallows_Model\\datasets\\crowdrank\\hit_uid_ranking.csv");
//    CrowdRank crowdRank = new CrowdRank(data);
//    
//    RankingSample sample = crowdRank.getFullSample();
//    System.out.println(sample);
//    System.out.println(crowdRank.reconstructFullSample());
//  }
  
  public void reconstructHits() throws Exception {
    for (int hitId: hits.keySet()) {
      Logger.info("Reconstructing hit %d...", hitId);
      RankingSample sample = hits.get(hitId);
      
      PreferenceClusterer clusterer = new PreferenceClusterer(5);
      PreferenceClusters clusters = clusterer.cluster(sample);
      
      AMPxSMixtureReconstructor reconstructor = new AMPxSMixtureReconstructor(100, 0.1);
      MallowsMixtureModel model = reconstructor.model(clusters);
      Logger.info("\n==================== Model of hit %d ====================\n%s\n", hitId, model);
    }
  }
  
  public static void main(String[] args) throws Exception {
    File data = new File("C:\\Projects\\Rank\\Papers\\prefaggregation\\Mallows_Model\\datasets\\crowdrank\\hit_uid_ranking.csv");
    CrowdRank crowdRank = new CrowdRank(data);
    Logger.info("Hits loaded: %d", crowdRank.getHitCount());
    for (int i = 1; i <= crowdRank.getHitCount(); i++) {
      RankingSample sample = crowdRank.getHitSample(i);
      Logger.info("HIT %d: %d users", i, sample.size());
    }
    
    // crowdRank.reconstructHits();
  }
  
}
