
/*@version 1.4
 * @author Sandhya Gunturu.
 * VRDPDFHeader class is written to get header for every  page generated.
 * This is called from VRDPDFProductSpecPageHeaderGenerator.java
 */
package com.hf.wc.product;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;

import com.hf.wc.util.HFConstants;
import com.lcs.wc.client.web.PDFGeneratorHelper;
import com.lcs.wc.foundation.LCSQuery;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.product.LCSProductQuery;
import com.lcs.wc.season.LCSProductSeasonLink;
import com.lcs.wc.season.LCSSeason;
import com.lcs.wc.season.LCSSeasonQuery;
import com.lcs.wc.season.LCSSeasonMaster;
import com.lcs.wc.season.SeasonProductLocator;
import com.lcs.wc.sourcing.LCSSourcingConfig;
import com.lcs.wc.sourcing.LCSSourcingConfigMaster;
import com.lcs.wc.specification.FlexSpecification;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSLog;
import com.lcs.wc.util.LCSProperties;
import com.lcs.wc.util.VersionHelper;
import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.sun.j3d.utils.image.ImageException;

import wt.fc.WTObject;
import wt.part.WTPartMaster;
import wt.util.WTException;
import wt.util.WTProperties;

/**
 * @author Administrator
 *
 */
/**
 * @author 11430
 *
 */
public class HFPDFHeader {
	
	public static String PRODUCT_ID = "PRODUCT_ID"; 
    public static  String SPEC_ID = "SPEC_ID";
    public static String SEASON_MASTER_ID = "SEASONMASTER_ID";
    public static PDFGeneratorHelper pgh = new PDFGeneratorHelper();
    /*
     * Getting all keys from properties file
     * 
     */
    private static final String IMAGE_NOT_FOUND = LCSProperties.get("com.hf.wc.techpack.ImageNotFoundURL", "\\codebase\\rfa\\images\\ImageNotAvailable_2.png");
    private Image imageNotFound = null;
    /*
     * setting fields default value
     * 
     */
	public static  String wt_home= "";
    public static String imageFile = "";
    private LCSProduct product = null;
    private LCSSeason season = null;
    private LCSProductSeasonLink link = null;
   // private LCSCalendar calendar = null;
    FlexSpecification spec = null;
	LCSSourcingConfig sourceObj = null;
	String seasonId = null;
	Boolean noSeasonSelected = false; /* Fix for IM455450*/
	private static DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	private static final String FORMLABEL = "FORMLABEL";
	private static final String DISPLAYTEXT = "DISPLAYTEXT";
	
