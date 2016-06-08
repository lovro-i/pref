package edu.drexel.cs.db.db4pref.data;

import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.core.RankingSample;
import edu.drexel.cs.db.db4pref.core.TopRanking;
import edu.drexel.cs.db.db4pref.util.FileUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

/** Loads sample from input */
public class SampleLoader {

   
  private final boolean weighted;
  private final boolean ids;
  private final String delimiters;
  private final boolean pack;
  private final boolean top;
  
  
  /** 
   * @param ids Does the file contain numeric ids, or tags
   * @param weighted If true, the last value in the line is ranking weight
   * @param top Are rankings top-k or not
   */ 
  public SampleLoader(boolean ids, boolean weighted, boolean top) {
    this(ids, weighted, top, false);
  }
  
  /** 
   * @param ids Does the file contain numeric ids, or tags
   * @param weighted If true, the last value in the line is ranking weight
   * @param top Are rankings top-k or not
   * @param pack Should same ratings be represented by weights, or by multiple instances
   * @param delimiters Commas, tabs, semi-colons...
   */
  public SampleLoader(boolean ids, boolean weighted, boolean top, boolean pack, String delimiters) {
    this.ids = ids;
    this.weighted = weighted;
    this.top = top;
    this.pack = pack;
    this.delimiters = delimiters;
    
  }
  
  /** 
   * @param ids Does the file contain numeric ids, or tags
   * @param weighted If true, the last value in the line is ranking weight
   * @param top Are rankings top-k or not
   * @param pack Should same ratings be represented by weights, or by multiple instances
   */
  public SampleLoader(boolean ids, boolean weighted, boolean top, boolean pack) {
    this(ids, weighted, top, pack, ", \t;");
  }
  
  public RankingSample loadSample(File file) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(file));
    return loadSample(reader);
  }
  
  public RankingSample loadSample(Reader reader) throws IOException {
    List<String> lines = FileUtils.readLines(reader);
    ItemSet itemSet = getItemSet(lines);
    return getSample(lines, itemSet);
  }
  
  private ItemSet getItemSet(List<String> lines) {
    if (ids) return getItemSetById(lines);
    else return getItemSetByTag(lines);
  }
  
  private ItemSet getItemSetById(List<String> lines) {
    int maxId = -1;
    for (String line: lines) {
      line = line.split(", ")[0];
      StringTokenizer tokenizer = new StringTokenizer(line, delimiters);
      while (tokenizer.hasMoreTokens()) {
        int id = Integer.parseInt(tokenizer.nextToken());
        if (weighted && !tokenizer.hasMoreTokens()) break;
        maxId = Math.max(maxId, id);
      }
    }
    return new ItemSet(maxId + 1);
  }
  
  private ItemSet getItemSetByTag(List<String> lines) {
    Set<String> ids = new HashSet<String>();
    for (String line: lines) {
      StringTokenizer tokenizer = new StringTokenizer(line, delimiters);
      while (tokenizer.hasMoreTokens()) {
        String tag = tokenizer.nextToken();        
        if (weighted && !tokenizer.hasMoreTokens()) break;
        ids.add(tag);
      }
    }
    return new ItemSet(ids);
  }
  
  private RankingSample getSample(List<String> lines, ItemSet itemSet) {
    if (ids) return getSampleById(lines, itemSet);
    else return getSampleByTag(lines, itemSet);
  }
  
  private Ranking newRanking(ItemSet itemSet) {
    if (top) return new TopRanking(itemSet);
    else return new Ranking(itemSet);
  }
  
  private RankingSample getSampleById(List<String> lines, ItemSet itemSet) {
    RankingSample sample = new RankingSample(itemSet);
    for (String line: lines) {
      Ranking r = newRanking(itemSet);
      StringTokenizer tokenizer = new StringTokenizer(line, delimiters);
      double w = 1;
      while (tokenizer.hasMoreTokens()) {
        String t = tokenizer.nextToken();        
        if (weighted && !tokenizer.hasMoreTokens()) {
          w = Double.parseDouble(t);
        }
        else {
          int id = Integer.parseInt(t);
          Item e = itemSet.getItemById(id);
          r.add(e);
        }
      }
      add(sample, r, w);
    }
    return sample;
  }

  

  
  private RankingSample getSampleByTag(List<String> lines, ItemSet itemSet) {
    RankingSample sample = new RankingSample(itemSet);
    for (String line: lines) {
      Ranking r = newRanking(itemSet);
      StringTokenizer tokenizer = new StringTokenizer(line, delimiters);
      double w = 1;
      while (tokenizer.hasMoreTokens()) {
        String t = tokenizer.nextToken();        
        if (weighted && !tokenizer.hasMoreTokens()) {
          w = Double.parseDouble(t);
        }
        else {          
          Item e = itemSet.getItemByTag(t);
          r.add(e);
        }
      }
      add(sample, r, w);
    }
    return sample;
  }
  
  private void add(RankingSample sample, Ranking r, double w) {
    if (!weighted && !pack) {
      sample.add(r);
      return;
    }
    
    if (!weighted && pack) {
      sample.addWeight(r, 1d);
      return;
    }
    
    if (weighted && pack) {
      sample.addWeight(r, w);
      return;
    }
    
    if (weighted && !pack) {
      while (w >= 1) {
        sample.add(r, 1);
        w = w - 1;
      }
      if (w > 0.0001) {
        sample.add(r, w);
      }
    }
  }
  
  public static void main(String[] args) throws IOException {
    File folder = new File("C:\\Projects\\Rank\\Data\\sushi");
    File file = new File(folder, "sushi3a.csv");
    
    SampleLoader loader = new SampleLoader(true, false, false);
    RankingSample sample = loader.loadSample(file);
    System.out.println(sample);
  }
}
