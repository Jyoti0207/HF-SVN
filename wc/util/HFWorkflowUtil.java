package com.hf.wc.util;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.sql.Date;
import java.sql.Timestamp;

import com.lcs.wc.calendar.LCSCalendarQuery;
import com.lcs.wc.calendar.LCSCalendarTask;
import com.lcs.wc.calendar.LCSCalendarTaskLogic;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSLogic;
import com.lcs.wc.placeholder.Placeholder;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.sample.LCSSample;
import com.lcs.wc.season.LCSSeason;
//import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonProductLink;
import com.lcs.wc.season.SeasonProductLocator;
import com.lcs.wc.util.LCSLog;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.VersionHelper;

import wt.fc.ObjectReference;
//import wt.fc.PersistenceServerHelper;
//import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.org.WTUser;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
//import wt.workflow.engine.WfActivity;
//import wt.workflow.engine.WfEngineHelper;
//import wt.workflow.engine.WfExecutionObject;
//import wt.workflow.engine.WfProcess;
import wt.workflow.work.WfAssignedActivity;


/**
 * hfWorkflowUtil class file contains all methods used in workflow tasks.
 * @author "true" johndeere
 * @version "true" 1.0
 */

public final class HFWorkflowUtil {
	/**
	 * declaring a boolean attribute DEBUG.
	 * 
	 */
	public static final boolean DEBUG;


	/**
	 * Reading property Entry for Estimated End Date attribute key.
	 * @throws InvocationTargetException 
	 * @throws RemoteException 
	 * 
	 */


	/*
	public static void main(String[] args) throws RemoteException, InvocationTargetException{
		RemoteMethodServer methodServer = RemoteMethodServer.getDefault();
		methodServer.invoke("getSeasonType", HFWorkflowUtil.class.getName(), null, null, null);
	}
	 */


	public static final String ESTENDDATE_KEY = LCSProperties
			.get("com.hf.wc.util.HFWorkflowUtil.estEndDate_Key","hfProjectedDate");
	
	
	public static final String SEASON_TYPE = LCSProperties
			.get("com.hf.wc.util.HFWorkflowUtil.season_type","hfproductcategory");

	/**
	 * reading a property entry.
	 * 
	 */
	static {
		//DEBUG = LCSProperties
		//	.getBoolean("com.bs.wc.util.hfWorkflowUtil.verbose");
		DEBUG=true;

	}

	/**
	 * Hidden Constructor.
	 */
	protected HFWorkflowUtil() {
	}

