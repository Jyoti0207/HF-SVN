package com.hf.wc.product; 

import java.awt.Color;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.hf.wc.util.HFConstants;
import com.lcs.wc.client.web.PDFGeneratorHelper;
import com.lcs.wc.document.LCSDocumentLogic;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.part.LCSPartMaster;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSProductQuery;
import com.lcs.wc.product.LCSSKU;
import com.lcs.wc.season.LCSProductSeasonLink;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonMaster;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.season.SeasonProductLocator;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSLog;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.VersionHelper;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

import wt.util.WTException;

/**
 * @author ITC Infotech India Pvt.LTD.
 * @version "true" 1.2 
 * HFCoverPage class is written to display first page as cover page in the PDF generated.
 * This is called from HFPDFProductSpecPageGenerator2.java*
 */
public class HFCoverPage {

	private static PDFGeneratorHelper pgh = new PDFGeneratorHelper();	
	public static final String PAGE_TYPE=LCSProperties.get("com.vrd.wc.techpack.PageType");	// getting image page type from properties files
	//private VRDPSDUtil util =null; 
	public static final String PRODUCT_ID = "PRODUCT_ID"; 
	public static  final String SPEC_ID = "SPEC_ID";
	public static final String SEASON_MASTER_ID = "SEASONMASTER_ID";
	private LCSProductSeasonLink link ;
	public static final String disclaimerText = LCSProperties.get("com.vrd.wc.techpack.Disclaimer");
	static final LCSDocumentLogic DOC_LOGIC = new LCSDocumentLogic();
	private String seasonId="";
	private LCSProduct product ;
	private LCSSeason season ;

	private static final String FORMLABEL = "FORMLABEL";
	private static final String DISPLAYTEXT = "DISPLAYTEXT";

	/**
	 * @param doc
	 * @param tParams
	 * @param keys
	 * @throws WTException
	 * @throws DocumentException
	 */
	public void drawCoverpageContent(Document doc,Map tParams,Collection keys) throws WTException, DocumentException
	{

		LCSLog.debug("Entering drawCoverpageContent >>> Params >> "+tParams);
		LCSProduct productObject =null;
		product = null;
		link = null;
		season = null;

		if (!FormatHelper.hasContent((String) tParams.get(PRODUCT_ID))) {

			throw new WTException(
					"Can not create PDFProductSpecificationHeader without product_ID");
		} 
		// getting product object using product id
		productObject = (LCSProduct) LCSProductQuery.findObjectById((String) tParams.get(PRODUCT_ID)); 
		if (!tParams.containsKey(SEASON_MASTER_ID) ) {

			// Fetching season-product link
			link = (LCSProductSeasonLink) SeasonProductLocator.getSeasonProductLink(productObject);
			// Fetching season
			season = (LCSSeason) SeasonProductLocator.getSeasonRev(link);

		}else{
			// getting season object using season master id
			seasonId = (String) tParams.get(SEASON_MASTER_ID);
			// getting season master object using season id
			LCSSeasonMaster seasonOBJ = (LCSSeasonMaster) LCSQuery.findObjectById(seasonId);
			if (FormatHelper.hasContent(seasonId)) {
				// getting season object using season master object
				season = (LCSSeason) VersionHelper.latestIterationOf(seasonOBJ); 
			}
			LCSLog.debug(">>>>>>>>>>>>>>SEASON ID PRESENT >>>> "+season);
		}
		if(season != null) {
			 // getting season product link using product and season objects
			link = (LCSProductSeasonLink) LCSSeasonQuery.findSeasonProductLink(productObject,season);
		}

		product = productObject;
		addCoverPageContentTable(doc,tParams);




	}

