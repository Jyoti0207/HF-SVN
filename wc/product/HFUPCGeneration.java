package com.hf.wc.product;

import org.apache.log4j.Logger;


import com.hf.wc.util.HFConstants;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.util.LCSProperties;

import wt.fc.WTObject;
import wt.util.WTException;

/**
 * @author ITC Infotech.
 * class consists of plugin method generateUPC which fires on SKU creation.
 * @version "true" 1.1.
 */
public final class HFUPCGeneration {

	private HFUPCGeneration(){

	}
	private static Logger loggerObject = Logger.getLogger(HFUPCGeneration.class);
	/**
	 * Variable to store Model Number att key.
	 */
	private final static String hunterPrefix = LCSProperties.get("com.hf.wc.product.HFUPCGeneration.hunterPrefix","049694");
	/**
	

	/**
	 * This method is used to generate UPC at SKU creation.
	 * @param primaryBusinessObject WTObject.
	 * @throws WTException WTException.
	 * @throws WTException WTException.
	 */
	public static void generateUPC(WTObject primaryBusinessObject) throws WTException{
		//System.out.println("!!! generateUPC plugin trigerred !!!");
		loggerObject.info("!!! generateUPC plugin trigerred !!!");
		if(primaryBusinessObject instanceof LCSSKU){
			LCSSKU skuObj = (LCSSKU)primaryBusinessObject;
			loggerObject.info(":::::::skuObj "+ skuObj);
			if (!skuObj.isPlaceholder()){
			String itemNumberStr =(String)skuObj.getValue(HFConstants.itemNumberAttKey);
			loggerObject.info(":::::::itemNumberStr "+ itemNumberStr);
			
			String num= hunterPrefix.concat(itemNumberStr);
			int checkDigit=HFUPCGeneration.getNumberWithCheckDigit(num);
			String checkDig=Integer.toString(checkDigit);
			String upc= num.concat(checkDig);
			//System.out.println(UPC);
			skuObj.setValue(HFConstants.UPC,upc);
			//LCSLogic.persist(skuObj, true);
		}
	}
	}

	/**
	 * This method gives a checkDigit number(last digit of the UPC number).
	 * @param input.
	 * @return int.
	 */
	public static int getNumberWithCheckDigit(String input) {
		int sum=0,sum1 = 0,sum2=0;
		String input1=input.trim();
		char[]  charIn = input1.toCharArray();
		for (int i = 0; i < charIn.length; i++) {
			if(i % 2 == 0){
				sum1 += Integer.parseInt(String.valueOf(charIn[i]));
			}
			else{
				sum2 += Integer.parseInt(String.valueOf(charIn[i]));
			}
		}
		sum=sum1*3+sum2;
		int temp=sum%10,checkDigit=0;
		if(temp==0){
			checkDigit=0;
		}
		else{
			checkDigit=10-temp;	
		}
		return checkDigit;
	}
}
