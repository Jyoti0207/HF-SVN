/*
 * HFImagePagePDFGenerator.java
 *
 * Created on August 18, 2005, 10:57 AM
 */

package com.hf.wc.document;

import java.util.*;
import java.io.*;

import com.lcs.wc.util.*;
import com.lcs.wc.document.*;
import com.lcs.wc.client.web.*;
import com.lcs.wc.client.web.pdf.*;
import com.lowagie.text.html.simpleparser.*;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.svg.*;

import wt.util.*;
import wt.content.*;
import wt.method.*;

import wt.log4j.LogR;
import org.apache.log4j.Logger;
/** Class for building an Image Page (for Product Specification view) as a PDF
 *
 * This class can put out a single PDF for an Image Page, and can also be used
 * to create a single content "page" for a larger PDF Document
 *
 * NOTE: Requires iText
 *
 *
 * @author Chuck Schmerling
 */
public class HFImagePagePDFGenerator extends ImagePagePDFGenerator{
    private static final Logger LOGGER = LogR.getLogger(ImagePagePDFGenerator.class.getName());

    private LCSDocument ip;
    
    int rows = 0;
    int cols = 0;
    
    float docWidth = 0;
    float tableWidth = 0;
    float cellWidth = 0;
    
    float vPadding = 15.0f;
    float hPadding = 5.0f;
    
    float cellPadding = 5.0f;
    
    float pageHeight = 0;
    float cellHeight = 0;
    
    boolean includeComments = true;
    String textLayout = "";
    private String pageTitle = "";
    private PdfWriter writer = null;
    private boolean isAImanaged = false;
    
    
    
   
    static final float SQUEEZE_FACTOR = LCSProperties.get("com.lcs.wc.document.PDFGenerator.squeezeFactor", 2.01f);
    static final float HEIGHT_FACTOR = LCSProperties.get("com.lcs.wc.document.PDFGenerator.heightFactor", 1.f);
    
    static final String PARSER = XMLResourceDescriptor.getXMLParserClassName();
    
    public static String DOCUMENT_ID = "DOCUMENT_ID";
    static final LCSDocumentLogic DOC_LOGIC = new LCSDocumentLogic();
    private PDFGeneratorHelper pgh = new PDFGeneratorHelper();
    final static String IMAGE = "image";
    final static String IMAGE_C = "IMAGE";
    final static String CONTENT = "CONTENT";
    final static String VIEWABLE_GEOMETRY = LCSProperties.get("com.lcs.wc.document.imageGeneration.viewableGeometry");  

    
            
    /** Constructor for the class
     */    
    public HFImagePagePDFGenerator() {
    }
    
    /** Creates a new instance of ImagePagePDFGenerator with a PdfWriter
     * as it's parameter
     * @param pw the PdfWriter required to generate an Image from a PdfImportedPage.
     * @throws WTException
     */
    public HFImagePagePDFGenerator(PdfWriter pw) {
        writer = pw;
    }

    /** Creates a new instance of ImagePagePDFGenerator, taking the Image Page (LCSDocument)
     * as it's parameter
     * @param doc the Document required to make the PDF
     * @throws WTException
     */
    public HFImagePagePDFGenerator(LCSDocument doc) throws WTException {
        this.setDocument(doc);
    }
    
    
    /** sets the required Image Page object for creating the PDF
     * @param doc
     * @throws WTException
     */    
    public void setDocument(LCSDocument doc) throws WTException{
        this.ip = doc;
        String layoutType = (String)this.ip.getValue("pageLayout");
        this.pageTitle = (String)this.ip.getValue("name");
        isAImanaged = LCSDocumentLogic.isAImanaged(doc);
        
        if("1x1".equals(layoutType)){
            rows = 1;
            cols = 1;
            
        } else if("1x2".equals(layoutType)){
            rows = 1;
            cols = 2;
            
        } else if("2x1".equals(layoutType)){
            rows = 2;
            cols = 1;
            
        } else if("2x2".equals(layoutType)){
            rows = 2;
            cols = 2;
            
        } else if("2x3".equals(layoutType)){
            rows = 2;
            cols = 3;
            
        } else if("3x2".equals(layoutType)){
            rows = 3;
            cols = 2;
            
        } else if("3x3".equals(layoutType)){
            rows = 3;
            cols = 3;
        }
        
        this.textLayout = (String)doc.getValue("textLayout");
    }
    
