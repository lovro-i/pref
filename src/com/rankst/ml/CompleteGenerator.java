package com.rankst.ml;

import com.rankst.entity.ElementSet;
import com.rankst.entity.Ranking;
import com.rankst.util.Utils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Batch executer which uses CompleteTrainer to generate train data */
public class CompleteGenerator {
  private CompleteTrainer trainer;
  
  public CompleteGenerator(File arff) throws Exception {
    this.trainer = new CompleteTrainer(arff);
  }
  
  // int[] sampleSizes = { 100, 200, 500, 1000, 2000, 5000, 10000 }; //, 20000, 50000, 100000, 200000, 500000 };
  int[] sampleSizes = { 1000, 10000, 5000, 2000, 1000, 500, 200, 100 };
  private int reps = 20;
  double phis[] = TrainUtils.step(0.05, 0.8, 0.05);
  int[] ns = { 15, 20, 50, 25, 30, 40, 50, 70, 100, 200 };
  int bootstraps = CompleteAttributes.BOOTSTRAPS;
  
  
  int next = 0;
  List<Trainer> trainers = new ArrayList<Trainer>();
  
  private synchronized void next() {
    if (next < trainers.size()) {
      System.out.println("Starting trainer " + next + " / " + trainers.size());
      Utils.memStat();
      trainers.get(next++).start();
    }
  }
  
  private void generate() throws InterruptedException, IOException {
//    DataWriter writer = new DataWriter(3 * 60 * 1000);
//    writer.start();

    for (int n: ns) {
      ElementSet elements = new ElementSet(n);
      Ranking reference = elements.getReferenceRanking();
      
      for (int samples: sampleSizes) {
        for (double phi: phis) {          
          Utils.memStat();
          long start = System.currentTimeMillis();
          for (int i = 0; i < reps; i++) {
            trainer.generateTrainData(reference, samples, phi, bootstraps);        
          }      
          System.out.println(String.format("n: %d, samples: %d, phi: %2f in %d sec", n, samples, phi, (System.currentTimeMillis() - start) / 1000));
          trainer.write();
        }        
      }
    }
    
//    for (int n : ns) {
//      ElementSet elements = new ElementSet(n);
//      Ranking reference = elements.getReferenceRanking();
//      
//      for (int samples: sampleSizes) {
//        Trainer t = new Trainer(reference, samples);        
//        trainers.add(t);
//      }
//    }
//    System.out.println("Initialized " + trainers.size() + " trainers");
//
//    
//    int concurrent = 1;    
//    for (next = 0; next < concurrent; next++) trainers.get(next).start();
//    
//    for (Trainer t : trainers) t.join();
//    
//    writer.end();
    trainer.write();
    System.out.println("Done.");
  }
  
  
  public static void main(String[] args) throws Exception {
    File folder = new File("C:\\Projects\\Rankst\\Results.3");
    File file = new File(folder, "complete.train.arff");
    CompleteGenerator generator = new CompleteGenerator(file);
    generator.generate();
  }
  
  
  private class Trainer extends Thread {

    private final Ranking reference;
    private final int samples;
    
    private Trainer(Ranking reference, int samples) {
      this.reference = reference;
      this.samples = samples;
    }
    
    @Override
    public void run() {
      long start = System.currentTimeMillis();
      for (double phi: phis) {
        for (int i = 0; i < reps; i++) {
          trainer.generateTrainData(reference, samples, phi, bootstraps);        
        }      
      }
      System.out.println(String.format("Trainer [%d elements, %d samples] done in %d sec", reference.size(), samples, (System.currentTimeMillis() - start) / 1000));
      next();      
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
          trainer.write();
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
