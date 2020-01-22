package com.hf.wc.color;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.lcs.wc.color.LCSColor;
import com.lcs.wc.db.Criteria;
import com.lcs.wc.db.FlexObject;
import com.lcs.wc.db.PreparedQueryStatement;
import com.lcs.wc.db.SearchResults;
import com.lcs.wc.flextype.FlexType;
import com.lcs.wc.flextype.FlexTypeCache;
import com.lcs.wc.foundation.LCSLifecycleManaged;
import com.lcs.wc.foundation.LCSLifecycleManagedLogic;
import com.lcs.wc.foundation.LCSLifecycleManagedQuery;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.sun.xml.rpc.processor.modeler.j2ee.xml.string;

import wt.fc.WTObject;
import wt.util.WTException;

/**
 * @author ITC Infotech
 * consists of following methods:-
 * setLegacyNumber.
 * returnNewFinishCode.
 * getCurrentFinishCode.
 * ifColor.
 */
public final class HFColorPlugins {

	private HFColorPlugins(){

	}
	/**
	 * legacyCodeBOFT to store legacyCodeBOFT.
	 */
	public static final String legacyCodeBOFT = LCSProperties.get("com.hf.wc.color.HFColorPlugins.legacyCodeBOFT", "Business Object\\hfLegacyFinishCode");

	/**
	 * colorCodeBOAttKey to store colorCodeBOAttKey.
	 */
	public static final String colorCodeBOAttKey = LCSProperties.get("com.hf.wc.color.HFColorPlugins.colorCodeBOAttKey", "hfColorCode");

	/**
	 * finishCodeAttKey to store finishCodeAttKey.
	 */
	public static final String finishCodeAttKey = LCSProperties.get("com.hf.wc.color.HFColorPlugins.colorCodeBOAttKey", "hfColorNumber");

	private static Logger loggerObject = Logger.getLogger(HFColorPlugins.class);

	/**
	 * @param obj WTObject.
	 * @throws WTException WTException.
	 */
	public static void setLegacyNumber(WTObject obj) throws WTException{
		loggerObject.info("::::::::::::setLegacyNumber plugin fired:::::::::::::::::::::::::");
		if(obj instanceof LCSColor){
			LCSColor colorObj = (LCSColor) obj;
			ifColor(colorObj);
		}
	}

	/**
	 * @param number
	 * @param currentFinishCode
	 * @param firstCharacter
	 * @return currentFinishCode1.
	 */
	public static String returnNewFinishCode(Integer number, String currentFinishCode, String firstCharacter){
		Integer number1 = null;
		String currentFinishCode1 = null;
		String num = null;
		number1 = number+1;
		if((number1/10) == 0)
		{

			num = "0" + number1.toString();
			currentFinishCode1 = firstCharacter + num;
		}
		else
		{
			loggerObject.info("numberelse:::::::::::::"+number1);
			currentFinishCode1 = firstCharacter + number1;
			loggerObject.info("currentFinishCode:::::::::::::"+currentFinishCode1);
		}
		return currentFinishCode1;
	}


	/**
	 * @param colorObj
	 * @param lcm
	 * @throws WTException WTException
	 */
	public static void getCurrentFinishCode(LCSColor colorObj, LCSLifecycleManaged lcm) throws WTException{
		PreparedQueryStatement pqs = new PreparedQueryStatement();
		pqs.appendFromTable(LCSColor.class);
		FlexType ft = FlexTypeCache.getFlexTypeRoot("Color");
		String colName = ft.getAttribute(finishCodeAttKey).getColumnName();
		pqs.appendSelectColumn("LCSColor", colName);
		//pqs.appendCriteria(new Criteria("Color", "latestIterationInfo","1",Criteria.EQUALS));
		SearchResults sr = LCSQuery.runDirectQuery(pqs);
		Vector vector1 = sr.getResults();
		HashSet<String> vector = new HashSet<>();
		
		
		Iterator itr = vector1.iterator();
		while(itr.hasNext()){
			FlexObject fo = (FlexObject)itr.next();
			loggerObject.info("::::::::::::::::::fo isd::::"+ fo);
			String colorNumber = fo.getString("LCSColor."+  colName);
			vector.add(colorNumber);
		}
		//LCSLifecycleManaged bo = (LCSLifecycleManaged) VersionHelper.latestIterationOf(boMaster);
		//loggerObject.info("bo:::::::::::::"+bo);
		String currentFinishCode = (String)lcm.getValue(colorCodeBOAttKey);
		loggerObject.info("currentFinishCode:::::::::::::"+currentFinishCode);
		colorObj.setValue(finishCodeAttKey, currentFinishCode);
		

		//////////////////////////////////////////////////////////////
		currentFinishCode = getNextValidSeq(currentFinishCode,vector);
		loggerObject.info(":::::setting finid=sh cosde:::"+ currentFinishCode);
		
		loggerObject.info(":::::lcm.setValue(colorCodeBOAttKeysde:::"+ lcm.getValue(colorCodeBOAttKey));
		
		
		lcm.setValue(colorCodeBOAttKey, currentFinishCode);
		loggerObject.info(":::::lcm.setValue(colorCodeBOAttKeysde::post:::"+ lcm.getValue(colorCodeBOAttKey));
		LCSLifecycleManagedLogic.persist(lcm);
		

	}

