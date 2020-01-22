package com.hf.wc.product;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.*;
import org.apache.log4j.Logger;
import wt.method.RemoteAccess;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.util.LCSProperties;
import com.ptc.core.lwc.server.PersistableAdapter;
import wt.epm.*;
import wt.fc.*;
import wt.part.*;
import wt.pom.PersistenceException;
import wt.util.*;
import wt.vc.VersionControlHelper;

/**
 * @author ITCINFOTECH 
 * This class is called by HFSBOMCreation for SBOM AUTOMATION.
 */
public final class HFServicePartCreation  implements RemoteAccess {
	/**
	 * Variable to store product_no att key.
	 */
	private final static String PRODUCTKEY = LCSProperties.get("com.hf.wc.product.HFServicePartCreation.productKey");
	/**
	 * Variable to hold the MOA table values.
	 */
	private  static Map<String, String> quantityCollection = new HashMap<String, String>();
	
	private final static String NULL_STR = "null";
	/**
	 * Constructor object. 
	 */
	private HFServicePartCreation() 
	{
		
	}
	/**
	 * Logger object.
	 */
	private static Logger log = Logger.getLogger(HFServicePartPlugin.class.getName());
	/**
	 * This method is invoked to fetch the MOA table values
	 * 
	 * @param partObj WTPart.
	 * @throws WTException 
	 * @throws PersistenceException 
	 * @throws WTPropertyVetoException 
	 */
	public static List<String> getServiceableParts (WTPart partObj, LCSSeason season) throws PersistenceException, WTException, WTPropertyVetoException 
	{
		// WTPart object to store the Latest version of the given WTPart
		WTPart parentPart = null;
		// An ArrayList to hold the values of a MOA Table.
		ArrayList<String> moaValuesList = new ArrayList<String>();
		quantityCollection.clear();
		moaValuesList.clear();
		// Fetching the Latest version of the given WTPart Object.
		WTPartMaster partMaster = (WTPartMaster) partObj.getMaster();
		parentPart = (WTPart) wt.vc.VersionControlHelper.service.allIterationsOf(partMaster).nextElement();
		// Calling PartFinish Method to fetch the serviceable Parts and MOA
		// table values.
		getPartFinish(parentPart,season);
		// Looping through to fetch the corresponding part and finish.
		for (Map.Entry storedQuantity : quantityCollection.entrySet()) {
			String quantity = (String) storedQuantity.getValue();
			String productFinish = storedQuantity.getKey().toString();
			String productFinishQuantity = productFinish + "," + quantity;
			log.info("productFinishQuantity:"+productFinishQuantity);
			// Filtering the ListValues to avoid duplication
			if (!moaValuesList.contains(productFinishQuantity)) {
				log.info(moaValuesList.size() + "inside array");
				// Adding values to the ArrayList
				moaValuesList.add(productFinishQuantity);
			}
		}
		// Clearing the HashMap for to insert same part,finish for next models.
		quantityCollection.clear();
		// Returning the MOAList.
		return moaValuesList;
	}
	/**
	 * This method is invoked to fetch the serviceable parts,Finish,Quantity.
	 * @param parentPart WTPart.
	 * @throws WTException 
	 * @throws WTPropertyVetoException 
	 */
	public static void getPartFinish(WTPart parentPart, LCSSeason season) throws WTPropertyVetoException, WTException 
	{
		//Initializing method variables for local operation. 
		QueryResult usesQueryResult = null;
		int quantity = 0;
		String finish = null;
		String serviceable = null;
		log.info("Inside getPartFinish Method");
				//API to fetch the parts used by the input WTPart.
				usesQueryResult = WTPartHelper.service.getUsesWTPartMasters(parentPart);
				if (usesQueryResult != null && usesQueryResult.size() > 0) {
					//Looping through the structure to fetch all the children of the parentPart.
					while (usesQueryResult.hasMoreElements()) {
						WTPartUsageLink partUsageLink = (WTPartUsageLink) usesQueryResult.nextElement();
						WTPartMaster childPartMaster = (WTPartMaster) partUsageLink.getUses();
						WTPart childPart = (WTPart) VersionControlHelper.service.allVersionsOf(childPartMaster).nextElement();
						quantity = (int) partUsageLink.getQuantity().getAmount();
						//filterServiceable method invoked to find the fetched WTPart is serviceable or Not.
						serviceable	= HFServiceBOMQuery.filterServiceable(partUsageLink);
						//serviceable = (String) rms.invoke("findServiceable", HFServicePartCreation.class.getName(), null,new Class[] { WTPart.class }, new Object[] {childPart});
						log.info(serviceable);
						//Filtering serviceable.
						if ("YES".equalsIgnoreCase(serviceable)) {
							//findFinish method invoked to find finish of the given WTPart.
							finish	= HFServiceBOMQuery.findFinish (partUsageLink);
							//finish = (String) rms.invoke("fetchFinish", HFServicePartCreation.class.getName(), null,new Class[] { WTPartUsageLink.class }, new Object[] { partUsageLink });
							//getFinshCode method invoked to find the finish code from Finish.
							String filteredFinish = getFinshCode(finish);
							log.info(filteredFinish.trim());
							//createServicePart method invoked to create service part, Service model.
							try {
								HFServicePartCreationHelper.createServicePart(childPart, filteredFinish, quantity,season);
							} catch (RemoteException e) {
								e.printStackTrace();
							} catch (InvocationTargetException e) {
								e.printStackTrace();
							} catch (ParseException e) {
								e.printStackTrace();
							}
						}
						//The method getPartFinish is called recursively to find all the children under the parent. 
						getPartFinish(childPart,season);
					}
				}
			
		

	}
	
