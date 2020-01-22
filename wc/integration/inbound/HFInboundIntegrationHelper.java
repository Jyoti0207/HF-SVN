package com.hf.wc.integration.inbound;

import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.hf.wc.integration.util.HFIntegrationConstants;
import com.hf.wc.integration.util.HFLogFileGenerator;
import com.hf.wc.integration.util.HFSendEmail;
import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.QueryColumn;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeAttribute;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSQuery;

import wt.query.QueryException;
import wt.queue.ProcessingQueue;
import wt.queue.QueueHelper;
import wt.util.WTException;

/**
 * JCColorwayIntegrationHelper. 
 * Helper class for Colorway Integration.
 * @author ITC Infotech
 * @version 1.0
 */
public final class HFInboundIntegrationHelper {

	/**
	 * Private COnstructor.
	 */
	private HFInboundIntegrationHelper(){

	}
	private final static String SKUAREV	=	"skuarev";
	/**
	 * logger object.
	 */
	static final Logger logger = Logger.getLogger(HFInboundIntegrationHelper.class);	

	/**
	 * getProcessQueue.
	 * @param strQueueName for strQueueName
	 * @return processing queue for processing queue.
	 * @throws WTException 
	 */
	public static synchronized ProcessingQueue getProcessQueue(String strQueueName) throws WTException {
		HFLogFileGenerator.configureLog("SalsifyInboundInfoLog");
		//getting the queue
		logger.info("Start getProcessQueue.............strQueueName: "+strQueueName);
		// Initialize
		ProcessingQueue processQueue = null;
		try {
			processQueue = (ProcessingQueue) QueueHelper.manager.getQueue(strQueueName,	ProcessingQueue.class);
			logger.info("getProcessQueue.............processQueue from queue helper: "+processQueue);
			// Check if the queue exists. If not, create the queue.
			if (processQueue == null) {
				logger.info("Queue does not exist!! Creating a new queue '"+ strQueueName + "'.");
				processQueue = QueueHelper.manager.createQueue(strQueueName);
				// Enabling the queue if not enabled.
				processQueue.setEnabled(true);
				// Starting the queue here if it is not in Started state.
				processQueue = QueueHelper.manager.startQueue(processQueue);		
			}
			//Starting the queue if not started
			if ("STARTED".equals(processQueue.getQueueState())) {
				processQueue = QueueHelper.manager.startQueue(processQueue);
			}
			logger.info("getProcessQueue.............processQueue: "+processQueue);
		}
		catch (QueryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getStackTrace());
			logger.error("getProcessQueue - Exception occured when querying the process queue. Queue Name: "+strQueueName+ ". Error Message : " +e.getLocalizedMessage());
		}catch (WTException e) {
			/** Caught WT Exception. */
			logger.error(e.getStackTrace());
			logger.error("getProcessQueue - Exception occured when retrieving the process queue. Queue Name: "+strQueueName+ ". Error Message : " +e.getLocalizedMessage());
		}
		logger.info("End getProcessQueue.............processQueue: "+processQueue);

		//returning the process queue
		return processQueue;
	}

	/**
	 * getColorwayBasedOnID.
	 * @param itemNumber for itemNumber
	 * @return collection for collection
	 * @throws WTException for WTException
	 */
	public static String getColorwayBasedOnID(String itemNumber, String attributeName) throws WTException{
		logger.info("::::::::::::::::::inside getcolorwaybasedonid::::::!!!");
		HFLogFileGenerator.configureLog("SalsifyInboundInfoLog");
		String idA2A2SkuSeasonLink="";
		FlexType productType = FlexTypeCache.getFlexTypeFromPath("Product");
		FlexTypeAttribute seqAttribute = productType.getAttribute(attributeName);
		PreparedQueryStatement stmt =  new PreparedQueryStatement();
		stmt.appendFromTable(SKUAREV);
		stmt.appendSelectColumn(SKUAREV, "branchIditerationInfo");
		stmt.appendCriteria( new Criteria(SKUAREV, "latestiterationInfo","1","="));
		stmt.appendAndIfNeeded();
		stmt.appendCriteria(new Criteria(new QueryColumn(SKUAREV, seqAttribute.getColumnName()), itemNumber, Criteria.EQUALS));
		logger.info(":::::::::::::::::::::::::::::::::stmt:::"+ stmt);
		Collection queryResult = LCSQuery.runDirectQuery(stmt).getResults();
		Iterator colorwayIr = queryResult.iterator();
		logger.info(":::::::::::::::::::::::::::::::::queryResult:::"+ queryResult);
		while(colorwayIr.hasNext()){
			logger.info(":::::::::::::inside while loop of getColorwayBasedOnID::::::::::::::::: ");
			logger.info("colorwayIrcolorwayIr --->"+colorwayIr);
			FlexObject fObj =  (FlexObject)colorwayIr.next();
			idA2A2SkuSeasonLink = fObj.getString("SKUAREV.BRANCHIDITERATIONINFO");
		}
		logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>idA2A2SkuSeasonLink>>>"+idA2A2SkuSeasonLink);
		return idA2A2SkuSeasonLink;
	}

	/**
	 * sendEmailIfColorwayNotPresent.
	 * @param itemNumber for itemNumber
	 */
	public static void sendEmailIfColorwayNotPresent(String itemNumber){
		HFLogFileGenerator.configureLog("SalsifyInboundInfoLog");
		String mailBody = itemNumber+" Is not present in flex PLM.Can not save data from inbound queue";
		String mailSubject = "Inbound failed for Item Number"+ itemNumber;
		HFSendEmail.sendEmail(mailBody, mailSubject, HFIntegrationConstants.TO_EMAIL_ADDRESS);
		logger.info(">>>>>>>>>>>>>>>sendEmailIfColorwayNotPresent");

	}
}
