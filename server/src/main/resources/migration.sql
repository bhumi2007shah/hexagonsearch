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

--For ticket #230
ALTER TABLE USERS
    ALTER COLUMN ROLE TYPE VARCHAR(17);

--For ticket #246
ALTER TABLE USERS
ADD COLUMN COMPANY_ADDRESS_ID INTEGER REFERENCES COMPANY_ADDRESS(ID),
ADD COLUMN COMPANY_BU_ID INTEGER REFERENCES COMPANY_BU(ID);

--https://github.com/hexagonsearch/litmusblox-scheduler/issues/16
--Clear all timestamps in the jcm_communication_details table if the chat_invite_flag is false
update jcm_communication_details set chat_invite_timestamp_sms = null, chat_incomplete_reminder_1_timestamp_sms = null, chat_incomplete_reminder_2_timestamp_sms = null, link_not_visited_reminder_1_timestamp_sms = null, link_not_visited_reminder_2_timestamp_sms = null, chat_complete_timestamp_sms = null, chat_invite_timestamp_email = null, chat_incomplete_reminder_1_timestamp_email = null, chat_incomplete_reminder_2_timestamp_email = null, link_not_visited_reminder_1_timestamp_email = null, link_not_visited_reminder_2_timestamp_email = null, chat_complete_timestamp_email = null where chat_invite_flag = false;

-- For ticket #241 - update all candidate source to naukri where candidate source is plugin
update job_candidate_mapping set candidate_source= 'Naukri' where candidate_source='Plugin';



--For ticket #224
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

INSERT INTO STAGE_MASTER(ID, STAGE_NAME) VALUES
(1, 'Source'),
(2, 'Screen'),
(3, 'Resume Submit'),
(4, 'Interview'),
(5, 'Make Offer'),
(6, 'Offer'),
(7, 'Join');

INSERT INTO STEPS_PER_STAGE (STAGE_ID, STEP_NAME) VALUES
(1, 'Source'),
(2, 'Screen'),
(3, 'Resume Submit'),
(4, 'L1'),
(4, 'L2'),
(4, 'L3'),
(5, 'Make Offer'),
(6, 'Offer'),
(7, 'Join');

ALTER TABLE COMPANY_STAGE_STEP DROP CONSTRAINT company_stage_step_stage_fkey;
ALTER TABLE COMPANY_STAGE_STEP ADD CONSTRAINT company_stage_step_stage_fkey FOREIGN KEY (STAGE) REFERENCES STAGE_MASTER(ID);

-- populate company stage step for all existing companies
INSERT INTO COMPANY_STAGE_STEP (COMPANY_ID, STAGE, STEP, CREATED_ON, CREATED_BY)
 SELECT COMPANY.ID, STAGE_ID, STEP_NAME, COMPANY.CREATED_ON ,COMPANY.CREATED_BY
 FROM COMPANY, STEPS_PER_STAGE ORDER BY COMPANY.ID;

-- create table to store job specific
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

-- populate stage step for all existing jobs which are complete
INSERT INTO JOB_STAGE_STEP(JOB_ID, STAGE_STEP_ID, CREATED_BY, CREATED_ON)
 SELECT JOB.ID, COMPANY_STAGE_STEP.ID, JOB.CREATED_BY, JOB.DATE_PUBLISHED
 FROM JOB, COMPANY_STAGE_STEP
 WHERE JOB.COMPANY_ID = COMPANY_STAGE_STEP.COMPANY_ID
 AND JOB.DATE_PUBLISHED IS NOT NULL
 ORDER BY JOB.ID;

 -- add columns to job_candidate_mapping table to hold job_stage_step_id and flag indicating candidate rejection
ALTER TABLE JOB_CANDIDATE_MAPPING
ADD COLUMN REJECTED BOOL NOT NULL DEFAULT 'f';

ALTER TABLE JOB_CANDIDATE_MAPPING DROP CONSTRAINT job_candidate_mapping_stage_fkey;
ALTER TABLE JOB_CANDIDATE_MAPPING ALTER COLUMN STAGE DROP NOT NULL;
ALTER TABLE JOB_CANDIDATE_MAPPING ADD CONSTRAINT job_candidate_mapping_stage_fkey FOREIGN KEY (STAGE) REFERENCES JOB_STAGE_STEP(ID);

--clear old dirty data of jcm related to 'Draft' jobs
DELETE FROM JCM_COMMUNICATION_DETAILS
WHERE JCM_ID IN (SELECT ID FROM JOB_CANDIDATE_MAPPING WHERE JOB_ID IN (SELECT ID FROM JOB WHERE STATUS = 'Draft'));

DELETE FROM CANDIDATE_SCREENING_QUESTION_RESPONSE
WHERE JOB_CANDIDATE_MAPPING_ID IN (SELECT ID FROM JOB_CANDIDATE_MAPPING WHERE JOB_ID IN (SELECT ID FROM JOB WHERE STATUS = 'Draft'));

DELETE FROM CANDIDATE_TECH_RESPONSE_DATA
WHERE JOB_CANDIDATE_MAPPING_ID IN (SELECT ID FROM JOB_CANDIDATE_MAPPING WHERE JOB_ID IN (SELECT ID FROM JOB WHERE STATUS = 'Draft'));

DELETE FROM JOB_CANDIDATE_MAPPING
WHERE JOB_ID IN (SELECT ID FROM JOB WHERE STATUS = 'Draft');

-- set rejected flag to false for all existing records
UPDATE JOB_CANDIDATE_MAPPING
SET REJECTED = 'F';

-- set the stage to 'Source' for all existing jcm records
UPDATE JOB_CANDIDATE_MAPPING
SET STAGE = (
 SELECT JOB_STAGE_STEP.ID
 FROM JOB_STAGE_STEP
 WHERE JOB_STAGE_STEP.JOB_ID = JOB_CANDIDATE_MAPPING.JOB_ID
 AND STAGE_STEP_ID = (
  SELECT COMPANY_STAGE_STEP.ID FROM COMPANY_STAGE_STEP, JOB
  WHERE COMPANY_STAGE_STEP.STEP = 'Source'
  AND COMPANY_STAGE_STEP.COMPANY_ID = JOB.COMPANY_ID
  AND JOB.ID = JOB_CANDIDATE_MAPPING.JOB_ID
 )
);

ALTER TABLE JOB_CANDIDATE_MAPPING ALTER COLUMN STAGE SET NOT NULL;

-- modify history table
ALTER TABLE JCM_HISTORY
ADD COLUMN STAGE INTEGER REFERENCES JOB_STAGE_STEP(ID);