	/**
	 * This method is used to set due date for each workflow activity based on
	 * Target End date for mapped Calendar task.
	 * @param primaryBusinessObject WTObject
	 * @param taskName String
	 * @param activityName String
	 * @throws WTPropertyVetoException 
	 */
	public static void setDueDateForCalendarTask(
			WTObject primaryBusinessObject, String taskName, String activityName, ObjectReference self) throws WTPropertyVetoException {
		LCSCalendarTaskLogic taskLogic = new LCSCalendarTaskLogic();
		try {

			// Starting the task
			taskLogic.startTask(primaryBusinessObject, taskName);
			if (DEBUG) {
				LCSLog.debug("hfWorkflowUtil - Following calendar task is started "
						+ taskName);
			}
			// Get the calender task using task name.
			LCSCalendarTask currentTask = LCSCalendarQuery.getTask(
					primaryBusinessObject, taskName);

			// Initialise the End Date
			Timestamp endDate = currentTask.getTargetDate();
			if (endDate == null) {
				endDate = currentTask.getEstEndDate();
			}

			new Date(endDate.getTime());
			WfAssignedActivity wfAct = null;
			if(self.getObject() instanceof WfAssignedActivity){
				wfAct = (WfAssignedActivity)self.getObject();
				LCSLog.debug("wfAct name ===>>> "+wfAct.getName());
				LCSLog.debug("endDate ===>>> "+endDate);
				wfAct.setDeadline(endDate);
			}

			if(wfAct!=null){
				LCSLogic.persist(wfAct, true);
			}
			//WTUser wtUser = (WTUser) SessionHelper.manager.getPrincipal();
			//LCSLog.debug(":::::::::::::::::::::get value ::" + currentTask.getFlexType().getAttribute("hfResponsibleRole").getAttValueList().getValue("hfProductManager", Locale.getDefault()));

			//currentTask.setValue("hfResponsibleRole", currentTask.getValue("hfProductManager"));
			//LCSLogic.persist(currentTask,true);

			//currentTask.setValue("hfResponsibleUser", FormatHelper.getNumericObjectIdFromObject(wtUser));
			//LCSLog.debug("setDueDateForCalendarTask method ::::::responsible role is::::" + FormatHelper.getNumericObjectIdFromObject(wtUser));
			new LCSCalendarTaskLogic().saveCalendarTask( currentTask, true);

			// Fetch the primary business object
			//LCSProduct product = (LCSProduct) primaryBusinessObject;
			// Fetch all process associated with current primary business object
			//QueryResult assocProcessResult = WfEngineHelper.service.getAssociatedProcesses(product, null,	product.getContainerReference());
			//LCSLog.debug("assocProcessResult ==>>> "+assocProcessResult);
			// Check if associated products has more elements
			/*if (assocProcessResult.hasMoreElements()) {
				if (DEBUG) {
					LCSLog.debug("hfWorkflowUtil -  assocProcessResult has more elements");
				}
				WfProcess process = (wt.workflow.engine.WfProcess) assocProcessResult
						.nextElement();
				Enumeration<?> startedActivityEnum = WfEngineHelper.service
						.getProcessSteps(process, null);
				while (startedActivityEnum.hasMoreElements()) {
					LCSLog.debug("assocProcessResult ==>>> "+assocProcessResult);
					WfActivity activity = (WfActivity) startedActivityEnum
							.nextElement();
					setDueDateOnWFActivity(activity,endDate,activityName);
				}			

			}*/
		} catch (WTException e) {
			LCSLog.debug(e.getMessage());
		}

	}




	/**
	 * This method determines whether the workflow is generated for product
	 * object.
	 * 
	 * @param primaryBusinessObject WTObject
	 * @return prodtype String
	 */
	public static String getProductType(WTObject primaryBusinessObject) {

		LCSProduct product;
		String prodtype = "";
		if(primaryBusinessObject instanceof Placeholder)
		{
			prodtype="placeholder";
		}
		// Check if primaryBusinessObject is a product
		else if (primaryBusinessObject instanceof LCSProduct) {

			product = (LCSProduct) primaryBusinessObject;

			try {
				// Get the SeasonProductlink object for primaryBusinessObject
				LCSSeasonProductLink spLink = SeasonProductLocator
						.getSeasonProductLink(product);
				if (spLink != null
						&& !("SKU").equalsIgnoreCase(spLink.getSeasonLinkType())) {
					// If primaryBusinessObject is a season-product
					prodtype = "product";
				} else {
					// If product is not associated with season
					prodtype = "NA";
				}
			} catch (WTException e) {
				LCSLog.debug(e.getMessage());
			}
			// If primaryBusinessObject is not of type LCSProduct
		} else {
			prodtype = "NA";
		}

		if (DEBUG) {
			LCSLog.debug("hfWorkflowUtil - Workflow routed for " + prodtype);
		}

		return prodtype;
	}

