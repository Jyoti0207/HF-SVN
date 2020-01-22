/*
 * VRDPDFProductSpecificationGenerator2.java
 *
 * Created on August 31, 2005, 9:20 AM
 */

package com.hf.wc.product;

import com.hf.wc.util.HFConstants;
import com.lcs.wc.client.web.*;
import com.lcs.wc.product.LCSProduct;
import com.lcs.wc.specification.FlexSpecification;
import com.lcs.wc.util.*;

import wt.util.WTMessage;

import java.awt.Color;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.Map;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfGState;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
/** Writes the title bar at the top of each PDF Spec page.
 * Writes the title, the "status", and the page number
 */
public class HFPDFProductSpecPageHeaderGenerator extends PdfPageEventHelper {
    private static final Color GREEN = null;
	/**
     * Declared to store header image.
     */
    public Image headerImage;
    /**
     * Declared to store table object.
     */
    public PdfPTable table;
    /**
     * Declared to store state object.
     */
    public PdfGState gstate;
    /**
     * Declared to store template object.
     */
    public PdfTemplate tpl;
    /**
     * Declared to store template object.
     */
    public PdfTemplate tp2;
    /**
     * Declared to store base font object.
     */
    public BaseFont font;
    /**
     * Declared to store header text.
     */
    public String headerTextLeft = "Test1";
    /**
     * Declared to store header text right.
     */
    public String headerTextRight = " ";
    /**
     * Declared to store header text center.
     */
    public String headerTextCenter = "TEST";
    /**
     * Declared to store params.
     */
    public Map params = null;
    