UPDATE JCM_HISTORY
SET STAGE = (
  SELECT JOB_STAGE_STEP.ID
  FROM JOB_STAGE_STEP, JOB_CANDIDATE_MAPPING
  WHERE JCM_HISTORY.JCM_ID = JOB_CANDIDATE_MAPPING.ID
  AND JOB_STAGE_STEP.JOB_ID = JOB_CANDIDATE_MAPPING.JOB_ID
  AND STAGE_STEP_ID = (
    SELECT COMPANY_STAGE_STEP.ID
    FROM COMPANY_STAGE_STEP, JOB
    WHERE COMPANY_STAGE_STEP.STEP = 'Source'
    AND COMPANY_STAGE_STEP.COMPANY_ID = JOB.COMPANY_ID
    AND JOB.ID = JOB_CANDIDATE_MAPPING.JOB_ID
  )
);

ALTER TABLE JCM_HISTORY ALTER COLUMN STAGE SET NOT NULL;

DELETE FROM MASTER_DATA WHERE TYPE = 'stage';

--For ticket #247
ALTER TABLE JOB_HIRING_TEAM
DROP COLUMN STAGE_STEP_ID;

ALTER TABLE JOB_HIRING_TEAM
ADD COLUMN STAGE_STEP_ID INTEGER REFERENCES JOB_STAGE_STEP(ID) NOT NULL;

--For ticket #257
ALTER TABLE MASTER_DATA
ALTER COLUMN VALUE_TO_USE TYPE VARCHAR (20);

INSERT INTO public.master_data(type, value, value_to_use) VALUES
('role', 'HR Recruiter', 'Recruiter'),
('role', 'HR Head', 'ClientAdmin'),
('role', 'Admin', 'ClientAdmin'),
('role', 'Hiring Manager', 'BusinessUser'),
('role', 'Interviewer', 'BusinessUser');

--For ticket #262
UPDATE CREATE_JOB_PAGE_SEQUENCE SET PAGE_DISPLAY_ORDER = 2, PAGE_DISPLAY_NAME = 'Screening' WHERE PAGE_NAME = 'screeningQuestions';
UPDATE CREATE_JOB_PAGE_SEQUENCE SET PAGE_DISPLAY_ORDER = 3 WHERE PAGE_NAME = 'expertise';
UPDATE CREATE_JOB_PAGE_SEQUENCE SET PAGE_DISPLAY_ORDER = 4 WHERE PAGE_NAME = 'keySkills';
UPDATE CREATE_JOB_PAGE_SEQUENCE SET PAGE_DISPLAY_ORDER = 5 WHERE PAGE_NAME = 'capabilities';
UPDATE CREATE_JOB_PAGE_SEQUENCE SET PAGE_DISPLAY_ORDER = 6, DISPLAY_FLAG = 'true' WHERE PAGE_NAME = 'hiringTeam';
UPDATE CREATE_JOB_PAGE_SEQUENCE SET PAGE_DISPLAY_ORDER = 7 WHERE PAGE_NAME = 'preview';
UPDATE CREATE_JOB_PAGE_SEQUENCE SET PAGE_DISPLAY_ORDER = 8 WHERE PAGE_NAME = 'jobDetail';

--For ticket  #268
ALTER TABLE JOB_CANDIDATE_MAPPING
ADD COLUMN ALTERNATE_EMAIL VARCHAR (50),
ADD COLUMN ALTERNATE_MOBILE VARCHAR (15),
ADD COLUMN SERVING_NOTICE_PERIOD BOOL NOT NULL DEFAULT 'f',
ADD COLUMN NEGOTIABLE_NOTICE_PERIOD BOOL NOT NULL DEFAULT 'f',
ADD COLUMN OTHER_OFFERS BOOL NOT NULL DEFAULT 'f',
ADD COLUMN UPDATE_RESUME BOOL NOT NULL DEFAULT 'f',
ADD COLUMN COMMUNICATION_RATING SMALLINT DEFAULT 0;

ALTER TABLE CANDIDATE_DETAILS
ADD COLUMN RELEVANT_EXPERIENCE NUMERIC (4, 2);


-- FOR TICKET #258
DROP TABLE IF EXISTS EXPORT_FORMAT_DETAIL;
DROP TABLE IF EXISTS EXPORT_FORMAT_MASTER;
CREATE TABLE EXPORT_FORMAT_MASTER(
ID serial PRIMARY KEY NOT NULL,
COMPANY_ID integer REFERENCES COMPANY(ID) DEFAULT NULL,
FORMAT varchar(15) NOT NULL,
SYSTEM_SUPPORTED BOOL DEFAULT FALSE
);

CREATE TABLE EXPORT_FORMAT_DETAIL(
    ID serial PRIMARY KEY NOT NULL,
    FORMAT_ID integer REFERENCES EXPORT_FORMAT_MASTER(ID) NOT NULL,
    COLUMN_NAME VARCHAR(20),
    HEADER VARCHAR(20),
    POSITION SMALLINT,
    UNIQUE(FORMAT_ID, POSITION)
);

INSERT INTO export_format_master
(format, system_supported)
values
('All Data', true);

INSERT INTO export_format_detail
(format_id, column_name, header,  "position")
VALUES
(1, 'candidateName','Candidate Name', 1),
(1, 'chatbotStatus','Chatbot Status', 2),
(1, 'chatFilledTimeStamp', 'Chatbot Filled Timstamp', 3),
(1, 'currentStage','Stage', 4),
(1, 'keySkillsStrength','Key Skills Strength', 5),
(1, 'currentCompany','Current Company', 6),
(1, 'currentDesignation','Current Designation', 7),
(1, 'email','Email', 8),
(1, 'countryCode','Country Code', 9),
(1, 'mobile','Mobile', 10),
(1, 'totalExperience','Total Experience', 11),
(1, 'createdBy','Created By', 12);


--For ticket #272
UPDATE STAGE_MASTER SET STAGE_NAME='Sourcing' WHERE STAGE_NAME = 'Source';
UPDATE STAGE_MASTER SET STAGE_NAME='Screening' WHERE STAGE_NAME = 'Screen';
UPDATE STAGE_MASTER SET STAGE_NAME='Submitted' WHERE STAGE_NAME = 'Resume Submit';
UPDATE STAGE_MASTER SET STAGE_NAME='Hired' WHERE STAGE_NAME = 'Join';

UPDATE STEPS_PER_STAGE SET STEP_NAME='Sourcing' WHERE STEP_NAME = 'Source';
UPDATE STEPS_PER_STAGE SET STEP_NAME='Screening' WHERE STEP_NAME = 'Screen';
UPDATE STEPS_PER_STAGE SET STEP_NAME='Submitted' WHERE STEP_NAME = 'Resume Submit';
UPDATE STEPS_PER_STAGE SET STEP_NAME='Hired' WHERE STEP_NAME = 'Join';

UPDATE COMPANY_STAGE_STEP SET STEP='Sourcing' WHERE STEP = 'Source';
UPDATE COMPANY_STAGE_STEP SET STEP='Screening' WHERE STEP = 'Screen';
UPDATE COMPANY_STAGE_STEP SET STEP='Submitted' WHERE STEP = 'Resume Submit';
UPDATE COMPANY_STAGE_STEP SET STEP='Hired' WHERE STEP = 'Join';

