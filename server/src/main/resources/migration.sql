--Add EMAIL, MOBILE in JCM
ALTER TABLE JOB_CANDIDATE_MAPPING ADD COLUMN EMAIL VARCHAR (50);
ALTER TABLE JOB_CANDIDATE_MAPPING ADD COLUMN MOBILE VARCHAR (15);
ALTER TABLE JOB_CANDIDATE_MAPPING ADD COLUMN COUNTRY_CODE VARCHAR (5);

UPDATE JOB_CANDIDATE_MAPPING SET EMAIL = concat('sdedhia+',ID,'@gmail.com');
UPDATE JOB_CANDIDATE_MAPPING SET MOBILE = concat('87600',ID,'6785');

ALTER TABLE JOB_CANDIDATE_MAPPING ALTER COLUMN EMAIL SET NOT NULL;

-- rename uuid column in jcm
ALTER TABLE JOB_CANDIDATE_MAPPING
rename column jcm_uuid to chatbot_uuid;

Insert into MASTER_DATA (TYPE, VALUE) values
('industry','Any'),
('industry','Accounting / Finance'),
('industry','Advertising / PR / MR / Events'),
('industry','Agriculture / Dairy'),
('industry','Animation'),
('industry','Architecture / Interior Design'),
('industry','Auto / Auto Ancillary'),
('industry','Aviation / Aerospace Firm'),
('industry','Banking / Financial Services / Broking'),
('industry','BPO / ITES'),
('industry','Brewery / Distillery'),
('industry','Chemicals / PetroChemical / Plastic / Rubber'),
('industry','Construction / Engineering / Cement / Metals'),
('industry','Consumer Durables'),
('industry','Courier / Transportation / Freight'),
('industry','Ceramics /Sanitary ware'),
('industry','Defence / Government'),
('industry','Education / Teaching / Training'),
('industry','Electricals / Switchgears'),
('industry','Export / Import'),
('industry','Facility Management'),
('industry','Fertilizers / Pesticides'),
('industry','FMCG / Foods / Beverage'),
('industry','Food Processing'),
('industry','Fresher / Trainee'),
('industry','Gems & Jewellery'),
('industry','Glass'),
('industry','Heat Ventilation Air Conditioning'),
('industry','Hotels / Restaurants / Airlines / Travel'),
('industry','Industrial Products / Heavy Machinery'),
('industry','Insurance'),
('industry','IT-Software / Software Services'),
('industry','IT-Hardware & Networking'),
('industry','Telecom / ISP'),
('industry','KPO / Research /Analytics'),
('industry','Legal'),
('industry','Media / Dotcom / Entertainment'),
('industry','Internet / Ecommerce'),
('industry','Medical / Healthcare / Hospital'),
('industry','Mining'),
('industry','NGO / Social Services'),
('industry','Office Equipment / Automation'),
('industry','Oil and Gas / Power / Infrastructure / Energy'),
('industry','Paper'),
('industry','Pharma / Biotech / Clinical Research'),
('industry','Printing / Packaging'),
('industry','Publishing'),
('industry','Real Estate / Property'),
('industry','Recruitment'),
('industry','Retail'),
('industry','Security / Law Enforcement'),
('industry','Semiconductors / Electronics'),
('industry','Shipping / Marine'),
('industry','Steel'),
('industry','Strategy /Management Consulting Firms'),
('industry','Textiles / Garments / Accessories'),
('industry','Tyres'),
('industry','Water Treatment / Waste Management'),
('industry','Wellness/Fitness/Sports'),
('industry','Other');

ALTER TABLE COMPANY
ADD COLUMN COMPANY_DESCRIPTION TEXT,
ADD COLUMN WEBSITE VARCHAR(245),
ADD COLUMN LANDLINE VARCHAR(10),
ADD COLUMN INDUSTRY INTEGER REFERENCES MASTER_DATA(ID),
ADD COLUMN LINKEDIN VARCHAR(245),
ADD COLUMN FACEBOOK VARCHAR(245),
ADD COLUMN TWITTER VARCHAR(245),
ADD COLUMN LOGO VARCHAR(245),
ADD COLUMN SUBSCRIPTION VARCHAR(5) NOT NULL DEFAULT 'Lite';

ALTER TABLE USERS
ADD COLUMN INVITATION_MAIL_TIMESTAMP TIMESTAMP NOT NULL DEFAULT localtimestamp,
ADD COLUMN RESET_PASSWORD_FLAG BOOL NOT NULL DEFAULT 'f',
ADD COLUMN RESET_PASSWORD_EMAIL_TIMESTAMP TIMESTAMP;

ALTER TABLE JCM_COMMUNICATION_DETAILS
ADD COLUMN CHAT_INVITE_FLAG BOOL DEFAULT 'f';

alter table users alter column user_uuid drop not null;

CREATE TABLE JCM_PROFILE_SHARING_DETAILS (
    ID UUID PRIMARY KEY DEFAULT uuid_generate_v1(),
    SENDER_ID INTEGER REFERENCES USERS(ID) NOT NULL,
    RECEIVER_NAME varchar(45) NOT NULL,
    RECEIVER_EMAIL varchar(50) NOT NULL,
    JOB_CANDIDATE_MAPPING_ID INTEGER REFERENCES JOB_CANDIDATE_MAPPING(ID) NOT NULL,
    EMAIL_SENT_ON TIMESTAMP DEFAULT NULL,
    HIRING_MANAGER_INTEREST BOOL DEFAULT FALSE,
    HIRING_MANAGER_INTEREST_DATE TIMESTAMP DEFAULT NULL,
    CONSTRAINT UNIQUE_JCM_HIRING_MANAGER UNIQUE (ID, SENDER_ID, RECEIVER_EMAIL)
);

alter table JCM_COMMUNICATION_DETAILS
ADD COLUMN CHAT_COMPLETE_FLAG BOOL DEFAULT 'f';

alter table CANDIDATE_EDUCATION_DETAILS
alter column INSTITUTE_NAME type varchar(75);


drop table JCM_PROFILE_SHARING_DETAILS;

CREATE TABLE JCM_PROFILE_SHARING_MASTER (
    ID serial PRIMARY KEY NOT NULL,
    RECEIVER_NAME varchar(45) NOT NULL,
    RECEIVER_EMAIL varchar(50) NOT NULL,
    SENDER_ID INTEGER REFERENCES USERS(ID) NOT NULL,
    EMAIL_SENT_ON TIMESTAMP DEFAULT NULL
);

CREATE TABLE JCM_PROFILE_SHARING_DETAILS (
    ID UUID PRIMARY KEY DEFAULT uuid_generate_v1(),
    PROFILE_SHARING_MASTER_ID INTEGER REFERENCES JCM_PROFILE_SHARING_MASTER(ID) NOT NULL,
    JOB_CANDIDATE_MAPPING_ID INTEGER REFERENCES JOB_CANDIDATE_MAPPING(ID) NOT NULL,
    HIRING_MANAGER_INTEREST BOOL DEFAULT FALSE,
    HIRING_MANAGER_INTEREST_DATE TIMESTAMP DEFAULT NULL,
    CONSTRAINT UNIQUE_JCM_HIRING_MANAGER UNIQUE (ID, PROFILE_SHARING_MASTER_ID)
);

