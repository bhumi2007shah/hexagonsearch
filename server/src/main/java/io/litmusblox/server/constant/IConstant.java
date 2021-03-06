/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.constant;

import java.util.Arrays;
import java.util.List;

/**
 * @author : Sumit
 * Date : 4/7/19
 * Time : 11:35 PM
 * Class Name : IConstant
 * Project Name : server
 */
public interface IConstant {

    // Regex
    String REGEX_FOR_JOB_TITLE = "^[\\&\\/\\(\\)\\[\\]\\+\\#\\-\\.\\,a-zA-Z0-9\\s\\t]+$";
    String REGEX_FOR_COMPANY_JOB_ID = "^[\\-\\/\\.\\,\\a-zA-Z0-9\\s]*$";
    String REGEX_FOR_COMPANY_NAME = "^[\\&\\'\\-\\.a-zA-Z0-9\\s]+$";
    String REGEX_FOR_USER_DESIGNATION = "^[\\&\\-\\,a-zA-Z\\s]+$";

    enum CountryCode {
        INDIA_CODE("+91"), AUS_CODE("+61"), CAN_CODE("+1"), UK_CODE("+44"), SING_CODE("+65"), US_CODE("+1");

        private String countryCode;

        CountryCode(String val) {
            this.countryCode = val;
        }

        public String getValue() {
            return this.countryCode;
        }
    }

    String INDIAN_MOBILE_PATTERN = "(0/91)?[6-9][0-9]{9}";
    String JUNK_MOBILE_PATTERN = "([0-9])\\1{8,}";
    String REGEX_FOR_EMAIL_VALIDATION = "^[a-z0-9A-Z]+[\\w.-]+@[a-zA-Z]+[a-zA-Z0-9.-]+[a-zA-Z]$";
    String REGEX_FOR_MOBILE_VALIDATION = "[\\d]+";
    String REGEX_TO_CLEAR_SPECIAL_CHARACTERS_FOR_EMAIL = "[^\\d\\w@.-]";
    String REGEX_TO_CLEAR_SPECIAL_CHARACTERS_FOR_MOBILE = "[^\\d]";
    String REGEX_FOR_NAME_VALIDATION = "[a-zA-Z\\-][a-zA-Z.\\-\\s]*";
    String REGEX_TO_CLEAR_SPECIAL_CHARACTERS_FOR_NAME = "[^\\w\\s\\-]*";
    String REGEX_FOR_DOT_IN_NAME = "(?<=[a.zA-Z\\.]\\s)+";
    String REGEX_TO_FIND_ONLINE_PROFILE_UNIQUE_ID = "(?<=\\/)(?:[\\w\\-]+)(?:\\/)?$";
    String REGEX_TO_VALIDATE_COMPANY_SHORT_NAME = "^[a-zA-Z]+[a-zA-Z0-9]+$";


    // lengths
    Integer TITLE_MAX_LENGTH = 100;
    int JOB_ID_MAX_LENGTH = 10;
    int MAX_INSTITUTE_LENGTH = 75;
    Integer CANDIDATE_NAME_MAX_LENGTH = 45;
    Integer CANDIDATE_EMAIL_MAX_LENGTH = 50;
    Integer CV_TEXT_API_RESPONSE_MIN_LENGTH = 50;
    Integer COMPANY_SHORT_NAME = 25;

    String REPO_LOCATION = "repoLocation";
    String TEMP_REPO_LOCATION = "temprepolocation";
    String ERROR_FILES_REPO_LOCATION = "error_files";
    String CV_STORAGE_LOCATION = "cvStorageUrl";
    String DATE_FORMAT_yyyymmdd_hhmm = "yyyyMMdd_HHmm";
    String STR_INDIA = "India";

    String TOKEN_HEADER = "Authorization";
    String TOKEN_PREFIX = "Bearer ";
    String CANDIDATE_CV = "CandidateCv";
    String SENTRY_DSN = "sentryDSN";
    String UPLOAD_FILE_TYPE = "Upload file type";
    String LOCALHOST_LOOPBACK = "0:0:0:0:0:0:0:1";
    String YEAR_OF_PASSING = "yearOfPassing";
    String DAYS = "Days";


    enum UserStatus {
        New, Active, Blocked, Inactive;
    }

    enum UserRole {
        RECRUITER(Names.RECRUITER),
        SUPER_ADMIN(Names.SUPER_ADMIN),
        CLIENT_ADMIN(Names.CLIENT_ADMIN),
        RECRUITMENT_AGENCY(Names.RECRUITMENT_AGENCY),
        BUSINESS_USER(Names.BUSINESS_USER);

        public class Names {
            public static final String RECRUITER = "Recruiter";
            public static final String SUPER_ADMIN = "SuperAdmin";
            public static final String CLIENT_ADMIN = "ClientAdmin";
            public static final String RECRUITMENT_AGENCY = "RecruitmentAgency";
            public static final String BUSINESS_USER = "BusinessUser";
        }

