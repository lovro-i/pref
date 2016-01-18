package edu.drexel.cs.db.rank.loader;

import edu.drexel.cs.db.rank.entity.Element;
import edu.drexel.cs.db.rank.entity.ElementSet;
import edu.drexel.cs.db.rank.entity.Ratings;
import edu.drexel.cs.db.rank.entity.RatingsSample;
import edu.drexel.cs.db.rank.util.FileUtils;
import edu.drexel.cs.db.rank.util.Logger;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/** Loads RatingsSample from the text file (user_id, item_id, rating [, weight]) */
public class RatingsLoader {

  private final ElementSet elements;
  private final RatingsSample sample;
  private final String delimiters;
  
  private Map<String, Element> tags = new HashMap<String, Element>();
  
  public RatingsLoader(File file) throws IOException {
    this(new FileReader(file), ", \t;");
  }
  
  /** Loads sample from the reader
   * @param reader Source of data
   * @param weighted If true, the last value in the line is ranking weight
   * @param delimiters Commas, tabs, semi-colons...
   */
  public RatingsLoader(Reader reader, String delimiters) throws IOException {
    this.delimiters = delimiters;
    
    List<String> lines = FileUtils.readLines(reader);
    
    // Load elements
    this.elements = getElements(lines);
    for (Element e: elements) {
      tags.put((String) e.getTag(), e);
    }
    
    // Load ratings
    this.sample = new RatingsSample(elements);
    loadSample(lines);
  }
  
  
  private void loadSample(List<String> lines) {
    Map<Integer, Ratings> users = new HashMap<Integer, Ratings>();
    for (String line: lines) {
      try {
        StringTokenizer tokenizer = new StringTokenizer(line, delimiters);
        Integer uid = Integer.valueOf(tokenizer.nextToken());
        String itemId = tokenizer.nextToken();
        Float val = Float.valueOf(tokenizer.nextToken());
        Ratings ratings = users.get(uid);
        if (ratings == null) {
          ratings = new Ratings(elements);
          users.put(uid, ratings);
        }
        ratings.put(tags.get(itemId), val);
      }
      catch (NumberFormatException skip) {}
    }
    
    for (Ratings ratings: users.values()) {
      sample.add(ratings);
    }
  }

  
  public ElementSet getElements(List<String> lines) {
    Set<String> ids = new HashSet<String>();
    for (String line: lines) {
      try {
        StringTokenizer tokenizer = new StringTokenizer(line, delimiters);
        int uid = Integer.parseInt(tokenizer.nextToken()); // skip uid
        String itemId = tokenizer.nextToken();
        ids.add(itemId);
      }
      catch (NumberFormatException skip) {}
    }
    return new ElementSet(ids.toArray());
  }

  public RatingsSample getRatingsSample() {
    return sample;
  }  
}
