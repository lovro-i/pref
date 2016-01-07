package edu.drexel.cs.db.rank.measure;

/** For measuring goodness of a reconstruction method */
public interface ReconstructionError {

  public void add(double real, double reconstructed);
  public double getError();
  
}
