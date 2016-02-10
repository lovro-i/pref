package edu.drexel.cs.db.rank.incomplete;

import java.util.ArrayList;
import weka.core.Attribute;


public class IgnorantIncompleteAttributes {

    public static final Attribute ATTRIBUTE_ITEMS = new Attribute("items");
    public static final Attribute ATTRIBUTE_SAMPLE_SIZE = new Attribute("sample_size");
    public static final Attribute ATTRIBUTE_RESAMPLE_SIZE = new Attribute("resample_size");
    public static final Attribute ATTRIBUTE_MISSING_RATE = new Attribute("missing_rate"); 
    public static final Attribute ATTRIBUTE_BOOTSTRAPS = new Attribute("bootstraps"); 
    public static final Attribute ATTRIBUTE_TRIANGLE_THRESHOLD = new Attribute("triangle_threshold"); 
    
    public static final Attribute ATTRIBUTE_TRIANGLE_NO_ROW = new Attribute("triangle_no_row"); 
    public static final Attribute ATTRIBUTE_TRIANGLE_BY_ROW = new Attribute("triangle_by_row");
    public static final Attribute ATTRIBUTE_COMPLETER_MEAN = new Attribute("completer_mean");
    public static final Attribute ATTRIBUTE_COMPLETER_VAR = new Attribute("completer_var");
    public static final Attribute ATTRIBUTE_REAL_PHI = new Attribute("real_phi");
    
    
    public static ArrayList<Attribute> getAttributes() {
      ArrayList<Attribute> attributes = new ArrayList<Attribute>();
      attributes.add(ATTRIBUTE_ITEMS);
      attributes.add(ATTRIBUTE_SAMPLE_SIZE);
      attributes.add(ATTRIBUTE_RESAMPLE_SIZE);
      attributes.add(ATTRIBUTE_MISSING_RATE);
      attributes.add(ATTRIBUTE_BOOTSTRAPS);
      attributes.add(ATTRIBUTE_TRIANGLE_THRESHOLD);
      attributes.add(ATTRIBUTE_TRIANGLE_NO_ROW);
      attributes.add(ATTRIBUTE_TRIANGLE_BY_ROW);
      attributes.add(ATTRIBUTE_COMPLETER_MEAN);
      attributes.add(ATTRIBUTE_COMPLETER_VAR);
      attributes.add(ATTRIBUTE_REAL_PHI);
      return attributes;
    }
    
}
