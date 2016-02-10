package edu.drexel.cs.db.rank.noisy;

import java.util.ArrayList;
import weka.core.Attribute;


public class NoisyAttributes {

  public static final int BOOTSTRAPS = 100;
  
  public static final Attribute ATTRIBUTE_ITEMS = new Attribute("items");
  public static final Attribute ATTRIBUTE_SAMPLE_SIZE = new Attribute("sample_size");
  public static final Attribute ATTRIBUTE_DIRECT_PHI = new Attribute("direct_phi");
  public static final Attribute ATTRIBUTE_BOOTSTRAP_MEAN = new Attribute("bootstrap_mean");
  public static final Attribute ATTRIBUTE_BOOTSTRAP_MIN = new Attribute("bootstrap_min");
  public static final Attribute ATTRIBUTE_BOOTSTRAP_VAR = new Attribute("bootstrap_var");
  public static final Attribute ATTRIBUTE_REAL_PHI = new Attribute("real_phi");
  
  

  static ArrayList<Attribute> getAttributes(boolean triangle, int boots) {
    ArrayList<Attribute> attributes = new ArrayList<Attribute>();
    attributes.add(ATTRIBUTE_ITEMS);
    attributes.add(ATTRIBUTE_SAMPLE_SIZE);
    if (triangle) attributes.add(ATTRIBUTE_DIRECT_PHI);
    if (boots > 0) {
      attributes.add(ATTRIBUTE_BOOTSTRAP_MEAN);
      attributes.add(ATTRIBUTE_BOOTSTRAP_MIN);
      attributes.add(ATTRIBUTE_BOOTSTRAP_VAR);
    }
    attributes.add(ATTRIBUTE_REAL_PHI);
    return attributes;
  }  
  
}