	/**
	 * @param doc
	 * @param tParams
	 * @throws DocumentException
	 * @throws WTException
	 */
	private void addCoverPageContentTable(Document doc, Map tParams)
			throws DocumentException, WTException {
		PdfPTable mainTable = new PdfPTable(1);
		mainTable.setWidthPercentage(95F); 
		mainTable.setSpacingAfter(20.0F);


		PdfPCell table1Cell = new PdfPCell(addCoverPageTable1());
		table1Cell.setBorder(0);
		table1Cell.setBorderWidth(0);
		table1Cell.setPadding(0);
		table1Cell.setUseBorderPadding(false);
		mainTable.addCell(table1Cell);
		PdfPCell spacerCell = new PdfPCell(pgh.multiFontPara(" "));
		spacerCell.setBorder(0);
		mainTable.addCell(spacerCell);
		mainTable.addCell(spacerCell);
		table1Cell = new PdfPCell(addCoverPageTable2(tParams));
		table1Cell.setBorder(0);
		table1Cell.setBorderWidth(0);
		table1Cell.setPadding(0);
		table1Cell.setUseBorderPadding(false);
		mainTable.addCell(table1Cell);
		spacerCell.setBorder(0);
		mainTable.addCell(spacerCell);
		table1Cell = new PdfPCell(addCoverPageTable3());
		table1Cell.setBorder(1);
		table1Cell.setBorderWidth(1);
		table1Cell.setBorderWidthLeft(1);
		table1Cell.setBorderWidthRight(1);
		table1Cell.setPadding(0);
		table1Cell.setUseBorderPadding(false);
		mainTable.addCell(table1Cell);

		//adding main table
		doc.add(mainTable);
	}





	private PdfPCell returnColumnHeaderCell(String label) {
		PdfPCell cell = new PdfPCell(pgh.multiFontPara(label,
				pgh.getCellFont(FORMLABEL, null, null)));
		cell.setBackgroundColor(PDFGeneratorHelper
				.getColor(PDFGeneratorHelper.WHITE));
		//cell.setBorderWidth(0.0f);
		cell.setHorizontalAlignment(Element.ALIGN_LEFT);
		cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
		cell.setPadding(2f);
		//cell.setPadding(0f);
		//cell.setPaddingBottom(0f);
		return cell;
	}

