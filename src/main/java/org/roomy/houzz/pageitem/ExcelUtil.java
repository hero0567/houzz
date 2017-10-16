// PACKAGE/IMPORTS --------------------------------------------------
package org.roomy.houzz.pageitem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * TODO: Update with a detailed description of the interface/class.
 *
 */
public class ExcelUtil
{

  private static int start = 7;
  private static final String MANUF = "Manufacture";
  private static final String CATEGORY = "Category";
  private static final String W = "Width";
  private static final String D = "Depth";
  private static final String H = "Height";

  public static Workbook createWb(String name)
  {
    InputStream ins = null;
    Workbook wb = null;
    try
    {
      ins = new FileInputStream(new File(name));
      wb = WorkbookFactory.create(ins);
      ins.close();
    }
    catch (Exception e)
    {
      System.out.println("Failed to load excel file. Please make sure the name is corrected.");
    }
    return wb;
  }

  public static List<String> readRow(Workbook wb, String name, int index)
  {
    List<String> list = new ArrayList<String>();
    Sheet sheet = wb.getSheet(name);
    for (Row row : sheet)
    {
      Cell cell = row.getCell(index);
      String value = cell.getStringCellValue();
      if (value != null && value.trim().length() > 0)
      {
        list.add(value);
      }
    }
    //list.clear();
    //list.add("www.houzz.com/photos/38125605");
    return list;
  }

  public static void save(Workbook wb, String excel, String name, List<String> results) throws Exception
  {
    System.out.println("Start to merge result to excel.");
    Sheet sheet = wb.getSheet(name);
    int urlIndex = ExcelUtil.getURLIndex(sheet);

    addHeader(sheet.getRow(0));
    for (Row row : sheet)
    {
      addSearchResult(row, results, urlIndex);
    }

    FileOutputStream excelFileOutPutStream = new FileOutputStream(excel);
    wb.write(excelFileOutPutStream);
    excelFileOutPutStream.flush();
    excelFileOutPutStream.close();
    System.out.println("Successful to merge result to excel.");
  }

  public static void addHeader(Row row)
  {
    int index = getExcelIndex(row);
    start = index;
    int begein = start;

    Cell cell = row.getCell(begein + 1);
    if (cell == null)
    {
      row.createCell(begein + 1).setCellValue(MANUF);
    }
    else
    {
      cell.setCellValue(MANUF);
    }

    cell = row.getCell(begein + 2);
    if (cell == null)
    {
      row.createCell(begein + 2).setCellValue(CATEGORY);
    }
    else
    {
      cell.setCellValue(CATEGORY);
    }

    cell = row.getCell(begein + 3);
    if (cell == null)
    {
      row.createCell(begein + 3).setCellValue(W);
    }
    else
    {
      cell.setCellValue(W);
    }

    cell = row.getCell(begein + 4);
    if (cell == null)
    {
      row.createCell(begein + 4).setCellValue(D);
    }
    else
    {
      cell.setCellValue(D);
    }

    cell = row.getCell(begein + 5);
    if (cell == null)
    {
      row.createCell(begein + 5).setCellValue(H);
    }
    else
    {
      cell.setCellValue(H);
    }
  }

  public static int getExcelIndex(Row row)
  {
    short countNum = row.getLastCellNum();
    for (short i = 0; i < countNum; i++)
    {
      String value = getCell(row.getCell(i));
      if ("Manufacture".equalsIgnoreCase(value))
      {
        return i;
      }
    }
    return countNum + 2;
  }

  public static void addSearchResult(Row row, List<String> results, int index)
  {
    int begin = start;
    Cell cell = row.getCell(index);
    String url = String.valueOf(getCell(cell));
    if (url == null || url.trim().length() < 5)
    {
      return;
    }

    System.out.println("Merge result for:" + url);
    for (String line : results)
    {
      String[] arr = line.split(";");
      if (arr.length < 6)
      {
        continue;
      }
      String sku = arr[0];
      if (url.trim().indexOf(sku.trim()) > 0)
      {
        String manuf = arr[1];
        String w = arr[2];
        if (w != null)
        {
          w = w.replace("W ", "");
        }
        String d = arr[3];
        if (d != null)
        {
          d = d.replace("D ", "");
        }
        String h = arr[4];
        if (h != null)
        {
          h = h.replace("H ", "");
          h = h.replace("&nbsp", "");
        }
        String category = arr[5];
        cell = row.getCell(begin + 1);
        if (cell == null)
        {
          row.createCell(begin + 1).setCellValue(manuf);
        }
        else
        {
          cell.setCellValue(manuf);
        }

        cell = row.getCell(begin + 2);
        if (cell == null)
        {
          row.createCell(begin + 2).setCellValue(category);
        }
        else
        {
          cell.setCellValue(category);
        }

        cell = row.getCell(begin + 3);
        if (cell == null)
        {
          row.createCell(begin + 3).setCellValue(w);
        }
        else
        {
          cell.setCellValue(w);
        }

        cell = row.getCell(begin + 4);
        if (cell == null)
        {
          row.createCell(begin + 4).setCellValue(d);
        }
        else
        {
          cell.setCellValue(d);
        }

        cell = row.getCell(begin + 5);
        if (cell == null)
        {
          row.createCell(begin + 5).setCellValue(h);
        }
        else
        {
          cell.setCellValue(h);
        }

        return;
      }
    }
  }

  public static String getCell(Cell cell)
  {
    String cellValue = "";
    if (cell == null)
      return cellValue;
    switch (cell.getCellType())
    {
      case Cell.CELL_TYPE_NUMERIC:
        cellValue = Double.valueOf(cell.getNumericCellValue()).longValue() + "";
        break;
      case Cell.CELL_TYPE_BOOLEAN:
        cellValue = cell.getBooleanCellValue() + "";
        break;
      case Cell.CELL_TYPE_FORMULA:
        cellValue = cell.getCellFormula() + "";
        break;
      case Cell.CELL_TYPE_STRING:
        cellValue = cell.getRichStringCellValue().toString() + "";
        break;
      case Cell.CELL_TYPE_ERROR:
        cellValue = cell.getErrorCellValue() + "";
        break;
      default:
        cellValue = "";
        break;
    }
    return cellValue.trim();
  }

  public static int getURLIndex(Sheet urlSheet)
  {
    Row row = urlSheet.getRow(0);
    for (int i = 0; i < 20; i++)
    {
      String value = row.getCell(i).getStringCellValue();
      if ("Product URL".equalsIgnoreCase(value))
      {
        System.out.println("Read url from excel column:" + value);
        return i;
      }
    }
    return 0;
  }

  public static void main(String[] args) throws Exception
  {
    Workbook wb = ExcelUtil.createWb("ROBatch0006.xlsx");
    ExcelUtil.readRow(wb, "Sheet1", 2);
    ExcelUtil.save(wb, "ROBatch0006.xlsx", null, null);
  }
}
