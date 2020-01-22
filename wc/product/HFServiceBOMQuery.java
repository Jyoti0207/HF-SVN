package com.hf.wc.product;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.*;
import org.apache.log4j.Logger;
import com.lcs.wc.util.LCSProperties;
import com.ptc.core.lwc.server.*;
import com.ptc.core.meta.common.*;
import com.ptc.core.meta.container.common.AttributeTypeSummary;
import wt.epm.*;
import wt.epm.build.EPMBuildRule;
import wt.epm.familytable.EPMFamilyTable;
import wt.epm.structure.*;
import wt.fc.ObjectIdentifier;
import wt.fc.ObjectNoLongerExistsException;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.meta.LocalizedValues;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.*;
import wt.pom.PersistenceException;
import wt.util.WTException;

public final class HFServiceBOMQuery implements RemoteAccess{
	/**
	 * Variable to store  RMI user.
	 */
	private final static String RMIUSER = LCSProperties.get("com.hf.wc.product.HFServicePartCreation.user");
	/**
	 * Variable to store RMI password.
	 */
	private final static String RMIPWD = LCSProperties.get("com.hf.wc.product.HFServicePartCreation.password");
	
	/**
	 * Logger object.
	 */
	private static Logger log = Logger.getLogger(HFServiceBOMQuery.class.getName());
	/**
	 * Variable to store serviceable att key.
	 */
	private final static String SERVICEABLEKEY = LCSProperties.get("com.hf.wc.product.HFServicePartCreation.serviceable");
	/**
	 * Variable to store finishCode att key.
	 */
	private final static String FINISHCODEKEY = LCSProperties.get("com.hf.wc.product.HFServicePartCreation.serviceable.finishkey");
	
	private final static String NULL_STR = "null";
	