alter table EMAIL_LOG
alter column TEMPLATE_NAME type varchar(30);

-- Fix for ticket #76
alter table USERS alter column INVITATION_MAIL_TIMESTAMP drop not null;
alter table USERS alter column INVITATION_MAIL_TIMESTAMP set default null;

-- Fix for ticket #81
update configuration_settings
set config_value = 50 where config_name='maxScreeningQuestionsLimit';

-- fix for ticket #80
alter table job_candidate_mapping
add column candidate_first_name varchar(45),
add column candidate_last_name varchar(45);

update job_candidate_mapping
set candidate_first_name = first_name from candidate where candidate.id = job_candidate_mapping.candidate_id;

update job_candidate_mapping
set candidate_last_name = last_name from candidate where candidate.id = job_candidate_mapping.candidate_id;

alter table job_candidate_mapping
alter column candidate_first_name set not null,
alter column candidate_last_name set not null;

-- changes for ticket #88
CREATE TABLE CV_PARSING_DETAILS (
    ID serial PRIMARY KEY NOT NULL,
    CV_FILE_NAME varchar(255),
    PROCESSED_ON TIMESTAMP,
    PROCESSING_TIME smallint,
    PROCESSING_STATUS varchar(10),
    PARSING_RESPONSE text
);

CREATE TABLE CANDIDATE_OTHER_SKILL_DETAILS (
    ID serial PRIMARY KEY NOT NULL,
    CANDIDATE_ID INTEGER REFERENCES CANDIDATE(ID) NOT NULL,
    SKILL VARCHAR(50),
    LAST_USED DATE,
    EXP_IN_MONTHS smallint
);

ALTER TABLE CANDIDATE_SKILL_DETAILS
ADD COLUMN EXP_IN_MONTHS smallint;

insert into configuration_settings(config_name, config_value)
values('maxCvFiles',20);

ALTER TABLE CV_PARSING_DETAILS
  RENAME COLUMN PARSING_RESPONSE TO PARSING_RESPONSE_JSON;

ALTER TABLE CV_PARSING_DETAILS
  ADD PARSING_RESPONSE_TEXT text,
  ADD PARSING_RESPONSE_HTML text;

ALTER TABLE CANDIDATE_SKILL_DETAILS
  ADD COLUMN VERSION VARCHAR(10);


------------- For ticket #107
ALTER TABLE JOB_CAPABILITIES
ADD COLUMN CAPABILITY_ID INTEGER;

-- add script here to update existing capabilities with capability_name

ALTER TABLE JOB_CAPABILITIES ALTER COLUMN CAPABILITY_ID SET NOT NULL;

ALTER TABLE JOB_CAPABILITIES
DROP COLUMN IMPORTANCE_LEVEL;

ALTER TABLE JOB_CAPABILITIES
ADD COLUMN WEIGHTAGE SMALLINT NOT NULL DEFAULT 2;

-- For ticket #119
DELETE FROM MASTER_DATA WHERE TYPE='importanceLevel';

-- For ticket #52
CREATE TABLE COMPANY_HISTORY (
    ID serial PRIMARY KEY NOT NULL,
    COMPANY_ID INTEGER REFERENCES COMPANY(ID) NOT NULL,
    DETAILS VARCHAR(300),
    UPDATED_ON TIMESTAMP,
    UPDATED_BY INTEGER REFERENCES USERS(ID) NOT NULL
);

DROP TABLE JOB_HISTORY;

CREATE TABLE JOB_HISTORY (
    ID serial PRIMARY KEY NOT NULL,
    JOB_ID INTEGER REFERENCES JOB(ID) NOT NULL,
    DETAILS VARCHAR(300),
    UPDATED_ON TIMESTAMP,
    UPDATED_BY INTEGER REFERENCES USERS(ID) NOT NULL
);

-- add a column error_message to cv_parsing_details for rChilli
ALTER TABLE CV_PARSING_DETAILS
ADD COLUMN ERROR_MESSAGE varchar(100);


-- delete duplicate entry in skills master table and also remove rows from job key skills which references skill_id. Need to match by lower case.
SELECT
    LOWER(skill_name),
    COUNT( LOWER(skill_name) )
FROM
    skills_master
GROUP BY
    LOWER(skill_name)
HAVING
    COUNT( LOWER(skill_name) )> 1
ORDER BY
    LOWER(skill_name);

DELETE
FROM
    skills_master a
        USING skills_master b
WHERE
    a.id < b.id
    AND LOWER(a.skill_name) = LOWER(b.skill_name);

-- Added unique constraint on skill_name in skills_master with case insensitivity
Alter table skills_master add constraint unique_skill_name unique(skill_name);

-- For ticket #123
ALTER TABLE JOB_CANDIDATE_MAPPING
ADD COLUMN CHATBOT_STATUS VARCHAR(15),
ADD COLUMN SCORE SMALLINT,
ADD COLUMN CHATBOT_UPDATED_ON TIMESTAMP;

CREATE TABLE CANDIDATE_TECH_RESPONSE_DATA(
    ID serial PRIMARY KEY NOT NULL,
    JOB_CANDIDATE_MAPPING_ID INTEGER REFERENCES JOB_CANDIDATE_MAPPING(ID) NOT NULL,
    TECH_RESPONSE TEXT,
    CONSTRAINT UNIQUE_JCM_TECH_RESPONSE UNIQUE(JOB_CANDIDATE_MAPPING_ID)
);


INSERT INTO CANDIDATE_TECH_RESPONSE_DATA (JOB_CANDIDATE_MAPPING_ID)
SELECT ID FROM JOB_CANDIDATE_MAPPING;

-- For ticket #126

CREATE TABLE JCM_HISTORY(
	ID serial PRIMARY KEY NOT NULL,
	JCM_ID INTEGER REFERENCES JOB_CANDIDATE_MAPPING(ID) NOT NULL,
	DETAILS VARCHAR(300),
	UPDATED_ON TIMESTAMP,
	UPDATED_BY INTEGER REFERENCES USERS(ID)
);

-- For ticket #135
ALTER TABLE JOB
ADD COLUMN SCORING_ENGINE_JOB_AVAILABLE BOOL DEFAULT 'f';

UPDATE JOB SET SCORING_ENGINE_JOB_AVAILABLE = 'f';

-- For ticket #143
ALTER TABLE JCM_COMMUNICATION_DETAILS ADD COLUMN HR_CHAT_COMPLETE_FLAG BOOL DEFAULT 'f';

UPDATE JCM_COMMUNICATION_DETAILS SET HR_CHAT_COMPLETE_FLAG = 't' where CHAT_COMPLETE_FLAG='t';

-- For ticket #144
INSERT INTO CONFIGURATION_SETTINGS(CONFIG_NAME, CONFIG_VALUE)
VALUES('mlCall',1);

