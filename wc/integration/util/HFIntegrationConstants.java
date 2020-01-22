package com.hf.wc.integration.util;

import com.lcs.wc.util.LCSProperties;

/**
 * HFIntegrationConstants.
 * Class that holds all attribute keys and constants.
 * @version 1.0
 */
public final class HFIntegrationConstants {

	/**
	 * Constructor for HFIntegrationConstants.
	 */
	private HFIntegrationConstants(){

	}



	//############################################################### Salsify Inbound ##########################################################################



	/**
	 * COLORWAY_ITEM_NUMBER for COLORWAY_ITEM_NUMBER
	 */
	public static final String COLORWAY_ITEM_NUMBER = LCSProperties.get("com.hf.wc.integration.salsify.common.ItemNumber","hfItemNumberStr");

	/**
	 * TO_EMAIL_ADDRESS for TO_EMAIL_ADDRESS.
	 */
	public static final String TO_EMAIL_ADDRESS = LCSProperties.get("com.cna.integration.outbound.toEmailAddresses");
	
	



	/**
	 * SALSIFY_INBOUND_STAGING_DB_IP for SALSIFY_INBOUND_STAGING_DB_IP.
	 */
	public static final String SALSIFY_INBOUND_STAGING_DB_IP = LCSProperties.get("com.hf.wc.integration.salsify.outbound.stagingdb.ip");

	/**
	 * INBOUND_STAGING_DB_USER for INBOUND_STAGING_DB_USER.
	 */
	public static final String INBOUND_STAGING_DB_USER = LCSProperties.get("com.hf.wc.integration.salsify.outbound.stagingdb.user");

	/**
	 * INBOUND_STAGING_DB_PASS for INBOUND_STAGING_DB_PASS.
	 */
	public static final String INBOUND_STAGING_DB_PASS = LCSProperties.get("com.hf.wc.integration.salsify.outbound.stagingdb.password");

	/**
	 * INBOUND_STAGING_DB_PROCESSSTATE for INBOUND_STAGING_DB_PROCESSSTATE
	 */
	public static final String INBOUND_STAGING_DB_PROCESSSTATE = LCSProperties.get("com.hf.wc.integration.salsify.inbound.stagingdb.ProcessState");

	/**
	 * INBOUND_ATTRIBUTES for INBOUND_ATTRIBUTES
	 */
	public static final String INBOUND_ATTRIBUTES = LCSProperties.get("com.hf.wc..integration.salsify.inbound.AttributeKeys");



	/**
	 * COLORWAY_INTEGRATION_QUEUE_NAME for COLORWAY_INTEGRATION_QUEUE_NAME.
	 */
	public static final String INBOUND_INTEGRATION_QUEUE_NAME = LCSProperties.get("com.hf.wc.integration.salsify.outbound.HFSalsifyInboundQueue");

		/**
	 * SALSIFY_INBOUND_QUEUE_NAME for SALSIFY_INBOUND_QUEUE_NAME.
	 */
	public static final String SALSIFY_INBOUND_QUEUE_NAME = LCSProperties.get("com.hf.wc.integration.salsify.inbound.QueueName");

	/**
	 * SALSIFY_INBOUND_TABLENAME for SALSIFY_INBOUND_TABLENAME.
	 */
	public static final String SALSIFY_INBOUND_TABLENAME = LCSProperties.get("com.hf.wc.integration.salsify.inbound.TableName");

	/**
	 * SALSIFY_INBOUND_COLORWAY_ATTRIBUTES for SALSIFY_INBOUND_COLORWAY_ATTRIBUTES.
	 */
	public static final String SALSIFY_INBOUND_COLORWAY_ATTRIBUTES = LCSProperties.get("com.hf.wc.integration.salsify.inbound.ColorwayAttributes");

	/**
	 * SALSIFY_INBOUND_PRODUCT_ATTRIBUTES for SALSIFY_INBOUND_PRODUCT_ATTRIBUTES.
	 */
	public static final String SALSIFY_INBOUND_PRODUCT_ATTRIBUTES = LCSProperties.get("com.hf.wc.integration.salsify.inbound.ProductAttributes");



	//############################################################### Salsify Outbound ##################################################################

	/**
	 * COLORWAY_STAGING_DB_PASS for COLORWAY_STAGING_DB_PASS.
	 */
	public static final String COLORWAY_INTEGRATION_STATUS = LCSProperties.get("com.hf.wc.integration.salsify.outbound.colorway.IntegrationStatus");

	/**
	 * COLORWAY_STAGING_DB_IP for COLORWAY_STAGING_DB_IP.
	 */
	public static final String COLORWAY_STAGING_DB_IP = LCSProperties.get("com.hf.wc.integration.salsify.outbound.stagingdb.ip");

