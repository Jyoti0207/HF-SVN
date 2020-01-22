package com.hf.wc.util;

import com.lcs.wc.calendar.LCSCalendarQuery;
import com.lcs.wc.calendar.LCSCalendarTask;
import com.lcs.wc.calendar.LCSCalendarTaskLogic;
import com.lcs.wc.product.LCSProduct;
import wt.fc.WTObject;
import wt.util.WTException;

/**
 * @author ITC Infotech
 *
 *
 *
 *
 */
public final class HFSkipWFTask {
	
	private HFSkipWFTask(){
		
	}
	/**
	 * @param taskName
	 * @param obj WTObject
	 * @return  String
	 */
	public static String  skipWFTask( String taskName,WTObject obj){
		String skip = "No";
		try {
			if(obj instanceof LCSProduct){
				LCSProduct prodObj = (LCSProduct)obj;
				//System.out.println("::::::::::::::::taskName:::"+ taskName);
				//System.out.println("::::::::::::::::taskName:::"+ isskip.toString());
				LCSCalendarTask currentTask = LCSCalendarQuery.getTask(
						prodObj, taskName);
				
				Boolean isskip = (Boolean) currentTask.getValue(HFConstants.SKIP);
				//System.out.println("::::::::::::::::isskip:::"+ isskip.toString());
				if(isskip){
					LCSCalendarTaskLogic taskLogic = new LCSCalendarTaskLogic();
					taskLogic.startTask(prodObj, taskName);
					HFWorkflowUtil.endCalendarTask(prodObj, taskName);
					skip = "Yes";
				}
			}
		} catch (WTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//returning value of skip---true or false
		return skip;		
	}
}