-- For ticket #145
ALTER TABLE JOB
ADD COLUMN BU_ID INTEGER REFERENCES COMPANY_BU(ID),
ADD COLUMN FUNCTION INTEGER REFERENCES MASTER_DATA(ID),
ADD COLUMN CURRENCY VARCHAR (10),
ADD COLUMN MIN_SALARY INTEGER,
ADD COLUMN MAX_SALARY INTEGER,
ADD COLUMN MIN_EXPERIENCE NUMERIC (4, 2),
ADD COLUMN MAX_EXPERIENCE NUMERIC (4, 2),
ADD COLUMN EDUCATION INTEGER REFERENCES MASTER_DATA(ID),
ADD COLUMN JOB_LOCATION INTEGER REFERENCES COMPANY_ADDRESS(ID),
ADD COLUMN INTERVIEW_LOCATION INTEGER REFERENCES COMPANY_ADDRESS(ID),
ADD COLUMN EXPERTISE INTEGER REFERENCES MASTER_DATA(ID);

ALTER TABLE JOB ALTER COLUMN NO_OF_POSITIONS SET DEFAULT 1;

-- For ticket #151
INSERT INTO MASTER_DATA (TYPE, VALUE)
VALUES
 ( 'noticePeriod','0'),
 ( 'noticePeriod','15'),
 ( 'noticePeriod','30'),
 ( 'noticePeriod','60'),
 ( 'noticePeriod','45'),
 ( 'noticePeriod','90'),
 ( 'noticePeriod','Others');

ALTER TABLE CANDIDATE_COMPANY_DETAILS
RENAME COLUMN NOTICE_PERIOD TO NOTICE_PERIOD_OLD;

ALTER TABLE CANDIDATE_COMPANY_DETAILS
ADD COLUMN NOTICE_PERIOD INTEGER REFERENCES MASTER_DATA(ID);

UPDATE CANDIDATE_COMPANY_DETAILS
SET NOTICE_PERIOD = (SELECT ID FROM MASTER_DATA WHERE TYPE = 'noticePeriod' AND VALUE = CANDIDATE_COMPANY_DETAILS.NOTICE_PERIOD_OLD);

-- Note: If above query does not work using the next one.
UPDATE CANDIDATE_COMPANY_DETAILS
SET NOTICE_PERIOD = (SELECT ID FROM MASTER_DATA WHERE TYPE = 'noticePeriod' AND VALUE = CANDIDATE_COMPANY_DETAILS.NOTICE_PERIOD_OLD::character varying);


ALTER TABLE CANDIDATE_COMPANY_DETAILS DROP COLUMN NOTICE_PERIOD_OLD;

-- For ticket #154

CREATE TABLE WEIGHTAGE_CUTOFF_MAPPING(
    ID serial PRIMARY KEY NOT NULL,
    WEIGHTAGE INTEGER DEFAULT NULL,
    PERCENTAGE SMALLINT DEFAULT NULL,
    CUTOFF SMALLINT DEFAULT NULL,
    STAR_RATING SMALLINT NOT NULL,
    CONSTRAINT UNIQUE_WEIGHTAGE_STAR_RATING_MAPPING UNIQUE(WEIGHTAGE, STAR_RATING)
);

CREATE TABLE WEIGHTAGE_CUTOFF_BY_COMPANY_MAPPING(
    ID serial PRIMARY KEY NOT NULL,
    COMPANY_ID INTEGER REFERENCES COMPANY(ID) NOT NULL,
    WEIGHTAGE INTEGER DEFAULT NULL,
    PERCENTAGE SMALLINT DEFAULT NULL,
    CUTOFF SMALLINT DEFAULT NULL,
    STAR_RATING SMALLINT NOT NULL,
    CONSTRAINT UNIQUE_WEIGHTAGE_STAR_RATING_BY_COMPANY_MAPPING UNIQUE(COMPANY_ID, WEIGHTAGE, STAR_RATING)
);

CREATE TABLE JOB_CAPABILITY_STAR_RATING_MAPPING (
   ID serial PRIMARY KEY NOT NULL,
   JOB_ID INTEGER REFERENCES JOB(ID) NOT NULL,
   JOB_CAPABILITY_ID INTEGER REFERENCES JOB_CAPABILITIES(ID) NOT NULL,
   WEIGHTAGE SMALLINT NOT NULL,
   CUTOFF SMALLINT NOT NULL,
   PERCENTAGE SMALLINT NOT NULL,
   STAR_RATING SMALLINT NOT NULL,
   CONSTRAINT UNIQUE_JOB_CAPABILITY_WEIGHTAGE_STAR_RATING UNIQUE(JOB_CAPABILITY_ID,WEIGHTAGE,STAR_RATING)
);

insert into weightage_cutoff_mapping (weightage, percentage, cutoff, star_rating)
values
(2,100,10,1),
(2,100,20,2),
(2,80,40,3),
(2,40,80,4),
(2,20,100,5),
(6,100,20,1),
(6,100,40,2),
(6,60,60,3),
(6,20,100,4),
(6,0,100,5),
(10,0,30,1),
(10,0,50,2),
(10,0,70,3),
(10,0,80,4),
(10,0,100,5);

--For ticket #162
ALTER TABLE MASTER_DATA
ADD COLUMN VALUE_TO_USE SMALLINT,
ADD COLUMN COMMENTS VARCHAR (255);

UPDATE MASTER_DATA
SET VALUE_TO_USE = 1, COMMENTS = 'Candidate has 1-2 years of relevant work experience and works on given tasks on day to day basis. Exposure to job complexities is limited and needs support/guidance for complex tasks.' where value='Beginner';
UPDATE MASTER_DATA
SET VALUE_TO_USE = 2, COMMENTS = 'Candidate can independently handle all tasks. Typically has 2 - 5 years of relevant work experience. Dependable on senior for assigned work. Can participate in training/grooming of juniors' where value = 'Competent';
UPDATE MASTER_DATA
SET VALUE_TO_USE = 3, COMMENTS = 'Considered as a Master in the organization/industry. Candidate can handle highly complex scenarios and is the go-to person for others. Such candidates are rare to find and often come at a high cost. Select this option if you want to hire a expert.' where value = 'Expert';

--For ticket #161
update master_data set value='0 - 2 yrs' where value='0 - 3 yrs';
update master_data set value='2 - 4 yrs' where value='4 - 7 yrs';
update master_data set value='4 - 6 yrs' where value='8 - 12 yrs';
update master_data set value='6 - 8 yrs' where value='13 - 15 yrs';
update master_data set value='8 - 10 yrs' where value='17 - 20 yrs';

INSERT INTO MASTER_DATA (TYPE, VALUE)
VALUES( 'experienceRange', '10 - 15 yrs'),
 ( 'experienceRange', '16 - 20 yrs');

ALTER TABLE JOB
ADD COLUMN NOTICE_PERIOD INTEGER REFERENCES MASTER_DATA(ID);

ALTER TABLE JOB
ALTER COLUMN min_salary SET DEFAULT 0,
ALTER COLUMN max_salary SET DEFAULT 0;

