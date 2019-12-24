SET session_replication_role TO 'replica';


DROP TABLE IF EXISTS COUNTRY;
CREATE TABLE COUNTRY(
  ID serial PRIMARY KEY NOT NULL,
   COUNTRY_NAME VARCHAR (15) NOT NULL,
   COUNTRY_CODE VARCHAR (5) NOT NULL,
   MAX_MOBILE_LENGTH SMALLINT NOT NULL,
  COUNTRY_SHORT_CODE varchar(2) NOT NULL
);

DROP TABLE IF EXISTS USERS;
CREATE TABLE USERS(
  ID serial PRIMARY KEY NOT NULL,
   EMAIL VARCHAR (50) NOT NULL,
   PASSWORD VARCHAR (60),
   FIRST_NAME VARCHAR (45) NOT NULL,
   LAST_NAME VARCHAR (45) NOT NULL,
   MOBILE VARCHAR (15) NOT NULL,
   COMPANY_ID INTEGER NOT NULL,
   ROLE VARCHAR (17) NOT NULL,
   DESIGNATION VARCHAR (45),
   STATUS VARCHAR (10) DEFAULT 'NEW',
   COUNTRY_ID INTEGER NOT NULL REFERENCES COUNTRY(ID),
   USER_UUID uuid DEFAULT uuid_generate_v1(),
   INVITATION_MAIL_TIMESTAMP TIMESTAMP,
   RESET_PASSWORD_FLAG BOOL NOT NULL DEFAULT 'f',
   RESET_PASSWORD_EMAIL_TIMESTAMP TIMESTAMP,
   USER_TYPE varchar(15) default 'Recruiting',
   COMPANY_ADDRESS_ID INTEGER ,
   COMPANY_BU_ID INTEGER ,
   CREATED_ON TIMESTAMP NOT NULL,
   CREATED_BY INTEGER,
   UPDATED_ON TIMESTAMP,
   UPDATED_BY INTEGER
);

DROP TABLE IF EXISTS CONFIGURATION_SETTINGS;
CREATE TABLE CONFIGURATION_SETTINGS(
  ID serial PRIMARY KEY NOT NULL,
   CONFIG_NAME VARCHAR (50) NOT NULL UNIQUE,
   CONFIG_VALUE SMALLINT NOT NULL
);

DROP TABLE IF EXISTS MASTER_DATA;
CREATE TABLE MASTER_DATA(
  ID serial PRIMARY KEY NOT NULL,
   TYPE VARCHAR (50) NOT NULL,
   VALUE VARCHAR (100) NOT NULL,
   VALUE_TO_USE VARCHAR (20),
   COMMENTS VARCHAR (255),
   CONSTRAINT UNIQUE_MASTER_DATA UNIQUE (TYPE, VALUE)
);

DROP TABLE IF EXISTS SKILLS_MASTER;
CREATE TABLE SKILLS_MASTER(
  ID serial PRIMARY KEY NOT NULL,
   SKILL_NAME VARCHAR (100) NOT NULL
);

DROP TABLE IF EXISTS TEMP_SKILLS;
CREATE TABLE TEMP_SKILLS(
  ID serial PRIMARY KEY NOT NULL,
   SKILL_NAME VARCHAR (100) NOT NULL,
   REVIEWED BOOL DEFAULT 'f'
);

DROP TABLE IF EXISTS STAGE_MASTER;
CREATE TABLE STAGE_MASTER (
ID serial PRIMARY KEY NOT NULL,
STAGE_NAME varchar(15) NOT NULL,
CONSTRAINT UNIQUE_STAGE_NAME UNIQUE(STAGE_NAME)
);

DROP TABLE IF EXISTS STEPS_PER_STAGE;
CREATE TABLE STEPS_PER_STAGE(
ID serial PRIMARY KEY NOT NULL,
STAGE_ID integer REFERENCES STAGE_MASTER(ID) NOT NULL,
STEP_NAME varchar(15) NOT NULL,
CONSTRAINT UNIQUE_STAGE_STEP UNIQUE(STAGE_ID, STEP_NAME)
);

DROP TABLE IF EXISTS SCREENING_QUESTION;
CREATE TABLE SCREENING_QUESTION(
   ID serial PRIMARY KEY NOT NULL,
   QUESTION VARCHAR (150) NOT NULL,
   QUESTION_TYPE INTEGER REFERENCES MASTER_DATA(ID) NOT NULL,
   OPTIONS VARCHAR(100)[],
   MULTILEVELOPTIONS VARCHAR(500)
);

DROP TABLE IF EXISTS COMPANY;
CREATE TABLE COMPANY(
  ID serial PRIMARY KEY NOT NULL,
   COMPANY_NAME VARCHAR (255) NOT NULL,
   ACTIVE BOOL DEFAULT 't',
   COMPANY_DESCRIPTION TEXT,
   WEBSITE VARCHAR(245),
   LANDLINE VARCHAR(10),
   INDUSTRY INTEGER REFERENCES MASTER_DATA(ID),
   LINKEDIN VARCHAR(245),
   FACEBOOK VARCHAR(245),
   TWITTER VARCHAR(245),
   LOGO VARCHAR(245),
   SUBSCRIPTION VARCHAR(5) NOT NULL DEFAULT 'Lite',
   COMPANY_TYPE VARCHAR(15) DEFAULT 'Individual' NOT NULL,
   RECRUITMENT_AGENCY_ID INTEGER REFERENCES COMPANY(ID),
   CREATED_ON TIMESTAMP NOT NULL,
   CREATED_BY INTEGER REFERENCES USERS(ID) NOT NULL,
   UPDATED_ON TIMESTAMP,
   UPDATED_BY INTEGER REFERENCES USERS(ID)
);

