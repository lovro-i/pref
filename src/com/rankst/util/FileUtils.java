package com.rankst.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class FileUtils {

  public static PrintWriter append(File file) throws IOException {
    FileWriter fstream = new FileWriter(file, true);
    return new PrintWriter(new BufferedWriter(fstream));
  }

  
  public static PrintWriter write(File file) throws IOException {
    FileWriter fstream = new FileWriter(file, false);
    return new PrintWriter(new BufferedWriter(fstream));
  }
  
  public static ArrayList<String> read(File file) throws FileNotFoundException {
    Scanner s = new Scanner(file);
    ArrayList<String> list = new ArrayList<String>();
    while (s.hasNext()) {
      list.add(s.next());
    }
    s.close();
    return list;
  }

}
