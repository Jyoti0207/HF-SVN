package com.hf.wc.product;

import java.util.Collection;
import java.util.Iterator;

//import org.apache.log4j.Logger;

import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.flextype.FlexTypeQuery;
import com.lcs.wc.foundation.LCSLifecycleManaged;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.moa.LCSMOAObject;
import com.lcs.wc.moa.LCSMOAObjectQuery;
import com.lcs.wc.moa.LCSMOATable;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;

import wt.fc.WTObject;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

public final class HFProductPlugins {
	
	private HFProductPlugins() {
		
	
	}

	/**
	 * fetching businessObjectType from property entry.
	 */

	//FOrmat in which new look up need to be added 
	/*
	 * Business Object hierarchy1~MOA table hierarchy key~Primary Key attribute key as on Product , BO Hierarchy 2~moaKEY 2~PRODUCT ATT2.....
	 * Assumption : MOA attribute Keys should be same as Product attribute keys
	 * 

	 */
	//private static Logger loggerObject = Logger.getLogger(HFProductPlugins.class);
	//private static final String businessObjectType = LCSProperties.get("com.hf.wc.product.HFProductPlugins.lookup.businessObjectTypes","Business Object\\hfLightKits~hfLightKit~hfLightSpec,Business Object\\dummyTables~dummyTable~dummyAtt");
	private static final String businessObjectType = LCSProperties.get("com.hf.wc.product.HFProductPlugins.lookup.businessObjectTypes","Business Object\\hfLightKits~hfLightKit~hfLightSpec,Business Object\\hfTransmitters~hfTransmitter~hfIncludeFriendlyValue,Business Object\\hfReceivers~hfReceiver~hfReceiverType");
	//private static final String lightKitMOAColumnKeys = LCSProperties.get("com.hf.wc.product.HFProductPlugins.lightKitMOAColumnKeys","hfLightSpec,hfBaseType,hfBulbType,hfBulbsIncluded,hfColorRenderingIndex,hfDimmableBulbs,hfLEDLightKit,hfLifetimeExpectation,hfLightOutput,hfWattsperBulb");
	public static void setLightKitAtts (WTObject primaryBusinessObject) throws WTException, WTPropertyVetoException {

		LCSProduct prodObj = (LCSProduct) primaryBusinessObject;
		if(prodObj instanceof LCSProduct){
			String[] lookUpMapping=businessObjectType.split(",");
			for(String boNode :lookUpMapping){
				String[] values = boNode.split("~");
				String hierarchy=values[0];
				//loggerObject.info("hierarchy::::::"+hierarchy);
				String moaTableKey=values[1];
				//loggerObject.info("moaTableKey::::::"+moaTableKey);
				String moaPrimartAttKey=values[2];
				//loggerObject.info("moaPrimartAttKey::::::"+moaPrimartAttKey);
				String productAttValue = (String) prodObj.getValue(moaPrimartAttKey);
				LCSMOAObject moaRow = processColorCodeLookUpTable(productAttValue,moaPrimartAttKey,moaTableKey,hierarchy);
				//loggerObject.info("moaRow:::::::"+moaRow);
				//loggerObject.info("moaTableKey11111111111111111::::::"+moaTableKey);
				String moaAttKeys = LCSProperties.get("com.hf.wc.product.HFProductPlugins."+moaTableKey);
				if(moaRow!=null && FormatHelper.hasContent(moaAttKeys))
				{
					//New property to be defined for each Moa table > Varying paramater will be the MOA table Key
					//loggerObject.info("moaAttKeys:::::::"+moaAttKeys);
					/*if (FormatHelper.hasContent(moaAttKeys))
					{
					String[] attKeys = moaAttKeys.split(",");
					for(String attKey:attKeys){
						prodObj.setValue(attKey, moaRow.getValue(attKey));
					}
					}*/
					//loggerObject.info("moaAttKeys>>>>>>>>>>>>>>>>"+moaAttKeys);
					
					String[] attKeys = moaAttKeys.split(",");
					//loggerObject.info("attKeys>>>>>>>>>>>>>>>>"+attKeys);
					for(String attKey:attKeys){
						//loggerObject.info("attKey>>>>>>>>>>>>>>>"+attKey);
						prodObj.setValue(attKey, moaRow.getValue(attKey));
						
					}
				}		
			}
		}
	}

	public static LCSMOAObject processColorCodeLookUpTable(String prodLightSpec,String moaPrimartAttKey,  String moaTableKey, String hierarchy)
			throws WTException, WTPropertyVetoException {
		LCSMOAObject moaRow= null;

		// getting the flextype of businessObject
		FlexType lookUpTableFlexType = FlexTypeCache.getFlexTypeFromPath(hierarchy);
		// initializing a new flextype query object
		FlexTypeQuery flexTypeQuery = new FlexTypeQuery();
		// getting all the objects of the lookup table
		SearchResults result = flexTypeQuery.findAllObjectsTypedBy(lookUpTableFlexType);
		String busObjId = "";
		@SuppressWarnings("unchecked")
		Collection<FlexObject> results = result.getResults();
		// iterating through the objects of look up table
		Iterator<FlexObject> it = results.iterator();
		while (it.hasNext()) {
			// getting the current flex object
			FlexObject fo = (FlexObject) it.next();
			// getting the business object Id
			busObjId = String.valueOf(fo.get("LCSLIFECYCLEMANAGED.IDA2A2")); 
			// getting the lookup table object
			LCSLifecycleManaged lightKitLookUpTable = (LCSLifecycleManaged) LCSQuery.findObjectById("OR:com.lcs.wc.foundation.LCSLifecycleManaged:"
					+ busObjId);
			// getting the MOA table object collection
			Collection<?> moaObjectCollection = ((LCSMOATable)lightKitLookUpTable.getValue(moaTableKey)).getRows();
			Iterator<?> itColl = moaObjectCollection.iterator();
			while (itColl.hasNext()) {
				FlexObject flexObj = (FlexObject) itColl.next();
				String id = flexObj.getData("OID");
				// getting the MOA row object
				LCSMOAObject rowObject = (LCSMOAObject) LCSMOAObjectQuery
						.findObjectById("OR:com.lcs.wc.moa.LCSMOAObject:" + id);
				// getting the currentColorFamily
				String currentLightSpec = (String) rowObject
						.getValue(moaPrimartAttKey.toUpperCase());
				if (currentLightSpec.equalsIgnoreCase(prodLightSpec)) {
					moaRow = rowObject;

				}
			}

		}
		return moaRow;

	}

}

