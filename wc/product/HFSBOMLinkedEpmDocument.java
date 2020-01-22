package com.hf.wc.product;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import com.lcs.wc.util.LCSProperties;
import com.ptc.core.lwc.server.PersistableAdapter;
import wt.epm.EPMDocument;
import wt.epm.build.EPMBuildRule;
import wt.epm.familytable.EPMFamilyTable;
import wt.epm.structure.EPMDescribeLink;
import wt.epm.structure.EPMStructureHelper;
import wt.fc.ObjectIdentifier;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.part.WTPartMaster;
import wt.pom.PersistenceException;
import wt.util.WTException;
import wt.vc.VersionControlHelper;

public final class HFSBOMLinkedEpmDocument implements RemoteAccess{
	/**
	 * Variable to store  RMI user.
	 */
	private final static String RMIUSER = LCSProperties.get("com.hf.wc.product.HFServicePartCreation.user");
	/**
	 * Variable to store RMI password.
	 */
	private final static String RMIPWD = LCSProperties.get("com.hf.wc.product.HFServicePartCreation.password");
	/**
	 * Variable  for repeated Srting literals.
	 */
	/**
	 * Variable to store  EPM Description Key.
	 */
	private final static String EPMDECRIPTION = LCSProperties.get("com.hf.wc.product.HFServicePartCreation.epmJdeDescription");
	/**
	 * Variable to store  Null
	 */
	private final static String NULL_STR = "null";
	/**
	 * Constructor object.
	 */
	private HFSBOMLinkedEpmDocument() {
		
	}
	/**
	 * Logger object.
	 */
	private static Logger log = Logger.getLogger(HFSBOMLinkedEpmDocument.class.getName());
	/**
	 * This method is invoked to Fetch the EPMDocument associated and its WTPart. 
	 * @param part WTPart.
	 * @throws RemoteException, InvocationTargetException.
	 * @throws ParseException 
	 */
	public static Map<WTPart, String> fetchEpmDocAssociated(WTPart part1)throws RemoteException, InvocationTargetException, ParseException {
		QueryResult attachedCadQR = null;
		//EPMDocument epmDoc = new EPMDocument();
		//fetching rms Connection
		RemoteMethodServer rms = RemoteMethodServer.getDefault();
		rms.setUserName(RMIUSER);
		rms.setPassword(RMIPWD);
		String linkType = NULL_STR;
		String modelNumber = "No Model";
		HashMap<WTPart, String> partModel = new HashMap<WTPart, String>();
		ArrayList<WTPart> partNumberList = new ArrayList();
		partNumberList.clear();
		try {
			WTPartMaster partmaster = (WTPartMaster) part1.getMaster();
			//Get the latest Version of WTPart Object
			WTPart part = (WTPart) wt.vc.VersionControlHelper.service.allIterationsOf(partmaster).nextElement();
			attachedCadQR = PersistenceHelper.manager.navigate(part, EPMBuildRule.ROLE_AOBJECT_ROLE, EPMBuildRule.class,true);
			linkType = "BuildRule";
			//If no documents are found under Build Rule, checking under describe link.
			/*if (attachedCadQR.size() == 0) {
				attachedCadQR = PersistenceHelper.manager.navigate(part, EPMDescribeLink.DESCRIBED_BY_ROLE,EPMDescribeLink.class, false);
				linkType = "DescribeLink";
			}*/
			if (attachedCadQR != null && attachedCadQR.size() > 0) {
				//System.out.println("Inside epmfound");
				//If more than one epmdocument is associated, the fetch latest epmdocument.
				EPMDocument epmDoc = getLatestCadList(attachedCadQR, linkType);
				
				int familyStatus = epmDoc.getFamilyTableStatus();
				//Finding the type of Famuily Object.
				//Generic
				if (familyStatus == 2) {
					//System.out.println("Attached Part is GENERIC%%%%%%%%%%%%%%%%");
					//Invoking fetchpartNumber to find the associated WTParts.
					partModel = (HashMap<WTPart, String>) HFServiceBOMQuery.fetchpartNumber(epmDoc, rms);
				}
				//Instances
				if (familyStatus == 1) {
					EPMDocument generic = 	findGenericforInstance(epmDoc);
					//Invoking fetchpartNumber to find the associated WTParts.
					partModel = (HashMap<WTPart, String>) HFServiceBOMQuery.fetchpartNumber(generic, rms);
				}
				if (familyStatus == 0 || familyStatus == 3 && part.isEndItem()) {
					//System.out.println("Attached Part is NON FAMILY%%%%%%%%%%%%%%%%");
					modelNumber	=	HFServiceBOMQuery.findModelNumber(epmDoc);
					//Adding the fetched WTPart to collection as the linked epmdocument is non-family.
					partModel.put(part, modelNumber);
				}
			} 
			//If no empdocumnet is attached, the same WTPart added to collection and it is returned.
			else {
				partModel.put(part, modelNumber);
			}
		} catch (WTException e) {
			log.info("Exception in fetching WTPart Numbers");
		}
		//System.out.println("The Hashmap Size is" + partModel.size());
		//Returning the collection of WTPart.
		return partModel;
	}
	/**
	 * This method fetches the genericEPMDocument for the given Instance EPMDocument.
	 * @param epmDoc EPMDocument.
	 * @throws WTException 
	 */
	private static EPMDocument findGenericforInstance(EPMDocument epmDoc) throws WTException {
		EPMDocument generic = new EPMDocument();
		EPMDocument tempgeneric = null;
		//Finding the generic of the given instance.
		QueryResult qeryResult = EPMStructureHelper.service.navigateContainedIn(epmDoc, null, true);
		EPMFamilyTable familyTable = (EPMFamilyTable) qeryResult.nextElement();
		QueryResult queryResult = EPMStructureHelper.service.navigateContains(familyTable, null, true); 
		//Looping through the structure to find the generic EPMDocument.
		while (queryResult.hasMoreElements()) {
			tempgeneric = (EPMDocument) queryResult.nextElement();
			//Find the generic in the collection
			if (tempgeneric.getFamilyTableStatus() == 2) {
				generic = tempgeneric; // Generic Part
			}
		}
		return generic;
	}
	/**
	 * This method fetches the latest Cad Object if more than one cad object is linked to WTPart.
	 * @param attachedCadQR QueryResult.
	 * @param linkType String.
	 * @throws WTException 
	 * @throws PersistenceException 
	 * @throws ParseException 
	 */
	public static EPMDocument getLatestCadList(QueryResult attachedCadQR,String linkType) throws PersistenceException, WTException, ParseException   {

		//EPMDocument linkedCad = new EPMDocument();
		EPMDocument latestCad = new EPMDocument();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		int counter = 0;
		Date creationDate = new Date();
		//Date comparisionDate = new Date();
		//Looping through the query result for all epmdocuments.
		while (attachedCadQR.hasMoreElements()) {
			EPMDocument cad =null;
			//if the link is of type Build Rule
			if ("BuildRule".equalsIgnoreCase(linkType)){
				cad = (EPMDocument)attachedCadQR.nextElement();
			}
			//if the link is of type Describe By
			else
			{
				EPMDescribeLink link = (EPMDescribeLink) attachedCadQR.nextElement();
				cad = link.getDescribedBy();
			}
			//Fetching latest Version of the linked EPMDocument.
			EPMDocument linkedCad = (EPMDocument) VersionControlHelper.service.allVersionsOf(cad).nextElement();
			String date = linkedCad.getCreateTimestamp().toString();
			String[] str = date.split("\\ ");
			//Finding the latest created EPMDocument.
			if (counter == 0) {
				creationDate = sdf.parse(str[0]);
				latestCad = linkedCad;
			} 
			else 
			{
				Date comparisionDate = sdf.parse(str[0]);
				if (!creationDate.before(comparisionDate)) {
					latestCad = linkedCad;
				}
			}
			counter++;
		}
		//Returning the latest EPMDocument.
		return latestCad;
	}
	public static String fetchJdeDescription(WTPart engineeringPart) throws PersistenceException, WTException, ParseException, RemoteException, InvocationTargetException {
		WTPartMaster partmaster = (WTPartMaster) engineeringPart.getMaster();
		String description = ""; 
		RemoteMethodServer rms = RemoteMethodServer.getDefault();
		rms.setUserName(RMIUSER);
		rms.setPassword(RMIPWD);
		//Get the latest Version of WTPart Object
		WTPart part = (WTPart) wt.vc.VersionControlHelper.service.allIterationsOf(partmaster).nextElement();
		QueryResult attachedCadQR = PersistenceHelper.manager.navigate(part, EPMBuildRule.ROLE_AOBJECT_ROLE, EPMBuildRule.class,true);
		String type = "BuildRule";
		//If no documents are found under Build Rule, checking under describe link.
		/*if (attachedCadQR.size() == 0) {
			attachedCadQR = PersistenceHelper.manager.navigate(part, EPMDescribeLink.DESCRIBED_BY_ROLE,EPMDescribeLink.class, false);
			linkType = "DescribeLink";
		}*/
		if (attachedCadQR != null && attachedCadQR.size() > 0) {
			// If more than one epmdocument is associated, the fetch latest epmdocument.

			EPMDocument cadDocument = getLatestCadList(attachedCadQR, type);
			log.info("Inside Find Model Number");
			description = (String) rms.invoke("fetchDescription", HFSBOMLinkedEpmDocument.class.getName(), null,new Class[] { EPMDocument.class }, new Object[] { cadDocument });
		}
		return description;
	}
	public static String fetchDescription(EPMDocument epmObj) throws WTException  {
		 
		String jdeDescription = "";
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
		priceObj.load(EPMDECRIPTION);
		//Fetching the Product Value.
		Object model = priceObj.get(EPMDECRIPTION);
		log.info("-------JDE DESCRIPTION :-------------   :" + model);
		
		if (model != null) {
			jdeDescription = model.toString();
		} else {
			jdeDescription = NULL_STR;
		}
		log.info("-------JDE DESCRIPTION :-------------   :" + jdeDescription);
		//Returning the Model Number.
		return jdeDescription;
	}
}
