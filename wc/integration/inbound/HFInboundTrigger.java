package com.hf.wc.integration.inbound;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;
import org.apache.log4j.Logger;
import com.hf.wc.integration.util.*;
import com.hf.wc.integration.util.HFSendEmail;
import com.lcs.wc.flextype.*;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.util.*;
import wt.method.RemoteMethodServer;
import wt.queue.ProcessingQueue;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.util.*;
import wt.vc.wip.WorkInProgressHelper;



/**
 * HFSalsifyInboundTrigger.
 * This is the java class which is getting called on the execution of batch job.
 * @version 1.0
 *
 */
public final class HFInboundTrigger implements wt.method.RemoteAccess{

	/**
	 * Private Constructor.
	 */
	private HFInboundTrigger(){
	}

	/**
	 * logger object.
	 */
	static final Logger logger = Logger.getLogger(HFInboundTrigger.class);	

	/**
	 * Final String for DERIVEDSTRING
	 */
	/*private static final String DERIVEDSTRING = "derivedString";*/

	/**
	 *  Final String for DRIVEN
	 */
	/*private static final String DRIVEN = "driven";*/

	/**
	 *  Final String for TEXT
	 */
	private static final String TEXT = "text";

	/**
	 *  Final String for BOOLEAN
	 */
	//private static final String BOOLEAN = "boolean";
	/**
	 *  Final String for TEXTAREA
	 */
	//private static final String TEXTAREA = "textArea";
	/**
	 *  Final String for FLOAT
	 */
	private static final String FLOAT = "float";

	/**
	 * Final String for CHOICE
	 */
	private static final String CHOICE = "choice";
	/**
	 *  Final String for CURRENCY
	 */
	private static final String CURRENCY = "currency";
	/**
	 *  Final String for INTEGER
	 */
	private static final String INTEGER = "integer";

	/**
	 *  Final String for URL
	 */
	private static final String URL = "url";

	private static String interfaceType = "";
	private static String colorwayAttrib = "";
	private static String productAttrib = "";
	private static String tablename="";
	private static String USERNAME =LCSProperties.get("com.hf.wc.report.HFFinishReport.user");
	private static String PASSWORD =LCSProperties.get("com.hf.wc.report.HFFinishReport.password");
	//private static String colorwayAttribKey = "";
	//private static String productAttribKey = "";
	private final static String SALSIFY_INBOUND_INFO_LOG = "SalsifyInboundInfoLog";
	private final static String YEAR = "year";
	
	
	/**
	 * main method for triggering the integration.
	 * @param args for args
	 * @throws InvocationTargetException for InvocationTargetException
	 * @throws IOException for IOException
	 */
	public static void main(String[] args) throws InvocationTargetException, IOException {

		HFLogFileGenerator.configureLog(SALSIFY_INBOUND_INFO_LOG);

		logger.info("Batch program started - Salsify INBOUND Integration");

		WTProperties wtProperties = WTProperties.getLocalProperties();
		String noofMethodServerPh = wtProperties.getProperty("wt.manager.monitor.start.MethodServer");
		String backgroundServerPh = wtProperties.getProperty("wt.rmi.server.hostname");
		logger.info("main method batch process ------- HFSalsifyInboundTrigger ----- ....backgroundServer: "+backgroundServerPh);
		RemoteMethodServer backgroundMethodServer = null;
		//Using background method server if available
		if (!"1".equals(noofMethodServerPh) && FormatHelper.hasContent(backgroundServerPh)) {
			URL url = new URL("https://" + backgroundServerPh + "/Windchill/rfa");
			backgroundMethodServer = RemoteMethodServer.getInstance(url,"BackgroundMethodServer");
		} else {
			backgroundMethodServer = RemoteMethodServer.getDefault();
		}
		Class[] argObjects = {String.class};
		final Object[] argValues = {args[0]};

		/** Invoking rmsObj **/

		logger.info("Executing main");
		backgroundMethodServer.setUserName(USERNAME);
		backgroundMethodServer.setPassword(PASSWORD);
		backgroundMethodServer.invoke("execute", HFInboundTrigger.class.getName(), null, argObjects, argValues);
		logger.info(">>>>>>>>>>>>>>Batch program ended - INBOUND Integration");
	}

