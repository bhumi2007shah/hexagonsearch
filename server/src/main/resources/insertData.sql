delete from country;
INSERT INTO COUNTRY (COUNTRY_NAME, COUNTRY_CODE, MAX_MOBILE_LENGTH, COUNTRY_SHORT_CODE)
 VALUES
 ('Australia','+61', 9,'au'),
 ('Canada','+1', 10,'ca'),
 ('India','+91', 10,'in'),
 ('Singapore','+65', 8,'sg'),
 ('United Kingdom','+44', 10,'gb'),
 ('United States','+1', 10,'us');

-- following inserts are NOT to be run in test and prod environments
-- these will be used for unit testing purpose only
ALTER TABLE users DISABLE TRIGGER ALL;

-- password = 123456
insert into users (id, email, password, first_name, last_name, mobile, company_id, role, country_id, created_on, status)
values(1, 'test@litmusblox.io', '$2a$10$BwQoXoB2b9A9XE8Xc2KQbOdTGWVXYQ3QiiklqZBi/nYSRzvCPfJo.', 'Lb', 'Test', '9090909090', 2, 'Recruiter', 3, current_date, 'Active');

insert into company (id, company_name, created_on, created_by)
values(2, 'LB', current_date, 1);

ALTER TABLE users ENABLE TRIGGER ALL;
-- end of inserts only for unit tests


delete from master_data;
INSERT INTO MASTER_DATA (TYPE, VALUE)
VALUES
 ('education', 'Association of Chartered Certifed Accountants (ACCA)'),
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
 ('education', 'Chartered Aaccountant Integrated Professional Competence Course (CA IPCC)'),
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
 ('education', 'Journalism/Mass Comunication (Journalism/Mass Comm.)'),
 ('education', 'Masters in Information Management (MIM)'),
 ('education', 'Master of Law (LLM)'),
 ('education', 'Master of Arts (MA)'),
 ('education', 'Master of Architecture (MArch)'),
 ('education', 'Master of Business Administration (MBA)'),
 ('education', 'Master of Business Management (MBM)'),
 ('education', 'Master of Computer Applications (MCA)'),
 ('education', 'Master of Computer Management (MCM)'),
 ('education', 'Master of Commerce (MCom)'),
 ('education', 'Masters in Diploma in Business Administration (MDBA)'),
 ('education', 'Management Development Programmes (MDP)'),
 ('education', 'Masters of Engineering (ME)'),
 ('education', 'Master of Education (MEd)'),
 ('education', 'Mechanical, Electrical and Plumbing (MEP)'),
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
 ('education', 'Post Graduate Diploma (PG Diploma)'),
 ('education', 'Post Graduate Certification in Business Management (PGCBM)'),
 ('education', 'Postgraduate Certificate in Human Resource Management (PGCHRM)'),
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
 ('education', 'Other (Other)'),

 ( 'questionType','Checkbox'),
 ( 'questionType','Radio button'),
 ( 'questionType','InputBox'),
 ( 'questionType','Location'),
 ( 'questionType','Slider'),
 ( 'questionType','Calendar'),

 ( 'addressType','Job Location'),
 ( 'addressType','Interview Location'),
 ( 'addressType','Both'),

 ( 'process','Hiring Manager'),
 ( 'process','Lead Recruiter'),
 ( 'process','Sourcing'),
 ( 'process','Screening'),
 ( 'process','Interview'),
 ( 'process','Offer Management'),

 ( 'noticePeriod','0'),
 ( 'noticePeriod','15'),
 ( 'noticePeriod','30'),
 ( 'noticePeriod','60'),
 ( 'noticePeriod','45'),
 ( 'noticePeriod','90'),
 ( 'noticePeriod','Others');


delete from configuration_settings;
-- max limits for various parameters
insert into CONFIGURATION_SETTINGS(CONFIG_NAME, CONFIG_VALUE) values
('maxScreeningQuestionsLimit',50),
('dailyCandidateUploadPerUserLimit',500),
('dailyCandidateInviteLimit',500),
('candidatesPerFileLimit',100),
('sendEmail',1),
('sendSms',1),
('maxCvFiles',20),
('mlCall',1),
('maxCapabilities',10),
('cvRatingTimeout', 5000);


delete from skills_master;
 -- key skill master data
