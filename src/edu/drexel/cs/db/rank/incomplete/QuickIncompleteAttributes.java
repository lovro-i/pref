package edu.drexel.cs.db.rank.incomplete;

import java.util.ArrayList;
import weka.core.Attribute;


public class QuickIncompleteAttributes {

  public static final int BOOTSTRAPS = 10;
  
  public static final Attribute ATTRIBUTE_ELEMENTS = new Attribute("elements"); // number of elements
  public static final Attribute ATTRIBUTE_SAMPLE_SIZE = new Attribute("sample_size"); // number of rankings in the sample
  public static final Attribute ATTRIBUTE_MISSING = new Attribute("missing"); // percentage of missing elemenets (0..1)
  public static final Attribute ATTRIBUTE_COMPLETER_MEAN = new Attribute("completer_mean");
  public static final Attribute ATTRIBUTE_COMPLETER_VAR = new Attribute("completer_var");
  public static final Attribute ATTRIBUTE_REAL_PHI = new Attribute("real_phi");
  
  public static final ArrayList<Attribute> ATTRIBUTES = new ArrayList<Attribute>();
  
  static {
    ATTRIBUTES.add(ATTRIBUTE_ELEMENTS);
    ATTRIBUTES.add(ATTRIBUTE_SAMPLE_SIZE);
    ATTRIBUTES.add(ATTRIBUTE_MISSING);
    ATTRIBUTES.add(ATTRIBUTE_COMPLETER_MEAN);
    ATTRIBUTES.add(ATTRIBUTE_COMPLETER_VAR);
    ATTRIBUTES.add(ATTRIBUTE_REAL_PHI);
  }
}
