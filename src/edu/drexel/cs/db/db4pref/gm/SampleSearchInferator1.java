package edu.drexel.cs.db.db4pref.gm;

import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.MapPreferenceSet;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.posterior.PreferenceExpander;
import edu.drexel.cs.db.db4pref.util.ExecUtils;
import edu.drexel.cs.db.db4pref.util.FileUtils;
import edu.drexel.cs.db.db4pref.util.Logger;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.TimeoutException;


public class SampleSearchInferator1 {

  private static String bin = "./ijgp-samplesearch";
  
  private final GraphicalModel gm;
  
  private Integer i = null;
  
  private long timeout = 0;
  private long time;
  
  public SampleSearchInferator1(GraphicalModel gm) {
    this.gm = gm;
  }
  
  public long getTime() {
    return time;
  }
  
  public void setI(Integer i) {
    this.i = i;
  }
  
  /** Process timeout in milliseconds. 0 for no timeout */
  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }
  
  /** @returns log probability (natural logarithm) */
  public Double exec(int sec) throws IOException, InterruptedException {
    File evidence = new File("no.evidence");
    if (!evidence.exists()) {
      PrintWriter empty = FileUtils.write(evidence);
      empty.write("0");
      empty.close();
    }
    
    long start = System.currentTimeMillis();
    Double p = null;
    PrintWriter uai = FileUtils.write(new File("bn.uai"));
    ExportUAI1 export = new ExportUAI1(gm);
    uai.write(export.toString());
    uai.close();

    ProcessBuilder pb = new ProcessBuilder();
    if (i == null) pb.command(bin, "bn.uai", "no.evidence", String.valueOf(sec), "PR");
    else pb.command(bin, "-i", String.valueOf(i), "bn.uai", "no.evidence", String.valueOf(sec), "PR");
    
    int exitValue;
    if (timeout > 0) exitValue = ExecUtils.execute(pb, timeout, null, null);
    else exitValue = ExecUtils.execute(pb, null, null);
    // Process process = pb.start();
    // process.waitFor();

    List<String> lines = FileUtils.readLines(new File("bn.uai.PR"));
    try {
      if (exitValue == 0 && lines.size() == 2) p = Double.valueOf(lines.get(1));
    }
    catch (NumberFormatException e) {}
    this.time = System.currentTimeMillis() - start;
    
    if (p != null) p = p / Math.log10(Math.E);
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

    SampleSearchInferator1 inferator = new SampleSearchInferator1(gm);
    inferator.exec(10);
  }
}
