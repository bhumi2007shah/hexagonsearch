CREATE TABLE MASTER_DATA(
  ID serial PRIMARY KEY NOT NULL,
   TYPE VARCHAR (50) NOT NULL,
   VALUE VARCHAR (100) NOT NULL,
   CONSTRAINT UNIQUE_MASTER_DATA UNIQUE (TYPE, VALUE)
);

----------------------------------------------------

CREATE TABLE SKILLS_MASTER(
  ID serial PRIMARY KEY NOT NULL,
   SKILL_NAME VARCHAR (100) NOT NULL
);

----------------------------------------------------

CREATE TABLE TEMP_SKILLS(
  ID serial PRIMARY KEY NOT NULL,
   SKILL_NAME VARCHAR (100) NOT NULL,
   REVIEWED BOOL DEFAULT 'f'
);

-------------------------------------------------------------------------------------------------------------------------

CREATE TABLE SCREENING_QUESTION(
   ID serial PRIMARY KEY NOT NULL,
   QUESTION VARCHAR (150) NOT NULL,
   QUESTION_TYPE INTEGER REFERENCES MASTER_DATA(ID) NOT NULL,
   OPTIONS VARCHAR(100)[]
);

---------------------------------------------------------------------------------------------------------------------


CREATE TABLE COUNTRY(
  ID serial PRIMARY KEY NOT NULL,
   COUNTRY_NAME VARCHAR (15) NOT NULL,
   COUNTRY_CODE VARCHAR (5) NOT NULL,
   MAX_MOBILE_LENGTH SMALLINT NOT NULL
);


---------------------------------------------------------------------------------------------------------------------------------------------------

CREATE TABLE USERS(
  ID serial PRIMARY KEY NOT NULL,
   EMAIL VARCHAR (50) NOT NULL,
   PASSWORD VARCHAR (50),
   FIRST_NAME VARCHAR (45) NOT NULL,
   LAST_NAME VARCHAR (45) NOT NULL,
   MOBILE VARCHAR (15) NOT NULL,
   COMPANY_ID INTEGER NOT NULL,
   ROLE VARCHAR (15) NOT NULL,
   DESIGNATION VARCHAR (45),
   STATUS VARCHAR (10) DEFAULT 'NEW',
   COUNTRY_ID INTEGER NOT NULL REFERENCES COUNTRY(ID),
   CREATED_ON TIMESTAMP NOT NULL,
   CREATED_BY INTEGER,
   UPDATED_ON TIMESTAMP,
   UPDATED_BY INTEGER
);

-----------------------------------------------------------------------------------------------------------

CREATE TABLE COMPANY(
  ID serial PRIMARY KEY NOT NULL,
   COMPANY_NAME VARCHAR (255) NOT NULL,
   ACTIVE BOOL DEFAULT 't',
   CREATED_ON TIMESTAMP NOT NULL,
   CREATED_BY INTEGER REFERENCES USERS(ID) NOT NULL,
   UPDATED_ON TIMESTAMP,
   UPDATED_BY INTEGER REFERENCES USERS(ID) 
);

ALTER TABLE USERS
ADD CONSTRAINT fk_company FOREIGN KEY (COMPANY_ID) REFERENCES COMPANY (ID);
-----------------------------------------------------------------------------------------------------------


CREATE TABLE COMPANY_ADDRESS(
  ID serial PRIMARY KEY NOT NULL,
   COMPANY_ID INTEGER REFERENCES COMPANY(ID) NOT NULL,
   ADDRESS VARCHAR (150) NOT NULL,
   ADDRESS_TYPE INTEGER REFERENCES MASTER_DATA(ID) NOT NULL,
   LATITUDE DOUBLE PRECISION,
   LONGITUDE DOUBLE PRECISION,
   CREATED_ON TIMESTAMP NOT NULL,
   CREATED_BY INTEGER REFERENCES USERS(ID) NOT NULL,
   UPDATED_ON TIMESTAMP,
   UPDATED_BY INTEGER REFERENCES USERS(ID)  
);

--------------------------------------------------------------------------------------------------------

CREATE TABLE COMPANY_BU(
  ID serial PRIMARY KEY NOT NULL,
   COMPANY_ID INTEGER REFERENCES COMPANY(ID) NOT NULL,
   BUSINESS_UNIT VARCHAR (100) NOT NULL,
   CREATED_ON TIMESTAMP NOT NULL,
   CREATED_BY INTEGER REFERENCES USERS(ID) NOT NULL,
   UPDATED_ON TIMESTAMP,
   UPDATED_BY INTEGER REFERENCES USERS(ID)  
);

---------------------------------------------------------------------------------------------------------