--For ticket #276
ALTER TABLE CANDIDATE_COMPANY_DETAILS
ALTER COLUMN COMPANY_NAME TYPE VARCHAR(75);

ALTER TABLE CANDIDATE_PROJECT_DETAILS
ALTER COLUMN COMPANY_NAME TYPE VARCHAR(75);

--For ticket #267
Update cv_parsing_details set cv_rating_api_flag = false where job_candidate_mapping_id not in (select job_candidate_mapping_id from cv_rating)
and processing_status = 'Success' and cv_rating_api_flag is true;

--For ticket #268
Insert into MASTER_DATA (TYPE, VALUE) values
('reasonForChange','Too much time spent in Commuting to work'),
('reasonForChange','Too much travelling in the job'),
('reasonForChange','Have been in same company for too long'),
('reasonForChange','Company has shutdown'),
('reasonForChange','Company is downsizing /got a layoff'),
('reasonForChange','Am a Contract employee, want to shift to permanent employment'),
('reasonForChange','Want to work in a different domain'),
('reasonForChange','Want to work in a different project'),
('reasonForChange','Not getting paid my salary on time'),
('reasonForChange','Have not been promoted for a long time'),
('reasonForChange','Want to work with a Larger Size Company'),
('reasonForChange','Want to work with a Bigger Brand'),
('reasonForChange','Want to get away from shift working'),
('reasonForChange','Have been on maternity break'),
('reasonForChange','Have been on Sabbatical'),
('reasonForChange','Other');

ALTER TABLE JOB_CANDIDATE_MAPPING
ADD COLUMN REASON_FOR_CHANGE VARCHAR(100);

--For ticket #284
ALTER TABLE COMPANY_ADDRESS
DROP CONSTRAINT company_address_address_title_key;

ALTER TABLE COMPANY_ADDRESS
ADD CONSTRAINT UNIQUE_COMPANY_ADDRESS_TITLE UNIQUE(COMPANY_ID, ADDRESS_TITLE);

--For ticket #289
INSERT INTO MASTER_DATA(TYPE, VALUE) VALUES
('callOutCome', 'Connected'),
('callOutCome', 'No Answer'),
('callOutCome', 'Busy'),
('callOutCome', 'Wrong Number'),
('callOutCome', 'Left Message/VoiceMail');

ALTER TABLE JCM_HISTORY
ADD COLUMN CALL_LOG_OUTCOME VARCHAR(25),
ADD COLUMN SYSTEM_GENERATED BOOL DEFAULT 't' NOT NULL;

ALTER TABLE JCM_HISTORY
RENAME COLUMN DETAILS TO COMMENT;

ALTER TABLE JCM_HISTORY
ALTER COLUMN COMMENT TYPE TEXT;

--For ticket #28
UPDATE SMS_TEMPLATES SET  TEMPLATE_CONTENT = 'Oh no [[${commBean.receiverfirstname}]]!  The Litmus Profile you started creating for the [[${commBean.jobtitle}]] job at [[${commBean.sendercompany}]] was left incomplete. It''s important that you finish the profile to be considered for the job. Continue from where you left last. Just click the link to continue. [[${commBean.chatlink}]]'  WHERE TEMPLATE_NAME = 'ChatIncompleteReminder1';

--For ticket #255
ALTER TABLE JOB
ADD COLUMN JOB_REFERENCE_ID UUID NOT NULL DEFAULT uuid_generate_v1();

--From ML get capability_name length 50
ALTER TABLE JOB_CAPABILITIES
ALTER COLUMN CAPABILITY_NAME TYPE VARCHAR(50);

--For ticket #301
INSERT INTO MASTER_DATA(TYPE, VALUE) VALUES
('referrerRelation', 'Candidate reported to me directly'),
('referrerRelation', 'I reported to the Candidate'),
('referrerRelation', 'We were peers in the same company'),
('referrerRelation', 'Candidate is a friend'),
('referrerRelation', 'Candidate is a relative'),
('referrerRelation', 'We were students together'),
('referrerRelation', 'I don''t know the candidate, simply referring'),
('jobType', 'Full Time'),
('jobType', 'Part Time'),
('jobType', 'Temporary'),
('jobType', 'Intern');

--For ticket #310
ALTER TABLE JOB
ADD COLUMN JOB_TYPE INTEGER REFERENCES MASTER_DATA(ID);

UPDATE JOB
SET JOB_TYPE = (SELECT ID FROM MASTER_DATA WHERE TYPE = 'jobType' AND VALUE = 'Full Time');

ALTER TABLE JOB
ALTER COLUMN JOB_TYPE SET NOT NULL;

DROP TABLE JOB_DETAILS;

ALTER TABLE COMPANY_ADDRESS
ADD COLUMN CITY VARCHAR(100),
ADD COLUMN STATE VARCHAR(100),
ADD COLUMN COUNTRY VARCHAR(50);

-- view to concatenate all skills as comma separated values per job
drop view if exists jobKeySkillAggregation;
create view jobKeySkillAggregation as
select job_key_skills.job_id as jobId, string_agg(trim(skills_master.skill_name), ',') as keySkills
from skills_master, job_key_skills
where skills_master.id = job_key_skills.skill_id
group by job_key_skills.job_id;

-- view to select all required fields for search query
drop view if exists jobDetailsView;
create view jobDetailsView AS
select
	job.id as jobId,
	job.company_id as companyId,
	job.job_title as jobTitle,
	job.job_type as jobType,
	job.created_on as jobCreatedOn,
	company_address.address as jobLocation,
	company_address.city as jobLocationCity,
	company_address.state as jobLocationState,
	company_address.country as jobLocationCountry,
	exp.value as jobExperience,
	education.value as education, jobKeySkillAggregation.keyskills as keyskills
from job
left join company_address
on job.job_location = company_address.id
left join master_data exp
on job.experience_range = exp.id
left join master_data education
on job.education = education.id
left join jobKeySkillAggregation
on job.id = jobKeySkillAggregation.jobId
where job.status = 'Live'
order by jobId;

--For ticket  #290
ALTER TABLE JOB_CANDIDATE_MAPPING
ADD COLUMN CV_FILE_TYPE VARCHAR (10);

--Migrate all cvTypes from candidate detail table to job candidate mapping table
UPDATE JOB_CANDIDATE_MAPPING AS JCM
SET CV_FILE_TYPE = CD.CV_FILE_TYPE
FROM CANDIDATE_DETAILS AS CD
WHERE JCM.CANDIDATE_ID = CD.CANDIDATE_ID AND CD.CV_FILE_TYPE IS NOT NULL;

