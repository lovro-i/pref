package edu.drexel.cs.db.db4pref.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;


public class TestUtils {

  public static PrintWriter getWriter(String[] args) throws IOException {
    PrintWriter out;
    if (args.length > 0) {
      File file = new File(args[0]);
      Logger.info("Writing to %s", file);
      out = FileUtils.append(file);
    }
    else {
      Logger.warn("No file specified, writing to console");
      out = new PrintWriter(System.out);
    }
    return out;
  }
    
}