    private Map<String, ApplicationData> buildContentMap() throws WTException 
    {
        try{
            this.ip = (LCSDocument) ContentHelper.service.getContents(this.ip);
            Vector<?> data = ContentHelper.getApplicationData(this.ip);
            HashMap<String, ApplicationData> map = new HashMap<String, ApplicationData>();
            
            Iterator<?> i = data.iterator();
            while(i.hasNext()){
                ApplicationData ad = (ApplicationData)i.next();
                map = addToMap(map, ad);
            }
            
            for (int z = 1; z < 10; z++) {
                LCSImage image = (LCSImage)ip.getValue("imageRef" + z);
                if (image == null)
                    continue;
                
                image = (LCSImage) ContentHelper.service.getContents(image);
                ApplicationData primary = (ApplicationData)ContentHelper.getPrimary(image);
                primary.setDescription(IMAGE_C + z);
                map = addToMap(map, primary);
            }
            
            String name, desc = null;
            ApplicationData ad = null;
            boolean needSecondary = true;
            for (int z = 1; z < 10; z++) {
                LCSDocument refDoc = (LCSDocument)ip.getValue("documentRef" + z);
                if (refDoc == null)
                    continue;
                
                refDoc = (LCSDocument) ContentHelper.service.getContents(refDoc);
                ApplicationData primary = (ApplicationData)ContentHelper.getPrimary(refDoc);
                needSecondary = true;
                if (!LCSDocumentLogic.isAImanaged(refDoc) && primary != null) {
                    name = primary.getFileName();
                    if (DOC_LOGIC.isRasterGraphic(name) || LCSDocumentLogic.isPDFCompatible(name, false)) {
                    	if (LOGGER.isDebugEnabled()) {LOGGER.debug("\t referenced primary file name " + name);}
                        primary.setDescription(IMAGE_C + z);
                        map = addToMap(map, primary);
                        needSecondary = false;
                    }
                }
                if (needSecondary) {
                	data = ContentHelper.getApplicationData(refDoc);
                    for (int zz = 0; zz < data.size(); zz++) {
                        ad = (ApplicationData) data.elementAt(zz);
                        desc = ad.getDescription();
                        if (!FormatHelper.hasContent(desc)) {
                        	continue;
                        }
                        if (desc.startsWith(IMAGE_C + "1")) {
                        	ad.setDescription(IMAGE_C + z);
                            map = addToMap(map, ad);
                           if (LOGGER.isDebugEnabled()) {LOGGER.debug("\t image " + z + " : " + ad.getFileName());}
                        } else if (desc.startsWith(CONTENT + "1")) {
                            ad.setDescription(CONTENT + z);
                           if (LOGGER.isDebugEnabled()) {LOGGER.debug("\t content " + z + " : " + ad.getFileName());}
                            map = addToMap(map, ad);
                        } else {
                        	continue;
                        }
                    }
                }
            }

            return map;
        }
        catch(Exception e){
            throw new WTException(e);
        }
    }
        
    private HashMap<String, ApplicationData> addToMap(HashMap<String, ApplicationData> map, ApplicationData ad)
    {
        ArrayList<String> commentList = new ArrayList<String>();
        commentList.addAll(MOAHelper.getMOACollection(FormatHelper.format(ad.getDescription())));
        String commentPrefix = (String) commentList.get(0);
        map.put(commentPrefix, ad);
        return map;
    }
    
