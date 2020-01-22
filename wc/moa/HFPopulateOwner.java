package com.hf.wc.moa;

import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.moa.LCSMOAObject;
import com.lcs.wc.moa.LCSMOAObjectLogic;
import com.lcs.wc.part.LCSPartMaster;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.season.SeasonProductLocator;
import com.lcs.wc.util.LCSProperties;

import wt.fc.WTObject;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

/**
 * @author ITC Infotech.
 * @version 1.0.
 */
public final class HFPopulateOwner {
	
	private HFPopulateOwner(){
		
	}
	public final static String Family = LCSProperties.get("com.hf.wc.moa.HFPopulateOwner.product", "hfProduct");

	/**
	 * This method sets product reference on MOA Table.
	 * @param primaryBusinessObject WTObject.
	 * @throws WTPropertyVetoException WTPropertyVetoException.
	 * @throws WTException WTException.
	 */
	public static void populateOwner(WTObject primaryBusinessObject) throws WTPropertyVetoException, WTException {
		
		String propertyValue[] = null;
		if(primaryBusinessObject instanceof LCSMOAObject){
			LCSMOAObject moaObj= (LCSMOAObject)primaryBusinessObject;
			// getting flexType
			FlexType moaFt= moaObj.getFlexType();
			String moaFtKey = moaFt.getTypeName();
			// reading the hierarchies from property entry
			propertyValue= LCSProperties.get("com.hf.wc.populateOwner.moaKeys").split(",");
			for(String pd:propertyValue){
				if(moaFtKey.equalsIgnoreCase(pd)){
					LCSPartMaster partMaster =(LCSPartMaster)moaObj.getOwner();
					// getting product object
					LCSProduct prodObj=	SeasonProductLocator.getProductARev(partMaster);
					// setting product reference on MOA 
					moaObj.setValue(Family, prodObj);
					LCSMOAObjectLogic.persist(moaObj,true);
				}
			}
		}
	}	
}