ALTER TABLE CANDIDATE_DETAILS
DROP COLUMN CV_FILE_TYPE;

--For ticket #311
CREATE TABLE EMPLOYEE_REFERRER (
ID serial PRIMARY KEY NOT NULL,
FIRST_NAME VARCHAR (45) NOT NULL,
LAST_NAME VARCHAR (45) NOT NULL,
EMAIL VARCHAR(50) NOT NULL UNIQUE,
EMPLOYEE_ID VARCHAR(10) NOT NULL,
MOBILE VARCHAR (15) NOT NULL,
LOCATION VARCHAR(50) NOT NULL,
CREATED_ON TIMESTAMP NOT NULL
);

CREATE TABLE CANDIDATE_REFERRAL_DETAIL(
ID serial PRIMARY KEY NOT NULL,
JOB_CANDIDATE_MAPPING_ID INTEGER REFERENCES JOB_CANDIDATE_MAPPING(ID) NOT NULL,
EMPLOYEE_REFERRER_ID INTEGER REFERENCES EMPLOYEE_REFERRER(ID) NOT NULL,
REFERRER_RELATION INTEGER REFERENCES MASTER_DATA(ID) NOT NULL,
REFERRER_CONTACT_DURATION SMALLINT NOT NULL
);

ALTER TABLE JOB_CANDIDATE_MAPPING
ALTER COLUMN CANDIDATE_SOURCE TYPE VARCHAR(17);


INSERT INTO public.users(
 email, first_name, last_name, mobile, company_id, role, status, country_id, created_on)
	VALUES ('systemuser@hex.com', 'System', 'User','1234567890',
			(select id from company where company_name= 'LitmusBlox'),'BusinessUser','New', 3, now());


-- Increase address length #329
ALTER TABLE company_address ALTER COLUMN address type VARCHAR(300);

--Increase designation length #337
ALTER TABLE CANDIDATE_COMPANY_DETAILS ALTER COLUMN DESIGNATION TYPE VARCHAR(100);

-- Additional column for company_short_name
ALTER TABLE COMPANY
ADD COLUMN SHORT_NAME VARCHAR(8) UNIQUE;

--For ticket #344
ALTER TABLE CV_PARSING_DETAILS
ADD COLUMN CV_CONVERT_API_FLAG BOOL NOT NULL DEFAULT 'f';

--adding flag to identify if job has screening question or not
ALTER TABLE Job ADD COLUMN HR_QUESTION_AVAILABLE BOOL NOT NULL DEFAULT 'f';
update job set hr_question_available = 't' where id in (select distinct job_id from job_screening_questions);

--additional column for tricentis requirement. ticket #346
ALTER TABLE Job ADD COLUMN RESUBMIT_HR_CHATBOT BOOL NOT NULL DEFAULT 'f';

update Job set RESUBMIT_HR_CHATBOT='f';


ALTER TABLE export_format_detail ALTER COLUMN column_name TYPE VARCHAR(25), ALTER COLUMN header TYPE VARCHAR(25);

ALTER TABLE JCM_COMMUNICATION_DETAILS RENAME COLUMN CHAT_COMPLETE_FLAG TO TECH_CHAT_COMPLETE_FLAG;

update job_candidate_mapping
set chatbot_status='Invited'
where id in (select jcm_id from jcm_communication_details where jcm_communication_details.chat_invite_flag='t') and
chatbot_status is NULL and candidate_interest='f';

update job_candidate_mapping
set chatbot_status='Not Interested'
where
candidate_interest_timestamp is not null and candidate_interest='f';

update job_candidate_mapping
set chatbot_status='Incomplete'
where
candidate_interest_timestamp is not null and candidate_interest='t' and chatbot_status is null;

-- #355 set role="Hiring Manager" i.e: BusinessUser for users of clients of recruitment agency.
update users set role='BusinessUser'
where
company_id in
(
    select id from company where recruitment_agency_id is not null
);

--For ticket #336
ALTER TABLE COMPANY ALTER COLUMN SHORT_NAME TYPE VARCHAR(25);

--For ticket #350
ALTER TABLE COMPANY
ADD COLUMN SUBDOMAIN_CREATED BOOL NOT NULL DEFAULT 'f',
ADD COLUMN SUBDOMAIN_CREATED_ON TIMESTAMP;

-- For ticket #333
ALTER TABLE CV_PARSING_DETAILS
ADD COLUMN PARSING_RESPONSE_ML TEXT,
ADD COLUMN PARSING_RESPONSE_PYTHON TEXT;

--Before use
CREATE EXTENSION IF NOT EXISTS hstore;

CREATE TABLE CV_PARSING_API_DETAILS(
ID serial PRIMARY KEY NOT NULL,
API_URL VARCHAR (255) NOT NULL,
API_SEQUENCE SMALLINT NOT NULL,
ACTIVE BOOL DEFAULT TRUE,
COLUMN_TO_UPDATE VARCHAR (25) NOT NULL,
QUERY_ATTRIBUTES HSTORE,
CONSTRAINT UNIQUE_API_URL UNIQUE(API_URL),
CONSTRAINT UNIQUE_API_SEQUENCE UNIQUE(API_SEQUENCE)
);

INSERT INTO CV_PARSING_API_DETAILS (API_URL, API_SEQUENCE, ACTIVE, COLUMN_TO_UPDATE, QUERY_ATTRIBUTES) VALUES
('https://rest.rchilli.com/RChilliParser/Rchilli/parseResume', 1, true, 'PARSING_RESPONSE_JSON',
'"userkey" => "2SNEDYNPV30",
"version" => "7.0.0",
"subuserid" => "Hexagon Search"'
),
('http://cvparser.litmusblox.net/parsecv', 2, true, 'PARSING_RESPONSE_PYTHON',null
),
('https://cia1z4r0d4.execute-api.ap-south-1.amazonaws.com/Test/resumeParser-Test', 3, true, 'PARSING_RESPONSE_ML',
null
);

--For ticket #374
CREATE TABLE CUSTOMIZED_CHATBOT_PAGE_CONTENT(
ID serial PRIMARY KEY NOT NULL,
COMPANY_ID INTEGER REFERENCES COMPANY(ID) NOT NULL,
PAGE_INFO hstore NOT NULL,
CONSTRAINT UNIQUE_PAGE_INFO_COMPANY UNIQUE(COMPANY_ID)
);

ALTER TABLE JOB
ADD COLUMN CUSTOMIZED_CHATBOT bool default 'f' NOT NULL;

INSERT INTO CUSTOMIZED_CHATBOT_PAGE_CONTENT (COMPANY_ID, PAGE_INFO) VALUES
(80, '"introText" => "text", "showCompanyLogo" => true, "thankYouText" => "<p>text to be displayed</p>", "showFollowSection" => true');

ALTER TABLE USER_SCREENING_QUESTION
ALTER COLUMN QUESTION TYPE VARCHAR(250),
ALTER COLUMN OPTIONS TYPE VARCHAR(200)[];

