--Add EMAIL, MOBILE in JCM
alter table JOB_CANDIDATE_MAPPING add COLUMN EMAIL VARCHAR (50);
alter table JOB_CANDIDATE_MAPPING add COLUMN MOBILE VARCHAR (15);
alter table JOB_CANDIDATE_MAPPING add COLUMN COUNTRY_CODE VARCHAR (5);

update JOB_CANDIDATE_MAPPING set EMAIL = concat('sdedhia+',ID,'@gmail.com');
update JOB_CANDIDATE_MAPPING set MOBILE = concat('87600',ID,'6785');

alter table JOB_CANDIDATE_MAPPING alter COLUMN EMAIL SET NOT NULL;

-- rename uuid column in jcm
alter table JOB_CANDIDATE_MAPPING
rename column jcm_uuid to chatbot_uuid;

insert into MASTER_DATA (TYPE, VALUE) values
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

alter table COMPANY
add COLUMN COMPANY_DESCRIPTION TEXT,
add COLUMN WEBSITE VARCHAR(245),
ADD COLUMN LANDLINE VARCHAR(10),
ADD COLUMN INDUSTRY INTEGER REFERENCES MASTER_DATA(ID),
ADD COLUMN LINKEDIN VARCHAR(245),
ADD COLUMN FACEBOOK VARCHAR(245),
ADD COLUMN TWITTER VARCHAR(245),
ADD COLUMN LOGO VARCHAR(245),
ADD COLUMN SUBSCRIPTION VARCHAR(5) NOT NULL DEFAULT 'Lite';

alter table USERS
add COLUMN INVITATION_MAIL_TIMESTAMP TIMESTAMP NOT NULL DEFAULT localtimestamp,
add COLUMN RESET_PASSWORD_FLAG BOOL NOT NULL DEFAULT 'f',
add COLUMN RESET_PASSWORD_EMAIL_TIMESTAMP TIMESTAMP;

alter table JCM_COMMUNICATION_DETAILS
add COLUMN CHAT_INVITE_FLAG BOOL DEFAULT 'f';

alter table users alter column user_uuid drop not null;

