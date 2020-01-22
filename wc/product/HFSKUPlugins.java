package com.hf.wc.product;

import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.lcs.wc.db.*;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSLifecycleManaged;
import com.lcs.wc.foundation.LCSLifecycleManagedLogic;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.part.LCSPartMaster;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.*;
import com.lcs.wc.sourcing.*;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSLog;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.VersionHelper;

import wt.fc.WTObject;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

/**
 * This class is triggered for logic applicable on SKU object.
 * @author ITC Infotech
 * @version 1.1 
 */
public final class HFSKUPlugins {

	/**
	 * logger object.
	 */
	static final Logger logger = Logger.getLogger(HFSKUPlugins.class);

	/**
	 * Variable to store BO hierarchy.
	 */
	private final static String modelNumberFlexType = LCSProperties.get("com.hf.wc.product.HFSKUPlugins.modelNumberFlexType", "Business Object\\hfModelNumber");
	/**
	 * Variable to store Model Number att key.
	 */
	private final static String modelNumberBOAttKey = LCSProperties.get("com.hf.wc.product.HFSKUPlugins.modelNumberBOAttKey", "hfModelNumber");
	/**
	 * Variable to store Is Used? Att key.
	 */
	private final static String isUsedBOAttKey = LCSProperties.get("com.hf.wc.product.HFSKUPlugins.isUsedBOAttKey", "hfIfUsed");
	/**
	 * Variable to store Item Number Att key.
	 */
	private final static String itemNumberAttKey = LCSProperties.get("com.hf.wc.product.HFSKUPlugins.itemNumberAttKey", "hfItemNumberStr");
	/**
	 * Variable to store LifeCycleManaged Att key.
	 */
	private final static String model = LCSProperties.get("com.hf.wc.product.HFSKUPlugins.model", "hfModel");
	/**
	 * Variable to store LifeCycleManaged Att key.
	 */
	private final static String lifeCycleManaged = LCSProperties.get("com.hf.wc.product.HFSKUPlugins.lifeCycleManaged", "LCSLifecycleManaged");
	/**
	 * Variable to store modelName Att key.
	 */
	private final static String modelName = LCSProperties.get("com.hf.wc.product.HFSKUPlugins.modelName", "skuName");



	/**
	 * Constructor object.
	 */
	private HFSKUPlugins(){

	}

	/**
	 * This method sets colorway reference on costsheet
	 * @param obj WTobject.
	 * @throws WTException WTException.
	 * @throws WTPropertyVetoException WTPropertyVetoException.
	 */
	public static void createCostSheetForSKU(WTObject obj)throws WTException, WTPropertyVetoException{

		LCSLog.debug("Colorway Plugin Trigerred");
		LCSLog.debug("Colorway Plugin Trigerred");
		LCSSKU skuObject = (LCSSKU) obj;
		LCSLog.debug("skuObject.isPlaceholder()---------------------------:"+VersionHelper.isFirstVersion(skuObject));
		LCSLog.debug("skuObject.isPlaceholder()---------------------------:"+VersionHelper.isFirstVersion(skuObject));
		if(!skuObject.isPlaceholder() && VersionHelper.isFirstVersion(skuObject)){
			LCSProduct prodObj = (LCSProduct)skuObject.getProduct();
			//FlexType productType = prodObj.getFlexType();

			LCSLog.debug("skuObject---------------------------:"+skuObject);
			LCSLog.debug("skuObject---------------------------:"+skuObject);
			LCSLog.debug("prodObj---------------------------:"+prodObj);
			LCSLog.debug("prodObj---------------------------:"+prodObj);
			LCSLog.debug("MODEL_NAME---------------------------:"+skuObject.getValue(modelName));
			LCSLog.debug("MODEL_NAME---------------------------:"+skuObject.getValue(modelName));
			LCSProductSeasonLink spLink = null;
			LCSSeason season = null;
			//Find seasons used for given product
			Collection<LCSSeasonMaster> seasonCollection = new LCSSeasonQuery().findSeasons(prodObj);
			LCSLog.debug("seasonCollection---------------------------:"+seasonCollection);
			LCSLog.debug("seasonCollection---------------------------:"+seasonCollection);

			Collection sConfiglist = LCSSourcingConfigQuery.getSourcingConfigForProduct(prodObj.getMaster());

			if(seasonCollection!=null && seasonCollection.size() > 0 ){
				for(LCSSeasonMaster seasonMaster: seasonCollection){
					season = (LCSSeason)VersionHelper.latestIterationOf(seasonMaster);
					spLink = (LCSProductSeasonLink) LCSSeasonQuery.findSeasonProductLink(prodObj, season);

					LCSLog.debug("season---------------------------:"+season);
					LCSLog.debug("season---------------------------:"+season);
				}
				Iterator sourcingItr = sConfiglist.iterator();
				double skuMasterId =0;
				int skuMasterInt = 0;
				while(sourcingItr.hasNext())
				{
					//LCSLog.debug("currentSource---------------------------:"+sConfig);
					//Collection<LCSSourcingConfig> sourcingConf = new LCSSourcingConfigQuery().getSourceToSeasonLinks(sConfig);
					//LCSLog.debug("sourcingConf"+sourcingConf);

					LCSSourcingConfig sConfig = (LCSSourcingConfig) sourcingItr.next();
					//LCSCostSheet costSheet = new LCSProductCostSheet();
					/*costSheet.setCostSheetType("PRODUCT");
			FlexType referencedFlexType = productType.getReferencedFlexType("COST_SHEET_TYPE_ID");
			costSheet.setMaster(new LCSCostSheetMaster());
			 double productARevId = 0.0D;
			productARevId = sConfig.getProductARevId();
	        costSheet.setSourcingConfigRevId(Double.parseDouble(FormatHelper.getNumericVersionIdFromObject(sConfig)));
	        ((LCSCostSheetMaster)costSheet.getMaster()).setSourcingConfigRevId(Double.parseDouble(FormatHelper.getNumericVersionIdFromObject(sConfig)));
	        ((LCSCostSheetMaster)costSheet.getMaster()).setSourcingConfigMasterReference(sConfig.getMasterReference());*/
					skuMasterId = sConfig.getPlaceHolderSKUMasterId();
					skuMasterInt = (int)skuMasterId;
					LCSPartMaster skuMaster = (LCSPartMaster) LCSQuery.findObjectById("OR:com.lcs.wc.part.LCSPartMaster:"+skuMasterInt);
					LCSCostSheet costSheet = new LCSCostSheetLogic().createNewCostSheet(sConfig,skuMaster, spLink, season, false ,(String)skuObject.getValue(modelName));
					costSheet.setValue(model, skuObject);
					LCSCostSheetLogic.persist(costSheet);
				}

			}
		}

	}