    /**
     * Declared to store product object.
     */
    public LCSProduct product = null;
    /**
     * Declared to store specification object.
     */
    public FlexSpecification spec = null; 
    /**
     * Declared to store font class;
     */
    public String fontClass = "TABLESECTIONHEADER";
    /**
     * Declared to store page numbers class.
     */
    public String pageNumFontClass = "PAGE_NUMBERS";
    /**
     * Declared to store the PDFGeneratorHelper object.
     */
    public PDFGeneratorHelper pgh = new PDFGeneratorHelper();
    /**
     * Declared to store the cell height.
     */
    public float cellHeight = 15.0f;
    private int i = 0;
   // String currentDate=VRDPSDHelper.getCurrentDate();
    /**
     * Generates a document with a header containing Page x of y and with a Watermark on every page.
     * @param args no arguments needed
     */
    public static void main(String args[]) {
        try {
            // step 1: creating the document
            Document doc = new Document(PageSize.A4, 50, 50, 100, 72);
            // step 2: creating the writer
            PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream("test.pdf"));
            // step 3: initialisations + opening the document
            writer.setPageEvent(new HFPDFProductSpecPageHeaderGenerator());
            doc.open();
            // step 4: adding content
            String text = "some padding text ";
            for (int k = 0; k < 10; ++k)
                text += text;
            Paragraph p = new Paragraph(text);
            p.setAlignment(Element.ALIGN_JUSTIFIED);
            doc.add(p);
            // step 5: closing the document
            doc.close();
        }
        catch ( Exception e ) {
			com.lcs.wc.util.LCSLog.stackTrace(e);
        }
    }
 
    /** Sets up the document for having the header written
     * @see com.lowagie.text.pdf.PdfPageEventHelper#onOpenDocument(com.lowagie.text.pdf.PdfWriter, com.lowagie.text.Document)
     * @param writer
     * @param document
     */
    
    public void onOpenDocument(PdfWriter writer, Document document) {
        try {
            
        	
            // initialization of the template
            //CHUCK - Not sure what these numbers mean
            tpl = writer.getDirectContent().createTemplate(100, 100);
            tp2 = writer.getDirectContent().createTemplate(100, 100);
            //CHUCK - Not sure what a bounding box is
            tpl.setBoundingBox(new Rectangle(-20, -20, 100, 100));
            tp2.setBoundingBox(new Rectangle(-20, -20, 100, 100));
            // initialization of the font
            font = BaseFont.createFont("Helvetica", BaseFont.WINANSI, false);

        }
        catch(Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    /* VRD Cutsom code: This method is written to get the header for every techpack page generated.
     * @param writer
     * @param document
     * @return void
     */
    public void onStartPage(PdfWriter writer, Document document){
   
    try{
    	 
    		//VRD Customization:start
    		HFPDFHeader headerData = new HFPDFHeader();
    		headerData.drawContent(document,params);
    		//VRD Customization:End
    	  	}catch(Exception e){
    		LCSLog.stackTrace(e);
    	}
    }	
    

    /** on the end of the page the title bar is written to the page
     * @see com.lowagie.text.pdf.PdfPageEventHelper#onEndPage(com.lowagie.text.pdf.PdfWriter, com.lowagie.text.Document)
     * @param writer
     * @param document
     */
    public void onEndPage(PdfWriter writer, Document document) {
        try{            

            PdfContentByte cb = writer.getDirectContent();
            cb.saveState();
            cb.restoreState();
            PdfContentByte cb1 = writer.getDirectContent();
            cb1.saveState();
            cb1.restoreState(); 
             
 
            Font cellfont = pgh.getCellFont(fontClass, null, "8");
            
            // write the headertable
            PdfPTable table = new PdfPTable(4);
            table.setTotalWidth(document.right() - document.left());
            
            PdfPCell left = new PdfPCell(pgh.multiFontPara(headerTextLeft, cellfont));
            left.setHorizontalAlignment(Element.ALIGN_LEFT);
            left.setFixedHeight(cellHeight);
            left.setBorder(0);
            table.addCell(left);
            
            PdfPCell center = new PdfPCell(pgh.multiFontPara(headerTextCenter, cellfont));
            center.setHorizontalAlignment(Element.ALIGN_CENTER);
            center.setFixedHeight(cellHeight);
            center.setBorder(0);
            table.addCell(center);
            PdfPCell right = new PdfPCell(pgh.multiFontPara(headerTextRight, cellfont));
            right.setHorizontalAlignment(Element.ALIGN_LEFT);
            right.setFixedHeight(cellHeight);
            right.setBorder(0);
            table.addCell(right);
            
			Object[] objB = { Integer.toString(writer.getPageNumber())};
            String text = WTMessage.getLocalizedMessage ( RB.MAIN, "pageOf_LBL", objB );
            Font pageOfFont = pgh.getCellFont(pageNumFontClass, null, "8");
            font = pageOfFont.getCalculatedBaseFont(false);

            PdfPCell pageOf = new PdfPCell(pgh.multiFontPara(text, pageOfFont));
            pageOf.setPaddingRight(font.getWidthPoint("0000", 8));
            pageOf.setHorizontalAlignment(Element.ALIGN_RIGHT);
            pageOf.setBorder(0);
            table.addCell(pageOf);
            
            table.writeSelectedRows(0, -1, document.left(), document.getPageSize().getHeight(), cb);
            
            float textBase = document.getPageSize().getHeight() - 9;
            
            // for odd pagenumbers, show the footer at the left

            float adjust = font.getWidthPoint("000", 8);
            cb.addTemplate(tpl, document.right() - adjust, textBase);
			cb.addTemplate(tp2, document.right() - adjust, textBase);
            cb.saveState();
             
            cb.restoreState();
            
            //Custom Footer
           // PdfContentByte cb1 = writer.getDirectContent();
			//cb.saveState();
            PdfPTable table1 = new PdfPTable(2);
            table1.setWidthPercentage(95F);
			PdfPCell colorCell = new PdfPCell();
			colorCell.setBackgroundColor( new Color(0x3C, 0xB0, 0x43));
			Chunk chnk = new Chunk(HFConstants.TECHPACK_FOOTER, FontFactory.getFont(
					"Arial", 6, Color.BLACK));
			colorCell.setBorder(0);
			
			table1.addCell(colorCell);
			PdfPCell centerCell = new PdfPCell(new Phrase(chnk));
			centerCell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
			centerCell.setBorderWidth(1);
			centerCell.setBorderColor(new Color(0xA9, 0xA9, 0xA9));
			centerCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
			
			table1.addCell(centerCell);
		
			table1.setTotalWidth(document.right() - document.left());
			table1.writeSelectedRows(
					0,
					-1,
					document.left(),
					document.bottom() -16
							+ (font.getAscentPoint("left text", 12) - font
									.getDescentPoint("left text", 12)),
					cb1);
			
			PdfPTable pdfFooterTable = new PdfPTable(1);
			((PdfPTable) pdfFooterTable).getDefaultCell().setBorder(0);
			
			
			pdfFooterTable.setTotalWidth(document.right() - document.left());
			pdfFooterTable.writeSelectedRows(0, -1, document.left(), document.top()+ document.topMargin() + 20, cb1);
			// System.out.println("document.topMargin()::::::::::::::::::"+document.topMargin());
			// pdfFooterTable.writeSelectedRows(0, -1, document.left(),document.bottom() , cb1);
			//System.out.println("document.bottom()::::::::::::::::::"+document.bottom());
			cb1.saveState();
			cb1.restoreState();
        }
      
        catch(Exception e){
            e.printStackTrace();
        }

    }
    
    /** goes back and fills in the page numbers for the title bar on each page
     * @see com.lowagie.text.pdf.PdfPageEventHelper#onCloseDocument(com.lowagie.text.pdf.PdfWriter, com.lowagie.text.Document)
     * @param writer
     * @param document
     */
    
    public void onCloseDocument(PdfWriter writer, Document document) {
        tpl.setColorFill(pgh.getColor(pageNumFontClass));
        tpl.setColorStroke(pgh.getColor(pageNumFontClass));
        tpl.beginText();
        tpl.setFontAndSize(font, 8);
        tpl.setTextMatrix(0, -1);
        //tpl.showText("" + (writer.getPageNumber() - 1));
        tpl.endText();
        
        tp2.setColorFill(pgh.getColor(pageNumFontClass));
        tp2.setColorStroke(pgh.getColor(pageNumFontClass));
        tp2.beginText();
        tp2.setFontAndSize(font, 8);
        tp2.setTextMatrix(0, -1);
		System.out.println("writer.getPageNumber()>>"+writer.getPageNumber());
        tp2.showText("" + (writer.getPageNumber() - 1));
        tp2.endText();
    }
    
}