	/* This method is called from VRDPDFProductSpecPageHeaderGenerator.java
     *
     * @param doc
     * @param params
     * @return void
     * 
     */
    public void drawContent(Document doc,Map params) throws DocumentException, WTException, IOException{
		try{
			
			LCSProduct productObject =null;
			if (params != null)
			{
				
    		if (!FormatHelper.hasContent((String) params.get(PRODUCT_ID))) {
    			
                throw new WTException(
                        "Can not create PDFProductSpecificationHeader without product_ID");
    		} 
    		product = (LCSProduct) LCSProductQuery.findObjectById((String) params.get(PRODUCT_ID)); // getting product object using product id
    		if (!params.containsKey(SEASON_MASTER_ID) ) {
    			
    			// Fetching season-product link
    			link = (LCSProductSeasonLink) SeasonProductLocator.getSeasonProductLink(product);
				// Fetching season
				season = (LCSSeason) SeasonProductLocator.getSeasonRev(link);
				//	noSeasonSelected = true;  /* Fix for IM455450*/
			}else{
				
				seasonId = (String) params.get(SEASON_MASTER_ID); // getting season object using season master id
				LCSSeasonMaster seasonOBJ = (LCSSeasonMaster) LCSQuery.findObjectById(seasonId);// getting season master object using season id
				if (FormatHelper.hasContent(seasonId)) {
					season = (LCSSeason) VersionHelper.latestIterationOf(seasonOBJ); // getting season object using season master object
				}
				System.out.println(">>>>>>>>>>>>>>SEASON ID PRESENT "+season);
			}
			if(season != null) {
				link = (LCSProductSeasonLink) LCSSeasonQuery.findSeasonProductLink(product,season); // getting season product link using product and season objects
			}
			if(FormatHelper.hasContent((String) params.get(SPEC_ID))){
				spec = (FlexSpecification) LCSProductQuery
                        .findObjectById((String) params.get(SPEC_ID));// getting Specification object using Specification id
				 WTObject sourceWTObject = spec.getSpecSource();
				// sourceObj = LCSSourcingConfigQuery.getEffectiveIteration((LCSSourcingConfigMaster) sourceWTObject, new Date()); // getting SourcingConfig object 

				LCSSourcingConfigMaster scfgMaster = (LCSSourcingConfigMaster) spec.getSpecSource();
				LCSSourcingConfig scfg = (LCSSourcingConfig) VersionHelper.latestIterationOf(scfgMaster);

				sourceObj = (LCSSourcingConfig) VersionHelper.latestIterationOf(scfgMaster);

			}
			if(link !=null){
				
			}
			
			wt_home=WTProperties.getServerProperties().getProperty("wt.home");
			imageFile = FormatHelper.formatOSFolderLocation(wt_home)+FormatHelper.formatOSFolderLocation(IMAGE_NOT_FOUND);					
			imageNotFound = Image.getInstance(imageFile);
			
			PdfPTable headerTable = new PdfPTable(1);
			headerTable.setHorizontalAlignment(Element.ALIGN_CENTER);
			headerTable.setHorizontalAlignment(Element.ALIGN_MIDDLE);
			// Getting first row table for QVC header table 
			PdfPCell cell = createFirstRowDataCell(params);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setHorizontalAlignment(Element.ALIGN_MIDDLE);
			headerTable.addCell(cell);
			// Getting Second row table for QVC header table 
			cell = createSecondRowDataCell(params,"second");
			headerTable.addCell(cell);
			headerTable.setHorizontalAlignment(Element.ALIGN_CENTER);
			headerTable.setHorizontalAlignment(Element.ALIGN_MIDDLE);
			
			//Getting Third Row
			
			cell = createThirdRowDataCell(params);
			headerTable.addCell(cell);
			headerTable.setHorizontalAlignment(Element.ALIGN_CENTER);
			headerTable.setHorizontalAlignment(Element.ALIGN_MIDDLE);
			
			headerTable.setWidthPercentage(95F); 
			headerTable.setSpacingAfter(20.0F);
			headerTable.setHorizontalAlignment(Element.ALIGN_CENTER);
			headerTable.setHorizontalAlignment(Element.ALIGN_MIDDLE);
			doc.add(headerTable);
		}else{
			System.out.println("no value");
		}
		}catch(BadElementException e){
		}
			Class<? extends HFPDFHeader> e = null;
			LCSLog.debug(getClass()+" :  drawContent()>>>> Message String  "+e);
		
	}
    
    /**   
     * Generating First row data cell for header table   
     * @param params
     * @return
     * @throws ImageException
     * @throws MalformedURLException
     * @throws IOException
     * @throws DocumentException
     * @throws WTException
     */
   
    private PdfPCell createFirstRowDataCell(Map params) throws ImageException, MalformedURLException, IOException, DocumentException, WTException {
		// TODO Auto-generated method stub first    
    	
    	
    	
    	float first[] = { 30F, 40F , 30F};
    	
    	PdfPTable firstRowTable= new PdfPTable(first);
    	
    	firstRowTable.addCell(HFPDFHelper.getProductAttributeCell(HFConstants.BRAND, FORMLABEL, DISPLAYTEXT,
						product, season, link,"PRODUCT",0));

		// row 1 start
    	firstRowTable.addCell(HFPDFHelper
				.getProductAttributeCell(HFConstants.DESIGNER, FORMLABEL,
						DISPLAYTEXT, product, season, link,"PRODUCT-SEASON",0));
    	firstRowTable.addCell(HFPDFHelper.getSeasonCell(FORMLABEL, DISPLAYTEXT, season,0));
		
    	firstRowTable.addCell(HFPDFHelper.getProductTypeAttributeCell(HFConstants.DIVISION, FORMLABEL, DISPLAYTEXT,
				product,0));
    	firstRowTable.addCell(HFPDFHelper.getProductAttributeCell(HFConstants.PROGRAM_MANAGER, FORMLABEL, DISPLAYTEXT, product, season, link,"PRODUCT-SEASON",0));
    	firstRowTable.addCell(HFPDFHelper.getCreationtDate(FORMLABEL,
				DISPLAYTEXT,0));
    	firstRowTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
    	
    	
    	PdfPCell dataCell = new PdfPCell(firstRowTable);
    	dataCell.setBorderWidth(0);
    	float width[] = {80F,20F};
    	PdfPTable firstTable = new PdfPTable(width);
    	firstTable.addCell(dataCell);
    	
    	PdfPCell logoCell =createImageCell();
    	firstTable.addCell(logoCell);
    	
    	firstTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
    	PdfPCell finalFirstRowCell = new PdfPCell(firstTable);
    	//finalFirstRowCell.setBorder(0);
		return finalFirstRowCell;	
    }
		