	/**
	 * COLORWAY_STAGING_DB_USER for COLORWAY_STAGING_DB_USER.
	 */
	public static final String COLORWAY_STAGING_DB_USER = LCSProperties.get("com.hf.wc.integration.salsify.outbound.stagingdb.user");

	/**
	 * COLORWAY_STAGING_DB_PASS for COLORWAY_STAGING_DB_PASS.
	 */
	public static final String COLORWAY_STAGING_DB_PASS = LCSProperties.get("com.hf.wc.integration.salsify.outbound.stagingdb.password");

	/**
	 * OUTBOUND_SALSIFY_STAGING_DB_TABLENAME for OUTBOUND_SALSIFY_STAGING_DB_TABLENAME.
	 */
	public static final String OUTBOUND_SALSIFY_STAGING_DB_TABLENAME = LCSProperties.get("com.hf.wc.integration.salsify.outbound.stagingdb.tablename","test");

	/**
	 * OUTBOUND_SEND_TO_STAGING_DB for OUTBOUND_SEND_TO_STAGING_DB.
	 */
	public static final String OUTBOUND_SEND_TO_STAGING_DB = LCSProperties.get("com.hf.wc.integration.salsify.outbound.SendToStagingDB");


	/**
	 * PACKAGINGNAME for PACKAGINGNAME.
	 */
	public static final String PACKAGINGNAME = LCSProperties.get("com.hf.wc.integration.salsify.outbound.packagingName");
	

	/**
	 * SUPPLIERNUMBER for SUPPLIERNUMBER.
	 */
	public static final String SUPPLIERNUMBER = LCSProperties.get("com.hf.wc.integration.salsify.outbound.supplierNumber");



	//############################################################### JDE Sales Inbound ##################################################################

	/**
	 * JDESALES_INBOUND_QUEUE_NAME for JDESALES_INBOUND_QUEUE_NAME.
	 */
	public static final String JDESALES_INBOUND_QUEUE_NAME = LCSProperties.get("com.hf.wc.integration.jde.sales.inbound.QueueName");

	/**
	 * JDESALES_INBOUND_TABLENAME for JDESALES_INBOUND_TABLENAME.
	 */
	public static final String JDESALES_INBOUND_TABLENAME = LCSProperties.get("com.hf.wc.integration.jde.sales.inbound.TableName");

	/**
	 * JDESALES_INBOUND_COLORWAY_ATTRIBUTES_YEAR1 for JDESALES_INBOUND_COLORWAY_ATTRIBUTES_YEAR1.
	 */
	public static final String JDESALES_INBOUND_COLORWAY_ATTRIBUTES_YEAR1 = LCSProperties.get("com.hf.wc.integration.jde.sales.inbound.ColorwayAttributes.year1");

	/**
	 * JDESALES_INBOUND_COLORWAY_ATTRIBUTES_YEAR2 for JDESALES_INBOUND_COLORWAY_ATTRIBUTES_YEAR2.
	 */
	public static final String JDESALES_INBOUND_COLORWAY_ATTRIBUTES_YEAR2 = LCSProperties.get("com.hf.wc.integration.jde.sales.inbound.ColorwayAttributes.year2");

	/**
	 * JDESALES_INBOUND_COLORWAY_ATTRIBUTES_YEAR3 for JDESALES_INBOUND_COLORWAY_ATTRIBUTES_YEAR3.
	 */
	public static final String JDESALES_INBOUND_COLORWAY_ATTRIBUTES_YEAR3 = LCSProperties.get("com.hf.wc.integration.jde.sales.inbound.ColorwayAttributes.year3");

	/**
	 * JDESALES_INBOUND_PRODUCT_ATTRIBUTES_YEAR1 for JDESALES_INBOUND_PRODUCT_ATTRIBUTES_YEAR1.
	 */
	public static final String JDESALES_INBOUND_PRODUCT_ATTRIBUTES_YEAR1 = LCSProperties.get("com.hf.wc.integration.jde.sales.inbound.ProductAttributes.year1");

	/**
	 * JDESALES_INBOUND_PRODUCT_ATTRIBUTES_YEAR2 for JDESALES_INBOUND_PRODUCT_ATTRIBUTES_YEAR2.
	 */
	public static final String JDESALES_INBOUND_PRODUCT_ATTRIBUTES_YEAR2 = LCSProperties.get("com.hf.wc.integration.jde.sales.inbound.ProductAttributes.year2");