	/**
	 * execute.
	 * @throws WTException for WTException.
	 * @throws WTPropertyVetoException for WTPropertyVetoException
	 * @throws SQLException for SQLException
	 */
	public static void execute(String args) throws WTException, WTPropertyVetoException, SQLException{

		ProcessingQueue processQueue = null;
		ResultSet resultset = null;
		interfaceType=args;
		//HFSalsifyInboundDataReader inboundDataReader = new HFSalsifyInboundDataReader();
		//if(args.equalsIgnoreCase("Salsify_Inbound")){
		if(("Salsify_Inbound").equalsIgnoreCase(args)){	
			HFLogFileGenerator.configureLog(SALSIFY_INBOUND_INFO_LOG);
			logger.info("Start - Executing from Method Server - Salsify Inbound Integration");
			// Processing Queue for Salsify Inbound Integration
			processQueue = HFInboundIntegrationHelper.getProcessQueue(HFIntegrationConstants.SALSIFY_INBOUND_QUEUE_NAME);

			logger.info(":::::::::::::::::::::::before:resultset::"+ resultset);
			//Pulling attribute values from HFSalsifyInboundDataReader
			resultset = HFInboundDataReader.getColorwayDataFromTable(HFIntegrationConstants.SALSIFY_INBOUND_TABLENAME);
			logger.info("::::::::::after::::::::::::::resultset::"+ resultset);
			//Getting Table Name
			tablename=HFIntegrationConstants.SALSIFY_INBOUND_TABLENAME;
			//Getting Colorway Attributes
			colorwayAttrib = HFIntegrationConstants.SALSIFY_INBOUND_COLORWAY_ATTRIBUTES;
			logger.info("::::::::::::::::::::::::colorwayAttrib::"+ colorwayAttrib);
			//Getting Product Attributes
			productAttrib = HFIntegrationConstants.SALSIFY_INBOUND_PRODUCT_ATTRIBUTES;
			logger.info("::::::::::::::::::::::::productAttrib::"+ productAttrib);
			salsifyAndJDECostInboundmapping(resultset,processQueue);  
		}

		if(("JDE_Sales_Inbound").equalsIgnoreCase(args)){
			HFLogFileGenerator.configureLog(SALSIFY_INBOUND_INFO_LOG);
			logger.info("Start - Executing from Method Server - JDE Sales Inbound Integration");
			// Processing Queue for JDE Sales Inbound Integration
			processQueue = HFInboundIntegrationHelper.getProcessQueue(HFIntegrationConstants.JDESALES_INBOUND_QUEUE_NAME);
			Collection itemNumbersList = HFInboundDataReader.getColorwayDataFromJDESalesInboundTable(HFIntegrationConstants.JDESALES_INBOUND_TABLENAME);
			Iterator itr =	itemNumbersList.iterator();
			while (itr.hasNext()){
				String itemNumber = (String) itr.next();
				resultset = HFInboundDataReader.fetchResultSet(itemNumber);
				//logger.info("::::::::::::::::::::::resultset::"+ resultset);
				//Getting Table Name for JDE_Sales_Inbound
				tablename=HFIntegrationConstants.JDESALES_INBOUND_TABLENAME;
				//Getting colorway attributes for JDE_Sales_Inbound
				//colorwayAttrib = HFIntegrationConstants.JDESALES_INBOUND_COLORWAY_ATTRIBUTES_YEAR1;
				//Getting Product Attributes for JDE_Sales_Inbound
				//productAttrib = HFIntegrationConstants.JDESALES_INBOUND_PRODUCT_ATTRIBUTES_YEAR1;
				logger.info("processQueue>>>>"+processQueue.getName());
				jdeSalesInboundMapping(resultset,processQueue);  
			}
		}

		if(("JDE_Cost_Inbound").equalsIgnoreCase(args)){
			HFLogFileGenerator.configureLog(SALSIFY_INBOUND_INFO_LOG);
			logger.info("Start - Executing from Method Server - JDE Cost Inbound Integration");
			// Processing Queue for JDE Cost Inbound Integration
			processQueue = HFInboundIntegrationHelper.getProcessQueue(HFIntegrationConstants.JDECOST_INBOUND_QUEUE_NAME);

			resultset = HFInboundDataReader.getColorwayDataFromTable(HFIntegrationConstants.JDECOST_INBOUND_TABLENAME);
			logger.info(":::::::::::::::::::::::after:resultset::"+ resultset);
			//Getting Table Name for JDE_Cost_Inbound
			tablename=HFIntegrationConstants.JDECOST_INBOUND_TABLENAME;
			//Getting colorway attributes for JDE_Cost_Inbound
			colorwayAttrib = HFIntegrationConstants.JDECOST_INBOUND_COLORWAY_ATTRIBUTES;
			////Getting product attributes for JDE_Cost_Inbound
			productAttrib = HFIntegrationConstants.JDECOST_INBOUND_PRODUCT_ATTRIBUTES;
			salsifyAndJDECostInboundmapping(resultset,processQueue);
			logger.info("Outside method salsifyAndJDECostInboundmapping>>>>>>>>>>>>>>>>>::::::::::::");
		}

		logger.info("Processing Queue Name : " + processQueue);
		logger.info(":::::::::::::::::::::::::::resultset "+resultset);
		SessionServerHelper.manager.setAccessEnforced(Boolean.TRUE);
		logger.info("End - Executing from Method Server - Inbound Integration");
		//resultset.close();
	}