	/**
	 * This method sets ItemNumberStr For SKU on create
	 * @param obj WTObject.
	 * @throws WTException WTException.
	 * @throws WTPropertyVetoException WTPropertyVetoException.
	 */
	public static void setItemNumberForSKU(WTObject obj) throws WTException, WTPropertyVetoException{
		LCSLog.debug("Inside set Model Number/Item Number on SKU!!");
		if(obj instanceof LCSSKU){
			LCSSKU sku = (LCSSKU)obj;
			//if version is A
			//if(VersionHelper.getV){
			//PQS to fetch collection of available Model Numbers
			PreparedQueryStatement pqs = new PreparedQueryStatement();
			pqs.appendFromTable(lifeCycleManaged);
			FlexType ft = FlexTypeCache.getFlexTypeFromPath(modelNumberFlexType);
			String columnNameIsUsed = ft.getAttribute(isUsedBOAttKey).getColumnName();
			String columnNameModelNumber = ft.getAttribute(modelNumberBOAttKey).getColumnName();
			pqs.appendSelectColumn(lifeCycleManaged, columnNameModelNumber);
			pqs.appendSelectColumn(lifeCycleManaged, columnNameIsUsed);
			pqs.appendSelectColumn(lifeCycleManaged, "idA2A2");
			String typeIdPath = ft.getTypeIdPath();
			pqs.appendCriteria(new Criteria(lifeCycleManaged, columnNameIsUsed, "0", Criteria.EQUALS));
			pqs.appendAndIfNeeded();
			pqs.appendCriteria(new Criteria(lifeCycleManaged, "flexTypeIdPath", typeIdPath, Criteria.EQUALS));
			pqs.appendSortBy(new QueryColumn(lifeCycleManaged, columnNameModelNumber), QueryStatement.ASC);

			Collection queryResult = LCSQuery.runDirectQuery(pqs).getResults();
			LCSLog.debug("Available Model Number count>>"+queryResult.size());
			Iterator itr = queryResult.iterator();
			String id = null;
			while(itr.hasNext()){
				FlexObject fo = (FlexObject) itr.next();
				id = fo.getString("LCSLifecycleManaged.idA2A2");
				LCSLifecycleManaged managed = (LCSLifecycleManaged) LCSQuery.findObjectById("OR:com.lcs.wc.foundation.LCSLifecycleManaged:"+id);
				LCSLog.debug("Next Model Number to be set!!!"+managed.getName());
				//Change this to string later 
				if(!FormatHelper.hasContent((String)sku.getValue(itemNumberAttKey))){
					sku.setValue(itemNumberAttKey, (String)managed.getValue("name"));
					LCSLog.debug("After setting Model Number on SKU>>");
					managed.setValue(isUsedBOAttKey,true);
					LCSLifecycleManagedLogic.persist(managed);
					LCSLog.debug("After setting Is Used as TRUE>>");
				}
				break;
			}
		}
	}
}