create TABLE JCM_PROFILE_SHARING_DETAILS (
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
add COLUMN CHAT_COMPLETE_FLAG BOOL DEFAULT 'f';

alter table CANDIDATE_EDUCATION_DETAILS
alter column INSTITUTE_NAME type varchar(75);


drop table JCM_PROFILE_SHARING_DETAILS;

create TABLE JCM_PROFILE_SHARING_MASTER (
    ID serial PRIMARY KEY NOT NULL,
    RECEIVER_NAME varchar(45) NOT NULL,
    RECEIVER_EMAIL varchar(50) NOT NULL,
    SENDER_ID INTEGER REFERENCES USERS(ID) NOT NULL,
    EMAIL_SENT_ON TIMESTAMP DEFAULT NULL
);

create TABLE JCM_PROFILE_SHARING_DETAILS (
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
create TABLE CV_PARSING_DETAILS (
    ID serial PRIMARY KEY NOT NULL,
    CV_FILE_NAME varchar(255),
    PROCESSED_ON TIMESTAMP,
    PROCESSING_TIME smallint,
    PROCESSING_STATUS varchar(10),
    PARSING_RESPONSE text
);

create TABLE CANDIDATE_OTHER_SKILL_DETAILS (
    ID serial PRIMARY KEY NOT NULL,
    CANDIDATE_ID INTEGER REFERENCES CANDIDATE(ID) NOT NULL,
    SKILL VARCHAR(50),
    LAST_USED DATE,
    EXP_IN_MONTHS smallint
);

alter table CANDIDATE_SKILL_DETAILS
add COLUMN EXP_IN_MONTHS smallint;

insert into configuration_settings(config_name, config_value)
values('maxCvFiles',20);

alter table CV_PARSING_DETAILS
  rename COLUMN PARSING_RESPONSE TO PARSING_RESPONSE_JSON;

alter table CV_PARSING_DETAILS
  add PARSING_RESPONSE_TEXT text,
  add PARSING_RESPONSE_HTML text;

alter table CANDIDATE_SKILL_DETAILS
  add COLUMN VERSION VARCHAR(10);


------------- For ticket #107
alter table JOB_CAPABILITIES
add COLUMN CAPABILITY_ID INTEGER;

-- add script here to update existing capabilities with capability_name

alter table JOB_CAPABILITIES alter COLUMN CAPABILITY_ID SET NOT NULL;

alter table JOB_CAPABILITIES
drop COLUMN IMPORTANCE_LEVEL;

alter table JOB_CAPABILITIES
add COLUMN WEIGHTAGE SMALLINT NOT NULL DEFAULT 2;

-- For ticket #119
delete from MASTER_DATA where TYPE='importanceLevel';

-- For ticket #52
create TABLE COMPANY_HISTORY (
    ID serial PRIMARY KEY NOT NULL,
    COMPANY_ID INTEGER REFERENCES COMPANY(ID) NOT NULL,
    DETAILS VARCHAR(300),
    UPDATED_ON TIMESTAMP,
    UPDATED_BY INTEGER REFERENCES USERS(ID) NOT NULL
);

drop table JOB_HISTORY;

create TABLE JOB_HISTORY (
    ID serial PRIMARY KEY NOT NULL,
    JOB_ID INTEGER REFERENCES JOB(ID) NOT NULL,
    DETAILS VARCHAR(300),
    UPDATED_ON TIMESTAMP,
    UPDATED_BY INTEGER REFERENCES USERS(ID) NOT NULL
);

-- add a column error_message to cv_parsing_details for rChilli
alter table CV_PARSING_DETAILS
add COLUMN ERROR_MESSAGE varchar(100);


-- delete duplicate entry in skills master table and also remove rows from job key skills which references skill_id. Need to match by lower case.
select
    lower(skill_name),
    count( lower(skill_name) )
from
    skills_master
group by
    lower(skill_name)
having
    count( lower(skill_name) )> 1
order by
    lower(skill_name);

delete
from
    skills_master a
        USING skills_master b
WHERE
    a.id < b.id
    AND LOWER(a.skill_name) = LOWER(b.skill_name);

-- Added unique constraint on skill_name in skills_master with case insensitivity
alter table skills_master add constraint unique_skill_name unique(skill_name);

-- For ticket #123
alter table JOB_CANDIDATE_MAPPING
add COLUMN CHATBOT_STATUS VARCHAR(15),
ADD COLUMN SCORE SMALLINT,
ADD COLUMN CHATBOT_UPDATED_ON TIMESTAMP;

create TABLE CANDIDATE_TECH_RESPONSE_DATA(
    ID serial PRIMARY KEY NOT NULL,
    JOB_CANDIDATE_MAPPING_ID INTEGER REFERENCES JOB_CANDIDATE_MAPPING(ID) NOT NULL,
    TECH_RESPONSE TEXT,
    CONSTRAINT UNIQUE_JCM_TECH_RESPONSE UNIQUE(JOB_CANDIDATE_MAPPING_ID)
);


insert into CANDIDATE_TECH_RESPONSE_DATA (JOB_CANDIDATE_MAPPING_ID)
select ID from JOB_CANDIDATE_MAPPING;

-- For ticket #126

create TABLE JCM_HISTORY(
	ID serial PRIMARY KEY NOT NULL,
	JCM_ID INTEGER REFERENCES JOB_CANDIDATE_MAPPING(ID) NOT NULL,
	DETAILS VARCHAR(300),
	UPDATED_ON TIMESTAMP,
	UPDATED_BY INTEGER REFERENCES USERS(ID)
);

-- For ticket #135
alter table JOB
add COLUMN SCORING_ENGINE_JOB_AVAILABLE BOOL DEFAULT 'f';

update JOB set SCORING_ENGINE_JOB_AVAILABLE = 'f';

-- For ticket #143
alter table JCM_COMMUNICATION_DETAILS add COLUMN HR_CHAT_COMPLETE_FLAG BOOL DEFAULT 'f';

update JCM_COMMUNICATION_DETAILS set HR_CHAT_COMPLETE_FLAG = 't' where CHAT_COMPLETE_FLAG='t';

-- For ticket #144
insert into CONFIGURATION_SETTINGS(CONFIG_NAME, CONFIG_VALUE)
values('mlCall',1);

-- For ticket #145
alter table JOB
add COLUMN BU_ID INTEGER REFERENCES COMPANY_BU(ID),
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

alter table JOB alter COLUMN NO_OF_POSITIONS SET DEFAULT 1;

-- For ticket #151
insert into MASTER_DATA (TYPE, VALUE)
values
 ( 'noticePeriod','0'),
 ( 'noticePeriod','15'),
 ( 'noticePeriod','30'),
 ( 'noticePeriod','60'),
 ( 'noticePeriod','45'),
 ( 'noticePeriod','90'),
 ( 'noticePeriod','Others');

alter table CANDIDATE_COMPANY_DETAILS
rename COLUMN NOTICE_PERIOD TO NOTICE_PERIOD_OLD;

alter table CANDIDATE_COMPANY_DETAILS
add COLUMN NOTICE_PERIOD INTEGER REFERENCES MASTER_DATA(ID);

update CANDIDATE_COMPANY_DETAILS
set NOTICE_PERIOD = (select ID from MASTER_DATA where TYPE = 'noticePeriod' and VALUE = CANDIDATE_COMPANY_DETAILS.NOTICE_PERIOD_OLD);

-- Note: If above query does not work using the next one.
update CANDIDATE_COMPANY_DETAILS
set NOTICE_PERIOD = (select ID from MASTER_DATA where TYPE = 'noticePeriod' and VALUE = CANDIDATE_COMPANY_DETAILS.NOTICE_PERIOD_OLD::character varying);


alter table CANDIDATE_COMPANY_DETAILS drop COLUMN NOTICE_PERIOD_OLD;

-- For ticket #154

create TABLE WEIGHTAGE_CUTOFF_MAPPING(
    ID serial PRIMARY KEY NOT NULL,
    WEIGHTAGE INTEGER DEFAULT NULL,
    PERCENTAGE SMALLINT DEFAULT NULL,
    CUTOFF SMALLINT DEFAULT NULL,
    STAR_RATING SMALLINT NOT NULL,
    CONSTRAINT UNIQUE_WEIGHTAGE_STAR_RATING_MAPPING UNIQUE(WEIGHTAGE, STAR_RATING)
);

create TABLE WEIGHTAGE_CUTOFF_BY_COMPANY_MAPPING(
    ID serial PRIMARY KEY NOT NULL,
    COMPANY_ID INTEGER REFERENCES COMPANY(ID) NOT NULL,
    WEIGHTAGE INTEGER DEFAULT NULL,
    PERCENTAGE SMALLINT DEFAULT NULL,
    CUTOFF SMALLINT DEFAULT NULL,
    STAR_RATING SMALLINT NOT NULL,
    CONSTRAINT UNIQUE_WEIGHTAGE_STAR_RATING_BY_COMPANY_MAPPING UNIQUE(COMPANY_ID, WEIGHTAGE, STAR_RATING)
);

create TABLE JOB_CAPABILITY_STAR_RATING_MAPPING (
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
alter table MASTER_DATA
add COLUMN VALUE_TO_USE SMALLINT,
add COLUMN COMMENTS VARCHAR (255);

update MASTER_DATA
set VALUE_TO_USE = 1, COMMENTS = 'Candidate has 1-2 years of relevant work experience and works on given tasks on day to day basis. Exposure to job complexities is limited and needs support/guidance for complex tasks.' where value='Beginner';
update MASTER_DATA
set VALUE_TO_USE = 2, COMMENTS = 'Candidate can independently handle all tasks. Typically has 2 - 5 years of relevant work experience. Dependable on senior for assigned work. Can participate in training/grooming of juniors' where value = 'Competent';
update MASTER_DATA
set VALUE_TO_USE = 3, COMMENTS = 'Considered as a Master in the organization/industry. Candidate can handle highly complex scenarios and is the go-to person for others. Such candidates are rare to find and often come at a high cost. Select this option if you want to hire a expert.' where value = 'Expert';

--For ticket #161
update master_data set value='0 - 2 yrs' where value='0 - 3 yrs';
update master_data set value='2 - 4 yrs' where value='4 - 7 yrs';
update master_data set value='4 - 6 yrs' where value='8 - 12 yrs';
update master_data set value='6 - 8 yrs' where value='13 - 15 yrs';
update master_data set value='8 - 10 yrs' where value='17 - 20 yrs';

insert into MASTER_DATA (TYPE, VALUE)
values( 'experienceRange', '10 - 15 yrs'),
 ( 'experienceRange', '16 - 20 yrs');

alter table JOB
add COLUMN NOTICE_PERIOD INTEGER REFERENCES MASTER_DATA(ID);

alter table JOB
alter COLUMN min_salary SET DEFAULT 0,
alter COLUMN max_salary SET DEFAULT 0;

--For ticket #175
update master_data set value='0 - 2 Years' where value='0 - 2 yrs';
update master_data set value='2 - 4 Years' where value='2 - 4 yrs';
update master_data set value='4 - 6 Years' where value='4 - 6 yrs';
update master_data set value='6 - 8 Years' where value='6 - 8 yrs';
update master_data set value='8 - 10 Years' where value='8 - 10 yrs';
update master_data set value='10 - 15 Years' where value='10 - 15 yrs';
update master_data set value='15 - 20 Years' where value='16 - 20 yrs';
update master_data set value='20+ Years' where value='20+ yrs';

alter table JOB
add COLUMN EXPERIENCE_RANGE INTEGER REFERENCES MASTER_DATA(ID);

update Job j set experience_range =
(select id from master_data where value = (select concat((select concat_ws(' - ',replace(cast(min_experience as varchar), '.00',''), replace(cast(max_experience as varchar), '.00',''))), ' Years') from job
where id=j.id and min_experience is not null and max_experience is not null));

alter table JOB
drop COLUMN MIN_EXPERIENCE,
drop COLUMN MAX_EXPERIENCE;

--Update education values shown in the drop down in master data #178
update master_data set value = 'ACCA (ACCA)' where value = 'ACCA';
update master_data set value = 'B.S.S.E (BSSE)' where value = 'BSSE';
update master_data set value = 'Bachelor in Fine Arts (BFA)' where value = 'BFA';
update master_data set value = 'Bachelor in Foreign Trade (BFT)' where value = 'BFT';
update master_data set value = 'Bachelor in Management Studies (BMS)' where value = 'BMS';
update master_data set value = 'Bachelor of Architecture (BArch)' where value = 'BArch';
update master_data set value = 'Bachelor of Arts (BA)' where value = 'BA';
update master_data set value = 'Bachelor of Business Administration (BBA)' where value = 'BBA';
update master_data set value = 'Bachelor of Commerce (BCom)' where value = 'BCom';
update master_data set value = 'Bachelor of Commerce in Computer Application (BCCA)' where value = 'BCCA';
update master_data set value = 'Bachelor of Computer Applications (BCA)' where value = 'BCA';
update master_data set value = 'Bachelor of Computer Science (BCS)' where value = 'BCS';
update master_data set value = 'Bachelor of Dental Science (BDS)' where value = 'BDS';
update master_data set value = 'Bachelor of Design (BDes)' where value = 'BDes';
update master_data set value = 'Bachelor of Education (BEd)' where value = 'BEd';
update master_data set value = 'Bachelor of Engineering (BE)' where value = 'BE';
update master_data set value = 'Bachelor of Hotel Management (BHM)' where value = 'BHM';
update master_data set value = 'Bachelor of Information Technology (BIT)' where value = 'BIT';
update master_data set value = 'Bachelor of Pharmacy (BPharma)' where value = 'BPharma';
update master_data set value = 'Bachelor of Science (BSc)' where value = 'BSc';
update master_data set value = 'Bachelor of Technology. (BTech)' where value = 'BTech';
update master_data set value = 'Bachelor of Veterinary Science (BVSc)' where value = 'BVSc';
update master_data set value = 'Bachelors of Ayurveda where value =  Medicine & Surgery (BAMS)' where value = 'BAMS';
update master_data set value = 'Bachelors of Business Studies (BBS)' where value = 'BBS';
update master_data set value = 'Bachelors of Law (LLB)' where value = 'LLB';
update master_data set value = 'BBM (BBM)' where value = 'BBM';
update master_data set value = 'BHMS (BHMS)' where value = 'BHMS';
update master_data set value = 'BMM (BMM)' where value = 'BMM';
update master_data set value = 'Business Capacity Management (BCM)' where value = 'BCM';
update master_data set value = 'CA IPCC (CA IPCC)' where value = 'CA IPCC';
update master_data set value = 'CFA (CFA)' where value = 'CFA';
update master_data set value = 'Chartered Accountant (CA)' where value = 'CA';
update master_data set value = 'Company Secretary (CS)' where value = 'CS';
update master_data set value = 'CWA (CWA)' where value = 'CWA';
update master_data set value = 'Diploma (Diploma)' where value = 'Diploma';
update master_data set value = 'Diploma in Graphics & Animation (Diploma in Graphics & Animation)' where value = 'Diploma in Graphics & Animation';
update master_data set value = 'Doctor Of Philosophy (PHD)' where value = 'PHD';
update master_data set value = 'Executive Post Graduate Diploma in Business Management (EMBA)' where value = 'EMBA';
update master_data set value = 'Fashion/Designing (Fashion/Designing)' where value = 'Fashion/Designing';
update master_data set value = 'FCA (FCA)' where value = 'FCA';
update master_data set value = 'GD Art Commercial (Commercial Art)' where value = 'Commercial Art';
update master_data set value = 'Graduate Diploma in Business Administration (GDBA)' where value = 'GDBA';
update master_data set value = 'HSC (HSC)' where value = 'HSC';
update master_data set value = 'ICAI  CMA (ICAI/CMA)' where value = 'ICAI/CMA';
update master_data set value = 'ICWA (ICWA)' where value = 'ICWA';
update master_data set value = 'Integrated PG Course (I PG Course)' where value = 'I PG Course';
update master_data set value = 'Journalism/Mass Comunication (Journalism/Mass Comm.)' where value = 'Journalism/Mass Comm';
update master_data set value = 'M.E (ME)' where value = 'ME';
update master_data set value = 'M.phil (MPhil)' where value = 'MPhil';
update master_data set value = 'Management Development Programmes (MDP)' where value = 'MDP';
update master_data set value = 'Master of Architecture (MArch)' where value = 'MArch';
update master_data set value = 'Master of Arts (MA)' where value = 'MA';
update master_data set value = 'Master of Business Administration (MBA)' where value = 'MBA';
update master_data set value = 'Master of Business Management (MBM)' where value = 'MBM';
update master_data set value = 'Master of Commerce (MCom)' where value = 'MCom';
update master_data set value = 'Master of Computer Applications (MCA)' where value = 'MCA';
update master_data set value = 'Master of Computer Management (MCM)' where value = 'MCM';
update master_data set value = 'Master of Computer Science (MS CS)' where value = 'MS CS';
update master_data set value = 'Master of Education (MEd)' where value = 'MEd';
update master_data set value = 'Master of Financial Management (MFM)' where value = 'MFM';
update master_data set value = 'Master of Law (LLM)' where value = 'LLM';
update master_data set value = 'Master of Personnel Management (MPM)' where value = 'MPM';
update master_data set value = 'Master of Pharmacy (MPharma)' where value = 'MPharma';
update master_data set value = 'Master of Science (MSc)' where value = 'MSc';
update master_data set value = 'Master of Social Work (MSW)' where value = 'MSW';
update master_data set value = 'Master of Technology (MTech)' where value = 'MTech';
update master_data set value = 'Master of Veterinary Science (MVSc)' where value = 'MVSc';
update master_data set value = 'Master''s in Diploma in Business Administration (MDBA)' where value = 'MDBA';
update master_data set value = 'Masters in Fine Arts (MFA)' where value = 'MFA';
update master_data set value = 'Masters in Industrial Psychology (MS in Industrial Psychology)' where value = 'MS in Industrial Psychology';
update master_data set value = 'Masters in Information Management (MIM)' where value = 'MIM';
update master_data set value = 'Masters in Management Studies (MMS)' where value = 'MMS';
update master_data set value = 'Masters of finance and control (MFC)' where value = 'MFC';
update master_data set value = 'MBA/PGDM (MBA/PGDM)' where value = 'MBA/PGDM';
update master_data set value = 'MBBS (MBBS)' where value = 'MBBS';
update master_data set value = 'Medical (MS/MD)' where value = 'MS/MD';
update master_data set value = 'MEP (MEP)' where value = 'MEP';
update master_data set value = 'MS (MS)' where value = 'MS';
update master_data set value = 'Other (Other)' where value = 'Other';
update master_data set value = 'PG Diploma (PG Diploma)' where value = 'PG Diploma';
update master_data set value = 'PGDBA (PGDBA)' where value = 'PGDBA';
update master_data set value = 'Post Graduate Certification in Business Management (PGCBM)' where value = 'PGCBM';
update master_data set value = 'Post Graduate Diploma in Analytical Chemistry (PGDAC)' where value = 'PGDAC';
update master_data set value = 'Post Graduate Diploma in Computer Application (PGDCA)' where value = 'PGDCA';
update master_data set value = 'Post Graduate Program (PGP)' where value = 'PGP';
update master_data set value = 'Post Graduate Programme in Business Management ... (PGPBM)' where value = 'PGPBM';
update master_data set value = 'Post Graduate Programme in Management (PGPBM)' where value = 'PGPBM';
update master_data set value = 'Post Graduate Programme in Management (PGM)' where value = 'PGM';
update master_data set value = 'Postgraduate Certificate in Human Resource Management (PGCHRM)' where value = 'PGCHRM';
update master_data set value = 'PR/Advertising (PR/Advertising)' where value = 'PR/Advertising';
update master_data set value = 'Tourism (Tourism)' where value = 'Tourism';
update master_data set value = 'Vocational-Training (Vocational Training)' where value = 'Vocational Training';
insert into master_data(type, value) values ('education','Masters in Information Management (MIM)');

update MASTER_DATA set VALUE = '0 Days' where VALUE = '0';
update MASTER_DATA set VALUE = '15 Days' where VALUE = '15';
update MASTER_DATA set VALUE = '30 Days' where VALUE = '30';
update MASTER_DATA set VALUE = '60 Days' where VALUE = '60';
update MASTER_DATA set VALUE = '45 Days' where VALUE = '45';
update MASTER_DATA set VALUE = '90 Days' where VALUE = '90';

 insert into MASTER_DATA (TYPE, VALUE)
values
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

update JOB set FUNCTION = (select ID from MASTER_DATA where VALUE = 'ITES / BPO / Operations / Customer Service / Telecalling') where FUNCTION = (select ID from MASTER_DATA where VALUE = 'BPO');
update JOB set FUNCTION = (select ID from MASTER_DATA where VALUE = 'HR / Admin / PM / IR / Training') where FUNCTION = (select ID from MASTER_DATA where VALUE = 'Human Resources (HR)');
update JOB set FUNCTION = (select ID from MASTER_DATA where VALUE = 'Software Development -') where FUNCTION = (select ID from MASTER_DATA where VALUE = 'Information Technology (IT)');
update JOB set FUNCTION = (select ID from MASTER_DATA where VALUE = 'HR / Admin / PM / IR / Training') where FUNCTION = (select ID from MASTER_DATA where VALUE = 'Office Administration');
update JOB set FUNCTION = (select ID from MASTER_DATA where VALUE = 'Sales / Business Development / Client Servicing') where FUNCTION = (select ID from MASTER_DATA where VALUE = 'Sales');
update JOB set FUNCTION = (select ID from MASTER_DATA where VALUE = 'Production / Service Engineering / Manufacturing / Maintenance') where FUNCTION = (select ID from MASTER_DATA where VALUE = 'Manufacturing');

delete from MASTER_DATA where VALUE = 'BPO';
delete from MASTER_DATA where VALUE = 'Human Resources (HR)';
delete from MASTER_DATA where VALUE = 'Information Technology (IT)';
delete from MASTER_DATA where VALUE = 'Office Administration';
delete from MASTER_DATA where VALUE = 'Sales';
delete from MASTER_DATA where VALUE = 'Manufacturing';

--drop unique constraints of master_data for type and value
update MASTER_DATA set VALUE = '10 - 15 Years' where VALUE = '20+ Years';
update MASTER_DATA set VALUE = '15 - 20 Years' where ID = (select max(ID) from MASTER_DATA where VALUE = '10 - 15 Years');
update MASTER_DATA set VALUE = '20+ Years' where ID = (select max(ID) from MASTER_DATA where VALUE = '15 - 20 Years');
update MASTER_DATA set VALUE = '45 Days' where VALUE = '60 Days';
update MASTER_DATA set VALUE = '60 Days' where ID = (select max(ID) from MASTER_DATA where VALUE = '45 Days')
--add again unique constraints of master_data for type and value


-- For ticket #182
delete from JOB_CAPABILITY_STAR_RATING_MAPPING;

alter table JOB_CAPABILITY_STAR_RATING_MAPPING
add COLUMN JOB_ID INTEGER REFERENCES JOB(ID) NOT NULL;

-- For ticket #173
alter table CV_PARSING_DETAILS
add COLUMN CANDIDATE_ID INTEGER,
add COLUMN RCHILLI_JSON_PROCESSED BOOL;
-- For ticket #165
alter table company_bu
drop column updated_on, drop column updated_by;

alter table company_address
add column address_title varchar(100) not null unique default 'Default Address';


--For ticket #166
alter table JOB drop COLUMN CURRENCY;
alter table JOB add COLUMN CURRENCY VARCHAR(3) NOT NULL DEFAULT 'INR';

alter table JOB
add COLUMN HIRING_MANAGER INTEGER REFERENCES USERS(ID),
ADD COLUMN RECRUITER INTEGER REFERENCES USERS(ID);

-- #180
create INDEX idx_jcm_stage ON job_candidate_mapping(stage);
create INDEX idx_jcm_jobid ON job_candidate_mapping(job_id);
create INDEX idx_job_createdby ON job(created_by);
create INDEX idx_job_datearchived ON job(date_archived);


-- For ticket #147
create TABLE CREATE_JOB_PAGE_SEQUENCE(
	ID serial PRIMARY KEY NOT NULL,
	PAGE_NAME VARCHAR(25) NOT NULL,
	PAGE_DISPLAY_NAME VARCHAR(25) NOT NULL,
	PAGE_DISPLAY_ORDER SMALLINT NOT NULL,
	DISPLAY_FLAG BOOL NOT NULL DEFAULT 'T',
	SUBSCRIPTION_AVAILABILITY VARCHAR(5) NOT NULL DEFAULT 'LITE'
);

insert into CREATE_JOB_PAGE_SEQUENCE (PAGE_DISPLAY_NAME, PAGE_NAME, PAGE_DISPLAY_ORDER, DISPLAY_FLAG,SUBSCRIPTION_AVAILABILITY)
values
('Overview', 'overview', 1, 'T','Lite'),
('Expertise', 'expertise', 2, 'F','Lite'),
('Job Detail', 'jobDetail', 3, 'F','Max'),
('Screening Questions', 'screeningQuestions', 4, 'T','Lite'),
('Key Skills', 'keySkills', 5, 'T','Lite'),
('Hiring Team', 'hiringTeam', 6, 'F','Max'),
('Capabilities', 'capabilities', 7, 'T','Lite'),
('Preview', 'preview', 8, 'T','Lite');

--For ticket #183
insert into CONFIGURATION_SETTINGS(CONFIG_NAME, CONFIG_VALUE)
values('maxCapabilities',10);

-- For ticket #184
alter table CV_PARSING_DETAILS
add COLUMN JOB_CANDIDATE_MAPPING_ID INTEGER REFERENCES JOB_CANDIDATE_MAPPING(ID),
ADD COLUMN CV_RATING_API_FLAG BOOL DEFAULT 'F' NOT NULL,
ADD COLUMN CV_RATING_API_RESPONSE_TIME smallint;

create TABLE CV_RATING (
ID serial PRIMARY KEY NOT NULL,
JOB_CANDIDATE_MAPPING_ID integer REFERENCES JOB_CANDIDATE_MAPPING(ID) NOT NULL,
OVERALL_RATING smallint NOT NULL
);

create TABLE CV_RATING_SKILL_KEYWORD_DETAILS (
ID serial PRIMARY KEY NOT NULL,
CV_RATING_ID integer REFERENCES CV_RATING(ID) NOT NULL,
SUPPORTING_KEYWORDS text NOT NULL,
SKILL_NAME varchar(100) NOT NULL,
RATING smallint NOT NULL,
OCCURRENCE smallint NOT NULL
);

insert into CONFIGURATION_SETTINGS (CONFIG_NAME, CONFIG_VALUE)
values ('cvRatingTimeout', 30000);

--For ticket #204
alter table CV_PARSING_DETAILS
add COLUMN PARSED_TEXT JSON,
add COLUMN JOB_ID INTEGER,
add COLUMN EMAIL varchar(50);
--update parsing_response_json copy into parsedText column
update cv_parsing_details cpd set parsed_text=(select cast(parsing_response_json as json) from cv_parsing_details
where id=cpd.id and parsing_response_json is not null);
--Update email
update cv_parsing_details cpd set email=(select parsed_text ->> 'Email' as email from cv_parsing_details where id=cpd.id and parsed_text is not null);
--update jobId
update cv_parsing_details cpd set job_id = (select cast(SPLIT_PART(cv_file_name, '_', 2) as integer) from cv_parsing_details where id=cpd.id);
--update jcmId
update cv_parsing_details set job_candidate_mapping_id = jcm.id from job_candidate_mapping jcm where cv_parsing_details.job_id = jcm.job_id
and cv_parsing_details.email = jcm.email and cv_parsing_details.job_candidate_mapping_id is null and cv_parsing_details.email is not null;
--Drop supportive columns from cv_parsing_details table
alter table CV_PARSING_DETAILS
drop COLUMN PARSED_TEXT,
drop COLUMN JOB_ID,
drop COLUMN EMAIL;

--For ticket #185

insert into MASTER_DATA (TYPE, VALUE)
values
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

update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Bachelor of Arts (BA)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'BA');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Company Secretary (CS)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'CS');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Bachelors of Ayurveda Medicine & Surgery (BAMS)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'BAMS');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Bachelor of Architecture (BArch)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'BArch');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Bachelor of Business Administration (BBA)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'BBA');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Bachelor of Business Management (BBM)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'BBM');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Bachelors of Business Studies (BBS)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'BBS');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Bachelor of Computer Applications (BCA)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'BCA');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Bachelor of Commerce in Computer Application (BCCA)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'BCCA');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Business Capacity Management (BCM)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'BCM');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Bachelor of Commerce (BCom)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'BCom');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Bachelor of Computer Science (BCS)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'BCS');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Bachelor of Design (BDes)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'BDes');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Bachelor of Dental Science (BDS)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'BDS');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Bachelor of Engineering (BE)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'BE');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Bachelor of Education (BEd)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'BEd');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Bachelor in Fine Arts (BFA)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'BFA');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Bachelor in Foreign Trade (BFT)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'BFT');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Bachelor of Hotel Management (BHM)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'BHM');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Bachelor of Homeopathic Medicine and Surgery (BHMS)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'BHMS');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Bachelor of Information Technology (BIT)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'BIT');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Bachelor of Marketing Management (BMM)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'BMM');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Bachelor in Management Studies (BMS)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'BMS');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Bachelor of Pharmacy (BPharma)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'BPharma');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Bachelor of Science (BSc)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'BSc');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Bachelor of  Science in Software Engineering (BSSE)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'BSSE');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Bachelor of Technology. (BTech)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'BTech');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Bachelor of Veterinary Science (BVSc)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'BVSc');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Commercial Art(Commercial Art)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'Commercial Art');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Diploma (Diploma)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'Diploma');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Diploma in Graphics & Animation (Diploma in Graphics & Animation)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'Diploma in Graphics & Animation');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Fashion/Designing (Fashion/Designing)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'Fashion/Designing');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Graduate Diploma in Business Administration (GDBA)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'GDBA');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Higher Secondary School Certificate (HSC)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'HSC');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Bachelor of Law (LLB)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'LLB');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Bachelor in Medicine and Bachelor of Surgery (MBBS)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'MBBS');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Vocational-Training (Vocational Training)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'Vocational Training');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Association of Chartered Certified Accountants (ACCA)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'ACCA');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Chartered Accountant (CA)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'CA');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Chartered Accountant Integrated Professional Competence Course (CA IPCC)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'CA IPCC');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Chartered Financial Accountant  (CFA)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'CFA');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Cost and Works Accountancy (CWA)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'CWA');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Executive Post Graduate Diploma in Business Management (EMBA)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'EMBA');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Fellow of Chartered Accountants (FCA)'') WHERE EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'FCA');
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
UPDATE JOB SET EDUCATION = (SELECT ID FROM MASTER_DATA WHERE VALUE = 'Master's in Diploma in Business Administration (MDBA)') WHERE EDUCATION = (select ID from MASTER_DATA where VALUE = 'MDBA');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Management Development Programmes (MDP)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'MDP');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Masters of Engineering (ME)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'ME');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Master of Education (MEd)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'MEd');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Mechanical, Electrical and Plumbing (MEP)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'MEP');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Masters in Fine Arts (MFA)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'MFA');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Masters of finance and control (MFC)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'MFC');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Master of Financial Management (MFM)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'MFM');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Masters in Management Studies (MMS)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'MMS');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Master of Pharmacy (MPharma)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'MPharma');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Masters of Philosophy (MPhil)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'MPhil');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Master of Personnel Management (MPM)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'MPM');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Masters of Science (MS)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'MS');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Master of Science in Computer Science (MS CS)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'MS CS');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Masters in Industrial Psychology (MS in Industrial Psychology)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'MS in Industrial Psychology');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Masters of Surgery / Doctor of Medicine (MS/MD)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'MS/MD');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Master of Science (MSc)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'MSc');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Master of Social Work (MSW)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'MSW');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Master of Technology (MTech)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'MTech');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Master of Veterinary Science (MVSc)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'MVSc');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Other (Other)' and type='education') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'Other' and type='education');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Post Graduate Diploma (PG Diploma)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'PG Diploma');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Post Graduate Certification in Business Management (PGCBM)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'PGCBM');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Post Graduate Certificate in Human Resource Management (PGCHRM)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'PGCHRM');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Post Graduate Diploma in Analytical Chemistry (PGDAC)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'PGDAC');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Post Graduate Diploma in Business Administration (PGDBA)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'PGDBA');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Post Graduate Diploma in Computer Application (PGDCA)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'PGDCA');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Post Graduate Programme in Management (PGM)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'PGM');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Post Graduate Program (PGP)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'PGP');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Post Graduate Programme in Business Management (PGPBM)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'PGPBM');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Doctor Of Philosophy (PhD)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'PHD');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Public Relations / Advertising (PR/Advertising)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'PR/Advertising');
update JOB set EDUCATION = (select ID from MASTER_DATA where VALUE = 'Tourism (Tourism)') where EDUCATION = (select ID from MASTER_DATA where VALUE = 'Tourism');

