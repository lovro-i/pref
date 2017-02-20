package edu.drexel.cs.db.db4pref.posterior.sequential2;


public class Doubler {

  private double p;
  
  Doubler(double p) {
    this.p = p;
  }
  
  void add(double p) {
    this.p += p;
  }
  
  void set(double p) {
    this.p = p;
  }
  
  public double get() {
    return p;
  }
  
}