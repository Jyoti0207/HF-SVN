package com.hf.wc.integration.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.hf.wc.integration.outbound.HFProductColorwayOutbound;
import com.hf.wc.integration.util.HFIntegrationConstants;
import com.hf.wc.integration.util.HFLogFileGenerator;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.season.SeasonProductLocator;
import com.lcs.wc.flextype.FlexTyped;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSException;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.VersionHelper;

import wt.fc.WTObject;
import wt.util.WTException;

public final class SalsifyAttributeValidator {


	private SalsifyAttributeValidator(){
	}

	/**
	 * logger object.
	 */
	static final Logger logger = Logger.getLogger(SalsifyAttributeValidator.class);


	public static void processColorway(WTObject wtObj) throws Exception {
		logger.info("Triggering Colorway file>>>>>>>>>>>>>>>>");
		HFLogFileGenerator.configureLog("Outbound");

		if (wtObj instanceof LCSSKU) {
			LCSSKU skuObj = (LCSSKU)wtObj;

			if (!skuObj.isPlaceholder()) {
				logger.info("invoking processColorwayHelper skuObj is not placeholder >>>>>>>>>>>>>>>>");
				processColorwayHelper(skuObj);
			}
		}
	}
	
	private static void processColorwayHelper(LCSSKU skuObj) throws Exception{

		logger.info("::::::::::entering processColorwayHelper::::::::::::::::::::::");
		Collection<String> mandatoryColorwayAttributes= null;
		Collection<String> mandatoryProductAttributes=null;
		//String skuVersion = skuObj.getVersionDisplayIdentifier().toString();
		logger.info(":::::::::send to staging db:::::::::::::::::::"+ skuObj);
		
		
		
		
		//Set Default Value of sendToStagingDB as false.
		String sendToStagingDB = "false";
		//Use try block to catch the exception thrown by Windchill API.
		try
		{
			logger.info("::::::::::inside try block ::::sendToStagingDB:::::::::::::::::::");
		 sendToStagingDB = skuObj.getValue(HFIntegrationConstants.OUTBOUND_SEND_TO_STAGING_DB).toString();
		}
		//Handling NullPointerException 
		catch (NullPointerException e){
			//System.out.println(e);
			//System.out.println("caught Null PointerException");
			sendToStagingDB	=	"false";
		}
		logger.info(":::::::::::::::::::::::::hfsendtostagingdb::::::::::::::::::::::"+sendToStagingDB);
		
		String skuType = skuObj.getFlexType().getTypeName();
		
		String mandatoryModelAtt= null;
		String mandatoryFamilyAtt = null;
		if("hfFans".equalsIgnoreCase(skuType)){
		 mandatoryModelAtt = LCSProperties.get("com.hf.wc.salsify.validation.mandatoryAttributes.model.fan");
		 mandatoryFamilyAtt = LCSProperties.get("com.hf.wc.salsify.validation.mandatoryAttributes.family.fan");
		}
		 if("hfServiceParts".equalsIgnoreCase(skuType)){
			 mandatoryModelAtt = LCSProperties.get("com.hf.wc.salsify.validation.mandatoryAttributes.model.part");
			 mandatoryFamilyAtt = LCSProperties.get("com.hf.wc.salsify.validation.mandatoryAttributes.family.part");
		}
		logger.info(">>>>>>>>>>>>>>>Inside if wtObj instanceof LCSSKU");


		if(mandatoryModelAtt != null){
			mandatoryColorwayAttributes = FormatHelper.commaSeparatedListToSet(mandatoryModelAtt);
			logger.info(">>>>>>>>>>>>Inside if propertyValue");
		}
		if(mandatoryFamilyAtt != null){
			mandatoryProductAttributes = FormatHelper.commaSeparatedListToSet(mandatoryFamilyAtt);
			logger.info(">>>>>>>>>>>>Inside if propertyValue1");
		}


		if("true".equalsIgnoreCase(sendToStagingDB)){
			StringBuffer missingAttMessage = new StringBuffer();
			Collection<String> missingSKUAtts = null;
			Collection<String> missingProductAtts = null;

			skuObj=SeasonProductLocator.getSKUARev(skuObj);
			missingSKUAtts = validateObject(skuObj, mandatoryColorwayAttributes);
			logger.info(">>>>>>>>>>>>missingSKUAtts"+missingSKUAtts);

			// Validate Product
			LCSProduct prodARev = (LCSProduct)skuObj.getProduct();
			prodARev = (LCSProduct)VersionHelper.getVersion(prodARev, "A");
			missingProductAtts = validateObject(prodARev, mandatoryProductAttributes);
			logger.info(">>>>>>>>>>>>>>missingProductAtts"+missingProductAtts);
			// Product Template validation.
			//String productType = (String)prodARev.getValue("umProductType");
			missingAttMessage.append(getMissingAttributeMessage(missingSKUAtts, "MODEL"));
			missingAttMessage.append(getMissingAttributeMessage(missingProductAtts, "FAMILY"));
			if (FormatHelper.hasContent(missingAttMessage.toString())) {
				throw new LCSException("VALIDATION FAILURE FOR MODEL - CANNOT SAVE AS THE FOLLOWING DATA NEEDED FOR SALSIFY IS MISSING : " + missingAttMessage.toString());

			}
			else{
				//HFProductColorwayOutbound colorwayOutbound = new HFProductColorwayOutbound();
				logger.info(">>>>>>>>>>>>>>>>>sendProductColorwayData");
				HFProductColorwayOutbound.sendProductColorwayData(skuObj);
			}
		}
	
	}
	
	
	private static String getMissingAttributeMessage(final Collection<String> missingAtts, final String objectName) {
		HFLogFileGenerator.configureLog("Outbound");
		StringBuffer missingAttMessage = new StringBuffer();
		if (missingAtts != null && missingAtts.size() > 0) {
			logger.info(">>>>>>>>>>>Inside if missingAtts");
			missingAttMessage.append(objectName.concat( " = { "));
			Iterator<String> itr = missingAtts.iterator();

			while (itr.hasNext()) {
				missingAttMessage.append(itr.next());
				if (itr.hasNext()){
					missingAttMessage.append(", ");
				}
			}
			missingAttMessage.append(" } ; ");
			logger.info(">>>>>>>>>>>>missingAttMessage");
		}

		return missingAttMessage.toString();
	}

