/*
 * PDFImagePagesCollection.java
 *
 * Created on August 23, 2005, 10:24 AM
 */

package com.hf.wc.product;

import com.lcs.wc.util.*;
import com.lcs.wc.document.*;
import com.lcs.wc.client.web.pdf.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import wt.util.WTException;
import wt.log4j.LogR;
import org.apache.log4j.Logger;

import com.lcs.wc.client.web.pdf.PDFContentCollection;
import com.lcs.wc.document.ImagePagePDFGenerator;
import com.hf.wc.document.HFImagePagePDFGenerator;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.pdf.PdfWriter;
import com.lcs.wc.product.PDFProductSpecificationGenerator2;
import com.lcs.wc.product.PDFImagePagesCollection2;

import wt.util.*;

/**
 *
 * @author  Chuck
 */
public class HFPDFImagePagesCollection2 extends PDFImagePagesCollection2  {
	private static final Logger LOGGER = LogR.getLogger(HFPDFImagePagesCollection2.class.getName());
    public static final boolean INCLUDE_COMMENTS = LCSProperties.getBoolean("com.lcs.wc.product.PDFImagePagesCollection2.includeComments");
    
    /** Creates a new instance of PDFImagePagesCollection */
    public HFPDFImagePagesCollection2() {
    }
    
    public static final String PRODUCT_ID = "PRODUCT_ID";
    public static final String SPEC_ID = "SPEC_ID";
    public static final String PAGE_TYPE = "pageType";
    public static final String IMAGE_PAGES_HEADER_CLASS = "IMAGE_PAGES_HEADER_CLASS";
    public static final String IMAGE_PAGES_FOOTER_CLASS = "IMAGE_PAGES_FOOTER_CLASS";
    
    Collection pageTitles = new ArrayList();
    
    private PdfWriter writer = null;
    
    /** Gets a Collection of PdfPTables representing Image Pages.
     * Use com.lcs.wc.document.ImagesPagePDFGenerator to generatate each page
     *
     * @param params
     * @param document
     * @throws WTException
     * @return
     */    
    public Collection getPDFContentCollection(Map params, Document document) throws WTException {
        ArrayList imageDocs = new ArrayList();
        try{
            if(LOGGER.isDebugEnabled()) LOGGER.debug("Here I am in getPDFContentCollection params: " + params);
            if(!FormatHelper.hasContent((String)params.get(PRODUCT_ID))){
                return imageDocs;
            }
		   this.pageTitles = new ArrayList();
//            WTObject obj = (WTObject)LCSProductQuery.findObjectById((String)params.get(PRODUCT_ID));
//            if(!(obj instanceof LCSProduct)){
//                throw new WTException("Can not use PDFImagePagesCollection on a non-LCSProduct - " + obj);
//            }
//            
//            WTObject obj2 = (WTObject)LCSProductQuery.findObjectById((String)params.get(SPEC_ID));
//            if(obj2 == null || !(obj2 instanceof FlexSpecification)){
//                throw new WTException("Can not use PDFProductSpecificationMeasurements on without a FlexSpecification - " + obj);
//            }
            //FormatHelper.getVersionId(productSeasonRev)
//            LCSProduct product = (LCSProduct)obj;
//            FlexSpecification spec = (FlexSpecification)obj2;

//            Collection ipgs = getImagePages(product, spec, (String)params.get(PAGE_TYPE));
//            Iterator ips = ipgs.iterator();
//            while(ips.hasNext()){
//                doc = (FlexObject)ips.next();
                //contentMap.put(ImagePagePDFGenerator.DOCUMENT_ID, "VR:com.lcs.wc.document.LCSDocument:" + doc.get("LCSDOCUMENT.BRANCHIDITERATIONINFO"));
                //contentMap.put("HEADER_HEIGHT", params.get("HEADER_HEIGHT"));
                params.put(HFImagePagePDFGenerator.DOCUMENT_ID, params.get(HFPDFProductSpecificationGenerator2.COMPONENT_ID));
//                params.put(ImagePagePDFGenerator.DOCUMENT_ID, "VR:com.lcs.wc.document.LCSDocument:" + doc.get("LCSDOCUMENT.BRANCHIDITERATIONINFO"));
                HFImagePagePDFGenerator ippg = new HFImagePagePDFGenerator(writer);
                ippg.setIncludeComment(INCLUDE_COMMENTS);
                //ippg.setIncludeComment(false);
                //Element content = ippg.getPDFContent(contentMap, document);
                Element content = ippg.getPDFContent(params, document);
                imageDocs.add(content);
                String pageTitle = ippg.getPageTitle();
                pageTitles.add(pageTitle);
//            }
        }
        catch(Exception e){
            throw new WTException(e);
        }
        return imageDocs;
    }
    
    
//    private Collection getImagePages(LCSProduct product,FlexSpecification spec, String pageType) throws WTException{
//        try{
//            FlexType documentType = FlexTypeCache.getFlexTypeFromPath("Document");
//            FlexTypeAttribute docNameAtt = documentType.getAttribute("name");
//            String productId = FormatHelper.getObjectId((WTPartMaster)product.getMaster());
//            String specId = FormatHelper.getObjectId((WTPartMaster)spec.getMaster());
//
//            FlexTypeQueryStatement statement = (new LCSProductQuery()).getProductImagesQuery(productId, specId);
//            if(FormatHelper.hasContent(pageType)){
//                statement.appendAndIfNeeded();
//                statement.appendFlexCriteria("pageType", pageType, Criteria.EQUALS);          
//            }
//            statement.appendSortBy(docNameAtt.getSearchResultIndex());
//            SearchResults results = LCSQuery.runDirectQuery(statement);
//            return results.getResults();
//        }catch(Exception e){
//            e.printStackTrace();
//            throw new WTException(e);
//        }
//    }
    
    /** returns the titles for each page of the Image Page Collection
     * @return
     */    
    public Collection getPageHeaderCollection() {
        return this.pageTitles;
    }
    /**
     * debug method is no longer supported, please use log4j logger of the class.
     */
    @Deprecated
    public static void debug(String msg){
        
		if(LOGGER.isDebugEnabled()){
           LOGGER.debug(msg);
        }
    }
    
    public void setPdfWriter(PdfWriter pw) {
        writer = pw;
    }
}
