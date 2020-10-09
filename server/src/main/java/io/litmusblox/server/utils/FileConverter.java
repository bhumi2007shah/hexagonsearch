/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.utils;

import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Path;

/**
 * @author : sameer
 * Date : 05/10/20
 * Time : 11:09 AM
 * Class Name : FileConverter
 * Project Name : server
 */

@Log4j2
public class FileConverter {
    public static void convertDocxToPdf(MultipartFile inputFile, Path filePath) {
        try {
            InputStream docFile = new BufferedInputStream(inputFile.getInputStream());
            XWPFDocument doc = new XWPFDocument(docFile);
            PdfOptions pdfOptions = PdfOptions.create();
            OutputStream out = new FileOutputStream(new File(filePath.toString()+".pdf"));
            PdfConverter.getInstance().convert(doc, out, pdfOptions);
            doc.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
