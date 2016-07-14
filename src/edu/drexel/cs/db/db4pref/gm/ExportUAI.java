package edu.drexel.cs.db.db4pref.gm;

import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.MapPreferenceSet;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.posterior.PreferenceExpander;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/** Saves graphical model in UAI format */
public class ExportUAI {

  private final GraphicalModel gm;

  public ExportUAI(GraphicalModel gm) {
    this.gm = gm;
  }
  
  /** Older version
  @Deprecated
  public String out() {
    StringBuilder sb = new StringBuilder();
    List<Variable> vars = gm.getVariables();
    
    sb.append("MARKOV\n");
    sb.append(vars.size()).append('\n');
    
    // Cardinalities
    for (Variable var: vars) {
      sb.append(var.getValues().size());
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
      List<Integer> domain = new ArrayList<Integer>(var.getValues());
      Collections.sort(domain);
      List<Variable> parents = var.getParents();
      
      
      if (parents.isEmpty()) {
        sb.append(domain.size()).append('\n');
        for (Integer v: domain) {
          double p = var.getProbability(v);
          sb.append(p);
          sb.append(' ');
        }
        sb.append('\n');
      }
      else if (parents.size() == 1) {
        Variable parent = parents.get(0);
        List<Integer> domain1 = new ArrayList<Integer>(parent.getValues());
        Collections.sort(domain1);
        sb.append(domain.size() * domain1.size()).append('\n');
        for (Integer v: domain) {
          for (Integer v1: domain1) {
            double p = var.getProbability(v, v1);
            sb.append(p);
            sb.append(' ');
          }
          sb.append('\n');
        }
      }
      else if (parents.size() == 2) {
        Variable parent1 = parents.get(0);
        Variable parent2 = parents.get(1);
        List<Integer> domain1 = new ArrayList<Integer>(parent1.getValues());
        Collections.sort(domain1);
        List<Integer> domain2 = new ArrayList<Integer>(parent2.getValues());
        Collections.sort(domain2);
        sb.append(domain.size() * domain1.size() * domain2.size()).append('\n');
        for (Integer v: domain) {
          for (Integer v1: domain1) {
            for (Integer v2: domain2) {
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
  */
 
  private int cardinality(Variable var) {
    return Collections.max(var.getValues()) + 1; // +1 because it's zero indexed
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
  
  /** Sort variables by number of parents */
  private List<Variable> sortVariablesBySize(List<Variable> variables) {
    List<Variable> vars = new ArrayList<Variable>();
    List<Variable> from = new ArrayList<Variable>(variables);
    
    int parentCount = 0;
    while (!from.isEmpty()) {
      Iterator<Variable> it = from.iterator();
      while (it.hasNext()) {
        Variable var = it.next();
        if (var.getParents().size() == parentCount) {
          vars.add(var);
          it.remove();
        }
      }
      parentCount++;
    }
    return vars;
  }
  
  @Override
  public String toString() {
    List<Variable> vars = sortVariablesBySize(sortVariablesByDependence((gm.getVariables())));
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
      sb.append(cardinality(var));
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
      int car = cardinality(var);
      
      if (parents.isEmpty()) {
        sb.append(car).append('\n');
        for (int v = 0; v < car; v++) {
          double p = var.getProbability(v);
          sb.append(p);
          sb.append(' ');
        }
        sb.append('\n');
      }
      else if (parents.size() == 1) {
        Variable parent = parents.get(0);
        int car1 = cardinality(parent);
        sb.append(car * car1).append('\n');
        for (int v = 0; v < car; v++) {
          for (int v1 = 0; v1 < car1; v1++) {
            double p = var.getProbability(v, v1);
            sb.append(p).append(' ');
          }
          sb.append('\n');
        }
      }
      else if (parents.size() == 2) {
        Variable parent1 = parents.get(0);
        int car1 = cardinality(parent1);
        Variable parent2 = parents.get(1);
        int car2 = cardinality(parent2);
        sb.append(car * car1 * car2).append('\n');
        for (int v = 0; v < car; v++) {
          for (int v1 = 0; v1 < car1; v1++) {
            for (int v2 = 0; v2 < car2; v2++) {
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
  
  public static void main(String[] args) throws UnsupportedEncodingException {
    ItemSet items = new ItemSet(10);
    items.tagOneBased();
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), 0.2);

    MapPreferenceSet v = new MapPreferenceSet(items);
    //v.addByTag(4, 2);
    //v.addByTag(4, 3);
    v.addByTag(2, 4);
    // v.addByTag(3, 4);
    // v.addByTag(4, 6);
    // v.addByTag(3, 1);

    PreferenceExpander exp = new PreferenceExpander(model);
    double p = exp.getProbability(v);
    System.out.println(Math.log10(p));

//    System.sb.appendln(Math.pow(10, -0.0934217));
    
    GraphicalModel gm = new GraphicalModel(model, v);
    gm.setOneBased(true);
    gm.build();
//    gm.display();
    ExportUAI uai = new ExportUAI(gm);
    System.out.println(uai);

  }
  
}
