package edu.drexel.cs.db.rank.noisy;

import java.util.ArrayList;
import weka.core.Attribute;


public class NoisyAttributes {

  public static final int BOOTSTRAPS = 100;
  
  public static final Attribute ATTRIBUTE_ITEMS = new Attribute("items");
  public static final Attribute ATTRIBUTE_SAMPLE_SIZE = new Attribute("sample_size");
  public static final Attribute ATTRIBUTE_DIRECT_PHI = new Attribute("direct_phi");
  public static final Attribute ATTRIBUTE_BOOTSTRAP_PHI = new Attribute("bootstrap_phi");
  public static final Attribute ATTRIBUTE_BOOTSTRAP_VAR = new Attribute("bootstrap_var");
  public static final Attribute ATTRIBUTE_REAL_PHI = new Attribute("real_phi");
  
  public static final ArrayList<Attribute> ATTRIBUTES = new ArrayList<Attribute>();
  
  static {
    ATTRIBUTES.add(ATTRIBUTE_ITEMS);
    ATTRIBUTES.add(ATTRIBUTE_SAMPLE_SIZE);
    ATTRIBUTES.add(ATTRIBUTE_DIRECT_PHI);
    ATTRIBUTES.add(ATTRIBUTE_BOOTSTRAP_PHI);
    ATTRIBUTES.add(ATTRIBUTE_BOOTSTRAP_VAR);
    ATTRIBUTES.add(ATTRIBUTE_REAL_PHI);
  }
  
}
