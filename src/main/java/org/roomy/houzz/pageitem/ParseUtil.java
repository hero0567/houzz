// PACKAGE/IMPORTS --------------------------------------------------
package org.roomy.houzz.pageitem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Update with a detailed description of the interface/class.
 *
 */
public class ParseUtil
{
  private static int count = 0;
  private static String[] files = null;
  public static List<String> results = new ArrayList<String>();

  public static String getSku(String url)
  {
    String sku = null;
    String[] name = url.split("/");
    if (name.length < 5)
    {
      System.out.println("url not corrected for:" + url);
      return sku;
    }
    sku = name[4];
    return sku;
  }

  public static boolean loadFromLocal(String url, String oldSku)
  {
    String sku = getSku(url);
    if (files == null)
    {
      File f = new File("pages");
      files = f.list();
      if (files == null)
      {
        return false;
      }
    }

    for (String file : files)
    {
      if (file.equals(sku + ".html"))
      {
        System.out.println("Load file from local:" + sku);
        String html = readHtml("./pages/" + file);
        parseHtml(sku, html, oldSku);
        return true;
      }
    }
    System.out.println("not found:" + sku);
    return false;
  }

  public static String readHtml(String path)
  {
    StringBuilder html = new StringBuilder();
    try
    {

      File file = new File(path);
      if (file.isFile() && file.exists())
      {
        InputStreamReader read = new InputStreamReader(new FileInputStream(file), "UTF-8");
        BufferedReader reader = new BufferedReader(read);
        String line;
        while ((line = reader.readLine()) != null)
        {
          html.append(line).append("\n");
        }
        reader.close();
        read.close();
      }
    }
    catch (Exception e)
    {
      System.out.println("File load failed:" + path);
    }
    return html.toString();
  }

  /**
   * 
   * @param sku
   * @param html
   * @param oldSku the url will be changed to another url. To make sure
   *          could match the oldSku, then save the data to oldSku instead of
   *          the new SKU from url
   */
  public static void parseHtml(String sku, String html, String oldSku)
  {
    System.out.println("Begin to parse html:" + sku);
    StringBuilder b = new StringBuilder();
    boolean isSizeweight = false;
    boolean isCategory = false;
    boolean ismanuf = false;
    int manufline = 0;
    String manuf = "";
    String sizeweight = "";
    String category = "";

    String[] arr = html.split("\n");
    for (String s : arr)
    {
      if (s.indexOf("compId=\"manuf\"") > 0)
      {
        ismanuf = true;
        continue;
      }
      if (ismanuf)
      {
        manufline++;
        if (manufline == 3)
        {
          manuf = getValue(s);
          ismanuf = false;
          System.out.println("          " + manuf);
        }
        continue;
      }
      if (s.indexOf("<dt class=\"key\">Size") > 0)
      {
        isSizeweight = true;
        continue;
      }
      if (s.indexOf("<dt class=\"key\">Weight") > 0)
      {
        isSizeweight = true;
        continue;
      }
      if (isSizeweight)
      {
        sizeweight = getValue(s);
        System.out.println("          " + sizeweight);
        sizeweight = getWDH(sizeweight);
        System.out.println("          " + sizeweight);
        isSizeweight = false;
        continue;
      }
      if (s.indexOf("<dt class=\"key\">Category</dt>") > 0)
      {
        isCategory = true;
        continue;
      }
      if (isCategory)
      {
        category = getValue(s);
        category = getValue(category);
        isCategory = false;
        System.out.println("          " + category);
        break;
      }
    }
    if (oldSku != null)
    {
      sku = oldSku;
    }
    sku = removeLastChar(sku);
    manuf = removeLastChar(manuf);
    sizeweight = removeLastChar(sizeweight);
    if (sizeweight.equals(""))
    {
      sizeweight = ";;";
    }
    category = removeLastChar(category);

    sku = replaceUnusedString(sku);
    manuf = replaceUnusedString(manuf);
    sizeweight = replaceUnusedString(sizeweight);
    category = replaceUnusedString(category);

    b.append(sku).append(";").append(manuf).append(";").append(sizeweight).append(";").append(category);
    results.add(b.toString());
  }

  private static String removeLastChar(String s)
  {
    if (s == null)
    {
      return s;
    }
    if (s.endsWith(";"))
    {
      s = s.substring(0, s.length() - 1);
    }
    return s;
  }

  public static String replaceUnusedString(String s)
  {
    if (s == null)
    {
      return "";
    }
    s = s.replaceAll("</span>", "");
    return s;
  }

  public static String getValue(String line)
  {
    int begein = line.indexOf(">");
    int end = line.lastIndexOf("<");
    if (begein < end)
    {
      String value = line.substring(begein + 1, end);
      return value;
    }
    return line;
  }

  public static String getWDH(String line)
  {
    String[] wdh = line.split("/");
    String value = "";
    if (wdh.length > 2)
    {
      String w = wdh[0].trim().replace("\"", "");
      String d = wdh[1].trim().replace("\"", "");
      String h = wdh[2].trim().replace("\"", "");
      value = w + ";" + d + ";" + h;
    }
    return value;
  }

  public static void saveHtml(String sku, String html) throws IOException
  {
    String fname = sku + ".html";
    String path = "pages";
    if (!new File(path).exists())
    {
      new File(path).mkdirs();
    }
    FileOutputStream out = new FileOutputStream(new File("pages//" + fname));
    PrintStream p = new PrintStream(out);
    p.print(html);
    p.close();
    out.close();

    System.out.println("Save page " + fname);
  }

  public static void saveToResult()
  {
    try
    {
      if (count < results.size())
      {
        String fname = "./result.txt";
        FileOutputStream fs = new FileOutputStream(new File(fname));
        PrintStream p = new PrintStream(fs);
        count = results.size();
        for (String line : ParseUtil.results)
        {
          p.println(line);
        }
        p.close();
      }
    }
    catch (Exception e)
    {
      System.out.println("Output failed.");
    }
  }
}
