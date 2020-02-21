/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.impl;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import io.litmusblox.server.service.IDetectTextInImage;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gcp.vision.CloudVisionTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : sameer
 * Date : 25/01/20
 * Time : 2:58 PM
 * Class Name : DetectText
 * Project Name : server
 */
@Log4j2
@Service
public class DetectTextInImage implements IDetectTextInImage {

    @Autowired
    CloudVisionTemplate cloudVisionTemplate;

    /**
     * Function to convert image to text.
     * @param imageUrl - this url will be sent to google api
     * @return String - extracted text from image
     * @throws Exception
     */
    @Override
    public String detectText(URL imageUrl) throws Exception {
        List<AnnotateImageRequest> requests = new ArrayList<>();
        String text = null;
        InputStream is = imageUrl.openStream();
        ByteString imgByteString = ByteString.readFrom(new BufferedInputStream(is));
        Image img = Image.newBuilder().setContent(imgByteString).build();
        Feature feat = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
        AnnotateImageRequest request =
                AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
        requests.add(request);

        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
                List<AnnotateImageResponse> responses = response.getResponsesList();

            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    log.error("Error: {}", res.getError().getMessage());
                    return null;
                }

                // For full list of available annotations, see http://g.co/cloud/vision/docs
                for (EntityAnnotation annotation : res.getTextAnnotationsList()) {
                    if(text == null){
                        text = annotation.getDescription().trim();
                    }
                }
            }
        }
        return text;
    }
}
