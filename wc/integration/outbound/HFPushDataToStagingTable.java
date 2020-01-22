package com.hf.wc.integration.outbound;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hf.wc.integration.util.HFIntegrationConstants;
import com.hf.wc.integration.util.HFLogFileGenerator;
import com.lcs.wc.db.PreparedQueryStatement;

import wt.util.WTException;
import wt.util.WTPropertyVetoException;


/**
 * @author ITCINFOTECH
 *
 */
public final class HFPushDataToStagingTable {
	
	private final static String FAILURE	=	"failure";
	private final static String SUCCESS	=	"success";
	private HFPushDataToStagingTable(){
		
	}
	/*
	 * Logger Object for class HFPushDataToStagingTable
	 */
	private static Logger loggerObject = Logger.getLogger(HFPushDataToStagingTable.class);
	/**
	 * jdbcsqlserver constant.
	 */
	public static final String jdbcsqlserver = "jdbc:sqlserver:";

	/**
	 * @param dataMap
	 * @param attKeyList
	 * @param columnName
	 * @param isPart
	 * @param tableName
	 * @return status
	 * @throws WTException
	 * @throws WTPropertyVetoException
	 */
	public static String insertDataInTable(Map dataMap, Collection attKeyList, Map columnName, String isPart, String tableName) throws WTException, WTPropertyVetoException{
		HFLogFileGenerator.configureLog("Outbound");
		loggerObject.info("Column name --> "+columnName +" dataMap--->>>"+dataMap);
		PreparedStatement pstInsertStmnt = null;
		PreparedStatement pstSelectStmnt=null;
		Connection conn = null;
		String status="";
		String columnsList="";
		String updatelist="";
		String valueList="";
		String searchAttKey="";
		
		
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
		 LocalDateTime now = LocalDateTime.now();  
		//Timestamp modifiedTs = productColorwayObj.getModifyTimestamp();
		//loggerObject.info("::::::modifiedTs:::::::::" + modifiedTs);
		String modTs = dtf.format(now);
		loggerObject.info("::::::modTs:::::::::" + modTs);
		
		
		
		//Check the value attKeyList
		if (attKeyList!=null)
		{
			Iterator skuKeyIter= attKeyList.iterator();
			try
			{
				conn = DriverManager.getConnection(jdbcsqlserver+"//"+HFIntegrationConstants.COLORWAY_STAGING_DB_IP,HFIntegrationConstants.COLORWAY_STAGING_DB_USER,HFIntegrationConstants.COLORWAY_STAGING_DB_PASS);
				loggerObject.info("This is the connection::::::::::::::::::::"+conn);
				String query="";
				//iterating the skuKeyIter
				while(skuKeyIter.hasNext()){
					String attKey = (String) skuKeyIter.next();
					System.out.println(":::::::(String)dataMap.get(attKey)::::::::::::"+(String)dataMap.get(attKey));
					System.out.println(":::::::(String) columnName.get(attKey)::::::::::::"+(String) columnName.get(attKey));
					
					updatelist=updatelist.concat((String) columnName.get(attKey)).concat("='").concat((String)dataMap.get(attKey)).concat("',");
					columnsList=columnsList.concat((String)columnName.get(attKey)).concat(",");
					valueList=valueList.concat("'").concat((String)dataMap.get(attKey)).concat("'").concat(",");
				}
					 
				loggerObject.info("columnsList Final"+columnsList);
				//check for isPart
				if ("true".equalsIgnoreCase(isPart)){
					columnsList=columnsList+"PROCESSED,PART,MODIFIED_DATE";
					valueList=valueList+"'NO','YES'"+",'"+modTs+"'";
					updatelist=updatelist+"PROCESSED='NO',PART='YES',MODIFIED_DATE="+"'"+modTs+"'";
					searchAttKey = "skuName";
				}
				else{
					columnsList=columnsList+"PROCESSED,PART,MODIFIED_DATE";
					valueList=valueList+"'NO','NO'"+",'"+modTs+"'";
					updatelist=updatelist+"PROCESSED='NO',PART='NO',MODIFIED_DATE="+"'"+modTs+"'";
					searchAttKey = HFIntegrationConstants.COLORWAY_ITEM_NUMBER;
				
				}
				 String sqlQuery = "SELECT Count(*) as count FROM "+tableName+" WHERE ITEM_NUMBER='"+dataMap.get(searchAttKey)+"'";
				//+" AND MODEL_NAME="+HFIntegrationConstants.MODEL_NAME);
				
				loggerObject.info("Search model query :::::::::::"+sqlQuery);
				pstSelectStmnt = conn.prepareStatement(sqlQuery);
				ResultSet resultset = pstSelectStmnt.executeQuery();
				resultset.next();
					//check for resultset size
					if (resultset.getInt(1)==0){
						StringBuffer strB= new StringBuffer();
						strB.append("INSERT INTO ");
						strB.append(tableName);
						strB.append(" (");
						strB.append(columnsList);
						strB.append(") VALUES ( ");
						strB.append(valueList);
						strB.append(")");
						
						query = strB.toString();
						//query = "INSERT INTO " + tableName + " ("+columnsList+") VALUES ( "+valueList+ ")";
						loggerObject.info(">>>>>>>>>>>>>>>>Inside insert query::::");
					}
					else{
						StringBuffer strB= new StringBuffer();
						
						strB.append("UPDATE ");
						strB.append(tableName);
						strB.append(" SET ");
						strB.append(updatelist);
						strB.append(" WHERE ITEM_NUMBER= '");
						strB.append(dataMap.get(searchAttKey));
						strB.append("'");
						
						
						query = strB.toString();
						//query="UPDATE "+tableName+ " SET "+updatelist+" WHERE ITEM_NUMBER= '"+dataMap.get(searchAttKey)+"'";
						loggerObject.info(">>>>>>>>>>>>>>>else block UPDATE query:::::");
					}
					
					
				status=SUCCESS;
				loggerObject.info("insert/update Query:::::::::::::::::::::::::"+query);
				
				resultset.close();
				pstSelectStmnt.close();
				pstInsertStmnt = conn.prepareStatement(query);
				pstInsertStmnt.execute();
				pstInsertStmnt.close();
				conn.close();
				loggerObject.info("##########GETFLEXOBJECTS - END");
				
			}		
			catch(SQLException e)
			{
				status=FAILURE;
				loggerObject.info("THIS IS THE message from the exception<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<"+e.getMessage());
				e.printStackTrace();
			}			
			catch(Exception e)
			{
				status=FAILURE;
				loggerObject.info("THIS IS THE message from the exception**************************************"+e);
				e.printStackTrace();
			}
		}
		//Return the status
		return status;
	}
	