	/**
	 * @param resultCol
	 * @param processQueue
	 * @throws SQLException SQLException
	 * @throws WTException WTException
	 */ 
	public static void salsifyAndJDECostInboundmapping(ResultSet resultCol,ProcessingQueue processQueue) throws SQLException, WTException {
		logger.info("Inside method salsifyAndJDECostInboundmapping>>>>>>>>>>>>>>>>>::::::::::::");
		logger.info("Inside method salsifyAndJDECostInboundmapping>>>>>>>>>>>>>>>>>>>>>>resultCol:::::"+resultCol);
		logger.info("resultCol.1()>>>>>>>>>>>>"+resultCol.getMetaData().getColumnCount());
		while(resultCol.next()){
			logger.info(">>>>>>>>>>>>>>>>>>salsifyAndJDECostInboundmapping::::::::::::::Inside data reader while loop");
			wt.org.WTPrincipal wtprincipal = SessionHelper.manager.getPrincipal();

			String[] skuAttKeys = colorwayAttrib.split(",");
			String[] prodAttKeys = productAttrib.split(",");
			logger.info(skuAttKeys[0]);
			Map queryMap = new HashMap();

			for(String s:skuAttKeys){
				String[] key =  s.split("~");
				//logger.info("::::::::::::::::::::key[1]"+ key);
				queryMap.put(key[1],resultCol.getString(key[0]));
				//logger.info("::::::::::::::::::::key[1]"+ key[1]);

			}
			if(prodAttKeys.length>1){
			for(String pd:prodAttKeys){
				String[] key = pd.split("~");
				queryMap.put(key[1],resultCol.getString(key[0]));

			}
			}
			Class[] argTypes = { Map.class ,String.class,String.class};
			Object[] argValues = { queryMap ,interfaceType,tablename};
			// Add new entry in the process queue Extraction
			
			logger.info("Adding map to Queue >>>>>>>>>>>>>>>>>>>:::::::::::::"+ queryMap);
			logger.info("interfaceType>>>>>>>>>>>>>>>:::::::::::::"+ interfaceType);
			logger.info("tablename>>>>>>>>>>>>>>>>:::::::::::::"+ tablename);
			processQueue.addEntry(wtprincipal,"processColorwayData",HFInboundTrigger.class.getName(), argTypes, argValues);
			logger.info("After tablename>>>>>>>>>>>>>>>>:::::::::::::"+ tablename);
		}
	}
	/**
	 * @param resultCol
	 * @param processQueue
	 * @throws SQLException SQLException.
	 * @throws WTException WTException.
	 */
	public static void jdeSalesInboundMapping(ResultSet resultCol,ProcessingQueue processQueue) throws SQLException, WTException {
		
		//Reading data from resultset
		
		while(resultCol.next()){
			
			wt.org.WTPrincipal wtprincipal = SessionHelper.manager.getPrincipal();
			logger.info("Inside data reader while loop");
			String[] skuAttKeysYear1 = HFIntegrationConstants.JDESALES_INBOUND_COLORWAY_ATTRIBUTES_YEAR1.split(",");
			String[] skuAttKeysYear2 = HFIntegrationConstants.JDESALES_INBOUND_COLORWAY_ATTRIBUTES_YEAR2.split(",");
			String[] skuAttKeysYear3 = HFIntegrationConstants.JDESALES_INBOUND_COLORWAY_ATTRIBUTES_YEAR3.split(",");
			String year = resultCol.getString("GL_DATE_FISCAL_YEAR");
			//will incorporate product attributes later on requirements
			String[] prodAttKeys = productAttrib.split(",");

			Map queryMap = new HashMap();
			
			if(year.equalsIgnoreCase(HFIntegrationConstants.JDE_SALES_INBOUND_YEAR1)){
				queryMap.put(YEAR, year);
				for(String s:skuAttKeysYear1){
					String[] key =  s.split("~");
					//logger.info(">>>key[1]"+ key);
					queryMap.put(key[1],resultCol.getString(key[0]));
					
					//logger.info(">>>>key[1]"+ key[1]);
				}
				
			}

			if(year.equalsIgnoreCase(HFIntegrationConstants.JDE_SALES_INBOUND_YEAR2)){
				queryMap.put(YEAR, year);
				for(String s:skuAttKeysYear2){
					String[] key =  s.split("~");
					//logger.info(">>>>>key[1]"+ key);
					queryMap.put(key[1],resultCol.getString(key[0]));
					
					//logger.info(">>>>>>key[1]"+ key[1]);
				}
			}

			if(year.equalsIgnoreCase(HFIntegrationConstants.JDE_SALES_INBOUND_YEAR3)){
				queryMap.put(YEAR, year);
				for(String s:skuAttKeysYear3){
					String[] key =  s.split("~");
					//logger.info("::::::::::::::::::::key[1]"+ key);
					queryMap.put(key[1],resultCol.getString(key[0]));
					
					//logger.info(">>>>>>>key[1]"+ key[1]);
				}
			}

			/*logger.info("prodAttKeys>>>>>>>>>>>>>>"+productAttrib.isEmpty());
			if (!productAttrib.isEmpty()) {
				logger.info("INSIDE IF");
				for(String pd:prodAttKeys){
					String[] key = pd.split("~");
					queryMap.put(key[1],resultCol.getString(key[0]));
					productAttribKey=productAttribKey+key[0]+",";
				}
			}
*/
			Class[] argTypes = { Map.class,String.class,String.class};
			Object[] argValues = { queryMap ,interfaceType,tablename};
			processQueue.addEntry(wtprincipal,"processColorwayData",HFInboundTrigger.class.getName(), argTypes, argValues);
		}

	}

