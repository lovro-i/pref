/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.drexel.cs.db.db4pref.posterior.labeled;

import edu.drexel.cs.db.db4pref.core.Item;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author hping
 */
public class State implements Cloneable {

  protected Expander expander;
  protected List<Integer> positions; // positions of topMatching items.

  /**
   * Create an empty state
   *
   * @param expander
   */
  public State(Expander expander) {
    this.expander = expander;

    positions = new ArrayList<>();
    for (int i = 0; i < expander.getInitialRanking().length(); i++) {
      positions.add(i);
    }
  }

  public State(State state) {
    this.expander = state.getExpander();
    this.positions = new ArrayList<>(state.getPositions()); // TODO syntax not sure.
  }

  public Expander getExpander() {
    return expander;
  }

  public List<Integer> getPositions() {
    return positions;
  }

  public void increasePositionByOne(int pos) {
    int prev = positions.get(pos);
    positions.set(pos, prev + 1);
  }

  public State clone() {
    return new State(this);
  }

  public void insert(Expands expands, Item item, boolean missing, double p) {
    if (missing) {
      insertMissing(expands, item, p);
    } else {
      insertPresent(expands, item, p);
    }
  }

  public void insertMissing(Expands expands, Item item, double p) {

    boolean isRelated = expander.isInPreferences(item);
    if (isRelated) {
      boolean hasParent = expander.hasLatestParent(item);
      if (hasParent) {
        int topMatchingItemPosInInitRanking = expander.getTopMatchingPositionInInitialRanking(item);
        int latestParentPosInInitRanking = expander.getLatestParentPositionInInitialRanking(item);
        insertItemToLegalPositions(expands, item, p, latestParentPosInInitRanking, topMatchingItemPosInInitRanking);
      } else {
        int topMatchingItemPosInInitRanking = expander.getTopMatchingPositionInInitialRanking(item);
        insertItemToLegalPositions(expands, item, p, -1, topMatchingItemPosInInitRanking);
      }
    } else {
      insertItemToLegalPositions(expands, item, p, -1, -1);
    }
  }

  public void insertItemToLegalPositions(Expands expands, Item item, double p, int lowerbound, int upperbound) {
    int step = expander.getReferenceIndex(item);
    int currentRankingLength = step + numberOfPreinsertedItems(step);
    List<Integer> boundaries = new ArrayList<>(positions);
    boundaries.add(0, -1);
    boundaries.add(currentRankingLength);
    for (int interval = 0; interval < boundaries.size() - 1; interval++) {

      if (interval > lowerbound && interval <= upperbound) {
        continue;
      } else {
        State newState = this.clone();
        for (int i = 0; i < positions.size(); i++) {
          if (i >= interval) {
            newState.increasePositionByOne(i);
          }
        }
        int rangeLeft = boundaries.get(interval);
        int rangeRight = boundaries.get(interval + 1);

        double prob = 0;
        for (int j = rangeLeft + 1; j <= rangeRight; j++) {
          prob += insertionProbability(step, j);
        }
        expands.add(newState, prob * p);
      }
    }
  }

  // TODO the preinserted item search should be optimized.
  public double insertionProbability(int step, int pos) {
    int count = 0;
    List<Item> initRanking = expander.getInitialRanking().getItems();
    for (int i = 0; i < positions.size(); i++) {
      if (positions.get(i) >= pos) {
        break;
      } else {
        if (expander.getReferenceIndex(initRanking.get(i)) > step) {
          count++;
        }
      }
    }

//    System.out.println(expander.getInitialRanking());
//    System.out.println(positions);
//    System.out.format("step=%d, pos=%d, count=%d\n",step, pos, count);
    double phi = expander.getModel().getPhi();
    int position = pos - count;
    double p = Math.pow(phi, Math.abs(step - position)) * (1 - phi) / (1 - Math.pow(phi, step + 1));

    return p;
  }

  public int numberOfPreinsertedItems(int step) {
    int count = 0;
    for (Item e : expander.getInitialRanking().getItems()) {
      if (expander.getReferenceIndex(e) > step) {
        count++;
      }
    }
    return count;
  }

  public void insertPresent(Expands expands, Item item, double p) {
    int step = expander.getReferenceIndex(item);
    int pos = positions.get(expander.getInitialRanking().indexOf(item));
    double prob = insertionProbability(step, pos);
    expands.add(this, prob * p);
  }

}