    private PdfPCell createPDFCell(ApplicationData data, String key) throws WTException{
        try{
            
            String commentKey = "imageComments" + key.substring(key.length()-1);
            String comments;
            PdfPCell cCell = new PdfPCell(new Paragraph());
            boolean hasComment = false;
            if(this.includeComments){
                comments = (String)this.ip.getValue(commentKey);
                if (isAImanaged && FormatHelper.hasContent(comments)) {
                	hasComment = true;
                	Paragraph par = pgh.multiFontPara(new Paragraph(comments));
                	cCell = new PdfPCell(par);
                } else if(FormatHelper.hasContent(comments)) {
                	hasComment = comments.length() > 7;
                    PdfPTable cCellTable = new PdfPTable(1);
                    StyleSheet style = new StyleSheet();
                    StringReader sr = new StringReader(comments);
                    ArrayList<?> list = HTMLWorker.parseToList(sr, style);
                    Object obj = null;
                    com.lowagie.text.List commentList = null;
                    PdfPCell cell = null;
                    Paragraph par = null;
                    for (Iterator<?> it = list.iterator(); it.hasNext(); ) {
                       obj = it.next();
                       if (obj instanceof Paragraph) {
                            cell = new PdfPCell(pgh.multiFontPara((Paragraph) obj));
                    	} else if (obj instanceof com.lowagie.text.List) {
                            commentList = (com.lowagie.text.List)obj;
                            par = pgh.multiFontPara(new Paragraph(""));
                            cell = new PdfPCell(par);
                            cell.addElement(commentList);
                    	}
                        cell.setBorderWidth(0f);
                        cCellTable.addCell(cell);
                    }
                    cCell = new PdfPCell(cCellTable);
                }
            }
            int cellCols = 2;
            if("Top".equals(textLayout) || "Bottom".equals(textLayout) || !this.includeComments || !hasComment){
                cellCols = 1;
            } 
            PdfPTable cellTable = new PdfPTable(cellCols);

            
            cCell.setBorder(0);
            
            if("Right".equals(textLayout) || "Left".equals(textLayout)){
                cCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            } else {
                cCell.setVerticalAlignment(Element.ALIGN_TOP);
            }
            cCell.setNoWrap(false);
            cCell.setPaddingLeft(hPadding);
            cCell.setPaddingRight(hPadding);
            
            String file = LCSDocumentHelper.service.downloadContent(data);
            
            Image img = null;
            if (LCSDocumentLogic.isPDFCompatible(file)) {
                if (writer == null) {
                    throw new LCSException(RB.DOCUMENT, "nullWriter_ERR", RB.objA);
                }
			 if (LOGGER.isDebugEnabled()) {
               LOGGER.debug("\t The image is PDF");
			 }
                PdfReader reader = new PdfReader(file);
                PdfImportedPage page = writer.getImportedPage(reader, 1);
                img = Image.getInstance(page);
            } else if (file.toLowerCase().endsWith(".svg")) {
                String out = getRaster(file, false);
                if (FormatHelper.hasContent(out)) {
				 if (LOGGER.isDebugEnabled()) {
                 LOGGER.debug("\t The image is Embedded Raster"); 
				 }
                    img = Image.getInstance(out);
                } else {
                    if (writer == null) {
                        throw new LCSException(RB.DOCUMENT, "nullWriter_ERR", RB.objA);
                    }
				 if (LOGGER.isDebugEnabled()) {
                  LOGGER.debug("\t The image is SVG"); 
				 }
                    img = svg2pdf(writer, file);
                }
            } else {
                img = Image.getInstance(file);
            }
            
 
            PdfPCell iCell = new PdfPCell(new Paragraph());
            iCell.setUseBorderPadding(true);
            iCell.setPaddingBottom(vPadding);
            iCell.setPaddingTop(vPadding);
            iCell.setPaddingLeft(hPadding);
            iCell.setPaddingRight(hPadding);
            
            iCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            iCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            iCell.setBorder(0);
            if (hasComment && ("Top".equals(textLayout) || "Bottom".equals(textLayout))) {
            	iCell.setFixedHeight(cellHeight * HEIGHT_FACTOR * .80f);
            } else {
            	iCell.setFixedHeight(cellHeight);
            }
			iCell.setCellEvent(new ImageCellEvent(img, hPadding, vPadding));
            
            if("Top".equals(textLayout) || "Left".equals(textLayout)){
                if(this.includeComments && hasComment){
                    cellTable.addCell(cCell);
                }
                cellTable.addCell(iCell);
            } else{
                cellTable.addCell(iCell);
                if(this.includeComments && hasComment){
                    cellTable.addCell(cCell);
                }
            }
			
			
            
			
			
            PdfPCell cell = new PdfPCell(cellTable);
			System.out.println("cellHeight>>>>"+cellHeight);
            cell.setFixedHeight(cellHeight);
		   /*if(rows==1){  
    			cell.setFixedHeight(cellHeight + 70f); // 100 is max possible.
				System.out.println("cellHeight 1>>>>"+cellHeight+70);
    		}else if(rows==2){
    			cell.setFixedHeight(cellHeight + 40f);  // 50 is max possible.
				System.out.println("cellHeight 2>>>>"+cellHeight+30);
    		}else if(rows==3){
    			cell.setFixedHeight(cellHeight + 35f); // 30 is max possible
				System.out.println("cellHeight 3 >>>>"+cellHeight+10);
    		}*/
			
			
			
			
            cell.setVerticalAlignment(Element.ALIGN_TOP);
            return cell;
        }
        catch(Exception e){
            e.printStackTrace();
            if (e instanceof WTException) {
                throw (WTException)e;
            } else {
                throw new WTException(e);
            }
        }
    }
    
    
    
