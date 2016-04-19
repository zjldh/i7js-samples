/*

    This file is part of the iText (R) project.
    Copyright (c) 1998-2016 iText Group NV

*/

package com.itextpdf.samples.sandbox.images;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.xobject.PdfImageXObject;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.samples.GenericTest;
import com.itextpdf.test.annotations.type.SampleTest;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

@Category(SampleTest.class)
public class LargeImage2 extends GenericTest {
    public static final String SRC = "./src/test/resources/pdfs/large_image.pdf";
    public static final String DEST = "./target/test/resources/sandbox/images/large_image2.pdf";

    public static void main(String[] args) throws Exception {
        File file = new File(DEST);
        file.getParentFile().mkdirs();
        new LargeImage2().manipulatePdf(DEST);
    }

    @Override
    protected void manipulatePdf(String dest) throws Exception {
        PdfReader reader = new PdfReader(new FileInputStream(SRC));
        reader.setCloseStream(false);

        File tmp = File.createTempFile("large_image", ".pdf", new File("."));
        tmp.deleteOnExit();

        PdfDocument tempDoc = new PdfDocument(reader, new PdfWriter(new FileOutputStream(tmp)));
        Rectangle rect = tempDoc.getPage(1).getPageSize();

        if (rect.getWidth() < 14400 && rect.getHeight() < 14400) {
            System.out.println("The size of the PDF document is within the accepted limits");
            System.exit(0);
        }

        PdfDictionary pageDict = tempDoc.getPage(1).getPdfObject();
        PdfDictionary pageResources = pageDict.getAsDictionary(PdfName.Resources);
        PdfDictionary pageXObjects = pageResources.getAsDictionary(PdfName.XObject);
        PdfName imgRef = pageXObjects.keySet().iterator().next();
        PdfStream imgStream = pageXObjects.getAsStream(imgRef);
        PdfImageXObject imgObject = new PdfImageXObject(imgStream);

        Image img = new Image(imgObject);
        img.scaleToFit(14400, 14400);
        img.setFixedPosition(0, 0);
        tempDoc.addNewPage(1, new PageSize(img.getImageScaledWidth(), img.getImageScaledHeight()));
        new Document(tempDoc).add(img);
        tempDoc.close();


        // We create a new file that only contains the new first page
        tempDoc = new PdfDocument(new PdfReader(tmp.getAbsolutePath()));

        PdfDocument resultDoc = new PdfDocument(new PdfWriter(DEST));
        resultDoc.initializeOutlines();
        tempDoc.copyPagesTo(1, 1, resultDoc);

        resultDoc.close();
        tempDoc.close();
    }
}
