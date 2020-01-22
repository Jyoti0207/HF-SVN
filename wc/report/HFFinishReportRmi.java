package com.hf.wc.report;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.*;
import org.apache.log4j.Logger;
import com.ibm.icu.text.SimpleDateFormat;
import com.lcs.wc.util.LCSProperties;
import com.ptc.core.lwc.server.*;
import com.ptc.core.meta.common.*;
import com.ptc.core.meta.container.common.AttributeTypeSummary;
import wt.epm.EPMDocument;
import wt.epm.build.EPMBuildRule;
import wt.epm.structure.EPMDescribeLink;
import wt.fc.*;
import wt.meta.LocalizedValues;
import wt.method.RemoteMethodServer;
import wt.org.WTPrincipal;
import wt.part.*;
import wt.pom.PersistenceException;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.vc.VersionControlHelper;

public final class HFFinishReportRmi implements wt.method.RemoteAccess{
	
	/**
	 * Variable to store Remote Method Server Password.
	 */
	public static final String RMIPWD	=	LCSProperties.get("com.hf.wc.report.HFFinishReport.password");
	/**
	 * Variable to store Remote Method User Name.
	 */
	public static final String RMIUSER	=	LCSProperties.get("com.hf.wc.report.HFFinishReport.user");
	/**
	 * Variable to store Product Number Key from Windchill.
	 */
	public static final String MODELKEY	=	LCSProperties.get("com.hf.wc.report.HFFinishReport.productNumber"); 
	/**
	 * Logger object.
	 */
	private static Logger log = Logger.getLogger(HFFinishReport.class.getName());
	/**  
	 * Constructor object.
	 */
	private HFFinishReportRmi() {
		// TODO Auto-generated constructor stub
	}
	/**
	 * This method fetches the ModelNumber of the given Part Object 
	 * @param partObj WTPart.
	 * @throws WTException 
	 * @throws PersistenceException 
	 * @throws ParseException.
	 * @throws InvocationTargetException 
	 * @throws RemoteException 
	 */
	public static String fetchModelNumber(WTPart partObj) throws PersistenceException, WTException, ParseException, RemoteException, InvocationTargetException  {
		QueryResult attachedCadQR = null;
		//EPMDocument epmDoc = new EPMDocument();
		//Fetching RMS Access.
		RemoteMethodServer rms = RemoteMethodServer.getDefault();
		rms.setUserName(RMIUSER);
		rms.setPassword(RMIPWD);
		String modelNumber = "No Model";
		String linkType = "null";
		//Creating WTPart Master Object 
		WTPartMaster partmaster = (WTPartMaster) partObj.getMaster();
		//Fetching Latest Version
		WTPart latestPartObj = (WTPart) wt.vc.VersionControlHelper.service.allIterationsOf(partmaster).nextElement();
		//Fetching the Associated EPMDocument Object
		attachedCadQR = PersistenceHelper.manager.navigate(latestPartObj, EPMBuildRule.ROLE_AOBJECT_ROLE, EPMBuildRule.class,true);
		linkType = "BuildRule";
		//Checking for Describe By Link
		if (attachedCadQR.size() == 0) {
			attachedCadQR = PersistenceHelper.manager.navigate(latestPartObj, EPMDescribeLink.DESCRIBED_BY_ROLE,EPMDescribeLink.class, false);
			linkType = "DescribeLink";
		}
		//Fetching Latest EPMDocuemnt Object if more than one EPMdocument exist.
		if (attachedCadQR != null && attachedCadQR.size() > 0) {
			EPMDocument epmDoc = getLatestCadList(attachedCadQR, linkType);
			modelNumber = (String) rms.invoke("fetchingprodno", HFFinishReportRmi.class.getName(), null,
					new Class[] { EPMDocument.class, String.class }, new Object[] { epmDoc, MODELKEY });
		}
		//Returns Model Number
		return modelNumber;
	}
	/**
	 * This method fetches the latest Cad Object if more than one cad object is linked to WTPart.
	 * @param attachedCadQR QueryResult.
	 * @param linkType String.
	 * @throws WTException  
	 * @throws PersistenceException 
	 * @throws ParseException 
	 */
	public static EPMDocument getLatestCadList(QueryResult attachedCadQR,String linkType) throws ParseException, PersistenceException, WTException {

		//EPMDocument linkedCad = new EPMDocument();
		EPMDocument latestCad = new EPMDocument();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		int counter = 0;
		Date creationDate = new Date();
		//Date comparisionDate = new Date(); 
		//Looping through the query result for all epmdocuments.
		while (attachedCadQR.hasMoreElements()) {
			EPMDocument cadObj;
			//if the link is of type Build Rule
			if ("BuildRule".equalsIgnoreCase(linkType.trim())){
				cadObj = (EPMDocument)attachedCadQR.nextElement();
			}
			//if the link is of type Describe By
			else
			{
				EPMDescribeLink link = (EPMDescribeLink) attachedCadQR.nextElement();
				cadObj = link.getDescribedBy();
			}
			//Fetching latest Version of the linked EPMDocument.
			EPMDocument linkedCad = (EPMDocument) VersionControlHelper.service.allVersionsOf(cadObj).nextElement();
			String date = linkedCad.getCreateTimestamp().toString();
			String[] str = date.split("\\ ");
			//Finding the latest created EPMDocument.
			if (counter == 0) {
				creationDate = sdf.parse(str[0]);
				latestCad = linkedCad;
			} else {
				Date comparisionDate = sdf.parse(str[0]);
				if (!creationDate.after(comparisionDate)) {
					latestCad = linkedCad;
				} 
			}
			counter++;
		}
		//Returning the latest EPMDocument.
		return latestCad;
	}
	/**
	 * This method invokes the rmi method  to finish and price of the given WTPart.
	 * @param wtpart WTPartUsageLink.
	 * @param finishKey String.
	 * @param priceKey	String.
	 * @throws RemoteException
	 * @throws InvocationTargetException. 
	 */
	public static String findFinish(WTPartUsageLink ulpart, String finishkey, String pricekey)  {
		RemoteMethodServer rms = RemoteMethodServer.getDefault();
		rms.setUserName(RMIUSER);
		rms.setPassword(RMIPWD);
		String finish 	= "No Finish";
		
		try {
			finish 	=	(String) rms.invoke("fetchingFinish", HFFinishReportRmi.class.getName(), null, new Class[] { WTPartUsageLink.class, String.class, String.class }, new Object[] { ulpart,finishkey, pricekey });
		} catch (RemoteException | InvocationTargetException e) {
			e.printStackTrace();
			log.info("Exception in findFinish Method");
		}
		return finish;
	}
	/**
	 * This method fetches the finish and price of the given WTPart.
	 * @param wtpart WTPartUsageLink.
	 * @param finishKey String.
	 * @param priceKey	String.
	 * @throws WTException
	 * @throws Exception 
	 */
	@SuppressWarnings("unused")
	public static String fetchingFinish(WTPartUsageLink wtpart, String finishKey, String priceKey) throws WTException  {
		String finish = "No finish";
		String price = "No price";
		String finalfinish = null;
		//Fetching OID of the given WTPart
		long id = wtpart.getPersistInfo().getObjectIdentifier().getId();
		String s = String.valueOf(id);
		String wt = "wt.part.WTPartUsageLink:" + s;
		try{
		ObjectIdentifier oid = ObjectIdentifier.newObjectIdentifier(wt);
		//Getting part usage link by oid.
		WTPartUsageLink prt = (WTPartUsageLink) PersistenceHelper.manager.refresh(oid);
		//Getting presistableadapter object.
		PersistableAdapter obj = new PersistableAdapter(prt, null, java.util.Locale.US,new com.ptc.core.meta.common.DisplayOperationIdentifier());
		//Loading Finish Key. 
		obj.load(finishKey); 
		Object partFinish = obj.get(finishKey);
		if (partFinish != null){
		//getting the display value of the finish.
		log.info("partFinish Fetched: "+partFinish);
		AttributeTypeSummary ats = obj.getAttributeDescriptor(finishKey);
		DataSet ds = ats.getLegalValueSet();
		log.info("EnumerationEntryIdentifier");
		EnumerationEntryIdentifier eei = ((EnumeratedSet) ds).getElementByKey(partFinish.toString());
		LWCEnumerationEntryValuesFactory eevf = new LWCEnumerationEntryValuesFactory();
		LocalizedValues value = eevf.get(eei, Locale.ENGLISH);
		log.info("The localized display value is: " + value.getDisplay());
		//Assigning the fetched display of the finish attribute.
		partFinish = value.getDisplay();
		if (partFinish != null) {
			finish = partFinish.toString();
		} 
		}
		else {
			finish = "No Finish";
		}
		}
		catch (ObjectNoLongerExistsException e) {
			// TODO Auto-generated catch block
			log.info("The part dosent hold a finish");
		} catch (WTException e) {
			// TODO Auto-generated catch block
			log.info("The part dosent hold a finish");
		}
		ObjectIdentifier oidPrice = ObjectIdentifier.newObjectIdentifier(wt);
		//Getting part usage link by oid.
		WTPartUsageLink prtPrice = (WTPartUsageLink) PersistenceHelper.manager.refresh(oidPrice);
		//Fetching the price of the given WTPart.
		PersistableAdapter priceObj = new PersistableAdapter(prtPrice, null, null, null);
		log.info(priceObj);
		//Loading priceKey. 
		priceObj.load(priceKey);
		Object fetchedPrice = priceObj.get(priceKey);
		log.info("-------price:-------------   :" + fetchedPrice);
		if (fetchedPrice != null) {
			price = fetchedPrice.toString();
		} else {
			price = "No Price";
		}
		//Concatenating finish with price.
		finish = finish.concat(",");
		finalfinish = finish.concat(price);
		//returning the concatenated string of finish and price.
		return finalfinish;
	}
	/**
	 * This method fetches the Model Number of the given WTPart.
	 * @param epmObj EPMDocument.
	 * @param modelKey	String.
	 * @throws WTException 
	 * @throws Exception 
	 */
	public static String fetchingprodno(EPMDocument epmObj, String modelKey) throws WTException {

		String adminUser= RMIUSER;
		//System.out.println("The Admin User is:"+adminUser);
		WTPrincipal currentUsr = SessionHelper.manager.getPrincipal();
		SessionHelper.manager.setAuthenticatedPrincipal(adminUser);
		//System.out.println("The Current User is:"+currentUsr.getName());
		String modelNumber = "ModelNumber";
		//Fetching the OID of the given Object.
		long id = epmObj.getPersistInfo().getObjectIdentifier().getId();
		String s = String.valueOf(id);
		String wt = "wt.epm.EPMDocument:" + s;
		ObjectIdentifier oid = ObjectIdentifier.newObjectIdentifier(wt);
		//Fetching the EPMDocument from the obtained OID.
		EPMDocument epm = (EPMDocument) PersistenceHelper.manager.refresh(oid);
		//Fetching the presistance object.
		PersistableAdapter priceObj = new PersistableAdapter(epm, null, null, null);
		log.info(priceObj);
		//Loading model key.
		priceObj.load(modelKey); 
		Object model = priceObj.get(modelKey);
		log.info("-------Model Number:-------------   :" + model);
		//Return null in nothis is found.
		if (model != null) {
			modelNumber = model.toString();
		} else {
			modelNumber = "No Model";
		}
		 SessionHelper.manager.setAuthenticatedPrincipal(currentUsr.getName());
	        //System.out.println(" The Current User is:"+currentUsr.getName());
		//Returning the fetched Model Number.
		return modelNumber;
	}
}
