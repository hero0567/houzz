package org.roomy.houzz.pageitem;

import java.util.HashMap;
import java.util.Map;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class HouzzCrawler extends WebCrawler
{
  private int count = 0;
  private Map<String, String> map = new HashMap<String, String>();

  public HouzzCrawler()
  {
  }

  /**
   * This method receives two parameters. The first parameter is the page in
   * which we have discovered this new url and the second parameter is the new
   * url. You should implement this function to specify whether the given url
   * should be crawled or not (based on your crawling logic). In this example,
   * we are instructing the crawler to ignore urls that have css, js, git, ...
   * extensions and to only accept urls that start with
   * "http://www.ics.uci.edu/". In this case, we didn't need the referringPage
   * parameter to make the decision.
   */
  @Override
  public boolean shouldVisit(Page referringPage, WebURL url)
  {
    String parent = referringPage.getWebURL().getURL();
    String parentSku = ParseUtil.getSku(parent);

    String child = url.getURL();
    String childSku = ParseUtil.getSku(child);

    if (!parentSku.equals(childSku))
    {
      System.out.println("Sku url changed from: " + parentSku + " to: " + childSku);
      if (ParseUtil.loadFromLocal(child, parentSku))
      {
        return false;
      }
      map.put(childSku, parentSku);
    }
    return true;
  }

  @Override
  public void visit(Page page)
  {
    String sku = "";
    try
    {
      String url = page.getWebURL().getURL();
      System.out.println("URL: " + url);

      StringBuilder b = new StringBuilder();
      sku = ParseUtil.getSku(url);
      if (sku == null)
      {
        b.append("url not corrected for:").append(url);
        ParseUtil.results.add(b.toString());
        return;
      }
      if (page.getParseData() instanceof HtmlParseData)
      {
        HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
        String html = htmlParseData.getHtml();
        String oldSku = map.get(sku);
        ParseUtil.parseHtml(sku, html, oldSku);
        ParseUtil.saveHtml(sku, html);
        if (oldSku != null)
        {
          ParseUtil.saveHtml(oldSku, html);
        }

      }
    }
    catch (Exception e)
    {
      System.out.println(sku + ": is exception.");
      ParseUtil.results.add(sku + ": is exception.");
    }
  }
}
