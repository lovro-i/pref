package edu.drexel.cs.db.rank.incomplete;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

public class IncompleteTrainer {
  
  private File file;
  private Instances data;
  
  
  public IncompleteTrainer(File file) throws Exception {
    this.file = file;
    if (file.exists()) {
      System.out.println("Loading existing dataset");
      InputStream is = new FileInputStream(file);
      ConverterUtils.DataSource source = new ConverterUtils.DataSource(is);
      data = source.getDataSet();
      System.out.println("Loaded instances: " + data.size());
    }
  }
  

  

  
  
  
  private class DataWriter extends Thread {
    
    private boolean running = false;
    private long sleep;
    
    private DataWriter(long sleep) {
      this.sleep = sleep;
    }
    
    public void run() {
      running = true;
      try {
        while (running) {
          Thread.sleep(sleep);
          System.out.println("Writing data...");
          //write();
        }
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }

    private void end() {
      this.running = false;
    }
  }
  
}
