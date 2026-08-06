package com.github.warren_bank.iptv_organizer.utils;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

public class StreamUtils {

  public static String read(InputStream inputStream) {
    String result = null;

    try {
      InputStreamReader isReader = new InputStreamReader(inputStream, "UTF-8");
      BufferedReader   bufReader = new BufferedReader(isReader);

      ArrayList<String> lines = new ArrayList<String>();
      String line;
      while ((line = bufReader.readLine()) != null) {
        lines.add(line);
      }

      result = TextUtils.join("\n", lines);
    } catch (Exception e) {
    } finally {
      try {
        if (inputStream != null) inputStream.close();
      } catch (Exception ignored) {}
    }

    return result;
  }

  public static void write(OutputStream outputStream, String text) {
    try {
      byte[] bytes = text.getBytes("UTF-8");
      outputStream.write(bytes);
      outputStream.flush();
    } catch (Exception e) {
    } finally {
      try {
        if (outputStream != null) outputStream.close();
      } catch (Exception ignored) {}
    }
  }

  public static void pipeFromFile(File srcFile, OutputStream outputStream) {
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(srcFile);
      byte[] buffer = new byte[8192];
      int bytesRead;

      while ((bytesRead = fis.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
      }
      outputStream.flush();
    }
    catch(Exception ignored) {}
    finally {
      try {
        if (fis != null) fis.close();
      } catch (Exception ignored) {}
      try {
        if (outputStream != null) outputStream.close();
      } catch (Exception ignored) {}
    }
  }

  public static void pipeToFile(InputStream inputStream, File dstFile) {
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(dstFile);
      byte[] buffer = new byte[8192];
      int bytesRead;

      while ((bytesRead = inputStream.read(buffer)) != -1) {
        fos.write(buffer, 0, bytesRead);
      }
    }
    catch(Exception ignored) {}
    finally {
      try {
        if (inputStream != null) inputStream.close();
      } catch (Exception ignored) {}
      try {
        if (fos != null) fos.close();
      } catch (Exception ignored) {}
    }
  }

}