	/**
	 * @param colorwayData
	 * @param timestampStr
	 * @throws WTException
	 * @throws WTPropertyVetoException
	 * @throws SQLException
	 * @throws ParseException
	 */
	public static void processColorwayData(Map colorwayData,String interfaceType,String tablename) throws WTException, WTPropertyVetoException, SQLException{
		HFLogFileGenerator.configureLog(SALSIFY_INBOUND_INFO_LOG);
		//Accessing Colorway attributes 
		boolean procesedstate = true;
		
		logger.info("Start ------ HFSalsifyInboundTrigger class ------- processColorwayData method --------");
		logger.info("colorwayData========>"+colorwayData);
		String itemNumber=(String)colorwayData.get(HFIntegrationConstants.COLORWAY_ITEM_NUMBER);	
		logger.info(":::::::::::::::: itemNumber:::"+itemNumber);
		if(FormatHelper.hasContent(itemNumber)){
		logger.info(":::::::::::::::: itemNumber:::"+itemNumber.length());
		}
		LCSSKU colorwayobj =new LCSSKU();
		String colorwayID ="";
		try {
			String[] skuAttKeys;
			
			if(itemNumber.length()==5)
			{
			colorwayID = HFInboundIntegrationHelper.getColorwayBasedOnID(itemNumber,HFIntegrationConstants.COLORWAY_ITEM_NUMBER);
			}
			logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>Colorway id"+colorwayID);
			//Get Colorway Object based on Colorway Id
			if(FormatHelper.hasContent(colorwayID))
			{
			 colorwayobj = (LCSSKU) LCSQuery.findObjectById("VR:com.lcs.wc.product.LCSSKU:"+colorwayID);
			//Checkout Colorway object
			logger.info("before checking out>>>>>>>>>>>>>>>>>");
			logger.info("Check for check out:"+WorkInProgressHelper.isCheckedOut(colorwayobj));
			if(!VersionHelper.isCheckedOut(colorwayobj)){
				
			colorwayobj=(LCSSKU) VersionHelper.checkout(colorwayobj);
			logger.info("Check out Check Version Helper:"+VersionHelper.isCheckedOut(colorwayobj));
			}
			logger.info("colorwayAttrib>>>>>>>>>>>>>>>>>"+colorwayobj.getName());
			if(("JDE_Sales_Inbound").equalsIgnoreCase(interfaceType)){
				ifJDESalesProcessColorwayData(colorwayData,colorwayobj);
			}
			else{
				if(("Salsify_Inbound").equalsIgnoreCase(interfaceType)){
				colorwayAttrib = HFIntegrationConstants.SALSIFY_INBOUND_COLORWAY_ATTRIBUTES;
				productAttrib =HFIntegrationConstants.SALSIFY_INBOUND_PRODUCT_ATTRIBUTES;
				}
				else if(("JDE_Cost_Inbound").equalsIgnoreCase(interfaceType)){
					colorwayAttrib = HFIntegrationConstants.JDECOST_INBOUND_COLORWAY_ATTRIBUTES;
				}
				logger.info(":::::::::::::::::::::::22222:colorwayAttrib::"+ colorwayAttrib);
				skuAttKeys=colorwayAttrib.split(",");
				for(String s:skuAttKeys){
					String[] key =  s.split("~");
					logger.info(">>>>>>>>>String s:skuAttKeys"+key[1]+">>>>>>>>>>>>>>>>>>>>>>>>Value" +colorwayData.get(key[1]));

					if(!(HFIntegrationConstants.COLORWAY_ITEM_NUMBER).equalsIgnoreCase(key[1])){
						String attkeyData=colorwayData.get(key[1]).toString();
						HFInboundTrigger.setColorwayObject(colorwayobj,key[1],attkeyData);
					}
				}
			}

			logger.info("before persist>>>>>>>>>>>>>>>>>:::::::::::::::");
			
			//Checkin Colorway Object
			
			/*logger.info("::::Housing Finish::::::::::::"+colorwayobj.getValue("hfHousingFinish"));
			logger.info("::::Level 5 Finish Category:::"+colorwayobj.getValue("hfLevel5Category"));
			logger.info("::::Blade Side1 Finish::::::::"+colorwayobj.getValue("hfBladeSide1Finish"));*/
			
			logger.info("Check gfor check out:"+WorkInProgressHelper.isCheckedOut(colorwayobj));
			colorwayobj=(LCSSKU) VersionHelper.checkin(colorwayobj);
			logger.info("::::after checking in:::::");
			extractProductAttrib(colorwayData, colorwayobj);
			//Setting Processed state as Yes
			if(procesedstate){
				HFInboundDataReader.setStateAsProcessed(itemNumber,tablename);
			}
		}
			else{
				HFInboundIntegrationHelper.sendEmailIfColorwayNotPresent(itemNumber);
			}
		}catch (WTException e) {
			logger.info(e.getMessage());
			logger.error("WTException Occured while processing Colorway Integration. Sequence Number is "+itemNumber);
			logger.error("::::::before undo checkout::::");			
			colorwayobj = (LCSSKU)  VersionHelper.undoCheckout(colorwayobj);
			logger.error("::::::::Previous version of the object has been Checked in:::::::"+colorwayobj.getName());
			String mailBody = itemNumber+" WTPropertyVetoException Occured while processing Colorway Integration. Sequence Number is";
			String mailSubject = "Inbound failed for Item Number"+ itemNumber;
			//Mail wiill sent to given mail id if error occurs
			HFSendEmail.sendEmail(mailBody, mailSubject, HFIntegrationConstants.TO_EMAIL_ADDRESS);
			

		} catch (WTPropertyVetoException e) {
			logger.error("WTPropertyVetoException Occured while processing Colorway Integration. Sequence Number is "+itemNumber);
			colorwayobj = (LCSSKU)  VersionHelper.undoCheckout(colorwayobj);
			logger.error("::::::1::Previous version of the object has been Checked in::::1:::"+colorwayobj.getName());
			logger.error(e.getStackTrace());
			String mailBody = e+" WTPropertyVetoException Occured while processing Colorway Integration. Sequence Number is ";
			String mailSubject = "WTPropertyVetoException Occured during inbound"+ e;
			//Mail wiill sent to given mail id if error occurs
			HFSendEmail.sendEmail(mailBody, mailSubject, HFIntegrationConstants.TO_EMAIL_ADDRESS);

		}

	}
	
	
	public static void ifJDESalesProcessColorwayData(Map colorwayData, LCSSKU colorwayobj) throws WTPropertyVetoException, WTException{
		String[] list = new String[100]; 
		//List<String> list = new ArrayList<String>();
		String year = colorwayData.get(YEAR).toString();
		logger.info("inside ifJDESalesProcessColorwayData");
		if(year.equalsIgnoreCase(HFIntegrationConstants.JDE_SALES_INBOUND_YEAR1)){
			list=HFIntegrationConstants.SALES_ATTKEYS_YEAR1.split(",");
		}

		if(year.equalsIgnoreCase(HFIntegrationConstants.JDE_SALES_INBOUND_YEAR2)){
			list=HFIntegrationConstants.SALES_ATTKEYS_YEAR2.split(",");
			logger.info(":Year Keys::"+ list);
		}

		if(year.equalsIgnoreCase(HFIntegrationConstants.JDE_SALES_INBOUND_YEAR3)){
			 list=HFIntegrationConstants.SALES_ATTKEYS_YEAR3.split(",");
		}
		logger.info("::::::::::list is::"+ list);
		for(String s:list){
			logger.info(">>>>>>>>>String s:skuAttKeys"+s+">>>>>>>>>>>>>>>>>>>>>>>>Value" +colorwayData.get(s));

			if(!(HFIntegrationConstants.COLORWAY_ITEM_NUMBER).equalsIgnoreCase(s)){
				String attkeyData=colorwayData.get(s).toString();
				HFInboundTrigger.setColorwayObject(colorwayobj,s,attkeyData);
			}
		}

	}
	
	