ALTER TABLE USERS
ADD CONSTRAINT fk_company FOREIGN KEY (COMPANY_ID) REFERENCES COMPANY (ID);

DROP TABLE IF EXISTS COMPANY_ADDRESS;
CREATE TABLE COMPANY_ADDRESS(
  ID serial PRIMARY KEY NOT NULL,
   COMPANY_ID INTEGER REFERENCES COMPANY(ID) NOT NULL,
   ADDRESS VARCHAR (150) NOT NULL,
   ADDRESS_TYPE INTEGER REFERENCES MASTER_DATA(ID) NOT NULL,
   LATITUDE DOUBLE PRECISION,
   LONGITUDE DOUBLE PRECISION,
   ADDRESS_TITLE VARCHAR(100) NOT NULL UNIQUE DEFAULT 'Default Address',
   CITY VARCHAR(100),
   STATE VARCHAR(100),
   COUNTRY VARCHAR(50),
   CREATED_ON TIMESTAMP NOT NULL,
   CREATED_BY INTEGER REFERENCES USERS(ID) NOT NULL,
   UPDATED_ON TIMESTAMP,
   UPDATED_BY INTEGER REFERENCES USERS(ID)
);

ALTER TABLE USERS
ADD CONSTRAINT fk_company_address FOREIGN KEY (COMPANY_ADDRESS_ID) REFERENCES COMPANY_ADDRESS (ID);

DROP TABLE IF EXISTS COMPANY_BU;
CREATE TABLE COMPANY_BU(
  ID serial PRIMARY KEY NOT NULL,
   COMPANY_ID INTEGER REFERENCES COMPANY(ID) NOT NULL,
   BUSINESS_UNIT VARCHAR (100) NOT NULL,
   CREATED_ON TIMESTAMP NOT NULL,
   CREATED_BY INTEGER REFERENCES USERS(ID) NOT NULL,
   UPDATED_ON TIMESTAMP,
   UPDATED_BY INTEGER REFERENCES USERS(ID)
);

ALTER TABLE USERS
ADD CONSTRAINT fk_company_bu FOREIGN KEY (COMPANY_BU_ID) REFERENCES COMPANY_BU (ID);

DROP TABLE IF EXISTS COMPANY_STAGE_STEP;
CREATE TABLE COMPANY_STAGE_STEP(
  ID serial PRIMARY KEY NOT NULL,
   COMPANY_ID INTEGER REFERENCES COMPANY(ID) NOT NULL,
   STAGE INTEGER REFERENCES STAGE_MASTER(ID) NOT NULL,
   STEP VARCHAR (25) NOT NULL,
   CREATED_ON TIMESTAMP NOT NULL,
   CREATED_BY INTEGER REFERENCES USERS(ID) NOT NULL,
   UPDATED_ON TIMESTAMP,
   UPDATED_BY INTEGER REFERENCES USERS(ID),
   CONSTRAINT UNIQUE_COMPANY_STAGE_STEP UNIQUE (COMPANY_ID, STAGE,STEP)
);

DROP TABLE IF EXISTS COMPANY_SCREENING_QUESTION;
CREATE TABLE COMPANY_SCREENING_QUESTION(
  ID serial PRIMARY KEY NOT NULL,
   COMPANY_ID INTEGER REFERENCES COMPANY(ID) NOT NULL,
   QUESTION VARCHAR (150) NOT NULL,
   QUESTION_TYPE INTEGER REFERENCES MASTER_DATA(ID) NOT NULL,
   OPTIONS VARCHAR(100)[],
   CREATED_ON TIMESTAMP NOT NULL,
   CREATED_BY INTEGER REFERENCES USERS(ID) NOT NULL,
   UPDATED_ON TIMESTAMP,
   UPDATED_BY INTEGER REFERENCES USERS(ID)
);

DROP TABLE IF EXISTS USER_SCREENING_QUESTION;
CREATE TABLE USER_SCREENING_QUESTION(
  ID serial PRIMARY KEY NOT NULL,
  USER_ID INTEGER REFERENCES USERS(ID) NOT NULL,
  QUESTION VARCHAR (150) NOT NULL,
  QUESTION_TYPE INTEGER REFERENCES MASTER_DATA(ID) NOT NULL,
  OPTIONS VARCHAR(100)[],
  CREATED_ON TIMESTAMP NOT NULL,
  UPDATED_ON TIMESTAMP
);