        private final String label;

        UserRole(String label) {
            this.label = label;
        }

        public String toString() {
            return this.label;
        }
    }

    enum AddJobPages {
        overview, screeningQuestions, keySkills, capabilities, jobDetail, hiringTeam, expertise, preview;
    }

    enum JobStatus {
        DRAFT("Draft"), PUBLISHED("Live"), ARCHIVED("Archived");
        private String value;

        JobStatus(String val) {
            this.value = val;
        }

        public String getValue() {
            return this.value;
        }
    }

    enum UPLOAD_STATUS {
        Success, Failure, Partial_Success
    }

    enum Stage {
        Source ("Sourcing"), Screen ("Screening"), ResumeSubmit ("Submitted"), Interview ("Interview"), MakeOffer("Make Offer"), Offer ("Offer"), Join ("Hired"), Reject ("Rejected");
        private String value;

        Stage(String val) { this.value = val; }

        public String getValue() { return this.value; }
    }

    enum UPLOAD_FORMATS_SUPPORTED {
        LitmusBlox, Naukri;
    }

    enum CandidateSource {
        SingleCandidateUpload("Individual"), File("File"), Naukri("Naukri"), LinkedIn("LinkedIn"), IIMJobs("IIMJobs"), DragDropCv("DragDropCv"), NaukriMassMail("NaukriMassMail"), NaukriJobPosting("NaukriJobPosting"), EmployeeReferral("EmployeeReferral"), CareerPage("CareerPage"), JobPosting("JobPosting");
        private String value;

        CandidateSource(String val) {
            this.value = val;
        }

        public String getValue() {
            return this.value;
        }
    }

    List AUTOSOURCED_TYPE = Arrays.asList(CandidateSource.NaukriMassMail.getValue(), CandidateSource.NaukriJobPosting.getValue(),CandidateSource.CareerPage.getValue(), CandidateSource.EmployeeReferral.getValue());

    enum LITMUSBLOX_FILE_COLUMNS {
        FirstName("First Name"), LastName("Last Name"), Email("Email"), Mobile("Mobile");
        private String value;

        LITMUSBLOX_FILE_COLUMNS(String val) {
            this.value = val;
        }

        public String getValue() {
            return this.value;
        }
    }

    enum NAUKRI_FILE_COLUMNS {
        SerialNumber("Serial Number"), CandidateName("Name of the Candidate"), ResumeId("Resume ID"), PostalAddress("Postal Address"), Telephone("Telephone No."), Mobile("Mobile No."), DOB("Date of Birth"), Email("Email"), WorkExperience("Work Experience"), ResumeTitle("Resume Title"), CurrentLocation("Current Location"), PreferredLocation("Preferred Location"), CurrentEmployer("Current Employer"), CurrentDesignation("Current Designation"), AnnualSalary("Annual Salary"), UGCourse("U.G. Course"), PGCourse("P. G. Course"), PPGCourse("P.P.G. Course"), LastActive("Last Active Date");
        private String value;

        NAUKRI_FILE_COLUMNS(String val) {
            this.value = val;
        }

        public String getValue() {
            return this.value;
        }
    }

    enum NAUKRI_XLS_FILE_COLUMNS {
        SerialNumber("Serial Number"), CandidateName("Name of the Candidate"), ResumeId("Resume ID"), PostalAddress("Postal Address"), Telephone("Telephone No."), Mobile("Mobile No."), DOB("Date of Birth"), Email("Email"), WorkExperience("Work Experience"), ResumeTitle("Resume Title"), CurrentLocation("Current Location"), PreferredLocation("Preferred Location"), CurrentEmployer("Current Employer"), CurrentDesignation("Current Designation"), AnnualSalary("Annual Salary"), UGCourse("U.G. Course"), PGCourse("P. G. Course"), PPGCourse("Post P. G. Course"), LastActive("Last Active Date");
        private String value;

        NAUKRI_XLS_FILE_COLUMNS(String val) {
            this.value = val;
        }

        public String getValue() {
            return this.value;
        }
    }

    String[] supportedExtensions = new String[]{"xls", "xlsx", "xml", "csv"};
    String[] cvUploadSupportedExtensions = new String[]{"doc", "docx", "html", "pdf", "rar", "rtf", "zip"};


    enum MAX_FIELD_LENGTHS {
        INSTITUTE_NAME(75), COMPANY_NAME(75), DESIGNATION(100), ADDRESS(255), KEY_SKILLS(255), ONLINE_PROFILE_URL(255), ONLINE_PROFILE_TYPE(20), WORK_SUMMARY(255), GENDER(1), DEGREE(100), SKILL(50), ROLE(40), YEAR_OF_PASSING(4), REASON_FOR_CHANGE(100), SPECIALIZATION(50), INTERVIEW_COMMENTS(250);

        private int value;

        MAX_FIELD_LENGTHS(int val) {
            this.value = val;
        }

