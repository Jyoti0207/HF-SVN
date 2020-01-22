package com.hf.wc.product;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.*;
import org.apache.log4j.Logger;
import wt.fc.WTObject;
import wt.method.RemoteAccess;
import wt.part.WTPart;
import wt.queue.ProcessingQueue;
import wt.queue.QueueHelper;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.flextype.*;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.moa.LCSMOAObject;
import com.lcs.wc.moa.LCSMOAObjectLogic;
import com.lcs.wc.moa.LCSMOATable;
import com.lcs.wc.partstruct.LCSPartToProductLinkQuery;
import com.lcs.wc.product.*;
import com.lcs.wc.season.*;
import com.lcs.wc.util.*;

/**
 * @author ITCINFOTECH This class is triggered for Automation of SBOM on Fan
 *         Models.
 */
public final class HFSBOMCreation implements RemoteAccess {

	/**
	 * Variable to store hierarchy.
	 */
	private final static String HIERARCHY = LCSProperties.get("com.hf.wc.product.HFSBOMCreation.hierarchy");
	/**
	 * Variable to store SBOM att key.
	 */
	private final static String SBOMKEY = LCSProperties.get("com.hf.wc.product.HFSBOMCreation.sbomKey");
	/**
	 * Variable to store description att key of SBOM MOA table.
	 */
	private final static String DESCRIPTIONKEY = LCSProperties.get("com.hf.wc.product.HFSBOMCreation.descriptionKey");
	/**
	 * Variable to store Level att key.
	 */
	private final static String LEVELKEY = LCSProperties.get("com.hf.wc.product.HFSBOMCreation.levelKey");
	/**
	 * Variable to store service Product Key.
	 */
	private final static String FAMILYKEY = LCSProperties.get("com.hf.wc.product.HFSBOMCreation.familyKey");
	/**
	 * Variable to store Model obj key.
	 */
	private final static String MODELKEY = LCSProperties.get("com.hf.wc.product.HFSBOMCreation.modelKey");
	/**
	 * Variable to store Quantity att key.
	 */
	private final static String QUANTITYKEY = LCSProperties.get("com.hf.wc.product.HFSBOMCreation.quantityKey");
	/**
	 * Variable to store ItemNumber att key.
	 */
	private final static String ITEMNUMBERKEY = LCSProperties.get("com.hf.wc.product.HFSBOMCreation.itemNumberKey");
	/**
	 * Variable to store unitofMeasure att key of SBOM MOA Table.
	 */
	private final static String UOMKEY = LCSProperties.get("com.hf.wc.product.HFSBOMCreation.unitofMeasure");
	/**
	 * Variable to store unitofMeasure att key of Service Part Model.
	 */
	private final static String MODELUOM = LCSProperties.get("com.hf.wc.product.HFSBOMCreation.serviceModelUom");
	/**
	 * Variable to store JDEDescription att key of Service Part Model.
	 */
	private final static String JDEDESCRIPTION = LCSProperties.get("com.hf.wc.product.HFSBOMCreation.jdeDescriptionKey");
	/**
	 * Variable to store season Name Key
	 */
	private final static String SEASONNAMEKEY = LCSProperties.get("com.hf.wc.product.HFSBOMCreation.seasonNameKey");
	/**
	 * Variable to store season Name
	 */
	private final static String SEASONNAME = LCSProperties.get("com.hf.wc.product.HFSBOMCreation.seasonName");
	/**
	 * Constructor object.
	 */
	private HFSBOMCreation() {

	}

	/**
	 * Logger object.
	 */
	private static Logger log = Logger.getLogger(HFSBOMCreation.class.getName());