DROP TABLE IF EXISTS JOB;
CREATE TABLE JOB(
  ID serial PRIMARY KEY NOT NULL,
   COMPANY_ID INTEGER REFERENCES COMPANY(ID) NOT NULL,
   COMPANY_JOB_ID VARCHAR (10),
   JOB_TITLE VARCHAR (100) NOT NULL,
   NO_OF_POSITIONS SMALLINT DEFAULT 1,
   JOB_DESCRIPTION TEXT NOT NULL,
   ML_DATA_AVAILABLE BOOL DEFAULT 'f',
   STATUS VARCHAR (10) DEFAULT 'Draft',
   DATE_PUBLISHED TIMESTAMP,
   DATE_ARCHIVED TIMESTAMP,
   SCORING_ENGINE_JOB_AVAILABLE BOOL DEFAULT 'f',
   BU_ID INTEGER REFERENCES COMPANY_BU(ID),
   FUNCTION INTEGER REFERENCES MASTER_DATA(ID),
   CURRENCY VARCHAR(3) NOT NULL DEFAULT 'INR',
   MIN_SALARY INTEGER default 0,
   MAX_SALARY INTEGER default 0,
   EDUCATION INTEGER REFERENCES MASTER_DATA(ID),
   EXPERIENCE_RANGE INTEGER REFERENCES MASTER_DATA(ID),
   JOB_LOCATION INTEGER REFERENCES COMPANY_ADDRESS(ID),
   INTERVIEW_LOCATION INTEGER REFERENCES COMPANY_ADDRESS(ID),
   EXPERTISE INTEGER REFERENCES MASTER_DATA(ID),
   HIRING_MANAGER INTEGER REFERENCES USERS(ID),
   RECRUITER INTEGER REFERENCES USERS(ID),
   NOTICE_PERIOD INTEGER REFERENCES MASTER_DATA(ID),
   JOB_REFERENCE_ID UUID NOT NULL DEFAULT uuid_generate_v1(),
   JOB_TYPE INTEGER REFERENCES MASTER_DATA(ID),
   CREATED_ON TIMESTAMP NOT NULL,
   CREATED_BY INTEGER REFERENCES USERS(ID),
   UPDATED_ON TIMESTAMP,
   UPDATED_BY INTEGER REFERENCES USERS(ID)
);

DROP TABLE IF EXISTS JOB_STAGE_STEP;
CREATE TABLE JOB_STAGE_STEP(
  ID serial PRIMARY KEY NOT NULL,
   JOB_ID INTEGER REFERENCES JOB(ID) NOT NULL,
   STAGE_STEP_ID INTEGER REFERENCES COMPANY_STAGE_STEP(ID) NOT NULL,
   CREATED_ON TIMESTAMP NOT NULL,
   CREATED_BY INTEGER REFERENCES USERS(ID) NOT NULL,
   UPDATED_ON TIMESTAMP,
   UPDATED_BY INTEGER REFERENCES USERS(ID),
   CONSTRAINT UNIQUE_JOB_STAGE_STEP UNIQUE (JOB_ID, STAGE_STEP_ID)
);

DROP TABLE IF EXISTS JOB_HIRING_TEAM;
CREATE TABLE JOB_HIRING_TEAM(
  ID serial PRIMARY KEY NOT NULL,
   JOB_ID INTEGER REFERENCES JOB(ID) NOT NULL,
   STAGE_STEP_ID INTEGER REFERENCES JOB_STAGE_STEP(ID) NOT NULL,
   USER_ID INTEGER REFERENCES USERS(ID) NOT NULL,
   SEQUENCE SMALLINT,
   CREATED_ON TIMESTAMP NOT NULL,
   CREATED_BY INTEGER REFERENCES USERS(ID) NOT NULL,
   UPDATED_ON TIMESTAMP,
   UPDATED_BY INTEGER REFERENCES USERS(ID)
);


DROP TABLE IF EXISTS JOB_SCREENING_QUESTIONS;
CREATE TABLE JOB_SCREENING_QUESTIONS(
  ID serial PRIMARY KEY NOT NULL,
  JOB_ID INTEGER REFERENCES JOB(ID) NOT NULL,
  MASTER_SCREENING_QUESTION_ID INTEGER REFERENCES SCREENING_QUESTION(ID),
  COMPANY_SCREENING_QUESTION_ID INTEGER REFERENCES COMPANY_SCREENING_QUESTION(ID),
  USER_SCREENING_QUESTION_ID INTEGER REFERENCES USER_SCREENING_QUESTION(ID),
  CREATED_ON TIMESTAMP NOT NULL,
  CREATED_BY INTEGER REFERENCES USERS(ID) NOT NULL,
  UPDATED_ON TIMESTAMP,
  UPDATED_BY INTEGER REFERENCES USERS(ID)
  CONSTRAINT CHK_ONLY_ONE_IS_NOT_NULL CHECK (ARRAY_LENGTH(ARRAY_REMOVE(ARRAY[MASTER_SCREENING_QUESTION_ID::INTEGER, COMPANY_SCREENING_QUESTION_ID::INTEGER, USER_SCREENING_QUESTION_ID::INTEGER], NULL), 1) = 1)
);