        public int getValue() {
            return this.value;
        }
    }

    enum CompanySubscription {
        Lite, Max, LDEB;
    }

    enum UPLOAD_TYPE {
        Candidates, Logo, CandidateCv;
    }

    enum PROCESS_FILE_TYPE {
        CsvFile, ExcelFile, HTMLFile, NaukriExcelFile
    }

    enum FILE_TYPE {
        rar, zip, other
    }

    enum InterviewType {
        SINGLE_INTERVIEW("Single Interview"), RECRUITMENT_DRIVE("Recruitment Drive");

        private String interviewType;

        InterviewType(String val) { this.interviewType = val; }

        public String getValue() { return this.interviewType; }
    }

    enum InterviewMode {
        TELEPHONIC("Telephonic"), IN_PERSION("In Person"), VIDEO_CONFERENCE("Video Conference");

        private String interviewMode;

        InterviewMode(String val) { this.interviewMode = val; }

        public String getValue() { return this.interviewMode; }
    }

    String FILE_STORAGE_URL = "fileStorageUrl";
    String ERROR_FILES = "error_files";
    String NOT_AVAILABLE = "Not Available";

    enum COMPANY_PAGES {
        Company, BusinessUnit, UsersAndTeams, Addresses, ScreeningQuestions
    }

    enum MlRolePredictionStatus {
        NO_ERROR("no_Error"), JDC_JTM_ERROR("jdc_jtm_Error"), SUFF_ERROR("suff_Error"), JDC_JTN_ERROR("jdc_jtn_Error"), JDB_JTM_ERROR("jdb_jtm_Error");
        private String value;

        MlRolePredictionStatus(String val) {
            this.value = val;
        }

        public String getValue() {
            return this.value;
        }
    }

    enum ChatbotStatus {
        COMPLETE("Complete"), INCOMPLETE("Incomplete"), NOT_INSTERESTED("Not Interested"), INVITED("Invited");
        private String value;

        ChatbotStatus(String val){ this.value = val;}

        public String getValue(){
            return this.value;
        }
    }

    //constants for create candidate if firstName, lastName.
    String NOT_FIRST_NAME = "Not";
    String NOT_LAST_NAME = "Available";

    String DATE_FORMAT = "yyyy-MM-dd";

    enum UserType {
        RECRUITING("Recruiting"), BUSINESS("Business");
        private String value;

        UserType(String val) {
            this.value = val;
        }

        public String getValue() {
            return this.value;
        }
    }

    enum CompanyType {
        INDIVIDUAL("Individual"), AGENCY("Agency");
        private String value;

        CompanyType(String val) {
            this.value = val;
        }

        public String getValue() {
            return this.value;
        }
    }

    String HR_RECRUITER = "HR Recruiter";
    String HR_HEAD = "HR Head";
    String HIRING_MANAGER = "Hiring Manager";
    String INTERVIEWER = "Interviewer";
    String ADMIN = "Admin";
    String DEFAULT_JOB_TYPE = "Full Time";

    String NOT_AVAILABLE_EMAIL = "@notavailable.io";
    String SYSTEM_USER_EMAIL = "systemuser@hex.com";

    String[] fetchItemsType = new String[]{"referrerRelation", "jobType", "interviewConfirmation", "countries", "education","otpExpiryMinutes"};

    String REPLACEMENT_KEY_FOR_SHORTNAME = "_shortName_";
    String GODADDY_SUCCESS_RESPONSE = "\"code\":";

    String NAUKRI_SUBJECT_STRING = "Naukri.com -";
    String REF_ID_MATCH_REGEX = "[a-fA-F0-9]{8}\\-[a-fA-F0-9]{4}\\-[a-fA-F0-9]{4}\\-[a-fA-F0-9]{4}\\-[a-fA-F0-9]{12}";

    String PARSING_RESPONSE_JSON = "PARSING_RESPONSE_JSON";
    String PARSING_RESPONSE_PYTHON = "PARSING_RESPONSE_PYTHON";
    String PARSING_RESPONSE_ML = "PARSING_RESPONSE_ML";

    int SCHEDULER_THREAD_POOL_SIZE = 20;
    int ASYNC_CORE_THREAD_POOL_SIZE = 5;
    int ASYNC_MAX_THREAD_POOL_SIZE = 10;

    String COMPANY_NAME_VARIABLE = "$companyName";

    Integer SCREENING_QUESTION_RESPONSE_MAX_LENGTH = 300;
    Integer SCREENING_QUESTION_COMMENT_MAX_LENGTH = 100;

    String CHAT_LINK="chatbotLink";
    String CHAT_LINK_HEADER="Chatbot Link";

    String [] apacheReloadCommand = {"sudo", "apache2ctl", "graceful"};
    int OTP_EXPIRY_SECONDS = 90;

    enum ASYNC_OPERATIONS {
        FileUpload, InviteCandidates
    }

}