	private static void extractProductAttrib(Map colorwayData, LCSSKU colorwayobj)
			throws WTException, WTPropertyVetoException {
		if(!productAttrib.isEmpty()){
			LCSProduct prodObj = (LCSProduct)colorwayobj.getProduct();
			//Checkout product object
			prodObj = (LCSProduct) VersionHelper.checkout(prodObj);
			String[] prodAttKeys = productAttrib.split(",");
			for(String pd:prodAttKeys){
				String[] keyProd = pd.split("~");
				if(!(HFIntegrationConstants.COLORWAY_ITEM_NUMBER).equalsIgnoreCase(keyProd[1])){
					String attkeyData=(String)colorwayData.get(keyProd[1]);
					HFInboundTrigger.setProductObject(prodObj,keyProd[1],attkeyData);
					
				}
			}
			//checkin Product Object
			VersionHelper.checkin(prodObj);
		}
	}


	/**
	 * @param colorwayobj
	 * @param attkey
	 * @param attkeyData
	 * @throws WTException WTException.
	 * @throws WTPropertyVetoException WTPropertyVetoException.
	 */
	public static void setColorwayObject(LCSSKU colorwayobj,String attkey,String attkeyData) throws WTPropertyVetoException, WTException{
		logger.info(":::::::::::::inside the method setColorwayObject::::::::::::::::::");
		//Setting Colorway Object Values acc to data types
		FlexType ftp = colorwayobj.getFlexType();
		FlexTypeAttribute flexTA = ftp.getAttribute(attkey);
		String colorwayAttType = flexTA.getAttVariableType();

		logger.info(">>>>>>>>ftp::::::::::::::"+ftp);
		logger.info(">>>>>>>>flexTA::::::::::::::"+flexTA);
		logger.info(">>>>>>>>colorwayAttType::::::::::::::"+colorwayAttType);
		

		// Condition to check if att is of type Float or Currency
		if(FLOAT.equals(colorwayAttType)||CURRENCY.equals(colorwayAttType))  // if the attribute type is float.
		{
			Double attkeyDatadbl = Double.parseDouble(attkeyData);
			logger.info(":::setColorwayObject:::::::setting float/currency attributes:::::::::::::::");
			colorwayobj.setValue(attkey, attkeyDatadbl);
		}
		// Condition to check if att is of type Text or Single List
		else if (TEXT.equals(colorwayAttType)||CHOICE.equals(colorwayAttType)) {

			String attkeyDataStr = (String)attkeyData;
			logger.info("::setColorwayObject::::::::text/choice attributes:::::::::::::::");
			colorwayobj.setValue(attkey, attkeyDataStr);
		}
		// Condition to check if att is of type hyperlink
		else if (URL.equals(colorwayAttType)) {
			colorwayobj.setValue(attkey, attkeyData);
			
		}
			// Condition to check if att is of type Integer
			else if (INTEGER.equals(colorwayAttType))  
			{
				logger.info("::setColorwayObject:::::::Integer attributes:::::::::::::::");
				Long attkeyDataLong = Long.parseLong(attkeyData);
				colorwayobj.setValue(attkey, attkeyDataLong);

		}
	}