	/**
	 * @param primaryBusinessObject WTObject
	 * @return prodtype String
	 */
	public static void assignTeam(WTObject primaryBusinessObject) {

				LCSProduct product = (LCSProduct) primaryBusinessObject;

			
				// Get the SeasonProductlink object for primaryBusinessObject
			try {
				LCSSeasonProductLink spLink = SeasonProductLocator
							.getSeasonProductLink(product);
				com.lcs.wc.team.TeamAssignPlugin.setRolesFromUserLists(spLink);
			} catch (WTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				
	}

	/**
	 * This method will get the value of seasonType attribute whether it is Core or Derivative
	 * 
	 * @param primaryBusinessObject
	 * @return seasonType
	 * @throws WTException
	 */
	public static String getSeasonType(WTObject primaryBusinessObject) throws WTException {
		String seasonType=null;
		//String seasonTypeDisplayName = null;
		

		//FlexType flexType = FlexTypeCache.getFlexTypeFromPath("com.lcs.wc.season.LCSSeason");
		if (primaryBusinessObject instanceof LCSProduct) {
			LCSProduct product = (LCSProduct) primaryBusinessObject;
			LCSSeasonProductLink spLink = SeasonProductLocator.getSeasonProductLink(product);
			LCSSeason lcsSeason = (LCSSeason) VersionHelper.latestIterationOf(spLink.getSeasonMaster());
			seasonType = (String) lcsSeason.getValue(SEASON_TYPE);
			//String seasonTypeDisplayName = lcsSeason.getFlexType().getAttribute("seasonType").getAttValueList().getValue(seasonType, Locale.getDefault());

		}

		if ("newproduct".equalsIgnoreCase(seasonType) || "international".equalsIgnoreCase(seasonType)){
			seasonType = "core";
		}

		/*// Check if primaryBusinessObject is a product
		else if (primaryBusinessObject instanceof LCSProduct) {

			product = (LCSProduct) primaryBusinessObject;

			try {
				// Get the SeasonProductlink object for primaryBusinessObject
				LCSSeasonProductLink spLink = SeasonProductLocator
						.getSeasonProductLink(product);
				if (spLink != null
						&& !("SKU").equalsIgnoreCase(spLink.getSeasonLinkType())) {
					// If primaryBusinessObject is a season-product
					prodtype = "product";
				} else {
					// If product is not associated with season
					prodtype = "NA";
				}
			} catch (WTException e) {
				LCSLog.debug(e.getMessage());
			}
			// If primaryBusinessObject is not of type LCSProduct
		} else {
			prodtype = "NA";
		}

		if (DEBUG) {
			LCSLog.debug("hfWorkflowUtil - Workflow routed for " + prodtype);
		}*/



		return seasonType;
	}


	/**
	 * This method will set the Estimated End Date and complete the given Calendar task.
	 * @param primaryBusinessObject WTObject
	 * @param taskName String
	 * 
	 */

	public static void endCalendarTask(WTObject primaryBusinessObject,
			String taskName) {
		LCSCalendarTaskLogic taskLogic = new LCSCalendarTaskLogic();
		//Timestamp estEndDate;
		Timestamp targetDate;

		try {
			LCSCalendarTask currentTask = LCSCalendarQuery.getTask(
					primaryBusinessObject, taskName);
			if(currentTask!=null)
			{
				targetDate =currentTask.getTargetDate();
				if(targetDate ==null)
				{
					targetDate = currentTask.getEstEndDate();
				}
				// Set EstEndDate to attribute before task is ended
				currentTask.setValue(HFConstants.PROJECTED_DATE, targetDate);
				WTUser wtUser = (WTUser) SessionHelper.manager.getPrincipal();
				currentTask.setValue(HFConstants.RESPONSIBLE_USER, wtUser);
				//currentTask.setValue("hfResponsibleUser", FormatHelper.getNumericObjectIdFromObject(wtUser));
				new LCSCalendarTaskLogic().saveCalendarTask( currentTask, true);

				// Complete the task
				taskLogic.endTask(primaryBusinessObject, taskName);
			}

		} catch (WTException e) {
			LCSLog.debug(e.getMessage());
		} catch (WTPropertyVetoException pve) {
			LCSLog.debug(pve.getMessage());
		}
	}


	/**
	 * This method checks whether the sample is of Material Sample type or not. 
	 * @param primaryBusinessObject WTObject.
	 * @return result.
	 * @throws WTException WTException.
	 */
	public static String getSampleType(WTObject primaryBusinessObject) throws WTException {
		String result = null;
		LCSLog.debug(":::::::inside method getSampleType:::::primaryBusinessObject::" + primaryBusinessObject);
		if(primaryBusinessObject instanceof LCSSample){
			
			LCSSample sampleObj = (LCSSample)primaryBusinessObject;

			String sampleType=sampleObj.getFlexType().getFullName();
			LCSLog.debug(":::::::from inside method :::::sampleType::" + sampleType);

			String str = FlexTypeCache.getFlexTypeFromPath("Sample\\Material\\Color Development").getFullName();
			LCSLog.debug(":::::::from inside method :::::str::" + str);

			if(sampleType.equalsIgnoreCase(str)){
				result = "Yes";
			}
			else
			{
				result = "No";
			}
			LCSLog.debug(":::::::from inside method :::::result::" + result);
		}
		return result;
	}
}
