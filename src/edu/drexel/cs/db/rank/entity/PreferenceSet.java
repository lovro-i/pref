package edu.drexel.cs.db.rank.entity;


public interface PreferenceSet {

  public void add(Element higher, Element lower);  
  
  public Sample toSample();
  
}
