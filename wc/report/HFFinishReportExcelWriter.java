package com.hf.wc.report;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.hssf.record.DefaultRowHeightRecord;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.lcs.wc.util.LCSProperties;

import org.apache.poi.util.IOUtils;
import org.apache.log4j.Logger;
/**
 * @author ITCINFOTECH 
 * This class is triggered for Custom Where Used Volume Report Excel Generation.
 */
public final class HFFinishReportExcelWriter {
	/**
	 * Variable to store excel image File Path.
	 */
	public static final String IMAGEPATH	=	LCSProperties.get("com.hf.wc.report.HFFinishReportExcelWriter.imagePath"); 
	/**
	 * Constructor object.
	 */
	private HFFinishReportExcelWriter() {

	}
	/**
	 * Logger object.
	 */
	private static Logger log = Logger.getLogger(HFFinishReportExcelWriter.class.getName());
	/**
	 * This method writes into the excel file.
	 * @param reportList (ArrayList<String>
	 * @param fileNamae String.
	 * @throws IOException.
	 */
	public static void writeToExcel(List<String> reportList, String fileNamae) throws IOException {
		//Creating Object of WorkBook.
		XSSFWorkbook workbook = new XSSFWorkbook();
		//Assinging the workSheet Name.
		log.info("reportList:"+reportList.size());
		XSSFSheet sheet = workbook.createSheet("Finish_Report");
		//Sey Column width
		sheet.setColumnWidth(1, 8000);
		sheet.setColumnWidth(2, 5000);
		sheet.setColumnWidth(3, 6000);
		sheet.setColumnWidth(4, 3000);  
		sheet.setColumnWidth(5, 2000); 
		sheet.setColumnWidth(6, 2000);
		//Reading the windchill home path
		String wthome = wt.util.WTProperties.getLocalProperties().getProperty("wt.home");
		//Creating font object
		XSSFFont font = workbook.createFont();
		font.setFontHeightInPoints((short) 13);
		font.setBold(true);
		XSSFFont finsihfont = workbook.createFont();
		finsihfont.setFontHeightInPoints((short) 20);
		finsihfont.setBold(true);
		//Formating cell values 
		CellStyle valuestyle	=	workbook.createCellStyle();
		valuestyle.setBorderBottom(BorderStyle.DOUBLE);
		valuestyle.setBorderTop(BorderStyle.DOUBLE);
		valuestyle.setBorderRight(BorderStyle.DOUBLE);
		valuestyle.setBorderLeft(BorderStyle.DOUBLE);
		//New Cell style
		CellStyle  style = workbook.createCellStyle();
		style.setFont(font);
		style.setBorderBottom(BorderStyle.DOUBLE);
		style.setBorderTop(BorderStyle.DOUBLE);
		style.setBorderRight(BorderStyle.DOUBLE);
		style.setBorderLeft(BorderStyle.DOUBLE);
		//Reading the path of the Image 
		InputStream inputStream = new FileInputStream(wthome +IMAGEPATH);
		byte[] bytes = IOUtils.toByteArray(inputStream);
		int pictureIdx = workbook.addPicture(bytes, XSSFWorkbook.PICTURE_TYPE_PNG);
		inputStream.close();
		CreationHelper helper = workbook.getCreationHelper();
		Drawing drawing = sheet.createDrawingPatriarch();
		//Set the image location in excel
		ClientAnchor anchor = helper.createClientAnchor();
		anchor.setCol1(0);
		anchor.setRow1(0);
		anchor.setCol2(8);
		anchor.setRow2(5);
		//adding image to the excel file
		drawing.createPicture(anchor, pictureIdx);
		//Cell pictureCell = sheet.createRow(2).createCell(1);
		//Set Row Height
		DefaultRowHeightRecord height = new DefaultRowHeightRecord();
		height.setRowHeight((short) 50);
		//New Cell Style 
		XSSFCellStyle finishstyle = workbook.createCellStyle();
		finishstyle.setFont(finsihfont);
		finishstyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
		finishstyle.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
		//Intialize row numbers to start writing the values
		Row row = sheet.createRow(5);
		Cell cell = row.createCell(0);
		cell.setCellValue("Part Finish Report");
		cell.setCellStyle(finishstyle);
		//Merging the cell
		sheet.addMergedRegion(new CellRangeAddress(5, 6, 0, 7));
		int rowNum = 9;
		int rowNum1 = 8;
		Row row1 = sheet.createRow(rowNum);
		Row row2 = sheet.createRow(rowNum1);
		int colNum = 0;
		//Set the display column
		int columnNumber	=	0;
		List <String> header	=	new ArrayList<String>();
		header.add("S.No");
		header.add("Part Number");
		header.add("Model Number");
		header.add("Assembly Number");
		header.add("Finish");
		header.add("Quantity");
		header.add("Price");
		header.add("Volume");
		while (columnNumber < header.size()){
		Cell cell1 = row2.createCell(columnNumber);
		cell1.setCellValue(header.get(columnNumber));
		columnNumber ++;
		}
		int sno = 1;
		//Looping through the List values
		for (String j : reportList) {
			//Break the row when the column limit reaches
			if (colNum == 8) {
				rowNum++;
				colNum = 0;
				row1 = sheet.createRow(rowNum);
				sheet.autoSizeColumn(colNum);
			}
			//increment the cell value
			if (colNum == 0) {
				Cell newcell = row1.createCell(colNum++);
				newcell.setCellValue(String.valueOf(sno));
				newcell.setCellStyle(valuestyle);
				sno++;
				//set column size
				sheet.autoSizeColumn(colNum);
			}
			//increment the cell value
			Cell valuecell = row1.createCell(colNum++);
			valuecell.setCellValue((String) j);
			valuecell.setCellStyle(valuestyle);
		}
		//Set cell formats for specific rows
		for (int i = 0; i < row2.getLastCellNum(); i++) {
			row2.getCell(i).setCellStyle(style);
		}
		//Writing values to excel file.
			FileOutputStream outputStream = new FileOutputStream(fileNamae);
			try{
			log.info("Writing to excel file");
			workbook.write(outputStream);}
			finally{
			// close Work book & write Stream
			outputStream.close();
			workbook.close();}
		
	}
}