		//secondRowTable
    private PdfPCell createSecondRowDataCell(Map params, String rowName) throws WTException, DocumentException {
	
		float second[] = { 30F, 35F , 35F};
    	PdfPTable secondRowTable= new PdfPTable(second);
		secondRowTable.addCell(HFPDFHelper.getProductAttributeCell(HFConstants.PRODUCT_NUMBER, FORMLABEL, DISPLAYTEXT,
				product, season, link,"PRODUCT",0));
		secondRowTable.addCell(HFPDFHelper.getProductAttributeCell(HFConstants.FAN_SIZES, FORMLABEL, DISPLAYTEXT,
				product, season, link,"PRODUCT",0));
		/*secondRowTable.addCell(HFPDFHelper
				.getCalendarTaskAttributeCell("Stage Gate 0: Project Proposal", HFConstants.SGO_DATE,FORMLABEL,
						DISPLAYTEXT, link,0));*/
		secondRowTable.addCell(HFPDFHelper.getProductAttributeCell(HFConstants.PRODUCT_DESCRIPTION, FORMLABEL, DISPLAYTEXT,
				product, season, link,"PRODUCT",0));
		secondRowTable.addCell(HFPDFHelper.getProductAttributeCell(HFConstants.APPLICATION, FORMLABEL, DISPLAYTEXT,
				product, season, link,"PRODUCT",0));
		secondRowTable.addCell(HFPDFHelper.getCalendarAttribute("Ship Date", FORMLABEL, DISPLAYTEXT,link,"hfTargetDate",0));
		secondRowTable.addCell(HFPDFHelper.getProductAttributeCell(HFConstants.FAMILY_NAME, FORMLABEL, DISPLAYTEXT,
				product, season, link,"PRODUCT",0));
		/*PdfPCell emptyCell =  new PdfPCell(pgh.multiFontPara("",pgh.getCellFont("FORMLABEL", null, null)));;
		emptyCell.setBorderWidth(0);
		secondRowTable.addCell(emptyCell);
		secondRowTable.addCell(emptyCell);*/
		
		secondRowTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
		PdfPCell dataCell = new PdfPCell(secondRowTable);
		dataCell.setBorderWidth(0);
		float width[] = {80F,20F};
    	PdfPTable secondTable = new PdfPTable(width);
    	secondTable.addCell(dataCell);
    	
    	PdfPCell thumbnailCell = createThumbnailImageCell(params);
    	secondTable.addCell(thumbnailCell);
    	secondTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
    	
    	PdfPCell finalSecondRowCell = new PdfPCell(secondTable);
    	//finalSecondRowCell.setBorder(0);
    	return finalSecondRowCell;
    }	
    
    //thirdRowTable
    
    private PdfPCell createThirdRowDataCell(Map params) throws ImageException, MalformedURLException, IOException, DocumentException, WTException {
		// TODO Auto-generated method stub first    
    	
    	
    	
    	float third[] = {30F, 40F, 30F};
    	PdfPTable thirdRowTable = new PdfPTable(third);
    	thirdRowTable.addCell(HFPDFHelper.getProductAttributeCell(HFConstants.SIX_SQUARE_LOCATION, FORMLABEL, DISPLAYTEXT,
				product, season, link,"PRODUCT",0));
    	PdfPCell emptyCell =  new PdfPCell(pgh.multiFontPara("",pgh.getCellFont("FORMLABEL", null, null)));;
		emptyCell.setBorderWidth(0);
		thirdRowTable.addCell(emptyCell);
		thirdRowTable.addCell(emptyCell);
    	thirdRowTable.addCell(HFPDFHelper.getProductAttributeCell(HFConstants.STYLE_PYRAMID, FORMLABEL, DISPLAYTEXT,
				product, season, link,"PRODUCT",0));
    	thirdRowTable.addCell(emptyCell);
		thirdRowTable.addCell(emptyCell);
    	
    	PdfPCell dataCell = new PdfPCell(thirdRowTable);
		dataCell.setBorderWidth(0);
		dataCell.setBorderWidthTop(0);
		float width[] = {100F};
    	PdfPTable thirdTable = new PdfPTable(width);
    	thirdTable.addCell(dataCell);
    	
    	
    	PdfPCell finalThirdRowCell = new PdfPCell(thirdTable);
    	
    	return finalThirdRowCell;
    }	
    

	

	
    