--For ticket #175
update master_data set value='0 - 2 Years' where value='0 - 2 yrs';
update master_data set value='2 - 4 Years' where value='2 - 4 yrs';
update master_data set value='4 - 6 Years' where value='4 - 6 yrs';
update master_data set value='6 - 8 Years' where value='6 - 8 yrs';
update master_data set value='8 - 10 Years' where value='8 - 10 yrs';
update master_data set value='10 - 15 Years' where value='10 - 15 yrs';
update master_data set value='15 - 20 Years' where value='16 - 20 yrs';
update master_data set value='20+ Years' where value='20+ yrs';

ALTER TABLE JOB
ADD COLUMN EXPERIENCE_RANGE INTEGER REFERENCES MASTER_DATA(ID);

update Job j set experience_range =
(select id from master_data where value = (select concat((SELECT concat_ws(' - ',replace(cast(min_experience As VARCHAR), '.00',''), replace(cast(max_experience As VARCHAR), '.00',''))), ' Years') from job
where id=j.id and min_experience is not null and max_experience is not null));

ALTER TABLE JOB
DROP COLUMN MIN_EXPERIENCE,
DROP COLUMN MAX_EXPERIENCE;

--Update education values shown in the drop down in master data #178
UPDATE master_data set value = 'ACCA (ACCA)' where value = 'ACCA';
UPDATE master_data set value = 'B.S.S.E (BSSE)' where value = 'BSSE';
UPDATE master_data set value = 'Bachelor in Fine Arts (BFA)' where value = 'BFA';
UPDATE master_data set value = 'Bachelor in Foreign Trade (BFT)' where value = 'BFT';
UPDATE master_data set value = 'Bachelor in Management Studies (BMS)' where value = 'BMS';
UPDATE master_data set value = 'Bachelor of Architecture (BArch)' where value = 'BArch';
UPDATE master_data set value = 'Bachelor of Arts (BA)' where value = 'BA';
UPDATE master_data set value = 'Bachelor of Business Administration (BBA)' where value = 'BBA';
UPDATE master_data set value = 'Bachelor of Commerce (BCom)' where value = 'BCom';
UPDATE master_data set value = 'Bachelor of Commerce in Computer Application (BCCA)' where value = 'BCCA';
UPDATE master_data set value = 'Bachelor of Computer Applications (BCA)' where value = 'BCA';
UPDATE master_data set value = 'Bachelor of Computer Science (BCS)' where value = 'BCS';
UPDATE master_data set value = 'Bachelor of Dental Science (BDS)' where value = 'BDS';
UPDATE master_data set value = 'Bachelor of Design (BDes)' where value = 'BDes';
UPDATE master_data set value = 'Bachelor of Education (BEd)' where value = 'BEd';
UPDATE master_data set value = 'Bachelor of Engineering (BE)' where value = 'BE';
UPDATE master_data set value = 'Bachelor of Hotel Management (BHM)' where value = 'BHM';
UPDATE master_data set value = 'Bachelor of Information Technology (BIT)' where value = 'BIT';
UPDATE master_data set value = 'Bachelor of Pharmacy (BPharma)' where value = 'BPharma';
UPDATE master_data set value = 'Bachelor of Science (BSc)' where value = 'BSc';
UPDATE master_data set value = 'Bachelor of Technology. (BTech)' where value = 'BTech';
UPDATE master_data set value = 'Bachelor of Veterinary Science (BVSc)' where value = 'BVSc';
UPDATE master_data set value = 'Bachelors of Ayurveda where value =  Medicine & Surgery (BAMS)' where value = 'BAMS';
UPDATE master_data set value = 'Bachelors of Business Studies (BBS)' where value = 'BBS';
UPDATE master_data set value = 'Bachelors of Law (LLB)' where value = 'LLB';
UPDATE master_data set value = 'BBM (BBM)' where value = 'BBM';
UPDATE master_data set value = 'BHMS (BHMS)' where value = 'BHMS';
UPDATE master_data set value = 'BMM (BMM)' where value = 'BMM';
UPDATE master_data set value = 'Business Capacity Management (BCM)' where value = 'BCM';
UPDATE master_data set value = 'CA IPCC (CA IPCC)' where value = 'CA IPCC';
UPDATE master_data set value = 'CFA (CFA)' where value = 'CFA';
UPDATE master_data set value = 'Chartered Accountant (CA)' where value = 'CA';
UPDATE master_data set value = 'Company Secretary (CS)' where value = 'CS';
UPDATE master_data set value = 'CWA (CWA)' where value = 'CWA';
UPDATE master_data set value = 'Diploma (Diploma)' where value = 'Diploma';
UPDATE master_data set value = 'Diploma in Graphics & Animation (Diploma in Graphics & Animation)' where value = 'Diploma in Graphics & Animation';
UPDATE master_data set value = 'Doctor Of Philosophy (PHD)' where value = 'PHD';
UPDATE master_data set value = 'Executive Post Graduate Diploma in Business Management (EMBA)' where value = 'EMBA';
UPDATE master_data set value = 'Fashion/Designing (Fashion/Designing)' where value = 'Fashion/Designing';
UPDATE master_data set value = 'FCA (FCA)' where value = 'FCA';
UPDATE master_data set value = 'GD Art Commercial (Commercial Art)' where value = 'Commercial Art';
UPDATE master_data set value = 'Graduate Diploma in Business Administration (GDBA)' where value = 'GDBA';
UPDATE master_data set value = 'HSC (HSC)' where value = 'HSC';
UPDATE master_data set value = 'ICAI  CMA (ICAI/CMA)' where value = 'ICAI/CMA';
UPDATE master_data set value = 'ICWA (ICWA)' where value = 'ICWA';
UPDATE master_data set value = 'Integrated PG Course (I PG Course)' where value = 'I PG Course';
UPDATE master_data set value = 'Journalism/Mass Comunication (Journalism/Mass Comm.)' where value = 'Journalism/Mass Comm';
UPDATE master_data set value = 'M.E (ME)' where value = 'ME';
UPDATE master_data set value = 'M.phil (MPhil)' where value = 'MPhil';
UPDATE master_data set value = 'Management Development Programmes (MDP)' where value = 'MDP';
UPDATE master_data set value = 'Master of Architecture (MArch)' where value = 'MArch';
UPDATE master_data set value = 'Master of Arts (MA)' where value = 'MA';
UPDATE master_data set value = 'Master of Business Administration (MBA)' where value = 'MBA';
UPDATE master_data set value = 'Master of Business Management (MBM)' where value = 'MBM';
UPDATE master_data set value = 'Master of Commerce (MCom)' where value = 'MCom';
UPDATE master_data set value = 'Master of Computer Applications (MCA)' where value = 'MCA';
UPDATE master_data set value = 'Master of Computer Management (MCM)' where value = 'MCM';
UPDATE master_data set value = 'Master of Computer Science (MS CS)' where value = 'MS CS';
UPDATE master_data set value = 'Master of Education (MEd)' where value = 'MEd';
UPDATE master_data set value = 'Master of Financial Management (MFM)' where value = 'MFM';
UPDATE master_data set value = 'Master of Law (LLM)' where value = 'LLM';
UPDATE master_data set value = 'Master of Personnel Management (MPM)' where value = 'MPM';
UPDATE master_data set value = 'Master of Pharmacy (MPharma)' where value = 'MPharma';
UPDATE master_data set value = 'Master of Science (MSc)' where value = 'MSc';
UPDATE master_data set value = 'Master of Social Work (MSW)' where value = 'MSW';
UPDATE master_data set value = 'Master of Technology (MTech)' where value = 'MTech';
UPDATE master_data set value = 'Master of Veterinary Science (MVSc)' where value = 'MVSc';
UPDATE master_data set value = 'Master''s in Diploma in Business Administration (MDBA)' where value = 'MDBA';
UPDATE master_data set value = 'Masters in Fine Arts (MFA)' where value = 'MFA';
UPDATE master_data set value = 'Masters in Industrial Psychology (MS in Industrial Psychology)' where value = 'MS in Industrial Psychology';
UPDATE master_data set value = 'Masters in Information Management (MIM)' where value = 'MIM';
UPDATE master_data set value = 'Masters in Management Studies (MMS)' where value = 'MMS';
UPDATE master_data set value = 'Masters of finance and control (MFC)' where value = 'MFC';
UPDATE master_data set value = 'MBA/PGDM (MBA/PGDM)' where value = 'MBA/PGDM';
UPDATE master_data set value = 'MBBS (MBBS)' where value = 'MBBS';
UPDATE master_data set value = 'Medical (MS/MD)' where value = 'MS/MD';
UPDATE master_data set value = 'MEP (MEP)' where value = 'MEP';
UPDATE master_data set value = 'MS (MS)' where value = 'MS';
UPDATE master_data set value = 'Other (Other)' where value = 'Other';
UPDATE master_data set value = 'PG Diploma (PG Diploma)' where value = 'PG Diploma';
UPDATE master_data set value = 'PGDBA (PGDBA)' where value = 'PGDBA';
UPDATE master_data set value = 'Post Graduate Certification in Business Management (PGCBM)' where value = 'PGCBM';
UPDATE master_data set value = 'Post Graduate Diploma in Analytical Chemistry (PGDAC)' where value = 'PGDAC';
UPDATE master_data set value = 'Post Graduate Diploma in Computer Application (PGDCA)' where value = 'PGDCA';
UPDATE master_data set value = 'Post Graduate Program (PGP)' where value = 'PGP';
UPDATE master_data set value = 'Post Graduate Programme in Business Management ... (PGPBM)' where value = 'PGPBM';
UPDATE master_data set value = 'Post Graduate Programme in Management (PGPBM)' where value = 'PGPBM';
UPDATE master_data set value = 'Post Graduate Programme in Management (PGM)' where value = 'PGM';
UPDATE master_data set value = 'Postgraduate Certificate in Human Resource Management (PGCHRM)' where value = 'PGCHRM';
UPDATE master_data set value = 'PR/Advertising (PR/Advertising)' where value = 'PR/Advertising';
UPDATE master_data set value = 'Tourism (Tourism)' where value = 'Tourism';
UPDATE master_data set value = 'Vocational-Training (Vocational Training)' where value = 'Vocational Training';
INSERT into master_data(type, value) values ('education','Masters in Information Management (MIM)');

