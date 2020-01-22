package com.hf.wc.product;

import wt.folder.Folder;
import java.io.*;
import java.util.*;
import org.apache.log4j.Logger;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import wt.fc.ObjectIdentifier;
import wt.fc.PersistenceHelper;
import wt.part.WTPart;
import wt.part.WTPartMaster;
import wt.pds.StatementSpec;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.vc.wip.WorkInProgressHelper;
/**
 * @author ITCINFOTECH 
 * This class Used to Set the Given WTPart as END ITEM.
 */
public final class HFEnditem {
	
	/**
	 * Variable to store finishCode att key.
	 */
	private  static String filePath ="  ";
	/**
	 * Constructor object.
	 */
	private HFEnditem()
	{
		
	}
	/**
	 * Logger object.
	 */
	private static Logger log = Logger.getLogger(HFEnditem.class.getName());
		public static void main(String args[])throws IOException, WTException, WTPropertyVetoException{
		Map <String,String> endItemList = excelReader(args[0]);
		setEndItem(endItemList);
	}
	/**
	 * This method is invoked to fetch the MOA table values
	 * 
	 * @param partObj WTPart.
	 * @throws WTPropertyVetoException 
	 * @throws Exception.
	 */
	public static void setEndItem(Map<String, String> endItemlist) throws WTException, WTPropertyVetoException {
		String partNumber	=	"";
		//String modelNumber	=	"";
		QuerySpec qs		=	null;
		WTPartMaster partmaster;
		WTPart part;
		//looping through to find all part numbers to be set as end item.
		for (Map.Entry storedList : endItemlist.entrySet()) {
			log.info(storedList.getKey() + "\t" + storedList.getValue() + "\n");
			partNumber	=	(String)storedList.getKey();
			//modelNumber	=	(String)StoredList.getValue() ;
			//finding the Latest version of the WTPart object
			qs	=	new QuerySpec(WTPartMaster.class);
			qs.appendWhere(new SearchCondition(WTPartMaster.class, WTPartMaster.NUMBER, "=", partNumber.trim()),new int[] { 0, 1 });
			wt.fc.QueryResult qr = PersistenceHelper.manager.find((StatementSpec) qs);
			if (qr.size() == 0){
				 log.info("The WTPart Obj is not found in Windchill:"+partNumber.trim());
			}
			else {
				while (qr.hasMoreElements()) {
					partmaster = (WTPartMaster) qr.nextElement();
					// fetching the part object from WTPartMaster object
					part = (WTPart) wt.vc.VersionControlHelper.service.allIterationsOf(partmaster).nextElement();
					if (!partmaster.isEndItem()) {
						long partId = part.getPersistInfo().getObjectIdentifier().getId();
						String id = String.valueOf(partId);
						String wt = "wt.part.WTPart:" + id;
						// fetching the WTPart which is in current usage state
						final Folder folder = (Folder) WorkInProgressHelper.service.getCheckoutFolder();
						ObjectIdentifier oid = ObjectIdentifier.newObjectIdentifier(wt);
						part = (WTPart) PersistenceHelper.manager.refresh(oid);
						// Checkout the working copy of the WTPart only when its not checkedout
						if (!WorkInProgressHelper.isCheckedOut(part)){
						part = (WTPart) WorkInProgressHelper.service.checkout(part, (wt.folder.Folder) folder, null).getWorkingCopy();
						}
						// Set the latest version of WTPart as end Item
						partmaster.setEndItem(true);
						PersistenceHelper.manager.save(partmaster);
						// Checkin the end item.
						part = (WTPart) WorkInProgressHelper.service.checkin(part, null);
						partmaster.isEndItem();
					}
				}
			}
		}
	}
	/**
	 * This method is invoked to read the WTPart numbers 
	 * from given EXCEL FILE
	 * @param filepath 
	 * @param partObj WTPart.
	 * @throws Exception.
	 */
	public static Map<String, String> excelReader(String filepath) throws IOException {
		// Creating a Workbook from an Excel file (.xls or .xlsx)
		Workbook workbook = null;
		HashMap<String, String> endItemlist = new HashMap<String, String>();
		//BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		log.info("Please Enter the file path");
		// Reading data using readLine
		filePath =  filepath;
		if (filePath != null)
		{ 
			try {
				workbook = WorkbookFactory.create(new File(filePath));
			} catch (EncryptedDocumentException | org.apache.poi.openxml4j.exceptions.InvalidFormatException
					| IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(workbook !=null){
			Iterator <Sheet>itr = workbook.sheetIterator();
			while (itr.hasNext()){
				Sheet sheet = itr.next();
			/*}
			if (workbook.getNumberOfSheets() > 0) {
				// Getting the Sheet at index zero
				Sheet sheet = workbook.getSheetAt(0);*/
				// Create a DataFormatter to format and get each cell's value as
				// String
				DataFormatter dataFormatter = new DataFormatter();
				// for-each loop to iterate over the rows and columns
				for (Row row : sheet) {
					Cell modelcell = row.getCell(1);
					Cell endItem = row.getCell(3);
					String modelNumber = dataFormatter.formatCellValue(modelcell);
					String endItemNumber = dataFormatter.formatCellValue(endItem);
					endItemNumber = endItemNumber.concat(".ASM");
					endItemlist.put(endItemNumber.trim(), modelNumber.trim());
				}
				endItemlist.remove("Drawing Number.ASM");
			}
			
			// Closing the workbook
			workbook.close();
			}
		}
		return endItemlist;
	}
}

