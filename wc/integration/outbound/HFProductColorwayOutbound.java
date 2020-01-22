package com.hf.wc.integration.outbound;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import java.time.format.DateTimeFormatter; 
import java.time.LocalDateTime;    
import com.hf.wc.integration.inbound.HFInboundIntegrationHelper;
import com.hf.wc.integration.util.HFExtractAttsValues;
import com.hf.wc.integration.util.HFIntegrationConstants;
import com.hf.wc.integration.util.HFLogFileGenerator;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.foundation.LCSLogic;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.moa.LCSMOAObject;
import com.lcs.wc.moa.LCSMOAObjectQuery;
import com.lcs.wc.moa.LCSMOATable;
import com.lcs.wc.part.LCSPartMaster;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.SeasonProductLocator;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.VersionHelper;

import wt.fc.WTObject;
import wt.queue.ProcessingQueue;
import wt.queue.QueueHelper;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.vc.wip.WorkInProgressHelper;
import wt.vc.wip.Workable;

/** This is the File to send outbound data.
 * @author "true" ITCINFOTECH
 * @version "true" 1.0
 */
public final class HFProductColorwayOutbound {

	/**
	 * @param args
	 */
	/**
	 * loggerObject for logging.
	 */
	private HFProductColorwayOutbound(){
		
	}
	private static Logger loggerObject = Logger.getLogger(HFProductColorwayOutbound.class);
	/**
	 * For Status to be set to Model
	 */
	private static String status	=	"failure";
	private final static String BOM_TYPE =	"BOM_TYPE";
	private final static String BRANCH_PLANT =	"BRANCH_PLANT";
	
	/**
	/**
	 * HFProductColorwayOutbound constructor.
	 * 
	 */	
	private static String isPart = "false";
	private static String propertyValueColorway [] = null;
	private static String propertyValueProduct [] = null;
	


	/**
	 * sendProductColorwayData Method to send data.
	 * @param obj for obj
	 * @throws WTException
	 * @throws WTPropertyVetoException 
	 */	
	public static void sendProductColorwayData(LCSSKU colorwayObj) throws WTException, WTPropertyVetoException {
		LCSSKU colorwayObject	=	colorwayObj;
		loggerObject.info(":::::::::: isPart value right at the beginning ::::::" +isPart);
		loggerObject.info("::::::::::colorwayObject ::::::" + colorwayObject.getName());
		//String property ="NUMBER_OF_BLADES~hfNumberofBlades, BLADE_PITCH~hfBladePitchList, BLADES_INCLUDED~hfBladesIncluded, NUMBER_OF_BULBS~hfNumberofBulbs, BULBS_INCLUDED~hfBulbsIncluded, MARKETING_DESCRIPTION~hfMarketingDescription, DIM_A_ANGLED_CEILING_TO_BOTTOM_OF_FAN~hfDimAAngledCeilingtoBottomofFan, DIM_A_FLUSH_CEILING_TO_BOTTOM_OF_FAN~hfDimAFlushCeilingtoBottomofFan, DIM_A_STANDARD_CEILING_TO_BOTTOM_OF_FAN~hfDimAStandardCeilingtoBottomofFan, DIM_B_ANGLED_CEILING_TO_BOTTOM_OF_FAN_BLADE~hfDimBAngledCeilingtoBottomofFanBlade, DIM_B_FLUSH_CEILING_TO_BOTTOM_OF_FAN_BLADE~hfDimBFlushCeilingtoBottomofFanBlade, DIM_B_STANDARD_CEILING_TO_BOTTOM_OF_FAN_BLADE~hfDimBStandardCeilingtoBottomofFanBlade, DIM_C_STANDARD_CEILING_TO_BOTTOM_OF_CANOPY~hfDimCStandardCeilingtoBottomofCanopy, DIM_D_STANDARD_WIDTH_OF_FAN_BODY~hfDimDStandardWidthOfFanBody, DIM_E_STANDARD_CANOPY_WIDTH~hfDimEStandardCanopyWidth, DIM_F_ANGLED_CEILING_TO_BOTTOM_OF_LIGHT~hfDimFAngledCeilingtoBottomofLight, DIM_F_FLUSH_CEILING_TO_BOTTOM_OF_LIGHT~hfDimFFlushCeilingtoBottomofLight, DIM_F_STANDARD_CEILING_TO_BOTTOM_OF_LIGHT~hfDimFStandardCeilingtoBottomofLight, DIM_G_STANDARD_HEIGHT_OF_MOTOR_HOUSING~hfDimGStandardHeightofMotorHousing, INCLUDED_DOWNROD_LENGTH~hfIncludedDownrodLength, SECOND_INCLUDED_DOWNROD_LENGTH~hfSecondIncludedDownrodLength, THIRD_INCLUDED_DOWNROD_LENGTH~hfThirdIncludedDownrodLength, REVERSABLE_MOTOR~hfReversableMotor, IMAP~hfIMAP, ITEM_STATUS~hfItemStatus, LEVEL_2_BRAND~hfLevel2Brand, LEVEL_4_FAMILY_NAME~hfLevel4familyname, LOCATION_USAGE~hfLocationUsage, MARKETING_GROUP~hfMarketingGroup, PRODUCT_CLASS~hfProductClass, PULL_CHAIN_INCLUDED~hfPullChainIncluded, RECALL~hfRecall, SIZE~hfSize, UPC~hfUPC, VOLTS_HZ~hfVoltsHz, REVERSIBLE_BLADES~hfReversibleBlades, ENERGY_STAR~hfEnergyStar, CAN_BE_USED_WITHOUT_LIGHT_KIT~hfCanbeusedwithoutLightKit, AIRFLOW~hfAirflow, AIRFLOW_EFFIENCY~hfAirflowEffiency, ESTIMATEDYEARLYENERGYCOST~hfEstimatedYearlyEnergyCost, ENERGY_USE~hfEnergyUse, VELOCITY~hfVelocityFanPerformanceinMPH, SHIPPING_LENGTH~hfShippingLengthin, SHIPPING_HEIGHT~hfShippingHeightin, SHIPPING_CUBIC_FEET~hfShippingCubicFeet, SHIPPING_WEIGHT~hfShippingWeightLbs, SHIPPING_WIDTH~hfShippingWidthin, NEW_WEB_ROOM~hfnewWebRoom, SIX_SQUARE~hf6squares, STYLE_PYRAMID~hfStylePyramid, ALTERNATE_LANGUAGE~hfAlternateLanguage, FILM_SIZE_H~hfFilmSizeHmm, FILM_SIZE_L~hfFilmSizeLmm, FILM_SIZE_W~hfFilmSizeWmm, CURRENT_SHIP_PACK_QTY~hfCurrentShipPackQty, ADDITIONAL_TRANSMITTER_RECEPTACLE~hfAdditionalTransmitterReceptacle, BASE_TYPE~hfBaseType, CONTROL_TYPE_INCLUDED~hfControlTypeIncluded, LIGHT_KIT_INCLUDED~hfLightKitIncluded, MOTOR_TYPE~hfMotorType, MOUNTING_OPTIONS_FRIENDLY_VALUE~hfMountingOptionsFriendlyValue, NOLIGHT_CAP_INCLUDED~hfNoLightCapIncluded, LEVEL_1_PRODUCT_CATEGORY~hfLevel1ProductCategory, RECEIVER_LOCATION~hfReceiverLocation, STAINLESS_STEEL_HARDWARE~hfStainlessSteelHardware, WEB_COLLECTION~hfWebCollection, DIMMABLE_BULBS~hfDimmableBulbs, WATTS_PER_BULB~hfWattsperBulb, RECEIVER_INCLUDED~hfReceiverIncluded, BUSINESS_UNIT~hfBusinessUnit, CANOPY_TYPE~hfCanopyType, LEVEL_6_SPEEDS~hfLevel6Speeds, LEVEL_7_BULB_TYPE~hfLevel7BulbType, LEVEL_7_LIGHT_CONTROLS~hfLevel7LightControls, LEVEL_7_MATERIAL~hfLevel7Material, LEVEL_8_BLADE_LENGTH~hfLevel8BladeLength, LEVEL_8_LIGHT_KIT_TYPE~hfLevel8LightKitType, FAMILY_NAME~productName, SIMPLECONNECT~hfSIMPLEconnect, SPEEDS~hfSpeed, WEB_COLLECTION_CODE~hfWebCollectCode, LEVEL3_SUBCATEGORY~hfLevel3SubCategory, ENERGY_GUIDE~hfEnergyGuide, INSTALLATION_GUIDE~hfInstallation, NEW_WEB_FAMILY_CODE~hfNewWebFamily, NEW_FAMILY_NAME~hfNewName, BULB_TYPE~hfBulbType, INCLUDED_CONTROL~hfIncluded, INCLUDED_CONTROL_FRIENDLY_VALUE~hfIncludeFriendlyValue, INTERNATIONAL~international, LEVEL_3_STYLE~hfLevelStyle, LEVEL_6_ROOM_SIZE~hfLevel6RoomSize, PRODUCT_TYPE~hfProductTypeMaterials, LEVEL_7_MOTOR_SIZE~hfLevelMotorSize, LED_LIGHT_KIT~hfLEDLightKit, ULTRA_LOW_PROFILE~hfUltraLowProfile, EXCLUSIVE_CASA_BLADES~hfExclusiveCasaBlades, WARRANTY~hfWarranty, BATTERIES_INCLUDED~hfBatteriesIncluded, NUMBER_OF_BATTERIES~hfNumberofBatteries, TYPE_OF_BATTERIES~hfTypeofBatteries, BATTERY_SIZE~hfBatterySize, RX_RECEIVER_PART_NUMBER~hfRx, TX_WALL_CONTROL~hfTx, POPLOCK_BLADES~hfPoplockBlades, SUBCLASS~hfSubClass, UPLIGHT_BULB_BASE~hfUplightBulbBase, UPLIGHT_QTY~hfUplightQty, UPLIGHT_WATTS~hfUplightWatts, CORRELATED_COLOR_TEMPERATURE~hfCorrelatedColorTemperature, COLOR_RENDERING_INDEX~hfColorRenderingIndex, LIFETIME_EXPECTATION~hfLifetimeExpectation, CONTROL_ENGINEERING_PART~hfControlEngineeringPart, DRAWING_NUMBER~hfDrawingNumber, ENERGY_STAR_LIGHT_KIT~hfEnergyStarLightKit, MAX_WATTAGE_~hfMaxWattageTested, RECOMMENDED_BLADES~hfRecommended, ETL_TYPE~hfETLType, DIPS_ON_RECEIVER~hfDipsonReceiver, UPLIGHT_DOWNLIGHT_SEPARATE_CONTROL~UplightDownlightSeperateControl, FIVE_MINUTE_FAN~hfFiveMinuteFan, SOFTLIGHTS~hfSoftlights, LISTING_AGENCY~hfListingAgency, BLADE_MATERIAL~hfBladeMaterial, FAMILY_NUMBER~hfFamilyNumber, RECEIVER_NUMBER~hfReceivernumber, TRANSMITTER_NUMBER~hfTransmitternumber, TWIST_LOCK~hfTwistLock, SIMILAR_TO_~hfSimilar, TIER_LAUNCH_TYPE~hfTierLaunch, CFM_HIGH~hfCFMHigh, STYLE_TARGETS~hfStyle, CFM_WATT_EXTRA_LOW~hfCFMWattExtraLow, CFM_WATT_HIGH~hfCFMWattHigh, CFM_WATT_LOW~hfCFMWattLow, CFM_WATT_MEDIUM~hfCFMWattMedium, CFM_WATT_MEDIUM_HIGH~hfCFMWattMediumHigh, CFM_WATT_MEDIUM_LOW~hfCFMWattMediumLow, WATTS_EXTRA_LOW~hfWattsExtraLow, WATTS_HIGH~hfWattsHigh, WATTS_LOW~hfWattsLow, WATTS_MEDIUM~hfWattsMedium, WATTS_MEDIUM_HIGH~hfWattsMediumHigh, WATTS_MEDIUM_LOW~hfWattsMediumLow, LIGHT_SPEC~hfLightSpec, ID_NUMBER~hfIDNumber, EASY_INSTALL~hfEasyInstall, ROOM_TYPE~hfRoomTypePrd, RECEIVER_TYPE~hfReceiverType, HANGING_SYSTEM_TYPE~hfHangingSystem";
		//String property ="MARKETING_DESCRIPTION~hfMarketingDescription,IMAP~hfIMAP, ITEM_STATUS~hfItemStatus, LEVEL_2_BRAND~hfLevel2Brand,LOCATION_USAGE~hfLocationUsage, MARKETING_GROUP~hfMarketingGroup, PRODUCT_CLASS~hfProductClass, RECALL~hfRecall, UPC~hfUPC,SHIPPING_LENGTH~hfShippingLengthin, SHIPPING_HEIGHT~hfShippingHeightin, SHIPPING_CUBIC_FEET~hfShippingCubicFeet, SHIPPING_WEIGHT~hfShippingWeightLbs, SHIPPING_WIDTH~hfShippingWidthin, FILM_SIZE_H~hfFilmSizeHmm, FILM_SIZE_L~hfFilmSizeLmm, FILM_SIZE_W~hfFilmSizeWmm, CURRENT_SHIP_PACK_QTY~hfCurrentShipPackQty, LEVEL_1_PRODUCT_CATEGORY~hfLevel1ProductCategory, BUSINESS_UNIT~hfBusinessUnit, FAMILY_NAME~productName,WARRANTY~hfWarranty, SUBCLASS~hfSubClass,CONTROL_ENGINEERING_PART~hfControlEngineeringPart, DRAWING_NUMBER~hfDrawingNumber,LISTING_AGENCY~hfListingAgency, RECEIVER_NUMBER~hfReceivernumber, TRANSMITTER_NUMBER~hfTransmitternumber, ID_NUMBER~hfIDNumber, EASY_INSTALL~hfEasyInstall,HANGING_SYSTEM_TYPE~hfHangingSystem";
		HFLogFileGenerator.configureLog("Outbound");
		loggerObject.info(":::::::::::::Executing Colorway Create/Update event::::::::::::::::");
		//populatePropertyValues(colorwayObject);
		
				
			loggerObject.info(":::::::::colorwayObject::::::::::::::::::::::::"+colorwayObject);
			String queueName = LCSProperties.get("com.hf.wc.integration.salsify.outbound.HFSalsifyOutboundQueue");
			ProcessingQueue processQueue = null;
			try {
				processQueue = (ProcessingQueue) QueueHelper.manager.getQueue(queueName, wt.queue.ProcessingQueue.class);
				// Check if the queue exists. If not, create the queue.
				if (processQueue == null) {
					loggerObject.info("Queue does not exist!! Creating a new queue '"+ queueName + "'.");
					processQueue = QueueHelper.manager.createQueue(queueName);
					// Enabling the queue if not enabled.
					processQueue.setEnabled(true);
					// Starting the queue here if it is not in Started state.
					processQueue = QueueHelper.manager.startQueue(processQueue);
					
					
				}
				// Carrying on with the queue entry only if the Queue is in Started State.
				if (!processQueue.getQueueState().equals(ProcessingQueue.STARTED))
				{
					loggerObject.info("Queue is not started! Starting queue :::::::::queueName:::::::"+ queueName);
					loggerObject.info("Inside if block processQueue.getQueueState");
					processQueue = QueueHelper.manager.startQueue(processQueue);
				}
				if ("STARTED".equals(processQueue.getQueueState())) {
					String prodColorwayObjId = FormatHelper.getObjectId(colorwayObject);
					String  prodColorwayMap ="";
					prodColorwayMap = prodColorwayObjId;
					//prodColorwayMap.put("prodColorwayObjId", prodColorwayObjId);
					/*prodColorwayMap.put("propertyValueColorway", propertyValueColorway);
					prodColorwayMap.put("propertyValueProduct", propertyValueProduct);*/
					//prodColorwayMap.put("reqEvent", "post");
					Class[] argTypes = { String.class };
					Object[] argValues = { prodColorwayMap };
					wt.org.WTPrincipal wtprincipal = SessionHelper.manager.getPrincipal();
					processQueue.addEntry(wtprincipal, "pushProdColorwayUpdatedData",(HFProductColorwayOutbound.class).getName(),argTypes,argValues);
					loggerObject.info("###########12345 Successfully placed Colorway  in  Queue : "+prodColorwayObjId);
				} 
			} catch (WTException e) {
				//String failureComment="Failure has occured";
				status="failure";
				loggerObject.error(colorwayObject.getDisplayIdentity().toString());
				loggerObject.error(e);

			}
		}


