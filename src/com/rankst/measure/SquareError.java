package com.rankst.measure;


public class SquareError implements ReconstructionError {

  private double sum;
  private int count;
  
  @Override
  public synchronized void add(double real, double reconstructed) {
    double d = real - reconstructed;
    sum += d * d;
    count++;
  }

  @Override
  public synchronized double getError() {
    return sum / count;
  }

}
