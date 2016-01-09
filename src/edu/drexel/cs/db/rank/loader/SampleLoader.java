package edu.drexel.cs.db.rank.loader;

import edu.drexel.cs.db.rank.entity.Element;
import edu.drexel.cs.db.rank.entity.ElementSet;
import edu.drexel.cs.db.rank.entity.Ranking;
import edu.drexel.cs.db.rank.entity.Sample;
import edu.drexel.cs.db.rank.util.FileUtils;
import edu.drexel.cs.db.rank.util.Logger;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.StringTokenizer;

/** Loads sample from input */
public class SampleLoader {

  private final ElementSet elements;
  private final Sample sample;
  private final String delimiters;
  private final boolean weighted;
  
  
  public SampleLoader(File file, boolean weighted) throws IOException {
    this(new FileReader(file), weighted, ", \t;");
  }
  
  /** Loads sample from the reader
   * @param reader Source of data
   * @param weighted If true, the last value in the line is ranking weight
   * @param delimiters Commas, tabs, semi-colons...
   */
  public SampleLoader(Reader reader, boolean weighted, String delimiters) throws IOException {
    this.weighted = weighted;
    this.delimiters = delimiters;
    
    List<String> lines = FileUtils.readLines(reader);
    this.elements = getElements(lines);
    this.sample = new Sample(elements);
    loadSample(lines);
  }
  
  public Sample getSample() {
    return sample;
  }
  
  private ElementSet getElements(List<String> lines) {
    int maxId = -1;
    for (String line: lines) {
      StringTokenizer tokenizer = new StringTokenizer(line, delimiters);
      while (tokenizer.hasMoreTokens()) {
        int id = Integer.parseInt(tokenizer.nextToken());
        if (weighted && !tokenizer.hasMoreTokens()) break;
        maxId = Math.max(maxId, id);
      }
    }
    return new ElementSet(maxId + 1);
  }
  
  private void loadSample(List<String> lines) {
    for (String line: lines) {
      Ranking r = new Ranking(elements);
      StringTokenizer tokenizer = new StringTokenizer(line, delimiters);
      while (tokenizer.hasMoreTokens()) {
        int id = Integer.parseInt(tokenizer.nextToken());
        if (weighted && !tokenizer.hasMoreTokens()) break;
        Element e = elements.getElement(id);
        r.add(e);
      }
      sample.add(r);
    }
  }
  
  public static void main(String[] args) throws IOException {
    File folder = new File("C:\\Projects\\Rank\\Data\\sushi");
    File file = new File(folder, "sushi3a.csv");
    
    Sample sample = new SampleLoader(file, false).getSample();
    System.out.println(sample);
  }
}
