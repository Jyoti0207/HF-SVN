package com.hf.wc.product;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.log4j.Logger;

import com.lcs.wc.calendar.LCSCalendar;
import com.lcs.wc.calendar.LCSCalendarQuery;
import com.lcs.wc.calendar.LCSCalendarTask;
import com.lcs.wc.client.web.PDFGeneratorHelper;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTyped;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.LCSProductSeasonLink;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.SeasonProductLocator;
import com.lcs.wc.specification.FlexSpecification;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSLog;
import com.lcs.wc.util.MOAHelper;
import com.lcs.wc.util.VersionHelper;
import com.lowagie.text.Element;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

import wt.org.WTUser;
import wt.util.WTException;
import wt.util.WrappedTimestamp;

/**
 * This class is being used for Tech Pack generation
 * @author ITC Infotech
 * @version "true" 1.1
 */
public final class HFPDFHelper {
	
	/**
	 * Default constructor
	 */
	private HFPDFHelper(){
		
	}

	private static Logger loggerObject = Logger.getLogger(HFProductPlugins.class);
	// instance variable for page options
	public static final String PAGE_OPTIONS = "PAGE_OPTIONS";
	// instance variable for scope of source
	public static final String SCOPE_SOURCINGCONFIG = "SOURCING_CONFIG_SCOPE";
	private static final String dateFormat = "MM/dd/yyyy";

	
	// instance variable for debugging statements

	//private static 
	private static String attType = "";
	private static final String NA = "N/A";
	private static String displayValue = NA;
	private static final String WTUSER_OBJ_ID = "wt.org.WTUser:";
	// instance of Colon
			private static final String COLON = ":";
			private static PDFGeneratorHelper pgh = new PDFGeneratorHelper();
	

	/**
	 * This method formatAttributeData() format Attribute Data
	 * @param attValue of type Object
	 * @param attribute of type FlexTypeAttribute
	 * @throws WTException
	 * @return displayValue of type String
	 */
	public static String formatAttributeData(Object attValue,
			FlexTypeAttribute attribute) throws WTException {
	
	displayValue = NA;
		if ((attValue != null) && (attribute != null)) {
			attType = attribute.getAttVariableType();
			//GeorgeDebugUtil.debugLog(DEBUG,"GeorgePDFHelper.formatAttributeData() attribute\t"+ attribute);
			displayValue = processAttributeData(attValue, attType, attribute) ;
		}
		return displayValue;
	}
	
	/**
	 * @param attKey String.
	 * @param labelStyle String.
	 * @param contentStyle String.
	 * @param product LCSProduct.
	 * @param season LCSSeason.
	 * @param link LCSProductSeasonLink.
	 * @param attScope String.
	 * @param borderWidth int.
	 * @return
	 */
	public static PdfPCell getProductAttributeCell(String attKey,
			String labelStyle, String contentStyle, LCSProduct product,LCSSeason season,LCSProductSeasonLink link,String attScope,int borderWidth) {
		//Initialize attDisplay
		String attDisplay = "";
		//Initialize displayValue
		displayValue = NA;
		try {
			// Getting sourcing config flextype
			FlexType productType = product.getFlexType();
			//Getting supplier attribute
			FlexTypeAttribute attribute = productType.getAttribute(attKey);
			attDisplay = attribute.getAttDisplay();
			Object attValue = null;
			// Check if scope is product
			if ("PRODUCT".equals(attScope)) {
				attValue = product.getValue(attKey);
			}
			// Check of scope is product-season
			else if (("PRODUCT-SEASON".equals(attScope)) && (link != null)) {
				attValue = link.getValue(attKey);
			}
			// Check if scope is placeholder
			displayValue = formatAttributeData(attValue, attribute);
			
		} //end of try block
		catch (WTException e) {
			LCSLog.error("Exception in getProductAttributeCell()"
					+ e.getMessage());
		}//end of catch block

		return createAttributeDataCell(attDisplay, labelStyle, displayValue,
				contentStyle,borderWidth);
	}
	
