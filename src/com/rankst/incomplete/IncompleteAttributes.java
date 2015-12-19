package com.rankst.incomplete;

import java.util.ArrayList;
import weka.core.Attribute;


public class IncompleteAttributes {
  
  public static final int BOOTSTRAPS = 10;
  public static final int RESAMPLE_SIZE = 10000;
  
  public static final Attribute ATTRIBUTE_ELEMENTS = new Attribute("elements"); // number of elements
  public static final Attribute ATTRIBUTE_SAMPLE_SIZE = new Attribute("sample_size"); // number of rankings in the sample
  public static final Attribute ATTRIBUTE_RESAMPLE_SIZE = new Attribute("resample_size");
  public static final Attribute ATTRIBUTE_MISSING = new Attribute("missing"); // percentage of missing elemenets (0..1)
  public static final Attribute ATTRIBUTE_TRIANGLE_NO_ROW = new Attribute("triangle_no_row"); 
  public static final Attribute ATTRIBUTE_TRIANGLE_BY_ROW = new Attribute("triangle_by_row");
  public static final Attribute ATTRIBUTE_COMPLETER_MEAN = new Attribute("completer_mean");
  public static final Attribute ATTRIBUTE_COMPLETER_VAR = new Attribute("completer_var");
  public static final Attribute ATTRIBUTE_REAL_PHI = new Attribute("real_phi");
  
  public static final ArrayList<Attribute> ATTRIBUTES = new ArrayList<Attribute>();
  
  static {
    ATTRIBUTES.add(ATTRIBUTE_ELEMENTS);
    ATTRIBUTES.add(ATTRIBUTE_SAMPLE_SIZE);
    ATTRIBUTES.add(ATTRIBUTE_RESAMPLE_SIZE);
    ATTRIBUTES.add(ATTRIBUTE_MISSING);
    ATTRIBUTES.add(ATTRIBUTE_TRIANGLE_NO_ROW);
    ATTRIBUTES.add(ATTRIBUTE_TRIANGLE_BY_ROW);
    ATTRIBUTES.add(ATTRIBUTE_COMPLETER_MEAN);
    ATTRIBUTES.add(ATTRIBUTE_COMPLETER_VAR);
    ATTRIBUTES.add(ATTRIBUTE_REAL_PHI);
  }
  
}