	/**
	 * @param sbomDataMap
	 * @param sbomAttKeyList
	 * @param sbomColumnName
	 * @param sbomTableName
	 * @return
	 * @throws WTException
	 * @throws WTPropertyVetoException
	 */
	public static String insertSBOMMOADataInTable(Map sbomDataMap, Collection sbomAttKeyList, Map sbomColumnName, String sbomTableName) throws WTException, WTPropertyVetoException{
		HFLogFileGenerator.configureLog("Outbound");
		loggerObject.info("Column name --> "+sbomColumnName +" dataMap--->>>"+sbomDataMap);
		PreparedStatement pstInsertStmnt = null;
		PreparedStatement pstSelectStmnt=null;
		Connection conn = null;
		String sbomStatus="";
		String sbomColumnsList="";
		String sbomUpdatelist="";
		String sbomValueList="";
		String sbomWhereCondition=" WHERE MODEL_NUM='"+sbomDataMap.get("hfItemNumberStr")+"'" +"AND PART_NUMBER='"+sbomDataMap.get("hfmodel")+"'";
		//Check for attKeyList
		if (sbomAttKeyList!=null)
		{
			Iterator sbomSkuKeyIter= sbomAttKeyList.iterator();
			try
			{
				conn = DriverManager.getConnection(jdbcsqlserver+"//"+HFIntegrationConstants.COLORWAY_STAGING_DB_IP,HFIntegrationConstants.COLORWAY_STAGING_DB_USER,HFIntegrationConstants.COLORWAY_STAGING_DB_PASS);
				loggerObject.info("This is the connection"+conn);
				String query="";
				//Iterating the skuKeyIter
				while(sbomSkuKeyIter.hasNext()){
					String attKey = (String) sbomSkuKeyIter.next();
					sbomUpdatelist=sbomUpdatelist.concat((String) sbomColumnName.get(attKey)).concat("='").concat((String)sbomDataMap.get(attKey)).concat("',");
					sbomColumnsList=sbomColumnsList.concat((String)sbomColumnName.get(attKey)).concat(",");
					sbomValueList=sbomValueList.concat("'").concat((String)sbomDataMap.get(attKey)).concat("'").concat(",");
				}
				
				loggerObject.info("columnsList Final"+sbomColumnsList);
					sbomColumnsList=sbomColumnsList+"PROCESSED";
					sbomValueList=sbomValueList+"'NO'";
					sbomUpdatelist=sbomUpdatelist+"PROCESSED='NO'";
					
				
				 String sqlQuery = "SELECT Count(*) as count FROM "+sbomTableName+ sbomWhereCondition;
				//+" AND MODEL_NAME="+HFIntegrationConstants.MODEL_NAME);
				
				loggerObject.info("Search model query "+sqlQuery);
				pstSelectStmnt = conn.prepareStatement(sqlQuery);
				ResultSet resultset = pstSelectStmnt.executeQuery();
				resultset.next();
				//Check for resultset size
					if (resultset.getInt(1)==0){
						query = "INSERT INTO " + sbomTableName + " ("+sbomColumnsList+") VALUES ( "+sbomValueList+ ")";
						loggerObject.info(">>>>>>>>>>>>>>>>Inside resultset");
					}
					else{
						
						query="UPDATE "+sbomTableName+ " SET "+sbomUpdatelist+ sbomWhereCondition;
						loggerObject.info(">>>>>>>>>>>>>>>else block UPDATE");
					}
					
					
				sbomStatus=SUCCESS;
				loggerObject.info("insertQuery:::::::::::::::::::::::::"+query);
				resultset.close();
				pstSelectStmnt.close();
				pstInsertStmnt = conn.prepareStatement(query);
				pstInsertStmnt.execute();
				pstInsertStmnt.close();
				conn.close();
				loggerObject.info("##########GETFLEXOBJECTS - END");
				
			}		
			catch(SQLException e)
			{
				sbomStatus=FAILURE;
				loggerObject.info("THIS IS THE message from the exception<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<"+e.getMessage());
				e.printStackTrace();
			}			
			catch(Exception e)
			{
				sbomStatus=FAILURE;
				loggerObject.info("THIS IS THE message from the exception**************************************"+e);
				e.printStackTrace();
			}
		}
		//Return status
		return sbomStatus;
	}
}
