package com.hf.wc.integration.util;

import java.text.*;
import java.util.*;
import org.apache.log4j.Logger;
import com.lcs.wc.color.LCSColor;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.foundation.LCSManaged;
import com.lcs.wc.moa.LCSMOAObject;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.supplier.LCSSupplier;
import com.lcs.wc.util.FormatHelper;
import wt.fc.WTObject;
import wt.util.WTException;
import wt.util.WrappedTimestamp;
/**
 * @author
 *
 */
public final class HFExtractAttsValues {
	
	/**
	 * Constructor
	 */
	private HFExtractAttsValues(){
		
	}
	/**
	 * Final String for DERIVEDSTRING
	 */
	private static final String DERIVEDSTRING = "derivedString";
	/**
	 *  Final String for BOOLEAN
	 */
	private static final String BOOLEAN = "boolean";
	/**
	 *  Final String for TEXTAREA
	 */
	private static final String TEXTAREA = "textArea";
	/**
	 *  Final String for FLOAT
	 */
	private static final String FLOAT = "float";
	/**
	 *  Final String for CURRENCY
	 */
	private static final String CURRENCY = "currency";
	/**
	 *  Final String for INTEGER
	 */
	private static final String INTEGER = "integer";
	/**
	 * Final String for CHOICE
	 */
	private static final String CHOICE = "choice";
	
	/**
	 *  Final String for DRIVEN
	 */
	private static final String DRIVEN = "driven";
	
	/**
	 *  Final String for TEXT
	 */
	private static final String TEXT = "text";
	
	/**
	 *  MULTILIST String for MULTILIST
	 */
	private static final String MULTILIST = "moaList";
	
	/**
	 *  Final String for URL
	 */
	/*private static final String URL = "url";*/