--For ticket  #220
create TABLE CURRENCY (
ID serial PRIMARY KEY NOT NULL,
CURRENCY_FULL_NAME varchar(25),
CURRENCY_SHORT_NAME varchar(5),
COUNTRY varchar(5)
);

insert into public.currency(currency_full_name, currency_short_name, country) VALUES
('Australian Dollar', 'AUD', 'au'),
('Canadian Dollar', 'CAD', 'ca'),
('Indian Rupee', 'INR', 'in'),
('Singapore Dollar', 'SGD', 'sg'),
('Pound Sterling', 'GBP', 'gb'),
('US Dollar', 'USD', 'us');

--For ticket #227
update CONFIGURATION_SETTINGS
set CONFIG_VALUE = 5000
where CONFIG_NAME = 'cvRatingTimeout';

--Add unique constraint for jcm id in cv_rating table
alter table CV_RATING
add CONSTRAINT UNIQUE_CV_RATING_JCM UNIQUE(JOB_CANDIDATE_MAPPING_ID);

--Delete duplicate records from cvRatings
delete from cv_rating_skill_keyword_details
where cv_rating_skill_keyword_details.cv_rating_id in (
select a.id from cv_rating a, cv_rating b
where a.id < b.id and a.job_candidate_mapping_id = b.job_candidate_mapping_id);