DROP TABLE IF EXISTS JOB_KEY_SKILLS;
CREATE TABLE JOB_KEY_SKILLS(
  ID serial PRIMARY KEY NOT NULL,
   JOB_ID INTEGER REFERENCES JOB(ID) NOT NULL,
   SKILL_ID INTEGER REFERENCES SKILLS_MASTER(ID),
   SKILL_ID_FROM_TEMP INTEGER REFERENCES TEMP_SKILLS(ID),
   ML_PROVIDED BOOLEAN NOT NULL,
   SELECTED BOOLEAN NOT NULL,
   CREATED_ON TIMESTAMP NOT NULL,
   CREATED_BY INTEGER REFERENCES USERS(ID) NOT NULL,
   UPDATED_ON TIMESTAMP,
   UPDATED_BY INTEGER REFERENCES USERS(ID),
   CONSTRAINT CHK_ONLY_ONE_IS_NOT_NULL CHECK (ARRAY_LENGTH(ARRAY_REMOVE(ARRAY[SKILL_ID::INTEGER,SKILL_ID_FROM_TEMP::INTEGER], NULL), 1) = 1)
);

create unique index unique_job_skill_id on job_key_skills (job_id, skill_id) where skill_id_from_temp is null;
create unique index unique_job_temp_skill_id on job_key_skills (job_id, skill_id_from_temp) where skill_id is null;

DROP TABLE IF EXISTS JOB_HISTORY;
CREATE TABLE JOB_HISTORY (
    ID serial PRIMARY KEY NOT NULL,
    JOB_ID INTEGER REFERENCES JOB(ID) NOT NULL,
    DETAILS VARCHAR(300),
    UPDATED_ON TIMESTAMP,
    UPDATED_BY INTEGER REFERENCES USERS(ID) NOT NULL
);

DROP TABLE IF EXISTS JOB_CAPABILITIES;
CREATE TABLE JOB_CAPABILITIES(
  ID serial PRIMARY KEY NOT NULL,
  JOB_ID INTEGER REFERENCES JOB(ID) NOT NULL,
   CAPABILITY_NAME VARCHAR (50) NOT NULL,
   CAPABILITY_ID INTEGER NOT NULL,
   SELECTED BOOLEAN NOT NULL,
   WEIGHTAGE SMALLINT NOT NULL,
   CREATED_ON TIMESTAMP NOT NULL,
   CREATED_BY INTEGER REFERENCES USERS(ID) NOT NULL,
   UPDATED_ON TIMESTAMP,
   UPDATED_BY INTEGER REFERENCES USERS(ID)
);

DROP TABLE IF EXISTS CANDIDATE;
CREATE TABLE CANDIDATE(
  ID serial PRIMARY KEY NOT NULL,
   FIRST_NAME VARCHAR (45) NOT NULL,
   LAST_NAME VARCHAR (45) NOT NULL,
   CREATED_ON TIMESTAMP NOT NULL,
   CREATED_BY INTEGER REFERENCES USERS(ID) NOT NULL,
   UPDATED_ON TIMESTAMP ,
   UPDATED_BY INTEGER REFERENCES USERS(ID)
);

DROP TABLE IF EXISTS CANDIDATE_MOBILE_HISTORY;
CREATE TABLE CANDIDATE_MOBILE_HISTORY(
  ID serial PRIMARY KEY NOT NULL,
   CANDIDATE_ID INTEGER REFERENCES CANDIDATE(ID) NOT NULL,
   MOBILE VARCHAR (15) NOT NULL,
   COUNTRY_CODE VARCHAR (5) NOT NULL,
   CREATED_ON TIMESTAMP NOT NULL,
   CREATED_BY INTEGER REFERENCES USERS(ID) NOT NULL,
   CONSTRAINT UNIQUE_CANDIDATE_MOBILE_HISTORY UNIQUE (MOBILE, COUNTRY_CODE)
);

DROP TABLE IF EXISTS CANDIDATE_EMAIL_HISTORY;
CREATE TABLE CANDIDATE_EMAIL_HISTORY(
  ID serial PRIMARY KEY NOT NULL,
   CANDIDATE_ID INTEGER REFERENCES CANDIDATE(ID) NOT NULL,
   EMAIL VARCHAR (50) NOT NULL UNIQUE,
   CREATED_ON TIMESTAMP NOT NULL,
   CREATED_BY INTEGER REFERENCES USERS(ID) NOT NULL
);

DROP TABLE IF EXISTS CANDIDATE_DETAILS;
CREATE TABLE CANDIDATE_DETAILS(
  ID serial PRIMARY KEY NOT NULL,
   CANDIDATE_ID INTEGER REFERENCES CANDIDATE(ID) NOT NULL,
   DATE_OF_BIRTH DATE,
   GENDER CHAR (1),
   MARITAL_STATUS VARCHAR (10),
   CATEGORY VARCHAR (25),
   PHYSICALLY_CHALLENGED BOOL DEFAULT 'f',
   CURRENT_ADDRESS VARCHAR (255),
   LOCATION VARCHAR (50),
   PREFERRED_LOCATIONS VARCHAR (255),
   PREFERRED_JOB_TYPE VARCHAR (20),
   PREFERRED_EMPLOYMENT_STATUS VARCHAR (10),
   TOTAL_EXPERIENCE NUMERIC (4, 2),
   INDUSTRY VARCHAR (100),
   FUNCTIONAL_AREA VARCHAR (60),
   ROLE VARCHAR (40),
   KEY_SKILLS VARCHAR (255),
   RESUME_HEADLINE VARCHAR (255),
   WORK_SUMMARY VARCHAR (255),
   OTHER_CERTIFICATES VARCHAR (50),
   CV_FILE_TYPE VARCHAR (10),
   LAST_ACTIVE DATE,
   CANDIDATE_TYPE VARCHAR (25),
   RELEVANT_EXPERIENCE NUMERIC (4, 2)
);