    /** Create a PdfPTable object which represents the PDF content for the Image Page.
     *
     * NOTE: This will not include any Header
     * @throws WTException
     * @return
     */    
    public PdfPTable createPDFTable() throws WTException{
        PdfPTable table = null;
        try{
            
            table = new PdfPTable(cols);
            table.setWidthPercentage(95.0f);
            
            float p = table.getWidthPercentage() * .01f;
            tableWidth = docWidth * p;
                        
            
            if("Right".equals(textLayout) || "Left".equals(textLayout)){
                //Cell width is different because the columns are narrower
                float padding = hPadding * 4;
                cellWidth = tableWidth/(cols * 2);
                cellWidth = cellWidth - padding;
            }
            else{
                float padding = hPadding * 2;
                cellWidth = tableWidth/(cols);
                cellWidth = cellWidth - padding;
            }
                        
            //Workout cell height
            cellHeight = pageHeight/rows;
            
            Map<String, ApplicationData> contentMap = buildContentMap();
            
            int cells = rows * cols;
     /* 
      Some files, which are stored as 'CONTENT' can be added directly to a PDF.
      *These files are PDF, SVG and TIFF.  The qualification is checked in 'isBadForPDF'.  
      */
            for (int i = 0; i < cells; i++) {
                int indx = i + 1;
                String t = CONTENT + indx;
                ApplicationData ad = (ApplicationData)contentMap.get(t);
                if (isBadGraphics(ad)) {
                    t = IMAGE_C + indx;
                    ad = (ApplicationData)contentMap.get(t);
                }
                if(ad != null) {
                    PdfPCell cell = createPDFCell(ad, t);
                    table.addCell(cell);
                } else {
                    table.addCell("                  ");
                }
            } 
            int numRows = table.getRows().size();
            for (int i = 0; i < numRows; i++) {
                PdfPRow row = table.getRow(i);
                float h = row.getMaxHeights();
                if (h < 50.) { // no graphics in this row.
                    table.deleteRow(i);
                    numRows--;
                    i--;
                }
            }
        }
        catch(Exception e){
            throw new WTException(e);
        } 
        return table;
    }
    
    private boolean isBadGraphics(ApplicationData ad)
    {
        if (ad == null) return true;
        String fileName = ad.getFileName().toLowerCase();
        return !(fileName.endsWith(".ai") ||fileName.endsWith(".pdf") || fileName.endsWith("svg") || fileName.endsWith("tiff") || fileName.endsWith("tif"));
    }
    
    /** Fulfills the requirement of the PDFContent interface
     *
     * provides Interface access to the createPDFTable call
     * @param params The parameters required to look up the correct information for generation
     * @param document a PDF Document which the content will be added to
     * @throws WTException
     * @return a PdfPTable with the Image Page content
     */
    @Override
    public Element getPDFContent(Map params, Document document) throws WTException
    {
        try{
			pageHeight = 530.0f;
            //pageHeight = document.top() - document.bottom();
            docWidth = document.getPageSize().getWidth();
            
            if(FormatHelper.hasContent((String)params.get(DOCUMENT_ID))){
                LCSDocument imageDoc = (LCSDocument)LCSDocumentQuery.findObjectById((String)params.get(DOCUMENT_ID));
                HFImagePagePDFGenerator ipdg = new HFImagePagePDFGenerator(imageDoc);
                ipdg.writer = writer;
                if(params.get("HEADER_HEIGHT") != null){
                    
                    Object hh = params.get("HEADER_HEIGHT");
                    if(hh instanceof Float){
                        pageHeight = pageHeight - ((Float)hh).floatValue();
                    }
                    if(hh instanceof String){
                        pageHeight = pageHeight - (new Float((String)hh)).floatValue();
                    }
                }
				 pageHeight =  pageHeight - 80.0f;
				
				System.out.println("From Image Page >> height >>"+pageHeight);
                ipdg.setDocWidth(docWidth);
                ipdg.setPageHeight(pageHeight);
                ipdg.setIncludeComment(this.includeComments);
                Element table = ipdg.createPDFTable();
                this.pageTitle = ipdg.getPageTitle();
                return table;
            }
        }
        catch(Exception e){
            throw new WTException(e);
        }
        
        return null;
    }
    