	/**
	 * @param taskName String.
	 * @param displayName String.
	 * @param labelStyle String.
	 * @param contentStyle String.
	 * @param link LCSProductSeasonLink.
	 * @param borderWidth int.
	 * @return
	 */
	public static PdfPCell getCalendarTaskAttributeCell(String taskName, String displayName,
			String labelStyle, String contentStyle, LCSProductSeasonLink link,int borderWidth) {
		//Initialize attDisplay
		//String attDisplay = "";
		
		DateFormat dateFormatCalendarTask = new SimpleDateFormat(dateFormat);
		try {
			LCSProduct prod = SeasonProductLocator.getProductARev(link);
			LCSCalendarTask currentTask = LCSCalendarQuery.getTask(prod.getMaster(), taskName);
			//Get Estimated end date or Traget Date --Jyoti
			if(currentTask!=null)
			{
			Date estEndDate = currentTask.getTargetDate();
			if(estEndDate ==null)
			{
				estEndDate = currentTask.getEstEndDate();
			}
			
			WrappedTimestamp time = (WrappedTimestamp) estEndDate;
			displayValue = dateFormatCalendarTask.format(time);

			
		}
		}//end of try block
		catch (WTException e) {
			LCSLog.error("Exception in getProductAttributeCell()"
					+ e.getMessage());
		}//end of catch block

		return createAttributeDataCell(displayName, labelStyle, displayValue,
				contentStyle,borderWidth);
	}
	
	public static PdfPCell getCalendarAttribute(String displayName,String labelStyle, String contentStyle, LCSProductSeasonLink link, String attKey,int borderWidth) throws WTException{
		loggerObject.info("link::::::::::::::" +link);
		LCSProduct product = SeasonProductLocator.getProductARev(link);
		DateFormat calendarDate = new SimpleDateFormat(dateFormat);
		product=(LCSProduct)VersionHelper.latestIterationOf(product.getMaster());
		String prodOid =  FormatHelper.getVersionId(product);
		
		loggerObject.info("prodOid::::::::::::::" +prodOid);
		
		LCSCalendar prdSeasonCalendar =  new LCSCalendarQuery().findByOwnerId(prodOid);
		loggerObject.info("prdSeasonCalendar::::::::::::::" +prdSeasonCalendar);
		if(prdSeasonCalendar != null){
		Date dt = (Date)prdSeasonCalendar.getValue("hfTargetDate");
		//loggerObject.info("dt>>>>>>>>>>>>"+dt);
		if(dt != null)
		{
		WrappedTimestamp time = (WrappedTimestamp)dt;
		loggerObject.info("time>>>>>>>>>>>>>>>>"+time);
		displayValue = calendarDate.format(time);
		}else{
			displayValue = NA;	
		}
		}
		else{
			displayValue = NA;
		}
		
		return createAttributeDataCell(displayName, labelStyle, displayValue,
				contentStyle,borderWidth);
		
	}	
		
	
	public static PdfPCell getProductTypeAttributeCell( String displayName,
			String labelStyle, String contentStyle, LCSProduct product,int borderWidth) {
		//Initialize attDisplay
		
	
		displayValue= product.getFlexType().getFullNameDisplay(false);

		return createAttributeDataCell(displayName, labelStyle, displayValue,
				contentStyle,borderWidth);
	}
	
	


	
	
	/**
	 * This method geCreationtDate() get Creationt Date
	 * @param labelStyle of type String
	 * @param contentStyle of type String
	 * @return createAttributeDataCell of type PdfPCell
	 */
	public static PdfPCell getCreationtDate(String labelStyle,
			String contentStyle,int borderWidth) {
		//Initialize creationDate
		String creationDate = "";		
		Date date = new Date();
		DateFormat dateFormatCreationtDate = new SimpleDateFormat(dateFormat);
		creationDate = dateFormatCreationtDate.format(date).toString();
		return createAttributeDataCell(
				"Date", labelStyle, creationDate,contentStyle,true,borderWidth);
	}
	
	
	/**
	 * @param label String.
	 * @param labelStyle String.
	 * @param content String.
	 * @param contentStyle String.
	 * @param borderWidth int.
	 * @return
	 */
	public static PdfPCell createAttributeDataCell(String label,
			String labelStyle, String content, String contentStyle,int borderWidth) {
		return createAttributeDataCell(label, labelStyle, content,
				contentStyle, true,borderWidth);
	}

