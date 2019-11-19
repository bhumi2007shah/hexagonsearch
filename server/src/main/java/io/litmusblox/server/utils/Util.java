/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.utils;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.constant.IErrorMessages;
import io.litmusblox.server.error.ValidationException;
import io.litmusblox.server.model.Candidate;
import io.litmusblox.server.model.User;
import io.litmusblox.server.repository.CandidateRepository;
import io.litmusblox.server.service.MasterDataBean;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.WordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.*;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for holding Utility methods to be used across the application
 *
 * @author : Shital Raval
 * Date : 12/7/19
 * Time : 10:00 AM
 * Class Name : Util
 * Project Name : server
 */
@Configuration
@Log4j2
public class Util {

    @Autowired
    CandidateRepository candidateRepository;

    private static Pattern INDIAN_MOBILE_PATTERN = Pattern.compile(IConstant.INDIAN_MOBILE_PATTERN);

    private static Pattern JUNK_MOBILE_PATTERN = Pattern.compile(IConstant.JUNK_MOBILE_PATTERN);

    /**
     * Utility method to convert only relevant information into json
     *
     * @param responseBean the response bean to be converted to json
     * @param serializeMap map with key = filterclassname and value as a list of all bean properties required to be serialized
     * @param serializeExceptMap map with key = filterclassname and value as a list of bean properties that shouldn't be serialized
     * @return
     */
    public static String stripExtraInfoFromResponseBean(Object responseBean, Map<String, List<String>> serializeMap, Map<String, List<String>> serializeExceptMap) {

        ObjectMapper mapper = new ObjectMapper();

        String json="";
        try {

            SimpleFilterProvider filter = new SimpleFilterProvider();
            if (null != serializeMap)
                serializeMap.forEach((key, value) ->
                        filter.addFilter(key, SimpleBeanPropertyFilter.filterOutAllExcept(new HashSet<String>(value)))
                );

            if(null != serializeExceptMap)
                serializeExceptMap.forEach((key, value) ->
                        filter.addFilter(key, SimpleBeanPropertyFilter.serializeAllExcept(new HashSet<String>(value)))
                );

            json = mapper.writer(filter).writeValueAsString(responseBean);

        } catch (JsonGenerationException e) {
            log.error("error generating JSON string from response object: " + e.getMessage());
            e.printStackTrace();
        } catch (JsonMappingException e) {
            log.error("error generating JSON string from response object: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            log.error("error generating JSON string from response object: " + e.getMessage());
            e.printStackTrace();
        }

        return json;
    }

    public static boolean isNull(String s) {
        return (null == s || s.trim().length() == 0) ? true : false;
    }

    public static boolean isNotNull(String s) {
        return (null == s || s.trim().length() == 0) ? false : true;
    }

    public static boolean validateName(String name) throws ValidationException {
        if(isNull(name) || name.trim().length() == 0)
            throw new ValidationException(IErrorMessages.NAME_NULL_OR_BLANK + " - " + name, HttpStatus.BAD_REQUEST);
        if(name.length() > IConstant.CANDIDATE_NAME_MAX_LENGTH)
            throw new  ValidationException(IErrorMessages.NAME_FIELD_TOO_LONG + " - " + name, HttpStatus.BAD_REQUEST);
        if(!name.matches(IConstant.REGEX_FOR_NAME_VALIDATION)) {
            return false;
        }
        //name is valid
        return true;
    }

    public static boolean validateEmail(String email) throws ValidationException {
        if(Util.isNull(email) || email.trim().length() == 0)
            throw new ValidationException(IErrorMessages.EMAIL_NULL_OR_BLANK + " - " + email, HttpStatus.BAD_REQUEST);
        if(email.length() > IConstant.CANDIDATE_EMAIL_MAX_LENGTH)
            throw new  ValidationException(IErrorMessages.EMAIL_TOO_LONG + " - " + email, HttpStatus.BAD_REQUEST);

        //check domain name has at least one dot
        String domainName = email.substring(email.indexOf('@')+1);
        if(domainName.indexOf('.') == -1)
            throw new ValidationException(IErrorMessages.INVALID_EMAIL + " - " + email, HttpStatus.BAD_REQUEST);

        if(!email.matches(IConstant.REGEX_FOR_EMAIL_VALIDATION)) {
            return false;
        }
        //email address is valid
        return true;
    }

    public static boolean validateMobile(String mobile, String countryCode) throws ValidationException  {
        Map<String, Long> countryMap = getCountryMap();

        if(Util.isNull(mobile) || mobile.trim().length() == 0)
            throw new ValidationException(IErrorMessages.MOBILE_NULL_OR_BLANK + " - " + mobile, HttpStatus.BAD_REQUEST);

        if(!mobile.matches(IConstant.REGEX_FOR_MOBILE_VALIDATION))
            return false; //the caller should check for status, if it is false, due to regex failure, call again after cleaning up the mobile number

        if(countryCode.equals(IConstant.CountryCode.INDIA_CODE.getValue())) {
            Matcher m = INDIAN_MOBILE_PATTERN.matcher(mobile);
            if(!(m.find() && m.group().equals(mobile))) //did not pass the Indian mobile number pattern
                throw new ValidationException(IErrorMessages.INVALID_INDIAN_MOBILE_NUMBER + " - " + mobile, HttpStatus.BAD_REQUEST);
        }

        if(!countryCode.equals(IConstant.CountryCode.INDIA_CODE.getValue())){
            if(countryCode.equals(IConstant.CountryCode.AUS_CODE.getValue()) && mobile.length() != countryMap.get(IConstant.CountryCode.AUS_CODE.getValue()))
                throw new ValidationException(IErrorMessages.INVALID_AUSTRALIA_MOBILE_NUMBER + " - " + mobile, HttpStatus.BAD_REQUEST);

            if(countryCode.equals(IConstant.CountryCode.CAN_CODE.getValue()) && mobile.length() != countryMap.get(IConstant.CountryCode.CAN_CODE.getValue()))
                throw new ValidationException(IErrorMessages.INVALID_CANADA_MOBILE_NUMBER + " - " + mobile, HttpStatus.BAD_REQUEST);

            if(countryCode.equals(IConstant.CountryCode.UK_CODE.getValue()) && mobile.length() != countryMap.get(IConstant.CountryCode.UK_CODE.getValue()))
                throw new ValidationException(IErrorMessages.INVALID_UK_MOBILE_NUMBER + " - " + mobile, HttpStatus.BAD_REQUEST);

            if(countryCode.equals(IConstant.CountryCode.US_CODE.getValue()) && mobile.length() != countryMap.get(IConstant.CountryCode.US_CODE.getValue()))
                throw new ValidationException(IErrorMessages.INVALID_US_MOBILE_NUMBER + " - " + mobile, HttpStatus.BAD_REQUEST);

            if(countryCode.equals(IConstant.CountryCode.SING_CODE.getValue()) && mobile.length() != countryMap.get(IConstant.CountryCode.SING_CODE.getValue()))
                throw new ValidationException(IErrorMessages.INVALID_SINGAPORE_MOBILE_NUMBER + " - " + mobile, HttpStatus.BAD_REQUEST);

        }

        //check if the number is junk, like all the same digits
        if(JUNK_MOBILE_PATTERN.matcher(mobile).matches())
            throw new ValidationException(IErrorMessages.JUNK_MOBILE_NUMBER + " - " + mobile, HttpStatus.BAD_REQUEST);
        //mobile is valid
        return true;
    }

    public static String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }

