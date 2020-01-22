package com.hf.wc.integration.inbound;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.hf.wc.integration.util.HFIntegrationConstants;
import com.hf.wc.integration.util.HFLogFileGenerator;
import com.hf.wc.integration.util.HFSendEmail;

/**
 * HFSalsifyInboundDataReader.
 * Java file to read and write data into staging DB.
 * @author ITC Infotech
 * @version 1.0
 */
public final class HFInboundDataReader {

	/**
	 * Private constructor.
	 */
	private  HFInboundDataReader(){
	}

	/**
	 * logger object.
	 */
	static final Logger logger = Logger.getLogger(HFInboundDataReader.class);	

	/**
	 * colorwaylog constant.
	 */
	public static final String colorwayLog = "ColorwayLog";

	/**
	 * jdbcsqlserver constant.
	 */
	public static final String jdbcsqlserver = "jdbc:sqlserver:";


	/**
	 * AND.
	 */
	public static final String AND = "' AND ";

	static{
		try {
			//Class.forName("com.mysql.jdbc.Driver");
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * getColorwayDataFromTable.
	 * This method is used to get  data from the staging table.
	 * @param ipDatabase for ipDatabase.
	 * @param dbUser for dbUser.
	 * @param dbPass for dbPass.
	 * @param dbPort for dbPort.
	 * @return collection for collection.
	 * @throws SQLException for SQLException.
	 */
	public static final String jdbcoraclethin = "jdbc:oracle:thin:@";
	public static ResultSet getColorwayDataFromTable(String tableName) throws SQLException{

		HFLogFileGenerator.configureLog("SalsifyInboundInfoLog");
		logger.info("::::::::::::::Begin getColorwayDataFromTable");
		Connection conn = null;
		ResultSet resultset = null;
		PreparedStatement stmtQuery = null;
		

		try {
			conn = DriverManager.getConnection(jdbcsqlserver+"//"+HFIntegrationConstants.SALSIFY_INBOUND_STAGING_DB_IP,HFIntegrationConstants.INBOUND_STAGING_DB_USER,HFIntegrationConstants.INBOUND_STAGING_DB_PASS);
			//conn = DriverManager.getConnection(jdbcoraclethin+"//"+ipDatabase+":"+dbPort+"/WIND",dbUser,dbPass);
			final String sqlQuery = "SELECT * FROM "+tableName+" WHERE "+HFIntegrationConstants.INBOUND_STAGING_DB_PROCESSSTATE+"='N'";
			stmtQuery = conn.prepareStatement(sqlQuery);
			resultset = stmtQuery.executeQuery();
			logger.info("is closed >111>"+resultset.isClosed());
			logger.info(":resultset::::inside getColorwayDataFromTable:::::::" + resultset);
		}
		catch (SQLException e) {
			logger.error("SQLException Occured while fetching data from Staging Database::::::" + e);

			String mailBody = " SQLException Occured while fetching data from Staging Database"+ e;
			String mailSubject = "SQL exception during Inbound"; 
			HFSendEmail.sendEmail(mailBody, mailSubject, HFIntegrationConstants.TO_EMAIL_ADDRESS);

		} /* finally{

			if(resultset!=null){
				returnresultset = resultset;


			}

			if(stmtQuery!=null){
				stmtQuery.close();
			}
			if(conn!=null){
				logger.info("inside conn ");
				conn.close();
			}
		}*/
		logger.info("End Of getColorwayDataFromTable");
		logger.info("is closed >>"+resultset.isClosed());
		
		return resultset;

	}

	/**
	 * @param sequenceId
	 * @param colorwayNumber
	 * @param season
	 * @param ipDatabase
	 * @param dbUser
	 * @param dbPass
	 * @param dbPort
	 * @throws SQLException
	 */
	public static void setStateAsProcessed(String itemNumber, String tablename) throws SQLException{

		HFLogFileGenerator.configureLog("SalsifyInboundInfoLog");
		logger.info("Begin  setStateAsProcessed");
		Connection conn1 = null;
		ResultSet rs1 = null;
		PreparedStatement stmtQuery1 = null;
		PreparedStatement ps = null;
		try {

			conn1 = DriverManager.getConnection(jdbcsqlserver+"//"+HFIntegrationConstants.SALSIFY_INBOUND_STAGING_DB_IP,HFIntegrationConstants.INBOUND_STAGING_DB_USER,HFIntegrationConstants.INBOUND_STAGING_DB_PASS);
			//conn1 = DriverManager.getConnection(jdbcoraclethin+"//"+ipDatabase+":"+dbPort+"/WIND",dbUser,dbPass);
			ps = conn1.prepareStatement(
					"UPDATE "+tablename+" SET "+HFIntegrationConstants.INBOUND_STAGING_DB_PROCESSSTATE+" = ? WHERE ITEM_NUMBER"
							+" = ?");
			ps.setString(1,"YES");
			ps.setString(2,itemNumber);
			logger.info("PreparedStatement **** "+ps);
			ps.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQLException Occured while setting status as 'YES' in Staging database while processing Colorway Integration. Item Number is "+itemNumber);
			logger.error("SQLException setStateAsProcessed : " + e );
			logger.error(e.getStackTrace());
			String mailBody = " SQLException Occured while setting status as 'P' in Staging database while processing Colorway Integration. Item Number is "+ e;
			String mailSubject = "SQL exception during Inbound"; 
			HFSendEmail.sendEmail(mailBody, mailSubject, HFIntegrationConstants.TO_EMAIL_ADDRESS);
		}finally{
			if(stmtQuery1!=null){
				stmtQuery1.close();
			}
			if(rs1!=null){
				rs1.close();
			}
			if(conn1!=null){
				conn1.close();
			}		
		}
		logger.info("End Of setStateAsProcessed");
	}
	public static List <String> getColorwayDataFromJDESalesInboundTable(String tableName) throws SQLException{

		HFLogFileGenerator.configureLog("SalsifyInboundInfoLog");
		logger.info("Begin getColorwayDataFromJDESalesInboundTable");
		Connection conn = null;
		ResultSet resultset = null;
		PreparedStatement stmtQuery = null;
		PreparedStatement stmtQuery1 = null;
		ResultSet returnresultset = null;
		List <String> itemNumbersList = new ArrayList<String>();

		try {
			conn = DriverManager.getConnection(jdbcsqlserver+"//"+HFIntegrationConstants.SALSIFY_INBOUND_STAGING_DB_IP,HFIntegrationConstants.INBOUND_STAGING_DB_USER,HFIntegrationConstants.INBOUND_STAGING_DB_PASS);
			//conn = DriverManager.getConnection(jdbcoraclethin+"//"+ipDatabase+":"+dbPort+"/WIND",dbUser,dbPass);
			String sqlQuery = "SELECT * FROM "+tableName+" WHERE "+HFIntegrationConstants.INBOUND_STAGING_DB_PROCESSSTATE+"='N'";

			/*final String sqlQuery = "SELECT * FROM "+HFColorwayIntegationConstants.SALSIFY_INBOUND_TABLENAME+" WHERE "+HFColorwayIntegationConstants.INBOUND_STAGING_DB_PROCESSSTATE+"='R' AND "+HFColorwayIntegationConstants.INBOUND_STAGING_DB_SEQUENCENUMBER +" IS NOT NULL AND "+HFColorwayIntegationConstants.INBOUND_STAGING_DB_SEQUENCENUMBER +" = '' ORDER BY CREATEDDTTTM ASC";*/
			logger.info("Inside data reader Query Executed "+sqlQuery);
			stmtQuery = conn.prepareStatement(sqlQuery);
			resultset = stmtQuery.executeQuery();
			logger.info("is closed >111>"+resultset.isClosed());
			logger.info(":resultset::::inside getColorwayDataFromTable:::::::" + resultset);
			if (tableName.equalsIgnoreCase(HFIntegrationConstants.JDESALES_INBOUND_TABLENAME)){
				String sqlQuery1 = "SELECT DISTINCT ITEM_NUMBER FROM "+HFIntegrationConstants.JDESALES_INBOUND_TABLENAME+" WHERE PROCESSED='N'";
				logger.info(sqlQuery1);
				stmtQuery1 = conn.prepareStatement(sqlQuery1);
				ResultSet resultset1 =  stmtQuery1.executeQuery();
				while(resultset1.next() ){
					String itemNumber = (String) resultset1.getString(1);
					logger.info("itemNumber:" + itemNumber);
					itemNumbersList.add(itemNumber);
				}

			}
		}


		catch (SQLException e) {
			logger.error("SQLException Occured while fetching data from Staging Database::::::" + e);

			String mailBody = " SQLException Occured while fetching data from Staging Database"+ e;
			String mailSubject = "SQL exception during Inbound"; 
			HFSendEmail.sendEmail(mailBody, mailSubject, HFIntegrationConstants.TO_EMAIL_ADDRESS);

		} /* finally{

			if(resultset!=null){
				returnresultset = resultset;


			}

			if(stmtQuery!=null){
				stmtQuery.close();
			}
			if(conn!=null){
				logger.info("inside conn ");
				conn.close();
			}
		}*/
		logger.info("End Of getColorwayDataFromTable");
		logger.info(":::::::::::::::::::::::::returnresultset:::::::::::::::::"+returnresultset);
		logger.info("is closed >>"+resultset.isClosed());
		return itemNumbersList;

	}
	public static ResultSet fetchResultSet(String itemNumber) throws SQLException{

		PreparedStatement stmtQuery = null;
		Connection conn = null;
		String sqlQuery	=null;
		ResultSet resultset = null;
		
		conn = DriverManager.getConnection(jdbcsqlserver+"//"+HFIntegrationConstants.SALSIFY_INBOUND_STAGING_DB_IP,HFIntegrationConstants.INBOUND_STAGING_DB_USER,HFIntegrationConstants.INBOUND_STAGING_DB_PASS);
		sqlQuery = "SELECT TOP 3 * FROM " +HFIntegrationConstants.JDESALES_INBOUND_TABLENAME+ " WHERE ITEM_NUMBER ='"+itemNumber+"' AND PROCESSED ='N' ORDER BY GL_DATE_FISCAL_YEAR DESC";
		logger.info(":::::::::::::::sqlQuery::"+sqlQuery);
		stmtQuery = conn.prepareStatement(sqlQuery);
		resultset = stmtQuery.executeQuery();
		return resultset;
	}
}
