package com.hf.wc.calendar;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.lcs.wc.calendar.LCSCalendar;
import com.lcs.wc.calendar.LCSCalendarQuery;
import com.lcs.wc.calendar.LCSCalendarTask;
import com.lcs.wc.calendar.LCSCalendarTaskClientModel;
import com.lcs.wc.calendar.LCSCalendarTaskLogic;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSException;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.SortHelper;

import wt.fc.WTObject;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;




public class HFCalendarTaskPlugins
{
	private static final String TARGET_DATE_ATT = LCSProperties.get("com.lcs.wc.calendar.reverseCalculateTargetDates.newTargetDateAttribute", "targetDate");

	public HFCalendarTaskPlugins() {}

	private static Logger loggerObject = Logger.getLogger(HFCalendarTaskPlugins.class);
	public static final void forwardCalculateEstEndDate(WTObject wtobject)
			throws WTException, WTPropertyVetoException
	{
		loggerObject.info("Inside calendar task Plugins!");
		if (wtobject instanceof LCSCalendarTask){
			LCSCalendarTask task = (LCSCalendarTask)wtobject;
			/*  
		  int duration = (Integer)(((int)((long)task.getValue("hfDuration"))));
		  //loggerObject.info(":::::::::::::::duration is::::"+ duration);
		  Calendar dateCal = Calendar.getInstance();
		  Timestamp estStartDate = task.getEstStartDate();
		 // loggerObject.info("::::::::::::est start date:::::::"+estStartDate);
		  dateCal.setTime(estStartDate);
		  dateCal.add(Calendar.DATE,duration);
		  Date estEndDate = dateCal.getTime();
		  Timestamp estEndDateTimestamp=new Timestamp(estEndDate.getTime());  
		  task.setEstEndDate(estEndDateTimestamp);
		  task.setDuration(1);
		  //loggerObject.info("::::::::::::::est end date:::::::"+task.getEstEndDate());
		  LCSCalendarTaskLogic.persist(task,true);

	  }

			 */

			LCSCalendarTask task1 =   deriveCustomTimeline(task);
			task1.setDuration(1);
			LCSCalendarTaskLogic.persist(task1,true);
		} 



	}