	/**
	 * JDESALES_INBOUND_PRODUCT_ATTRIBUTES_YEAR3 for JDESALES_INBOUND_PRODUCT_ATTRIBUTES_YEAR3.
	 */
	public static final String JDESALES_INBOUND_PRODUCT_ATTRIBUTES_YEAR3 = LCSProperties.get("com.hf.wc.integration.jde.sales.inbound.ProductAttributes.year3");

	
	/**
	 * SALES_COLORWAY_ATTRIB_KEY_YEAR1 for SALES_COLORWAY_ATTRIB_KEY_YEAR1.
	 */
	public static final String SALES_COLORWAY_ATTRIB_KEY_YEAR1 = LCSProperties.get("com.hf.wc.integration.jde.sales.inbound.allColorwayAttKeyOnly.year1");

	/**
	 * SALES_COLORWAY_ATTRIB_KEY_YEAR2 for SALES_COLORWAY_ATTRIB_KEY_YEAR2.
	 */
	public static final String SALES_COLORWAY_ATTRIB_KEY_YEAR2 = LCSProperties.get("com.hf.wc.integration.jde.sales.inbound.allColorwayAttKeyOnly.year2");

	/**
	 * SALES_COLORWAY_ATTRIB_KEY_YEAR3 for SALES_COLORWAY_ATTRIB_KEY_YEAR3.
	 */
	public static final String SALES_COLORWAY_ATTRIB_KEY_YEAR3 = LCSProperties.get("com.hf.wc.integration.jde.sales.inbound.allColorwayAttKeyOnly.year3");




	/**
	 * JDE_SALES_INBOUND_YEAR1 for JDE_SALES_INBOUND_YEAR1.
	 */
	public static final String JDE_SALES_INBOUND_YEAR1 = LCSProperties.get("com.hf.wc.integration.jde.sales.inbound.year1");
	
	/**
	 * JDE_SALES_INBOUND_YEAR1 for JDE_SALES_INBOUND_YEAR1.
	 */
	public static final String JDE_SALES_INBOUND_YEAR2 = LCSProperties.get("com.hf.wc.integration.jde.sales.inbound.year2");

	/**
	 * JDE_SALES_INBOUND_YEAR1 for JDE_SALES_INBOUND_YEAR1.
	 */
	public static final String JDE_SALES_INBOUND_YEAR3 = LCSProperties.get("com.hf.wc.integration.jde.sales.inbound.year3");


	
	public static final String SALES_ATTKEYS_YEAR1 = LCSProperties.get("com.hf.wc.integration.jde.sales.inbound.ColorwayAttributekeys.year1");
	public static final String SALES_ATTKEYS_YEAR2 = LCSProperties.get("com.hf.wc.integration.jde.sales.inbound.ColorwayAttributekeys.year2");
	public static final String SALES_ATTKEYS_YEAR3 = LCSProperties.get("com.hf.wc.integration.jde.sales.inbound.ColorwayAttributekeys.year3");
	


	//####################################################### JDE Cost Inbound ##########################################################

	/**
	 * JDECOST_INBOUND_QUEUE_NAME for JDECOST_INBOUND_QUEUE_NAME.
	 */
	public static final String JDECOST_INBOUND_QUEUE_NAME = LCSProperties.get("com.hf.wc.integration.jde.cost.inbound.QueueName");

	/**
	 * JDECOST_INBOUND_TABLENAME for JDECOST_INBOUND_TABLENAME.
	 */
	public static final String JDECOST_INBOUND_TABLENAME = LCSProperties.get("com.hf.wc.integration.jde.cost.inbound.TableName");

	/**
	 * JDECOST_INBOUND_COLORWAY_ATTRIBUTES for JDECOST_INBOUND_COLORWAY_ATTRIBUTES.
	 */
	public static final String JDECOST_INBOUND_COLORWAY_ATTRIBUTES = LCSProperties.get("com.hf.wc.integration.jde.cost.inbound.ColorwayAttributes");

	/**
	 * JDECOST_INBOUND_PRODUCT_ATTRIBUTES for JDECOST_INBOUND_PRODUCT_ATTRIBUTES.
	 */
	public static final String JDECOST_INBOUND_PRODUCT_ATTRIBUTES = LCSProperties.get("com.hf.wc.integration.jde.cost.inbound.ProductAttributes");




	//####################################################### JDE Outbound Service Part ##########################################################

















	//####################################################### JDE Outbound Service BOM ##########################################################


	/**
	 * OUTBOUND_SBOM_STAGING_DB_TABLENAME for OUTBOUND_SBOM_STAGING_DB_TABLENAME.
	 */
	public static final String OUTBOUND_SBOM_STAGING_DB_TABLENAME = LCSProperties.get("com.hf.wc.integration.jde.outbound.sbom.stagingdb.tablename");

	/**
	 * MODEL_NAME for MODEL_NAME.
	 */
	public static final String MODEL_NAME = LCSProperties.get("com.hf.wc.integration.modelName");










}
