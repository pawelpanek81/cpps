package pl.copytools.cp;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

public class Cp {

  private static final String TERMINATION_SEQ = "!";
  private static final String CONTINUATION_SEQ = "@";

  public static void main(String[] args) throws IOException, UnsupportedFlavorException {

    if (args.length != 1) {
      System.exit(1);
    }
    Path path = Paths.get(args[0]);

    sendAndWait("");
    try (InputStream in = new BufferedInputStream(new FileInputStream(path.toFile()))) {
      byte[] temp = new byte[300];
      int len;
      while ((len = in.read(temp)) > 0) {
        byte[] data;
        if (len == temp.length) {
          data = temp;
        } else {
          data = new byte[len];
          System.arraycopy(temp, 0, data, 0, len);
        }
        sendAndWait(Base64.getEncoder().encodeToString(data));
      }
    }
    setClipboard(TERMINATION_SEQ);
  }

  private static void sendAndWait(String base64) throws IOException, UnsupportedFlavorException {
    setClipboard(base64);
//    System.err.print(base64);
    while(true) {
      if (getClipboard().equals(CONTINUATION_SEQ)) {
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