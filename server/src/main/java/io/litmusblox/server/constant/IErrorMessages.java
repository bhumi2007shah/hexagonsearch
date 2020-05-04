/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.constant;

/**
 * @author : Sumit
 * Date : 5/7/19
 * Time : 11:55 AM
 * Class Name : IErrorMessages
 * Project Name : server
 */
public interface IErrorMessages {

    String NULL_MESSAGE="should not be null";
    String BLANK_MESSAGE="should not be blank";
    String ALPHANUMERIC_MESSAGE="should be alphanumeric";
    String SPECIAL_CHARACTER_MESSAGE="should not contain special character";
    String EMPTY_AND_NULL_MESSAGE="should not be null or empty ";
    String SCREENING_QUESTIONS_VALIDATION_MESSAGE="Screening questions should not be more than 10 ";

    String MOBILE_NULL_OR_BLANK = "Mobile number cannot be null or blank";
    String MOBILE_INVALID_DATA = "Mobile number can contain only digits 0-9 in any combination";
    String INVALID_INDIAN_MOBILE_NUMBER = "Mobile number is not a valid Indian Mobile number ";
    String NAME_NULL_OR_BLANK = "Name cannot be null or blank";
    String NAME_FIELD_TOO_LONG = "Name cannot exceed 45 characters";
    String NAME_FIELD_SPECIAL_CHARACTERS = "Name should be only letters (A-Z)";
    String EMAIL_TOO_LONG = "Email address cannot exceed 50 characters";
    String EMAIL_NULL_OR_BLANK = "Email address cannot be null or blank";
    String INVALID_EMAIL = "Invalid Email address";
    String JUNK_MOBILE_NUMBER = "Mobile number is junk as it contains the same digits.";
    String INTERNAL_SERVER_ERROR = "Something went wrong into server";
    String MAX_CANDIDATE_PER_FILE_EXCEEDED = "Maximum candidates per file limit exceeded";
    String MAX_CANDIDATES_PER_USER_PER_DAY_EXCEEDED = "Daily limit for maximum candidates per user exceeded";
    String MAX_FILES_PER_UPLOAD = "Exceeded limit of maximum files that can be uploaded in a single request";
    String UNSUPPORTED_FILE_SOURCE = "Unsupported file source : ";
    String UNSUPPORTED_FILE_TYPE = "Unsupported file type with extension";
    String INVALID_SETTINGS = "Invalid System Settings";
    String MISSING_COLUMN_NAMES_FIRST_ROW = "First row in the file does not have correct column names";
    String DUPLICATE_CANDIDATE = "Candidate already exists ";
    String INVALID_REQUEST_FROM_PLUGIN = "Invalid request object from plugin, missing Candidate info";
    String DUPLICATE_USER_EMAIL = "User with email already exists";

    String CANDIDATE_ID_MISMATCH_FROM_HISTORY = "Found different Candidate ids for mobile and email : ";
    String JOB_NOT_LIVE = "Selected job is in \'Draft\' stage";
    String CLIENT_ADMIN_EXISTS_FOR_COMPANY = "Client admin for company already exists: ";
    String USER_COUNTRY_NULL = "Country for new  user not selected";
    String USER_NOT_FOUND = "User does not exist ";
    String INVALID_CREDENTIALS="Invalid credentials";
    String FORGOT_PASSWORD_USER_BLOCKED = "User is blocked";
    String DISABLED_USER = "User is blocked / disabled";
    String FORGOT_PASSWORD_DUPLICATE_REQUEST = "Forgot password request has already been received ";
    String USER_EMAIL_TOKEN_MISMATCH = "Invalid email address to set password";
    String PASSWORD_MISMATCH = "Given password and confirm password are either null or do not match";
    String COMPANY_NAME_NOT_VALID= "Company name not valid";
    String NO_EMAIL_PROVIDED = "No email address provided";
    String USER_NOT_ACTIVE = "User was not activated ";
    String INVALID_RECEIVER_NAME = "Receiver's name should be between 1 and 45 characters";
    String JOB_NOT_ARCHIVED = "Selected job is not archived";
    String FAILED_TO_SAVE_FILE = "Fail to save the file";
    String JOB_COMPANY_MISMATCH = "Requested job does not belong to the company of the logged in user";
    String ML_DATA_UNAVAILABLE = "No matching skills and capabilities found for the job title and job description";
    String JCM_NOT_FOUND = "No Job Candidate Mapping found for id: ";
    String UUID_NOT_FOUND = "No mapping found for uuid ";
    String ML_DATA_DUPLICATE_SKILLS = "Received ML response with duplicate skills\n";

    String CANDIDATE_ID_MISMATCH_FROM_HISTORY_FOR_MOBILE = "Found different Candidate ids for mobile : ";
    String CANDIDATE_ID_MISMATCH_FROM_HISTORY_FOR_EMAIL = "Found different Candidate ids for email : ";

    String INVALID_AUSTRALIA_MOBILE_NUMBER = "Mobile number is not a valid Australian Mobile number ";
    String INVALID_CANADA_MOBILE_NUMBER = "Mobile number is not a valid Canada Mobile number ";
    String INVALID_UK_MOBILE_NUMBER = "Mobile number is not a valid Uk Mobile number ";
    String INVALID_US_MOBILE_NUMBER = "Mobile number is not a valid Us Mobile number ";
    String INVALID_SINGAPORE_MOBILE_NUMBER = "Mobile number is not a valid Singapore Mobile number ";

    String USER_DESIGNATION_NOT_VALID= "User designation not valid";
    String OTP_VERIFICATION_FAILED = "OTP verification failed";
    String INVALID_COMPANY_SHORT_NAME = "Company short name contain only alpha numeric value start with character";
    String INVALID_DATE_OF_BIRTH_FORMAT = "The Date of Birth format is invalid or not handled";
}