delete from cv_rating a USING cv_rating b
WHERE a.id < b.id AND a.job_candidate_mapping_id = b.job_candidate_mapping_id;

--For ticket #236
alter table CANDIDATE_EDUCATION_DETAILS
alter COLUMN DEGREE TYPE VARCHAR(100);

--For update cv_rating_api_flag in cvParsingDetails duplicate jobCandidateMapping id
update cv_parsing_details set cv_rating_api_flag = true where job_candidate_mapping_id in(
select a.job_candidate_mapping_id from cv_parsing_details a, cv_parsing_details b
where a.id < b.id and a.job_candidate_mapping_id = b.job_candidate_mapping_id);

--For ticket #234
alter table USERS
add COLUMN USER_TYPE varchar(15) default 'Recruiting';

--For ticket #232
alter table COMPANY
add COLUMN COMPANY_TYPE VARCHAR(15) DEFAULT 'Individual' NOT NULL,
ADD COLUMN RECRUITMENT_AGENCY_ID INTEGER REFERENCES COMPANY(ID);

--Add Unique constraint in Jcm
alter table JOB_CANDIDATE_MAPPING
add CONSTRAINT unique_job_candidate UNIQUE(JOB_ID, CANDIDATE_ID);

--For ticket #230
alter table USERS
    alter COLUMN ROLE TYPE VARCHAR(17);

