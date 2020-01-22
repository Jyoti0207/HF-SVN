package com.hf.wc.report;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.*;
import wt.method.RemoteMethodServer;
import wt.org.WTPrincipal;

import com.ibm.icu.util.StringTokenizer;
import com.lcs.wc.db.*;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.flextype.FlexTypeScopeDefinition;
import com.lcs.wc.flextype.FootwearApparelFlexTypeScopeDefinition;
import com.lcs.wc.flextype.scope.ScopeDefinition;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.SeasonProductLocator;
import com.lcs.wc.sourcing.CostSheetFlexTypeScopeDefinition;
import com.lcs.wc.util.VersionHelper;
import wt.fc.*;
import wt.part.*;
import wt.pds.StatementSpec;
import wt.query.*;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.vc.VersionControlHelper;
import wt.vc.struct.StructHelper;
import com.lcs.wc.util.LCSProperties;
import org.apache.log4j.Logger;
/**
 * @author ITCINFOTECH 
 * This class is triggered for Custom Where Used Volume Report.
 */
public final class HFFinishReport implements wt.method.RemoteAccess {
	
	/**
	 * Variable to store excel File Path.
	 */
	public static final String EXCELPATH	=	LCSProperties.get("com.hf.wc.report.HFFinishReport.excelFilePath"); 
	/**
	 * Variable to store Remote Method Server Password.
	 */
	public static final String RMIPWD	=	LCSProperties.get("com.hf.wc.report.HFFinishReport.password");
	/**
	 * Variable to store Remote Method Server User Name.
	 */
	public static final String RMIUSER	=	LCSProperties.get("com.hf.wc.report.HFFinishReport.user");
	/**
	 * Variable to store Finish Key from Windchill.
	 */
	public static final String FINISHKEY	=	LCSProperties.get("com.hf.wc.report.HFFinishReport.finish");
	/**
	 * Variable to store Product Price Key from Windchill.
	 */
	public static final String PRICEKEY	=	LCSProperties.get("com.hf.wc.report.HFFinishReport.price");
	/**
	 * Variable to store ItemNumber Column From Flex.
	 */
	public static final String MODELKEY	=	LCSProperties.get("com.hf.wc.report.HFFinishReport.itemNUmberKey");
	/**
	 * Variable to store Quantity Key from Windchill.
	 */
	public static final String QUANTITYKEY	=	LCSProperties.get("com.hf.wc.report.HFFinishReport.year1Quantity");
	/**
	 * Global Variable to Store the Parent List.
	 */
	public static final List<WTPart> parentList = new ArrayList<WTPart>();
	/**
	 * Global Variable to Store Value of PartNumber and corresponding Finsh.
	 */
	public static final Map<String, String> quantityCollection = new HashMap<String, String>();
	/**
	 * Global Variable to Store the Values to be Displayed in the Excel Report.
	 */
	public static final List<String> displayList = new ArrayList<String>();
	/**
	 * Variable to store default Price.
	 */
	public static final String defaultPrice	=	"No Price";
	/**
	 * Variable to store Invalid WTPart
	 */
	public static final String INVALIDPART	=	"INVALID WTPART";
	/**
	 * Constructor object.
	 */
	private HFFinishReport() {

	}
	/**
	 * Logger object.
	 */
	private static Logger log = Logger.getLogger(HFFinishReport.class.getName());
	/**
	 * This method fetches and sends value to fetchPartReport method
	 * Invokes writeToExcel from HFExcelWriter.
	 * @param partNumber String.
	 * @throws WTException 
	 * @throws IOException 
	 * @throws WTException,PersistenceException.
	 */
	public static void prefetch(String partNumber) throws WTException, IOException {
		
		String adminUser= RMIUSER;
		log.info(adminUser);
		WTPrincipal currentUsr = SessionHelper.manager.getPrincipal();
		SessionHelper.manager.setAuthenticatedPrincipal(adminUser);
		log.info("The Current User is:"+currentUsr.getName());
		// Clearing the display List For Multiple Value entries.
		displayList.clear();
		// Separates the Value by comma and invokes fetchPartReport method.
		StringTokenizer tokenizer = new StringTokenizer(partNumber, ",");
		while (tokenizer.hasMoreTokens()) {
			String wtPartNumber = tokenizer.nextToken();
			QuerySpec qs = new QuerySpec(WTPartMaster.class);
			qs.appendWhere(new SearchCondition(WTPartMaster.class, WTPartMaster.NUMBER, "=", wtPartNumber.trim()),
					new int[] { 0, 1 });
			wt.fc.QueryResult qr = PersistenceHelper.manager.find((StatementSpec) qs);
			if (qr.size() > 0) {
				WTPartMaster partMaster = (WTPartMaster) qr.nextElement();
				WTPart childPart = (WTPart) wt.vc.VersionControlHelper.service.allIterationsOf(partMaster).nextElement();
					try {
						log.info("Number Entered for report Generation:"+childPart.getName());
						log.info("Version:"+childPart.getIterationDisplayIdentifierSansView());
						fetchPartReport(childPart);
					} catch (InvocationTargetException | ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			} 
			else{
				displayList.add(wtPartNumber.trim());
				displayList.add(INVALIDPART);
				displayList.add(INVALIDPART);
				displayList.add(INVALIDPART);
				displayList.add(INVALIDPART);
				displayList.add(INVALIDPART);
				displayList.add(INVALIDPART);
				
			}
			
		}
		//Invokes writeToExcel method for writing values to excel.
		HFFinishReportExcelWriter.writeToExcel(displayList, EXCELPATH);  
		displayList.clear();
        SessionHelper.manager.setAuthenticatedPrincipal(currentUsr.getName());
       
	}
	/**
	 * This method invokes getPartStructure to fetch the given Children under 
	 * different parent and prepare arrayList to  write to ExcelMethod. 
	 * @param childPart WTPart.
	 * @throws WTException 
	 * @throws InvocationTargetException 
	 * @throws PersistenceException.
	 */
	public static void fetchPartReport(WTPart childPart) throws IOException, ParseException, WTException, InvocationTargetException {
		//Enable RMS access.
		RemoteMethodServer rms = RemoteMethodServer.getDefault();
		rms.setUserName(RMIUSER);
		rms.setPassword(RMIPWD);
		//Clearing the collections.
		parentList.clear();
		quantityCollection.clear();
		
		//Local Variable for filtering values.
		ArrayList <String> valueCheck = new ArrayList <String>();
		String volume	=	"0";
		//Invoke getPartStructure for fetching Part Structure.
		getPartStructure(childPart);
		int count = 0;
		//Looping through All the Parents Fetched.
		while (parentList.size() > count) {
			WTPart part = parentList.get(count);
			log.info(parentList.size());
			String parentNumber = part.getNumber();
			//Invoke fetchModelNumber to get the Model Number of the Parent.
			String modelNumber = HFFinishReportRmi.fetchModelNumber(part);
			log.info("modelNumber:"+modelNumber);
			//Fetching the Finish of the Child Under the Parent.
			WTPart parentPart	=	parentList.get(count);
			String assemblyNumber	=	parentPart.getNumber().trim();
			getPartFinish(parentList.get(count),childPart.getNumber());		
			//Looping the Values to be make Report.
			for (Map.Entry storedQuantity : quantityCollection.entrySet()) {
				String splitString = (String) storedQuantity.getValue();
				String[] split = splitString.split("\\,");
				String finalQuantity = split[0];
				String finalPrice = split[1];
				//Filtering Price
				if (!defaultPrice.equalsIgnoreCase(finalPrice.trim())){
				int price	=	Integer.parseInt(finalPrice) * Integer.parseInt(finalQuantity);
				finalPrice	=	Integer.toString(price);}
				else{
					finalPrice	=	"0";
				}
				//Filtering the values to be displayed.
				String tempValueCheck =  childPart.getNumber().trim()+storedQuantity.getKey().toString()+finalQuantity+parentNumber;
				if (!valueCheck.contains(tempValueCheck))
				{
				valueCheck.add(tempValueCheck);
				//Fetching the Volume for the given ModelNumber.
					if (!"No Model".equalsIgnoreCase(modelNumber.trim())) {
						volume = fetchvolume(modelNumber.trim(), finalQuantity, QUANTITYKEY);
						log.info("Volume:"+volume);
					}
				//Adding Values to List for excel Report.
				displayList.add(childPart.getNumber().trim());
				displayList.add(modelNumber);
				displayList.add(assemblyNumber);
				displayList.add(storedQuantity.getKey().toString());
				displayList.add(finalQuantity);
				displayList.add(finalPrice);
				displayList.add(volume);
				}
			}
			//Clearing the collection for next Values.
			quantityCollection.clear();
			count++;
		}
	} 
	/**
	 * This method fetches the the ParentStructure  
	 * @param childPart WTPart.
	 * @throws WTException 
	 * @throws IOException 
	 */
	public static void getPartStructure(WTPart childPart) throws IOException, WTException {
		QueryResult usedQuerResult = null;
		//Accessing RMS connection
		RemoteMethodServer rms = RemoteMethodServer.getDefault();
		rms.setUserName(RMIUSER);
		rms.setPassword(RMIPWD);
		//Fetching Master object 
		
		WTPartMaster partmaster = (WTPartMaster) childPart.getMaster();
		//Navigating to the parent WTPart
		usedQuerResult = StructHelper.service.navigateUsedBy(partmaster);
		
		//Looping through the parents to find the end Item.
		if(usedQuerResult !=null){
			while (usedQuerResult.hasMoreElements() ) {
				
					WTPart parentPart = (WTPart) usedQuerResult.nextElement();
					//invoking the method recursively to find all parents
					getPartStructure(parentPart);
				
			}
		}
		
		//If the fetched object has no more parent, the adding to parentList collection
		if ((usedQuerResult == null) || usedQuerResult.size() < 1) {
			WTPart parent = (WTPart) wt.vc.VersionControlHelper.service.allIterationsOf(partmaster).nextElement();
				if (!parentList.contains(parent)) {
					parentList.add(parent);
				}
		}
	}
	/**
	 * This method fetches the the Finish of the given Part Object.
	 * Also loops through the structure of the parent to find given child.
	 * @param parentPart WTPart.
	 * @param partNumber String.
	 * @throws InvocationTargetException 
	 * @throws RemoteException 
	 * @throws WTException 
	 */
	public static void getPartFinish(WTPart parentPart, String partNumber) throws RemoteException, InvocationTargetException, WTException {
		QueryResult usesQueryResult = null;
		//Accessing the RMS connection
		RemoteMethodServer rms = RemoteMethodServer.getDefault();
		rms.setUserName(RMIUSER);
		rms.setPassword(RMIPWD);
		
			//Fetching all the children of the given parent Object
			usesQueryResult = WTPartHelper.service.getUsesWTPartMasters(parentPart);
			if (usesQueryResult != null && usesQueryResult.size() > 0) {
			//Looping through the values (children of the WTPart fetched)
				while (usesQueryResult.hasMoreElements()) {
					//Fetching PartUsage Link
					WTPartUsageLink ulpart = (WTPartUsageLink) usesQueryResult.nextElement();
					WTPartMaster childPartMaster = (WTPartMaster) ulpart.getUses();
					WTPart childPart = (WTPart) VersionControlHelper.service.allVersionsOf(childPartMaster).nextElement();
					int quantity = 0;
					String finish = null;
					//Fetching quantity from partUsage Link
					quantity = (int) ulpart.getQuantity().getAmount();
					int tempquantity = quantity;
					String priceQuantity = null;
					priceQuantity = Integer.toString(tempquantity);
					//Filtering for the user given Part Number
					if (childPart.getNumber().toString().equalsIgnoreCase(partNumber)) {
						//Fetching Finish of the child Part
						finish = HFFinishReportRmi.findFinish(ulpart,FINISHKEY,PRICEKEY);
						//(String) rms.invoke("fetchingFinish", HFFinishReport.class.getName(), null, new Class[] { WTPartUsageLink.class, String.class, String.class }, new Object[] { ulpart, FINISHKEY, PRICEKEY });
						//Finish is a combination of Finish and Price, splitiing it by comma. 
						String[] str = finish.split("\\,");
						String price = defaultPrice;
						//Assigning price vale only when its not null. 
						if (!defaultPrice.equalsIgnoreCase(str[1].trim())) {
							price = str[1];
						}
						finish = str[0];
						priceQuantity = priceQuantity.concat(",");
						priceQuantity = priceQuantity.concat(price);
						//Adding values to the Collection, to be used for Quantity roll up.
						rollUpQunatity(finish, priceQuantity, quantity);
					}
					//The method is invoked recursively for fetching the children under sub assembly as well.
					getPartFinish(childPart, partNumber);
				}
			}
		
	}
	
	private static void rollUpQunatity(String finish, String priceQuantity, int quantity) {
		
		if (!quantityCollection.containsKey(finish.trim())) {
			quantityCollection.put(finish, priceQuantity);
		} 
		//If the same finish is repeated for the same child the quantity is rolled up.
		else {
			int tempquantity = 0;
			String price = defaultPrice;
			//Looping through the hasMap to find the quantity of the stored parent.
			for (Map.Entry storedQuantity : quantityCollection.entrySet()) {
				if (storedQuantity.getKey().equals(finish)) {
					String tempPriceQuantity = (String) storedQuantity.getValue();
					String[] split = tempPriceQuantity.split("\\,");
					if (!defaultPrice.equalsIgnoreCase(split[1].trim())) {
						price = split[1];
					}
					String quantityCalc = split[0];
					tempquantity = Integer.parseInt(quantityCalc);
					//Adding the stored quantity with fetched one.
					tempquantity = tempquantity + quantity;
					tempPriceQuantity = Integer.toString(tempquantity).concat(",");
					tempPriceQuantity = tempPriceQuantity.concat(price);
					//Adding the rolled up quantity to the collection
					quantityCollection.put(finish, tempPriceQuantity);
				}
			}
		}
		
	}
	/**
	 * This method fetches the Volume for the given SKU Object.
	 * @param epmObj EPMDocument.
	 * @param modelNumber	String.
	 * @param quantity String.
	 * @param quantityKey String.
	 * @throws WTException 
	 */
	
	
	private static String fetchvolume(String modelNumber, String quantity, String quantityKey) throws WTException {

		//LCSSKU skuObj = new LCSSKU();
		log.info("modelNumber is:"+modelNumber.trim()+"for the given SKUOBJECT"+modelNumber.length());
		String volume = null;
		int totalVolume = 0;
		int totalQuantity = Integer.parseInt(quantity);
		//Prepared statement to query the LCSSKU table for the Model Number Column.  
		PreparedQueryStatement statement = new PreparedQueryStatement();
		//appending table.
		statement.appendFromTable("LCSSKU");
		if (modelNumber.length()>0){
		//Appending column
		statement.appendSelectColumn(new QueryColumn("LCSSKU", "branchIditerationInfo"));
		String itemNumberColumn	=	FlexTypeCache.getFlexTypeRoot("Product").getAttribute(MODELKEY).getColumnName(FootwearApparelFlexTypeScopeDefinition.PRODUCT_SKU_LEVEL);
		log.info(itemNumberColumn);
		statement.appendCriteria(new Criteria(new QueryColumn("LCSSKU", itemNumberColumn), modelNumber.trim(), Criteria.EQUALS));
		//executing the query.
		SearchResults results = LCSQuery.runDirectQuery(statement);
		//Fetching the LCSSku object for the given Model Number. 
		if (results.getResultsFound() > 0) {
			//Fetching the FlexObject.
			FlexObject fObject = (FlexObject) results.getResults().elementAt(0);
			LCSSKU skuObj = (LCSSKU) LCSQuery.findObjectById("VR:com.lcs.wc.product.LCSSKU:" + fObject.getData("LCSSKU.branchIditerationInfo"));
			skuObj = SeasonProductLocator.getSKUARev(skuObj);
			//Fetching the Volume for the given SKU object.
			volume = skuObj.getValue(quantityKey).toString();
			log.info("SKU object volume is:"+ volume);
		}
		//Return 0 if the the SKU has no volume or null is being fetched.
		if (volume != null && (volume.length() > 0)) {
			totalVolume = (Integer.parseInt(volume) * totalQuantity);
		} else {
			totalVolume = 0; 
		}
		}
		//returning the volume.
		volume = Integer.toString(totalVolume);
		return volume;
	}
}