-- to update custom chatbot detail for tricentis.
update CUSTOMIZED_CHATBOT_PAGE_CONTENT set PAGE_INFO='"introText"=>"Automation premier League requires you to get tested on", "thankYouText"=>"The sore of your test will be communicated to you via email tomorrow from tricentis_apl@litmusblox.io", "showCompanyLogo"=>"false", "showFollowSection"=>"false", "showProceedButton"=>"true", "showConsentPage"=>"false"' where company_id=43;

--For ticket #389
UPDATE MASTER_DATA SET VALUE = 'Left Message or Voicemail' WHERE VALUE = 'Left Message/VoiceMail';

--For ticket #373
CREATE TABLE STAGE_STEP_MASTER (
ID serial PRIMARY KEY NOT NULL,
STAGE varchar(15) NOT NULL,
STEP varchar(25) NOT NULL,
CONSTRAINT UNIQUE_STAGE_STEP_KEY UNIQUE(STAGE, STEP)
);

Insert into STAGE_STEP_MASTER(STAGE, STEP)
VALUES ('Sourcing','Sourcing'),
('Screening','Screening'),
('Submitted','Submitted'),
('Interview','L1'),
('Make Offer','Make Offer'),
('Offer','Offer'),
('Hired','Hired');

ALTER TABLE JCM_HISTORY DROP CONSTRAINT jcm_history_stage_fkey;
ALTER TABLE JOB_CANDIDATE_MAPPING DROP CONSTRAINT job_candidate_mapping_stage_fkey;
ALTER TABLE JOB_HIRING_TEAM DROP CONSTRAINT job_hiring_team_stage_step_id_fkey;

update job_candidate_mapping set stage = stageMaster.stageStepId from (select stageStepId, jobStageStep from (select jst.job_id, jst.id as jobStageStep, sm.stage_name as stageName, ssm."id" as stageStepId from stage_master sm
inner join company_stage_step cst on
sm."id" = cst.stage
inner join stage_step_master ssm on
sm.stage_name = ssm.stage
inner join job_stage_step jst on
cst.id = jst.stage_step_id) as stageMaster) as stageMaster where stage=stageMaster.jobStageStep;

update job_hiring_team set stage_step_id = stageMaster.stageStepId from (select stageStepId, jobStageStep from (select jst.job_id, jst.id as jobStageStep, sm.stage_name as stageName, ssm."id" as stageStepId from stage_master sm
inner join company_stage_step cst on
sm."id" = cst.stage
inner join stage_step_master ssm on
sm.stage_name = ssm.stage
inner join job_stage_step jst on
cst.id = jst.stage_step_id) as stageMaster) as stageMaster where stage_step_id=stageMaster.jobStageStep;

update jcm_history set stage = stageMaster.stageStepId from (select stageStepId, jobStageStep from (select jst.job_id, jst.id as jobStageStep, sm.stage_name as stageName, ssm."id" as stageStepId from stage_master sm
inner join company_stage_step cst on
sm."id" = cst.stage
inner join stage_step_master ssm on
sm.stage_name = ssm.stage
inner join job_stage_step jst on
cst.id = jst.stage_step_id) as stageMaster) as stageMaster where stage=stageMaster.jobStageStep;

ALTER TABLE job_candidate_mapping
ADD CONSTRAINT job_candidate_mapping_stage_step_fkey FOREIGN KEY(stage) REFERENCES stage_step_master(id);

ALTER TABLE job_hiring_team
ADD CONSTRAINT job_hiring_team_stage_step_fkey FOREIGN KEY(stage_step_id) REFERENCES stage_step_master(id);

ALTER TABLE jcm_history
ADD CONSTRAINT jcm_history_stage_step_fkey FOREIGN KEY(stage) REFERENCES stage_step_master(id);

DROP TABLE company_stage_step, job_stage_step, steps_per_stage, stage_master;

--For ticket #377
ALTER TABLE COMPANY
ADD COLUMN COUNTRY_ID INTEGER REFERENCES COUNTRY(ID);

--Update existing record's
UPDATE COMPANY
SET COUNTRY_ID = (SELECT ID FROM COUNTRY WHERE COUNTRY_NAME = 'India');

ALTER TABLE COMPANY
ALTER COLUMN COUNTRY_ID SET NOT NULL;
UPDATE job_candidate_mapping SET candidate_source = 'NaukriJobPosting' WHERE candidate_source = 'NaukriMail';

INSERT INTO export_format_detail
(format_id, column_name, header, "position")
VALUES
(1, 'createdOn','Created On', 13),
(1, 'capabilityScore', 'Capability Score', 14);


-- #42 litmusblox-chatbot
INSERT INTO CUSTOMIZED_CHATBOT_PAGE_CONTENT (COMPANY_ID, PAGE_INFO) VALUES
(4080, '"introText"=>"Automation premier League requires you to get tested on", "thankYouText"=>"The score of your test will be communicated to you via email tomorrow", "showCompanyLogo"=>"false", "showFollowSection"=>"false", "showProceedButton"=>"true", "showConsentPage"=>"false"');


-- #399 litmusblox-backend Screening questions: Increase length of input field response
alter table candidate_screening_question_response alter column response type varchar(300);

-- #382 Design & Implement: Emails/SMS for job postings/mass mailing
ALTER TABLE JOB_CANDIDATE_MAPPING
ADD COLUMN AUTOSOURCED bool NOT NULL default 'f';

UPDATE job_candidate_mapping set autosourced='t' where candidate_source in ('NaukriJobPosting','NaukriMassMail', 'EmployeeReferral', 'CareerPage');

ALTER TABLE JCM_COMMUNICATION_DETAILS
ADD COLUMN autosource_acknowledgement_timestamp_email TIMESTAMP DEFAULT NULL,
ADD COLUMN autosource_acknowledgement_timestamp_sms TIMESTAMP DEFAULT NULL;

insert into sms_templates(template_name, template_content) values
('autosourceAcknowledgement', '[[${commBean.sendercompany}]] thanks you for your application for [[${commBean.jobtitle}]] position. We will be in touch with you for further action if your profile is shortlisted. Good luck!'),
('autosourceApplicationShortlisted', '[[${commBean.sendercompany}]] has shortlisted your application for [[${commBean.jobtitle}]] position. Please click on the link below to complete your profile and be considered for an interview. [[${commBean.chatlink}]]'),
('autosourceLinkNotVisited', 'Last Reminder: [[${commBean.sendercompany}]] has shortlisted your application for [[${commBean.jobtitle}]] position. Click on the link below to complete your profile and be considered for an interview. [[${commBean.chatlink}]]');

ALTER TABLE email_log ALTER COLUMN template_name TYPE VARCHAR(35);

