package com.hf.wc.season;

import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import com.lcs.wc.calendar.*;
import com.lcs.wc.part.LCSPartMaster;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.season.*;
import com.lcs.wc.util.*;
import com.lcs.wc.wf.WFHelper;
import wt.fc.Persistable;
import wt.fc.WTObject;
import wt.inf.container.*;
import wt.lifecycle.*;
import wt.org.DirectoryContextProvider;
import wt.org.OrganizationServicesHelper;
import wt.org.WTOrganization;
import wt.util.WTException;

/**
 * @author ITCINFOTECH This class is triggered on Product Association to Season
 *         Group.
 */
public final class HFCopySeasonGroupCalendar {
	/**
	 * Variable to store Calendar Type.
	 */
	private final static String[] CALTYPE = LCSProperties.get("com.hf.wc.season.calendarTemplateType").split(",");
	/**
	 * Variable to store LifeCycle Name.
	 */
	private final static String[] LCNAME = LCSProperties.get("com.hf.wc.season.lifecycleName").split(",");
	/**
	 * Constructor object.
	 */
	private HFCopySeasonGroupCalendar() {
	}
	/**
	 * Logger object.
	 */
	private static Logger log = Logger.getLogger(HFCopySeasonGroupCalendar.class.getName());
	 /**
	 * This method is invoked to copy the calendar associated to the SG to
	 * Product being associated to the SG Associates
	 * @param wtObj
	 * @throws WTException
	 */
	public static void copyCalendarTemplate(WTObject wtObj) throws WTException {
		log.info("Copy season Plugin is triggered");
		// Checking the instance of the object fetched.
		if (wtObj instanceof SeasonGroupToProductLink) {
			SeasonGroupToProductLink sgpl = (SeasonGroupToProductLink) wtObj;
			SeasonGroupMaster seasonGrpMstr = sgpl.getSeasonGroupMaster();
			LCSPartMaster partMaster = sgpl.getProductMaster();
			SeasonGroup seasonGroupObj = (SeasonGroup)VersionHelper.latestIterationOf(seasonGrpMstr);
			// Here the Product Object fetched is of 'B' Version.
			LCSProduct productObj = (LCSProduct)VersionHelper.latestIterationOf(partMaster);
			// LCSProductSeasonLink prodSeasonLink = (LCSProductSeasonLink)
			// SeasonProductLocator.getSeasonProductLink(productObj);
			log.info("SeasonGroup Name is>>" + seasonGroupObj.getName());
			log.info("Product Name is>>" + productObj.getName());
			//String oid = FormatHelper.getObjectId(seasonGroupObj);
			//log.info("SeasonGroup oid is:::::::" + oid);
			// Copying the Calendar object from SG to Product.
			LCSCalendarQuery.findByOwner(seasonGroupObj);
			LCSCalendar calendar = LCSCalendarQuery.findByOwner(seasonGroupObj); // (LCSCalendar)query.findByOwner(seasonGrpMstr);
			if (calendar != null) {
				// LCSCalendarQuery.findByOwner(productMaster);
				log.info("Calendar  name is>>" + LCSCalendarQuery.findByOwner(seasonGroupObj));
				// Using LCSCalendarLogic to copy the calendar from SG.
				LCSCalendarLogic calendarLogic = new LCSCalendarLogic();
				// LCSCalendar copyCal = calendarLogic.copyCalendar(calendar);
				LCSCalendarTask firstTask = LCSCalendarQuery.getFirstTask(calendar);
				calendarLogic.initiateCalendar(productObj, calendar, firstTask.getStartDate());
				// calendarLogic.initiateCalendar(productObj, calendarTemplate,calendar.getStartDate());
				String calendarType = calendar.getFlexType().getFullNameDisplay();
				// setLifeCycle method is called to set the product with the lifecylce as per the calendar.
				setLifeCycle(productObj, calendarType);
			}
			// Throws the WTexception to ui if no calendar is associated to the Season Group.
			else {

				throw new LCSException(" Add a Calendar Template to the Season Group: " + seasonGroupObj.getName());
			}
		}
	}

	/**
	 * This method is used to assign the product with new Lifecycle.
	 * @param productObj
	 * @param calendarType
	 * @throws WTException
	 */
	public static void setLifeCycle(LCSProduct productObj, String calendarType) throws WTException {

		
		log.info(":::::::::::setLifeCycle invokedinvoked>>");
	
		Map<String, String> lCName = new HashMap<String, String>();
		int count = 0;
		for (String s : CALTYPE) {
			log.info("lCName::" + s);
			log.info("calType:" + LCNAME[count]);
			lCName.put(s, LCNAME[count]);
			count++;
		}
		log.info("lCName::" + lCName.size());
		log.info("calendarType:>>" + calendarType);
		String lifeCycleName = lCName.get(calendarType);
		log.info("lifeCycleName>>" + lifeCycleName);
		LCSProduct obj = (LCSProduct) productObj;
		// Fetching the LifeCycleManaged object from the given Product Object.
		LifeCycleManaged lifecyclemanaged = (LifeCycleManaged) obj;
		// Setting LifeCycle.
		WTContainerRef exchangeRef = WTContainerHelper.service.getExchangeRef();
		ExchangeContainer container = (ExchangeContainer) exchangeRef.getObject();
		DirectoryContextProvider dcp = container.getContextProvider();
		WTOrganization org = OrganizationServicesHelper.manager.getOrganization(FlexContainerHelper.getOrganizationContainerName(), dcp);
		WTContainerRef containerRef = WTContainerHelper.service.getOrgContainerRef(org);
		// If the lifecycleName dosen't exist the function is returned.
		if (!FormatHelper.hasContent(lifeCycleName)) {
			return;
		}
		LifeCycleTemplateReference lifecycletemplatereference = LifeCycleHelper.service.getLifeCycleTemplateReference(lifeCycleName, containerRef);
		// Assigning the product object attached to the SG with new LC.
		if (lifecycletemplatereference != null) {
			WFHelper.service.terminateAllRelatedProcesses((Persistable) lifecyclemanaged);
			LifeCycleHelper.service.reassign((wt.lifecycle.LifeCycleManaged) lifecyclemanaged,
					lifecycletemplatereference);
		}
	}
}