	/**
	 * This method createAttributeDataCell() create Attribute Data Cell
	 * @param labelValue of type String
	 * @param labelStyle of type String
	 * @param content of type String
	 * @param contentStyle of type String
	 * @param displayLabel of type boolean
	 * @return tableCell of type PdfPCell
	 */
	private static PdfPCell createAttributeDataCell(String labelValueP,
			String labelStyle, String content, String contentStyle,
			boolean displayLabel,int borderWidth) {
		// instance PDFPTable
		float width[] = {35F,65F};
		PdfPTable table = new PdfPTable(width);
		String labelValue = labelValueP;
		if (FormatHelper.hasContent(labelValue)) {
			labelValue = labelValue + COLON;
		}
		PdfPCell cell = new PdfPCell(pgh.multiFontPara(labelValue,
				pgh.getCellFont(labelStyle, null, null)));
		cell.setBackgroundColor(PDFGeneratorHelper
				.getColor(PDFGeneratorHelper.WHITE));
		cell.setBorderWidth(borderWidth);
		cell.setHorizontalAlignment(Element.ALIGN_LEFT);
		cell.setVerticalAlignment(Element.ALIGN_TOP);
		cell.setPaddingTop(1.0f);
		cell.setPaddingBottom(1.0f);

		if (displayLabel) {
			table.addCell(cell);
		}

		cell = new PdfPCell(pgh.multiFontPara(content,
				pgh.getCellFont(contentStyle, null, null)));
		cell.setBackgroundColor(PDFGeneratorHelper
				.getColor(PDFGeneratorHelper.WHITE));
		cell.setBorderWidth(borderWidth);
		//Allign  cell to Horizontal left
		cell.setHorizontalAlignment(Element.ALIGN_LEFT);
		//Allign  cell to Horizontal top
		cell.setVerticalAlignment(Element.ALIGN_TOP);
		cell.setPaddingTop(1.0f);
		cell.setPaddingBottom(1.0f);
		table.addCell(cell);

		// Creating  tableCell of type PdfPCell
		PdfPCell tableCell = new PdfPCell(table);
		tableCell.setBorder(0);
		// Set Padding
		tableCell.setPadding(1.0f);
		//Set BackgroundColor as white
		tableCell.setBackgroundColor(PDFGeneratorHelper
				.getColor(PDFGeneratorHelper.WHITE));
		//Set BorderColor as white
		tableCell.setBorderColor(PDFGeneratorHelper
				.getColor(PDFGeneratorHelper.WHITE));
		//Allign table cell to Horizontal
		tableCell.setHorizontalAlignment(Element.ALIGN_LEFT);
		//Allign table cell to Vertical
		tableCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

		return tableCell;
	}
		
	/**
	 * This method processAttributeData() process Attribute Data
	 * @param attValue of type Object
	 * @param attType of type String
	 * @param attribute of type FlexTypeAttribute
	 * @throws WTException
	 * @return displayValue of type String
	 */
	private static String processAttributeData(Object attValue, String attType, 
			FlexTypeAttribute attribute) throws WTException{
		
		displayValue = NA;
		String stringValue = "";
		if ("userList".equals(attType)) {
			displayValue = processUserList(attValue) ;
		} else if ("date".equals(attType)) {
			WrappedTimestamp time = (WrappedTimestamp) attValue;
			DateFormat attributeDateFormat = new SimpleDateFormat(dateFormat);
			displayValue = attributeDateFormat.format(time);

		} else if ("choice".equals(attType)) {
			stringValue = (String) attValue;
			displayValue = attribute.getAttValueList().getValue(
					stringValue, null);
		} else if ("moaList".equals(attType) || "moaEntry".equals(attType)) {
			stringValue = (String) attValue;
			displayValue = MOAHelper.parseOutDelimsLocalized(stringValue,
					", ", attribute.getAttValueList(), null);
		} else if ("driven".equals(attType)) {
			stringValue = (String) attValue;
			displayValue = attribute.getAttValueList().getValue(
					stringValue, null);
		} else {
			//displayValue = attValue.toString();
			displayValue=getDisplayValue(attValue,attType);
		}
		
		return displayValue;
	}
	