	/**
	 * This method creates creates queue for SBOM creation
	 * 
	 * @param objWTobject.
	 * @throws Exception.
	 */
	public static void sendProduct(WTObject obj) throws WTException, WTPropertyVetoException {
		if (obj instanceof LCSProduct) {
			String queueName = LCSProperties.get("com.hf.wc.product.HFSBOMCreation.queueName");
			ProcessingQueue processQueue = null;
			try {
				processQueue = (ProcessingQueue) QueueHelper.manager.getQueue(queueName,
						wt.queue.ProcessingQueue.class);
				// Check if the queue exists. If not, create the queue.
				if (processQueue == null) {
					processQueue = QueueHelper.manager.createQueue(queueName);
					// Enabling the queue if not enabled.
					processQueue.setEnabled(true);
					// Starting the queue here if it is not in Started state.
					processQueue = QueueHelper.manager.startQueue(processQueue);
				}
				// Carrying on with the queue entry only if the Queue is in
				// Started State.
				if (!processQueue.getQueueState().equals(ProcessingQueue.STARTED)) {
					processQueue = QueueHelper.manager.startQueue(processQueue);
				}
				if ("STARTED".equals(processQueue.getQueueState())) {

					String id = FormatHelper.getObjectId(obj);
					Class[] argTypes = { String.class };
					Object[] argValues = { id };
					wt.org.WTPrincipal wtprincipal = SessionHelper.manager.getPrincipal();
					processQueue.addEntry(wtprincipal, "createBOMForServicePart", (HFSBOMCreation.class).getName(),
							argTypes, argValues);
				}
			} catch (WTException e) {
				log.info("Exception in invoking Queue");
			}
		}
	}
	/**
	 * This method creates the SBOM MOA table for the Fan Models
	 * 
	 * @param String.
	 * @throws RemoteException, InvocationTargetException, ParseException, WTException, WTPropertyVetoException.
	 */
	public static void createBOMForServicePart(String fanProductId)	throws RemoteException, InvocationTargetException, ParseException, WTException, WTPropertyVetoException {
		log.info("SBOM Automation  Trigerred with ID:" + fanProductId);
		WTPart wtPart = null;
		List wtparts = new ArrayList();
		LCSProduct fanProduct = (LCSProduct) LCSQuery.findObjectById(fanProductId);
		//fetching season product link for given product
		LCSSeasonProductLink lsp = SeasonProductLocator.getSeasonProductLink(fanProduct);
		//Finding the season by means of the name
		LCSSeason season = HFServicePartCreationHelper.fetchSeasobObject(SEASONNAMEKEY,SEASONNAME);
		fanProduct = SeasonProductLocator.getProductARev(lsp);
		log.info("The Season Associated to the Fan product is:" + season.getName());
		// LCSSeason season =(LCSSeason)VersionHelper.latestIterationOf(seasonMaster);
		LCSPartToProductLinkQuery ptpLink = new LCSPartToProductLinkQuery();
		Collection partCollection = ptpLink.findAssociatedParts(fanProduct);
		Iterator partIterator = partCollection.iterator();
		// Iterating the WTPart for the Fan Product.
		while (partIterator.hasNext()) {
			FlexObject flexobj = (FlexObject) partIterator.next();
			wtPart = (WTPart) LCSQuery.findObjectById("VR:wt.part.WTPart:" + flexobj.getData("WTPART.branchIditerationInfo"));
			wtparts.add(wtPart);
		}
		log.info("The No.of wtparts linked are:" + wtparts.size());
		Iterator wtPartIterator = wtparts.iterator();
		while (wtPartIterator.hasNext()) {
			wtPart = (WTPart) wtPartIterator.next();
			LCSProductQuery prodQuery = new LCSProductQuery();
			LCSProduct serviceProduct = null;
			LCSSKU serviceColorwayObj = null;
			LCSSKU tempcolorwayObj = null;
			LCSSKU fanColorwayObj = null;
			FlexType flexType = null;
			long productId = 0;
			String productIdValue = null;
			long colorwayId = 0;
			String colorwayIdValue = null;
			// The partModel HashMap holds the WTPartObj and PRODUCT NUMBER.
			HashMap<WTPart, String> partModel = (HashMap<WTPart, String>) HFSBOMLinkedEpmDocument.fetchEpmDocAssociated(wtPart);
			log.info("partModel size:" + partModel.size());
			// Fetching the associated SKU Object
			Collection<?> skuCollection = fanProduct.findSKUObjects();
			Iterator skuItr = skuCollection.iterator();
			// Addinfg the fan model to the collection
			Map<String, LCSSKU> fanModelMap = new HashMap();
			while (skuItr.hasNext()) {
				tempcolorwayObj = (LCSSKU) skuItr.next();
				String itemNumberFanModel = (String) tempcolorwayObj.getValue(ITEMNUMBERKEY);
				fanModelMap.put(itemNumberFanModel, tempcolorwayObj);
			}
			log.info("fanModelMap size:" + skuCollection.size());
			// Looping through the WTPart Object fetched to find the serviceable
			for (Map.Entry storedQuantity : partModel.entrySet()) {
				String itemNumber = (String) storedQuantity.getValue();
				// Fetching the colrway object for the obtained Model Number
				fanColorwayObj = (LCSSKU) fanModelMap.get(itemNumber);
				// The fetched WTPart Object is assigned to part (WTPart)
				wtPart = (WTPart) storedQuantity.getKey();
				log.info("Part Number:" + wtPart.getNumber() + "itemNumber:" + itemNumber);
				int count = 0;
				// Moa Row data is held in moaValues ArrayList
				ArrayList moaValues = (ArrayList) HFServicePartCreation.getServiceableParts(wtPart, season);
				flexType = FlexTypeCache.getFlexTypeFromPath(HIERARCHY);
				// Looping the MOALIST to insert the values to the Fan Model.
				if (fanColorwayObj != null) {
					fanColorwayObj = SeasonProductLocator.getSKUARev(fanColorwayObj);
					Iterator itr = moaValues.iterator();
					// Iterating the Model List fetched
					fanColorwayObj = SeasonProductLocator.getSKUARev(fanColorwayObj);
					LCSMOATable moaObj = (LCSMOATable) fanColorwayObj.getValue(SBOMKEY);
					//invoking the MOA row clearance method
					clearResuableTableRows(moaObj);			
					while (itr.hasNext()) {
						Map<String, LCSSKU> serviceModelMap = new HashMap();
						String productFinishQuantity = (String) itr.next();
						// Splitting the concatenated string to fetch service Product Name, finish, quantity.
						String[] split = productFinishQuantity.split("\\,");
						String prodName = split[0];
						String finish = split[1];
						String quantity = split[2];
						log.info("seviceModelName:" + prodName + finish);
						String colorwayName = prodName + finish + " " + "(" + prodName + ")";
						// Fetching the Service Product Object by name.
						serviceProduct = prodQuery.findProductByNameType(prodName, flexType);
						// Fetching the Service Product ID.
						productId = serviceProduct.getBranchIdentifier();
						productIdValue = String.valueOf(productId);
						// Fetching the Service Model Object for the fetched
						// Service Product
						Collection<?> seriveSkuCollection = LCSSKUQuery.findAllSKUs(serviceProduct, false);
						Iterator<?> serviceSkuItr = seriveSkuCollection.iterator();
						while (serviceSkuItr.hasNext()) {
							tempcolorwayObj = (LCSSKU) serviceSkuItr.next();
							if (tempcolorwayObj.getName().toString().trim().equalsIgnoreCase(colorwayName)) {
								serviceModelMap.put(colorwayName.trim(), tempcolorwayObj);
							}
						}
						serviceColorwayObj = (LCSSKU) serviceModelMap.get(colorwayName);
						if (serviceColorwayObj != null) {
							log.info(serviceColorwayObj.getName());
							// Fetching Model Object ID.
							colorwayId = serviceColorwayObj.getBranchIdentifier();
							colorwayIdValue = String.valueOf(colorwayId);
							String description = (String) serviceColorwayObj.getValue(JDEDESCRIPTION);
							String unitofMeasure = (String) serviceColorwayObj.getValue(MODELUOM);
							// Creating an instance of Fan Model SBOM.
							FlexObject newRow = new FlexObject();
							Map<String, FlexObject> rowData = new Hashtable<String, FlexObject>();
							// Adding values to the HashTable to be inserted to
							// MOA TABLE.
							newRow.put("SORTINGNUMBER", Integer.toString(count));
							newRow.put("ID", Integer.toString(count));
							newRow.put("DROPPED", "false");
							newRow.put(LEVELKEY, 1);
							newRow.put(FAMILYKEY, productIdValue);
							newRow.put(MODELKEY, colorwayIdValue);
							newRow.put(QUANTITYKEY, quantity);
							newRow.put(DESCRIPTIONKEY, description);
							newRow.put(UOMKEY, unitofMeasure);
							// Adding the values to the MOATABLE
							moaObj.addRow(newRow);
							rowData.put(Integer.toString(count), newRow);
							// Creating LCSMOA object to update the SBOM row.`
							LCSMOAObjectLogic moaLogic = new LCSMOAObjectLogic();
							// updating the MOA table
							moaLogic.updateMOAObjectCollection(fanColorwayObj,fanColorwayObj.getFlexType().getAttribute(SBOMKEY), (Hashtable) rowData);
							moaObj.clear();
							count++;
						}
					}
				}
			}
		}
	}
	/**
	 * This method delete the MOA table rows foe every new invoke
	 * 
	 * @param LCSMOATable moaObj.
	 * @throws Exception.
	 */
	private static void clearResuableTableRows(LCSMOATable moaObj) throws WTException {
		//Collects the rows for the given MOA object
		Collection<FlexObject> moaRows = moaObj.getRows();
		//Looping the selection 
		for (FlexObject flexObj : moaRows) {
			String moaObjid = flexObj.getString("oid");
			//Find's the object by id
			LCSMOAObject moa = (LCSMOAObject) LCSQuery.findObjectById("OR:com.lcs.wc.moa.LCSMOAObject:" + moaObjid);
			//Delete's the object record.
			LCSMOAObjectLogic.deleteObject(moa);
		}

	}

	
}
