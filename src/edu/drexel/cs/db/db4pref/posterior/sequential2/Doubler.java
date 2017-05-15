package edu.drexel.cs.db.db4pref.posterior.sequential2;


public class Doubler {

  private double p;
  
  public Doubler(double p) {
    this.p = p;
  }
  
  public synchronized void add(double p) {
    this.p += p;
  }
  
  public void set(double p) {
    this.p = p;
  }
  
  public double get() {
    return p;
  }
  
}