package edu.drexel.cs.db.db4pref.gm;

import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.MapPreferenceSet;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.posterior.PreferenceExpander;
import edu.drexel.cs.db.db4pref.posterior.Span;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

/** Saves graphical model in UAI format */
public class ExportUAI {

  private final GraphicalModel gm;

  public ExportUAI(GraphicalModel gm) {
    this.gm = gm;
  }
  
 
  private Span getSpan(Variable var) {
    int min = Integer.MAX_VALUE;
    int max = Integer.MIN_VALUE;
    for (int v: var.getValues()) {
      min = Math.min(min, v);
      max = Math.max(max, v);
    }
    return new Span(min, max);
  }
  

  /** Returns true if all var's parents are in the to set */
  private boolean containsParents(Variable var, Set<Variable> to) {
    for (Variable parent: var.getParents()) {
      if (!to.contains(parent)) return false;
    }
    return true;
  }
  
  /** Sort variables by dependence */
  private List<Variable> sortVariablesByDependence(List<Variable> variables) {
    List<Variable> vars = new ArrayList<Variable>();
    List<Variable> from = new ArrayList<Variable>(variables);
    Set<Variable> to = new HashSet<Variable>();
    while (vars.size() < variables.size()) {
      Iterator<Variable> it = from.iterator();
      while (it.hasNext()) {
        Variable var = it.next();
        if (containsParents(var, to)) {
          vars.add(var);
          to.add(var);
          it.remove();
        }
      }
    }
    
    return vars;
  }
  
  
  
  @Override
  public String toString() {
    List<Variable> vars = gm.getVariables(); // sortVariablesByDependence((gm.getVariables()));
    return toString(vars);
  }
    
  /** Return UAI with random variable order */
  public String toStringRandom() {
    List<Variable> vars = new ArrayList<Variable>(gm.getVariables());
    Collections.shuffle(vars);
    return toString(vars);
  } 

  private String toString(List<Variable> vars) {
    StringBuilder sb = new StringBuilder();
    
    // Collections.swap(vars, vars.size()-1, vars.size()-2);
    
    sb.append("MARKOV\n");
    sb.append(vars.size()).append('\n');
    
    // Cardinalities
    for (Variable var: vars) {
      sb.append(getSpan(var).size());
      sb.append(' ');
    }
    sb.append('\n');

    
    // Clique count
//    int cliques = 0;
//    for (Variable var: vars) {
//      if (!var.getParents().isEmpty()) cliques++;
//    }
//    sb.appendln(cliques);
    sb.append(vars.size()).append('\n');
    
    // Cliques
    for (Variable var: vars) {
      List<Variable> parents = var.getParents();
      // if (parents.isEmpty()) continue;
      sb.append(parents.size() + 1);
      sb.append(' ');
      sb.append(vars.indexOf(var));
      for (Variable parent: parents) {
        sb.append(' ');
        sb.append(vars.indexOf(parent));
      }
      sb.append('\n');
    }
    
    
    // Tables
    for (Variable var: vars) {
      sb.append('\n');
      List<Variable> parents = var.getParents();
      Span span = getSpan(var);
      
      if (parents.isEmpty()) {
        sb.append(span.size()).append('\n');
        for (int v = span.from; v <= span.to; v++) {
          double p = var.getProbability(v);
          sb.append(p);
          sb.append(' ');
        }
        sb.append('\n');
      }
      else if (parents.size() == 1) {
        Variable parent = parents.get(0);
        Span span1 = getSpan(parent);
        sb.append(span.size() * span1.size()).append('\n');
        for (int v = span.from; v <= span.to; v++) {
          for (int v1 = span1.from; v1 <= span1.to; v1++) {
            double p = var.getProbability(v, v1);
            sb.append(p).append(' ');
          }
          sb.append('\n');
        }
      }
      else if (parents.size() == 2) {
        Variable parent1 = parents.get(0);
        Span span1 = getSpan(parent1);
        Variable parent2 = parents.get(1);
        Span span2 = getSpan(parent2);
        sb.append(span.size() * span1.size() * span2.size()).append('\n');
        for (int v = span.from; v <= span.to; v++) {
          for (int v1 = span1.from; v1 <= span1.to; v1++) {
            for (int v2 = span2.from; v2 <= span2.to; v2++) {
              double p = var.getProbability(v, v1, v2);
              sb.append(p);
              sb.append(' ');
            }
          }
          sb.append('\n');
        }
      }
    }
    return sb.toString();
  }
  
  
}