	  /* This method is to get the customer logo on the  first row of header.    
	 * @return
	 * @throws ImageException
	 * @throws BadElementException
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private PdfPCell createImageCell() throws ImageException,BadElementException,MalformedURLException, IOException{
    	Image image = null;
    	String imageURL="";
    	imageURL = LCSProperties.get("com.hf.wc.techpack.header.firstRowOrder.image", "\\codebase\\rfa\\hf\\images\\HunterLogo.PNG");
    	wt_home=WTProperties.getServerProperties().getProperty("wt.home");
    	imageURL = FormatHelper.formatOSFolderLocation(wt_home)+FormatHelper.formatOSFolderLocation(imageURL);
    	PdfPCell imageCell=null;
    	if(imageURL!=null && !imageURL.equals(""))
		{
			File imageFile=new File(imageURL);
			if(imageFile.exists()){
				image = Image.getInstance(imageURL);
			}else{
				image = this.imageNotFound;
			}
		}else{
			image = this.imageNotFound;
		}
		
		//image.scalePercent(100); 
		image.scaleToFit(150f,20f);
		
		imageCell = new PdfPCell(image,false);
		imageCell.setHorizontalAlignment(Element.ALIGN_CENTER);
		imageCell.setVerticalAlignment(Element.ALIGN_TOP);
		//imageCell.setFixedHeight(10F);
		imageCell.setBorderWidthBottom(0);
		imageCell.setBorderWidthTop(0);
		imageCell.setPadding(1);
		return imageCell;		
    }
	
	 /* This method is to get theproduct thumbnail for second row of header.
     * @params params
     * @return PdfPCell
     * 
     */
	private PdfPCell createThumbnailImageCell(Map params) throws WTException{
		try {
            if (!FormatHelper.hasContent((String) params.get(PRODUCT_ID))) {
                throw new WTException(
                        "Can not create PDFProductSpecificationHeader without product_ID");
            }
            LCSProduct product = (LCSProduct) LCSProductQuery
                    .findObjectById((String) params.get(PRODUCT_ID));
            String productThumbnail = "";
            Image image = null;
			
			if(product.getPartPrimaryImageURL() != null)
            {
            productThumbnail = product.getPartPrimaryImageURL();
            System.out.println("productThumbnail1111>>>>"+productThumbnail);
            System.out.println("product.getPartPrimaryImageURL()::::::"+ product.getPartPrimaryImageURL());
            }
            else {
            	image = this.imageNotFound;
            	System.out.println("image22222>>>>>>>>>>>>>"+image);
            }
            //productThumbnail = product.getPartPrimaryImageURL();
            //System.out.println("productThumbnail1111>>>>"+productThumbnail);
            
            productThumbnail = productThumbnail.replaceAll("/Windchill/", "");
            System.out.println("productThumbnail2222>>>>"+productThumbnail);

            if (FormatHelper.hasContent(productThumbnail)) {
            	productThumbnail = wt_home + File.separator + "codebase" + File.separator + productThumbnail;
            	  System.out.println("productThumbnail3333>>>>"+productThumbnail);
				if(new File(productThumbnail).exists()){
					image = Image.getInstance(productThumbnail);
				}else {
					image = this.imageNotFound;
                }
            } else {
            	image = this.imageNotFound;
            }
            //image.scalePercent(1000f);
    		//image.scaleToFit(50f, 3500f);
			image.scaleToFit(50f, 50f);
            PdfPCell cell = new PdfPCell(image, false);
            cell.setPadding(1F);
            //cell.setFixedHeight(20.0F);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_TOP);
            //cell.setBorderWidthLeft(0);
            //cell.setBorderWidthTop(0);
            return cell;
        } catch (Exception e) {
        	LCSLog.debug(getClass()+" :  createThumbnailImageCell()>>>> Message String  "+e);
            throw new WTException(e);
        }
	}
	
	
		
	
	
}