	private static String getDisplayValue(Object attValue, String attType2) throws WTException {
		String stringValue = "";
		
		if ("object_ref".equals(attType2) ||("object_ref_list".equals(attType2))) {
			
			displayValue = processObjectRef(attValue);
		} 
		
		else if ("boolean".equals(attType2)) {
			displayValue= checkIfBoolean(attValue);
		} 
		/* Modified on 19/03/2013
		formating attribute of type integer*/
		else if ("integer".equals(attType2) || "sequence".equals(attType2))  {
		
			stringValue = attValue.toString();
			Double doubleValue = new Double(stringValue);
			Long longValue = doubleValue.longValue();
			stringValue = longValue.toString();
			displayValue = stringValue;
			
		}else {
			displayValue = attValue.toString();
		}
		return displayValue;
	}

	private static String checkIfBoolean(Object attValue) {
		if((boolean) attValue)
		{
			displayValue = "Yes";
		}
		else{
			displayValue = "No";
		}
		return displayValue;
	}

	/**
	 * This method processUserList() process User List
	 * @param attValue of type Object
	 * @throws WTException
	 * @return displayValue of type String
	 */
	public static String processUserList(Object attValue) throws WTException{
		displayValue=NA;
		FlexObject pmFlexobj = (FlexObject) attValue;
		String userID = (String) pmFlexobj.get("OID");		
		if (!"".equals(userID)) {	
			WTUser user = (WTUser) LCSQuery.findObjectById(WTUSER_OBJ_ID + userID);
			displayValue = user.getFullName();			
		}
		return displayValue;
	}
		
	
	/**
	 * This method processObjectRef() process Object Reference
	 * @param attValue of type Object
	 * @throws WTException
	 * @return displayValue of type String
	 */
	private static String processObjectRef(Object attValue) throws WTException{
		FlexTyped flexTypedObj = (FlexTyped)attValue;
		return (String) flexTypedObj.getValue("name");
		
	}

	/**
	 * this method to fetch season cell
	 * 
	 * @param labelStyle String.
	 * @param contentStyle String.
	 * @param season LCSSeason.
	 * @return PdfPcell
	 */
	
	public static PdfPCell getSeasonCell(String labelStyle,
			String contentStyle, LCSSeason season,int borderWidth) {
		// Initialize displayValue
		displayValue = NA;

		displayValue = season.getName();

		return createAttributeDataCell(
				"Season", labelStyle, displayValue, contentStyle,true,borderWidth);
	}
	/**
	 * @param attKey String.
	 * @param displaytext String.
	 * @param sku LCSSKU.
	 * @return PdfPCell.
	 * @throws WTException
	 */
	public static PdfPCell getSKUAttributeCell(String attKey, String displaytext, LCSSKU sku) throws WTException {
		FlexType skuType = sku.getFlexType();
		//Getting supplier attribute
		FlexTypeAttribute attribute = skuType.getAttribute(attKey);
		
		Object attValue = null;
		// Check if scope is product
	
		attValue = sku.getValue(attKey);
		
		String value = formatAttributeData(attValue, attribute);
		PdfPCell cell = new PdfPCell(pgh.multiFontPara(value,
				pgh.getCellFont("DISPLAYTEXT", null, null)));
		cell.setBackgroundColor(PDFGeneratorHelper
				.getColor(PDFGeneratorHelper.WHITE));
		cell.setHorizontalAlignment(Element.ALIGN_LEFT);
		cell.setVerticalAlignment(Element.ALIGN_TOP);
		cell.setPadding(1.0f);
		return cell;
	}
	