UPDATE MASTER_DATA SET VALUE = '0 Days' WHERE VALUE = '0';
UPDATE MASTER_DATA SET VALUE = '15 Days' WHERE VALUE = '15';
UPDATE MASTER_DATA SET VALUE = '30 Days' WHERE VALUE = '30';
UPDATE MASTER_DATA SET VALUE = '60 Days' WHERE VALUE = '60';
UPDATE MASTER_DATA SET VALUE = '45 Days' WHERE VALUE = '45';
UPDATE MASTER_DATA SET VALUE = '90 Days' WHERE VALUE = '90';

 INSERT INTO MASTER_DATA (TYPE, VALUE)
VALUES
('function','Accounting / Tax / Company Secretary / Audit'),
('function','Agent'),
('function','Airline / Reservations / Ticketing / Travel'),
('function','Analytics & Business Intelligence'),
('function','Anchoring / TV / Films / Production'),
('function','Architects / Interior Design / Naval Arch'),

('function','Art Director / Graphic / Web Designer'),
('function','Banking / Insurance'),
('function','Content / Editors / Journalists'),
('function','Corporate Planning / Consulting / Strategy'),
('function','Entrepreneur / Businessman / Outside Management Consultant'),
('function','Export / Import'),
('function','Fashion'),
('function', 'Front Office Staff / Secretarial / Computer Operator'),
('function','Hotels / Restaurant Management'),
('function', 'HR / Admin / PM / IR / Training'),
('function', 'ITES / BPO / Operations / Customer Service / Telecalling'),
('function','Legal / Law'),
('function','Medical Professional / Healthcare Practitioner / Technician'),
('function','Mktg / Advtg / MR / Media Planning / PR / Corp. Comm'),
('function','Packaging Development'),
('function','Production / Service Engineering / Manufacturing / Maintenance'),
('function','Project Management / Site Engineers'),
('function','Purchase / SCM'),
('function','R&D / Engineering Design'),
('function','Sales / Business Development / Client Servicing'),
('function','Security'),
('function','Shipping'),
('function','Software Development -'),
('function','Software Development - Application Programming'),
('function','Software Development - Client Server'),
('function','Software Development - Database Administration'),
('function','Software Development - e-commerce / Internet Technologies'),
('function','Software Development - Embedded Technologies'),
('function','Software Development - ERP / CRM'),
('function','Software Development - Network Administration'),
('function','Software Development - Others'),
('function','Software Development - QA and Testing'),
('function','Software Development - System Programming'),
('function','Software Development - Telecom Software'),
('function','Software Development - Systems / EDP / MIS'),
('function','Teaching / Education / Language Specialist'),
('function', 'Telecom / IT-Hardware / Tech. Staff / Support'),
('function','Top Management'),
('function','Any Other');

UPDATE JOB SET FUNCTION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'ITES / BPO / Operations / Customer Service / Telecalling') WHERE FUNCTION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'BPO');
UPDATE JOB SET FUNCTION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'HR / Admin / PM / IR / Training') WHERE FUNCTION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Human Resources (HR)');
UPDATE JOB SET FUNCTION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Software Development -') WHERE FUNCTION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Information Technology (IT)');
UPDATE JOB SET FUNCTION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'HR / Admin / PM / IR / Training') WHERE FUNCTION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Office Administration');
UPDATE JOB SET FUNCTION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Sales / Business Development / Client Servicing') WHERE FUNCTION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Sales');
UPDATE JOB SET FUNCTION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Production / Service Engineering / Manufacturing / Maintenance') WHERE FUNCTION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Manufacturing');

DELETE FROM MASTER_DATA WHERE VALUE = 'BPO';
DELETE FROM MASTER_DATA WHERE VALUE = 'Human Resources (HR)';
DELETE FROM MASTER_DATA WHERE VALUE = 'Information Technology (IT)';
DELETE FROM MASTER_DATA WHERE VALUE = 'Office Administration';
DELETE FROM MASTER_DATA WHERE VALUE = 'Sales';
DELETE FROM MASTER_DATA WHERE VALUE = 'Manufacturing';