insert into skills_master (skill_name) values
('Core Java'),
('Advance Java'),
('J2EE'),
('Spring'),
('Hibernate'),
('Struts'),
('Swing'),
('MultiThreading'),
('Java Beans'),
('JSF'),
('JSP'),
('JDBC'),
('JMS'),
('Servlets'),
('Collections'),
('WebServices - Soap'),
('WebServices - Rest'),
('EJB'),
('Play'),
('JavaFX'),
('Netty'),
('Wicket'),
('JMX'),
('JAF'),
('JPA'),
('Ant'),
('Maven'),
('Gradle'),
('Tomcat'),
('JBoss'),
('GlassFish'),
('WebLogic'),
('TestNG'),
('Cactus'),
('EasyMock'),
('Mockito'),
('Cucumber'),
('Javascript - AngularJS'),
('Javascript - NodeJS'),
('Javascript - ExtJS'),
('Javascript - BackboneJS'),
('Javascript - DOJO'),
('Javascript - Jquery'),
('Javascript - EmberJS'),
('Javascript - ReactJS'),
('Javascript - KnockoutJS'),
('HTML'),
('HTML 5'),
('CSS'),
('Photoshop'),
('UX Designer'),
('CorelDraw'),
('Silverlight'),
('AJAX'),
('Javascript - Sails'),
('Javascript - Express'),
('Javascript - Grunt/Gulp'),
('Javascript - Brocolli'),
('Javascript - Webpack'),
('Javascript - Jasmine'),
('Javascript - Karma'),
('Javascript - Mocha'),
('.Net '),
('.Net - C#.Net'),
('.Net - VB.Net'),
('WCF'),
('WPF'),
('TFS'),
('VB'),
('MVC'),
('ADO'),
('Azure'),
('C#'),
('Sharepoint'),
('Excel'),
('TSQL'),
('SQL Server'),
('SQL Server SSRS'),
('SQL Server SSAS'),
('SQL Server SSIS'),
('TFS'),
('ASP'),
('Exchange'),
('ASP.NET Web API (2)'),
('ASP.NET MVC'),
('ASP.NET Web Forms (old school)'),
('nHibernate'),
('Entity Framework'),
('Linq2SQL'),
('ADO.NET'),
('nUnit'),
('SpecFlow'),
('MStest'),
('VC++'),
('STL'),
('Boost'),
('QT'),
('vxWidgets'),
('Loki'),
('POCO'),
('OpenCV'),
('VC'),
('Big Data - HBase'),
('Big Data - Hadoop'),
('Big Data - Hive'),
('Big Data - Spark'),
('Big Data - MapReduce'),
('Big Data - HDFS'),
('Big Data - MongoDB'),
('Oracle'),
('Oracle - BAM'),
('Oracle - Financials'),
('Oracle - Middleware'),
('Oracle - ORMB'),
('Oracle - SQL'),
('Oracle - Essbase'),
('MySQL'),
('NoSQL'),
('NoSQL - Cassandra'),
('NoSQL - MongoDB'),
('NoSQL - Berkeley DB'),
('NoSQL - Redis'),
('NoSQL - Riak'),
('NoSQL - CouchDB'),
('NoSQL - DynamoDB'),
('MySQL Stored Procedures'),
('MSSQL'),
('SQL'),
('DB2'),
('Postgresql'),
('Sybase'),
('Architecture'),
('Access'),
('SQLite'),
('Windows'),
('Linux'),
('RedHat RHEL'),
('Solaris'),
('Unix'),
('Android'),
('Mac/iOS'),
('Manual'),
('UAT'),
('Automation - Selenium'),
('Automation - Required'),
('Automation - Jasmine'),
('Automation - JUnit'),
('Automation - Protactor'),
('Automation - Silk Test'),
('Automation - Sahi Pro'),
('Automation - WebDriver'),
('Automation - SoapUI'),
('Automation - Watir'),
('Automation - Watin'),
('Automation - QTP'),
('Automation - Cucumber'),
('Automation - IBM Rational Functional Tester'),
('Automation - Appium'),
('Validation and Verification'),
('Performance - JMeter'),
('Performance - HP QC'),
('Performance - NeoLoad'),
('Performance - LoadRunner'),
('Performance - LoadUI'),
('Performance - Siege'),
('Performance - Gatling'),
('Infrastructure - SFTP'),
('Infrastructure - Vmware'),
('Infrastructure - SCCM'),
('Infrastructure - SCOM'),
('Infrastructure - Websence'),
('Infrastructure - Active Directory'),
('Infrastructure - Xendesktop'),
('Infrastructure - Netscalar'),
('Infrastructure - DNS'),
('Infrastructure - Web DMZ'),
('Infrastructure - ITIL'),
('Infrastructure - Application Packaging'),
('Application - JSB'),
('Application - Siebel'),
('Production'),
('Reporting - Regulatory Reporting'),
('Financial Planning and Analysis'),
('Trade Finance - Collateral Management'),
('Trade Finance'),
('Trade Finance - Settlement'),
('Risk - Market Risk'),
('Risk - Credit Risk'),
('CA'),
('Accounting'),
('PMO'),
('Fund Acounting'),
('Fund Acounting'),
('Internal Audit'),
('Finance Audit'),
('IT Audit'),
('NGO Audit'),
('HR'),
('ETL'),
('Informatica'),
('Pentaho'),
('Cognos'),
('Abinitio'),
('Business Objects'),
('Chef'),
('Puppet'),
('SVN'),
('Ansible'),
('Jenkins'),
('Artifactory'),
('Integration - Continuous'),
('Integration - Tools'),
('Nexus'),
('KVM'),
('Xen'),
('Xen'),
('VirtualBox'),
('Vagrant'),
('CFEngine'),
('SaltStack'),
('RANCID'),
('Ubuntu Juju'),
('RANCID'),
('New Relic'),
('Nagios'),
('Icinga'),
('Graphite'),
('Ganglia'),
('Cacti'),
('PagerDuty'),
('Sensu'),
('TCP/IP'),
('HTTP'),
('HTTP/2'),
('WebSockets'),
('Web Servers - Apache'),
('Web Servers - Tomcat'),
('Web Servers - IIS'),
('Web Servers - jBoss'),
('Scrum Master'),
('PMO'),
('PM'),
('Technical'),
('Business Analysis'),
('Perl'),
('Shell'),
('Python'),
('Ruby'),
('Bash'),
('AWK'),
('Python'),
('Ruby'),
('Lua'),
('Sinatra'),
('Padrino'),
('JRuby'),
('RSpec'),
('Capybara'),
('Watir'),
('Cucumber'),
('Passenger'),
('Capistrano'),
('Cake PHP'),
('Lucene'),
('Codeignitor'),
('Wordpress'),
('Joomla'),
('Symfony (2)'),
('Zend Framework (2)'),
('Yii'),
('Laravel'),
('Twig (templating)'),
('PHPUnit (testing)'),
('Doctrine (ORM)'),
('PHPStorm (IDE)'),
('Pear'),
('Smarty'),
('FICO'),
('ABAP'),
('HANA'),
('ME/MII'),
('Security'),
('MM'),
('SAP - Basis'),
('SAP - SCM'),
('GIT'),
('Subversion'),
('Endevor'),
('Mercurial'),
('SVN'),
('CVS'),
('Perforce'),
('Bitbucket'),
('Gitlab'),
('Goldensource'),
('Illustrator'),
('Flex'),
('CS'),
('Jasper/iReport'),
('Jira'),
('Pega'),
('TIBCO'),
('Tivoli'),
('Tableau'),
('TeamCity'),
('Murex'),
('Apache Kafka'),
('Memcached'),
('Nginx'),
('Varnish'),
('Squid'),
('haproxy'),
('CDN'),
('Big Data - Pig'),
('Big Data - Apache HBase'),
('Big Data - Cassandra'),
('Big Data - MapReduce'),
('Big Data - Spark'),
('TFS'),
('CruiseControl'),
('Bamboo'),
('Hudson'),
('Travis'),
('Mantis'),
('Confluence'),
('RedMine'),
('YouTrack'),
('Trello'),
('BaseCamp'),
('Visual Studio'),
('Eclipse'),
('NetBeans'),
('Aptana'),
('xCode'),
('WebStorm'),
('IntelliJ IDEA'),
('LAMP'),
('MVC'),
('OOPs'),
('SaaS'),
('Text NLP'),
('Ruby On Rails'),
('Swift'),
('XML'),
('JSON'),
('Scala'),
('Erlang'),
('OCaml'),
('Haskell'),
('F#'),
('Clojure'),
('Elixir'),
('Joomla'),
('Drupal'),
('Wordpress'),
('Magento (for e-commerce)'),
('Django'),
('Flask'),
('Pyramid'),
('Pylons'),
('SQLAlchemy'),
('web2py'),
('Kivy'),
('Akka'),
('Cats'),
('Lift'),
('Play'),
('Scalaz'),
('Slick'),
('Shapeless'),
('Spray'),
('iOS - Objective C'),
('iOS - Swift'),
('iOS - CoreData'),
('iOS - Cocoa'),
('iOS - Cocoa Touch'),
('iOS - UIKit'),
('iOS - CoreLocation'),
('iOS - CoreFoundation'),
('iOS - CoreImage'),
('iOS - CoreGraphics'),
('iOS - Cocoa Pods'),
('iOS - Flurry'),
('iOS - TestFlight'),
('iOS - HockeyApp'),
('Android - SDK'),
('Android - NDK'),
('Android - IDE'),
('Android - Gradle'),
('Android - AndEngine'),
('Android - Robotium'),
('jQuery Mobile'),
('PhoneGap'),
('Sencha Touch'),
('Dojo Mobile'),
('Titanium'),
('Xamarin'),
('OOD'),
('Design Patterns'),
('MVC'),
('SOA'),
('UML'),
('Webservices'),
('Markup'),
('Markup - XML'),
('Markup - XSLT'),
('Markup - xPath'),
('Markup - XMPP'),
('API'),
('API - Facebook API'),
('API - Google API'),
('API - Twitter API'),
('Agile'),
('Accounts Payable'),
('Asset Management'),
('Automation - Appium'),
('Business Intelligence'),
('Bootstrap'),
('C++'),
('C'),
('Chatbot'),
('CICS'),
('Citrix'),
('Claims'),
('Cloud Computing'),
('COBOL'),
('Data Analysis'),
('Data Mining'),
('Data Modelling'),
('Data Science'),
('Digital'),
('ERP/CRM'),
('Firewall'),
('Firewall'),
('Forecasting'),
('Fund Management'),
('GoldenGate'),
('Grails'),
('Hyperion'),
('Indirect Taxation'),
('Internet Protocol'),
('Marketing'),
('Digital'),
('Marketing-Head'),
('Maximo'),
('MDM'),
('Microservices'),
('MicrosoftExchange'),
('MongoDB'),
('NAT'),
('Neoload'),
('Neoload'),
('Security'),
('Networking'),
('OBIEE'),
('OFSAA'),
('OIPA'),
('OMNI'),
('Oozie'),
('OracleFusion'),
('ORMB'),
('Peoplesoft'),
('PHP'),
('QTP'),
('Recruitment'),
('Regulatory Reporting'),
('Requirement Gathering'),
('Risk'),
('Robotic Procees Automation'),
('Sailpoint'),
('Salesforce'),
('SAN'),
('SAP'),
('SAP - ICM'),
('SAP - R3'),
('SAS'),
('SCCM'),
('Big Data - Sqoop'),
('Big Data - Sqoop'),
('ServiceDesk'),
('ServiceNow'),
('Statutory Audit'),
('Tally'),
('Technical Writer'),
('UI Designer'),
('VPN'),
('Web Servers - WebLogic'),
('WindowsFAX'),
('FreeSwitch');

