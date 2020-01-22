package com.hf.wc.product;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.log4j.Logger;
import com.lcs.wc.color.LCSColor;
import com.lcs.wc.db.*;
import com.lcs.wc.flextype.*;
import com.lcs.wc.foundation.LCSLogic;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.*;
import com.lcs.wc.season.*;
import com.lcs.wc.util.LCSProperties;
import wt.part.WTPart;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
/**
 * @author ITCINFOTECH 
 * This class is called by HFServicePartCreation for SERVICE PART CREATION.
 */
public final class HFServicePartCreationHelper {
	
	/**
	 * Variable to store new finish code att key.
	 */
	private final static String FINISHCODEKEY = LCSProperties.get("com.hf.wc.product.HFServicePartCreation.finishcodekey");
	/**
	 * Variable to store sky obj key.
	 */
	private final static String SKUKEY = LCSProperties.get("com.hf.wc.product.HFServicePartCreation.skuKey");
	/**
	 * Variable to store sky obj key.
	 */
	private final static String COLORKEY = LCSProperties.get("com.hf.wc.product.HFServicePartCreation.colorKey");
	/**
	 * Variable to store service Product Key.
	 */
	private final static String FAMILYKEY = LCSProperties.get("com.hf.wc.product.HFServicePartCreation.familyKey");
	/**
	 * Variable to store  hierarchy.
	 */
	private final static String HIERARCHY = LCSProperties.get("com.hf.wc.product.HFServicePartCreation.hierarchy");
	/**
	 * Variable to store  stagingDB KEY.
	 */
	private final static String STAGINGDBKEY = LCSProperties.get("com.hf.wc.product.HFServicePartCreation.stagingDBKey");
	/**
	 * Variable to store JDEDescription att key of Service Part Model.
	 */
	private final static String JDEDESCRIPTION = LCSProperties.get("com.hf.wc.product.HFSBOMCreation.jdeDescriptionKey");
	/**
	 * Constructor object.
	 */
	private HFServicePartCreationHelper() 
	{
		
	}
	/**
	 * Logger object.
	 */
	private static Logger log = Logger.getLogger(HFServicePartCreationHelper.class.getName());
	/**
	 * This method is invoked to to seth the season object sor SP. 
	 * @param String seasonNameKey, String seasonName.
	 * @throws WTException 
	 * @throws WTPropertyVetoException 
	 */
	public static LCSSeason fetchSeasobObject(String seasonNameKey, String seasonName) throws WTException {
		LCSSeason season = null;
		String seasonColumn = FlexTypeCache.getFlexTypeRoot("Season").getAttribute(seasonNameKey).getColumnName();
		PreparedQueryStatement statement = new PreparedQueryStatement();
		statement.appendFromTable("LCSSeason");
		statement.appendSelectColumn(new QueryColumn("LCSSeason", "branchIditerationInfo"));
		statement.appendCriteria(
				new Criteria(new QueryColumn("LCSSeason", seasonColumn), seasonName.trim(), Criteria.EQUALS));
		SearchResults results = LCSQuery.runDirectQuery(statement);
		if (results.getResultsFound() > 0) {
			FlexObject flexObject = (FlexObject) results.getResults().elementAt(0);
			season = (LCSSeason) LCSQuery.findObjectById("VR:com.lcs.wc.season.LCSSeason:" + flexObject.getData("LCSSeason.branchIditerationInfo"));
		}
		return season;
	}
	/**
	 * This method is invoked to create serviceable Parts and Models. 
	 * @param serviceablePart WTPart, finish String, quantity Integer.
	 * @throws WTException 
	 * @throws WTPropertyVetoException 
	 * @throws ParseException 
	 * @throws InvocationTargetException 
	 * @throws RemoteException 
	  */
	public static void createServicePart(WTPart serviceablePart, String finish, int quantity, LCSSeason season) throws WTException, WTPropertyVetoException, RemoteException, InvocationTargetException, ParseException  {

		log.info(serviceablePart.getNumber());
		String localFinish	=	finish;
		LCSProductQuery pquery = new LCSProductQuery();
		LCSSeasonClientModel seasonModel = new LCSSeasonClientModel();
		seasonModel.load(season.toString());
		//LCSSKUClientModel skuModelObj = new LCSSKUClientModel();
		LCSColor color = null;
		//Fetching the hierarchy for prodcut and Model creation
		FlexType flexType = FlexTypeCache.getFlexTypeFromPath(HIERARCHY);
		//Filtering based on the length of the ServicePartNumber
		//Case 1: If the length is 8 the  finish assigned to the service part is considered for modelName for Model Creation.
		String jdeDesription = HFSBOMLinkedEpmDocument.fetchJdeDescription(serviceablePart);
		log.info("jdeDesription:"+jdeDesription);
		String[] splitByDot = serviceablePart.getNumber().trim().split("\\.");
		String productName = splitByDot[0];
		if (productName.length() == 8) {
			productName = productName.trim();
			String[] splitByHyphen = productName.split("\\-");
			String firstName = splitByHyphen[0];
			String secondName = splitByHyphen[1];
			productName = firstName.trim().concat(secondName.trim());
		}
		//Case 2: If the length is 12 the last 3 digit is uses as finish irrespective to finish assigned.
		if (productName.length() == 12) {
			productName = productName.trim();
			String[] splitByHyphen = productName.split("\\-");
			String firstName = splitByHyphen[0];
			String secondName = splitByHyphen[1];
			productName = firstName.trim().concat(secondName.trim());
			localFinish = splitByHyphen[2];
		}
		// Quantity is fetched from from WTPartUsageLink
		String quantityValue = Integer.toString(quantity);
		String partNumber = productName;
		String partFinish = partNumber + "," + localFinish;
		// rollUpQuantity method invoked to roll up the quantity if servicePart has same finish under one Fan Model.
		HFServicePartCreation.rollUpQunatity(partFinish, quantityValue, quantity);
		// Product Object corresponding to serviceablePart
		//System.out.println("%%%%%%%%%%%%%%%%%%%%%%" + productName + "%%%%%%%%%%%%%%%%%%%%%%");
		LCSProduct product = pquery.findProductByNameType(productName,flexType);
		// Colorway corresponding to given Finish
		LCSSKU colorwayObj = null;
		// Fetch Color object for given finish code
		PreparedQueryStatement statement = new PreparedQueryStatement();
		statement.appendFromTable("LCSColor");
		statement.appendSelectColumn(new QueryColumn("LCSColor", "idA2A2"));
		log.info("FINISHCODEKEY:"+FINISHCODEKEY);
		String colorColumn	=	FlexTypeCache.getFlexTypeRoot("Color").getAttribute(FINISHCODEKEY).getColumnName();
		log.info("colorColumn:"+colorColumn);
		statement.appendCriteria(new Criteria(new QueryColumn("LCSColor",colorColumn ), localFinish.trim(), Criteria.EQUALS));
		//System.out.println(statement);
		SearchResults results = LCSQuery.runDirectQuery(statement);
		if (results.getResultsFound() > 0) {
			FlexObject fObject = (FlexObject) results.getResults().elementAt(0);
			color = (LCSColor) LCSQuery.findObjectById("OR:com.lcs.wc.color.LCSColor:" + fObject.getData("LCSColor.idA2A2"));
		}	
		// Creating new ServiceProduct if it's not present in Flex.
		boolean newProduct = false;
		if (product == null) {
			LCSProductClientModel producttModelObj = new LCSProductClientModel();
			producttModelObj.setFlexType(flexType);
			producttModelObj.setValue(FAMILYKEY, productName);
			producttModelObj.setSeasonMaster(season.getMaster());	
			LCSLogic.deriveFlexTypeValues(producttModelObj);
			//Saving the Service Product.
			producttModelObj.save();
			product = producttModelObj.getBusinessObject();
			newProduct = true;
			log.info("producttModelObj obj:" +product.getName());
			Collection productMasterIds = new ArrayList();
			productMasterIds.add(product.getMaster().toString());
			seasonModel.addProducts(productMasterIds);
		} 
		//Creating 10 digit Model Name. 
		String modelName = product.getName().trim().concat(localFinish.trim());
		log.info("modelName:"+modelName);
		//Creating a tempModelName variable with same name as ModelName returned by api. 
		String tempModelName = modelName + " " + "(" + product.getName().trim() + ")";
		//Check whether the Model already exist for the Service Part 
		if (!newProduct) {
			Collection skuCollection = LCSSKUQuery.findAllSKUs(product, false);
			//System.out.println("&&&&&&&&&&&&&&&&" + skuCollection.size() + "*************");
			Iterator testItr = skuCollection.iterator();
			HashMap<String, LCSSKU> skuName = new HashMap<String, LCSSKU>();
			while (testItr.hasNext()) {
				
				LCSSKU tempcolorwayObj = (LCSSKU) testItr.next();
				//System.out.println("value assinged" + tempcolorwayObj.getName());
				skuName.put(tempcolorwayObj.getName(), tempcolorwayObj);
			}
			if (skuName.keySet().contains(tempModelName)) {
				colorwayObj = (LCSSKU) skuName.get(tempModelName);
			} else {
				colorwayObj = null;
			}
		}
		//Creating a Model With the customCreated Model Name for the Service Part.
		LCSSKUClientModel skuModelObj = new LCSSKUClientModel();
		skuModelObj.setFlexType(flexType);
		//Creating a Model, if  the dosen't exist in flex.
		if (colorwayObj == null) {
			log.info("\n ModelName for creation is :" + modelName);
			skuModelObj.setValue(SKUKEY,modelName.trim());
			skuModelObj.setProductMaster(product.getMaster());
			//Assigning the fetched color for the Model.
			if (color != null) {
				log.info("color:"+color);
				skuModelObj.setValue(COLORKEY, color);
				skuModelObj.setValue(JDEDESCRIPTION,jdeDesription);
				skuModelObj.setValue(STAGINGDBKEY,"NO");
				LCSLogic.deriveFlexTypeValues(skuModelObj);
				//Saving the Model Object.
				skuModelObj.save();
				LCSSKU sku = skuModelObj.getBusinessObject();
				log.info("\n Model Obj Created is :"+sku.getName());
				//Adding SKU to season 
				Collection skuMasterIds	= new ArrayList ();
				skuMasterIds.add(sku.getMaster().toString());
				seasonModel.addSKUs(skuMasterIds);
			}
			else {
				log.info("\n Legal Finish Not Found for WTPart:"+serviceablePart.getNumber().toString().trim());
			}
		} 
		
	}
}