	private static Logger loggerObject = Logger.getLogger(HFExtractAttsValues.class);
	/**
	 * @param sku
	 * @param skuKeys2
	 * @param dataMap 
	 * @throws WTException
	 */
	public static Map processSKUAttKeys(LCSSKU sku, Collection keysColl, String isPart) throws WTException {
		
		HFLogFileGenerator.configureLog("Outbound");
		Map dataMap = new HashMap();
		loggerObject.info("################inside#####processSKUAttKeys########################");
		FlexType ft = sku.getFlexType();
		loggerObject.info("################Colorway FlexType----!!!!!" + ft);
		String value = "";
		Object valueObj = "";


	if(keysColl!=null){
		Iterator skuKeyIter= keysColl.iterator();
		while(skuKeyIter.hasNext()){
			value = "";
			String attKey = (String) skuKeyIter.next();
			//colName=attKey;
			FlexTypeAttribute fta = ft.getAttribute(attKey);
			String attType = fta.getAttVariableType();
			valueObj =  sku.getLogicalValue(attKey);
			if(valueObj!=null){
				value=processAtts(attType,valueObj,fta);
			}
			loggerObject.info("attKey>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+attKey);
			loggerObject.info("value>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+value);
			dataMap.put(attKey, value);
		}
	}

	
		return dataMap;
	}


	public static Map processMOAAttKeys(LCSMOAObject moaObj, Collection keysColl) throws WTException {
		loggerObject.info(":::::inside the method processMOAAttKeys:::::::::::");
		HFLogFileGenerator.configureLog("SBOM");
		Map dataMap = new HashMap();
		FlexType ft = moaObj.getFlexType();
		loggerObject.info("::::::::::::::::::Colorway FlexType>>>>>>>" + ft);
		String value = "";
		Object valueObj = "";

		if(keysColl!=null){
			Iterator moaKeyIter= keysColl.iterator();
			while(moaKeyIter.hasNext()){
				value = "";
				String attKey = (String) moaKeyIter.next();
				//colName=attKey;
				FlexTypeAttribute fta = ft.getAttribute(attKey);
				String attType = fta.getAttVariableType();
				valueObj =  moaObj.getValue(attKey);
				if(valueObj!=null){
					value=processAtts(attType,valueObj,fta);
				}
				dataMap.put(attKey,value);
			}
		} 
		loggerObject.info("Date map"+dataMap);
		return dataMap;
	}

	public static Map processProductAttKeys(LCSProduct prod,Collection prodKeysColl) throws WTException {
		loggerObject.info(":::::::::::::::inside processProductAttKeys:::::::::");
		Map prodDataMap = new HashMap();
		FlexType ftp = prod.getFlexType();
		loggerObject.info(":::::::::Product Flex Type"+ftp);
		String prodValue = "";
		Object prodValueObject="";
		
		if (prodKeysColl!=null) {
			Iterator prodKeyIter = prodKeysColl.iterator();
			while(prodKeyIter.hasNext()){
			prodValue = "";
			String prodAttKey = (String)prodKeyIter.next();
		
			FlexTypeAttribute flexTA = ftp.getAttribute(prodAttKey);
			String prodAttType = flexTA.getAttVariableType();
		
			prodValueObject =  prod.getValue(prodAttKey);
		
			if (prodValueObject!=null){
			prodValue=processAtts(prodAttType,prodValueObject,flexTA);
			}
			loggerObject.info("prodAttKey>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+prodAttKey);
			loggerObject.info("prodValue>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+prodValue);
			prodDataMap.put(prodAttKey, prodValue);
			
		}	
		
	}
		return prodDataMap;
	}
	/**
	 * @param attType
	 * @param valueObj
	 * @param fta
	 * @return
	 * @throws WTException
	 */
	@SuppressWarnings("deprecation")
	public static String processAtts(String attType, Object valueObj, FlexTypeAttribute fta) throws WTException {
		loggerObject.info("::: att type::::"+ attType);
		HFLogFileGenerator.configureLog("Outbound");
		String value="";
		boolean checkifString = DERIVEDSTRING.equals(attType) || TEXT.equals(attType)|| TEXTAREA.equals(attType);
		value =	classifiedValue1(attType,valueObj);
		if(FLOAT.equals(attType)||CURRENCY.equals(attType))  // if the attribute type is float.
		{
			value=getFloat((Double) valueObj,fta);       
			
		}
		else if (BOOLEAN.equals(attType) ) // && valueObj!=null)
		{
			if (valueObj!=null) {
				value = valueObj.toString();
				}
			else {
				value="false";
			}
		}
		 // if the attribute type is derivedString or text or boolean or textArea.
		else if(checkifString)          
		{
			   // getting value of the attribute. 
				value = (String)valueObj;            
			
		} 	
			
		else if ("".equalsIgnoreCase(value)){
			
			value	=	calssifiedValue(attType,valueObj,fta);
		}
		//adding extra ' to the obtained String
		if (FormatHelper.hasContent(value)){
		if (value.contains("'"))
		{
			value	=	value.replace("'", "''");
		}
		}
		return value;
	}
	
	
	private static String classifiedValue1(String attType, Object valueObj) {
		
		String value = "";
		if(INTEGER.equals(attType)) 
		{
				int number = FormatHelper.parseInt(valueObj.toString());
				value =String.valueOf(number);								
			
		}
		
		else if ("date".equals(attType) &&valueObj!=null) // if the attribute type is date.
		{
			value=getDate(valueObj);
			
		}
		
		else if ("url".equals(attType) && valueObj!=null) // if the attribute type is url.
		{
			value = getURL(valueObj);
			
			//value=valueObj.toString();
		}
		
		return value;
	}


	public static String calssifiedValue(String attType, Object valueObj,FlexTypeAttribute fta)  throws WTException {
		
		String value="";
		
		 if ("object_ref_list".equalsIgnoreCase(attType) ||"object_ref".equalsIgnoreCase(attType) ) {
			//LCSLifecycleManaged objRef = (LCSLifecycleManaged) valueObj;
			WTObject myObj =(WTObject) valueObj;
			if (valueObj instanceof LCSProduct){
				LCSProduct prod	=	(LCSProduct) myObj;
				value =	prod.getName();
				loggerObject.info("Prod Value:"+value);
			}
			 if (valueObj instanceof LCSSKU){
				LCSSKU sku	=	(LCSSKU) myObj;
				value =	sku.getName();
				loggerObject.info("SKU Value:::::::::::::::::::"+value);
				if (value.contains(" ")){
					//value= value.substring(0,value.indexOf(" "));
					String[] str = value.split("\\("); 
					value =  str[0].trim();
					loggerObject.info("Split SKU Value::::::::::::"+value);
				}
				
			}
			if(valueObj instanceof LCSColor) {
				LCSColor color	=	(LCSColor) myObj;
				//value = color.getName();
				 value = (String) color.getValue(HFIntegrationConstants.PACKAGINGNAME);
				
				loggerObject.info("Color Value:"+value);
			}
			
			if(valueObj instanceof LCSSupplier) {
				LCSSupplier supplier	=	(LCSSupplier) myObj;
				//value = supplier.getName();
				value = (String) supplier.getValue(HFIntegrationConstants.SUPPLIERNUMBER);
				loggerObject.info("supplier Value:"+value);
			}
			
			
		/*	loggerObject.info(">>>>>>myObj>>>>>"+myObj);
					if(valueObj !=null){
				value =(String) myObj.getIdentity();
				if (value.contains(" ")){
					value= value.substring(0,value.indexOf(" "));
				}
			}*/
		}
		else {
			
			value	=	classifiedValues2(attType,fta,valueObj);
			
			
			
			
		}
		 if ("null".equalsIgnoreCase(value)|| !FormatHelper.hasContent(value))
		 {
		 value ="";
		 }
		return value;
		
	}
	
	
	
	private static String classifiedValues2(String attType, FlexTypeAttribute fta, Object valueObj) throws WTException {
		String value = "";
		loggerObject.info("::::::::::::::inside classifiedValues2:::::::::::::::::::::");
		// if the attribute type is choice or driven.
		if (attType.equalsIgnoreCase(CHOICE) || attType.equalsIgnoreCase(DRIVEN)
				|| "ColorSelect".equalsIgnoreCase(attType)) {	
			loggerObject.info("::::::classifiedValues2::::::att Type"+ attType);
				if (valueObj != null) {
					String value1 = fta.getAttValueList().getValueFromValueStore((String) valueObj, "_CUSTOM_JDECode");
					if (FormatHelper.hasContent(value1)) {
						value = value1;
					} else {
						// getting value of the attribute.
						value = fta.getAttValueList().getValue((String) valueObj,
								com.lcs.wc.client.ClientContext.getContext().getLocale());
					}
				}
			}
		else if(attType.equalsIgnoreCase(MULTILIST)){
			
			
			
					String returnJdeValue= "";
					String value1="";
			
			if (valueObj != null) {
				System.out.println(":::::::::multilist::::::inside if>>>>>");
				
					// getting value of the attribute.
					value = fta.getAttValueList().getValue((String) valueObj,
							com.lcs.wc.client.ClientContext.getContext().getLocale());
					String returnValue = "";
					if (value!=null){
						value = value.replace("|~*~|", ",");
						System.out.println(":::::::::::::::inside if>>>>3>"+ value);
						String [] needValue = value.split(",");
						for (String pd: needValue){
							System.out.println("::::::::::::::::pd   >>>>>"+ pd);
							String displayValue	=	fta.getDisplayValue(pd);
							String jdeValue = fta.getAttValueList().getValueFromValueStore((String) valueObj, "_CUSTOM_JDECode");
							if(FormatHelper.hasContent(jdeValue)){
								returnJdeValue = returnJdeValue.concat(jdeValue).concat(",");
							}
							returnValue = returnValue.concat(displayValue).concat(",");
							/*ArrayList valuList = new ArrayList();
							 * 
							valuList.add(displayValue+",");
							String [] needValue */
							System.out.println("The Display value is::::::::::::"+displayValue);
							System.out.println("The jde value is::::::::::::"+jdeValue);
						}
					value = returnValue.substring(0,returnValue.length()-1);
					if(FormatHelper.hasContent(returnJdeValue)){
					value1 = returnJdeValue.substring(0,returnJdeValue.length()-1);
					}
					System.out.println(":::value is::::::::::::"+value);
					System.out.println(":::jde value is::::::::::::"+value1);
					if (FormatHelper.hasContent(value1)){
						value=value1;
					}
					}
					//value=MOAHelper.parseOutDelimsLocalized((String) ( valueObj).getValue(valueObj), ", ",sizeDefinitionObj.getFlexType().getAttribute(FEDAS_CODE_ATT_KEY_SIZE_DEFINITION).getAttValueList(),null);
				
			}
		
			
		}
		
		return value;
	}


	/**
	 * @param attValue Double
	 * @param att FlexTypeAttribute
	 * @return
	 */
	public static String getFloat(Double attValue,FlexTypeAttribute att) { // getting float attribute values
		// TODO Auto-generated method stub
		HFLogFileGenerator.configureLog("Outbound");
		String value=null;
		if(attValue!=null)             // attValue should not be null.
		{
			Double d = (Double)attValue; // getting value of the attribute.
			int precision = att.getAttDecimalFigures();
			if(precision != 0){
				String str = "";
				StringBuilder build = new StringBuilder("");
				for(int i=0;i<precision;i++){
					build = build.append("#");
				}
				str = build.toString();
				String s = "#.";
			
//				StringBuffer temp = new StringBuffer();
//				temp.append(s);
//				temp.append(str);
				DecimalFormat twoDForm = new DecimalFormat(s+str);
				value = twoDForm.format(d);
			}else{
				value = String.valueOf(d.intValue());                // type casting double to string.
			}
		}  
		return value;
	}
	/**
	 * @param attValue
	 * @return
	 */
	public static String getDate(Object attValue) { // getting date attribute value
		// TODO Auto-generated method stub
		String value="";
		String format = null;        // defined to store format value.
		if(attValue!=null)             // attValue should not be null.
		{                                                                                                              
			WrappedTimestamp time =(WrappedTimestamp)attValue;                // type casting attValue to WrappedTimestamp                                                                                                
			format = "MM/dd/yyyy";             // setting default date format.
			DateFormat formatter = new SimpleDateFormat(format);                // setting simple date format to the date format. 
			TimeZone timeZone = null;          // defined store time zone value.
			timeZone = TimeZone.getTimeZone("GMT"); // setting default time zone to GMT.
			formatter.setTimeZone(timeZone); // setting time zone to the date format.
			value = formatter.format(time);
		}else{
			value = "";
		}
		return value;
	}
	
	
	//todo>>>>>>>>>>>>>>>>>rajat 
	public static String getURL(Object valueObj) { // getting url attribute value
		// TODO Auto-generated method stub
		String value="";
		//String format = null;        // defined to store format value.
		if(valueObj!=null)           // valueObj should not be null.
		{                                                                                                              
			value=valueObj.toString();
		}
		return value;
	}

	
}