	public static LCSCalendarTask deriveCustomTimeline(LCSCalendarTask task)
			throws WTException
	{
		try
		{

			LCSCalendarTask precedent = task.getPrecedent();
			loggerObject.info("::::::::::::precedent:::::::::"+ precedent);
			if(precedent != null && precedent.getEndDate() == null)
			{

				task.setStartDate(null);
				task.setEndDate(null);
			}
			if(task.getStartDate() == null)
			{
				Calendar dateCal = Calendar.getInstance();
				dateCal.setTime(precedent.getEstEndDate());
				dateCal.add(5, 1);

				dateCal.add(5, task.getEndLag());

				task.setEstStartDate(new Timestamp(dateCal.getTime().getTime()));
			} else
			{

				task.setEstStartDate(task.getStartDate());
			}
			if(task.getEndDate() == null)
			{
				Calendar dateCal = Calendar.getInstance();
				if(task.getStartDate() == null)
				{
					dateCal.setTime(task.getEstStartDate());
				} else
				{
					dateCal.setTime(task.getStartDate());
				}

				dateCal.add(5, ((int)((long)task.getValue("hfDuration"))));

				task.setEstEndDate(new Timestamp(dateCal.getTime().getTime()));
				loggerObject.info("::::::::::::EstEndDate:::::::::"+ task.getEstEndDate());
				loggerObject.info("::::::::::::EstStsrtDate:::::::::"+ task.getEstStartDate());
			} else
			{

				task.setEstEndDate(task.getEndDate());
				loggerObject.info(":::::else:::::::EstEndDate:::::::::"+ task.getEstEndDate());
				loggerObject.info("::::::else::::::EstStsrtDate:::::::::"+ task.getEstStartDate());
			}

		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new WTException(e);
		}
		Timestamp sd;
		if(task.getStartDate() == null)
		{
			sd = task.getEstStartDate();
		} else
		{
			sd = task.getStartDate();
		}
		if(task.getEstEndDate().getTime() < sd.getTime())
		{
			loggerObject.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			loggerObject.info((new StringBuilder()).append("Name: ").append(task.getName()).toString());
			loggerObject.info((new StringBuilder()).append("Est End Date: ").append(task.getEstEndDate()).toString());
			loggerObject.info((new StringBuilder()).append("Est Start Date: ").append(task.getEstStartDate()).toString());
			loggerObject.info((new StringBuilder()).append("Start Date: ").append(task.getStartDate()).toString());
			loggerObject.info((new StringBuilder()).append("End Date: ").append(task.getEndDate()).toString());
			loggerObject.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			throw new LCSException("End Date cannot be earlier than Start Date");
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(task.getEstEndDate());

		cal.setTime(task.getEstStartDate());

		if(task.getStartDate() != null)
		{
			cal.setTime(task.getStartDate());
		}
		if(task.getEndDate() != null)
		{
			cal.setTime(task.getEndDate());
		}
		return task;
	}



	public static final WTObject reverseCalculateTargetDates(WTObject wtobject)
			throws WTException
	{


		if ((wtobject instanceof LCSCalendar))
		{

			LCSCalendar lcscalendar = (LCSCalendar)wtobject;
			loggerObject.info(":::::::lcscalendar.getType()::::::::::plugin trigerred:::::::::: "+lcscalendar.getType());
			if(!lcscalendar.isTemplate()){
				loggerObject.info(":::::::reverseCalculateTargetDates::::::::::plugin trigerred:::::::::: ");
				Hashtable<String, LCSCalendarTask> newTasks = new Hashtable();

				String targetDateAtt = TARGET_DATE_ATT.trim();
				if (!lcscalendar.getFlexType().getAttributeKeyList().contains(targetDateAtt.toUpperCase())) {
					throw new WTException("ERROR: Calendar type definition does not have a soft attribute with internal name '" + targetDateAtt + "'. see Javadoc for plugin method LCSCalendarTaskPlugins.reverseCalculateTargetDates.");
				}
				Date finalTargetDate = (Date)lcscalendar.getValue(targetDateAtt);

				try
				{
					List<LCSCalendarTask> calTasks = new Vector();
					calTasks.addAll(LCSCalendarQuery.findTasks(lcscalendar));
					SortHelper.sortByComparableAttribute(calTasks, "targetDate");


					if (finalTargetDate != null) {
						for (LCSCalendarTask lcscalTask : calTasks) {
							lcscalTask.setTargetDate(new Timestamp(finalTargetDate.getTime()));
							newTasks.put(lcscalTask.getName(), lcscalTask);
						}

						boolean isDone = false;

						while (!isDone) {
							isDone = true;
							Hashtable<String, LCSCalendarTask> tasksToAdd = new Hashtable();
							for (String key : newTasks.keySet()) {
								LCSCalendarTask lcscalTask = (LCSCalendarTask)newTasks.get(key);
								LCSCalendarTask lcscalPreTask = lcscalTask.getPrecedent();

								if (lcscalPreTask != null)
								{
									Calendar mathCal = Calendar.getInstance();
									mathCal.setTime(lcscalTask.getTargetDate());
									mathCal.add(5, -lcscalTask.getEndLag());
									mathCal.add(5, - (Integer)(((int)((long)lcscalTask.getValue("hfDuration"))) + 1));
									mathCal.set(11, 12);
									mathCal.set(14, 0);
									mathCal.set(12, 0);
									mathCal.set(13, 0);
									Date curTargetDate = lcscalPreTask.getTargetDate();
									if (curTargetDate.after(mathCal.getTime())) {
										lcscalPreTask.setTargetDate(new Timestamp(mathCal.getTime().getTime()));
										tasksToAdd.put(lcscalPreTask.getName(), lcscalPreTask);
										isDone = false;
									}
								}
							}
							newTasks.putAll(tasksToAdd);
						}


						isDone = false;
						while (!isDone) {
							isDone = true;
							Hashtable tasksToAdd = new Hashtable();
							for (String key : newTasks.keySet()) {
								LCSCalendarTask lcscalTask = (LCSCalendarTask)newTasks.get(key);
								LCSCalendarTask lcscalPreTask = lcscalTask.getPrecedent();

								if (lcscalPreTask != null) {
									Calendar mathCal = Calendar.getInstance();
									mathCal.setTime(lcscalPreTask.getTargetDate());
									mathCal.add(5, lcscalTask.getEndLag());
									mathCal.add(5, (Integer)(((int)((long)lcscalTask.getValue("hfDuration"))) + 1));
									mathCal.set(11, 12);
									mathCal.set(14, 0);
									mathCal.set(12, 0);
									mathCal.set(13, 0);
									Date curTargetDate = lcscalTask.getTargetDate();
									if (curTargetDate.after(mathCal.getTime())) {
										lcscalTask.setTargetDate(new Timestamp(mathCal.getTime().getTime()));
										tasksToAdd.put(lcscalTask.getName(), lcscalTask);
										isDone = false;
									}
								}
							}
							newTasks.putAll(tasksToAdd);
						}
					}


					LCSCalendarTaskClientModel taskModel = new LCSCalendarTaskClientModel();
					for (String key : newTasks.keySet()) {
						LCSCalendarTask lcscalTask = (LCSCalendarTask)newTasks.get(key);
						taskModel.load(FormatHelper.getObjectId(lcscalTask));
						taskModel.setTargetDate(lcscalTask.getTargetDate());
						taskModel.save();
					}
				} catch (WTPropertyVetoException wtpropertyvetoexception) { Hashtable<String, LCSCalendarTask> tasksToAdd;
				LCSCalendarTaskClientModel taskModel;
				wtpropertyvetoexception.printStackTrace();
				throw new WTException(wtpropertyvetoexception);
				}
			}
		}
		return wtobject;
	}

	static Class _mthclass$(String s) {
		try {
			return Class.forName(s);
		} catch (ClassNotFoundException classnotfoundexception) {
			throw new NoClassDefFoundError(classnotfoundexception.getMessage());
		}
	}
}