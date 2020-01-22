package com.hf.wc.report;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFPicture;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFSimpleShape;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.util.IOUtils;

import wt.util.WTMessage;

import com.lcs.wc.client.web.CareWashTableColumn;
import com.lcs.wc.client.web.ExcelGenerator;
import com.lcs.wc.client.web.TableColumn;
import com.lcs.wc.client.web.TableData;
import com.lcs.wc.client.web.TableSectionHeader;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.flexbom.BOMColorTableColumn;
import com.lcs.wc.planning.ProductPlanTableColumn;
import com.lcs.wc.util.FileLocation;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSLog;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.RB;

public class HFExcelGenerator extends ExcelGenerator{
public static final String imagefilePath = FileLocation.imageLocation;
	private static final int preferedImageColumnWidth = Integer.parseInt(LCSProperties.get("com.lcs.wc.client.web.ExcelGenerator.imageColumnWidth", "2000"));
	private static int preferedBOMColorColumnWidth = Integer.parseInt(LCSProperties.get("com.lcs.wc.client.web.ExcelGenerator.bomColorColumnWidth", "3000"));
	private static int preferedBOMColorThumbnailWidth = Integer.parseInt(LCSProperties.get("com.lcs.wc.client.web.ExcelGenerator.bomColorThumbnailWidth", "1200"));

    public void drawHeader(){
        if(this.ethg != null){
            //this.ethg.setLabel(this.reportName);
            int rowCount = this.ethg.createHeader(this.wb, this.ws, this.columns.size());
            setRowcount(rowCount);
        }
        if (!FormatHelper.hasContent(seasonName)) {
        	return;
        }
        int t = getRowcount();
        HSSFRow row = this.ws.createRow(getRowcount());
    	t++;
        setRowcount(t);
    	HSSFCell cell = row.createCell(0);
        String lineLabel = WTMessage.getLocalizedMessage(RB.MAIN, "line_LBL", RB.objA);
        //ILTD - Custom LineLabel
        lineLabel = "Portfolio Dashboard";
        cell.setCellValue(lineLabel + " - " + seasonName);
    	HSSFCellStyle cellstyle = egh.getCellStyle(this.tableSubHeaderClass, "left", fontSize, HSSFDataFormat.getBuiltinFormat("text"), false);
    	cell.setCellStyle(cellstyle);
    }
    
