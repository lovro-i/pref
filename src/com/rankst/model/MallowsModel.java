package com.rankst.model;

import com.rankst.entity.ElementSet;
import com.rankst.entity.Ranking;


public class MallowsModel {

  private final Ranking center;
  private final double phi;
  
  public MallowsModel(Ranking center, double phi) {
    this.center = center;
    this.phi = phi;
  }

  public ElementSet getElements() {
    return this.center.getElementSet();
  }

  public Ranking getCenter() {
    return center;
  }

  public double getPhi() {
    return phi;
  }
  
  /** Expected distance from the center, depends only on phi */
  public double getE() {
    return phiToE(phi);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof MallowsModel)) return false;
    MallowsModel mm = (MallowsModel) obj;
    return this.center.equals(mm.center) && this.phi == mm.phi;
  }

  @Override
  public String toString() {
    return "Center: " + center + "; phi: " + phi;
  }
  
  /** Convert phi to expected distance */
  public static double phiToE(double phi) {
    return phi / (1 - phi);
  }
  
  /** Convert expected distance to phi */
  public static double eToPhi(double e) {
    return e / (e+1);
  }
  
}