	private static Collection<String> validateObject(FlexTyped fObj, final Collection<String> mandatoryAttList) throws Exception {
		HFLogFileGenerator.configureLog("Outbound");
		Collection<String> missingValues = new ArrayList<String>();
		try {
			if (fObj != null && mandatoryAttList != null && mandatoryAttList.size() > 0) {

				FlexType fType = fObj.getFlexType();
				Iterator<String> itrMandatoryAttList = mandatoryAttList.iterator();
				FlexTypeAttribute attDef = null;
				String attKey = null;
				String displayValue = null;
				logger.info(">>>>>>>>>>>>>>>>>>if fObj != null && mandatoryAttList != null");
				while (itrMandatoryAttList.hasNext()) {
					attKey = itrMandatoryAttList.next();
					attDef = fType.getAttribute(attKey);

					if (fObj.getValue(attKey) instanceof String) {

						displayValue = attDef.getDisplayValue((String) fObj.getValue(attKey));
						logger.info(">>>>>>>>>>>>>>>>>>displayValue"+displayValue );
						if (!FormatHelper.hasContent(displayValue)) {
							missingValues.add(attDef.getAttDisplay());
						}
					} else if (fObj.getValue(attKey) instanceof FlexTyped) {

						if (fObj.getValue(attKey) == null) {
							missingValues.add(attDef.getAttDisplay());
							logger.info(">>>>>>>>>>>>>>>>missingValues.add(attDef.getAttDisplay());");
						}
					} else if (fObj.getValue(attKey) instanceof Double) {
						if (!FormatHelper.hasContent(String.valueOf(((Double) fObj.getValue(attKey)).doubleValue()))) {
							missingValues.add(attDef.getAttDisplay());
							logger.info(">>>>>>>>>>>>>missingValues.add(attDef.getAttDisplay());");
						}

					}else if (fObj.getValue(attKey) instanceof Long) {
						Long lng = (Long)fObj.getValue(attKey);
						String lngStr = "";
						if(lng!=null){
							lngStr = lng.toString();
							logger.info(">>>>>>>>>>>>lngStr");
						}
						if (!FormatHelper.hasContent(lngStr)) {
							missingValues.add(attDef.getAttDisplay());
						}

					} else if (fObj.getValue(attKey) instanceof FlexObject) {

						if (!FormatHelper.hasContent(((FlexObject) fObj.getValue(attKey)).getData("FULLNAME"))) {
							missingValues.add(attDef.getAttDisplay());
							logger.info(">>>>>>>>>>>>>>missingValues.add(attDef.getAttDisplay());");
						}

					} else {
						missingValues.add(attDef.getAttDisplay());
					}
				}
			}


		} catch (Exception excp) {
			logger.info("ERROR DUE TO : " + excp.getMessage());
			excp.printStackTrace();
			throw excp;
		}

		return missingValues;
	}
}
