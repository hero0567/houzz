package org.roomy.houzz.pageitem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class HouzzMain
{

  private static String[] files = null;
  private static String expired = "10/1/2017";
  private static Workbook wb;
  private static String excel = "ROBatch0006.xlsx";
  private static String sheet = "Sheet1";

  public static void main(String[] args) throws Exception
  {

    if (args.length > 0)
    {
      excel = args[0];
    }
    if (args.length > 1)
    {
      sheet = args[1];
    }

    if (licenceCheck())
    {
      return;
    }

    String crawlStorageFolder = "./crawler";
    int numberOfCrawlers = 30;

    CrawlConfig config = new CrawlConfig();
    config.setCrawlStorageFolder(crawlStorageFolder);
    config.setMaxDepthOfCrawling(0);
    /*
     * Instantiate the controller for this crawl.
     */
    PageFetcher pageFetcher = new PageFetcher(config);
    RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
    RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig,
        pageFetcher);
    CrawlController controller = new CrawlController(config, pageFetcher,
        robotstxtServer);

    addExcelSeed(controller, excel, sheet);

    controller.start(HouzzCrawler.class, numberOfCrawlers);
    ParseUtil.saveToResult();
    addResultToExcel(excel, sheet);
  }

  public static boolean licenceCheck()
  {
    Date cur = new Date();
    Date.parse(expired);
    return cur.getTime() > Date.parse(expired);
  }

  public static void addResultToExcel(String excel, String sheet) throws Exception
  {
    List<String> results = loadResult();
    ExcelUtil.save(wb, excel, sheet, results);
  }

  public static List<String> loadResult() throws Exception
  {
    List<String> list = new ArrayList<String>();
    File results = new File("./result.txt");
    if (!(results.isFile() && results.exists()))
    {
      return list;
    }

    InputStreamReader read = new InputStreamReader(new FileInputStream(results), "UTF-8");
    BufferedReader reader = new BufferedReader(read);
    String line;
    while ((line = reader.readLine()) != null)
    {
      list.add(line);
    }
    reader.close();
    read.close();
    return list;
  }

  public static void findMissingSku() throws IOException
  {
    List<String> missList = new ArrayList<String>();
    File results = new File("./result.txt");
    if (!(results.isFile() && results.exists()))
    {
      return;
    }

    InputStreamReader read = new InputStreamReader(new FileInputStream(results), "UTF-8");
    BufferedReader reader = new BufferedReader(read);
    String line;
    while ((line = reader.readLine()) != null)
    {
      String[] arry = line.split(";");
      boolean found = loadFromLocal(arry[0].trim());
      if (!found)
      {
        missList.add(arry[0].trim());
      }
    }
    reader.close();
    read.close();

    String fname = "./missing.txt";
    FileOutputStream fs = new FileOutputStream(new File(fname));
    PrintStream p = new PrintStream(fs);
    for (String l : missList)
    {
      p.println(l);
    }
    p.close();
    fs.close();
  }

  public static boolean loadFromLocal(String sku)
  {
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
        return true;
      }
    }
    return false;
  }

  public static void addExcelSeed(CrawlController controller, String excel, String sheet)
  {
    int local = 0;
    int remote = 0;
    wb = ExcelUtil.createWb(excel);
    Sheet urlSheet = wb.getSheet(sheet);
    if (urlSheet == null)
    {
      System.out.println("Sheet name is incorrect. Please check the name or save the excel!");
      System.out.println("Please make sure you have two paramenters! e.g. java -jar xxx.jar <file name> <sheet name>.");
      return;
    }
    int urlIndex = ExcelUtil.getURLIndex(urlSheet);
    List<String> urls = ExcelUtil.readRow(wb, sheet, urlIndex);
    for (String url : urls)
    {
      if (!(url.startsWith("www") || url.startsWith("WWW")
          || url.startsWith("http") || url.startsWith("HTTP")))
      {
        continue;
      }
      if (!url.startsWith("http"))
      {
        url = "http://" + url;
      }
      if (ParseUtil.loadFromLocal(url, null))
      {
        local++;
        System.out.println("Load from local count:" + local);
        System.out.println("Load from net count:" + remote);
        continue;
      }
      controller.addSeed(url);
      remote++;
      System.out.println("add seed:" + url);
      System.out.println("Load from local count:" + local);
      System.out.println("Load from net count:" + remote);
    }
  }
}
