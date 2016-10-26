package edu.drexel.cs.db.db4pref.posterior.app;

import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.MapPreferenceSet;


public class Test {

  public static MapPreferenceSet pref1() {
    ItemSet its = new ItemSet(30);
    its.tagOneBased();
    MapPreferenceSet pref = new MapPreferenceSet(its);
    pref.addByTag(24, 19);
    pref.addByTag(26, 11);
    pref.addByTag(25, 14);
    pref.addByTag(25, 15);
    pref.addByTag(22, 13);
    return pref;
  }
  
  
  public static MapPreferenceSet pref2() {
    ItemSet its = new ItemSet(30);
    its.tagOneBased();
    MapPreferenceSet pref = new MapPreferenceSet(its);
    pref.addByTag(18, 11);
    pref.addByTag(4, 30);
    pref.addByTag(25, 22);
    pref.addByTag(16, 30);
    pref.addByTag(1, 18);
    return pref;
  }
  
  /** Around 11 seconds */
  public static MapPreferenceSet pref3() {
    ItemSet its = new ItemSet(30);
    its.tagOneBased();
    MapPreferenceSet pref = new MapPreferenceSet(its);
    pref.addByTag(7, 16);
    pref.addByTag(24, 3);
    pref.addByTag(20, 4);
    pref.addByTag(6, 22);
    pref.addByTag(23, 14);
    return pref;
  }
  
  /** Around 3 seconds; -4.583774 */
  public static MapPreferenceSet pref4() {
    ItemSet its = new ItemSet(30);
    its.tagOneBased();
    MapPreferenceSet pref = new MapPreferenceSet(its);
    pref.addByTag(27, 24);
    pref.addByTag(16, 2);
    pref.addByTag(11, 30);
    pref.addByTag(9, 29);
    pref.addByTag(27, 17);
    return pref;
  }
  
  /** Around 110 seconds */
  public static MapPreferenceSet pref5() {
    ItemSet its = new ItemSet(30);
    its.tagOneBased();
    MapPreferenceSet pref = new MapPreferenceSet(its);
    pref.addByTag(30, 1);
    pref.addByTag(3, 23);
    pref.addByTag(21, 9);
    pref.addByTag(4, 25);
    pref.addByTag(14, 20);
    return pref;
  }

  /** Around 70 seconds */
  public static MapPreferenceSet pref6() {
    ItemSet its = new ItemSet(30);
    its.tagOneBased();
    MapPreferenceSet pref = new MapPreferenceSet(its);
    pref.addByTag(34, 30);
    pref.addByTag(35, 1);
    pref.addByTag(31, 32);
    pref.addByTag(16, 35);
    pref.addByTag(36, 26);
    return pref;
  }
  
}
