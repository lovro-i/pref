package edu.drexel.cs.db.db4pref.model;

import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.distance.KendallTauDistance;
import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.MapPreferenceSet;
import edu.drexel.cs.db.db4pref.core.MutablePreferenceSet;
import edu.drexel.cs.db.db4pref.core.Preference;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.core.RankingSample;
import edu.drexel.cs.db.db4pref.core.Sample.PW;
import edu.drexel.cs.db.db4pref.util.Logger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;


public class MallowsModel {

  private final Ranking center;
  private final double phi;
  
  public MallowsModel(Ranking center, double phi) {
    this.center = center;
    this.phi = phi;
  }

  public ItemSet getItemSet() {
    return this.center.getItemSet();
  }

  public Ranking getCenter() {
    return center;
  }

  public double getPhi() {
    return phi;
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
  
  /** Normalization factor */
  public double z() {
    double z = 1;
    double s = 1;
    double phip = 1;
    for (int i = 1; i < this.getItemSet().size(); i++) {
      phip *= phi;
      s += phip;
      z *= s;
    }
    return z;
  }
  
  
  /** @return Probability of the ranking being generated by this model */
  public double getProbability(Ranking r) {
    double d = KendallTauDistance.between(center, r);
    return Math.pow(phi, d) / z();
  }
  
  
  public double getLogProbability(Ranking r) {
    double d = KendallTauDistance.between(center, r);
    return d * Math.log(phi) - Math.log(z());
  }
    
    
  public double getLogLikelihood(RankingSample sample) {
    double ll = 0;
    double lnZ = Math.log(z());
    double lnPhi = Math.log(phi);
    for (PW<Ranking> pw: sample) {
      ll += pw.w * (KendallTauDistance.between(center, pw.p) * lnPhi - lnZ);
    }
    return ll / sample.sumWeights();
  }

  /** Upper probability bound on normalization constant, Lu & Boutilier, Theorem 17 */
  public double getUpperBound(PreferenceSet pref) {
    MutablePreferenceSet tcPref = pref.transitiveClosure();
    int d = 0;
    int s = 0;
    for (Preference p: tcPref.getPreferences()) {
      if (center.contains(p)) s++;
      else d++;
    }
    
    double ub = Math.pow(phi, d) * Math.pow((1 + phi), this.center.size() - s - d);
    return ub;
  }
  
  public static Set<Item> getAntiChain(PreferenceSet pref) {
    MutablePreferenceSet tc = pref.transitiveClosure();
    Set<Item> x = new HashSet<Item>();
    Set<Item> y = new HashSet<Item>();
    int c = 0;
    while (!tc.isEmpty()) { // || x.size() > tc.getItems().size()) {
      y.clear();
      for (Item item: tc.getItems()) {
        Collection<Item> higher = tc.getHigher(item);
        if (higher.isEmpty()) y.add(item);
      }
//      Logger.info("Y %d: %s", y.size(), y);
//      Logger.info("X %d: %s", x.size(), x);
//      Logger.info("TC %d %s: %s", tc.size(), tc.isEmpty(), tc);
//      Logger.waitKey();
      
      if (y.size() > x.size()) {
        x.clear();
        x.addAll(y);
      }
      for (Item item: y) {
        tc.remove(item);
      }
    }
    return x;
  }
  
  /** Lower probability bound on normalization constant, Lu & Boutilier, Theorem 19 */
  public double getLowerBound(PreferenceSet pref) {
    Set<Item> X = getAntiChain(pref);
    // Logger.info("Anitchain: %s", X);
    MutablePreferenceSet tc = pref.transitiveClosure();
    Set<Item> Y = new HashSet<Item>();
    Set<Item> Z = new HashSet<Item>();
    for (Item item: X) {
      Y.addAll(tc.getHigher(item));
      Z.addAll(tc.getLower(item));
    }
    
    Set<Item> union = new HashSet<Item>(Y);
    union.addAll(Z);
    // Logger.info("%d %d %d %d", X.size(), Y.size(), Z.size(), union.size());
    if (union.size() != Y.size() + Z.size()) Logger.warn("PROBLEM!");

    MapPreferenceSet tcy = new MapPreferenceSet(pref.getItemSet());
    MapPreferenceSet tcz = new MapPreferenceSet(pref.getItemSet());
    for (Preference p: tc.getPreferences()) {
      if (Y.contains(p.higher) && Y.contains(p.lower)) tcy.add(p);
      if (Z.contains(p.higher) && Z.contains(p.lower)) tcz.add(p);
    }
    
    // Logger.info("TCY: %s", tcy);
    // Logger.info("TCZ: %s", tcz);
    
    Set<Ranking> omegaY = tcy.getRankings();
    Set<Ranking> omegaZ = tcz.getRankings();
    // Logger.info("Omega Y: %s", omegaY);
    // Logger.info("Omega Z: %s", omegaZ);
    
    int delta = 0;
    for (Item itemX: X) {
      for (Item itemY: Y) {
        if (center.isPreferred(itemX, itemY)) delta++;
      }
    }
    for (Item itemX: X) {
      for (Item itemZ: Z) {
        if (center.isPreferred(itemZ, itemX)) delta++;
      }
    }
    for (Item itemY: Y) {
      for (Item itemZ: Z) {
        if (center.isPreferred(itemZ, itemY)) delta++;
      }
    }
    // Logger.info("Delta: %d", delta);
    
    // Consistent rankings over Y
    double cy = 0;
    Ranking sigmaY = center.toRanking(Y);
    for (Ranking r: omegaY) {
      double d = KendallTauDistance.between(r, sigmaY);
      cy += Math.pow(phi, d);
    }
    
    // Consistent rankings over Z
    double cz = 0;
    Ranking sigmaZ = center.toRanking(Z);
    for (Ranking r: omegaZ) {
      double d = KendallTauDistance.between(r, sigmaZ);
      cz += Math.pow(phi, d);
    }
    
    double pu = 1;
    for (int i = 1; i <= X.size(); i++) {
      double s = 0;
      for (int j = 0; j <= i-1; j++) {
        s += Math.pow(phi, j);
      }
      pu *= s;
    }
    
    // Logger.info("CY %f, CZ %f, PU %f", cy, cz, pu);
    double lb = Math.pow(phi, delta) * pu;
    if (cz > 0) lb *= cz;
    if (cy > 0) lb *= cy;
    return lb;
  } 
  
  
  public static void main(String[] args) {
    Random random = new Random();
    ItemSet items = new ItemSet(20);
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), 0.2);
    Ranking r = model.getItemSet().getRandomRanking();
    List<Preference> pset = new ArrayList<>(r.transitiveClosure().getPreferences());
    int size = (int) (0.8 * pset.size());
    while (pset.size() > size) {
      pset.remove(random.nextInt(pset.size()));
    }
    
    MapPreferenceSet pref = new MapPreferenceSet(items);
    for (Preference p: pset) pref.add(p);
    
    double p = 0;
    for (Ranking r1: pref.getRankings()) {
      p += model.getProbability(r1);
    }
    
    double ub = model.getUpperBound(pref);
    Logger.info("P = %f, UB = %f, UB/Z = %f", Math.log(p), Math.log(ub), Math.log(ub/model.z()));
    Logger.info("LB/Z = %f", Math.log(model.getLowerBound(pref)/model.z()));
  }
  
}
