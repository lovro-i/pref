package edu.drexel.cs.db.rank.preference;

import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.preference.PreferenceSample.PW;
import java.util.ArrayList;
import java.util.Objects;


public class PreferenceSample extends ArrayList<PW> {

    private final ItemSet itemSet;

    public PreferenceSample(ItemSet items) {
      this.itemSet = items;
    }

    public ItemSet getItems() {
      return itemSet;
    }

    public double sumWeights() {
      double s = 0;
      for (PW pw: this) s += pw.w;
      return s;
    }

    public void add(PreferenceSet pref, double weight) {
      this.add(new PW(pref, weight));
    }  

    public PreferenceSet getPreferenceSet(int index) {
      return this.get(index).p;
    }

    public double getWeight(int index) {
      return this.get(index).w;
    }
    
    public double getWeight(PreferenceSet pref) {
      double w = 0;
      for (PW pw: this) w += pw.w;
      return w;
    }

    public void add(PreferenceSet pref) {
      this.add(pref, 1);
    }
    
    public static class PW {
    
      public final PreferenceSet p;
      public final double w;

      public PW(PreferenceSet p, double w) {
        this.p = p;
        this.w = w;
      }

      public String toString() {
        if (w == 1) return p.toString();
        else return p.toString() + " (" + w + ")";
      }

      @Override
      public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.p);
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.w) ^ (Double.doubleToLongBits(this.w) >>> 32));
        return hash;
      }

      @Override
      public boolean equals(Object obj) {
        if (this == obj) {
          return true;
        }
        if (obj == null) {
          return false;
        }
        if (getClass() != obj.getClass()) {
          return false;
        }
        final PW other = (PW) obj;
        return true;
      }
      
    }
}