    /**
     *  This method draws the text column headers for each column.
     *  The format of the Column header Content is specified by the
     *  html style class 'TABLESUBHEADER'. The following class properties
     *  control how the headers are rendered.
     *<ul> This method accesses the following TableGenerator class properties:
     * <li>&nbsp; this.wrapColumnHeaders - default = false
     * <li>&nbsp; this.sortable - default = false
     *</ul>
     *<ul> This method accesses the following TableColumn class properties:
     * <li>&nbsp; TableColumn.displayed
     * <li>&nbsp; TableColumn.columnWidth , if not null
     * <li>&nbsp; TableColumn.headerAlign
     * <li>&nbsp; TableColumn.headerLink , if has content
     * <li>&nbsp; TableColumn.headerLabel
     *</ul>
     *
     *  @return  a <code>String</code> representing the html tag code
     *  @see com.lcs.wc.client.web.TableColumn
     **/
    @Override
    public String drawContentColumnHeaders(String tid){
    	
    	int t = getRowcount();
        HSSFRow row = this.ws.createRow(getRowcount());
        t++;
        setRowcount(t);
        HSSFCell cell;
        int cellcount = 0;
        HSSFCellStyle cellstyle;
//        if(indentSections && FormatHelper.hasContent(this.groupIndex)){
//            cell = row.createCell(cellcount);
//            cell.setCellValue("");
//            cellcount++;
//        }

        if(this.columns != null){
            for (TableColumn column : this.columns) {
                if(column.isDisplayed()){
                    cell = row.createCell(cellcount);
                    cellstyle = egh.getCellStyle(this.tableSubHeaderClass, column.getAlign(), fontSize, HSSFDataFormat.getBuiltinFormat("text"), column.isExcelHeaderWrapping());
                    int width =column.getExcelMinCharWidth();
					//FX22 work, POI latest no longer needs encoding set, handles it on its own now so api was removed
                    //cell.setEncoding(HSSFCell.ENCODING_UTF_16);
                    cell.setCellStyle(cellstyle);
                    cell.setCellValue(column.getHeaderLabel());
                    if(FormatHelper.hasContent(column.getHeaderLabel())){
	                    String[] header = column.getHeaderLabel().split("\\s");
	                    for(String h : header){
	                    	if(h.length() > width){
	                    		width = h.length();
	                    	}
	                    }
                    }
                    
                    // ILTD - Custom column width for column 'Target Date & Completion Date'.
                    if("Target Date & Completion Date".equals(column.getHeaderLabel())) {
                    	this.ws.setColumnWidth(cell.getColumnIndex(), 4000);
                    }
                    else if(column.isImage()){
                    	this.ws.setColumnWidth(cell.getColumnIndex(), preferedImageColumnWidth);
                    } else {
                	if(column.isExcelColumnWidthAutoFitContent()) {
                		this.ws.autoSizeColumn(cell.getColumnIndex());
                	} else {
	                    	//8.43 is default column size
	                    	if(width>8.43)
	                    		this.ws.setColumnWidth(cell.getColumnIndex(), (int)(width*256*1.1));
                	}
                    }
                    cellcount++;
                }
            }

            // FOR UNDEFINED PRESENTATION
        } else if(this.data.size() > 0){
            Object o = this.data.iterator().next();
            if(o instanceof FlexObject){
                FlexObject flex = (FlexObject) o;
                Iterator<?> e = flex.keySet().iterator();
                while(e.hasNext()){
                    String key = (String) e.next();
                    cell = row.createCell(cellcount);
                    cellstyle = egh.getCellStyle(this.tableSubHeaderClass, "left", fontSize);
					//FX22 work, POI latest no longer needs encoding set, handles it on its own now so api was removed
                    //cell.setEncoding(HSSFCell.ENCODING_UTF_16);
                    cell.setCellStyle(cellstyle);
                    cell.setCellValue(key);
                    cellcount++;
                }
            }
        }
        return "";
    }
	
    private String getFloatFormat(TableColumn column){
        //.getDecimalPrecision()
        String stub = "#,##0";
        int prec = column.getDecimalPrecision();
        if(prec > 0){
            stub = stub + ".";
            for(int i = 0; i < prec; i++){
                stub = stub + "0";
            }
        }
        return stub;
    }