	/**
	 * @return
	 * @throws WTException
	 */
	//coverpage table1
	private PdfPTable addCoverPageTable1() throws WTException {

		float width[] = {15F, 30F, 25F, 30F };
		PdfPTable coverPageTable1 = new PdfPTable(width);

		//	coverPageTable1.setWidthPercentage(100);


		PdfPCell headerCell = new PdfPCell(pgh.multiFontPara("Family Details",
				pgh.getCellFont("HunterFanLabel", null, null)));
		headerCell.setColspan(4);
		headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
		headerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

		//headerCell.setBorderWidth(0);
		//headerCell.setPadding(0);
		//headerCell.setBorderWidthTop(0);
		//setting background color
		headerCell.setBackgroundColor( new Color(0xFF, 0xCC, 0x99));

		//Adding header cell to coverpage table1
		coverPageTable1.addCell(headerCell);

		//Adding attributes to table1
		PdfPCell labelCell = HFPDFHelper.getProductCoverPageLabelCell(HFConstants.ENERGY_STAR,product);
		PdfPCell valueCell = HFPDFHelper.getProductCoverPageAttributeCell(HFConstants.ENERGY_STAR, product);
		coverPageTable1.addCell(labelCell);
		coverPageTable1.addCell(valueCell);
		labelCell = HFPDFHelper.getProductCoverPageLabelCell(HFConstants.CANOPY_TYPE,product);
		valueCell = HFPDFHelper.getProductCoverPageAttributeCell(HFConstants.CANOPY_TYPE, product);
		coverPageTable1.addCell(labelCell);
		coverPageTable1.addCell(valueCell);
		labelCell = HFPDFHelper.getProductCoverPageLabelCell(HFConstants.NUMBER_OF_BLADES,product);
		valueCell = HFPDFHelper.getProductCoverPageAttributeCell(HFConstants.NUMBER_OF_BLADES, product);
		coverPageTable1.addCell(labelCell);
		coverPageTable1.addCell(valueCell);
		labelCell = HFPDFHelper.getProductCoverPageLabelCell(HFConstants.LEVEL_SEVEN_MOTOR_SIZE,product);
		valueCell = HFPDFHelper.getProductCoverPageAttributeCell(HFConstants.LEVEL_SEVEN_MOTOR_SIZE, product);
		coverPageTable1.addCell(labelCell);
		coverPageTable1.addCell(valueCell);
		labelCell = HFPDFHelper.getProductCoverPageLabelCell(HFConstants.NUMBER_OF_BULBS,product);
		valueCell = HFPDFHelper.getProductCoverPageAttributeCell(HFConstants.NUMBER_OF_BULBS, product);
		coverPageTable1.addCell(labelCell);
		coverPageTable1.addCell(valueCell);
		labelCell = HFPDFHelper.getProductCoverPageLabelCell(HFConstants.BULB_TYPE,product);
		valueCell = HFPDFHelper.getProductCoverPageAttributeCell(HFConstants.BULB_TYPE, product);
		coverPageTable1.addCell(labelCell);
		coverPageTable1.addCell(valueCell);
		labelCell = HFPDFHelper.getProductCoverPageLabelCell(HFConstants.WATTS_PER_BULB,product);
		valueCell = HFPDFHelper.getProductCoverPageAttributeCell(HFConstants.WATTS_PER_BULB, product);
		coverPageTable1.addCell(labelCell);
		coverPageTable1.addCell(valueCell);
		labelCell = HFPDFHelper.getProductCoverPageLabelCell(HFConstants.INCLUDED_DOWNROD_LENGTH,product);
		valueCell = HFPDFHelper.getProductCoverPageAttributeCell(HFConstants.INCLUDED_DOWNROD_LENGTH, product);
		coverPageTable1.addCell(labelCell);
		coverPageTable1.addCell(valueCell);
		labelCell = HFPDFHelper.getProductCoverPageLabelCell(HFConstants.SPEEDS,product);
		valueCell = HFPDFHelper.getProductCoverPageAttributeCell(HFConstants.SPEEDS, product);
		coverPageTable1.addCell(labelCell);
		coverPageTable1.addCell(valueCell);
		labelCell = HFPDFHelper.getProductCoverPageLabelCell(HFConstants.SECOND_INCLUDED_DOWNROD_LENGTH,product);
		valueCell = HFPDFHelper.getProductCoverPageAttributeCell(HFConstants.SECOND_INCLUDED_DOWNROD_LENGTH, product);
		coverPageTable1.addCell(labelCell);
		coverPageTable1.addCell(valueCell);
		labelCell = HFPDFHelper.getProductCoverPageLabelCell(HFConstants.PULL_CHAIN_INCLUDED,product);
		valueCell = HFPDFHelper.getProductCoverPageAttributeCell(HFConstants.PULL_CHAIN_INCLUDED, product);
		coverPageTable1.addCell(labelCell);
		coverPageTable1.addCell(valueCell);
		labelCell = HFPDFHelper.getProductCoverPageLabelCell(HFConstants.THIRD_INCLUDED_DOWN_ROD_LENGTH,product);
		valueCell = HFPDFHelper.getProductCoverPageAttributeCell(HFConstants.THIRD_INCLUDED_DOWN_ROD_LENGTH, product);
		coverPageTable1.addCell(labelCell);
		coverPageTable1.addCell(valueCell);
		//labelCell = HFPDFHelper.getProductCoverPageLabelCell(HFConstants.TX_WALL_CONTROL,product);
		//valueCell = HFPDFHelper.getProductCoverPageAttributeCell(HFConstants.TX_WALL_CONTROL, product);
		//coverPageTable1.addCell(labelCell);
		//coverPageTable1.addCell(valueCell);
		//labelCell = HFPDFHelper.getProductCoverPageLabelCell(HFConstants.RX_RECEIVER,product);
		//valueCell = HFPDFHelper.getProductCoverPageAttributeCell(HFConstants.RX_RECEIVER, product);
		//coverPageTable1.addCell(labelCell);
		//coverPageTable1.addCell(valueCell);

		//setting border to coverPageTable1
		coverPageTable1.getDefaultCell().setBorder(Rectangle.NO_BORDER);
		return coverPageTable1;
	}

