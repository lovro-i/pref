package com.rankst.measure;

/** For measuring goodness of a reconstruction method */
public interface ReconstructionError {

  public void add(double real, double reconstructed);
  public double getError();
  
}