	public static void setProductObject(LCSProduct prodObj,String attkey,String attkeyData) throws WTException, WTPropertyVetoException{
		
		logger.info(":::::::::::::inside the method setProductObject::::::::::::::::::");
		logger.info(":::::::::::::prodObj>>>>>>>>>>>>>>>" +prodObj);
		//Setting Product Object Values acc to data types
		FlexType ftp = prodObj.getFlexType();
		logger.info(":::::::::::::ftp>>>>>>>>>>>>" +ftp);
		FlexTypeAttribute flexTA = ftp.getAttribute(attkey);
		logger.info(":::::::::::::proftpdObj>>>>>>>>>>" +flexTA);
		String productAttType = flexTA.getAttVariableType();
		logger.info(":::::::::::::productAttType>>>>>>>>>>>>" +productAttType);

		// Condition to check if att is of type Float or Currency
		if(FLOAT.equals(productAttType)||CURRENCY.equals(productAttType))  // if the attribute type is float.
		{
			Double attkeyDatadbl = Double.parseDouble(attkeyData);
			logger.info(":::setProductObject:::::::setting float/currency attributes:::::::::::::::");
			prodObj.setValue(attkey, attkeyDatadbl);
		}
		// Condition to check if att is of type Text or Single List
		else if (TEXT.equals(productAttType)||CHOICE.equals(productAttType)) {

			String attkeyDataStr = (String)attkeyData;
			logger.info("::setProductObject::::::::text/choice attributes:::::::::::::::");
			logger.info("::setProductObject::::::::attkey::::::::::::::"+attkey);
			logger.info("::setProductObject:::::::attkeyDataStr:::::::::::::"+attkeyDataStr);
			prodObj.setValue(attkey, attkeyDataStr);
			//LCSLogic.persist(prodObj,true);
		}
		// Condition to check if att is of type hyperlink
			else if (URL.equals(productAttType)) {
			logger.info("::::product::::url attribute::::::::::::::");
			prodObj.setValue(attkey, attkeyData);
			
		}

	}
}