DROP TABLE IF EXISTS JOB_CANDIDATE_MAPPING;
CREATE TABLE JOB_CANDIDATE_MAPPING (
   ID	serial PRIMARY KEY NOT NULL,
   JOB_ID INTEGER REFERENCES JOB(ID) NOT NULL,
   CANDIDATE_ID INTEGER REFERENCES CANDIDATE(ID) NOT NULL,
   CANDIDATE_FIRST_NAME varchar(45) NOT NULL,
   CANDIDATE_LAST_NAME varchar(45) NOT NULL,
   EMAIL VARCHAR (50) NOT NULL,
   MOBILE VARCHAR (15),
   COUNTRY_CODE VARCHAR (5),
   STAGE INTEGER REFERENCES MASTER_DATA(ID) NOT NULL,
   CANDIDATE_SOURCE VARCHAR (10) NOT NULL,
   CHATBOT_UUID UUID NOT NULL DEFAULT uuid_generate_v1(),
   CANDIDATE_INTEREST BOOLEAN DEFAULT 'F',
   CANDIDATE_INTEREST_TIMESTAMP TIMESTAMP,
   CHATBOT_STATUS VARCHAR(15),
   SCORE SMALLINT,
   CHATBOT_UPDATED_ON TIMESTAMP,
   ALTERNATE_MOBILE VARCHAR (15),
   ALTERNATE_EMAIL VARCHAR (50),
   SERVING_NOTICE_PERIOD BOOL NOT NULL DEFAULT 'f',
   NEGOTIABLE_NOTICE_PERIOD BOOL NOT NULL DEFAULT 'f',
   OTHER_OFFERS BOOL NOT NULL DEFAULT 'f',
   UPDATE_RESUME BOOL NOT NULL DEFAULT 'f',
   COMMUNICATION_RATING SMALLINT DEFAULT 0,
   REASON_FOR_CHANGE VARCHAR(100),
   CREATED_ON TIMESTAMP NOT NULL,
   CREATED_BY INTEGER REFERENCES USERS(ID) NOT NULL,
   UPDATED_ON TIMESTAMP,
   UPDATED_BY INTEGER REFERENCES USERS(ID),
   JOB_REFERENCE_ID UUID NOT NULL DEFAULT uuid_generate_v1(),
   CONSTRAINT unique_job_candidate UNIQUE(JOB_ID, CANDIDATE_ID)
);

DROP TABLE IF EXISTS CANDIDATE_SCREENING_QUESTION_RESPONSE;
CREATE TABLE CANDIDATE_SCREENING_QUESTION_RESPONSE(
  ID serial PRIMARY KEY NOT NULL,
  JOB_CANDIDATE_MAPPING_ID INTEGER REFERENCES JOB_CANDIDATE_MAPPING(ID) NOT NULL,
  JOB_SCREENING_QUESTION_ID INTEGER REFERENCES JOB_SCREENING_QUESTIONS(ID) NOT NULL,
  RESPONSE VARCHAR(100) NOT NULL,
  COMMENT VARCHAR(100)
);

--Add create db script for plugin

DROP TABLE IF EXISTS CANDIDATE_EDUCATION_DETAILS;
CREATE TABLE CANDIDATE_EDUCATION_DETAILS (
	 ID serial PRIMARY KEY NOT NULL,
	  CANDIDATE_ID INTEGER REFERENCES CANDIDATE(ID) NOT NULL,
	  DEGREE VARCHAR(60),
	  SPECIALIZATION VARCHAR(50),
	  INSTITUTE_NAME VARCHAR(75),
	  YEAR_OF_PASSING VARCHAR(4)
  );

DROP TABLE IF EXISTS CANDIDATE_COMPANY_DETAILS;
CREATE TABLE CANDIDATE_COMPANY_DETAILS (
  ID serial PRIMARY KEY NOT NULL,
  CANDIDATE_ID INTEGER REFERENCES CANDIDATE(ID) NOT NULL,
  COMPANY_NAME VARCHAR(75),
  DESIGNATION VARCHAR(50),
  SALARY VARCHAR(20),
  LOCATION VARCHAR(100),
  NOTICE_PERIOD VARCHAR(15),
  START_DATE DATE,
  END_DATE DATE
  );

DROP TABLE IF EXISTS CANDIDATE_PROJECT_DETAILS;
CREATE TABLE CANDIDATE_PROJECT_DETAILS (
  ID serial PRIMARY KEY NOT NULL,
  CANDIDATE_ID INTEGER REFERENCES CANDIDATE(ID) NOT NULL,
  COMPANY_NAME VARCHAR(75),
  FROM_DATE DATE,
  TO_DATE DATE,
  CLIENT_NAME VARCHAR(50),
  ROLE VARCHAR(40),
  ROLE_DESCRIPTION VARCHAR(255),
  SKILLS_USED VARCHAR(255),
  EMPLOYMENT_STATUS VARCHAR(10)
  );