	private  HFServiceBOMQuery() {
		// TODO Auto-generated constructor stub
	}
 
	
	/**
	 * This method is called  to invoke the rms Method for fetching Finish.
	 * @param partUsageLink WTPartUsageLink.
	 */
	public static String findFinish(WTPartUsageLink partUsageLink) {
		// RemoteMethodServer for invoking rms Methods.
		RemoteMethodServer rms = RemoteMethodServer.getDefault();
		rms.setUserName(RMIUSER);
		rms.setPassword(RMIPWD);
		String partFinish = null;
		// Invoking rms method to find the finish of Serviceable WTPart.
		try {
			partFinish	=	(String) rms.invoke("fetchFinish", HFServiceBOMQuery.class.getName(), null,new Class[] { WTPartUsageLink.class }, new Object[] { partUsageLink });
		} catch (RemoteException e) {
			log.info("RemoteException");
		} catch (InvocationTargetException e) {
			log.info("InvocationTargetException");
		}
		return partFinish;
	}
	/**
	 * This method is called  to invoke the rms Method.
	 * @param childPart WTPart.
	 */
	public static String filterServiceable(WTPartUsageLink partUsageLink) {
		// RemoteMethodServer for invoking rms Methods.
		RemoteMethodServer rms = RemoteMethodServer.getDefault();
		rms.setUserName(RMIUSER);
		rms.setPassword(RMIPWD);
		String servicePart = null;
		//Invoking rms method to check the type of WTPart.
		try {
			servicePart = (String) rms.invoke("findServiceable", HFServiceBOMQuery.class.getName(), null,new Class[] { WTPartUsageLink.class }, new Object[] { partUsageLink });
		} catch (RemoteException e) {
			log.info("RemoteException");
		} catch (InvocationTargetException e) {
			log.info("InvocationTargetException");
		}
		return servicePart;
	}
	/**
	 * This method is invoked to fetch the Finish of given WTPart
	 * @param wtpart WTPartUsageLink.
	 * @throws WTException 
	 * @throws Exception.
	 */
	public static String fetchFinish(WTPartUsageLink wtPartLink)  
	{
		String finish = "No finish";
		//Fetching the id of the given WTPartUsagLink object.
		long id = wtPartLink.getPersistInfo().getObjectIdentifier().getId();
		String idValue = String.valueOf(id);
		String wtString = "wt.part.WTPartUsageLink:" + idValue;
		//Fetching the OID of the given WTPartUsagLink object.
		try {
			ObjectIdentifier oid = ObjectIdentifier.newObjectIdentifier(wtString);
			WTPartUsageLink partUsageLink = (WTPartUsageLink) PersistenceHelper.manager.refresh(oid);
			// PersistableAdapter with 3 arguments to find the list values of finish.
			PersistableAdapter persistableAdapterObj = new PersistableAdapter(partUsageLink, null, java.util.Locale.US,new com.ptc.core.meta.common.DisplayOperationIdentifier());
			//Loading the finshkey to fetch the list of finish values
			persistableAdapterObj.load(FINISHCODEKEY);
			Object partFinish = persistableAdapterObj.get(FINISHCODEKEY);
			log.info("Part Finish for the Given Serviceable part is: "+partFinish); 
			if (partFinish!= null){
			AttributeTypeSummary attributeTypeSummary = persistableAdapterObj.getAttributeDescriptor(FINISHCODEKEY);
			DataSet ds = attributeTypeSummary.getLegalValueSet();
			//System.out.println(ds);
			//fetching the display value of the finish under WTPartUsageLink.
				EnumerationEntryIdentifier eei = ((EnumeratedSet) ds).getElementByKey(partFinish.toString());
				LWCEnumerationEntryValuesFactory eevf = new LWCEnumerationEntryValuesFactory();
				//fetching the display value from the list of finish.
				LocalizedValues value = eevf.get(eei, Locale.ENGLISH);
				//System.out.println("The localized display value is: " + value.getDisplay());
				//fetching the display value of the list.
				partFinish = value.getDisplay();
				if (partFinish != null) {
					finish = partFinish.toString();
				} 
				else {
					finish = NULL_STR;
				}
			}
			
		} catch (WTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			finish = NULL_STR;
		}
		
		//Returns Null if there is no finish associated to the serviceable part. 
		

		return finish;
	}
	/**
	 * This method is invoked to find if the fetched is serviceable. 
	 * @param wtpart WTPart.
	 * @throws WTException.
	 */
	public static String findServiceable(WTPartUsageLink wtPartLink) 
	{
		String serviceable	=	"NO";
		//Fetching the id of the given WTPart object.
		long id	=	wtPartLink.getPersistInfo().getObjectIdentifier().getId();
		String idValue	=	String.valueOf(id);
		String wtString =	"wt.part.WTPartUsageLink:" + idValue;
		try {
			ObjectIdentifier oid = ObjectIdentifier.newObjectIdentifier(wtString);
			WTPartUsageLink partUsageLink = (WTPartUsageLink) PersistenceHelper.manager.refresh(oid);
			// PersistableAdapter with 3 arguments to find the list values of
			// Service.[Y/N]
			PersistableAdapter obj = new PersistableAdapter(partUsageLink, null, java.util.Locale.US,new com.ptc.core.meta.common.DisplayOperationIdentifier());
			// Loading the Service Key
			obj.load(SERVICEABLEKEY); 
			Object servicePart = obj.get(SERVICEABLEKEY);
			if (servicePart != null) {
			// Getting the description of the object
			AttributeTypeSummary ats = obj.getAttributeDescriptor(SERVICEABLEKEY);
			DataSet ds = ats.getLegalValueSet();
			// System.out.println(ds);
			// System.out.println("servicePart::::::::::::"+servicePart);
			// fetching the service value of the WTPart Object.
			EnumerationEntryIdentifier eei = ((EnumeratedSet) ds).getElementByKey(servicePart.toString());
			LWCEnumerationEntryValuesFactory eevf = new LWCEnumerationEntryValuesFactory();
			// fetching the display value from the list of service.
			LocalizedValues value = eevf.get(eei, Locale.ENGLISH);
			// System.out.println("The localized display value is: " +
			// value.getDisplay());
			// fetching the display value of the list.
			servicePart = value.getDisplay();
			serviceable = servicePart.toString();
			} else {
				serviceable = "NO";
			}
		} catch (ObjectNoLongerExistsException e) {
			// TODO Auto-generated catch block
			serviceable = "NO";
			log.info("Non serviceable part");
		} catch (WTException e) {
			// TODO Auto-generated catch block
			serviceable = "NO";
			log.info("Non serviceable part");
		}
		//Returns No if there is no service associated to the WTPart.
		return serviceable;
	}
	
