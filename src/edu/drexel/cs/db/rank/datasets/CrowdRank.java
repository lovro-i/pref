package edu.drexel.cs.db.rank.datasets;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.filter.Split;
import edu.drexel.cs.db.rank.incomplete.IncompleteReconstructor;
import edu.drexel.cs.db.rank.mixture.MallowsMixtureModel;
import edu.drexel.cs.db.rank.mixture.MallowsMixtureReconstructor;
import edu.drexel.cs.db.rank.reconstruct.MallowsReconstructor;
import edu.drexel.cs.db.rank.util.FileUtils;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;


public class CrowdRank {

  private File data;
  private Map<Integer, Sample> hits;
  private Sample fullSample;
  
  public CrowdRank(File data) {
    this.data = data;
  }
  
  private void loadHitSamples() throws IOException {
    hits = new HashMap<Integer, Sample>();
    List<String> lines = FileUtils.readLines(data);
    Set<Integer> hitIds = getHits(lines);
    for (Integer hit: hitIds) {
      ItemSet items = getItems(lines, hit);
      Sample sample = new Sample(items);
      hits.put(hit, sample);
      loadSample(sample, lines, hit);
    }
  }
  
  public Sample getSample(int hit) throws IOException {
    if (hits == null) loadHitSamples();
    return hits.get(hit);
  }
  
  
  private void loadFullSample() throws IOException {
    List<String> lines = FileUtils.readLines(data);
    ItemSet items = getItems(lines);
    fullSample = new Sample(items);
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
  
  public Sample getFullSample() throws IOException {
    if (fullSample == null) loadFullSample();
    return fullSample;
  }
  
  private void loadSample(Sample sample, List<String> lines, Integer hit) {
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
  
  /** Model the whole hit with a mixture of mallows */
  public MallowsMixtureModel reconstructFull(int hit) throws Exception {
    Sample sample = getSample(hit);
    File folder = new File("C:\\Projects\\Rank\\Results.3");
    File arff = new File(folder, "hit." + hit + ".train.arff");
    MallowsReconstructor single = new IncompleteReconstructor(arff, 3);
    MallowsMixtureReconstructor reconstructor = new MallowsMixtureReconstructor(single, 10);        
    return reconstructor.reconstruct(sample);
  }
  
  
  public MallowsMixtureModel reconstructFullSample() throws Exception {
    MallowsReconstructor single = new IncompleteReconstructor(3);
    MallowsMixtureReconstructor reconstructor = new MallowsMixtureReconstructor(single, 10);        
    return reconstructor.reconstruct(fullSample);
  }
  
  public static void main(String[] args) throws Exception {
    File data = new File("C:\\Projects\\Rank\\Papers\\prefaggregation\\Mallows_Model\\datasets\\crowdrank\\hit_uid_ranking.csv");
    CrowdRank crowdRank = new CrowdRank(data);
    
    Sample sample = crowdRank.getFullSample();
    System.out.println(sample);
    System.out.println(crowdRank.reconstructFullSample());
  }
  
}