CREATE TABLE COMPANY_STAGE_STEP(
  ID serial PRIMARY KEY NOT NULL,
   COMPANY_ID INTEGER REFERENCES COMPANY(ID) NOT NULL,
   STAGE INTEGER REFERENCES MASTER_DATA(ID) NOT NULL,
   STEP VARCHAR (25) NOT NULL,
   CREATED_ON TIMESTAMP NOT NULL,
   CREATED_BY INTEGER REFERENCES USERS(ID) NOT NULL,
   UPDATED_ON TIMESTAMP,
   UPDATED_BY INTEGER REFERENCES USERS(ID),
   CONSTRAINT UNIQUE_COMPANY_STAGE_STEP UNIQUE (COMPANY_ID, STAGE,STEP)
);

---------------------------------------------------------------------------------------------------------------------------------------------------

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


------------------------------------------------------------------------------------------------------------------------------------------------------

CREATE TABLE USER_SCREENING_QUESTION(
  ID serial PRIMARY KEY NOT NULL,
  USER_ID INTEGER REFERENCES USERS(ID) NOT NULL,
  QUESTION VARCHAR (150) NOT NULL,
  QUESTION_TYPE INTEGER REFERENCES MASTER_DATA(ID) NOT NULL,
  OPTIONS VARCHAR(100)[],
  CREATED_ON TIMESTAMP NOT NULL,
  UPDATED_ON TIMESTAMP
);

-------------------------------------------------------------------------------------------------------------------------------------------------------

CREATE TABLE JOB(
  ID serial PRIMARY KEY NOT NULL,
   COMPANY_ID INTEGER REFERENCES COMPANY(ID) NOT NULL,
   COMPANY_JOB_ID VARCHAR (10),
   JOB_TITLE VARCHAR (100) NOT NULL,
   NO_OF_POSITIONS SMALLINT NOT NULL,
   JOB_DESCRIPTION TEXT NOT NULL,
   ML_DATA_AVAILABLE BOOL DEFAULT 'f',
    STATUS VARCHAR (10) DEFAULT 'Draft',
   DATE_PUBLISHED TIMESTAMP,
   DATE_ARCHIVED TIMESTAMP,
   CREATED_ON TIMESTAMP NOT NULL,
   CREATED_BY INTEGER REFERENCES USERS(ID) NOT NULL,
   UPDATED_ON TIMESTAMP,
   UPDATED_BY INTEGER REFERENCES USERS(ID)
);

------------------------------------------------------------------------------------------------------------------------------------------------------

CREATE TABLE JOB_DETAIL(
   JOB_ID INTEGER REFERENCES JOB(ID) NOT NULL,
   BU_ID INTEGER REFERENCES COMPANY_BU(ID) NOT NULL,
   FUNCTION INTEGER REFERENCES MASTER_DATA(ID) NOT NULL,
   CURRENCY VARCHAR (10) NOT NULL,
   MIN_SALARY INTEGER,
   MAX_SALARY INTEGER,
   MIN_EXPERIENCE NUMERIC (4, 2) NOT NULL,
   MAX_EXPERIENCE NUMERIC (4, 2) NOT NULL,
   EDUCATION INTEGER REFERENCES MASTER_DATA(ID) NOT NULL,
   JOB_LOCATION INTEGER REFERENCES COMPANY_ADDRESS(ID) NOT NULL,
   INTERVIEW_LOCATION INTEGER REFERENCES COMPANY_ADDRESS(ID) NOT NULL,
   EXPERTISE INTEGER REFERENCES MASTER_DATA(ID) NOT NULL,
   PRIMARY KEY (JOB_ID)
)
INHERITS (JOB);

------------------------------------------------------------------------------------------------------------------------------------------------

CREATE TABLE JOB_HIRING_TEAM(
  ID serial PRIMARY KEY NOT NULL,
   JOB_ID INTEGER REFERENCES JOB(ID) NOT NULL,
   STAGE_STEP_ID INTEGER REFERENCES COMPANY_STAGE_STEP(ID) NOT NULL,
   USER_ID INTEGER REFERENCES USERS(ID) NOT NULL,
   SEQUENCE SMALLINT,
   CREATED_ON TIMESTAMP NOT NULL,
   CREATED_BY INTEGER REFERENCES USERS(ID) NOT NULL,
   UPDATED_ON TIMESTAMP,
   UPDATED_BY INTEGER REFERENCES USERS(ID)
);

----------------------------------------------------------------------------------------------------------------------------------------------------

