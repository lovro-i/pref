package com.rankst.util;


public class Logger {

  public static void info(String msg, Object... args) {
    System.out.println(String.format(msg, args));
  }
  
  public static void info(Object obj) {
    System.out.println(obj.toString());
  }
    
}