--drop unique constraints of master_data for type and value
UPDATE MASTER_DATA SET VALUE = '10 - 15 Years' WHERE VALUE = '20+ Years';
UPDATE MASTER_DATA SET VALUE = '15 - 20 Years' WHERE ID = (SELECT MAX(ID) FROM MASTER_DATA WHERE VALUE = '10 - 15 Years');
UPDATE MASTER_DATA SET VALUE = '20+ Years' WHERE ID = (SELECT MAX(ID) FROM MASTER_DATA WHERE VALUE = '15 - 20 Years');
UPDATE MASTER_DATA SET VALUE = '45 Days' WHERE VALUE = '60 Days';
UPDATE MASTER_DATA SET VALUE = '60 Days' WHERE ID = (SELECT MAX(ID) FROM MASTER_DATA WHERE VALUE = '45 Days')
--add again unique constraints of master_data for type and value


-- For ticket #182
DELETE FROM JOB_CAPABILITY_STAR_RATING_MAPPING;

ALTER TABLE JOB_CAPABILITY_STAR_RATING_MAPPING
ADD COLUMN JOB_ID INTEGER REFERENCES JOB(ID) NOT NULL;

-- For ticket #173
ALTER TABLE CV_PARSING_DETAILS
ADD COLUMN CANDIDATE_ID INTEGER,
ADD COLUMN RCHILLI_JSON_PROCESSED BOOL;
-- For ticket #165
alter table company_bu
drop column updated_on, drop column updated_by;

alter table company_address
add column address_title varchar(100) not null unique default 'Default Address';


--For ticket #166
ALTER TABLE JOB DROP COLUMN CURRENCY;
ALTER TABLE JOB ADD COLUMN CURRENCY VARCHAR(3) NOT NULL DEFAULT 'INR';

ALTER TABLE JOB
ADD COLUMN HIRING_MANAGER INTEGER REFERENCES USERS(ID),
ADD COLUMN RECRUITER INTEGER REFERENCES USERS(ID);

-- #180
CREATE INDEX idx_jcm_stage ON job_candidate_mapping(stage);
CREATE INDEX idx_jcm_jobid ON job_candidate_mapping(job_id);
CREATE INDEX idx_job_createdby ON job(created_by);
CREATE INDEX idx_job_datearchived ON job(date_archived);


-- For ticket #147
CREATE TABLE CREATE_JOB_PAGE_SEQUENCE(
	ID serial PRIMARY KEY NOT NULL,
	PAGE_NAME VARCHAR(25) NOT NULL,
	PAGE_DISPLAY_NAME VARCHAR(25) NOT NULL,
	PAGE_DISPLAY_ORDER SMALLINT NOT NULL,
	DISPLAY_FLAG BOOL NOT NULL DEFAULT 'T',
	SUBSCRIPTION_AVAILABILITY VARCHAR(5) NOT NULL DEFAULT 'LITE'
);

INSERT INTO CREATE_JOB_PAGE_SEQUENCE (PAGE_DISPLAY_NAME, PAGE_NAME, PAGE_DISPLAY_ORDER, DISPLAY_FLAG,SUBSCRIPTION_AVAILABILITY)
VALUES
('Overview', 'overview', 1, 'T','Lite'),
('Expertise', 'expertise', 2, 'F','Lite'),
('Job Detail', 'jobDetail', 3, 'F','Max'),
('Screening Questions', 'screeningQuestions', 4, 'T','Lite'),
('Key Skills', 'keySkills', 5, 'T','Lite'),
('Hiring Team', 'hiringTeam', 6, 'F','Max'),
('Capabilities', 'capabilities', 7, 'T','Lite'),
('Preview', 'preview', 8, 'T','Lite');

--For ticket #183
INSERT INTO CONFIGURATION_SETTINGS(CONFIG_NAME, CONFIG_VALUE)
VALUES('maxCapabilities',10);

-- For ticket #184
ALTER TABLE CV_PARSING_DETAILS
ADD COLUMN JOB_CANDIDATE_MAPPING_ID INTEGER REFERENCES JOB_CANDIDATE_MAPPING(ID),
ADD COLUMN CV_RATING_API_FLAG BOOL DEFAULT 'F' NOT NULL,
ADD COLUMN CV_RATING_API_RESPONSE_TIME smallint;

CREATE TABLE CV_RATING (
ID serial PRIMARY KEY NOT NULL,
JOB_CANDIDATE_MAPPING_ID integer REFERENCES JOB_CANDIDATE_MAPPING(ID) NOT NULL,
OVERALL_RATING smallint NOT NULL
);

CREATE TABLE CV_RATING_SKILL_KEYWORD_DETAILS (
ID serial PRIMARY KEY NOT NULL,
CV_RATING_ID integer REFERENCES CV_RATING(ID) NOT NULL,
SUPPORTING_KEYWORDS text NOT NULL,
SKILL_NAME varchar(100) NOT NULL,
RATING smallint NOT NULL,
OCCURRENCE smallint NOT NULL
);

INSERT INTO CONFIGURATION_SETTINGS (CONFIG_NAME, CONFIG_VALUE)
VALUES ('cvRatingTimeout', 30000);

--For ticket #204
ALTER TABLE CV_PARSING_DETAILS
ADD COLUMN PARSED_TEXT JSON,
ADD COLUMN JOB_ID INTEGER,
ADD COLUMN EMAIL varchar(50);
--update parsing_response_json copy into parsedText column
update cv_parsing_details cpd set parsed_text=(select CAST(parsing_response_json as json) from cv_parsing_details
where id=cpd.id and parsing_response_json is not null);
--Update email
update cv_parsing_details cpd set email=(select parsed_text ->> 'Email' as email from cv_parsing_details where id=cpd.id and parsed_text is not null);
--update jobId
update cv_parsing_details cpd set job_id = (select CAST(SPLIT_PART(cv_file_name, '_', 2) as Integer) from cv_parsing_details where id=cpd.id);
--update jcmId
update cv_parsing_details set job_candidate_mapping_id = jcm.id from job_candidate_mapping jcm where cv_parsing_details.job_id = jcm.job_id
and cv_parsing_details.email = jcm.email and cv_parsing_details.job_candidate_mapping_id is null and cv_parsing_details.email is not null;
--Drop supportive columns from cv_parsing_details table
ALTER TABLE CV_PARSING_DETAILS
DROP COLUMN PARSED_TEXT,
DROP COLUMN JOB_ID,
DROP COLUMN EMAIL;

--For ticket #185

