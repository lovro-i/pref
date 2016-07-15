package edu.drexel.cs.db.db4pref.gm;

import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.MapPreferenceSet;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.posterior.PreferenceExpander;
import edu.drexel.cs.db.db4pref.util.FileUtils;
import edu.drexel.cs.db.db4pref.util.Logger;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.TimeoutException;


public class SampleSearchInferator {

  private static String bin = "./ijgp-samplesearch";
  
  private final GraphicalModel gm;
  
  private int count;
  private long totalTime;
  private long time;
  
  public SampleSearchInferator(GraphicalModel gm) {
    this.gm = gm;
  }
  
  public int getCount() {
    return count;
  }
  
  public long getTotalTime() {
    return totalTime;
  }
  
  public long getTime() {
    return time;
  }
  
  public double exec(int sec) throws IOException, InterruptedException {
    long totalStart = System.currentTimeMillis();
    PrintWriter empty = FileUtils.write(new File("no.evidence"));
    empty.close();
    
    Double p = null;
    {
      PrintWriter uai = FileUtils.write(new File("bn.uai"));
      ExportUAI export = new ExportUAI(gm);
      uai.write(export.toString());
      uai.close();
    
      ProcessBuilder pb = new ProcessBuilder();
      pb.command(bin, "bn.uai", "no.evidence", String.valueOf(sec), "PR");
      Process process = pb.start();
      process.waitFor();
    
      List<String> lines = FileUtils.readLines(new File("bn.uai.PR"));
      if (process.exitValue() == 0 || lines.size() == 2) p = Double.valueOf(lines.get(1));
    }
    
    count = 1;
    while (p == null) {
      count++;
      if (count % 10 == 0) Logger.info("Try #%d", count);
      PrintWriter uai = FileUtils.write(new File("bn.uai"));
      ExportUAI export = new ExportUAI(gm);
      uai.write(export.toStringRandom());
      uai.close();
      
      ProcessBuilder pb = new ProcessBuilder();
      pb.command(bin, "bn.uai", "no.evidence", String.valueOf(sec), "PR");
      long startTime = System.currentTimeMillis();
      Process process = pb.start();
      process.waitFor();
      time = System.currentTimeMillis() - startTime;
      
      List<String> lines = FileUtils.readLines(new File("bn.uai.PR"));
      if (process.exitValue() == 0 || lines.size() == 2) p = Double.valueOf(lines.get(1));
    }

    this.totalTime = System.currentTimeMillis() - totalStart;
    Logger.info("SampleSearch successful after %d tries (%d sec / %d sec)", count, time / 1000, totalTime / 1000);
    return p;
  }
    
  
    
  public static void main(String[] args) throws UnsupportedEncodingException, IOException, InterruptedException, TimeoutException {
    ItemSet items = new ItemSet(10);
    items.tagOneBased();
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), 0.2);

    MapPreferenceSet v = new MapPreferenceSet(items);
    v.addByTag(2, 4);
    v.addByTag(3, 4);
    v.addByTag(4, 6);
    // v.addByTag(3, 1);

    PreferenceExpander exp = new PreferenceExpander(model);
    double p = exp.getProbability(v);
    Logger.info("Exact result: %f", Math.log10(p));

    
    GraphicalModel gm = new GraphicalModel(model, v);
    gm.setOneBased(true);
    gm.build();

    SampleSearchInferator inferator = new SampleSearchInferator(gm);
    inferator.exec(10);
  }
}
