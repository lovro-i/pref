package edu.drexel.cs.db.rank.util;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;


public class Utils {

  public static long MB = 1024 * 1024;
  
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
 
    
}