delete from sms_templates;
INSERT INTO SMS_TEMPLATES (TEMPLATE_NAME, TEMPLATE_CONTENT) VALUES
('ChatInvite','New Job - [[${commBean.jobtitle}]] at [[${commBean.sendercompany}]]%n[[${commBean.receiverfirstname}]],%n[[${commBean.senderfirstname}]] from [[${commBean.sendercompany}]] has invited you to apply for the [[${commBean.jobtitle}]] position. Click the link below to apply.%n[[${commBean.chatlink}]]'),
('ChatCompleted','Congratulations [[${commBean.receiverfirstname}]]!%nYour Profile is now complete for the [[${commBean.jobtitle}]] position. [[${commBean.senderfirstname}]] from [[${commBean.sendercompany}]] will be in touch with you if your profile is shortlisted.%n%nGood luck!'),
('ChatIncompleteReminder1','Oh no [[${commBean.receiverfirstname}]]!  The Litmus Profile you started creating to for the [[${commBean.jobtitle}]] job at [[${commBean.sendercompany}]] was left incomplete. It''s important that you finish the profile to be considered for the job. Continue from where you left last. Just click the link to continue. [[${commBean.chatlink}]]'),
('ChatIncompleteReminder2','[[${commBean.receiverfirstname}]],%nThis is your final reminder%n[[${commBean.sendercompany}]] is still waiting to see your profile for the [[${commBean.jobtitle}]] job. Your Litmus Profile is not complete. It will take just a few more minutes to finish it. Please click the link to continue. [[${commBean.chatlink}]]'),
('LinkNotVisitedReminder1','[[${commBean.jobtitle}]] Job opportunity at [[${commBean.sendercompany}]]. [[${commBean.receiverfirstname}]], you are being considered for this job. Click the link to apply. [[${commBean.chatlink}]]'),
('LinkNotVisitedReminder2','[[${commBean.receiverfirstname}]], are you not interested in this job? [[${commBean.senderfirstname}]] from [[${commBean.sendercompany}]] has invited you to apply for the [[${commBean.jobtitle}]] position. Click the link below to apply. [[${commBean.chatlink}]]'),
('ChatNotVisitedReminder1','Hi [[${commBean.receiverfirstname}]],%nHere is your link to create your Litmus Profile for [[${commBean.jobtitle}]] job at [[${commBean.sendercompany}]]. It''s required to submit completed profile to be considered for the job. The link is valid only for 48 hours. Click the link to begin. [[${commBean.chatlink}]] '),
('ChatNotVisitedReminder2','[[${commBean.receiverfirstname}]],%nJust a reminder to complete your Litmus Profile for [[${commBean.jobtitle}]] job at [[${commBean.sendercompany}]]. It will take just a few minutes to finish it.  It''s required that you finish the profile to be considered for the job.  This link will expire in 24 hours.%nClick the link to apply. [[${commBean.chatlink}]] ');