	/**
	 * @param tParams
	 * @return
	 * @throws WTException
	 */
	//coverpage table2
	private PdfPTable addCoverPageTable2(Map tParams) throws WTException {
		PdfPTable coverPageTable2 = new PdfPTable(4);
		PdfPCell headerLabel= new PdfPCell(pgh.multiFontPara("Model Details",
				pgh.getCellFont("HunterFanLabel", null, null)));
		headerLabel.setColspan(4);
		headerLabel.setHorizontalAlignment(Element.ALIGN_CENTER);
		headerLabel.setVerticalAlignment(Element.ALIGN_MIDDLE);

		headerLabel.setBorderWidthBottom(0);
		//headerLabel.setBorderWidthRight(0);
		//headerLabel.setBorderWidthTop(0);
		//headerLabel.setPadding(0);
		headerLabel.setBackgroundColor( new Color(0xFF, 0xCC, 0x99));
		coverPageTable2.addCell(headerLabel);


		//Column Header
		//CoverPage Display Names
		coverPageTable2.addCell(returnColumnHeaderCell(HFConstants.MODEL_NUMBER_COLORWAY));
		coverPageTable2.addCell(returnColumnHeaderCell(HFConstants.BAR_CODE));
		coverPageTable2.addCell(returnColumnHeaderCell(HFConstants.ESTD_ANNUAL_VOLUME));
		coverPageTable2.addCell(returnColumnHeaderCell(HFConstants.MODEL_COMMENTS));
		Collection selectedColorways  = (Collection) tParams.get("COLORWAYS");
		Iterator itr = selectedColorways.iterator();

		while(itr.hasNext()){

			String skuMasterId = (String) itr.next();
			LCSPartMaster skuMaster = (LCSPartMaster) LCSQuery.findObjectById("OR:com.lcs.wc.part.LCSPartMaster:"+skuMasterId);
			LCSSKU sku = (LCSSKU)VersionHelper.getVersion(skuMaster, "A");

			LCSLog.debug("Selected SKU Name >>>"+sku.getName());
			//Actual Values
			PdfPCell skuCell = null;
			//Adding attributes to Model details Table
			skuCell = (HFPDFHelper.getSKUAttributeCell(HFConstants.MODEL_NUMBERS,DISPLAYTEXT,
					sku));
			//skuCell.setBorderWidth(1);
			coverPageTable2.addCell(skuCell);

			skuCell = (HFPDFHelper.getSKUAttributeCell(HFConstants.UPC,  DISPLAYTEXT,
					sku));
			//skuCell.setBorderWidth(1);
			coverPageTable2.addCell(skuCell);
			//hfEstimatedAnnualVolume
			skuCell = (HFPDFHelper.getSKUAttributeCell(HFConstants.ESTIMATED_ANNUAL_VOLUME,  DISPLAYTEXT,
					sku));
			//skuCell.setBorderWidth(1);
			coverPageTable2.addCell(skuCell);
			//hfModel
			skuCell = (HFPDFHelper.getSKUAttributeCell(HFConstants.SKU_NAME,  DISPLAYTEXT,
					sku));
			//skuCell.setBorderWidth(1);
			coverPageTable2.addCell(skuCell);
		}



		//setting border to coverPageTable2
		coverPageTable2.getDefaultCell().setBorder(Rectangle.NO_BORDER);
		return coverPageTable2;


	}

	/**
	 * @return coverPageTable3.
	 * @throws WTException.
	 */
	private PdfPTable addCoverPageTable3() throws WTException {
		//float first[] = { 100F};
		PdfPTable coverPageTable3 = new PdfPTable(1);
		//PdfPCell familyCommentsCell= new PdfPCell();

		//familyCommentsCell.setHorizontalAlignment(Element.ALIGN_LEFT);
		//familyCommentsCell.setVerticalAlignment(Element.ALIGN_TOP);

		//familyCommentsCell.setPadding(20.0F);

		//coverPageTable3.addCell(familyCommentsCell);
		//coverPageTable3.addCell(returnColumnHeaderCell("Family Comments:"));
		
		//PdfPCell familyCommentsCell = null;
		coverPageTable3.addCell(HFPDFHelper.getProductAttributeCell("hfFamilyComments", FORMLABEL, DISPLAYTEXT,
				product, season, link,"PRODUCT",0));
		
		//PdfPCell dataCell = new PdfPCell(coverPageTable3);
		
    	float width[] = {20F, 80F};
    	PdfPTable firstTable = new PdfPTable(width);
		//familyCommentsCell.setBorderWidth(1);
		//familyCommentsCell.setColspan(100);
    	PdfPCell familyCommentsCell = new PdfPCell(firstTable);
    	
		
		coverPageTable3.addCell(familyCommentsCell);
		
		return coverPageTable3;
	}
}