	/**
	 * This method is invoked to roll up the Quantity. 
	 * @param partFinish String, quantityValue String, quantity Integer.
	 * @throws Exception.
	 */
	public static void rollUpQunatity(String partFinish, String quantityValue, int quantity) {
		
			//If the value of partFinish is unique the HashMap is loaded with corresponding Quantity. 
			if (!quantityCollection.containsKey(partFinish)) {
				quantityCollection.put(partFinish, quantityValue);
			} 
			//If the value of partFinish is repeated the HashMap is loaded with rolled up Quantity.
			else {
				for (Map.Entry storedQuantity : quantityCollection.entrySet()) {
					if (storedQuantity.getKey().equals(partFinish)) {
						String quantityValue1 = (String) storedQuantity.getValue();
						int tempQuantity = Integer.parseInt(quantityValue1);
						//Adding the quantity with previously fetched quantity for same Finish.
						tempQuantity = tempQuantity + quantity;
						quantityValue1 = Integer.toString(tempQuantity);
						//Adding the rolled up values to the collection object.
						quantityCollection.put(partFinish, quantityValue1);
					}
				}
			}
		

	}
	
	/**
	 * This method fetches the product Number of the given EPMDocument.
	 * @param epmObj EPMDocument.
	 * @throws WTException 
	 * @throws Exception 
	 */
	public static String fetchingprodno(EPMDocument epmObj) throws WTException  {
		
		String modelNumber = "prd_no";
		long id = epmObj.getPersistInfo().getObjectIdentifier().getId();
		String s = String.valueOf(id);
		String wt = "wt.epm.EPMDocument:" +s;
		//Finding the oid of the given EPMDocument.
		ObjectIdentifier oid = ObjectIdentifier.newObjectIdentifier(wt);
		EPMDocument epm = (EPMDocument) PersistenceHelper.manager.refresh(oid);
		//Fetching the PersistableAdapter object.
		PersistableAdapter priceObj = new PersistableAdapter(epm, null, null, null);
		//System.out.println(priceObj);
		//Loading the Product Key.
		priceObj.load(PRODUCTKEY);
		//Fetching the Product Value.
		Object model = priceObj.get(PRODUCTKEY);
		//System.out.println("-------Model Number:-------------   :" + model);
		if (model != null) {
			modelNumber = model.toString();
		} else {
			modelNumber = NULL_STR;
		}
		//Returning the Model Number.
		return modelNumber;
	}
	
	public static String getFinshCode(String displayIdentity )
		{
			log.info(displayIdentity);
			String contentName = NULL_STR;
			if (!NULL_STR.equalsIgnoreCase(displayIdentity)){
			String[] str = displayIdentity.split("\\-"); 
			contentName =  str[0];}
			return contentName.trim();
		}
}