CREATE TABLE JOB_SCREENING_QUESTIONS(
  ID serial PRIMARY KEY NOT NULL,
  JOB_ID INTEGER REFERENCES JOB(ID) NOT NULL,
  MASTER_SCREENING_QUESTION_ID INTEGER REFERENCES SCREENING_QUESTION(ID),
  COMPANY_SCREENING_QUESTION_ID INTEGER REFERENCES COMPANY_SCREENING_QUESTION(ID),
  USER_SCREENING_QUESTION_ID INTEGER REFERENCES USER_SCREENING_QUESTION(ID),
  CREATED_ON TIMESTAMP,
  CREATED_BY INTEGER REFERENCES USERS(ID),
  UPDATED_ON TIMESTAMP NOT NULL,
  UPDATED_BY INTEGER REFERENCES USERS(ID) NOT NULL
  CONSTRAINT CHK_ONLY_ONE_IS_NOT_NULL CHECK (ARRAY_LENGTH(ARRAY_REMOVE(ARRAY[MASTER_SCREENING_QUESTION_ID::INTEGER, COMPANY_SCREENING_QUESTION_ID::INTEGER, USER_SCREENING_QUESTION_ID::INTEGER], NULL), 1) = 1)
);

--------------------------------------------------------------------------------------------------------------------------------------------------

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

---------------------------------------------------------------------------------------------------------------------------------------------------

CREATE TABLE JOB_HISTORY(
  ID serial PRIMARY KEY NOT NULL,
   JOB_ID INTEGER REFERENCES JOB(ID) NOT NULL,
   CHANGE_TEXT VARCHAR (255) NOT NULL,
   MODIFIED_BY INTEGER REFERENCES USERS(ID) NOT NULL,
   MODIFIED_ON TIMESTAMP NOT NULL
);

------------------------------------------------------------------------------------------------------------------------------------------------

CREATE TABLE JOB_CAPABILITIES(
  ID serial PRIMARY KEY NOT NULL,
  JOB_ID INTEGER REFERENCES JOB(ID) NOT NULL,
   CAPABILITY_NAME VARCHAR (45) NOT NULL,
   SELECTED BOOLEAN NOT NULL,
   IMPORTANCE_LEVEL INTEGER REFERENCES MASTER_DATA(ID) NOT NULL,
   CREATED_ON TIMESTAMP NOT NULL,
   CREATED_BY INTEGER REFERENCES USERS(ID) NOT NULL,
   UPDATED_ON TIMESTAMP,
   UPDATED_BY INTEGER REFERENCES USERS(ID)
);

--------------------------------------------------------------------------------------------------------------------------------------------

CREATE TABLE CANDIDATE(
  ID serial PRIMARY KEY NOT NULL,
   FIRST_NAME VARCHAR (45) NOT NULL,
   LAST_NAME VARCHAR (45) NOT NULL,
   CREATED_ON TIMESTAMP NOT NULL,
   CREATED_BY INTEGER REFERENCES USERS(ID) NOT NULL,
   UPDATED_ON TIMESTAMP ,
   UPDATED_BY INTEGER REFERENCES USERS(ID)
);


---------------------------------------------------------------------------------------------------------------------------------------


CREATE TABLE CANDIDATE_MOBILE_HISTORY(
  ID serial PRIMARY KEY NOT NULL,
   CANDIDATE_ID INTEGER REFERENCES CANDIDATE(ID) NOT NULL,
   MOBILE VARCHAR (15) NOT NULL,
   COUNTRY_CODE VARCHAR (5) NOT NULL,
   CREATED_ON TIMESTAMP NOT NULL,
   CREATED_BY INTEGER REFERENCES USERS(ID) NOT NULL,
   CONSTRAINT UNIQUE_CANDIDATE_MOBILE_HISTORY UNIQUE (MOBILE, COUNTRY_CODE)
);

----------------------------------------------------------------------------------------------------------------------------------------


CREATE TABLE CANDIDATE_EMAIL_HISTORY(
  ID serial PRIMARY KEY NOT NULL,
   CANDIDATE_ID INTEGER REFERENCES CANDIDATE(ID) NOT NULL,
   EMAIL VARCHAR (15) NOT NULL UNIQUE,
   CREATED_ON TIMESTAMP NOT NULL,
   CREATED_BY INTEGER REFERENCES USERS(ID) NOT NULL
);

---------------------------------------------------------------------------------------------------------------------------------------------


CREATE TABLE CANDIDATE_DETAILS(
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
   CANDIDATE_SOURCE VARCHAR (25) DEFAULT 'File' NOT NULL,
   CANDIDATE_TYPE VARCHAR (25),
   PRIMARY KEY (CANDIDATE_ID)
);

---------------------------------------------------------------------------------------------------------------------------------------------

CREATE TABLE JOB_CANDIDATE_MAPPING (
   ID	serial PRIMARY KEY NOT NULL,
   JOB_ID INTEGER REFERENCES JOB(ID) NOT NULL,
   CANDIDATE_ID INTEGER REFERENCES CANDIDATE(ID) NOT NULL,
   STAGE INTEGER REFERENCES MASTER_DATA(ID) NOT NULL,
   CREATED_ON TIMESTAMP NOT NULL,
   CREATED_BY INTEGER REFERENCES USERS(ID) NOT NULL,
   UPDATED_ON TIMESTAMP,
   UPDATED_BY INTEGER REFERENCES USERS(ID)
);