    /** Creates a PDF document with the contents of the Image Page passed in
     *
     * NOTE: It is created in the folder as specified in LCSProperties:
     *    "com.lcs.wc.client.web.PDFGenerator.exportLocation"
     * @throws WTException
     */    
    public void createPDF() throws WTException{
        try{
            String filename = this.ip.getValue("name") + ".pdf";
            
            File outFile = new File(FileLocation.PDFDownloadLocationFiles, filename);
            
            outFile = FileRenamer.rename(outFile);
                        
            Document pdfDoc = new Document();
            pdfDoc.setPageSize(PageSize.LETTER);
            
            pageHeight = pdfDoc.top() - pdfDoc.bottom();
            
			if (LOGGER.isDebugEnabled()) {
		  LOGGER.debug("pageHeight: " + pageHeight);
			}
                        
            pdfDoc.open();
            
            docWidth = pdfDoc.getPageSize().getWidth();
			if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("docWidth: " + docWidth); }
            
            pdfDoc.add(createPDFTable());
            pdfDoc.close();
            
        }
        catch(Exception e){
            throw new WTException(e);
        }
    }
        
    /** gets the Width of the PDF document
     * @return gets the Width of the PDF document
     */    
    public float getDocWidth(){
        return docWidth;
    }
    
    void setDocWidth(float docWidth){
        this.docWidth = docWidth;
    }
    
    /** Gets the vertical padding setting for the table containing the pdf Content
     * @return Gets the vertical padding setting for the table containing the pdf Content
     */    
    public float getVPadding(){
        return vPadding;
    }
    
    void setVPadding(float vPadding){
        this.vPadding = vPadding;
    }

    /** Gets the horizontal padding setting for the table containing the pdf Content
     * @return Gets the horizontal padding setting for the table containing the pdf Content
     */    
    public float getHPadding(){
        return hPadding;
    }
    
    void setHPadding(float hPadding){
        this.hPadding = hPadding;
    }

    /** Gets the padding setting for the table containing the pdf Content
     * @return Gets the padding setting for the table containing the pdf Content
     */    
    public float getCellPadding(){
        return cellPadding;
    }
    
    void setCellPadding(float cellPadding){
        this.cellPadding = cellPadding;
    }

    /** gets the height of the PDF document
     * @return gets the height of the PDF document
     */    
    public float getPageHeight(){
        return pageHeight;
    }
    
    void setPageHeight(float pageHeight){
        this.pageHeight = pageHeight;
    }
    
    /** sets whether or not the Image Page should have the comments included in the pdf content
     * @param includeComments sets whether or not the Image Page should have the comments included in the pdf content
     */    
    public void setIncludeComment(boolean includeComments){
        this.includeComments = includeComments;
    }
    
    /** returns whether or not the Image Page will have the comments included in the pdf content
     * @return returns whether or not the Image Page will have the comments included in the pdf content
     */    
    public boolean isIncludeComments(){
        return this.includeComments;
    }
    
    /** Gets the title for the page, if the content is included in a more inclusive
     * document (such as a Product Specification)
     * @return Gets the title for the page
     */    
    public String getPageTitle(){
        return this.pageTitle;
    }
    
    public Image svg2pdf(PdfWriter writer, String contentFile) throws IOException, BadElementException, org.apache.batik.transcoder.TranscoderException
    {
        File file = new File(contentFile);
        String uri = file.toURI().toString();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        org.apache.fop.svg.PDFTranscoder pdf = new org.apache.fop.svg.PDFTranscoder();
        TranscoderInput from = new TranscoderInput(uri);
        TranscoderOutput to = new TranscoderOutput(output);
        pdf.transcode(from, to);
        byte[] buffer = output.toByteArray();
        PdfReader reader = new PdfReader(buffer);
        PdfImportedPage page = writer.getImportedPage(reader, 1);
        return Image.getInstance(page);
     }
    
    
    
    
    
   
    
}