DROP TABLE IF EXISTS CANDIDATE_ONLINE_PROFILE;
CREATE TABLE CANDIDATE_ONLINE_PROFILE (
  ID serial PRIMARY KEY NOT NULL,
  CANDIDATE_ID INTEGER REFERENCES CANDIDATE(ID) NOT NULL,
  PROFILE_TYPE VARCHAR(20),
  URL VARCHAR(255)
  );

DROP TABLE IF EXISTS SMS_TEMPLATES;
CREATE TABLE SMS_TEMPLATES (
  ID serial PRIMARY KEY NOT NULL,
  TEMPLATE_NAME varchar(40) DEFAULT NULL,
  TEMPLATE_CONTENT varchar(400) DEFAULT NULL
);

DROP TABLE IF EXISTS CANDIDATE_WORK_AUTHORIZATION;
CREATE TABLE CANDIDATE_WORK_AUTHORIZATION (
  ID serial PRIMARY KEY NOT NULL,
  CANDIDATE_ID INTEGER REFERENCES CANDIDATE(ID) NOT NULL,
  COUNTRY_NAME VARCHAR(60),
  AUTHORIZED_TO_WORK BOOLEAN
  );

DROP TABLE IF EXISTS CANDIDATE_LANGUAGE_PROFICIENCY;
CREATE TABLE CANDIDATE_LANGUAGE_PROFICIENCY (
ID serial PRIMARY KEY NOT NULL,
  CANDIDATE_ID INTEGER REFERENCES CANDIDATE(ID) NOT NULL,
  LANGUAGE VARCHAR(15),
  PROFICIENCY VARCHAR(50)
  );

DROP TABLE IF EXISTS CANDIDATE_SKILL_DETAILS;
CREATE TABLE CANDIDATE_SKILL_DETAILS (
 ID serial PRIMARY KEY NOT NULL,
  CANDIDATE_ID INTEGER REFERENCES CANDIDATE(ID) NOT NULL,
  SKILL VARCHAR(50),
  LAST_USED DATE,
  EXP_IN_MONTHS smallint
  );

DROP TABLE IF EXISTS JCM_COMMUNICATION_DETAILS;
CREATE TABLE JCM_COMMUNICATION_DETAILS (
   ID	serial PRIMARY KEY NOT NULL,
   JCM_ID INTEGER REFERENCES JOB_CANDIDATE_MAPPING(ID) NOT NULL,
   CHAT_INVITE_TIMESTAMP_EMAIL TIMESTAMP DEFAULT NULL,
   CHAT_INVITE_TIMESTAMP_SMS TIMESTAMP DEFAULT NULL,
   CHAT_INCOMPLETE_REMINDER_1_TIMESTAMP_EMAIL TIMESTAMP DEFAULT NULL,
   CHAT_INCOMPLETE_REMINDER_1_TIMESTAMP_SMS TIMESTAMP DEFAULT NULL,
   CHAT_INCOMPLETE_REMINDER_2_TIMESTAMP_EMAIL TIMESTAMP DEFAULT NULL,
   CHAT_INCOMPLETE_REMINDER_2_TIMESTAMP_SMS TIMESTAMP DEFAULT NULL,
   LINK_NOT_VISITED_REMINDER_1_TIMESTAMP_EMAIL TIMESTAMP DEFAULT NULL,
   LINK_NOT_VISITED_REMINDER_1_TIMESTAMP_SMS TIMESTAMP DEFAULT NULL,
   LINK_NOT_VISITED_REMINDER_2_TIMESTAMP_EMAIL TIMESTAMP DEFAULT NULL,
   LINK_NOT_VISITED_REMINDER_2_TIMESTAMP_SMS TIMESTAMP DEFAULT NULL,
   CHAT_COMPLETE_TIMESTAMP_EMAIL TIMESTAMP DEFAULT NULL,
   CHAT_COMPLETE_TIMESTAMP_SMS TIMESTAMP DEFAULT NULL,
   CHAT_INVITE_FLAG BOOL DEFAULT 'f',
   CHAT_COMPLETE_FLAG BOOL DEFAULT 'f',
   HR_CHAT_COMPLETE_FLAG BOOL DEFAULT 'f'
);

DROP TABLE IF EXISTS EMAIL_LOG;
CREATE TABLE EMAIL_LOG (
  ID serial PRIMARY KEY NOT NULL,
  TEMPLATE_NAME VARCHAR(30) DEFAULT NULL,
  SENT_TO_ID INTEGER DEFAULT NULL,
  SENT_TO_EMAIL VARCHAR(50) DEFAULT NULL,
  SENT_TIMESTAMP TIMESTAMP DEFAULT NULL,
  TRANSACTION_ID VARCHAR(50) DEFAULT NULL,
  MESSAGE_ID VARCHAR(50) DEFAULT NULL,
  RESPONSE VARCHAR(350) DEFAULT NULL,
  RESPONSE_TIMESTAMP TIMESTAMP DEFAULT NULL,
  RESPONSE_CATEGORY VARCHAR(50) DEFAULT NULL,
  STATUS VARCHAR(50) DEFAULT NULL
);