INSERT INTO COUNTRY (COUNTRY_NAME, COUNTRY_CODE, MAX_MOBILE_LENGTH, COUNTRY_SHORT_CODE) VALUES
('Norway','+47', 8,'no');

--For ticket #383
ALTER TABLE COMPANY_ADDRESS DROP CONSTRAINT company_address_address_title_key;
Alter table COMPANY_ADDRESS add constraint company_address_address_title_company_id_key unique(address_title, company_id);

--For ticket #364
CREATE TABLE INTERVIEW_DETAILS (
ID serial PRIMARY KEY NOT NULL,
JOB_CANDIDATE_MAPPING_ID INTEGER REFERENCES JOB_CANDIDATE_MAPPING(ID) NOT NULL,
INTERVIEW_TYPE VARCHAR(20) NOT NULL,
INTERVIEW_MODE VARCHAR(20) NOT NULL,
INTERVIEW_LOCATION INTEGER REFERENCES COMPANY_ADDRESS(ID),
INTERVIEW_DATE TIMESTAMP NOT NULL,
INTERVIEW_INSTRUCTIONS TEXT,
SEND_JOB_DESCRIPTION bool DEFAULT 'f' NOT NULL,
CANCELLED bool DEFAULT 'f' NOT NULL,
CANCELLATION_REASON INTEGER REFERENCES MASTER_DATA(ID),
SHOW_NO_SHOW bool,
NO_SHOW_REASON INTEGER REFERENCES MASTER_DATA(ID),
COMMENTS VARCHAR(250),
INTERVIEW_REFERENCE_ID UUID NOT NULL DEFAULT uuid_generate_v1(),
CANDIDATE_CONFIRMATION bool,
CANDIDATE_CONFIRMATION_TIME TIMESTAMP,
CANCELLATION_COMMENTS VARCHAR(250),
SHOW_NO_SHOW_COMMENTS VARCHAR(250),
INTERVIEW_SCHEDULED_EMAIL_TIMESTAMP TIMESTAMP DEFAULT NULL,
INTERVIEW_CONFIRMED_EMAIL_TIMESTAMP TIMESTAMP DEFAULT NULL,
INTERVIEW_REMINDER_PREVIOUS_DAY_TIMESTAMP TIMESTAMP DEFAULT NULL,
INTERVIEW_REMINDER_SAME_DAY_EMAIL_TIMESTAMP TIMESTAMP DEFAULT NULL,
INTERVIEW_REMINDER_SAME_DAY_SMS_TIMESTAMP TIMESTAMP DEFAULT NULL,
INTERVIEW_NO_SHOW_EMAIL_TIMESTAMP TIMESTAMP DEFAULT NULL,
INTERVIEW_CANCELLED_EMAIL_TIMESTAMP TIMESTAMP DEFAULT NULL,
INTERVIEW_REJECTION_EMAIL_TIMESTAMP TIMESTAMP DEFAULT NULL,
CREATED_ON TIMESTAMP NOT NULL,
CREATED_BY INTEGER REFERENCES USERS(ID) NOT NULL,
UPDATED_ON TIMESTAMP,
UPDATED_BY INTEGER REFERENCES USERS(ID)
);

CREATE TABLE INTERVIEWER_DETAILS (
ID serial PRIMARY KEY NOT NULL,
INTERVIEW_ID INTEGER REFERENCES INTERVIEW_DETAILS(ID) NOT NULL,
INTERVIEWER INTEGER REFERENCES USERS(ID) NOT NULL,
CREATED_ON TIMESTAMP NOT NULL,
CREATED_BY INTEGER REFERENCES USERS(ID) NOT NULL,
UPDATED_ON TIMESTAMP,
UPDATED_BY INTEGER REFERENCES USERS(ID),
CONSTRAINT UNIQUE_INTERVIEW_MAPPING UNIQUE(INTERVIEW_ID, INTERVIEWER)
);

Insert into MASTER_DATA (TYPE, VALUE) values
('cancellationReasons','Client cancelled iv 1'),
('cancellationReasons','Candidate no show 1'),
('cancellationReasons','Panel not available 1'),
('cancellationReasons','Client cancelled iv 2'),
('cancellationReasons','Candidate no show 2'),
('cancellationReasons','Panel not available 2'),

('noShowReasons','Personal/Family'),
('noShowReasons','Professional'),
('noShowReasons','Medical'),
('noShowReasons','Logistics'),
('noShowReasons','Not reachable'),
('noShowReasons','Client Cancellation');

-- For ticket #406
Insert into MASTER_DATA (TYPE, VALUE) values
('interviewConfirmation','Yes, I will attend the interview'),
('interviewConfirmation','I wish to reschedule the interview'),
('interviewConfirmation','No, I am not able to attend the interview');

ALTER TABLE INTERVIEW_DETAILS
ADD COLUMN CANDIDATE_CONFIRMATION_VALUE INTEGER REFERENCES MASTER_DATA(ID);

ALTER TABLE export_format_detail ALTER COLUMN column_name TYPE VARCHAR(30), ALTER COLUMN header TYPE VARCHAR(30);

ALTER TABLE export_format_detail ADD COLUMN stage VARCHAR(15);

delete from export_format_detail where format_id=(select id from export_format_master where format='All Data');
INSERT INTO export_format_detail
(format_id, column_name, header,  "position", stage)
VALUES
(1, 'candidateName','Candidate Name', 1, null),
(1, 'chatbotStatus','Chatbot Status', 2, null),
(1, 'chatbotFilledTimeStamp', 'Chatbot Filled Timestamp', 3, null),
(1, 'currentStage','Stage', 4, null),
(1, 'keySkillsStrength','Key Skills Strength', 5, null),
(1, 'currentCompany','Current Company', 6, null),
(1, 'currentDesignation','Current Designation', 7, null),
(1, 'email','Email', 8, null),
(1, 'countryCode','Country Code', 9, null),
(1, 'mobile','Mobile', 10, null),
(1, 'totalExperience','Total Experience', 11, null),
(1, 'createdBy','Created By', 12, null),
(1, 'createdOn','Created On', 13, ''),
(1, 'capabilityScore', 'Capability Score', 14, ''),
(1, 'interviewDate','Interview Date', 15, 'Interview'),
(1, 'interviewType','Interview Type', 16, 'Interview'),
(1, 'interviewMode','Interview Mode', 17, 'Interview'),
(1, 'interviewLocation','Interview location', 18, 'Interview'),
(1, 'candidateConfirmation','Candidate Confirmation', 19, 'Interview'),
(1, 'candidateConfirmationTime','Candidate Confirmation Time', 20, 'Interview'),
(1, 'showNoShow','Show No Show', 21, 'Interview'),
(1, 'noShowReason','No Show Reason' ,22, 'Interview'),
(1, 'cancelled', 'Interview Cancelled', 23, 'Interview'),
(1, 'cancellationReason','Cancellation Reason', 24, 'Interview');

