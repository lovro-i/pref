package edu.drexel.cs.db.rank.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

  public static PrintWriter append(File file) throws IOException {
    FileWriter fstream = new FileWriter(file, true);
    return new PrintWriter(new BufferedWriter(fstream));
  }

  
  public static PrintWriter write(File file) throws IOException {
    FileWriter fstream = new FileWriter(file, false);
    return new PrintWriter(new BufferedWriter(fstream));
  }
  
  public static String read(File file) throws IOException {
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      StringBuilder sb = new StringBuilder();
      String line = br.readLine();

      while (line != null) {
        sb.append(line);
        sb.append(System.lineSeparator());
        line = br.readLine();
      }
      return sb.toString();
    }
  }
  
  public static List<String> readLines(File file) throws IOException {
    return readLines(new FileReader(file));
  }
  
  public static List<String> readLines(Reader reader) throws IOException {
    List<String> lines = new ArrayList<String>();
    try (BufferedReader br = new BufferedReader(reader)) {
      String line = br.readLine();

      while (line != null) {
        lines.add(line);
        line = br.readLine();
      }
      return lines;
    }
  }
  
  /*
  public static ArrayList<String> read(File file) throws FileNotFoundException {
    Scanner s = new Scanner(file);
    ArrayList<String> list = new ArrayList<String>();
    while (s.hasNext()) {
      list.add(s.next());
    }
    s.close();
    return list;
  }
  */
}
