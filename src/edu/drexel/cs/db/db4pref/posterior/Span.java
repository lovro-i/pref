package edu.drexel.cs.db.db4pref.posterior;


public class Span {

  public final int from;
  public int to;
  
  public Span(int from, int to) {
    this.from = from;
    this.to = to;
  }
  
  public void setTo(int to) {
    this.to = to;
  }
  
  
  @Override
  public String toString() {
    return "[" + from + ", " + to + "]";
  }
}
