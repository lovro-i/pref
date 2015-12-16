package com.rankst.util;

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
  

}
