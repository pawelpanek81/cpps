package pl.copytools.ps;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.*;
import java.util.Base64;

public class Ps {

  private static final String TERMINATION_SEQ = "!";
  private static final String CONTINUATION_SEQ = "@";
  private static String contents;

  public static void main(String[] args) throws IOException, UnsupportedFlavorException {

    if (args.length != 1) {
      System.exit(1);
    }

    contents = "";

    File file = new File(args[0]);
    sendAndWait(CONTINUATION_SEQ);
    try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
      byte[] rawData;
      contents = getClipboard();
      while (!contents.equals(TERMINATION_SEQ)) {
//        System.err.print(contents);
        rawData = Base64.getDecoder().decode(contents);
        out.write(rawData);
        sendAndWait(CONTINUATION_SEQ);
        contents = getClipboard();
      }
      out.flush();
    }
  }

  private static void sendAndWait(String base64) throws IOException, UnsupportedFlavorException {
    setClipboard(base64);
//    System.err.print(base64);
    while(true) {
      if (!getClipboard().equals(CONTINUATION_SEQ)) {
        break;
      }
    }
  }

  private static String getClipboard() throws UnsupportedFlavorException, IOException {
    Clipboard clipboard;
    while (true) {
      try {
        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        return (String) clipboard.getData(DataFlavor.stringFlavor);
      } catch (IllegalStateException e) {
        // try more luck :-)
      }
    }
  }

  private static void setClipboard(String data) {
    Clipboard clipboard;
    while (true) {
      try {
        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(data), null);
        break;
      } catch (IllegalStateException e) {
        // try more luck :-)
      }
    }
  }
}