	public static String findModelNumber(EPMDocument epmDoc) throws RemoteException, InvocationTargetException 
	{
		log.info("Inside Find Model Number");
		RemoteMethodServer rms = RemoteMethodServer.getDefault();
		rms.setUserName(RMIUSER);
		rms.setPassword(RMIPWD);
		String modelNumber = NULL_STR;
			modelNumber = (String) rms.invoke("fetchingprodno", HFServicePartCreation.class.getName(), null,new Class[] { EPMDocument.class }, new Object[] { epmDoc });
		
		return modelNumber;
	}
	/**
	 * This method is invoked to Fetch the associated of the given EPMDocuemnt. 
	 * @param epmDoc EPMDocument, rms RemoteMethodServer.
	 * @throws RemoteException, InvocationTargetException.
	 */
	public static Map<WTPart, String> fetchpartNumber(EPMDocument epmDoc, RemoteMethodServer rms)throws RemoteException, InvocationTargetException {
		HashSet<EPMDocument> instanceList = new HashSet<EPMDocument>();
		HashMap<WTPart, String> partModel = new HashMap<WTPart, String>();
		try {
			//System.out.println("********Generic Invoked");
			QueryResult attachedCadQR = null;
			QueryResult queryResult = null;
			//Fetching the family epmdocuments.
			queryResult = EPMStructureHelper.service.navigateContainedIn(epmDoc, null, true);
			while (queryResult.hasMoreElements()) {
				EPMFamilyTable familyTable = (EPMFamilyTable) queryResult.nextElement();
				QueryResult qrft = null;
				qrft = EPMStructureHelper.service.navigateContains(familyTable, null, true);
				//Looping through the structure to find the instance in the container.
				while (qrft.hasMoreElements()) {
					EPMDocument ftepmInstance = (EPMDocument) qrft.nextElement();
					//Filtering based on the instance.
					if (ftepmInstance.getFamilyTableStatus() != 2) {
						//Adding the instance to the collection. 
						instanceList.add(ftepmInstance);
					}
				}
			}
			//System.out.println("Instance Size:" + instanceList.size());
			Iterator itr = instanceList.iterator();
			//Iterating the instance to find the WTPart Associated.
			while (itr.hasNext()) {
				String modelNumber = "";
				EPMDocument epm = (EPMDocument) itr.next();
				EPMDocumentMaster epmmaster = (EPMDocumentMaster) epm.getMaster();
				epm = (EPMDocument) wt.vc.VersionControlHelper.service.allIterationsOf(epmmaster).nextElement();
				//Fetching the Model Number for SKU object by fetching product numbet of the EPMDocument.
				modelNumber	=	HFServiceBOMQuery.findModelNumber(epm);
				//modelNumber = (String) rms.invoke("fetchingprodno", HFServicePartCreation.class.getName(), null,new Class[] { EPMDocument.class }, new Object[] { epm });
				//Finding the associated WTPart.
				attachedCadQR = PersistenceHelper.manager.navigate(epm, EPMBuildRule.ROLE_BOBJECT_ROLE,	EPMBuildRule.class, true);
				//Retriving the WTPart Object.
				if (attachedCadQR != null && attachedCadQR.size() > 0) {
					WTPart part = (WTPart) attachedCadQR.nextElement();
					//System.out.println(part.getNumber());
					//Adding the WTParts to the Collection.
					if (part.isEndItem()){
					partModel.put(part, modelNumber);
					}
				}
			}
			//Clearing the instance collection for new generic epmdocument.
			instanceList.clear();
		} catch (PersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Returning the WTPart list.
		return partModel;
	}
}