delete from create_job_page_sequence;
INSERT INTO CREATE_JOB_PAGE_SEQUENCE (PAGE_DISPLAY_NAME, PAGE_NAME, PAGE_DISPLAY_ORDER, DISPLAY_FLAG,SUBSCRIPTION_AVAILABILITY)
VALUES
('Overview', 'overview', 1, 'T','Lite'),
('Expertise', 'expertise', 3, 'F','Lite'),
('Job Detail', 'jobDetail', 8, 'F','Max'),
('Screening', 'screeningQuestions', 2, 'T','Lite'),
('Key Skills', 'keySkills', 4, 'T','Lite'),
('Hiring Team', 'hiringTeam', 6, 'T','Max'),
('Capabilities', 'capabilities', 5, 'T','Lite'),
('Preview', 'preview', 7, 'T','Lite');

INSERT INTO MASTER_DATA (TYPE, VALUE, VALUE_TO_USE, COMMENTS)
VALUES
 ( 'expertise','Beginner', 1, 'Candidate has 1-2 years of relevant work experience and works on given tasks on day to day basis. Exposure to job complexities is limited and needs support/guidance for complex tasks.'),
 ( 'expertise','Competent', 2, 'Candidate can independently handle all tasks. Typically has 2 - 5 years of relevant work experience. Dependable on senior for assigned work. Can participate in training/grooming of juniors'),
 ( 'expertise','Expert', 3, 'Considered as a Master in the organization/industry. Candidate can handle highly complex scenarios and is the go-to person for others. Such candidates are rare to find and often come at a high cost. Select this option if you want to hire a expert.');

INSERT INTO MASTER_DATA (TYPE, VALUE)
VALUES
( 'experienceRange', '0 - 2 Years'),
( 'experienceRange', '2 - 4 Years'),
( 'experienceRange', '4 - 6 Years'),
( 'experienceRange', '6 - 8 Years'),
( 'experienceRange', '8 - 10 Years'),
( 'experienceRange', '10 - 15 Years'),
( 'experienceRange', '15 - 20 Years'),
( 'experienceRange', '20+ Years'),

( 'function','Accounting / Tax / Company Secretary / Audit'),
( 'function','Agent'),
( 'function','Airline / Reservations / Ticketing / Travel'),
( 'function','Analytics & Business Intelligence'),
( 'function','Anchoring / TV / Films / Production'),
( 'function','Architects / Interior Design / Naval Arch');

delete from weightage_cutoff_mapping;
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

INSERT INTO public.currency(currency_full_name, currency_short_name, country) VALUES
('Australian Dollar', 'AUD', 'au'),
('Canadian Dollar', 'CAD', 'ca'),
('Indian Rupee', 'INR', 'in'),
('Singapore Dollar', 'SGD', 'sg'),
('Pound Sterling', 'GBP', 'gb'),
('US Dollar', 'USD', 'us');

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