DROP TABLE IF EXISTS SMS_LOG;
CREATE TABLE SMS_LOG (
  ID serial PRIMARY KEY NOT NULL,
  TEMPLATE_NAME VARCHAR(40) DEFAULT NULL,
  SENT_TO_ID INTEGER DEFAULT NULL,
  SMS_SENT TIMESTAMP DEFAULT NULL,
  SMS_RESPONSE VARCHAR(100) DEFAULT NULL
);

DROP TABLE IF EXISTS JCM_PROFILE_SHARING_MASTER;
CREATE TABLE JCM_PROFILE_SHARING_MASTER (
    ID serial PRIMARY KEY NOT NULL,
    RECEIVER_NAME varchar(45) NOT NULL,
    RECEIVER_EMAIL varchar(50) NOT NULL,
    SENDER_ID INTEGER REFERENCES USERS(ID) NOT NULL,
    EMAIL_SENT_ON TIMESTAMP DEFAULT NULL
);

DROP TABLE IF EXISTS JCM_PROFILE_SHARING_DETAILS;
CREATE TABLE JCM_PROFILE_SHARING_DETAILS (
    ID UUID PRIMARY KEY DEFAULT uuid_generate_v1(),
    PROFILE_SHARING_MASTER_ID INTEGER REFERENCES JCM_PROFILE_SHARING_MASTER(ID) NOT NULL,
    JOB_CANDIDATE_MAPPING_ID INTEGER REFERENCES JOB_CANDIDATE_MAPPING(ID) NOT NULL,
    HIRING_MANAGER_INTEREST BOOL DEFAULT FALSE,
    HIRING_MANAGER_INTEREST_DATE TIMESTAMP DEFAULT NULL,
    CONSTRAINT UNIQUE_JCM_HIRING_MANAGER UNIQUE (ID, PROFILE_SHARING_MASTER_ID)
);

DROP TABLE IF EXISTS CV_PARSING_DETAILS;
CREATE TABLE CV_PARSING_DETAILS (
    ID serial PRIMARY KEY NOT NULL,
    CV_FILE_NAME varchar(255),
    PROCESSED_ON TIMESTAMP,
    PROCESSING_TIME smallint,
    PROCESSING_STATUS varchar(10),
    PARSING_RESPONSE_JSON text,
    PARSING_RESPONSE_TEXT text,
    PARSING_RESPONSE_HTML text,
    ERROR_MESSAGE varchar(100),
    JOB_CANDIDATE_MAPPING_ID INTEGER REFERENCES JOB_CANDIDATE_MAPPING(ID),
    CV_RATING_API_FLAG BOOL DEFAULT 'F' NOT NULL,
    CANDIDATE_ID INTEGER,
    RCHILLI_JSON_PROCESSED BOOL,
    CV_RATING_API_RESPONSE_TIME smallint
);

DROP TABLE IF EXISTS CANDIDATE_OTHER_SKILL_DETAILS;
CREATE TABLE CANDIDATE_OTHER_SKILL_DETAILS (
    ID serial PRIMARY KEY NOT NULL,
    CANDIDATE_ID INTEGER REFERENCES CANDIDATE(ID) NOT NULL,
    SKILL VARCHAR(50),
    LAST_USED DATE,
    EXP_IN_MONTHS smallint
);

DROP TABLE IF EXISTS COMPANY_HISTORY;
CREATE TABLE COMPANY_HISTORY (
    ID serial PRIMARY KEY NOT NULL,
    COMPANY_ID INTEGER REFERENCES COMPANY(ID) NOT NULL,
    DETAILS VARCHAR(300),
    UPDATED_ON TIMESTAMP,
    UPDATED_BY INTEGER REFERENCES USERS(ID) NOT NULL
);

DROP TABLE IF EXISTS CANDIDATE_TECH_RESPONSE_DATA;
CREATE TABLE CANDIDATE_TECH_RESPONSE_DATA(
    ID serial PRIMARY KEY NOT NULL,
    JOB_CANDIDATE_MAPPING_ID INTEGER REFERENCES JOB_CANDIDATE_MAPPING(ID) NOT NULL,
    TECH_RESPONSE TEXT,
    CONSTRAINT UNIQUE_JCM_TECH_RESPONSE UNIQUE(JOB_CANDIDATE_MAPPING_ID)
);

DROP TABLE IF EXISTS JCM_HISTORY;
CREATE TABLE JCM_HISTORY(
	ID serial PRIMARY KEY NOT NULL,
	JCM_ID INTEGER REFERENCES JOB_CANDIDATE_MAPPING(ID) NOT NULL,
	COMMENT TEXT,
	CALL_LOG_OUTCOME VARCHAR(25),
	SYSTEM_GENERATED BOOL DEFAULT 't' NOT NULL,
	UPDATED_ON TIMESTAMP,
	UPDATED_BY INTEGER REFERENCES USERS(ID)
);