	private static String getNextValidSeq(String currentFinishCode,HashSet vector) {
		String firstCharacter = currentFinishCode.substring(0,1);
		int firstCharacterValue = firstCharacter.charAt(0);

		String numberSeq = currentFinishCode.substring(currentFinishCode.length()-2);
		Integer number = Integer.parseInt(numberSeq);
		
		
		currentFinishCode = getFinalColorCode(firstCharacterValue,firstCharacter,number,currentFinishCode);
		loggerObject.info("::::::::::::::current finish code is ::::::::"+ currentFinishCode);
		// boolean isValid= false;
		if(vector.contains(currentFinishCode)){
		loggerObject.info(":::::Still here:::::");
			currentFinishCode = getNextValidSeq(currentFinishCode,vector);
			
			loggerObject.info(":::::vector dbcheck::::currentFinishCode:::::"+ currentFinishCode);
		}
	/*	else{
			loggerObject.info(":::else ::vector dbcheck::::currentFinishCode:::::"+ currentFinishCode);
			return currentFinishCode;
			
		}*/
		
		
		return currentFinishCode;
	}

	private static String getFinalColorCode(int firstCharacterValue, String firstCharacter, Integer number,
			String currentFinishCode) {
		
		
		if(number == 99){
			firstCharacter = String
					.valueOf((char) (firstCharacterValue + 1));
			currentFinishCode = firstCharacter + "01";
			//Setting the value of alphanumeric sequence.
			loggerObject.info(":::::if::::currentFinishCode:::::"+ currentFinishCode);
		}
		else{

			currentFinishCode=returnNewFinishCode(number,currentFinishCode,firstCharacter);
			loggerObject.info(":::else::::::currentFinishCode:::::"+ currentFinishCode);
		}
		return currentFinishCode;
	}

	/**
	 * @param colorObj.
	 * @throws WTException WTException.
	 * proceeds only if the object is an instance of color.
	 */
	public static void ifColor(LCSColor colorObj) throws WTException{

		String currentLegacyNumber = (String) colorObj.getValue(finishCodeAttKey);
		loggerObject.info("currentLegacyNumber:::::::::::::"+currentLegacyNumber);
		if(! FormatHelper.hasContent(currentLegacyNumber)){


			LCSLifecycleManagedQuery lifeCycleQuery=new LCSLifecycleManagedQuery();

			Map<String,String> criteria = new HashMap<String,String>();
			FlexType boFT = FlexTypeCache.getFlexTypeFromPath(legacyCodeBOFT);
			loggerObject.info("boFT:::::::::::::"+boFT);
			criteria.put("name","HF_COLOR_NEW_FINISH_CODE");

			Collection attCol=new ArrayList();	
			Collection col=	lifeCycleQuery.findLifecycleManagedsByCriteria(criteria, boFT, attCol, null, null).getResults();
			loggerObject.info("col::::::::::::"+col);
			Iterator it=col.iterator();
			LCSLifecycleManaged lcm=null;
			while(it.hasNext())
			{
				FlexObject flexObj=(FlexObject)it.next();
				String objId=(String) flexObj.get("LCSLIFECYCLEMANAGED.IDA2A2");
				lcm= (LCSLifecycleManaged) LCSQuery.findObjectById("OR:com.lcs.wc.foundation.LCSLifecycleManaged:"+objId);

			}
			//FlexTypeQuery flexTypeQuery = new FlexTypeQuery();
			//SearchResults result = flexTypeQuery.findAllObjectsTypedBy(boFT);
			//LCSPartMaster boMaster = new LCSLifecycleManagedQuery().findPartMasterByName("HF_COLOR_NEW_FINISH_CODE");

			//("HF_COLOR_NEW_FINISH_CODE");

			if(lcm !=null ){

				getCurrentFinishCode(colorObj,lcm);

			}
		}
	}
}