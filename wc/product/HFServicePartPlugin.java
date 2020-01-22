package com.hf.wc.product;
import com.lcs.wc.color.LCSColor;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.SeasonProductLocator;
import com.lcs.wc.util.LCSProperties;
import org.apache.log4j.Logger;
import wt.fc.WTObject;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

/**
 * This class is triggered for Model Name Automation Logic for Service Parts
 * @author ITC Infotech
 * @version 1.1 
 */
public final class HFServicePartPlugin {
	/**
	 * Variable to store  color object internal Name
	 */
	private final static String colorKey = LCSProperties.get("com.hf.wc.product.HFServicePartPlugin.colorKey");
	/**
	 * Variable to store  SKU object internal Name
	 */
	private final static String skuKey = LCSProperties.get("com.hf.wc.product.HFServicePartPlugin.skuKey");
	/**
	 * Variable to store Finish attribute internal Name
	 */
	private final static String finishKey = LCSProperties.get("com.hf.wc.product.HFServicePartPlugin.finishKey");
	/**
	 * Constructor object.
	 */ 
	private HFServicePartPlugin(){

	}
	private static Logger log = Logger.getLogger(HFServicePartPlugin.class.getName());
	/**
	 * @param obj WTobject.
	 * @throws WTException.
	 * @throws WTPropertyVetoException WTPropertyVetoException.
	 */
	
	public static void setColorwayNameForServicePart(WTObject obj) throws WTException, WTPropertyVetoException {
		log.info("Model Name Automation Plugin Trigerred");
		/**Assigning the instance object to LCSSKU object.*/
		LCSSKU colorwayObj = (LCSSKU) obj;
		/** Fetching the LCSProduct object from the LCSSKU object. */
		LCSProduct prodObj = SeasonProductLocator.getProductARev(colorwayObj);
		/** Variable to store the Product Name attribute value */
		String productName = prodObj.getName();
		/**The Model Name (10 digit) is set as the combination of 7 digit Product Name and 3 digit Legacy Finish Code.*/
		if (colorwayObj.getValue(colorKey) != null) {
			/** Variable to store the LCSColor object */
			LCSColor colorObj = (LCSColor) colorwayObj.getValue(colorKey);
			/** Variable to store the Legacy Finish Code attribute value for the given LCSColor */
			String colorcode = colorObj.getValue(finishKey).toString();
			/** Concatenating the LCSProduct name with Legacy Finish Code attribute value for the given LCSColor */
			String colorWayName = productName.concat(colorcode);
			colorwayObj.setValue(skuKey, colorWayName);
		}
	}

}