DROP TABLE IF EXISTS WEIGHTAGE_CUTOFF_MAPPING;
CREATE TABLE WEIGHTAGE_CUTOFF_MAPPING(
    ID serial PRIMARY KEY NOT NULL,
    WEIGHTAGE INTEGER DEFAULT NULL,
    PERCENTAGE SMALLINT DEFAULT NULL,
    CUTOFF SMALLINT DEFAULT NULL,
    STAR_RATING SMALLINT NOT NULL,
    CONSTRAINT UNIQUE_WEIGHTAGE_CUTOFF_MAPPING UNIQUE(WEIGHTAGE, STAR_RATING)
);

DROP TABLE IF EXISTS WEIGHTAGE_CUTOFF_BY_COMPANY_MAPPING;
CREATE TABLE WEIGHTAGE_CUTOFF_BY_COMPANY_MAPPING(
    ID serial PRIMARY KEY NOT NULL,
    COMPANY_ID INTEGER REFERENCES COMPANY(ID) NOT NULL,
    WEIGHTAGE INTEGER DEFAULT NULL,
    PERCENTAGE SMALLINT DEFAULT NULL,
    CUTOFF SMALLINT DEFAULT NULL,
    STAR_RATING SMALLINT NOT NULL,
    CONSTRAINT UNIQUE_WEIGHTAGE_CUTOFF_BY_COMPANY_MAPPING UNIQUE(COMPANY_ID, WEIGHTAGE, CUTOFF)
);

DROP TABLE IF EXISTS JOB_CAPABILITY_STAR_RATING_MAPPING;
CREATE TABLE JOB_CAPABILITY_STAR_RATING_MAPPING (
   ID serial PRIMARY KEY NOT NULL,
   JOB_CAPABILITY_ID INTEGER REFERENCES JOB_CAPABILITIES(ID) NOT NULL,
   JOB_ID INTEGER REFERENCES JOB(ID) NOT NULL,
   WEIGHTAGE SMALLINT NOT NULL,
   CUTOFF SMALLINT NOT NULL,
   PERCENTAGE SMALLINT NOT NULL,
   STAR_RATING SMALLINT NOT NULL,
   CONSTRAINT UNIQUE_JOB_CAPABILITY_WEIGHTAGE_STAR_RATING UNIQUE(JOB_CAPABILITY_ID,WEIGHTAGE,STAR_RATING)
);

DROP TABLE IF EXISTS CREATE_JOB_PAGE_SEQUENCE;
CREATE TABLE CREATE_JOB_PAGE_SEQUENCE(
	ID serial PRIMARY KEY NOT NULL,
	PAGE_NAME VARCHAR(25) NOT NULL,
	PAGE_DISPLAY_NAME VARCHAR(25) NOT NULL,
	PAGE_DISPLAY_ORDER SMALLINT NOT NULL,
	DISPLAY_FLAG BOOL NOT NULL DEFAULT 'T',
	SUBSCRIPTION_AVAILABILITY VARCHAR(5) NOT NULL DEFAULT 'LITE'
);

DROP TABLE IF EXISTS CV_RATING;
CREATE TABLE CV_RATING (
ID serial PRIMARY KEY NOT NULL,
JOB_CANDIDATE_MAPPING_ID integer REFERENCES JOB_CANDIDATE_MAPPING(ID) NOT NULL,
OVERALL_RATING smallint NOT NULL,
CONSTRAINT UNIQUE_CV_RATING_JCM UNIQUE(JOB_CANDIDATE_MAPPING_ID)
);

DROP TABLE IF EXISTS CV_RATING_SKILL_KEYWORD_DETAILS;
CREATE TABLE CV_RATING_SKILL_KEYWORD_DETAILS (
ID serial PRIMARY KEY NOT NULL,
CV_RATING_ID integer REFERENCES CV_RATING(ID) NOT NULL,
SUPPORTING_KEYWORDS text NOT NULL,
SKILL_NAME varchar(100) NOT NULL,
RATING smallint NOT NULL,
OCCURRENCE smallint NOT NULL
);

DROP TABLE IF EXISTS CURRENCY;
CREATE TABLE CURRENCY (
ID serial PRIMARY KEY NOT NULL,
CURRENCY_FULL_NAME varchar(25),
CURRENCY_SHORT_NAME varchar(5),
COUNTRY varchar(5)
);


SET session_replication_role TO 'origin';


DROP TABLE IF EXISTS EXPORT_FORMAT_MASTER;
CREATE TABLE EXPORT_FORMAT_MASTER(
ID serial PRIMARY KEY NOT NULL,
COMPANY_ID integer REFERENCES COMPANY(ID) DEFAULT NULL,
FORMAT varchar(15) NOT NULL,
SYSTEM_SUPPORTED BOOL DEFAULT FALSE
);


DROP TABLE IF EXISTS EXPORT_FORMAT_DETAIL;
CREATE TABLE EXPORT_FORMAT_DETAIL(
    ID serial PRIMARY KEY NOT NULL,
    FORMAT_ID integer REFERENCES EXPORT_FORMAT_MASTER(ID) NOT NULL,
    COLUMN_NAME VARCHAR(20),
    HEADER VARCHAR(20),
    POSITION SMALLINT,
    UNIQUE(FORMAT_ID, POSITION)
);