    public static boolean validateUploadFileType(String fileName) {
        String extension = getFileExtension(fileName);
        if(!Arrays.asList(IConstant.supportedExtensions).contains(extension)) {
            throw new ValidationException(IErrorMessages.UNSUPPORTED_FILE_TYPE + " - " + extension, HttpStatus.BAD_REQUEST);
        }
        return true;
    }

    public static String formatDate(Date date, String dateFormat) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        return simpleDateFormat.format(date);
    }

    public static void modifyTextFileInZip(String zipPath) throws IOException {
        Path zipFilePath = Paths.get(zipPath);
        try (FileSystem fs = FileSystems.newFileSystem(zipFilePath, null)) {
            Path source = fs.getPath("[Content_Types].xml");
            Path temp = fs.getPath("/[Content_Types]_new.xml");
            if (Files.exists(temp)) {
                throw new IOException("Invalid excel format");
            }
            Files.move(source, temp);
            streamCopy(temp, source);
            Files.delete(temp);
        }
        System.out.println("returning from zip modification method");
    }

    public static void streamCopy(Path src, Path dst) throws IOException {
        System.out.println("In the method to copy the missing information in xlsx file");
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(Files.newInputStream(src)));
             BufferedWriter bw = new BufferedWriter(
                     new OutputStreamWriter(Files.newOutputStream(dst)))) {

            String line;
            while ((line = br.readLine()) != null) {
                line = line.replace("<Override PartName=\"/_rels/.rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>", "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>\n" +
                        "<Default Extension=\"xml\" ContentType=\"application/xml\"/><Override PartName=\"/_rels/.rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>");
                bw.write(line);
                bw.newLine();
            }
        }
        System.out.println("Modified the file.....");
    }

    public static void handleCandidateName(Candidate candidate, String candidateName) {
        if (candidateName.indexOf('.') != -1) { //a dot (.) is found in the name, process accordingly
            String[] name = candidateName.split(IConstant.REGEX_FOR_DOT_IN_NAME);
            if (name.length > 1) {
                candidate.setLastName(toSentenceCase(name[0]));
                candidate.setFirstName(toSentenceCase(name[1]));
                return;
            }
            else if (name.length > 0) {
                candidate.setFirstName(toSentenceCase(name[0]));
                return;
            }
        }

        String[] name = candidateName.split("\\s+");
        candidate.setFirstName(toSentenceCase(name[0]));
        StringBuffer lastName = null;
        if (name.length > 1) {
            lastName = new StringBuffer();
            for (int i = 1; i < name.length; i++) {
                if (i > 1)
                    lastName.append(" ");
                lastName.append(toSentenceCase(name[i]));
            }
        }
        if (null != lastName)
            candidate.setLastName(toSentenceCase(lastName.toString()));

    }

    public static String indianMobileConvertor(String mobileNo) {
        //remove all occurences of '
        mobileNo = mobileNo.replaceAll("\'","");

        if(!Util.isNull(mobileNo)) {
            //check if number contains any prefix like 0 or +
            //strip all occurences of 0 and +
            while (mobileNo.charAt(0) == '0' || mobileNo.charAt(0) == '+') {
                mobileNo = mobileNo.substring(1);
            }
        }

        //strip all white spaces
        mobileNo = mobileNo.replaceAll("\\s+", "");

        //if mobile number is greater than 10 digits, and prefix is 91, remove 91
        if(mobileNo.length() > 10 && mobileNo.startsWith("91"))
            mobileNo = mobileNo.substring(2);

        //if mobile number is greater than 10 digits and start with anything else than 91 and replace any non digit character
        if(mobileNo.length() > 10 ){
            mobileNo = mobileNo.replaceAll("\\D", "");
            mobileNo = mobileNo.substring(mobileNo.length()-10);
        }

        return mobileNo;
    }

    public static void sendSentryErrorMail(String fileName, Map<String, String> headers, String processFileType){
        StringBuffer info = new StringBuffer(fileName).append(" - ").append(IErrorMessages.MISSING_COLUMN_NAMES_FIRST_ROW);
        log.info(info.toString());
        Map<String, String> breadCrumb = new HashMap<>();
        breadCrumb.put(IConstant.UPLOAD_FILE_TYPE,processFileType);
        SentryUtil.logWithStaticAPI(null, info.toString(), breadCrumb);
    }

    public static Date convertStringToDate(String stringDate) {
        if(!stringDate.isEmpty()){
            try {
                return new SimpleDateFormat("dd/MM/yyyy").parse(stringDate);
            } catch (ParseException e) {
                log.error("Given string not convert in Date...");
            }
        }
        //log.info("Given date string is empty");
        return null;
    }

    public static String getYearFromStringDate(String dateString){
        return dateString.split("/")[2];
    }

    public static String truncateField(Candidate candidate, String fieldName, int fieldLength, String fieldValue) {
        log.info("Inside truncateField method");
        //Candidate candidate = candidateRepository.findById(Long.parseLong(candidateId)).orElse(null);
        User loggedInUser = null;
        if(null != SecurityContextHolder.getContext().getAuthentication() && !SecurityContextHolder.getContext().getAuthentication().getPrincipal().equals("anonymousUser"))
            loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        StringBuffer info = new StringBuffer(fieldName).append(" is longer than the permitted length of ").append(fieldLength).append(" ").append(fieldValue);
        log.info(info.toString());
        Map<String, String> breadCrumb = new HashMap<>();
        if(null!=loggedInUser){
            breadCrumb.put("User Id",loggedInUser.getId().toString());
            breadCrumb.put("User Email", loggedInUser.getEmail());
        }
        if(null != candidate.getId())
            breadCrumb.put("Candidate Id",candidate.getId().toString());
        breadCrumb.put("Candidate Email",candidate.getEmail());
        breadCrumb.put("Candidate Mobile",candidate.getMobile());
        breadCrumb.put(fieldName, fieldValue);
        SentryUtil.logWithStaticAPI(null, info.toString(), breadCrumb);
        return fieldValue.substring(0, fieldLength);
    }

    /**
     * @param value to be capitalized
     * @return capitalized string i.e: if input is abc it will return Abc
     */
    public static String toSentenceCase(String value){
        return WordUtils.capitalize(value.toLowerCase());
    }

    public static Date getFormattedEndDate(String toDate, String dateFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        try {
            Date date = null;
            if (null != toDate) {
                date = sdf.parse(toDate);
            } else {
                // set todays date
                date = Calendar.getInstance().getTime();
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);
            return calendar.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            // ignore
        }
        return null;
    }

    public static Map<String, Long>getCountryMap(){
        Map<String, Long> countryMap =new HashMap<>();
        MasterDataBean.getInstance().getCountryList().forEach(country -> {
            countryMap.put(country.getCountryCode(), country.getMaxMobileLength());
        });
        return countryMap;
    }

    public static String validateCandidateName(String name){
        log.info("Inside validate name");
        String cleanFirstName = name;
        if (!validateName(name.trim())) {
            cleanFirstName = name.replaceAll(IConstant.REGEX_TO_CLEAR_SPECIAL_CHARACTERS_FOR_NAME, "");
            log.error("Special characters found, cleaning First name \"" + name + "\" to " + cleanFirstName);
            if (!validateName(cleanFirstName))
                throw new ValidationException(IErrorMessages.NAME_FIELD_SPECIAL_CHARACTERS + " - " + name, HttpStatus.BAD_REQUEST);
            cleanFirstName = Util.toSentenceCase(cleanFirstName);
        }
        return cleanFirstName;
    }

    public static Date getCurrentOrBefore1YearDate(Boolean dateBefore1Year) throws ParseException {
        LocalDateTime ldt = null;
       if(dateBefore1Year)
            ldt = LocalDateTime.now().minusYears(1);
       else
         ldt = LocalDateTime.now();

        DateTimeFormatter formmat = DateTimeFormatter.ofPattern(IConstant.DATE_FORMAT, Locale.ENGLISH);
        String formatter = formmat.format(ldt);
        return new SimpleDateFormat(IConstant.DATE_FORMAT).parse(formatter);
    }


}
