/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.drexel.cs.db.db4pref.posterior.labeled;

import java.util.HashMap;
import java.util.Map;

/**
 * Constraints of label ranges. For example, labelx.min &lt; 10, labelx.max &gt; labely.max.
 * 
 * Currently the labels are all denoted by their indices in itemSetList.
 *
 * @author hping
 */
public class LabelRanges {
  
  protected Map<Integer, Integer> labelMinLessThan = new HashMap<>();
  protected Map<Integer, Integer> labelMinGreaterThan = new HashMap<>();
  protected Map<Integer, Integer> labelMaxLessThan = new HashMap<>();
  protected Map<Integer, Integer> labelMaxGreaterThan = new HashMap<>();

  protected Map<Integer, Integer> labelMinLessThanLabelMin = new HashMap<>();
  protected Map<Integer, Integer> labelMaxLessThanLabelMax = new HashMap<>();
  protected Map<Integer, Integer> labelMinLessThanLabelMax = new HashMap<>();
  protected Map<Integer, Integer> labelMaxLessThanLabelMin = new HashMap<>();

  public void calculateInspectionTimes(){
    // TODO
  }
  
  public void addLabelMinLessThan(Integer label, Integer upperbound) {
    labelMinLessThan.put(label, upperbound);
  }

  public void addLabelMinGreaterThan(Integer label, Integer lowerbound) {
    labelMinGreaterThan.put(label, lowerbound);
  }

  public void addLabelMaxLessThan(Integer label, Integer upperbound) {
    labelMaxLessThan.put(label, upperbound);
  }

  public void addLabelMaxGreaterThan(Integer label, Integer lowerbound) {
    labelMaxGreaterThan.put(label, lowerbound);
  }

  public void addLabelMinLessThanLabelMin(Integer label1, Integer label2) {
    labelMinLessThanLabelMin.put(label1, label2);
  }

  public void addLabelMaxLessThanlabelMax(Integer label1, Integer label2) {
    labelMaxLessThanLabelMax.put(label1, label2);
  }

  public void addLabelMinLessThanlabelMax(Integer label1, Integer label2) {
    labelMinLessThanLabelMax.put(label1, label2);
  }

  public void addLabelMaxLessThanlabelMin(Integer label1, Integer label2) {
    labelMaxLessThanLabelMin.put(label1, label2);
  }
}
