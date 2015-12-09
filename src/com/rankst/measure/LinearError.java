package com.rankst.measure;


public class LinearError implements ReconstructionError {

  private double sum;
  private int count;
  
  @Override
  public synchronized void add(double real, double reconstructed) {
    sum += Math.abs(real - reconstructed);
    count++;
  }

  @Override
  public synchronized double getError() {
    return sum / count;
  }

}
