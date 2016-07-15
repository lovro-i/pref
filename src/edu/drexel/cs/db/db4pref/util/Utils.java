package edu.drexel.cs.db.db4pref.util;


public class Utils {

  public static final long MB = 1024 * 1024;

  public static final long ONE_SECOND = 1000;
  public static final long ONE_MINUTE = 60 * ONE_SECOND;
  public static final long ONE_HOUR = 60 * ONE_MINUTE;
  public static final long ONE_DAY = 24 * ONE_HOUR;
  
  
  public static void memStat() {
    long heapSize = Runtime.getRuntime().totalMemory(); 
    long heapMaxSize = Runtime.getRuntime().maxMemory();
    long heapFreeSize = Runtime.getRuntime().freeMemory(); 
    System.out.println(String.format("Heap: %d MB, max: %d MB, free: %d MB", heapSize / MB, heapMaxSize / MB, heapFreeSize / MB));
  }
  
  public static boolean equals(Object obj1, Object obj2) {
    if (obj1 == null) return obj2 == null;
    if (obj2 == null) return false;
    return obj1.equals(obj2);
  }
 
  
  public static String ellipsis(String s, int len) {
    if (len >= s.length()) return s;
    return s.substring(0, len) + "...";
  }
}