INSERT INTO MASTER_DATA (TYPE, VALUE)
VALUES
('education', 'Association of Chartered Certified Accountants (ACCA)'),
('education', 'Bachelor of Arts (BA)'),
('education', 'Bachelors of Ayurveda Medicine & Surgery (BAMS)'),
('education', 'Bachelor of Architecture (BArch)'),
('education', 'Bachelor of Business Administration (BBA)'),
('education', 'Bachelor of Business Management (BBM)'),
('education', 'Bachelors of Business Studies (BBS)'),
('education', 'Bachelor of Computer Applications (BCA)'),
('education', 'Bachelor of Commerce in Computer Application (BCCA)'),
('education', 'Business Capacity Management (BCM)'),
('education', 'Bachelor of Commerce (BCom)'),
('education', 'Bachelor of Computer Science (BCS)'),
('education', 'Bachelor of Design (BDes)'),
('education', 'Bachelor of Dental Science (BDS)'),
('education', 'Bachelor of Engineering (BE)'),
('education', 'Bachelor of Education (BEd)'),
('education', 'Bachelor in Fine Arts (BFA)'),
('education', 'Bachelor in Foreign Trade (BFT)'),
('education', 'Bachelor of Hotel Management (BHM)'),
('education', 'Bachelor of Homeopathic Medicine and Surgery (BHMS)'),
('education', 'Bachelor of Information Technology (BIT)'),
('education', 'Bachelor of Marketing Management (BMM)'),
('education', 'Bachelor in Management Studies (BMS)'),
('education', 'Bachelor of Pharmacy (BPharma)'),
('education', 'Bachelor of Law (LLB)'),
('education', 'Bachelor in Medicine and Bachelor of Surgery (MBBS)'),
('education', 'Bachelor of Science (BSc)'),
('education', 'Bachelor of  Science in Software Engineering (BSSE)'),
('education', 'Bachelor of Technology. (BTech)'),
('education', 'Bachelor of Veterinary Science (BVSc)'),
('education', 'Chartered Accountant (CA)'),
('education', 'Chartered Accountant Integrated Professional Competence Course (CA IPCC)'),
('education', 'Chartered Financial Accountant  (CFA)'),
('education', 'Commercial Art(Commercial Art)'),
('education', 'Company Secretary (CS)'),
('education', 'Cost and Works Accountancy (CWA)'),
('education', 'Diploma (Diploma)'),
('education', 'Diploma in Graphics & Animation (Diploma in Graphics & Animation)'),
('education', 'Doctor Of Philosophy (PhD)' ),
('education', 'Executive Post Graduate Diploma in Business Management (EMBA)'),
('education', 'Fashion/Designing (Fashion/Designing)'),
('education', 'Fellow of Chartered Accountants (FCA)'),
('education', 'Graduate Diploma in Arts (GD Arts)'),
('education', 'Graduate Diploma in Business Administration (GDBA)'),
('education', 'Higher Secondary School Certificate (HSC)'),
('education', 'Integrated PG Course (I PG Course)'),
('education', 'Institute of Cost Accountants of India - Cost and Management Accountant (ICAI CMA)'),
('education', 'Institute of Cost and Works Accountant of India (ICWA)'),
('education', 'Journalism/Mass Communication (Journalism/Mass Comm.)'),
('education', 'Management Development Programmes (MDP)'),
('education', 'Masters in Information Management (MIM)'),
('education', 'Masters in Diploma in Business Administration (MDBA)'),
('education', 'Master of Law (LLM)'),
('education', 'Master of Arts (MA)'),
('education', 'Master of Architecture (MArch)'),
('education', 'Master of Business Administration (MBA)'),
('education', 'Master of Business Management (MBM)'),
('education', 'Master of Computer Applications (MCA)'),
('education', 'Master of Computer Management (MCM)'),
('education', 'Master of Commerce (MCom)'),
('education', 'Masters of Engineering (ME)'),
('education', 'Master of Education (MEd)'),
('education', 'Masters in Fine Arts (MFA)'),
('education', 'Masters of finance and control (MFC)'),
('education', 'Master of Financial Management (MFM)'),
('education', 'Masters in Management Studies (MMS)'),
('education', 'Master of Pharmacy (MPharma)'),
('education', 'Masters of Philosophy (MPhil)'),
('education', 'Master of Personnel Management (MPM)'),
('education', 'Masters of Science (MS)'),
('education', 'Master of Science in Computer Science (MS CS)'),
('education', 'Master of Computer Science (MCS)'),
('education', 'Masters in Industrial Psychology (MS in Industrial Psychology)'),
('education', 'Masters of Surgery / Doctor of Medicine (MS/MD)'),
('education', 'Master of Science (MSc)'),
('education', 'Master of Social Work (MSW)'),
('education', 'Master of Technology (MTech)'),
('education', 'Master of Veterinary Science (MVSc)'),
('education', 'Mechanical, Electrical and Plumbing (MEP)'),
('education', 'Post Graduate Diploma (PG Diploma)'),
('education', 'Post Graduate Certification in Business Management (PGCBM)'),
('education', 'Post Graduate Certificate in Human Resource Management (PGCHRM)'),
('education', 'Post Graduate Diploma in Analytical Chemistry (PGDAC)'),
('education', 'Post Graduate Diploma in Business Administration (PGDBA)'),
('education', 'Post Graduate Diploma in Computer Application (PGDCA)'),
('education', 'Post Graduate Degree in Management (PGDM)'),
('education', 'Post Graduate Programme in Management (PGM)'),
('education', 'Post Graduate Program (PGP)'),
('education', 'Post Graduate Programme in Business Management (PGPBM)'),
('education', 'Public Relations / Advertising (PR/Advertising)'),
('education', 'Tourism (Tourism)'),
('education', 'Vocational-Training (Vocational Training)'),
('education', 'Other (Other)');

UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Bachelor of Arts (BA)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'BA');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Company Secretary (CS)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'CS');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Bachelors of Ayurveda Medicine & Surgery (BAMS)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'BAMS');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Bachelor of Architecture (BArch)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'BArch');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Bachelor of Business Administration (BBA)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'BBA');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Bachelor of Business Management (BBM)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'BBM');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Bachelors of Business Studies (BBS)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'BBS');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Bachelor of Computer Applications (BCA)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'BCA');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Bachelor of Commerce in Computer Application (BCCA)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'BCCA');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Business Capacity Management (BCM)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'BCM');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Bachelor of Commerce (BCom)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'BCom');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Bachelor of Computer Science (BCS)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'BCS');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Bachelor of Design (BDes)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'BDes');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Bachelor of Dental Science (BDS)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'BDS');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Bachelor of Engineering (BE)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'BE');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Bachelor of Education (BEd)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'BEd');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Bachelor in Fine Arts (BFA)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'BFA');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Bachelor in Foreign Trade (BFT)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'BFT');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Bachelor of Hotel Management (BHM)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'BHM');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Bachelor of Homeopathic Medicine and Surgery (BHMS)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'BHMS');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Bachelor of Information Technology (BIT)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'BIT');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Bachelor of Marketing Management (BMM)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'BMM');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Bachelor in Management Studies (BMS)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'BMS');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Bachelor of Pharmacy (BPharma)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'BPharma');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Bachelor of Science (BSc)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'BSc');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Bachelor of  Science in Software Engineering (BSSE)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'BSSE');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Bachelor of Technology. (BTech)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'BTech');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Bachelor of Veterinary Science (BVSc)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'BVSc');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Commercial Art(Commercial Art)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Commercial Art');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Diploma (Diploma)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Diploma');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Diploma in Graphics & Animation (Diploma in Graphics & Animation)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Diploma in Graphics & Animation');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Fashion/Designing (Fashion/Designing)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Fashion/Designing');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Graduate Diploma in Business Administration (GDBA)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'GDBA');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Higher Secondary School Certificate (HSC)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'HSC');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Bachelor of Law (LLB)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'LLB');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Bachelor in Medicine and Bachelor of Surgery (MBBS)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'MBBS');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Vocational-Training (Vocational Training)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Vocational Training');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Association of Chartered Certified Accountants (ACCA)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'ACCA');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Chartered Accountant (CA)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'CA');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Chartered Accountant Integrated Professional Competence Course (CA IPCC)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'CA IPCC');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Chartered Financial Accountant  (CFA)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'CFA');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Cost and Works Accountancy (CWA)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'CWA');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Executive Post Graduate Diploma in Business Management (EMBA)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'EMBA');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Fellow of Chartered Accountants (FCA)'') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'FCA');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Integrated PG Course (I PG Course)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'I PG Course');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Institute of Cost Accountants of India - Cost and Management Accountant (ICAI CMA)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'ICAI/CMA');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Institute of Cost and Works Accountant of India (ICWA)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'ICWA');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Journalism/Mass Communication (Journalism/Mass Comm.)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Journalism/Mass Comm');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Master of Law (LLM)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'LLM');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Master of Arts (MA)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'MA');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Master of Architecture (MArch)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'MArch');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Master of Business Administration (MBA)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'MBA');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Post Graduate Degree in Management (PGDM)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'MBA/PGDM');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Master of Business Management (MBM)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'MBM');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Master of Computer Applications (MCA)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'MCA');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Master of Computer Management (MCM)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'MCM');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Master of Commerce (MCom)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'MCom');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Master's in Diploma in Business Administration (MDBA)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'MDBA');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Management Development Programmes (MDP)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'MDP');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Masters of Engineering (ME)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'ME');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Master of Education (MEd)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'MEd');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Mechanical, Electrical and Plumbing (MEP)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'MEP');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Masters in Fine Arts (MFA)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'MFA');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Masters of finance and control (MFC)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'MFC');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Master of Financial Management (MFM)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'MFM');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Masters in Management Studies (MMS)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'MMS');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Master of Pharmacy (MPharma)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'MPharma');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Masters of Philosophy (MPhil)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'MPhil');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Master of Personnel Management (MPM)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'MPM');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Masters of Science (MS)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'MS');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Master of Science in Computer Science (MS CS)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'MS CS');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Masters in Industrial Psychology (MS in Industrial Psychology)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'MS in Industrial Psychology');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Masters of Surgery / Doctor of Medicine (MS/MD)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'MS/MD');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Master of Science (MSc)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'MSc');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Master of Social Work (MSW)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'MSW');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Master of Technology (MTech)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'MTech');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Master of Veterinary Science (MVSc)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'MVSc');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Other (Other)' and type='education') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Other' and type='education');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Post Graduate Diploma (PG Diploma)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'PG Diploma');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Post Graduate Certification in Business Management (PGCBM)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'PGCBM');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Post Graduate Certificate in Human Resource Management (PGCHRM)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'PGCHRM');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Post Graduate Diploma in Analytical Chemistry (PGDAC)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'PGDAC');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Post Graduate Diploma in Business Administration (PGDBA)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'PGDBA');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Post Graduate Diploma in Computer Application (PGDCA)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'PGDCA');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Post Graduate Programme in Management (PGM)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'PGM');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Post Graduate Program (PGP)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'PGP');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Post Graduate Programme in Business Management (PGPBM)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'PGPBM');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Doctor Of Philosophy (PhD)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'PHD');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Public Relations / Advertising (PR/Advertising)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'PR/Advertising');
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Tourism (Tourism)') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Tourism');

--For ticket  #220
CREATE TABLE CURRENCY (
ID serial PRIMARY KEY NOT NULL,
CURRENCY_FULL_NAME varchar(25),
CURRENCY_SHORT_NAME varchar(5),
COUNTRY varchar(5)
);

INSERT INTO public.currency(currency_full_name, currency_short_name, country) VALUES
('Australian Dollar', 'AUD', 'au'),
('Canadian Dollar', 'CAD', 'ca'),
('Indian Rupee', 'INR', 'in'),
('Singapore Dollar', 'SGD', 'sg'),
('Pound Sterling', 'GBP', 'gb'),
('US Dollar', 'USD', 'us');

--For ticket #227
UPDATE CONFIGURATION_SETTINGS
SET CONFIG_VALUE = 5000
WHERE CONFIG_NAME = 'cvRatingTimeout';

--Add unique constraint for jcm id in cv_rating table
ALTER TABLE CV_RATING
ADD CONSTRAINT UNIQUE_CV_RATING_JCM UNIQUE(JOB_CANDIDATE_MAPPING_ID);

--Delete duplicate records from cvRatings
delete from cv_rating_skill_keyword_details
where cv_rating_skill_keyword_details.cv_rating_id in (
select a.id from cv_rating a, cv_rating b
where a.id < b.id and a.job_candidate_mapping_id = b.job_candidate_mapping_id);

DELETE FROM cv_rating a USING cv_rating b
WHERE a.id < b.id AND a.job_candidate_mapping_id = b.job_candidate_mapping_id;

--For ticket #236
ALTER TABLE CANDIDATE_EDUCATION_DETAILS
ALTER COLUMN DEGREE TYPE VARCHAR(100);

--For update cv_rating_api_flag in cvParsingDetails duplicate jobCandidateMapping id
update cv_parsing_details set cv_rating_api_flag = true where job_candidate_mapping_id in(
select a.job_candidate_mapping_id from cv_parsing_details a, cv_parsing_details b
where a.id < b.id and a.job_candidate_mapping_id = b.job_candidate_mapping_id);

--For ticket #234
ALTER TABLE USERS
ADD COLUMN USER_TYPE varchar(15) default 'Recruiting';

--For ticket #232
ALTER TABLE COMPANY
ADD COLUMN COMPANY_TYPE VARCHAR(15) DEFAULT 'Individual' NOT NULL,
ADD COLUMN RECRUITMENT_AGENCY_ID INTEGER REFERENCES COMPANY(ID);

--Add Unique constraint in Jcm
ALTER TABLE JOB_CANDIDATE_MAPPING
ADD CONSTRAINT unique_job_candidate UNIQUE(JOB_ID, CANDIDATE_ID);

--https://github.com/hexagonsearch/litmusblox-scheduler/issues/16
--Clear all timestamps in the jcm_communication_details table if the chat_invite_flag is false
update jcm_communication_details set chat_invite_timestamp_sms = null, chat_incomplete_reminder_1_timestamp_sms = null, chat_incomplete_reminder_2_timestamp_sms = null, link_not_visited_reminder_1_timestamp_sms = null, link_not_visited_reminder_2_timestamp_sms = null, chat_complete_timestamp_sms = null, chat_invite_timestamp_email = null, chat_incomplete_reminder_1_timestamp_email = null, chat_incomplete_reminder_2_timestamp_email = null, link_not_visited_reminder_1_timestamp_email = null, link_not_visited_reminder_2_timestamp_email = null, chat_complete_timestamp_email = null where chat_invite_flag = false;