--For ticket #336
UPDATE COMPANY SET SHORT_NAME =
case
 when COMPANY_NAME = 'LitmusBlox' then 'LitmusBlox'
 when COMPANY_NAME = 'Hyperbola Technologies' then 'Hyperbola'
 when COMPANY_NAME = 'Bold Dialogue' then 'BoldDialogue'
 when COMPANY_NAME = 'EarlySalary' then 'EarlySalary'
 when COMPANY_NAME = 'KPIT Limited' then 'KPIT'
 when COMPANY_NAME = 'Mercurius IT' then 'MercuriusIT'
 when COMPANY_NAME = 'Aretove Technologies' then 'Aretove'
 when COMPANY_NAME = 'Gexcon India Pvt. Ltd.' then 'Gexcon'
 when COMPANY_NAME = 'Infogen Labs Pvt. Ltd.' then 'Infogen'
 when COMPANY_NAME = 'WhiteHedge Technologies' then 'WhiteHedge'
 when COMPANY_NAME = 'Hexagon Executive Search' then 'Hexagon'
 when COMPANY_NAME = 'Sanjay Tools and Accessories Pvt. Ltd.' then 'SanjayTools'
 when COMPANY_NAME = 'Krehsst Tech Solutions' then 'KrehsstTech'
 when COMPANY_NAME = 'TJC Group' then 'TJCGroup'
 when COMPANY_NAME = 'Clairvoyant India' then 'Clairvoyant'
 when COMPANY_NAME = 'L&T Infotech' then 'LTI'
 when COMPANY_NAME = 'Synechron Technologies' then 'Synechron'
 when COMPANY_NAME = 'Harman International' then 'Harman'
 when COMPANY_NAME = 'Expleo' then 'Expleo'
 when COMPANY_NAME = 'Quality Kiosk' then 'QualityKiosk'
 when COMPANY_NAME = 'Accurate Sales and Services' then 'AccurateSales'
 when COMPANY_NAME = 'Persistent Systems' then 'Persistent'
 when COMPANY_NAME = 'Fast Data Connect' then 'FastDataConnect'
 when COMPANY_NAME = 'Schlumberger' then 'Schlumberger'
 when COMPANY_NAME = 'Princeton IT Services' then 'PrincetonIT'
 when COMPANY_NAME = 'Tricentis' then 'Tricentis'
 when COMPANY_NAME = 'Evolent Health International Private Limited' then 'Evolent'
 when COMPANY_NAME = 'Techprimelab Software Pvt. Ltd.' then 'TechPrimeLab'
 when COMPANY_NAME = 'Melzer' then 'Melzer'
 when COMPANY_NAME = 'Shinde Developers Private Limited' then 'ShindeDevelopers'
 when COMPANY_NAME = 'Hexagon' then 'Hexa'
 when COMPANY_NAME = 'MRP Technologies' then 'MRPTech'
 when COMPANY_NAME = 'MRF' then 'MRF'
 when COMPANY_NAME = 'Witmans Advanced Fluids' then 'WitmansAdvF'
 when COMPANY_NAME = 'MPR' then 'MPR'
 when COMPANY_NAME = 'Spar Solutions' then 'SparSoln'
 when COMPANY_NAME = 'SR Pawar and company.' then 'SRPCompany'
 when COMPANY_NAME = 'Sci edge abstracts' then 'SciEdgeA'
 when COMPANY_NAME = 'Aventior' then 'Aventior'
 when COMPANY_NAME = 'Excellon Software' then 'ExcellonSoft'
 when COMPANY_NAME = 'Samrat Books' then 'SamratBook'
 when COMPANY_NAME = 'KK Tech' then 'KKTech'
 when COMPANY_NAME = 'Apna Job' then 'ApnaJob'
 when COMPANY_NAME = 'Mera Job' then 'MeraJob'
 ELSE SHORT_NAME
end;

--For ticket #415
ALTER TABLE COMPANY
ADD COLUMN COMPANY_UNIQUE_ID VARCHAR(8) UNIQUE;

--For ticket #364
ALTER TABLE INTERVIEW_DETAILS ALTER COLUMN INTERVIEW_LOCATION DROP NOT NULL;

--For scheduler ticket #33
ALTER TABLE INTERVIEW_DETAILS
ADD COLUMN INTERVIEW_SCHEDULED_EMAIL_TIMESTAMP TIMESTAMP DEFAULT NULL,
ADD COLUMN INTERVIEW_CONFIRMED_EMAIL_TIMESTAMP TIMESTAMP DEFAULT NULL,
ADD COLUMN INTERVIEW_REMINDER_PREVIOUS_DAY_TIMESTAMP TIMESTAMP DEFAULT NULL,
ADD COLUMN INTERVIEW_REMINDER_SAME_DAY_EMAIL_TIMESTAMP TIMESTAMP DEFAULT NULL,
ADD COLUMN INTERVIEW_REMINDER_SAME_DAY_SMS_TIMESTAMP TIMESTAMP DEFAULT NULL,
ADD COLUMN INTERVIEW_NO_SHOW_EMAIL_TIMESTAMP TIMESTAMP DEFAULT NULL,
ADD COLUMN INTERVIEW_CANCELLED_EMAIL_TIMESTAMP TIMESTAMP DEFAULT NULL,
ADD COLUMN INTERVIEW_REJECTION_EMAIL_TIMESTAMP TIMESTAMP DEFAULT NULL;

insert into sms_templates(template_name, template_content) values
('InterviewDay', 'You have an interview with [[${commBean.sendercompany}]] today at [[${commBean.interviewDate}]]. Below is the Google Maps link to the interview address. Please report 15 mins before. See you there! [[${commBean.interviewAddressLink}]]');

-- For ticket #410
ALTER TABLE COMPANY
ADD COLUMN SEND_COMMUNICATION bool NOT NULL DEFAULT 't';

-- For ticket #380
INSERT INTO SMS_TEMPLATES (TEMPLATE_NAME, TEMPLATE_CONTENT) VALUES
('OTPSms','Your OTP for LitmusBlox is [[${commBean.otp}]]. This OTP will expire in [[${commBean.otpExpiry}]] seconds.');