	/**
	 * @param attKey String.
	 * @param product LCSProduct.
	 * @return PdfPCell.
	 * @throws WTException
	 */
	public static PdfPCell getProductCoverPageAttributeCell(String attKey,  LCSProduct product) throws WTException {
		FlexType skuType = product.getFlexType();
		//Getting supplier attribute
		FlexTypeAttribute attribute = skuType.getAttribute(attKey);
		
		Object attValue = null;
		// Check if scope is product
	
		attValue = product.getValue(attKey);
		
		String value = formatAttributeData(attValue, attribute);
		PdfPCell cell = new PdfPCell(pgh.multiFontPara(value  ,
				pgh.getCellFont("DISPLAYTEXT", null, null)));
		cell.setBackgroundColor(PDFGeneratorHelper
				.getColor(PDFGeneratorHelper.WHITE));
		cell.setHorizontalAlignment(Element.ALIGN_LEFT);
		cell.setVerticalAlignment(Element.ALIGN_TOP);
		cell.setBorderWidthLeft(0);
		cell.setBorderWidthTop(0);
		cell.setUseBorderPadding(false);
		cell.setPaddingLeft(2);
		return cell;
	}
	
	/**
	 * @param attKey String.
	 * @param product LCSProduct.
	 * @return PdfPCell.
	 * @throws WTException
	 */
	public static PdfPCell getProductCoverPageLabelCell(String attKey,  LCSProduct product) throws WTException {
		FlexType skuType = product.getFlexType();
		//Getting supplier attribute
		FlexTypeAttribute attribute = skuType.getAttribute(attKey);
		
		displayValue = attribute.getAttDisplay();
		
	
		PdfPCell cell = new PdfPCell(pgh.multiFontPara(displayValue +" : "  ,
				pgh.getCellFont("FORMLABEL", null, null)));
		cell.setBackgroundColor(PDFGeneratorHelper
				.getColor(PDFGeneratorHelper.WHITE));
		
		cell.setHorizontalAlignment(Element.ALIGN_LEFT);
		cell.setVerticalAlignment(Element.ALIGN_TOP);
		cell.setBorderWidthRight(0);
		cell.setBorderWidthTop(0);
		cell.setUseBorderPadding(false);
		cell.setPaddingLeft(2);
		return cell;
	}
	
	/**
	 * @param attKey String.
	 * @param labelStyle String.
	 * @param contentStyle String.
	 * @param spec FlexSpecification.
	 * @param link LCSProductSeasonLink.
	 * @param borderWidth int.
	 * @return
	 */
	public static PdfPCell getSpecAttributeCell( String attKey,
			String labelStyle, String contentStyle, FlexSpecification spec,LCSProductSeasonLink link, int borderWidth) {
		
		//Initialize attDisplay
				String attDisplay = "";
				//Initialize displayValue
				displayValue = NA;
	
		try {
			displayValue= spec.getFlexType().getFullNameDisplay(false);
			FlexType specType = spec.getFlexType();
			//Getting supplier attribute
			FlexTypeAttribute attribute = specType.getAttribute(attKey);
			
			attDisplay = attribute.getAttDisplay();
			
			/* Object attValue = spec.getValue(attKey);*/
			
			 Object attValue = link.getValue(attKey);
		
			displayValue = formatAttributeData(attValue, attribute);
			
		} //end of try block
		catch (WTException e) {
			LCSLog.error("Exception in getProductAttributeCell()"
					+ e.getMessage());
		}//end of catch block

		return createAttributeDataCell(attDisplay, labelStyle, displayValue,
				contentStyle,borderWidth);
	
	}
}