--For ticket #246
alter table USERS
add COLUMN COMPANY_ADDRESS_ID INTEGER REFERENCES COMPANY_ADDRESS(ID),
ADD COLUMN COMPANY_BU_ID INTEGER REFERENCES COMPANY_BU(ID);

--https://github.com/hexagonsearch/litmusblox-scheduler/issues/16
--Clear all timestamps in the jcm_communication_details table if the chat_invite_flag is false
update jcm_communication_details set chat_invite_timestamp_sms = null, chat_incomplete_reminder_1_timestamp_sms = null, chat_incomplete_reminder_2_timestamp_sms = null, link_not_visited_reminder_1_timestamp_sms = null, link_not_visited_reminder_2_timestamp_sms = null, chat_complete_timestamp_sms = null, chat_invite_timestamp_email = null, chat_incomplete_reminder_1_timestamp_email = null, chat_incomplete_reminder_2_timestamp_email = null, link_not_visited_reminder_1_timestamp_email = null, link_not_visited_reminder_2_timestamp_email = null, chat_complete_timestamp_email = null where chat_invite_flag = false;

-- For ticket #241 - update all candidate source to naukri where candidate source is plugin
update job_candidate_mapping set candidate_source= 'Naukri' where candidate_source='Plugin';



--For ticket #224
drop table IF EXISTS STAGE_MASTER;
create TABLE STAGE_MASTER (
ID serial PRIMARY KEY NOT NULL,
STAGE_NAME varchar(15) NOT NULL,
CONSTRAINT UNIQUE_STAGE_NAME UNIQUE(STAGE_NAME)
);

drop table IF EXISTS STEPS_PER_STAGE;
create TABLE STEPS_PER_STAGE(
ID serial PRIMARY KEY NOT NULL,
STAGE_ID integer REFERENCES STAGE_MASTER(ID) NOT NULL,
STEP_NAME varchar(15) NOT NULL,
CONSTRAINT UNIQUE_STAGE_STEP UNIQUE(STAGE_ID, STEP_NAME)
);

insert into STAGE_MASTER(ID, STAGE_NAME) values
(1, 'Source'),
(2, 'Screen'),
(3, 'Resume Submit'),
(4, 'Interview'),
(5, 'Make Offer'),
(6, 'Offer'),
(7, 'Join');

insert into STEPS_PER_STAGE (STAGE_ID, STEP_NAME) values
(1, 'Source'),
(2, 'Screen'),
(3, 'Resume Submit'),
(4, 'L1'),
(4, 'L2'),
(4, 'L3'),
(5, 'Make Offer'),
(6, 'Offer'),
(7, 'Join');

alter table COMPANY_STAGE_STEP drop CONSTRAINT company_stage_step_stage_fkey;
alter table COMPANY_STAGE_STEP add CONSTRAINT company_stage_step_stage_fkey FOREIGN KEY (STAGE) REFERENCES STAGE_MASTER(ID);