-- For ticket Update sms content #38
delete from sms_templates;
INSERT INTO SMS_TEMPLATES (TEMPLATE_NAME, TEMPLATE_CONTENT) VALUES
('ChatInvite','NEW JOB ALERT - [[${commBean.receiverfirstname}]], your profile is shortlisted by [[${commBean.sendercompany}]] for [[${commBean.jobtitle}]]. Click [[${commBean.chatlink}]] to see JD and apply.'),
('ChatCompleted','Congratulations [[${commBean.receiverfirstname}]]! Your application is complete for the [[${commBean.jobtitle}]] position at [[${commBean.sendercompany}]]. We will be in touch with you soon.'),
('ChatIncompleteReminder1','Your application to [[${commBean.sendercompany}]] was incomplete. Just click the [[${commBean.chatlink}]] to continue and complete.'),
('ChatIncompleteReminder2','FINAL REMINDER - Complete your application for [[${commBean.jobtitle}]] job at [[${commBean.sendercompany}]]. It will take only 5 minutes. Click [[${commBean.chatlink}]] to continue.'),
('LinkNotVisitedReminder1','[[${commBean.receiverfirstname}]], [[${commBean.sendercompany}]] has shortlisted you for [[${commBean.jobtitle}]] Job. Click [[${commBean.chatlink}]] to know more and apply.'),
('LinkNotVisitedReminder2','Not interested in this job? [[${commBean.sendercompany}]] has invited you to apply for the [[${commBean.jobtitle}]] position. Click [[${commBean.chatlink}]] to start.'),
('ChatNotVisitedReminder1','[[${commBean.receiverfirstname}]], this is link to apply for [[${commBean.jobtitle}]] job at [[${commBean.sendercompany}]]. It is valid only for 24 hours. Click [[${commBean.chatlink}]] to begin.'),
('ChatNotVisitedReminder2','[[${commBean.receiverfirstname}]], Just a reminder to complete your application for [[${commBean.jobtitle}]] job at [[${commBean.sendercompany}]]. This link will expire in 24 hours. [[${commBean.chatlink}]] '),
('AutosourceAcknowledgement', 'Hi [[${commBean.receiverfirstname}]], Your application for [[${commBean.jobtitle}]] position at [[${commBean.sendercompany}]] has been received. Good luck!'),
('AutosourceApplicationShortlisted', '[[${commBean.receiverfirstname}]], [[${commBean.sendercompany}]] has shortlisted you for [[${commBean.jobtitle}]] position. Click on link to complete your profile. [[${commBean.chatlink}]] '),
('AutosourceLinkNotVisited', 'Last Reminder [[${commBean.receiverfirstname}]] - [[${commBean.sendercompany}]] has shortlisted your application. Click link to complete your profile. [[${commBean.chatlink}]]'),
('OTPSms','Your OTP for LitmusBlox is [[${commBean.otp}]]. This OTP will expire in [[${commBean.otpExpiry}]] seconds.'),
('InterviewDay', 'INTERVIEW REMINDER FOR [[${commBean.receiverfirstname}]] - You have an interview with [[${commBean.sendercompany}]] today at [[${commBean.interviewdate}]]. Please report 15 mins before. Click Google Maps link for directions. See you there! [[${commBean.interviewAddressLink}]]');

-- For #441
INSERT INTO CUSTOMIZED_CHATBOT_PAGE_CONTENT (COMPANY_ID, PAGE_INFO) VALUES
(6, '"introText"=>"As a part of org level role baselining, we seek your inputs on various aspects of your work experience regarding the role of",
"thankYouText"=>"No further action is required from your side",
"showCompanyLogo"=>"false", "showFollowSection"=>"false", "showProceedButton"=>"true", "showConsentPage"=>"false"');

-- For ticket #443
UPDATE SMS_TEMPLATES
SET TEMPLATE_CONTENT = 'Your OTP for LitmusBlox job application is [[${commBean.otp}]]. This OTP will expire in [[${commBean.otpExpiry}]] minutes.'
WHERE TEMPLATE_NAME = 'OTPSms';

--For ticket #430
ALTER TABLE USERS ADD CONSTRAINT UNIQUE_USERS_EMAIL_KEY UNIQUE(EMAIL);


-- For ticket #444
UPDATE SMS_TEMPLATES
SET TEMPLATE_CONTENT = 'Your OTP for [[${commBean.sendercompany}]] job application is [[${commBean.otp}]]. This OTP will expire in [[${commBean.otpExpiry}]] minutes.'
WHERE TEMPLATE_NAME = 'OTPSms';

INSERT INTO CONFIGURATION_SETTINGS(CONFIG_NAME, CONFIG_VALUE)
VALUES ('otpExpiryMinutes', 3);

--For ticket #452
ALTER TABLE COMPANY_ADDRESS
ADD COLUMN AREA VARCHAR(50) DEFAULT NULL;

--For ticket https://github.com/hexagonsearch/litmusblox-scheduler/issues/48
update sms_templates set template_content = 'You have an interview with [[${commBean.sendercompany}]] today at [[${commBean.interviewdate}]]. Below is the Google Maps link to the interview address. Please report 15 mins before. See you there! [[${commBean.interviewAddressLink}]]' where template_name = 'InterviewDay';

--For ticket #450
drop view if exists jobDetailsView;
drop view if exists jobKeySkillAggregation;

create view jobKeySkillAggregation as
select job_key_skills.job_id as jobId, string_agg(trim(lower(skills_master.skill_name)), ',') as keySkills
from skills_master, job_key_skills
where skills_master.id = job_key_skills.skill_id
group by job_key_skills.job_id;

create view jobDetailsView AS
select
	job.id as jobId,
	job.company_id as companyId,
	job.job_title as jobTitle,
	job.job_type as jobType,
	job.created_on as jobCreatedOn,
	job.date_published as jobPublishedOn,
	company_address.address as jobLocation,
	company_address.city as jobLocationCity,
	company_address.state as jobLocationState,
	company_address.country as jobLocationCountry,
	exp.value as jobExperience,
	education.value as education, jobKeySkillAggregation.keyskills as keyskills
from job
left join company_address
on job.job_location = company_address.id
left join master_data exp
on job.experience_range = exp.id
left join master_data education
on job.education = education.id
left join jobKeySkillAggregation
on job.id = jobKeySkillAggregation.jobId
where job.status = 'Live'
order by jobPublishedOn desc, jobId asc;

-- For ticket #35 litmusblox-scheduler
ALTER TABLE JCM_COMMUNICATION_DETAILS ADD COLUMN REJECTED_TIMESTAMP_EMAIL TIMESTAMP DEFAULT NULL;

-- For ticket #379 - Async handling of upload candidates from a file and invite candidates
CREATE TABLE ASYNC_OPERATIONS_ERROR_RECORDS (
ID serial PRIMARY KEY NOT NULL,
JOB_ID INTEGER REFERENCES JOB(ID),
CANDIDATE_FIRST_NAME varchar(45),
CANDIDATE_LAST_NAME varchar(45),
EMAIL VARCHAR (50),
MOBILE VARCHAR (15),
ASYNC_OPERATION VARCHAR(20),
ERROR_MESSAGE VARCHAR(100),
JOB_CANDIDATE_MAPPING_ID INTEGER REFERENCES JOB_CANDIDATE_MAPPING(ID),
FILE_NAME VARCHAR(255),
CREATED_ON TIMESTAMP NOT NULL,
CREATED_BY INTEGER REFERENCES USERS(ID) NOT NULL
);