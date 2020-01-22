package com.hf.wc.sourcing;

//import org.apache.log4j.Logger;

import com.hf.wc.color.HFColorPlugins;
import com.hf.wc.util.HFConstants;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.SeasonProductLocator;
import com.lcs.wc.sourcing.LCSProductCostSheet;

import wt.fc.WTObject;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

public final class HFCostsheetCalculations {
	
	private HFCostsheetCalculations() {
		
		
	}

	//public static Double freight = null;
	//public static Double shippingCubicFeet = null;
	//public static Double CONSTANT = 1.61;
	//private static Logger loggerObject = Logger.getLogger(HFCostsheetCalculations.class);

	public static void getFreightCost(WTObject primaryBusinessObject) throws WTException, WTPropertyVetoException {
		if (primaryBusinessObject instanceof LCSProductCostSheet)
		{

			LCSProductCostSheet costsheet = (LCSProductCostSheet)primaryBusinessObject;

			LCSSKU model=(LCSSKU) costsheet.getValue(HFConstants.MODEL_NAME);
			//loggerObject.info("model>>>>>>>>>>>>"+model);
			if (model!=null)

			{

				//LCSProduct productobj = (LCSProduct)VersionHelper.latestIterationOf(costsheet.getProductMaster());
				//LCSProduct productobj=SeasonProductLocator.getProductARev(costsheet.getProductMaster());


				/*loggerObject.info("productobj----------------"+productobj.findSeasonUsed().getName());*/
				/*Collection skuCollection = LCSSKUQuery.findSKUs(productobj);

		 Iterator itr = skuCollection.iterator();
		 loggerObject.info("itr.hasNext():::::::::::"+ itr.hasNext());
		 while(itr.hasNext()){
			 LCSSKU sku = (LCSSKU) itr.next();
			 loggerObject.info("sku-----------"+sku.getName());*/
				Double freight = (Double) costsheet.getValue(HFConstants.FREIGHT);
				//loggerObject.info("freight::::::" + freight);


				//	loggerObject.info("colorway feet value is --- " +  model.getValue("hfShippingCubicFeet"));



				Double shippingCubicFeet = (Double) model.getLogicalValue(HFConstants.SHIPPING_CUBIC_FEET);

				freight = (shippingCubicFeet) * (HFConstants.CONSTANT);


				costsheet.setValue(HFConstants.FREIGHT, freight);

			}else
			{
				LCSProduct productobj=SeasonProductLocator.getProductARev(costsheet.getProductMaster());



				Double freight = (Double) costsheet.getValue(HFConstants.FREIGHT);
				//loggerObject.info("freight::::::" + freight);


				//	loggerObject.info("colorway feet value is --- " +  model.getValue("hfShippingCubicFeet"));



				Double shippingCubicFeet = (Double) productobj.getValue(HFConstants.SHIPPING_CUBIC_FEET);

				freight = (shippingCubicFeet) * (HFConstants.CONSTANT);


				costsheet.setValue(HFConstants.FREIGHT, freight);

			}

		}

	}
}