	private static void populatePropertyValues(LCSSKU colorwayObject) {
		try {
			//String property ="ITEM_STATUS~hfItemStatuss,ITEM_NUMBER~hfItemNumberStr,MODEL_NUMBER~hfModelNumberCatalogNumber,BASE_RETAIL~hfBaseRetail,ABC_CLASSIFICATION~hfABCClassification,BUYER_NUMBER~hfBuyerNumber,COUNTRY_OF_ORIGIN~hfCountryofOrigin,DEMAND_TIME_FENCE_DAYS_BYHALIA~hfDemandTimeFenceDaysByhalia,DEMAND_TIME_FENCE_DAYS_SUPPLIER~hfDemandTimeFenceDaysSupplier,FIXED_DAYS_OF_SUPPLY~hfFixedDaysofSupply,FIXED_LOT_MULTIPLIER~hfFixedLotMultiplier,FIXED_ORDER_QUANTITY~hfFixedOrderQuantity,FULL_LEAD_TIME~hfFullLeadTime,INTRANSIT_LEAD_TIME~hfIntransitLeadTime,MAXIMUM_ORDER_QTY~hfMaximumOrderQty,MINIMUM_ORDER_QTY~hfMinimumOrderQty,MRP_SAFTEYSTOCK_DAYS~hfMRPSafteyStockDays,NOTE_CUSTOMER_SUPPORT~hfNoteCustomerSupport,NOTE_MATERIALS~hfNoteMaterials,PLANNER_NUMBER~hfPlannerNumber,PLANNING_MAKE_BUY_CODE~hfPlanningMakeBuyCode,PLANNINGTIME_FENCE_DAYS_BYHALIA~hfPlanningTimeFenceDaysByhalia,PLANNING_TIME_FENCE_DAYS_SUPPLIER~hfPlanningTimeFenceDaysSupplier,PRIMARY_SUPPLIER~hfPrimarySupplierFactory,QUANTITY_MASTER_PACK~hfQuantityMasterPack,SAFETY_STOCK_CODE~hfSafetyStockCode,SAFETYSTOCK_PERCENT~hfSafetyStockPercent,SAFETYSTOCK_QUANTITY~hfSafetyStockQuantity,SUPPLIER_LEAD_TIME~hfSupplierLeadTime,TARIFF_CODE~hfTariffCode,UOM~hfUOM,DEMAND_TIME_FENCE_DAYS_CHINA_WAREHOUSE~hfDemandTimeFenceDaysChinaWarehouse,PLANNING_TIME_FENCE_DAYS_CHINA_WAREHOUSE~hfPlanningTimeFenceDaysChinaWarehouse,INTRANSIT_LEAD_TIME_SUPPLIER~hfIntransitLeadTimeSupplier,INTRANSIT_LEAD_TIME_CHINA_WAREHOUSE~hfIntransitLeadTimeChinaWarehouse,SUPPLIER_LEAD_TIME_SUPPLIER~hfSupplierLeadTimeSupplier,SUPPLIER_LEAD_TIME_CHINA_WAREHOUSE~hfSupplierLeadTimeChinaWarehouse,SAFETY_STOCK_CODE_SUPPLIER~hfSafetyStockCodeSupplier,SAFETY_STOCK_CODE_CHINA_WAREHOUSE~hfSafetyStockCodeChinaWarehouse,SAFETYSTOCK_PERCENT_SUPPLIER~hfSafetyStockPercentSupplier,SAFETYSTOCK_PERCENT_CHINA_WAREHOUSE~hfSafetyStockPercentChinaWarehouse,SAFETYSTOCK_QUANTITY_SUPPLIER~hfSafetyStockQuantitySupplier,SAFETYSTOCK_QUANTITY_CHINA_WAREHOUSE~hfSafetyStockQuantityChinaWarehouse,MINIMUM_ORDER_QTY_SUPPLIER~hfMinimumOrderQtySupplier,MINIMUM_ORDER_QTY_CHINA_WAREHOUSE~hfMinimumOrderQtyChinaWarehouse,ABC_CLASSIFICATION_SUPPLIER~hfABCClassificationSupplier,ABC_CLASSIFICATION_CHINA_WAREHOUSE~hfABCClassificationChinaWarehouse,ABC_CLASSIFICATION_CORPORATE~hfABCClassificationCorporate,JDE_DESCRIPTION~hfJDEDescription,OLD_MODEL_NUMBER~hfOldModelNumber,SAFETY_STOCK_CODE_DESCRIPTION~hfSafetyCodeStockDescription,SAFETY_STOCK_CODE_VALUE~hfSafetyStockCodeValue,OLD_ITEM_NUMBER~hfOLDItemNumber,DISTRIBUTOR_NET_PRICE~hfDistributorNetPrice,LEVEL_5_FINISH_CATEGORY~hfLevel5Category,LEVEL_6_GLASS_FINISH_DESCRIPTION~hfLevel6GlassFinishDescription,SG1_ESTIMATED_12_MO_VOLUME~hfSG1Estimated12moVolume,SG2_ESTIMATED_12_MO_VOLUME~hfSG2Estimated12moVolume,LEVEL_6_HOLE_PATTERN_DESCRIPTION~hfLevel6HolePatterDescription,FCC_ID~hfFCCID,LEVEL_8_REVERSING~hfLevel8Reversing,ASSOCIATED_MODELS_FOR_PARTS~hfAssociatedModelsforparts,LIGHT_OUTPUT~hfLightOutput,COMMENTS_MARKETING~hfCommentsMarketing,COMMENTS_TECH_SUPPORT~hfCommentsTechSupport,BLADE_SIDE_1_FINISH~hfBladeSide1Finish,BLADE_SIDE_2_FINISH~hfBladeSide2Finish,HOUSING_FINISH~hfHousingFinish,SPECIFIC_FINISH~hfSpecificFinish,GLASS_FINISH~hfGlassFinish,GLASS_FINISH_CODE~hfGlassFinishCode,NEW_MODEL_NUMBER~hfNewModelnumber,QUANTITY_OVER_PACK~hfQuantityoverpack,MSRP~hfMsrp,ECOMMERCE_PRICE~hfEcommercePrice,SPECIAL_ORDER_PRICE~hfSpecialOrderPrice,UPC~hfUPC,SUPPLIER_NUMBER~hfSupplierNumber,USERNAME~hfUsername";
			//String propprod ="MARKETING_DESCRIPTION~hfMarketingDescription,IMAP~hfIMAP,LEVEL_2_BRAND~hfLevel2Brand,LOCATION_USAGE~hfLocationUsage,MARKETING_GROUP~hfMarketingGroup,PRODUCT_CLASS~hfProductClass,RECALL~hfRecall,SHIPPING_LENGTH~hfShippingLengthin,SHIPPING_HEIGHT~hfShippingHeightin,SHIPPING_CUBIC_FEET~hfShippingCubicFeet,SHIPPING_WEIGHT~hfShippingWeightLbs,SHIPPING_WIDTH~hfShippingWidthin,FILM_SIZE_H~hfFilmSizeHmm,FILM_SIZE_L~hfFilmSizeLmm,FILM_SIZE_W~hfFilmSizeWmm,CURRENT_SHIP_PACK_QTY~hfCurrentShipPackQty,LEVEL_1_PRODUCT_CATEGORY~hfLevel1ProductCategory,BUSINESS_UNIT~hfBusinessUnit,WARRANTY~hfWarranty,SUBCLASS~hfSubClass,CONTROL_ENGINEERING_PART~hfControlEngineeringPart,DRAWING_NUMBER~hfDrawingNumber,LISTING_AGENCY~hfListingAgency,RECEIVER_NUMBER~hfReceivernumber,TRANSMITTER_NUMBER~hfTransmitternumber,ID_NUMBER~hfIDNumber,EASY_INSTALL~hfEasyInstall";
			loggerObject.info("################# Inside Slasify Outbound  integration #################");

			if (colorwayObject != null && VersionHelper.isCheckedOut(colorwayObject)) {
				colorwayObject.setValue(HFIntegrationConstants.COLORWAY_INTEGRATION_STATUS,"Processing");
				colorwayObject= (LCSSKU) VersionHelper.checkin(colorwayObject);
				loggerObject.info(">>type>>>>:::::::::::::::"+colorwayObject.getFlexType().getTypeName());
				String ftIntName = colorwayObject.getFlexType().getTypeName();
				
				if("hfServiceParts".equalsIgnoreCase(ftIntName)){
					isPart = "true";
					loggerObject.info(":::::::::::::::::::entering servicepart for outbound::::::::::isPart:::::::::" + isPart);
					propertyValueColorway= LCSProperties.get("com.hf.wc.integration.part.outbound.colorwayAttributeKeys").split(",");
					
					propertyValueProduct= LCSProperties.get("com.hf.wc.integration.part.outbound.productAttributeKeys").split(",");
					//propertyValueColorway= property.split(",");
					//propertyValueProduct= propprod.split(",");
				}
				else{
					isPart = "false";
					loggerObject.info(":::::::::::::::::::entering fans for outbound::::::::isPart:::::::::::"+isPart);
					//String propcolorvalue="ITEM_STATUS~hfItemStatuss,ITEM_NUMBER~hfItemNumberStr,MODEL_NUMBER~hfModelNumberCatalogNumber,BASE_RETAIL~hfBaseRetail,ABC_CLASSIFICATION~hfABCClassification,BUYER_NUMBER~hfBuyerNumber,COUNTRY_OF_ORIGIN~hfCountryofOrigin,DEMAND_TIME_FENCE_DAYS_BYHALIA~hfDemandTimeFenceDaysByhalia,DEMAND_TIME_FENCE_DAYS_SUPPLIER~hfDemandTimeFenceDaysSupplier,FIXED_DAYS_OF_SUPPLY~hfFixedDaysofSupply,FIXED_LOT_MULTIPLIER~hfFixedLotMultiplier,FIXED_ORDER_QUANTITY~hfFixedOrderQuantity,FULL_LEAD_TIME~hfFullLeadTime,INTRANSIT_LEAD_TIME~hfIntransitLeadTime,MAXIMUM_ORDER_QTY~hfMaximumOrderQty,MINIMUM_ORDER_QTY~hfMinimumOrderQty,MRP_SAFTEYSTOCK_DAYS~hfMRPSafteyStockDays,NOTE_CUSTOMER_SUPPORT~hfNoteCustomerSupport,NOTE_MATERIALS~hfNoteMaterials,PLANNER_NUMBER~hfPlannerNumber,PLANNING_MAKE_BUY_CODE~hfPlanningMakeBuyCode,PLANNINGTIME_FENCE_DAYS_BYHALIA~hfPlanningTimeFenceDaysByhalia,PLANNING_TIME_FENCE_DAYS_SUPPLIER~hfPlanningTimeFenceDaysSupplier,PRIMARY_SUPPLIER~hfPrimarySupplierFactory,QUANTITY_MASTER_PACK~hfQuantityMasterPack,SAFETY_STOCK_CODE~hfSafetyStockCode,SAFETYSTOCK_PERCENT~hfSafetyStockPercent,SAFETYSTOCK_QUANTITY~hfSafetyStockQuantity,SUPPLIER_LEAD_TIME~hfSupplierLeadTime,TARIFF_CODE~hfTariffCode,UOM~hfUOM,DEMAND_TIME_FENCE_DAYS_CHINA_WAREHOUSE~hfDemandTimeFenceDaysChinaWarehouse,PLANNING_TIME_FENCE_DAYS_CHINA_WAREHOUSE~hfPlanningTimeFenceDaysChinaWarehouse,INTRANSIT_LEAD_TIME_SUPPLIER~hfIntransitLeadTimeSupplier,INTRANSIT_LEAD_TIME_CHINA_WAREHOUSE~hfIntransitLeadTimeChinaWarehouse,SUPPLIER_LEAD_TIME_SUPPLIER~hfSupplierLeadTimeSupplier,SUPPLIER_LEAD_TIME_CHINA_WAREHOUSE~hfSupplierLeadTimeChinaWarehouse,SAFETY_STOCK_CODE_SUPPLIER~hfSafetyStockCodeSupplier,SAFETY_STOCK_CODE_CHINA_WAREHOUSE~hfSafetyStockCodeChinaWarehouse,SAFETYSTOCK_PERCENT_SUPPLIER~hfSafetyStockPercentSupplier,SAFETYSTOCK_PERCENT_CHINA_WAREHOUSE~hfSafetyStockPercentChinaWarehouse,SAFETYSTOCK_QUANTITY_SUPPLIER~hfSafetyStockQuantitySupplier,SAFETYSTOCK_QUANTITY_CHINA_WAREHOUSE~hfSafetyStockQuantityChinaWarehouse,MINIMUM_ORDER_QTY_SUPPLIER~hfMinimumOrderQtySupplier,MINIMUM_ORDER_QTY_CHINA_WAREHOUSE~hfMinimumOrderQtyChinaWarehouse,ABC_CLASSIFICATION_SUPPLIER~hfABCClassificationSupplier,ABC_CLASSIFICATION_CHINA_WAREHOUSE~hfABCClassificationChinaWarehouse,ABC_CLASSIFICATION_CORPORATE~hfABCClassificationCorporate,JDE_DESCRIPTION~hfJDEDescription,OLD_MODEL_NUMBER~hfOldModelNumber,SAFETY_STOCK_CODE_DESCRIPTION~hfSafetyCodeStockDescription,SAFETY_STOCK_CODE_VALUE~hfSafetyStockCodeValue,OLD_ITEM_NUMBER~hfOLDItemNumber,DISTRIBUTOR_NET_PRICE~hfDistributorNetPrice,LEVEL_5_FINISH_CATEGORY~hfLevel5Category,LEVEL_6_GLASS_FINISH_DESCRIPTION~hfLevel6GlassFinishDescription,SG1_ESTIMATED_12_MO_VOLUME~hfSG1Estimated12moVolume,SG2_ESTIMATED_12_MO_VOLUME~hfSG2Estimated12moVolume,LEVEL_6_HOLE_PATTERN_DESCRIPTION~hfLevel6HolePatterDescription,FCC_ID~hfFCCID,LEVEL_8_REVERSING~hfLevel8Reversing,ASSOCIATED_MODELS_FOR_PARTS~hfAssociatedModelsforparts,LIGHT_OUTPUT~hfLightOutput,COMMENTS_MARKETING~hfCommentsMarketing,COMMENTS_TECH_SUPPORT~hfCommentsTechSupport,BLADE_SIDE_1_FINISH~hfBladeSide1Finish,BLADE_SIDE_2_FINISH~hfBladeSide2Finish,HOUSING_FINISH~hfHousingFinish,SPECIFIC_FINISH~hfSpecificFinish,GLASS_FINISH~hfGlassFinish,GLASS_FINISH_CODE~hfGlassFinishCode,NEW_MODEL_NUMBER~hfNewModelnumber,QUANTITY_OVER_PACK~hfQuantityoverpack,MSRP~hfMsrp,ECOMMERCE_PRICE~hfEcommercePrice,UPC~hfUPC,SPECIAL_ORDER_PRICE~hfSpecialOrderPrice,SUPPLIER_NUMBER~hfSupplierNumber,USERNAME~hfUsername";
					//propertyValueColorway=propcolorvalue.split(",");
					propertyValueColorway= LCSProperties.get("com.hf.wc.integration.fan.outbound.colorwayAttributeKeys").split(",");
	//				String propprodvalue ="NUMBER_OF_BLADES~hfNumberofBlades,BLADE_PITCH~hfBladePitchList,BLADES_INCLUDED~hfBladesIncluded,NUMBER_OF_BULBS~hfNumberofBulbs,BULBS_INCLUDED~hfBulbsIncluded,MARKETING_DESCRIPTION~hfMarketingDescription,DIM_A_ANGLED_CEILING_TO_BOTTOM_OF_FAN~hfDimAAngledCeilingtoBottomofFan,DIM_A_FLUSH_CEILING_TO_BOTTOM_OF_FAN~hfDimAFlushCeilingtoBottomofFan,DIM_A_STANDARD_CEILING_TO_BOTTOM_OF_FAN~hfDimAStandardCeilingtoBottomofFan,DIM_B_ANGLED_CEILING_TO_BOTTOM_OF_FAN_BLADE~hfDimBAngledCeilingtoBottomofFanBlade,DIM_B_FLUSH_CEILING_TO_BOTTOM_OF_FAN_BLADE~hfDimBFlushCeilingtoBottomofFanBlade,DIM_B_STANDARD_CEILING_TO_BOTTOM_OF_FAN_BLADE~hfDimBStandardCeilingtoBottomofFanBlade,DIM_C_STANDARD_CEILING_TO_BOTTOM_OF_CANOPY~hfDimCStandardCeilingtoBottomofCanopy,DIM_D_STANDARD_WIDTH_OF_FAN_BODY~hfDimDStandardWidthOfFanBody,DIM_E_STANDARD_CANOPY_WIDTH~hfDimEStandardCanopyWidth,DIM_F_ANGLED_CEILING_TO_BOTTOM_OF_LIGHT~hfDimFAngledCeilingtoBottomofLight,DIM_F_FLUSH_CEILING_TO_BOTTOM_OF_LIGHT~hfDimFFlushCeilingtoBottomofLight,DIM_F_STANDARD_CEILING_TO_BOTTOM_OF_LIGHT~hfDimFStandardCeilingtoBottomofLight,DIM_G_STANDARD_HEIGHT_OF_MOTOR_HOUSING~hfDimGStandardHeightofMotorHousing,INCLUDED_DOWNROD_LENGTH~hfIncludedDownrodLength,SECOND_INCLUDED_DOWNROD_LENGTH~hfSecondIncludedDownrodLength,THIRD_INCLUDED_DOWNROD_LENGTH~hfThirdIncludedDownrodLength,REVERSABLE_MOTOR~hfReversableMotor,IMAP~hfIMAP,LEVEL_2_BRAND~hfLevel2Brand,LEVEL_4_FAMILY_NAME~hfLevel4familyname,LOCATION_USAGE~hfLocationUsage,MARKETING_GROUP~hfMarketingGroup,PRODUCT_CLASS~hfProductClass,PULL_CHAIN_INCLUDED~hfPullChainIncluded,RECALL~hfRecall,SIZE~hfSize,VOLTS_HZ~hfVoltsHz,REVERSIBLE_BLADES~hfReversibleBlades,CAN_BE_USED_WITHOUT_LIGHT_KIT~hfCanbeusedwithoutLightKit,AIRFLOW~hfAirflow,AIRFLOW_EFFIENCY~hfAirflowEffiency,ESTIMATEDYEARLYENERGYCOST~hfEstimatedYearlyEnergyCost,ENERGY_USE~hfEnergyUse,VELOCITY~hfVelocityFanPerformanceinMPH,SHIPPING_LENGTH~hfShippingLengthin,SHIPPING_HEIGHT~hfShippingHeightin,SHIPPING_CUBIC_FEET~hfShippingCubicFeet,SHIPPING_WEIGHT~hfShippingWeightLbs,SHIPPING_WIDTH~hfShippingWidthin,NEW_WEB_ROOM~hfnewWebRoom,SIX_SQUARE~hf6squares,STYLE_PYRAMID~hfStylePyramid,FILM_SIZE_H~hfFilmSizeHmm,FILM_SIZE_L~hfFilmSizeLmm,FILM_SIZE_W~hfFilmSizeWmm,CURRENT_SHIP_PACK_QTY~hfCurrentShipPackQty,BASE_TYPE~hfBaseType,CONTROL_TYPE_INCLUDED~hfControlTypeIncluded,LIGHT_KIT_INCLUDED~hfLightKitIncluded,MOTOR_TYPE~hfMotorType,MOUNTING_OPTIONS_FRIENDLY_VALUE~hfMountingOptionsFriendlyValue,NOLIGHT_CAP_INCLUDED~hfNoLightCapIncluded,LEVEL_1_PRODUCT_CATEGORY~hfLevel1ProductCategory,RECEIVER_LOCATION~hfReceiverLocation,STAINLESS_STEEL_HARDWARE~hfStainlessSteelHardware,WEB_COLLECTION~hfWebCollection,DIMMABLE_BULBS~hfDimmableBulbs,WATTS_PER_BULB~hfWattsperBulb,RECEIVER_INCLUDED~hfReceiverIncluded,BUSINESS_UNIT~hfBusinessUnit,CANOPY_TYPE~hfCanopyType,LEVEL_6_SPEEDS~hfLevel6Speeds,LEVEL_7_BULB_TYPE~hfLevel7BulbType,LEVEL_7_LIGHT_CONTROLS~hfLevel7LightControls,LEVEL_7_MATERIAL~hfLevel7Material,LEVEL_8_BLADE_LENGTH~hfLevel8BladeLength,LEVEL_8_LIGHT_KIT_TYPE~hfLevel8LightKitType,SIMPLECONNECT~hfSIMPLEconnect,SPEEDS~hfSpeed,WEB_COLLECTION_CODE~hfWebCollectCode,LEVEL3_SUBCATEGORY~hfLevel3SubCategory,ENERGY_GUIDE~hfEnergyGuides,INSTALLATION_GUIDE~hfInstallation,NEW_WEB_FAMILY_CODE~hfNewWebFamily,NEW_FAMILY_NAME~hfNewName,BULB_TYPE~hfBulbType,INCLUDED_CONTROL_FRIENDLY_VALUE~hfIncludeFriendlyValue,LEVEL_6_ROOM_SIZE~hfLevel6RoomSize,LED_LIGHT_KIT~hfLEDLightKit,ULTRA_LOW_PROFILE~hfUltraLowProfile,WARRANTY~hfWarranty,BATTERIES_INCLUDED~hfBatteriesIncluded,NUMBER_OF_BATTERIES~hfNumberofBatteries,TYPE_OF_BATTERIES~hfTypeofBatteries,BATTERY_SIZE~hfBatterySize,POPLOCK_BLADES~hfPoplockBlades,SUBCLASS~hfSubClass,UPLIGHT_BULB_BASE~hfUplightBulbBase,UPLIGHT_QTY~hfUplightQty,UPLIGHT_WATTS~hfUplightWatts,CORRELATED_COLOR_TEMPERATURE~hfCorrelatedColorTemperature,COLOR_RENDERING_INDEX~hfColorRenderingIndex,LIFETIME_EXPECTATION~hfLifetimeExpectation,CONTROL_ENGINEERING_PART~hfControlEngineeringPart,DRAWING_NUMBER~hfDrawingNumber,DIPS_ON_RECEIVER~hfDipsonReceiver,UPLIGHT_DOWNLIGHT_SEPARATE_CONTROL~UplightDownlightSeperateControl,FIVE_MINUTE_FAN~hfFiveMinuteFan,SOFTLIGHTS~hfSoftlights,LISTING_AGENCY~hfListingAgency,BLADE_MATERIAL~hfBladeMaterial,RECEIVER_NUMBER~hfReceivernumber,TRANSMITTER_NUMBER~hfTransmitternumber,TWIST_LOCK~hfTwistLock,SIMILAR_TO_~hfSimilar,TIER_LAUNCH_TYPE~hfTierLaunch,CFM_HIGH~hfCFMHigh,CFM_WATT_EXTRA_LOW~hfCFMWattExtraLow,CFM_WATT_HIGH~hfCFMWattHigh,CFM_WATT_LOW~hfCFMWattLow,CFM_WATT_MEDIUM~hfCFMWattMedium,CFM_WATT_MEDIUM_HIGH~hfCFMWattMediumHigh,CFM_WATT_MEDIUM_LOW~hfCFMWattMediumLow,WATTS_EXTRA_LOW~hfWattsExtraLow,WATTS_HIGH~hfWattsHigh,WATTS_LOW~hfWattsLow,WATTS_MEDIUM~hfWattsMedium,WATTS_MEDIUM_HIGH~hfWattsMediumHigh,WATTS_MEDIUM_LOW~hfWattsMediumLow,LIGHT_SPEC~hfLightSpec,ID_NUMBER~hfIDNumber,EASY_INSTALL~hfEasyInstall,ROOM_TYPE~hfRoomTypePrd,RECEIVER_TYPE~hfReceiverType";
					
					//propertyValueProduct=propprodvalue.split(",");
					propertyValueProduct= LCSProperties.get("com.hf.wc.integration.fan.outbound.productAttributeKeys").split(",");
				}
			
}
		} catch (WTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * pushProdColorwayUpdatedData.
	 * @param prodColorwayMap for prodColorwayMap
	 * @throws WTException 
	 * @throws WTPropertyVetoException 
	 * 
	 */	
	@SuppressWarnings("null")
	public static void pushProdColorwayUpdatedData(String prodColorwayMap) throws WTException, WTPropertyVetoException  {
		System.out.println("Inside Queue12345------------------------");
		HFLogFileGenerator.configureLog("Outbound");	
		loggerObject.info("Executing pushprodColorwayUpdatedData from Queue");
		loggerObject.info(":::::::::prodColorwayMap::size:::::::::::"+prodColorwayMap);
		//LCSPartMaster skuMasterObj;
		//String skuMasterID=null;
		LCSSKU productColorwayObj = null ;
		//String propertyValue[] = null;

		Collection colorwayAttributeKeys = new ArrayList<String>();
		Collection productAttributeKeys= new ArrayList<String>();
		Collection allAttKey= new ArrayList<String>();
		Map columnName = new HashMap();
		//String requestType = (String) prodColorwayMap.get("reqEvent");
		String colorwayId = prodColorwayMap;
		//try{
			productColorwayObj = (LCSSKU) LCSQuery.findObjectById(colorwayId);
			LCSSKU prodColorwayLatestIteration = (LCSSKU) VersionHelper.latestIterationOf(productColorwayObj);
			loggerObject.info("prodColorwayLatestIteration ================"+prodColorwayLatestIteration);
			loggerObject.info("prodColorwayLatestIteration ================"+prodColorwayLatestIteration);
			productColorwayObj = SeasonProductLocator.getSKUARev(prodColorwayLatestIteration);
			// Modified timestamp attrib
			
			
			loggerObject.info("################12345LatestproductColorwayObj----!!!!!" + productColorwayObj);
			// Fetch product object
			LCSProduct prodObj = (LCSProduct)productColorwayObj.getProduct();
			//populatePropertyValues(productColorwayObj);
			try {
				//String property ="ITEM_STATUS~hfItemStatuss,ITEM_NUMBER~hfItemNumberStr,MODEL_NUMBER~hfModelNumberCatalogNumber,BASE_RETAIL~hfBaseRetail,ABC_CLASSIFICATION~hfABCClassification,BUYER_NUMBER~hfBuyerNumber,COUNTRY_OF_ORIGIN~hfCountryofOrigin,DEMAND_TIME_FENCE_DAYS_BYHALIA~hfDemandTimeFenceDaysByhalia,DEMAND_TIME_FENCE_DAYS_SUPPLIER~hfDemandTimeFenceDaysSupplier,FIXED_DAYS_OF_SUPPLY~hfFixedDaysofSupply,FIXED_LOT_MULTIPLIER~hfFixedLotMultiplier,FIXED_ORDER_QUANTITY~hfFixedOrderQuantity,FULL_LEAD_TIME~hfFullLeadTime,INTRANSIT_LEAD_TIME~hfIntransitLeadTime,MAXIMUM_ORDER_QTY~hfMaximumOrderQty,MINIMUM_ORDER_QTY~hfMinimumOrderQty,MRP_SAFTEYSTOCK_DAYS~hfMRPSafteyStockDays,NOTE_CUSTOMER_SUPPORT~hfNoteCustomerSupport,NOTE_MATERIALS~hfNoteMaterials,PLANNER_NUMBER~hfPlannerNumber,PLANNING_MAKE_BUY_CODE~hfPlanningMakeBuyCode,PLANNINGTIME_FENCE_DAYS_BYHALIA~hfPlanningTimeFenceDaysByhalia,PLANNING_TIME_FENCE_DAYS_SUPPLIER~hfPlanningTimeFenceDaysSupplier,PRIMARY_SUPPLIER~hfPrimarySupplierFactory,QUANTITY_MASTER_PACK~hfQuantityMasterPack,SAFETY_STOCK_CODE~hfSafetyStockCode,SAFETYSTOCK_PERCENT~hfSafetyStockPercent,SAFETYSTOCK_QUANTITY~hfSafetyStockQuantity,SUPPLIER_LEAD_TIME~hfSupplierLeadTime,TARIFF_CODE~hfTariffCode,UOM~hfUOM,DEMAND_TIME_FENCE_DAYS_CHINA_WAREHOUSE~hfDemandTimeFenceDaysChinaWarehouse,PLANNING_TIME_FENCE_DAYS_CHINA_WAREHOUSE~hfPlanningTimeFenceDaysChinaWarehouse,INTRANSIT_LEAD_TIME_SUPPLIER~hfIntransitLeadTimeSupplier,INTRANSIT_LEAD_TIME_CHINA_WAREHOUSE~hfIntransitLeadTimeChinaWarehouse,SUPPLIER_LEAD_TIME_SUPPLIER~hfSupplierLeadTimeSupplier,SUPPLIER_LEAD_TIME_CHINA_WAREHOUSE~hfSupplierLeadTimeChinaWarehouse,SAFETY_STOCK_CODE_SUPPLIER~hfSafetyStockCodeSupplier,SAFETY_STOCK_CODE_CHINA_WAREHOUSE~hfSafetyStockCodeChinaWarehouse,SAFETYSTOCK_PERCENT_SUPPLIER~hfSafetyStockPercentSupplier,SAFETYSTOCK_PERCENT_CHINA_WAREHOUSE~hfSafetyStockPercentChinaWarehouse,SAFETYSTOCK_QUANTITY_SUPPLIER~hfSafetyStockQuantitySupplier,SAFETYSTOCK_QUANTITY_CHINA_WAREHOUSE~hfSafetyStockQuantityChinaWarehouse,MINIMUM_ORDER_QTY_SUPPLIER~hfMinimumOrderQtySupplier,MINIMUM_ORDER_QTY_CHINA_WAREHOUSE~hfMinimumOrderQtyChinaWarehouse,ABC_CLASSIFICATION_SUPPLIER~hfABCClassificationSupplier,ABC_CLASSIFICATION_CHINA_WAREHOUSE~hfABCClassificationChinaWarehouse,ABC_CLASSIFICATION_CORPORATE~hfABCClassificationCorporate,JDE_DESCRIPTION~hfJDEDescription,OLD_MODEL_NUMBER~hfOldModelNumber,SAFETY_STOCK_CODE_DESCRIPTION~hfSafetyCodeStockDescription,SAFETY_STOCK_CODE_VALUE~hfSafetyStockCodeValue,OLD_ITEM_NUMBER~hfOLDItemNumber,DISTRIBUTOR_NET_PRICE~hfDistributorNetPrice,LEVEL_5_FINISH_CATEGORY~hfLevel5Category,LEVEL_6_GLASS_FINISH_DESCRIPTION~hfLevel6GlassFinishDescription,SG1_ESTIMATED_12_MO_VOLUME~hfSG1Estimated12moVolume,SG2_ESTIMATED_12_MO_VOLUME~hfSG2Estimated12moVolume,LEVEL_6_HOLE_PATTERN_DESCRIPTION~hfLevel6HolePatterDescription,FCC_ID~hfFCCID,LEVEL_8_REVERSING~hfLevel8Reversing,ASSOCIATED_MODELS_FOR_PARTS~hfAssociatedModelsforparts,LIGHT_OUTPUT~hfLightOutput,COMMENTS_MARKETING~hfCommentsMarketing,COMMENTS_TECH_SUPPORT~hfCommentsTechSupport,BLADE_SIDE_1_FINISH~hfBladeSide1Finish,BLADE_SIDE_2_FINISH~hfBladeSide2Finish,HOUSING_FINISH~hfHousingFinish,SPECIFIC_FINISH~hfSpecificFinish,GLASS_FINISH~hfGlassFinish,GLASS_FINISH_CODE~hfGlassFinishCode,NEW_MODEL_NUMBER~hfNewModelnumber,QUANTITY_OVER_PACK~hfQuantityoverpack,MSRP~hfMsrp,ECOMMERCE_PRICE~hfEcommercePrice,SPECIAL_ORDER_PRICE~hfSpecialOrderPrice,UPC~hfUPC,SUPPLIER_NUMBER~hfSupplierNumber,USERNAME~hfUsername";
				//String propprod ="MARKETING_DESCRIPTION~hfMarketingDescription,IMAP~hfIMAP,LEVEL_2_BRAND~hfLevel2Brand,LOCATION_USAGE~hfLocationUsage,MARKETING_GROUP~hfMarketingGroup,PRODUCT_CLASS~hfProductClass,RECALL~hfRecall,SHIPPING_LENGTH~hfShippingLengthin,SHIPPING_HEIGHT~hfShippingHeightin,SHIPPING_CUBIC_FEET~hfShippingCubicFeet,SHIPPING_WEIGHT~hfShippingWeightLbs,SHIPPING_WIDTH~hfShippingWidthin,FILM_SIZE_H~hfFilmSizeHmm,FILM_SIZE_L~hfFilmSizeLmm,FILM_SIZE_W~hfFilmSizeWmm,CURRENT_SHIP_PACK_QTY~hfCurrentShipPackQty,LEVEL_1_PRODUCT_CATEGORY~hfLevel1ProductCategory,BUSINESS_UNIT~hfBusinessUnit,WARRANTY~hfWarranty,SUBCLASS~hfSubClass,CONTROL_ENGINEERING_PART~hfControlEngineeringPart,DRAWING_NUMBER~hfDrawingNumber,LISTING_AGENCY~hfListingAgency,RECEIVER_NUMBER~hfReceivernumber,TRANSMITTER_NUMBER~hfTransmitternumber,ID_NUMBER~hfIDNumber,EASY_INSTALL~hfEasyInstall";
				loggerObject.info("################# 12345Inside Slasify Outbound  integration #################");
				productColorwayObj.setValue(HFIntegrationConstants.COLORWAY_INTEGRATION_STATUS,"Processing");
				
				if (VersionHelper.isCheckedOut(productColorwayObj)) {
					productColorwayObj.setValue(HFIntegrationConstants.COLORWAY_INTEGRATION_STATUS,"Processing");
					productColorwayObj= (LCSSKU) VersionHelper.checkin(productColorwayObj);
				}
				if (!VersionHelper.isCheckedOut(productColorwayObj)){
					WorkInProgressHelper.service.checkout((Workable)productColorwayObj,WorkInProgressHelper.service.getCheckoutFolder(),"").getWorkingCopy();
					productColorwayObj = (LCSSKU)VersionHelper.latestIterationOf(productColorwayObj.getMaster());               
					productColorwayObj.setValue(HFIntegrationConstants.COLORWAY_INTEGRATION_STATUS,"Processing");
					productColorwayObj= (LCSSKU) VersionHelper.checkin(productColorwayObj);
				}
					loggerObject.info(">>type>>>>:::::::::::::::"+productColorwayObj.getFlexType().getTypeName());
					String ftIntName = productColorwayObj.getFlexType().getTypeName();
					
					if("hfServiceParts".equalsIgnoreCase(ftIntName)){
						isPart = "true";
						loggerObject.info("::::::12345:::::::::::::entering servicepart for outbound::::::::::isPart:::::::::" + isPart);
						propertyValueColorway= LCSProperties.get("com.hf.wc.integration.part.outbound.colorwayAttributeKeys").split(",");
						propertyValueProduct= LCSProperties.get("com.hf.wc.integration.part.outbound.productAttributeKeys").split(",");
						//propertyValueColorway= property.split(",");
						//propertyValueProduct= propprod.split(",");
					}
					else{
						isPart = "false";
						loggerObject.info("::::::::::::::::::12345:entering fans for outbound::::::::isPart:::::::::::"+isPart);
						//String propcolorvalue="ITEM_STATUS~hfItemStatuss,ITEM_NUMBER~hfItemNumberStr,MODEL_NUMBER~hfModelNumberCatalogNumber,BASE_RETAIL~hfBaseRetail,ABC_CLASSIFICATION~hfABCClassification,BUYER_NUMBER~hfBuyerNumber,COUNTRY_OF_ORIGIN~hfCountryofOrigin,DEMAND_TIME_FENCE_DAYS_BYHALIA~hfDemandTimeFenceDaysByhalia,DEMAND_TIME_FENCE_DAYS_SUPPLIER~hfDemandTimeFenceDaysSupplier,FIXED_DAYS_OF_SUPPLY~hfFixedDaysofSupply,FIXED_LOT_MULTIPLIER~hfFixedLotMultiplier,FIXED_ORDER_QUANTITY~hfFixedOrderQuantity,FULL_LEAD_TIME~hfFullLeadTime,INTRANSIT_LEAD_TIME~hfIntransitLeadTime,MAXIMUM_ORDER_QTY~hfMaximumOrderQty,MINIMUM_ORDER_QTY~hfMinimumOrderQty,MRP_SAFTEYSTOCK_DAYS~hfMRPSafteyStockDays,NOTE_CUSTOMER_SUPPORT~hfNoteCustomerSupport,NOTE_MATERIALS~hfNoteMaterials,PLANNER_NUMBER~hfPlannerNumber,PLANNING_MAKE_BUY_CODE~hfPlanningMakeBuyCode,PLANNINGTIME_FENCE_DAYS_BYHALIA~hfPlanningTimeFenceDaysByhalia,PLANNING_TIME_FENCE_DAYS_SUPPLIER~hfPlanningTimeFenceDaysSupplier,PRIMARY_SUPPLIER~hfPrimarySupplierFactory,QUANTITY_MASTER_PACK~hfQuantityMasterPack,SAFETY_STOCK_CODE~hfSafetyStockCode,SAFETYSTOCK_PERCENT~hfSafetyStockPercent,SAFETYSTOCK_QUANTITY~hfSafetyStockQuantity,SUPPLIER_LEAD_TIME~hfSupplierLeadTime,TARIFF_CODE~hfTariffCode,UOM~hfUOM,DEMAND_TIME_FENCE_DAYS_CHINA_WAREHOUSE~hfDemandTimeFenceDaysChinaWarehouse,PLANNING_TIME_FENCE_DAYS_CHINA_WAREHOUSE~hfPlanningTimeFenceDaysChinaWarehouse,INTRANSIT_LEAD_TIME_SUPPLIER~hfIntransitLeadTimeSupplier,INTRANSIT_LEAD_TIME_CHINA_WAREHOUSE~hfIntransitLeadTimeChinaWarehouse,SUPPLIER_LEAD_TIME_SUPPLIER~hfSupplierLeadTimeSupplier,SUPPLIER_LEAD_TIME_CHINA_WAREHOUSE~hfSupplierLeadTimeChinaWarehouse,SAFETY_STOCK_CODE_SUPPLIER~hfSafetyStockCodeSupplier,SAFETY_STOCK_CODE_CHINA_WAREHOUSE~hfSafetyStockCodeChinaWarehouse,SAFETYSTOCK_PERCENT_SUPPLIER~hfSafetyStockPercentSupplier,SAFETYSTOCK_PERCENT_CHINA_WAREHOUSE~hfSafetyStockPercentChinaWarehouse,SAFETYSTOCK_QUANTITY_SUPPLIER~hfSafetyStockQuantitySupplier,SAFETYSTOCK_QUANTITY_CHINA_WAREHOUSE~hfSafetyStockQuantityChinaWarehouse,MINIMUM_ORDER_QTY_SUPPLIER~hfMinimumOrderQtySupplier,MINIMUM_ORDER_QTY_CHINA_WAREHOUSE~hfMinimumOrderQtyChinaWarehouse,ABC_CLASSIFICATION_SUPPLIER~hfABCClassificationSupplier,ABC_CLASSIFICATION_CHINA_WAREHOUSE~hfABCClassificationChinaWarehouse,ABC_CLASSIFICATION_CORPORATE~hfABCClassificationCorporate,JDE_DESCRIPTION~hfJDEDescription,OLD_MODEL_NUMBER~hfOldModelNumber,SAFETY_STOCK_CODE_DESCRIPTION~hfSafetyCodeStockDescription,SAFETY_STOCK_CODE_VALUE~hfSafetyStockCodeValue,OLD_ITEM_NUMBER~hfOLDItemNumber,DISTRIBUTOR_NET_PRICE~hfDistributorNetPrice,LEVEL_5_FINISH_CATEGORY~hfLevel5Category,LEVEL_6_GLASS_FINISH_DESCRIPTION~hfLevel6GlassFinishDescription,SG1_ESTIMATED_12_MO_VOLUME~hfSG1Estimated12moVolume,SG2_ESTIMATED_12_MO_VOLUME~hfSG2Estimated12moVolume,LEVEL_6_HOLE_PATTERN_DESCRIPTION~hfLevel6HolePatterDescription,FCC_ID~hfFCCID,LEVEL_8_REVERSING~hfLevel8Reversing,ASSOCIATED_MODELS_FOR_PARTS~hfAssociatedModelsforparts,LIGHT_OUTPUT~hfLightOutput,COMMENTS_MARKETING~hfCommentsMarketing,COMMENTS_TECH_SUPPORT~hfCommentsTechSupport,BLADE_SIDE_1_FINISH~hfBladeSide1Finish,BLADE_SIDE_2_FINISH~hfBladeSide2Finish,HOUSING_FINISH~hfHousingFinish,SPECIFIC_FINISH~hfSpecificFinish,GLASS_FINISH~hfGlassFinish,GLASS_FINISH_CODE~hfGlassFinishCode,NEW_MODEL_NUMBER~hfNewModelnumber,QUANTITY_OVER_PACK~hfQuantityoverpack,MSRP~hfMsrp,ECOMMERCE_PRICE~hfEcommercePrice,UPC~hfUPC,SPECIAL_ORDER_PRICE~hfSpecialOrderPrice,SUPPLIER_NUMBER~hfSupplierNumber,USERNAME~hfUsername";
						//propertyValueColorway=propcolorvalue.split(",");
						propertyValueColorway= LCSProperties.get("com.hf.wc.integration.fan.outbound.colorwayAttributeKeys").split(",");
	//					String propprodvalue ="NUMBER_OF_BLADES~hfNumberofBlades,BLADE_PITCH~hfBladePitchList,BLADES_INCLUDED~hfBladesIncluded,NUMBER_OF_BULBS~hfNumberofBulbs,BULBS_INCLUDED~hfBulbsIncluded,MARKETING_DESCRIPTION~hfMarketingDescription,DIM_A_ANGLED_CEILING_TO_BOTTOM_OF_FAN~hfDimAAngledCeilingtoBottomofFan,DIM_A_FLUSH_CEILING_TO_BOTTOM_OF_FAN~hfDimAFlushCeilingtoBottomofFan,DIM_A_STANDARD_CEILING_TO_BOTTOM_OF_FAN~hfDimAStandardCeilingtoBottomofFan,DIM_B_ANGLED_CEILING_TO_BOTTOM_OF_FAN_BLADE~hfDimBAngledCeilingtoBottomofFanBlade,DIM_B_FLUSH_CEILING_TO_BOTTOM_OF_FAN_BLADE~hfDimBFlushCeilingtoBottomofFanBlade,DIM_B_STANDARD_CEILING_TO_BOTTOM_OF_FAN_BLADE~hfDimBStandardCeilingtoBottomofFanBlade,DIM_C_STANDARD_CEILING_TO_BOTTOM_OF_CANOPY~hfDimCStandardCeilingtoBottomofCanopy,DIM_D_STANDARD_WIDTH_OF_FAN_BODY~hfDimDStandardWidthOfFanBody,DIM_E_STANDARD_CANOPY_WIDTH~hfDimEStandardCanopyWidth,DIM_F_ANGLED_CEILING_TO_BOTTOM_OF_LIGHT~hfDimFAngledCeilingtoBottomofLight,DIM_F_FLUSH_CEILING_TO_BOTTOM_OF_LIGHT~hfDimFFlushCeilingtoBottomofLight,DIM_F_STANDARD_CEILING_TO_BOTTOM_OF_LIGHT~hfDimFStandardCeilingtoBottomofLight,DIM_G_STANDARD_HEIGHT_OF_MOTOR_HOUSING~hfDimGStandardHeightofMotorHousing,INCLUDED_DOWNROD_LENGTH~hfIncludedDownrodLength,SECOND_INCLUDED_DOWNROD_LENGTH~hfSecondIncludedDownrodLength,THIRD_INCLUDED_DOWNROD_LENGTH~hfThirdIncludedDownrodLength,REVERSABLE_MOTOR~hfReversableMotor,IMAP~hfIMAP,LEVEL_2_BRAND~hfLevel2Brand,LEVEL_4_FAMILY_NAME~hfLevel4familyname,LOCATION_USAGE~hfLocationUsage,MARKETING_GROUP~hfMarketingGroup,PRODUCT_CLASS~hfProductClass,PULL_CHAIN_INCLUDED~hfPullChainIncluded,RECALL~hfRecall,SIZE~hfSize,VOLTS_HZ~hfVoltsHz,REVERSIBLE_BLADES~hfReversibleBlades,CAN_BE_USED_WITHOUT_LIGHT_KIT~hfCanbeusedwithoutLightKit,AIRFLOW~hfAirflow,AIRFLOW_EFFIENCY~hfAirflowEffiency,ESTIMATEDYEARLYENERGYCOST~hfEstimatedYearlyEnergyCost,ENERGY_USE~hfEnergyUse,VELOCITY~hfVelocityFanPerformanceinMPH,SHIPPING_LENGTH~hfShippingLengthin,SHIPPING_HEIGHT~hfShippingHeightin,SHIPPING_CUBIC_FEET~hfShippingCubicFeet,SHIPPING_WEIGHT~hfShippingWeightLbs,SHIPPING_WIDTH~hfShippingWidthin,NEW_WEB_ROOM~hfnewWebRoom,SIX_SQUARE~hf6squares,STYLE_PYRAMID~hfStylePyramid,FILM_SIZE_H~hfFilmSizeHmm,FILM_SIZE_L~hfFilmSizeLmm,FILM_SIZE_W~hfFilmSizeWmm,CURRENT_SHIP_PACK_QTY~hfCurrentShipPackQty,BASE_TYPE~hfBaseType,CONTROL_TYPE_INCLUDED~hfControlTypeIncluded,LIGHT_KIT_INCLUDED~hfLightKitIncluded,MOTOR_TYPE~hfMotorType,MOUNTING_OPTIONS_FRIENDLY_VALUE~hfMountingOptionsFriendlyValue,NOLIGHT_CAP_INCLUDED~hfNoLightCapIncluded,LEVEL_1_PRODUCT_CATEGORY~hfLevel1ProductCategory,RECEIVER_LOCATION~hfReceiverLocation,STAINLESS_STEEL_HARDWARE~hfStainlessSteelHardware,WEB_COLLECTION~hfWebCollection,DIMMABLE_BULBS~hfDimmableBulbs,WATTS_PER_BULB~hfWattsperBulb,RECEIVER_INCLUDED~hfReceiverIncluded,BUSINESS_UNIT~hfBusinessUnit,CANOPY_TYPE~hfCanopyType,LEVEL_6_SPEEDS~hfLevel6Speeds,LEVEL_7_BULB_TYPE~hfLevel7BulbType,LEVEL_7_LIGHT_CONTROLS~hfLevel7LightControls,LEVEL_7_MATERIAL~hfLevel7Material,LEVEL_8_BLADE_LENGTH~hfLevel8BladeLength,LEVEL_8_LIGHT_KIT_TYPE~hfLevel8LightKitType,SIMPLECONNECT~hfSIMPLEconnect,SPEEDS~hfSpeed,WEB_COLLECTION_CODE~hfWebCollectCode,LEVEL3_SUBCATEGORY~hfLevel3SubCategory,ENERGY_GUIDE~hfEnergyGuides,INSTALLATION_GUIDE~hfInstallation,NEW_WEB_FAMILY_CODE~hfNewWebFamily,NEW_FAMILY_NAME~hfNewName,BULB_TYPE~hfBulbType,INCLUDED_CONTROL_FRIENDLY_VALUE~hfIncludeFriendlyValue,LEVEL_6_ROOM_SIZE~hfLevel6RoomSize,LED_LIGHT_KIT~hfLEDLightKit,ULTRA_LOW_PROFILE~hfUltraLowProfile,WARRANTY~hfWarranty,BATTERIES_INCLUDED~hfBatteriesIncluded,NUMBER_OF_BATTERIES~hfNumberofBatteries,TYPE_OF_BATTERIES~hfTypeofBatteries,BATTERY_SIZE~hfBatterySize,POPLOCK_BLADES~hfPoplockBlades,SUBCLASS~hfSubClass,UPLIGHT_BULB_BASE~hfUplightBulbBase,UPLIGHT_QTY~hfUplightQty,UPLIGHT_WATTS~hfUplightWatts,CORRELATED_COLOR_TEMPERATURE~hfCorrelatedColorTemperature,COLOR_RENDERING_INDEX~hfColorRenderingIndex,LIFETIME_EXPECTATION~hfLifetimeExpectation,CONTROL_ENGINEERING_PART~hfControlEngineeringPart,DRAWING_NUMBER~hfDrawingNumber,DIPS_ON_RECEIVER~hfDipsonReceiver,UPLIGHT_DOWNLIGHT_SEPARATE_CONTROL~UplightDownlightSeperateControl,FIVE_MINUTE_FAN~hfFiveMinuteFan,SOFTLIGHTS~hfSoftlights,LISTING_AGENCY~hfListingAgency,BLADE_MATERIAL~hfBladeMaterial,RECEIVER_NUMBER~hfReceivernumber,TRANSMITTER_NUMBER~hfTransmitternumber,TWIST_LOCK~hfTwistLock,SIMILAR_TO_~hfSimilar,TIER_LAUNCH_TYPE~hfTierLaunch,CFM_HIGH~hfCFMHigh,CFM_WATT_EXTRA_LOW~hfCFMWattExtraLow,CFM_WATT_HIGH~hfCFMWattHigh,CFM_WATT_LOW~hfCFMWattLow,CFM_WATT_MEDIUM~hfCFMWattMedium,CFM_WATT_MEDIUM_HIGH~hfCFMWattMediumHigh,CFM_WATT_MEDIUM_LOW~hfCFMWattMediumLow,WATTS_EXTRA_LOW~hfWattsExtraLow,WATTS_HIGH~hfWattsHigh,WATTS_LOW~hfWattsLow,WATTS_MEDIUM~hfWattsMedium,WATTS_MEDIUM_HIGH~hfWattsMediumHigh,WATTS_MEDIUM_LOW~hfWattsMediumLow,LIGHT_SPEC~hfLightSpec,ID_NUMBER~hfIDNumber,EASY_INSTALL~hfEasyInstall,ROOM_TYPE~hfRoomTypePrd,RECEIVER_TYPE~hfReceiverType";
						
					//	propertyValueProduct=propprodvalue.split(",");
						propertyValueProduct= LCSProperties.get("com.hf.wc.integration.fan.outbound.productAttributeKeys").split(",");
					}
				
	
			} catch (WTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//FlexType flexType = prodObj.getFlexType();
			//propertyValue= LCSProperties.get("com.hf.wc.integration.salsify.outbound.colorwayAttributeKeys").split(",");
			loggerObject.info(":::::::::::::propertyValueColorway.length::"+propertyValueColorway);
			if(propertyValueColorway.length > 0)
			{
			for(String pd:propertyValueColorway){
				loggerObject.info("::::::colorway::::::::::pd:::"+ pd);
				String[] key = pd.split("~");
				if("true".equalsIgnoreCase(isPart) && "ITEM_NUMBER".equals(key[0]))
				{
				columnName.put("skuName",key[0]);
				colorwayAttributeKeys.add("skuName");
				}
				
				else
				{
				columnName.put(key[1],key[0]);
				colorwayAttributeKeys.add(key[1]);
				}
				
			}
			//columnName.put("modts","MODIFIED_DATE");
	}
	
			
			
			//>>>>>>>>>>>>uncomment it later
			//propertyValue = LCSProperties.get("com.hf.wc.integration.salsify.outbound.productAttributeKeys").split(",");
			
			loggerObject.info(":::::::::::::propertyValueProduct.length::"+propertyValueProduct.length);
			if(propertyValueProduct.length > 0)
			{
			for(String pd:propertyValueProduct ){
				loggerObject.info("::::::product::::::::::pd:::"+ pd);
				String[] key = pd.split("~");
				productAttributeKeys.add(key[1]);
				columnName.put(key[1],key[0]);
				}
			}
			allAttKey.addAll(colorwayAttributeKeys);
			allAttKey.addAll(productAttributeKeys);
			
			loggerObject.info("################Colorway Attributes to be fetched----!!!!!" + colorwayAttributeKeys);
			loggerObject.info("################Prooduct Attributes to be fetched----!!!!!" + productAttributeKeys);
			loggerObject.info(">>>>>>>>>Colorway Attributes to be fetched");
			Map skuDataMap = HFExtractAttsValues.processSKUAttKeys(productColorwayObj,colorwayAttributeKeys,isPart);
			Map productDataMap = HFExtractAttsValues.processProductAttKeys(prodObj, productAttributeKeys);
			Map dataForTable = new HashMap();
			//skuDataMap.put("modts",modTs);
			dataForTable.putAll(skuDataMap);
			dataForTable.putAll(productDataMap);
			//dataForTable.put("MODIFIED_DATE",modTs);
			
			//get productdata map by creating similar function called processproductAttKeys
			loggerObject.info("################Colorway Value fetched----!!!!!" + skuDataMap);
			loggerObject.info("################ Product Value Fetched------!!!!!"+productDataMap);
			loggerObject.info(">>>>>>>>>>>>Colorway Value fetched-");
			String tableName = HFIntegrationConstants.OUTBOUND_SALSIFY_STAGING_DB_TABLENAME;
			loggerObject.info("::::::value of isPart::::::before insertDataInTable::"+ isPart);
			status=HFPushDataToStagingTable.insertDataInTable(dataForTable,allAttKey,columnName,isPart,tableName);

			loggerObject.info("Execution completed from Queue:::::::"+status);
		//}
	/*catch(Exception wtExx){ //WTException
			loggerObject.error("Exception occured in pushing outbound data-->**"+ wtExx);
			//updateIntegrationStatus(wtExx.getMessage(),productColorwayObj.getValue("hfItemNumberStr").toString(),"Failure");
		}*/
		updateIntegrationStatus(productColorwayObj,status);
		
		loggerObject.info("::::colorway object we are dealing with::"+productColorwayObj.getName());
		String heirarchy = productColorwayObj.getFlexType().getFullName();
		if(!heirarchy.contains("hfServiceParts")){
		LCSMOATable moaTableObj = (LCSMOATable) productColorwayObj.getValue("hfsbom");
		loggerObject.info("::::::::moaTableObj:::::" + moaTableObj);
		Collection moaObjects = moaTableObj.getRows();
		loggerObject.info("::::::::::::::::moaObjects:"+moaTableObj);
		Iterator itr = moaObjects.iterator();
		while(itr.hasNext()){
			FlexObject flexObj = (FlexObject)itr.next();
			String moaObjid = flexObj.getString("oid");
			loggerObject.info("::::::::::moaObjid:::"+moaObjid);
			LCSMOAObject moaObj = (LCSMOAObject)LCSQuery.findObjectById("OR:com.lcs.wc.moa.LCSMOAObject:"+moaObjid);
			HFProductColorwayOutbound.sendSBOMMOAData(moaObj,productColorwayObj);
		}
		loggerObject.info(">>>>>>>>>>>>>>>>>>>>>"+status);
		}
	}
	public static void updateIntegrationStatus(LCSSKU itemNumber,String status) throws WTException, WTPropertyVetoException {

		loggerObject.info("Inside update integration status method");
		LCSSKU colorwayobj = (LCSSKU) itemNumber;
		loggerObject.info(":::::::::::::::::::::::::colorwayobj" + colorwayobj);
		loggerObject.info(":::::::::::::::::::::::::status" + status);
		colorwayobj=(LCSSKU) VersionHelper.checkout(colorwayobj);
		if("failure".equalsIgnoreCase(status)){
			colorwayobj.setValue(HFIntegrationConstants.OUTBOUND_SEND_TO_STAGING_DB,false);
			colorwayobj.setValue(HFIntegrationConstants.COLORWAY_INTEGRATION_STATUS,status);
			//colorwayobj= (LCSSKU) LCSLogic.persist(colorwayobj, true);
			colorwayobj=(LCSSKU) VersionHelper.checkin(colorwayobj);
			//doubt>>>>>>>>>>>>>>>>>since plugin is on postpersist whether to persist colorwayobj or not
		}
		else{
			colorwayobj.setValue(HFIntegrationConstants.COLORWAY_INTEGRATION_STATUS,status);
			//loggerObject.info(":::::::::::::::::::::::::Attribute status Value" + colorwayobj.getValue("hfIntegrationStatus"));
			LCSLogic.persist(colorwayobj, true);
			colorwayobj=(LCSSKU) VersionHelper.checkin(colorwayobj);
		}
		loggerObject.info(">>>>>>>>>Inside updateIntegrationStatus");
	}


	/**
	 * @param moaObject
	 * @param skuObj
	 * @throws WTPropertyVetoException
	 * @throws WTException
	 */
	public static void sendSBOMMOAData(LCSMOAObject moaObject,LCSSKU skuObj) throws WTPropertyVetoException, WTException{
		
			String itemNumber=(String)skuObj.getValue(HFIntegrationConstants.COLORWAY_ITEM_NUMBER);
			loggerObject.info("::::::::::::itemNumber:::" + itemNumber);
			HFLogFileGenerator.configureLog("SBOM");	
			loggerObject.info("Executing pushSBOMMOAUpdatedData from Queue");
			String propertyValue[] = null;
			Collection moaAttributeKeys = new ArrayList<String>();
				Map columnName = new HashMap();

				try{
					loggerObject.info("################LatestMOAObj----!!!!!" + moaObject);
					propertyValue= LCSProperties.get("com.hf.wc.integration.sbom.outbound.sbomAttributes").split(",");
					for(String pd:propertyValue){
						String[] key = pd.split("~");
						columnName.put(key[1],key[0]);
						
						if(!key[1].equalsIgnoreCase(HFIntegrationConstants.COLORWAY_ITEM_NUMBER)){
						moaAttributeKeys.add(key[1]);
					}
						
					}
					
					// Adding constant coloumns name in map
					columnName.put(BOM_TYPE,BOM_TYPE);
					columnName.put(BRANCH_PLANT,BRANCH_PLANT);
					
					loggerObject.info("################moaAttributeKeys to be fetched----!!!!!" + moaAttributeKeys);
					Map moaDataMap = HFExtractAttsValues.processMOAAttKeys(moaObject,moaAttributeKeys);
					
					// Adding Constant attributes keys in collection
					moaAttributeKeys.add(HFIntegrationConstants.COLORWAY_ITEM_NUMBER);
					moaAttributeKeys.add(BOM_TYPE);
					moaAttributeKeys.add(BRANCH_PLANT);
					loggerObject.info(":::::::::::::::::moaDataMap::"+ moaDataMap); 
					
					//adding Constant values to data map
					moaDataMap.put(HFIntegrationConstants.COLORWAY_ITEM_NUMBER,itemNumber);
					moaDataMap.put(BOM_TYPE,"SPR");
					moaDataMap.put(BRANCH_PLANT,"650");
					
					loggerObject.info("################moa Value fetched----!!!!!" + moaDataMap);
					
					String tableName = HFIntegrationConstants.OUTBOUND_SBOM_STAGING_DB_TABLENAME;
					
					status=HFPushDataToStagingTable.insertSBOMMOADataInTable(moaDataMap, moaAttributeKeys,columnName,tableName);
					processPartFromSBOM(moaDataMap);
					loggerObject.info("Execution completed from Queue"+status);
					}catch(WTException wtExx){ //WTException
					loggerObject.error("Exception occured in pushing outbound data--> */*/*/*/*/"+ wtExx);
					
					}
		
	}
	public static void processPartFromSBOM(Map moaDataMap) throws WTPropertyVetoException, WTException{
		
		String partNumber=moaDataMap.get("hfmodel").toString();
		loggerObject.info("PROCESSING PART NUMBER"+partNumber);
		//Making colorway object based on part number(sku name)
		String colorwayID = HFInboundIntegrationHelper.getColorwayBasedOnID(partNumber,"skuName");
		LCSSKU partColorwayObj = (LCSSKU) LCSQuery.findObjectById("VR:com.lcs.wc.product.LCSSKU:"+colorwayID);
		sendProductColorwayData(partColorwayObj);
		
	}
}
