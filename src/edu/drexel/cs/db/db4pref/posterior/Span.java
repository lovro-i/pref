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
  
  public int size() {
    return to - from + 1;
  }
  
  @Override
  public String toString() {
    return "[" + from + ", " + to + "]";
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 17 * hash + this.from;
    hash = 501 * hash + this.to;
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final Span other = (Span) obj;
    return (this.from == other.from) && (this.to == other.to);
  }
  
  
}