    @Override
    public String drawRows(Collection groupRows){
        Iterator<?> rows = groupRows.iterator();
        Iterator<?> columnsIt = null;
        TableColumn column = null;
        Iterator<?> keys;
        HSSFRow row;
        HSSFCell cell;
        HSSFCellStyle cellstyle;
        short cellFormat = HSSFDataFormat.getBuiltinFormat("text");
        //debug(2, "--drawRows(Collection)- ");
        boolean showColumn = true;
		String tableName;
		String uniqueColumn;
        boolean firstRow = true;
        while(rows.hasNext()){
        	int rowc = getRowcount();
            row = this.ws.createRow(rowc);
            rowc++;
            setRowcount(rowc);

            TableData td = (TableData)rows.next();
            System.out.println("---Row:  " + td);
            if(td instanceof TableSectionHeader){

                TableSectionHeader header = (TableSectionHeader) td;
                this.drawSectionHeader(header.getLabel());

                continue;
            }


            int cellcount = 0;
			
			//>>>>>>>>>>>>>>>>>>>>hf custom
			boolean includeThumbnail = false;
			int healthStatusColumn = 0;
			//int targetDateCol = 4;
			int calendarDateCol= 3;
            if(this.columns != null){

                columnsIt = this.columns.iterator();
                while(columnsIt.hasNext()){
                	showColumn = true;
                    column = (TableColumn) columnsIt.next();

                    // Determine if the column definition is overridden based on show Criteria
                    if (column.isShowCriteriaOverride()) {
                        column = column.getOverrideColumn(td);
                    }

                    System.out.println("column :  " + column);

                    if(column.isByGroup() && !firstRow){
                        td.setData(column.getTableIndex(), "");
                    }
                    if (!showRepeatedData) {
                    	String tableIndex = column.getTableIndex();
						if(tableIndex!=null && tableIndex.indexOf(".") > -1){
							tableName = tableIndex.substring(0, tableIndex.indexOf(".")).toUpperCase();
							uniqueColumn = (String)repeatedDataColumnMap.get(tableName);

							if(uniqueColumn!=null && td.getData(uniqueColumn)!=null){
								if(repeatedDataIds.get(uniqueColumn) != null && repeatedDataIds.get(uniqueColumn).equals(td.getData(uniqueColumn))){
									showColumn = false;
								}
							}
						}
                    }
                    if (column.isDisplayed()) {
                        if (column.showCell(td) && column.hasOwnerAccess(td)){
                            cell = row.createCell(cellcount);

                            // jc - Make sure we have the correct override column definition
                            column = column.getOverrideColumn(td);

                            //if("currency".equals(column.getAttributeType())){
                            //    cell.setCellStyle(ExcelGeneratorHelper.getCellStyleCurrency("", this.wb));
                            //}
							System.out.println("column.getAttributeType()>>>>>>>>>>>>>>>>"+column.getAttributeType());
                            String styleClass = this.getColumnStyleClass(td, column, this.getCellClass(this.darkRow));
                            //debug(3, "---columnDisplayed, column.showcell--" + column.getAttributeType());
                            cellFormat  = HSSFDataFormat.getBuiltinFormat("text");
                            if( "float".equals(column.getAttributeType()) || "currency".equals(column.getAttributeType()) || "constant".equals(column.getAttributeType()) ){
                            	cellFormat = df.getFormat(getFloatFormat(column));
                            } else if ( "integer".equals(column.getAttributeType()) ) {
                            	cellFormat =df.getFormat("#,##0");
                            } else if ( "date".equals(column.getAttributeType()) ) {
                            	cellFormat =df.getFormat("m/d/yy");
                            }

                            cellstyle = egh.getCellStyle(styleClass, column.getAlign(), fontSize, cellFormat, column.isExcelWrapping());
                            cell.setCellStyle(cellstyle);
                            if(column.getExcelMinCharWidth()>0)
                            this.ws.setColumnWidth(cell.getColumnIndex(), column.getExcelMinCharWidth()*256);

                            //System.out.println((short)(column.getExcelMinCharWidth()*256));

                            //debug(3, "---another columnDisplayed, column.showcell--");
           					//FX22 work, POI latest no longer needs encoding set, handles it on its own now so api was removed
							//cell.setEncoding(HSSFCell.ENCODING_UTF_16);


                            //Because of some weird formatting issue, the cell value may not match the column type
                            //We may display a character instead of what is expected.
                            //It would probably be a good idea to move all this formatting code to the TableColumns to prevent this kind of problem

                            if(column instanceof com.lcs.wc.planning.ProductPlanTableColumn){
                            	String value = column.drawExcelCell(td);
                            	if("\u2717".equals(value) || "\u2713".equals(value)){
                            		if("\u2717".equals(value) ){
                            			value = ProductPlanTableColumn.UNCHECKED_STRING;
                            		}else if("\u2717".equals(value) ){
                            			value = ProductPlanTableColumn.CHECKED_STRING;
                            		}
                            		cellFormat  = HSSFDataFormat.getBuiltinFormat("text");
                            		cellstyle = egh.getCellStyle(styleClass, column.getAlign(), fontSize, cellFormat);
                                    cell.setCellStyle(cellstyle);
                                    cell.setCellValue(value);
                                    cellcount++;
                                    continue;
                            	}

                            }
                            if (column instanceof CareWashTableColumn) {
                            	// Force non-dark style since carewash images have white background.
                            	styleClass = this.getColumnStyleClass(td, column, this.getCellClass(false));
                            	cellstyle = egh.getCellStyle(styleClass, column.getAlign(), fontSize, cellFormat, column.isExcelWrapping());
                                cell.setCellStyle(cellstyle);

                            	((CareWashTableColumn)column).drawCareWashImages(td, wb, ws, patriarch, row, cell);
                            } else if (column instanceof BOMColorTableColumn) {
                            	String imageIndex = ((BOMColorTableColumn)column).getImageIndex();
                            	if(FormatHelper.hasContent((imageIndex)) && FormatHelper.hasContent(td.getData(imageIndex))){
                            		String value = td.getData(imageIndex);
                                    String imagePath = "";
                                    StringTokenizer st = new StringTokenizer(value,"/");

                                    while(st.hasMoreTokens()) {
                                       imagePath = st.nextToken();
                                    }

                                    imagePath = imagefilePath + File.separator + imagePath;
                                    File imageFile = new File(imagePath);
                                    if(imageFile.exists() && imageFile.isFile()){
                                        try{
                                            //Grab image and get height/width and determine compression based on lcs.properties
                                            BufferedImage image=javax.imageio.ImageIO.read(imageFile);
                                            int imageWidth = image.getWidth(null);
                                            int imageHeight = image.getHeight(null);
                                            double imageRatio = (double)imageWidth / (double)imageHeight;
                                            int thumbWidth = preferedBOMColorThumbnailWidth;
                                            // if BOM color image width setting is wider than the whole BOM color column width
                                            // set it to 40% of the whole column
                                            if (preferedBOMColorColumnWidth < preferedBOMColorThumbnailWidth){
                                            	thumbWidth = (int)(preferedBOMColorColumnWidth * 0.4);
                                            }
                                            int thumbHeight = (int)(thumbWidth / imageRatio / 2);
                                            if(thumbHeight > row.getHeight()) {
                                            	row.setHeight((short)(thumbHeight)); // row height can only be enlarged
                                            }
                                        }catch(Exception e){
                                            e.printStackTrace();
                                        }
                                        // set BOM color column width
                                        ws.setColumnWidth(cell.getColumnIndex(), preferedBOMColorColumnWidth);
                                        //attach the image to worksheet in a particular column and row
                                        if (preferedBOMColorThumbnailWidth>0){
                                        	addBOMImageToWorkSheet(cell, imagePath,row.getRowNum(),cell.getColumnIndex(), preferedBOMColorColumnWidth, preferedBOMColorThumbnailWidth);
                                        }

                                    }
                            	} else {
                            		 ws.setColumnWidth(cell.getColumnIndex(), preferedBOMColorColumnWidth);
                            		 if (preferedBOMColorThumbnailWidth>0 && FormatHelper.hasContent(td.getData(column.getBgColorIndex()))){
                            			 addBOMColorToWorkSheet(cell, row.getRowNum(), cell.getColumnIndex(), td.getData(column.getBgColorIndex()), preferedBOMColorColumnWidth, preferedBOMColorThumbnailWidth);
                            		 }
                            	}
                            }
                            System.out.println("COlumn is image >>>>>>>>>>>>>>>>>>>>>>>>>>>>"+column.getTableIndex());
                            if(column.isImage()){
								//Fix for bug id - 19727
								if("LCSPRODUCT.PARTPRIMARYIMAGEURL".equalsIgnoreCase(column.getTableIndex()))
								{
									//System.out.println("::::::inside if partPrimary"+ td.getData(column.getTableIndex()));
									includeThumbnail=true;
									 healthStatusColumn =healthStatusColumn +1;
									// targetDateCol = targetDateCol+1;
									 calendarDateCol= calendarDateCol+1;
								}
								
                                if(!FormatHelper.hasContent(column.getBgColorIndex()) || !FormatHelper.hasContent(td.getData(column.getBgColorIndex())) || (FormatHelper.hasContent(column.getTableIndex()) && "LCSCOLOR.thumbnail".equals(column.getTableIndex()) && td.getData(column.getTableIndex())!=null && FormatHelper.hasContent(td.getData(column.getTableIndex())))){
                                	if(td.getData(column.getTableIndex()) != null && FormatHelper.hasContent(td.getData(column.getTableIndex())) ){
                                        String value = td.getData(column.getTableIndex());
                                        String imagePath = "";
                                        StringTokenizer st = new StringTokenizer(value,"/");

                                        while(st.hasMoreTokens()) {
                                           imagePath = st.nextToken();
                                        }


                                        /*
                                         *Image stored in DB URL is exact name of iamge on server so no need to decode it as it was encoded to save and if decode name will be wrong.
                                        try{
                                            //need to decode the image path as it is gotten from the web url stored on the object.
                                            System.out.println("******************image Path before decode is=" + imagePath);

                                            imagePath= java.net.URLDecoder.decode(imagePath, defaultCharsetEncoding);
                                            System.out.println("******************image Path after decode is=" + imagePath);
                                        }catch(Exception e){
                                            e.printStackTrace();
                                        }*/

                                        imagePath = imagefilePath + File.separator + imagePath;
                                        File imageFile = new File(imagePath);
										System.out.println("imageFile>>>"+imageFile);
                                        if(imageFile.exists() && imageFile.isFile()){
                                            try{
                                                //Grab image and get height/width and determine compression based on lcs.properties
                                                BufferedImage image=javax.imageio.ImageIO.read(imageFile);
                                                int imageWidth = image.getWidth(null);
                                                int imageHeight = image.getHeight(null);
                                                double imageRatio = (double)imageWidth / (double)imageHeight;
                                                int thumbWidth = preferedImageColumnWidth;
                                                int thumbHeight = (int)(thumbWidth / imageRatio / 2);
                                                if(thumbHeight > row.getHeight()) {
                                                	row.setHeight((short)(thumbHeight)); // row height can only be enlarged
                                                }
                                            }catch(Exception e){
                                                e.printStackTrace();
                                            }
                                            //attach the image to worksheet in a particular column and row
                                            addImageToWorkSheet(imagePath,row.getRowNum(),cell.getColumnIndex());
                                            //griffin
                                        }else{
                                            System.out.println("Could not find image to attach, possibly file does not exist in the images folder or a folder was passed in. " + imagePath);
                                        }
                                    }else{
										System.out.println("Column Data does not match up with result set objects so dont do anything!");
                                        // CG: Column Data does not match up with result set objects so dont do anything!
                                    }
                                }else{
                                    //could set the width and height but not going to right now. Just going to take default. If someone complains can cahnge easily with lines below.
                                    //this.ws.setColumnWidth((short)cell.getCellNum(), (short)1200);
                                    //row.setHeight((short)300);

                                    //Since its a color but is overridden by an image we are not going to show it so check that does not have data so be sure color and no image is present.
                                    if(!FormatHelper.hasContent(td.getData(column.getTableIndex()))){
                                    //assuming must be a color then if a background color is set and column is image so need to add as a shape.
                                        addColorToWorkSheet(row.getRowNum(), cell.getColumnIndex(), td.getData(column.getBgColorIndex()));
                                    }
                                }
                            }



                            // jc - Call the proper setCellValue() to ensure excel data type is not loaded as text
                            if ( ! FormatHelper.hasContent(column.getAttributeType()))  {
                                //cellstyle.setDataFormat(df.getFormat("text"));
                                //cell.setCellStyle(cellstyle);
								//CGriffin - If block put in so that if bg color is set and is image but no color specified then set to --. Ex: color column on material search page but no color on material.
								if((column.isImage() && FormatHelper.hasContent(column.getBgColorIndex())) && !FormatHelper.hasContent(td.getData(column.getBgColorIndex()))){
									cell.setCellValue(new HSSFRichTextString("--"));
									System.out.println(">>>>>>>>>>>here>>>>>>>>>>>>>");
									
								} // ILTD - Custom logic to insert Health Status image in the generated excel.
								else if(cellcount == healthStatusColumn) {
									
									// Insert Health Status image
									String healthStatus = td.getData("PRODUCT.HEALTHSTATUSFORREPORT");
									String imagePath = FileLocation.wt_home + LCSProperties.get("flexPLM.webHome.imageLocation") + "/images/";
									if("SAFE".equals(healthStatus)) {
										imagePath +=  "greenAlert.png";
									}
									else if("WARNING".equals(healthStatus)) {
										imagePath += "warning_16x16.gif";
									}
									else if("ALERT".equals(healthStatus)) {
										imagePath += "failure_fixable_16x16.gif";
									}
									
								    try {
										FileInputStream stream = new FileInputStream( imagePath );
										byte[] bytes = IOUtils.toByteArray(stream); 
										
										HSSFClientAnchor anchor = new HSSFClientAnchor();  
										//HSSFClientAnchor object sets the excel cell location where  the image needs to be inserted  
										//(col1, row1, x1, y1, col2, row2, x2, y2) 
										anchor.setAnchor((short)cellcount, rowc-1, 300, 20,(short)(cellcount+1), rowc-1, 0, 0); 
										anchor.setAnchorType(HSSFClientAnchor.MOVE_AND_RESIZE);
										int index=wb.addPicture(bytes, HSSFWorkbook.PICTURE_TYPE_PNG);  
										HSSFSheet sheet=wb.getSheetAt(0);  
										HSSFPatriarch patriarch=sheet.createDrawingPatriarch();  
										HSSFPicture picture = patriarch.createPicture(anchor,index);
										// Resize the image as per scale
										picture.resize(0.5);
									} catch (FileNotFoundException e) {
										LCSLog.debug(e.getMessage());
									} catch (IOException e) {
										LCSLog.debug(e.getMessage());
									}
									
								}// ILTD - Formatting the display value for 'Target Date & Completion Date' column.
								/*else if(cellcount == targetDateCol) {
									
									String targetCompletionLabel = "Target Date\nCompletion Date";
									
									HSSFFont font1 = wb.createFont();
	                                font1.setFontHeightInPoints((short)8);
	                                font1.setFontName("Arial");
	                                font1.setBoldweight(true);
	                                
	                                HSSFRichTextString richText = new HSSFRichTextString(targetCompletionLabel);
	                                richText.applyFont(font1);
									HSSFCellStyle style =  cell.getCellStyle();
	                                style.setWrapText(true);
	                                cell.setCellStyle(style);
	                                cell.setCellValue(richText);
								}*/ // ILTD - Formatting the display value for calendar dates.
								else if(cellcount >= calendarDateCol) {	
								System.out.println("calendarDateCol>>>"+calendarDateCol);
								System.out.println("cellcount>>>"+cellcount);
	                                
	                                HSSFFont font1 = wb.createFont();
	                                font1.setFontHeightInPoints((short)8);
	                                font1.setFontName("Arial");
	                                font1.setColor(HSSFColor.BLACK.index);
	                                
	                                HSSFFont font2 = wb.createFont();
	                                font2.setFontHeightInPoints((short)8);
	                                font2.setFontName("Arial");
	                                font2.setColor(HSSFColor.BLACK.index);
	                                
									String val = column.drawExcelCell(td);
	                                HSSFRichTextString richText = new HSSFRichTextString(val);
	                                String tgtDateColor = "";
	                                String compDateColor = "";
	                                // Get the display color for dates.
	                                String dateColor = td.getData(column.getTableIndex()+"_DATECOLOR");
									System.out.println("dateColor>>>"+dateColor);
	                                if(FormatHelper.hasContent(dateColor) ) {
	                                	
	                                	tgtDateColor = dateColor;
	                                	
	                                }
	                                
	                                if("red".equalsIgnoreCase(tgtDateColor)) {
	                                	font1.setColor(HSSFColor.RED.index);
	                                } else if("green".equalsIgnoreCase(tgtDateColor)) {
	                                	font1.setColor(HSSFColor.GREEN.index);
	                                }
	                                
	                               
	                                
	                                // If both Target date and Completion date exists.
	                                if(val.length() > 10) {
	                                	String date1 = val.substring(0,10);
	                                	
	                                	richText = new HSSFRichTextString(date1);
	                                	richText.applyFont( 0, 10, font1);
	                                	
	                                } // If only target date exists. 
	                                else {
	                                	richText.applyFont(font1);
	                                }
	                                HSSFCellStyle style = cell.getCellStyle();
	                                style.setWrapText(true);
	                                cell.setCellStyle(style);
	                                cell.setCellValue(richText);
	                                
								}else{
									cell.setCellValue(column.drawExcelCell(td));
								}
								
                            } else if ( "float".equals(column.getAttributeType()) || "currency".equals(column.getAttributeType()) || "constant".equals(column.getAttributeType()) ) {
                                //cellstyle.setDataFormat(df.getFormat("#,###.##"));
                                //cellstyle.setDataFormat(df.getFormat(getFloatFormat(column)));

                                //cell.setCellStyle(cellstyle);

                                column.setFormat(FormatHelper.FLOAT_FORMAT_NO_SYMBOLS);
                                String cellValue = column.drawExcelCell(td);
                                if(FormatHelper.hasContentAllowZero(cellValue)){
                                    cell.setCellValue(new Double(cellValue).doubleValue());

									int cw = (int)Math.round((1.1 *cellValue.length() * 256 ));
                                    if(cw  > this.ws.getColumnWidth(cell.getColumnIndex())){
                                    	this.ws.setColumnWidth(cell.getColumnIndex(), cw);
                                    }
                                }else{
                                	//Added this to allow putting a blank row
                                	cell.setCellValue(new HSSFRichTextString(""));
                                }
                            } else if ( "integer".equals(column.getAttributeType()) ) {
                                //cellstyle.setDataFormat(df.getFormat("#,##0"));
                                //cell.setCellStyle(cellstyle);

                                column.setFormat(FormatHelper.FLOAT_FORMAT_NO_SYMBOLS);
                                String cellValue = column.drawExcelCell(td);
                                if(FormatHelper.hasContentAllowZero(cellValue)){
                                    cell.setCellValue(new Integer(cellValue).intValue());

									int cw = (int)Math.round((1.1 *cellValue.length() * 256 ));
                                    if(cw  > this.ws.getColumnWidth(cell.getColumnIndex())){
                                    	this.ws.setColumnWidth(cell.getColumnIndex(), cw);
                                    }
                                }else{
                                	//Added this to allow putting a blank row
                                	cell.setCellValue(new HSSFRichTextString(""));
                                }
                            } else if ( "date".equals(column.getAttributeType()) ) {
                                //cellstyle.setDataFormat(df.getFormat("m/d/yy"));
								//cell.setCellStyle(cellstyle);
								String dateString = column.drawExcelCell(td);
								if (FormatHelper.hasContent(dateString)) {
								    cell.setCellValue(new HSSFRichTextString(FormatHelper.applyFormat(dateString, FormatHelper.DATE_ONLY_IGNORE_TZ_STRING_FORMAT)));
								} else {
								    cell.setCellValue(new HSSFRichTextString(""));
								}
                            } else {
                            	String cellValue = column.drawExcelCell(td);
                            	if(FormatHelper.hasContent(cellValue) && cellValue.length() > 255){
	                        		cellFormat  = 0;
	                        		cellstyle = egh.getCellStyle(styleClass, column.getAlign(), fontSize, cellFormat, column.isExcelWrapping());
	                                cell.setCellStyle(cellstyle);
                            	}

                                cell.setCellValue(cellValue);
                            }
                            cellcount++;
                        } else {
					        //debug(4, "---columnDisplayed--");
                            cell = row.createCell(cellcount);
                            cellstyle = egh.getCellStyle(this.getCellClass(this.darkRow), column.getAlign(), fontSize);
                            cell.setCellStyle(cellstyle);
                            cell.setCellValue("");
                            cellcount++;
                        }
                    }

                    // && ((column.isByGroup() && firstRow) || !column.isByGroup())
                    if(showColumn && (false) && ((column.isTotal() || column.isSubTotal()) || (this.groupByColumns != null && this.groupByColumns.contains(column)))){
                        this.addToTotals(td, column);
                    }
                    if(showColumn && this.showDiscreteRows && column.isDiscreteCount()){
                        this.addToDiscreteSet(td, column);
                    }
                }

            } else {
                if(td instanceof FlexObject){
                    FlexObject flex = (FlexObject) td;
                    keys = flex.keySet().iterator();
                    String key;
                    while(keys.hasNext()){
                        key = (String) keys.next();
                        cell = row.createCell(cellcount);
                        cellstyle = egh.getCellStyle(this.getCellClass(this.darkRow), "left", fontSize);
                        //FX22 work, POI latest no longer needs encoding set, handles it on its own now so api was removed
						//cell.setEncoding(HSSFCell.ENCODING_UTF_16);
                        cell.setCellStyle(cellstyle);
                        cell.setCellValue(flex.getString(key));
                        cellcount++;
                    }
                }
            }
            firstRow = false;
            this.darkRow = !this.darkRow;
            if (!showRepeatedData){
				Iterator<?> tableNameIterator= repeatedDataColumnMap.keySet().iterator();

				while(tableNameIterator.hasNext()){
					tableName = (String)tableNameIterator.next();
					uniqueColumn = (String)repeatedDataColumnMap.get(tableName);
					if(FormatHelper.hasContent(td.getData(uniqueColumn))){
						repeatedDataIds.put(uniqueColumn, td.getData(uniqueColumn));
					}else{
						repeatedDataIds.remove(uniqueColumn);
					}
				}
            }
        }
        return "";
    }

    private void addColorToWorkSheet(int cellRow, int cellColumn, String hexValue){
        //griffin

        int redInt = 0;
        int greenInt = 0;
        int blueInt = 0;

        if(hexValue != null){
           com.lcs.wc.color.LCSColorLogic logic = new com.lcs.wc.color.LCSColorLogic();
           redInt = logic.toDex(hexValue.substring(0,2));
           greenInt = logic.toDex(hexValue.substring(2,4));
           blueInt = logic.toDex(hexValue.substring(4,6));
        }

        try{
                HSSFClientAnchor anchor;
                anchor = new HSSFClientAnchor( 0, 0, 1023, 255, (short) cellColumn, cellRow, (short) cellColumn, cellRow );
                anchor.setAnchorType(HSSFClientAnchor.MOVE_AND_RESIZE);

                HSSFSimpleShape s = patriarch.createSimpleShape(anchor);
                s.setShapeType(HSSFSimpleShape.OBJECT_TYPE_RECTANGLE);
                s.setFillColor(redInt,greenInt,blueInt);

        }catch(Exception e){
               System.out.println("A Exception has occured adding a color to the Excel Worksheet. - " + e);
        }
    }

}