-- populate company stage step for all existing companies
insert into COMPANY_STAGE_STEP (COMPANY_ID, STAGE, STEP, CREATED_ON, CREATED_BY)
 select COMPANY.ID, STAGE_ID, STEP_NAME, COMPANY.CREATED_ON ,COMPANY.CREATED_BY
 from COMPANY, STEPS_PER_STAGE order by COMPANY.ID;

-- create table to store job specific
 create TABLE JOB_STAGE_STEP(
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
insert into JOB_STAGE_STEP(JOB_ID, STAGE_STEP_ID, CREATED_BY, CREATED_ON)
 select JOB.ID, COMPANY_STAGE_STEP.ID, JOB.CREATED_BY, JOB.DATE_PUBLISHED
 from JOB, COMPANY_STAGE_STEP
 where JOB.COMPANY_ID = COMPANY_STAGE_STEP.COMPANY_ID
 and JOB.DATE_PUBLISHED is not null
 order by JOB.ID;

 -- add columns to job_candidate_mapping table to hold job_stage_step_id and flag indicating candidate rejection
alter table JOB_CANDIDATE_MAPPING
add COLUMN REJECTED BOOL NOT NULL DEFAULT 'f';

alter table JOB_CANDIDATE_MAPPING drop CONSTRAINT job_candidate_mapping_stage_fkey;
alter table JOB_CANDIDATE_MAPPING alter COLUMN STAGE drop NOT NULL;
alter table JOB_CANDIDATE_MAPPING add CONSTRAINT job_candidate_mapping_stage_fkey FOREIGN KEY (STAGE) REFERENCES JOB_STAGE_STEP(ID);

--clear old dirty data of jcm related to 'Draft' jobs
delete from JCM_COMMUNICATION_DETAILS
where JCM_ID in (select ID from JOB_CANDIDATE_MAPPING where JOB_ID in (select ID from JOB where STATUS = 'Draft'));

delete from CANDIDATE_SCREENING_QUESTION_RESPONSE
where JOB_CANDIDATE_MAPPING_ID in (select ID from JOB_CANDIDATE_MAPPING where JOB_ID in (select ID from JOB where STATUS = 'Draft'));

delete from CANDIDATE_TECH_RESPONSE_DATA
where JOB_CANDIDATE_MAPPING_ID in (select ID from JOB_CANDIDATE_MAPPING where JOB_ID in (select ID from JOB where STATUS = 'Draft'));

delete from JOB_CANDIDATE_MAPPING
where JOB_ID in (select ID from JOB where STATUS = 'Draft');

-- set rejected flag to false for all existing records
update JOB_CANDIDATE_MAPPING
set REJECTED = 'F';

-- set the stage to 'Source' for all existing jcm records
update JOB_CANDIDATE_MAPPING
set STAGE = (
 select JOB_STAGE_STEP.ID
 from JOB_STAGE_STEP
 where JOB_STAGE_STEP.JOB_ID = JOB_CANDIDATE_MAPPING.JOB_ID
 and STAGE_STEP_ID = (
  select COMPANY_STAGE_STEP.ID from COMPANY_STAGE_STEP, JOB
  where COMPANY_STAGE_STEP.STEP = 'Source'
  and COMPANY_STAGE_STEP.COMPANY_ID = JOB.COMPANY_ID
  and JOB.ID = JOB_CANDIDATE_MAPPING.JOB_ID
 )
);

alter table JOB_CANDIDATE_MAPPING alter COLUMN STAGE SET NOT NULL;

-- modify history table
alter table JCM_HISTORY
add COLUMN STAGE INTEGER REFERENCES JOB_STAGE_STEP(ID);

update JCM_HISTORY
set STAGE = (
  select JOB_STAGE_STEP.ID
  from JOB_STAGE_STEP, JOB_CANDIDATE_MAPPING
  where JCM_HISTORY.JCM_ID = JOB_CANDIDATE_MAPPING.ID
  and JOB_STAGE_STEP.JOB_ID = JOB_CANDIDATE_MAPPING.JOB_ID
  and STAGE_STEP_ID = (
    select COMPANY_STAGE_STEP.ID
    from COMPANY_STAGE_STEP, JOB
    where COMPANY_STAGE_STEP.STEP = 'Source'
    and COMPANY_STAGE_STEP.COMPANY_ID = JOB.COMPANY_ID
    and JOB.ID = JOB_CANDIDATE_MAPPING.JOB_ID
  )
);

alter table JCM_HISTORY alter COLUMN STAGE SET NOT NULL;

delete from MASTER_DATA where TYPE = 'stage';

--For ticket #247
alter table JOB_HIRING_TEAM
drop COLUMN STAGE_STEP_ID;

alter table JOB_HIRING_TEAM
add COLUMN STAGE_STEP_ID INTEGER REFERENCES JOB_STAGE_STEP(ID) NOT NULL;

--For ticket #257
alter table MASTER_DATA
alter COLUMN VALUE_TO_USE TYPE VARCHAR (20);

insert into public.master_data(type, value, value_to_use) VALUES
('role', 'HR Recruiter', 'Recruiter'),
('role', 'HR Head', 'ClientAdmin'),
('role', 'Admin', 'ClientAdmin'),
('role', 'Hiring Manager', 'BusinessUser'),
('role', 'Interviewer', 'BusinessUser');

--For ticket #262
update CREATE_JOB_PAGE_SEQUENCE set PAGE_DISPLAY_ORDER = 2, PAGE_DISPLAY_NAME = 'Screening' where PAGE_NAME = 'screeningQuestions';
update CREATE_JOB_PAGE_SEQUENCE set PAGE_DISPLAY_ORDER = 3 where PAGE_NAME = 'expertise';
update CREATE_JOB_PAGE_SEQUENCE set PAGE_DISPLAY_ORDER = 4 where PAGE_NAME = 'keySkills';
update CREATE_JOB_PAGE_SEQUENCE set PAGE_DISPLAY_ORDER = 5 where PAGE_NAME = 'capabilities';
update CREATE_JOB_PAGE_SEQUENCE set PAGE_DISPLAY_ORDER = 6, DISPLAY_FLAG = 'true' where PAGE_NAME = 'hiringTeam';
update CREATE_JOB_PAGE_SEQUENCE set PAGE_DISPLAY_ORDER = 7 where PAGE_NAME = 'preview';
update CREATE_JOB_PAGE_SEQUENCE set PAGE_DISPLAY_ORDER = 8 where PAGE_NAME = 'jobDetail';

--For ticket  #268
alter table JOB_CANDIDATE_MAPPING
add COLUMN ALTERNATE_EMAIL VARCHAR (50),
ADD COLUMN ALTERNATE_MOBILE VARCHAR (15),
ADD COLUMN SERVING_NOTICE_PERIOD BOOL NOT NULL DEFAULT 'f',
ADD COLUMN NEGOTIABLE_NOTICE_PERIOD BOOL NOT NULL DEFAULT 'f',
ADD COLUMN OTHER_OFFERS BOOL NOT NULL DEFAULT 'f',
ADD COLUMN UPDATE_RESUME BOOL NOT NULL DEFAULT 'f',
ADD COLUMN COMMUNICATION_RATING SMALLINT DEFAULT 0;

alter table CANDIDATE_DETAILS
add COLUMN RELEVANT_EXPERIENCE NUMERIC (4, 2);


-- FOR TICKET #258
drop table IF EXISTS EXPORT_FORMAT_DETAIL;
drop table IF EXISTS EXPORT_FORMAT_MASTER;
create TABLE EXPORT_FORMAT_MASTER(
ID serial PRIMARY KEY NOT NULL,
COMPANY_ID integer REFERENCES COMPANY(ID) DEFAULT NULL,
FORMAT varchar(15) NOT NULL,
SYSTEM_SUPPORTED BOOL DEFAULT FALSE
);

create TABLE EXPORT_FORMAT_DETAIL(
    ID serial PRIMARY KEY NOT NULL,
    FORMAT_ID integer REFERENCES EXPORT_FORMAT_MASTER(ID) NOT NULL,
    COLUMN_NAME VARCHAR(20),
    HEADER VARCHAR(20),
    POSITION SMALLINT,
    UNIQUE(FORMAT_ID, POSITION)
);

insert into export_format_master
(format, system_supported)
values
('All Data', true);

insert into export_format_detail
(format_id, column_name, header,  "position")
values
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
update STAGE_MASTER set STAGE_NAME='Sourcing' where STAGE_NAME = 'Source';
update STAGE_MASTER set STAGE_NAME='Screening' where STAGE_NAME = 'Screen';
update STAGE_MASTER set STAGE_NAME='Submitted' where STAGE_NAME = 'Resume Submit';
update STAGE_MASTER set STAGE_NAME='Hired' where STAGE_NAME = 'Join';

update STEPS_PER_STAGE set STEP_NAME='Sourcing' where STEP_NAME = 'Source';
update STEPS_PER_STAGE set STEP_NAME='Screening' where STEP_NAME = 'Screen';
update STEPS_PER_STAGE set STEP_NAME='Submitted' where STEP_NAME = 'Resume Submit';
update STEPS_PER_STAGE set STEP_NAME='Hired' where STEP_NAME = 'Join';

update COMPANY_STAGE_STEP set STEP='Sourcing' where STEP = 'Source';
update COMPANY_STAGE_STEP set STEP='Screening' where STEP = 'Screen';
update COMPANY_STAGE_STEP set STEP='Submitted' where STEP = 'Resume Submit';
update COMPANY_STAGE_STEP set STEP='Hired' where STEP = 'Join';

--For ticket #276
alter table CANDIDATE_COMPANY_DETAILS
alter COLUMN COMPANY_NAME TYPE VARCHAR(75);

alter table CANDIDATE_PROJECT_DETAILS
alter COLUMN COMPANY_NAME TYPE VARCHAR(75);

--For ticket #267
update cv_parsing_details set cv_rating_api_flag = false where job_candidate_mapping_id not in (select job_candidate_mapping_id from cv_rating)
and processing_status = 'Success' and cv_rating_api_flag is true;

--For ticket #268
insert into MASTER_DATA (TYPE, VALUE) values
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

alter table JOB_CANDIDATE_MAPPING
add COLUMN REASON_FOR_CHANGE VARCHAR(100);

--For ticket #284
alter table COMPANY_ADDRESS
drop CONSTRAINT company_address_address_title_key;

alter table COMPANY_ADDRESS
add CONSTRAINT UNIQUE_COMPANY_ADDRESS_TITLE UNIQUE(COMPANY_ID, ADDRESS_TITLE);

--For ticket #289
insert into MASTER_DATA(TYPE, VALUE) values
('callOutCome', 'Connected'),
('callOutCome', 'No Answer'),
('callOutCome', 'Busy'),
('callOutCome', 'Wrong Number'),
('callOutCome', 'Left Message/VoiceMail');

alter table JCM_HISTORY
add COLUMN CALL_LOG_OUTCOME VARCHAR(25),
ADD COLUMN SYSTEM_GENERATED BOOL DEFAULT 't' NOT NULL;

alter table JCM_HISTORY
rename COLUMN DETAILS TO COMMENT;

alter table JCM_HISTORY
alter COLUMN COMMENT TYPE TEXT;

--For ticket #28
update SMS_TEMPLATES set  TEMPLATE_CONTENT = 'Oh no [[${commBean.receiverfirstname}]]!  The Litmus Profile you started creating for the [[${commBean.jobtitle}]] job at [[${commBean.sendercompany}]] was left incomplete. It''s important that you finish the profile to be considered for the job. Continue from where you left last. Just click the link to continue. [[${commBean.chatlink}]]'  where TEMPLATE_NAME = 'ChatIncompleteReminder1';

--For ticket #255
alter table JOB
add COLUMN JOB_REFERENCE_ID UUID NOT NULL DEFAULT uuid_generate_v1();

--From ML get capability_name length 50
alter table JOB_CAPABILITIES
alter COLUMN CAPABILITY_NAME TYPE VARCHAR(50);

--For ticket #301
insert into MASTER_DATA(TYPE, VALUE) values
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
alter table JOB
add COLUMN JOB_TYPE INTEGER REFERENCES MASTER_DATA(ID);

update JOB
set JOB_TYPE = (select ID from MASTER_DATA where TYPE = 'jobType' and VALUE = 'Full Time');

alter table JOB
alter COLUMN JOB_TYPE SET NOT NULL;

drop table JOB_DETAILS;

alter table COMPANY_ADDRESS
add COLUMN CITY VARCHAR(100),
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
create view jobDetailsView as
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
alter table JOB_CANDIDATE_MAPPING
add COLUMN CV_FILE_TYPE VARCHAR (10);

--Migrate all cvTypes from candidate detail table to job candidate mapping table
update JOB_CANDIDATE_MAPPING AS JCM
SET CV_FILE_TYPE = CD.CV_FILE_TYPE
FROM CANDIDATE_DETAILS AS CD
where JCM.CANDIDATE_ID = CD.CANDIDATE_ID and CD.CV_FILE_TYPE is not null;

alter table CANDIDATE_DETAILS
drop COLUMN CV_FILE_TYPE;

--For ticket #311
create TABLE EMPLOYEE_REFERRER (
ID serial PRIMARY KEY NOT NULL,
FIRST_NAME VARCHAR (45) NOT NULL,
LAST_NAME VARCHAR (45) NOT NULL,
EMAIL VARCHAR(50) NOT NULL UNIQUE,
EMPLOYEE_ID VARCHAR(10) NOT NULL,
MOBILE VARCHAR (15) NOT NULL,
LOCATION VARCHAR(50) NOT NULL,
CREATED_ON TIMESTAMP NOT NULL
);

create TABLE CANDIDATE_REFERRAL_DETAIL(
ID serial PRIMARY KEY NOT NULL,
JOB_CANDIDATE_MAPPING_ID INTEGER REFERENCES JOB_CANDIDATE_MAPPING(ID) NOT NULL,
EMPLOYEE_REFERRER_ID INTEGER REFERENCES EMPLOYEE_REFERRER(ID) NOT NULL,
REFERRER_RELATION INTEGER REFERENCES MASTER_DATA(ID) NOT NULL,
REFERRER_CONTACT_DURATION SMALLINT NOT NULL
);

alter table JOB_CANDIDATE_MAPPING
alter COLUMN CANDIDATE_SOURCE TYPE VARCHAR(17);


insert into public.users(
 email, first_name, last_name, mobile, company_id, role, status, country_id, created_on)
	VALUES ('systemuser@hex.com', 'System', 'User','1234567890',
			(select id from company where company_name= 'LitmusBlox'),'BusinessUser','New', 3, now());


-- Increase address length #329
alter table company_address alter COLUMN address type VARCHAR(300);

--Increase designation length #337
alter table CANDIDATE_COMPANY_DETAILS alter COLUMN DESIGNATION TYPE VARCHAR(100);

-- Additional column for company_short_name
alter table COMPANY
add COLUMN SHORT_NAME VARCHAR(8) UNIQUE;

--For ticket #344
alter table CV_PARSING_DETAILS
add COLUMN CV_CONVERT_API_FLAG BOOL NOT NULL DEFAULT 'f';

--adding flag to identify if job has screening question or not
alter table Job add COLUMN HR_QUESTION_AVAILABLE BOOL NOT NULL DEFAULT 'f';
update job set hr_question_available = 't' where id in (select distinct job_id from job_screening_questions);

--additional column for tricentis requirement. ticket #346
alter table Job add COLUMN RESUBMIT_HR_CHATBOT BOOL NOT NULL DEFAULT 'f';

update Job set RESUBMIT_HR_CHATBOT='f';


alter table export_format_detail alter COLUMN column_name TYPE VARCHAR(25), alter COLUMN header TYPE VARCHAR(25);

delete from export_format_detail where format_id=(select id from export_format_master where format='All Data');
insert into export_format_detail
(format_id, column_name, header,  "position")
values
(1, 'candidateName','Candidate Name', 1),
(1, 'chatbotStatus','Chatbot Status', 2),
(1, 'chatbotFilledTimeStamp', 'Chatbot Filled Timestamp', 3),
(1, 'currentStage','Stage', 4),
(1, 'keySkillsStrength','Key Skills Strength', 5),
(1, 'currentCompany','Current Company', 6),
(1, 'currentDesignation','Current Designation', 7),
(1, 'email','Email', 8),
(1, 'countryCode','Country Code', 9),
(1, 'mobile','Mobile', 10),
(1, 'totalExperience','Total Experience', 11),
(1, 'createdBy','Created By', 12);


alter table JCM_COMMUNICATION_DETAILS rename COLUMN CHAT_COMPLETE_FLAG TO TECH_CHAT_COMPLETE_FLAG;

update job_candidate_mapping
set chatbot_status='Invited'
where id in (select jcm_id from jcm_communication_details where jcm_communication_details.chat_invite_flag='t') and
chatbot_status is null and candidate_interest='f';

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
alter table COMPANY alter COLUMN SHORT_NAME TYPE VARCHAR(25);

--For ticket #350
alter table COMPANY
add COLUMN SUBDOMAIN_CREATED BOOL NOT NULL DEFAULT 'f',
add COLUMN SUBDOMAIN_CREATED_ON TIMESTAMP;

-- For ticket #333
alter table CV_PARSING_DETAILS
add COLUMN PARSING_RESPONSE_ML TEXT,
add COLUMN PARSING_RESPONSE_PYTHON TEXT;

--Before use
create EXTENSION IF NOT EXISTS hstore;

create TABLE CV_PARSING_API_DETAILS(
ID serial PRIMARY KEY NOT NULL,
API_URL VARCHAR (255) NOT NULL,
API_SEQUENCE SMALLINT NOT NULL,
ACTIVE BOOL DEFAULT TRUE,
COLUMN_TO_UPDATE VARCHAR (25) NOT NULL,
QUERY_ATTRIBUTES HSTORE,
CONSTRAINT UNIQUE_API_URL UNIQUE(API_URL),
CONSTRAINT UNIQUE_API_SEQUENCE UNIQUE(API_SEQUENCE)
);

insert into CV_PARSING_API_DETAILS (API_URL, API_SEQUENCE, ACTIVE, COLUMN_TO_UPDATE, QUERY_ATTRIBUTES) values
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
create TABLE CUSTOMIZED_CHATBOT_PAGE_CONTENT(
ID serial PRIMARY KEY NOT NULL,
COMPANY_ID INTEGER REFERENCES COMPANY(ID) NOT NULL,
PAGE_INFO hstore NOT NULL,
CONSTRAINT UNIQUE_PAGE_INFO_COMPANY UNIQUE(COMPANY_ID)
);

alter table JOB
add COLUMN CUSTOMIZED_CHATBOT bool default 'f' NOT NULL;

insert into CUSTOMIZED_CHATBOT_PAGE_CONTENT (COMPANY_ID, PAGE_INFO) values
(80, '"introText" => "text", "showCompanyLogo" => true, "thankYouText" => "<p>text to be displayed</p>", "showFollowSection" => true');

alter table USER_SCREENING_QUESTION
alter COLUMN QUESTION TYPE VARCHAR(250),
alter COLUMN OPTIONS TYPE VARCHAR(200)[];

-- to update custom chatbot detail for tricentis.
update CUSTOMIZED_CHATBOT_PAGE_CONTENT set PAGE_INFO='"introText"=>"Automation premier League requires you to get tested on", "thankYouText"=>"The sore of your test will be communicated to you via email tomorrow from tricentis_apl@litmusblox.io", "showCompanyLogo"=>"false", "showFollowSection"=>"false", "showProceedButton"=>"true", "showConsentPage"=>"false"' where company_id=43;

--For ticket #389
update MASTER_DATA set VALUE = 'Left Message or Voicemail' where VALUE = 'Left Message/VoiceMail';

--For ticket #373
create TABLE STAGE_STEP_MASTER (
ID serial PRIMARY KEY NOT NULL,
STAGE varchar(15) NOT NULL,
STEP varchar(25) NOT NULL,
CONSTRAINT UNIQUE_STAGE_STEP_KEY UNIQUE(STAGE, STEP)
);

insert into STAGE_STEP_MASTER(STAGE, STEP)
values ('Sourcing','Sourcing'),
('Screening','Screening'),
('Submitted','Submitted'),
('Interview','L1'),
('Make Offer','Make Offer'),
('Offer','Offer'),
('Hired','Hired');

alter table JCM_HISTORY drop CONSTRAINT jcm_history_stage_fkey;
alter table JOB_CANDIDATE_MAPPING drop CONSTRAINT job_candidate_mapping_stage_fkey;
alter table JOB_HIRING_TEAM drop CONSTRAINT job_hiring_team_stage_step_id_fkey;

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

alter table job_candidate_mapping
add CONSTRAINT job_candidate_mapping_stage_step_fkey FOREIGN KEY(stage) REFERENCES stage_step_master(id);

alter table job_hiring_team
add CONSTRAINT job_hiring_team_stage_step_fkey FOREIGN KEY(stage_step_id) REFERENCES stage_step_master(id);

alter table jcm_history
add CONSTRAINT jcm_history_stage_step_fkey FOREIGN KEY(stage) REFERENCES stage_step_master(id);

drop table company_stage_step, job_stage_step, steps_per_stage, stage_master;

--updated view for export data
drop view if exists exportDataView;
create view exportDataView as
select
	jcm.job_id as jobId,
	concat(jcm.candidate_first_name, ' ', jcm.candidate_last_name) as candidateName,
	jcm.chatbot_status as chatbotStatus,
	jcm.chatbot_updated_on as chatbotFilledTimeStamp,
	cvr.overall_rating as keySkillsStrength,
	ssm.stage as currentStage,
	currentCompany.company_name as currentCompany,
	currentCompany.designation as currentDesignation,
	jcm.email,
	jcm.country_code as countryCode,
	jcm.mobile,
	cd.total_experience as totalExperience,
	concat(users.first_name, ' ', users.last_name) as createdBy,
	jcm.created_on as createdOn,
	jcm.score as capabilityScore,
	jsq.jsqId as jsqId,
	jsq.ScreeningQn as screeningQuestion,
	csqr.response as candidateResponse
	from job_candidate_mapping jcm
	left join cv_rating cvr on cvr.job_candidate_mapping_id = jcm.id
	left join (
		select candidate_id, company_name, designation from candidate_company_details where id in (
				select min(id) from candidate_company_details
				group by candidate_id
			)
	) as currentCompany on jcm.candidate_id = currentCompany.candidate_id
	left join candidate_details cd on cd.candidate_id = jcm.candidate_id
	inner join users on users.id = jcm.created_by
	inner join stage_step_master ssm on ssm.id=jcm.stage
	left join (
		select jsq.id as jsqId, job_id jsqJobId , question as ScreeningQn from job_screening_questions jsq inner join screening_question msq on jsq.master_screening_question_id = msq.id
		union
		select jsq.id as jsqId, job_id jsqJobId, question as ScreeningQn from job_screening_questions jsq inner join user_screening_question usq on jsq.user_screening_question_id=usq.id
		union
		select jsq.id as jsqId, job_id jsqJobId, question as ScreeningQn from job_screening_questions jsq inner join company_screening_question csq on csq.id = jsq.company_screening_question_id
	) as jsq on jsq.jsqJobId = jcm.job_id
	left join
	candidate_screening_question_response csqr on csqr.job_screening_question_id = jsq.jsqId and csqr.job_candidate_mapping_id = jcm.id order by jobId, candidateName, jsq.jsqId;


--For ticket #377
alter table COMPANY
add COLUMN COUNTRY_ID INTEGER REFERENCES COUNTRY(ID);

--Update existing record's
update COMPANY
set COUNTRY_ID = (select ID from COUNTRY where COUNTRY_NAME = 'India');

alter table COMPANY
alter COLUMN COUNTRY_ID SET NOT NULL;
update job_candidate_mapping set candidate_source = 'NaukriJobPosting' where candidate_source = 'NaukriMail';

insert into export_format_detail
(format_id, column_name, header, "position")
values
(1, 'createdOn','Created On', 13),
(1, 'capabilityScore', 'Capability Score', 14);


-- #42 litmusblox-chatbot
insert into CUSTOMIZED_CHATBOT_PAGE_CONTENT (COMPANY_ID, PAGE_INFO) values
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