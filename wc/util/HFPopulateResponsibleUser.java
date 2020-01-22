package com.hf.wc.util;

import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.lcs.wc.calendar.LCSCalendar;
import com.lcs.wc.calendar.LCSCalendarQuery;
import com.lcs.wc.calendar.LCSCalendarTask;
import com.lcs.wc.calendar.LCSCalendarTaskLogic;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.part.LCSPartMaster;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.season.LCSProductSeasonLink;
//import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonProductLink;
import com.lcs.wc.season.SeasonProductLocator;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.VersionHelper;

//import wt.fc.PersistenceServerHelper;
//import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.org.WTUser;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

/**
 * @author Administrator.
 * @version "true" 1.1.
 * HFResponsibleUser class file contains getResponsibleUser method & getResponsibleUserViaCalendarTask method used in workflow tasks.
 * getResponsibleUser method fires on updating product season.
 * getResponsibleUserViaCalendarTask method fires on calendar initialization.
 */

public final class HFPopulateResponsibleUser {

	/**
	 * Hidden Constructor.
	 */
	protected HFPopulateResponsibleUser() {
	}
	private static Logger loggerObject = Logger.getLogger(HFPopulateResponsibleUser.class);
	/**
	 * This method populates responsible user on every calendar task on updating product season.
	 * @param primaryBusinessObject WTObject.
	 * @throws WTException WTException.
	 * @throws WTPropertyVetoException WTPropertyVetoException.
	 */
	public static void getResponsibleUser(WTObject primaryBusinessObject) throws WTException, WTPropertyVetoException {
		LCSProductSeasonLink prodSeasonObj=(LCSProductSeasonLink) primaryBusinessObject;
		LCSProduct product = SeasonProductLocator.getProductARev(prodSeasonObj);
		loggerObject.info(":::::::getResponsibleUser::::::::product:::::::::: ");
		product=(LCSProduct)VersionHelper.latestIterationOf(product.getMaster());
		String prodOid =  FormatHelper.getVersionId(product);
		//loggerObject.info("::::::::::::prodOid" + prodOid);
		LCSCalendar prdSeasonCalendar =  new LCSCalendarQuery().findByOwnerId(prodOid);

		// Enter only if calendar is present
		if(prdSeasonCalendar !=null){
			Collection  ctColl =LCSCalendarQuery.findTasks(prdSeasonCalendar);
			//loggerObject.info(":::::::::::ctColl "+ ctColl);
			Iterator itr = ctColl.iterator();
			while(itr.hasNext()){
				LCSCalendarTask ct = (LCSCalendarTask)itr.next();
				String name = "- Start of Calendar -";
				// Enter only if endDate is null && if task is not start of calendar
				if(!ct.getName().equalsIgnoreCase(name)&& ct.getEndDate()== null){
					String responsibleRole = (String) ct.getValue(HFConstants.RESPONSIBLE_ROLE);
					//loggerObject.info(":::::::::::responsibleRole "+ responsibleRole);
					ifResponsibleRoleExists(responsibleRole, prodSeasonObj,ct);
				}
			}
		}
	}
	public static void ifResponsibleRoleExists(String responsibleRole,LCSProductSeasonLink prodSeasonObj,LCSCalendarTask ct) throws WTException, WTPropertyVetoException{
		loggerObject.info("::::ifResponsibleRoleExists::::");
		if(FormatHelper.hasContent(responsibleRole)){
			FlexObject userFO = (FlexObject)prodSeasonObj.getValue(responsibleRole);
			if(userFO!= null){
				String userId = userFO.getString("OID");
				//loggerObject.info("::::::::::::userId "+ userId);
				WTUser user = (WTUser) LCSQuery.findObjectById((new StringBuilder())
						.append("OR:wt.org.WTUser:").append(userId).toString());
				//loggerObject.info(":::::::::::User "+ user);
				// Set the picked value of user in resposibleUser att of the calendar task
				ct.setValue(HFConstants.RESPONSIBLE_USER, user);
				LCSCalendarTaskLogic.persist(ct);
			}
		}
	}
	/**
	 * This method populates responsible users for each calendar task on initializing calendar from product.
	 * @param primaryBusinessObject WTObject.
	 * @throws WTException WTException.
	 * @throws WTPropertyVetoException WTPropertyVetoException.
	 */
	public static void getResponsibleUserViaCalendarTask(WTObject primaryBusinessObject) throws WTException, WTPropertyVetoException {
		loggerObject.info("::::getResponsibleUserViaCalendarTask::trigerred::");
		// Enter only if object is calendarTask
		if(primaryBusinessObject instanceof LCSCalendarTask){       
			LCSCalendarTask ct= (LCSCalendarTask)primaryBusinessObject;
			LCSCalendar calendar = ct.getCalendar();
			WTObject owner= calendar.getOwner();
			String taskName = "- Start of Calendar -";
			// Enter only if endDate is null && if task is not start of calendar && owner is LCSPartMaster
			if(!ct.getName().equalsIgnoreCase(taskName)&& ct.getEndDate()== null && owner instanceof LCSPartMaster){
				LCSProduct product=(LCSProduct)VersionHelper.latestIterationOf(owner);
				product=(LCSProduct)VersionHelper.latestIterationOf(product.getMaster());
				// Get productSesonLink from product
				LCSSeasonProductLink prodSeasonObj = SeasonProductLocator.getSeasonProductLink(product);
				// Get responsible role from the calendar task
				String responsibleRole = (String) ct.getValue(HFConstants.RESPONSIBLE_ROLE);
				ifResponsibleRoleViaCalendarExists(responsibleRole,prodSeasonObj,ct);
			}
		}

	}


	public static void ifResponsibleRoleViaCalendarExists(String responsibleRole,LCSSeasonProductLink prodSeasonObj,LCSCalendarTask ct) throws WTException, WTPropertyVetoException{
		loggerObject.info("::::ifResponsibleRoleViaCalendarExists::::");
		if(FormatHelper.hasContent(responsibleRole)){
			FlexObject userFO = (FlexObject)prodSeasonObj.getValue(responsibleRole);
			if (userFO!= null)
			{
				String userId = userFO.getString("OID");
				WTUser user = (WTUser) LCSQuery.findObjectById((new StringBuilder()).append("OR:wt.org.WTUser:").append(userId).toString());
				// Set the picked value of user in resposibleUser att of the calendar task
				ct.setValue(HFConstants.RESPONSIBLE_USER, user);
			}
		}

	}
}

