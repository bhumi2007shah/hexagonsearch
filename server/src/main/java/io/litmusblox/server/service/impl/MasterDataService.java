/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.error.ValidationException;
import io.litmusblox.server.error.WebException;
import io.litmusblox.server.model.*;
import io.litmusblox.server.repository.*;
import io.litmusblox.server.service.*;
import io.litmusblox.server.utils.Util;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.ConfigurablePropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service class for Master Data handling
 *
 * @author : Shital Raval
 * Date : 2/7/19
 * Time : 2:18 PM
 * Class Name : MasterDataService
 * Project Name : server
 */
@Log4j2
@Service
public class MasterDataService implements IMasterDataService {
    @Resource
    MasterDataRepository masterDataRepository;

    @Resource
    CountryRepository countryRepository;

    @Resource
    UserScreeningQuestionRepository userScreeningQuestionRepository;

    @Resource
    SkillMasterRepository skillMasterRepository;

    @Resource
    ScreeningQuestionsRepository screeningQuestionsRepository;

    @Resource
    ConfigurationSettingsRepository configurationSettingsRepository;

    @Resource
    CreateJobPageSequenceRepository createJobPageSequenceRepository;

    @Resource
    CurrencyRepository currencyRepository;

    @Resource
    StageStepMasterRepository stageStepMasterRepository;

    @Resource
    ExportFormatMasterRepository exportFormatMasterRepository;

    @Resource
    RejectionReasonMasterDataRepository rejectionReasonMasterDataRepository;

    @Resource
    IndustryMasterDataRepository industryMasterDataRepository;

    @Resource
    FunctionMasterDataRepository functionMasterDataRepository;

    @Resource
    RoleMasterDataRepository roleMasterDataRepository;

    @Resource
    StatementsBlockMasterDataRepository statementsBlockMasterDataRepository;

    @Resource
    AttributesMasterDataRepository attributesMasterDataRepository;

    @Autowired
    Environment environment;

    /**
     * Method that will be called during application startup
     * Will read all master data from database and store them in internal cache
     *
     * @throws Exception
     */
    @Override
    public void loadStaticMasterData() throws Exception {

        MasterDataBean.getInstance().getCountryList().addAll(countryRepository.findAll());

        //Page Data logic also move to getMasterData method because while load master data we don't have loggedIn user
        //add all pages that need to be displayed for the add job process
        /*createJobPageSequenceRepository.findByDisplayFlagIsTrueAndSubscriptionAvailabilityOrderByPageDisplayOrderAsc(loggedInUser.getCompany().getSubscription()).stream().forEach(page-> {
            MasterDataBean.getInstance().getJobPageNamesInOrder().add(page.getPageName());
        });*/

       stageStepMasterRepository.findAllByOrderByIdAsc().forEach(stageStepMaster -> {
            MasterDataBean.getInstance().getStage().add(stageStepMaster.getStage());
            MasterDataBean.getInstance().getStageStepMasterMap().put(stageStepMaster.getStage(), stageStepMaster.getId());
            MasterDataBean.getInstance().getStageStepMap().put(stageStepMaster.getId(), stageStepMaster);
        });

        currencyRepository.findAll().stream().forEach(currency -> {
            Map<String, String> salaryMap = new HashMap<>();
            salaryMap.put("minSalary", currency.getMinSalary().toString());
            salaryMap.put("maxSalary", currency.getMaxSalary().toString());
            salaryMap.put("salaryUnit", currency.getSalaryUnit());
            MasterDataBean.getInstance().getCurrencyList().add(currency.getCurrencyShortName());
            MasterDataBean.getInstance().getSalaryRange().put(currency.getCurrencyShortName(), salaryMap);
        });

            List<MasterData> masterDataFromDb = masterDataRepository.findAll();

        List<SkillsMaster> keySkillsList = skillMasterRepository.findAll();
        keySkillsList.stream().forEach(keySkill ->
                MasterDataBean.getInstance().getKeySkills().put(keySkill.getId(), keySkill.getSkillName())
                );
        //handle to the getter method of the map in the master data singleton instance class
        ConfigurablePropertyAccessor mapAccessor = PropertyAccessorFactory.forDirectFieldAccess(MasterDataBean.getInstance());

        //For every master data record from database, populate the corresponding map with key-value pairs
        masterDataFromDb.forEach(data -> {

            if(data.getType().equalsIgnoreCase("userRole"))
                MasterDataBean.getInstance().getUserRole().add(data.getValue());
            else if(data.getType().equalsIgnoreCase("reasonForChange"))
                MasterDataBean.getInstance().getReasonForChange().add(data.getValue());
            else if(data.getType().equalsIgnoreCase("questionCategory"))
                MasterDataBean.getInstance().getQuestionCategory().put(data.getValue(), data);
            else if(data.getType().equalsIgnoreCase("callOutCome"))
                MasterDataBean.getInstance().getCallOutCome().add(data.getValue());
            else if(data.getType().equalsIgnoreCase("interviewConfirmation"))
                MasterDataBean.getInstance().getInterviewConfirmation().put(data.getValue(), data);
            else if(data.getType().equalsIgnoreCase("questionType")){
                MasterDataBean.getInstance().getQuestionType().put(data.getId(), data.getValueToUSe());
                MasterDataBean.getInstance().getQuestionTypeMap().put(data.getValueToUSe(), data);
            }
            else
                ((Map)mapAccessor.getPropertyValue(data.getType())).put(data.getId(), data.getValue());

            if(data.getType().equalsIgnoreCase("noticePeriod"))
                MasterDataBean.getInstance().getNoticePeriodMapping().put(data.getValue(), data);

            if(data.getValue().equalsIgnoreCase(IConstant.DEFAULT_JOB_TYPE))
                MasterDataBean.getInstance().setDefaultJobType(data);
        });

        //Add this logic in getMasterData method because while load master data we don't have loggedIn user
        //set master screening questions depend on country id
            /*MasterDataBean.getInstance().getQuestionCategory().entrySet().forEach(category->{
                MasterDataBean.getInstance().getScreeningQuestions().put(category.getKey(), screeningQuestionsRepository.findByCountryIdAndQuestionCategory(loggedInUser.getCountryId(), category.getValue()));
            });*/

        //populate various configuration settings like max limits, send sms/email flag,etc
        List<ConfigurationSettings> configurationSettings = configurationSettingsRepository.findAll();
        ConfigurablePropertyAccessor configFieldAccesor = PropertyAccessorFactory.forDirectFieldAccess(MasterDataBean.getInstance().getConfigSettings());
        configurationSettings.forEach(config-> {
            configFieldAccesor.setPropertyValue(config.getConfigName(), config.getConfigValue());
        });

        //populate default export format supported by litmusblox
        List<ExportFormatMaster> exportFormatMasters = exportFormatMasterRepository.exportDefaultFormatMasterList();
        MasterDataBean.getInstance().getDefaultExportFormats().addAll(exportFormatMasters);

        //read the limit from application.properties
        //convert the maxUploadDataLimit from Mb into bytes
        String maxSize = environment.getProperty("spring.http.multipart.max-request-size");
        MasterDataBean.getInstance().getConfigSettings().setMaxUploadDataLimit(Integer.parseInt(maxSize.substring(0,maxSize.indexOf("MB")))*1024*1024);

        MasterDataBean.getInstance().setLoaded(true);

        // sentryDSN is only read from application.properties file as per profile it is not save in database
        MasterDataBean.getInstance().setSentryDSN(environment.getProperty(IConstant.SENTRY_DSN));
        MasterDataBean.getInstance().setOtpExpiryMinutes(MasterDataBean.getInstance().getConfigSettings().getOtpExpiryMinutes());

        //Set Candidate rejection reasons
        List<RejectionReasonMasterData> rejectionReasonMasterDataList = rejectionReasonMasterDataRepository.findAll();
        if(rejectionReasonMasterDataList.size()>0) {
            MasterDataBean.getInstance().getCandidateRejectionReasonMap().put(IConstant.Stage.Screen.getValue(), setRejectionReasonList(rejectionReasonMasterDataList, new ArrayList<>(), IConstant.Stage.Screen.getValue()));
            MasterDataBean.getInstance().getCandidateRejectionReasonMap().put(IConstant.Stage.Source.getValue(), setRejectionReasonList(rejectionReasonMasterDataList, new ArrayList<>(), IConstant.Stage.Source.getValue()));
            MasterDataBean.getInstance().getCandidateRejectionReasonMap().put(IConstant.Stage.Interview.getValue(), setRejectionReasonList(rejectionReasonMasterDataList, new ArrayList<>(), IConstant.Stage.Interview.getValue()));
            MasterDataBean.getInstance().getCandidateRejectionReasonMap().put(IConstant.Stage.MakeOffer.getValue(), setRejectionReasonList(rejectionReasonMasterDataList, new ArrayList<>(), IConstant.Stage.MakeOffer.getValue()));
            MasterDataBean.getInstance().getCandidateRejectionReasonMap().put(IConstant.Stage.Offer.getValue(), setRejectionReasonList(rejectionReasonMasterDataList, new ArrayList<>(), IConstant.Stage.Offer.getValue()));
            MasterDataBean.getInstance().getCandidateRejectionReasonMap().put(IConstant.Stage.Join.getValue(), setRejectionReasonList(rejectionReasonMasterDataList, new ArrayList<>(), IConstant.Stage.Join.getValue()));
            MasterDataBean.getInstance().getCandidateRejectionReasonMap().put(IConstant.Stage.ResumeSubmit.getValue(), setRejectionReasonList(rejectionReasonMasterDataList, new ArrayList<>(), IConstant.Stage.ResumeSubmit.getValue()));

            rejectionReasonMasterDataList.forEach(rejectionData ->{
               MasterDataBean.getInstance().getCandidateRejections().put(rejectionData.getId(), rejectionData);
            });
        }

        StageStepMaster stageStepMaster = new StageStepMaster();
        stageStepMaster.setId(3L);
        MasterDataBean.getInstance().getHiringManagerRejectReasonMap().put(IConstant.Stage.ResumeSubmit.getValue(), rejectionReasonMasterDataRepository.findByStageId(stageStepMaster));

        //Load JobIndustry in master data
        industryMasterDataRepository.findAll().forEach(industryMasterData -> {
            MasterDataBean.getInstance().getJobIndustry().put(industryMasterData.getId(), industryMasterData);
            MasterDataBean.getInstance().getJobIndustryMap().put(industryMasterData.getIndustry(), industryMasterData.getId());
        });

        //Load JobFunction in master data
        functionMasterDataRepository.findAll().forEach(functionMasterData ->
                MasterDataBean.getInstance().getFunction().put(functionMasterData.getId(), functionMasterData));

        MasterDataBean.getInstance().getJobIndustry().entrySet().forEach(jobIndustry ->{
            Map<String, Long> tempFunctionMap = new HashMap<>();
            functionMasterDataRepository.findByIndustry(jobIndustry.getValue()).forEach(functionMasterData1 ->
                tempFunctionMap.put(functionMasterData1.getFunction(), functionMasterData1.getId()));
            MasterDataBean.getInstance().getFunctionMap().put(jobIndustry.getKey(), tempFunctionMap);
        });

        MasterDataBean.getInstance().setExpertise(new LinkedHashMap<>());
        //Add expertise
        masterDataRepository.findByTypeOrderByValueToUSe("expertise").stream().forEach(expertise ->{
            MasterDataBean.getInstance().getExpertise().put(expertise.getId(), expertise);
        });

        //Load JobRole in master data
        roleMasterDataRepository.findAll().forEach(roleMasterData ->
                MasterDataBean.getInstance().getRole().put(roleMasterData.getId(), roleMasterData));
        MasterDataBean.getInstance().getFunction().entrySet().forEach(function ->{
            Map<String, Long> tempRoleMap = new HashMap<>();
            roleMasterDataRepository.findByFunction(function.getValue()).forEach(roleMasterData1 ->
                tempRoleMap.put(roleMasterData1.getRole(), roleMasterData1.getId()));
            MasterDataBean.getInstance().getRoleMap().put(function.getKey(), tempRoleMap);
        });

        //Load JobAttribute in master data
        attributesMasterDataRepository.findAll().forEach(attributeMasterData ->
                MasterDataBean.getInstance().getAttribute().put(attributeMasterData.getId(), attributeMasterData));
        MasterDataBean.getInstance().getFunction().entrySet().forEach(function ->{
            Map<String, Long> tempAttributeMap = new HashMap<>();
            attributesMasterDataRepository.findByFunction(function.getValue()).forEach(attributeMasterData1 ->
                    tempAttributeMap.put(attributeMasterData1.getJobAttribute(), attributeMasterData1.getId()));
            MasterDataBean.getInstance().getAttributeMap().put(function.getKey(), tempAttributeMap);
        });

        //Load statement block data
        MasterDataBean.getInstance().getStatementBlocks().addAll(statementsBlockMasterDataRepository.findAll());

    }

    private List<RejectionReasonMasterData> setRejectionReasonList(List<RejectionReasonMasterData> rejectionReasons, List<RejectionReasonMasterData> rejectionsPerStage, String stage){
        log.info("Inside setRejectionReasonList for stage : {}",stage);
        rejectionReasons.stream()
                .filter(rejectionReasonMasterData -> rejectionReasonMasterData.getStageId().getStage().equals(stage))
                .collect(Collectors.toList()).forEach(rejectionReasonMasterData->{
            rejectionsPerStage.add(rejectionReasonMasterData);
        });
        return rejectionsPerStage;
    }

    /**
     * Method that will reload all master data in memory
     *
     * @throws Exception
     */
    @Override
    public void reloadMasterData() throws Exception {

        MasterDataBean masterBean = MasterDataBean.getInstance();

        //field accessor for MasterDataBean
        ConfigurablePropertyAccessor fieldAccessor = PropertyAccessorFactory.forDirectFieldAccess(masterBean);

        //Clear contents of all Map and List
        Field[] allFieldsOfMasterDataBean = masterBean.getClass().getDeclaredFields();
        for(Field f : allFieldsOfMasterDataBean) {
            if (f.getType().equals(java.util.List.class)) {
                ((List)fieldAccessor.getPropertyValue(f.getName())).clear();
            }
            else if (f.getType().equals(java.util.Map.class)) {
                ((Map)fieldAccessor.getPropertyValue(f.getName())).clear();
            }
            else
                continue;
        }
        masterBean.setConfigSettings(new ConfigSettings());
        loadStaticMasterData();
    }

    /**
     * Method to fetch specific master data values from cache
     *
     * @param fetchItemList The items for which master data needs to be fetched from memory
     * @return response bean with Maps containing requested master data values
     * @throws Exception
     */
    @Override
    public MasterDataResponse fetchForItems(List<String> fetchItemList) throws Exception {
        log.info("Received request to fetch master data");
        long startTime = System.currentTimeMillis();

        MasterDataResponse master = new MasterDataResponse();
        //populate data for each of the required items
        fetchItemList.stream().forEach(item -> getMasterData(master, item, false));

        log.info("Completed request to fetch master data in " + (System.currentTimeMillis() - startTime) + "ms");
        return master;
    }

    /**
     * Service to get masterData for only specific fields, which is used in noAuth call
     *
     * @param fetchItemList (jobType, referrerRelation)
     * @return MasterDataResponse
     */
    @Override
    public MasterDataResponse fetchForItemsForNoAuth(List<String> fetchItemList) {
        log.info("Received request to fetch master data from no auth call");
        long startTime = System.currentTimeMillis();

        MasterDataResponse master = new MasterDataResponse();
        //populate data for each of the required items
        fetchItemList.stream().forEach(item -> {
            if(!Arrays.asList(IConstant.fetchItemsType).contains(item))
                throw new ValidationException("You can not access masterData for " +item+" Item", HttpStatus.UNAUTHORIZED);

            getMasterData(master, item, true);
        });

        log.info("Completed request to fetch master from no auth data in " + (System.currentTimeMillis() - startTime) + "ms");
        return master;
    }

    /**
     * Method to add master data to database.
     * Supported master data types:
     * 1. UserScreeningQuestion
     *
     * @param jsonData       master data to be persisted (in json format)
     * @param masterDataType the type of master data to be persisted
     */
    @Transactional
    public void addMasterData(String jsonData, String masterDataType) throws Exception {
        switch (masterDataType) {
            case UserScreeningQuestion.IDENTIFIER:
                //create a Java object from the json string
                UserScreeningQuestion objToSave = new ObjectMapper().readValue(jsonData, UserScreeningQuestion.class);
                objToSave.setCreatedOn(new Date());
                //persist to database
                userScreeningQuestionRepository.save(objToSave);
                break;
            default:
                throw new WebException("Unsupported action", HttpStatus.BAD_REQUEST);
        }
    }

    private static final String COUNTRY_MASTER_DATA = "countries";
    private static final String SCREENING_QUESTIONS_MASTER_DATA = "screeningQuestions";
    private static final String CONFIG_SETTINGS = "configSettings";
    private static final String SUPPORTED_FILE_FORMATS = "supportedFileFormats";
    private static final String SUPPORTED_CV_FILE_FORMATS = "supportedCvFileFormats";
    private static final String STAGE_STEP_MASTER_DATA = "stageStepMaster";
    private static final String ADD_JOB_PAGES = "addJobPages";
    private static final String CURRENCY_LIST = "currencyList";
    private static final String USER_ROLE = "userRole";
    private static final String REASON_FOR_CHANGE = "reasonForChange";
    private static final String DEFAULT_EXPORT_FORMAT = "defaultExportFormats";
    private static final String CALL_OUT_COME = "callOutCome";
    private static final String STAGE_MASTER_DATA = "stage";
    private static final String INTERVIEW_TYPE = "interviewType";
    private static final String INTERVIEW_MODE = "interviewMode";
    private static final String INTERVIEW_CANCELLATION_REASONS = "cancellationReasons";
    private static final String INTERVIEW_NO_SHOW_REASONS = "noShowReasons";
    private static final String INTERVIEW_CONFIRMATION = "interviewConfirmation";
    private static final String OTP_EXPIRY_MINUTES = "otpExpiryMinutes";
    private static final String CANDIDATE_REJECTION_REASONS = "candidateRejectionReasons";
    private static final String HIRING_MANAGER_REJECTION_REASONS = "hiringManagerRejectReasons";
    private static final String JOB_INDUSTRY = "jobIndustry";
    private static final String JOB_FUNCTION = "function";
    private static final String JOB_ROLE = "role";
    private static final String SALARY_RANGE = "salaryRange";
    private static final String ARCHIVE_STATUS = "archiveStatus";
    private static final String ARCHIVE_REASON = "archiveReason";
    private static final String STATEMENT_BLOCKS = "statementBlocks";
    private static final String JOB_ATTRIBUTE = "attribute";
    private static final String CANDIDATE_NOT_INTERESTED_REASON = "candidateNotInterestedReason";

    /**
     * Method to fetch specific master data from cache
     * @param master the response bean to be populated
     * @param input the requested master data
     *
     */
    private void getMasterData(MasterDataResponse master, String input, boolean isNoAuthCall) {
        User loggedInUser = null;

        if(!isNoAuthCall)
            loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        switch (input) {
            case COUNTRY_MASTER_DATA:
                master.getCountries().addAll(MasterDataBean.getInstance().getCountryList());
                break;
            case STAGE_STEP_MASTER_DATA:
                master.getStageStepMasterMap().putAll(MasterDataBean.getInstance().getStageStepMasterMap());
                break;
            case ADD_JOB_PAGES:
                log.info("AddJob pages for subscription : {}",loggedInUser.getCompany().getSubscription());
                MasterDataBean.getInstance().setAddJobPages(new ArrayList<>());
                MasterDataBean.getInstance().setJobPageNamesInOrder(new ArrayList<>());
                createJobPageSequenceRepository.findByDisplayFlagIsTrueAndSubscriptionAvailabilityOrderByPageDisplayOrderAsc(IConstant.CompanySubscription.Lite.name()).stream().forEach(page-> {
                    MasterDataBean.getInstance().getAddJobPages().add(page);
                    MasterDataBean.getInstance().getJobPageNamesInOrder().add(page.getPageName());
                });
                master.getAddJobPages().addAll(MasterDataBean.getInstance().getAddJobPages());
                break;
            case SCREENING_QUESTIONS_MASTER_DATA:
                User finalLoggedInUser = loggedInUser;
                screeningQuestionsRepository.findByCountryIdAndQuestionCategory(finalLoggedInUser.getCountryId().getId()).forEach(screeningQuestions -> {
                    master.getScreeningQuestions().put(screeningQuestions.getQuestionCategory().getValue(), Arrays.asList(screeningQuestions));
                });
                break;
            case CONFIG_SETTINGS:
                master.setConfigSettings(MasterDataBean.getInstance().getConfigSettings());
                break;
            case SUPPORTED_FILE_FORMATS:
                master.setSupportedFileFormats(Stream.of(IConstant.UPLOAD_FORMATS_SUPPORTED.values())
                                            .map(Enum::name)
                                            .collect(Collectors.toList()));
                break;
            case SUPPORTED_CV_FILE_FORMATS:
                master.setSupportedCvFileFormats(Arrays.asList(IConstant.cvUploadSupportedExtensions));
                break;
            case CURRENCY_LIST:
                master.setCurrencyList(MasterDataBean.getInstance().getCurrencyList());
                break;
            case USER_ROLE:
                master.getUserRole().addAll(MasterDataBean.getInstance().getUserRole());
                break;
            case REASON_FOR_CHANGE:
                master.getReasonForChange().addAll(MasterDataBean.getInstance().getReasonForChange());
                break;
            case CALL_OUT_COME:
                master.getCallOutCome().addAll(MasterDataBean.getInstance().getCallOutCome());
                break;
            case STAGE_MASTER_DATA:
                master.getStage().addAll(MasterDataBean.getInstance().getStage());
                break;
            case INTERVIEW_TYPE:
                master.getInterviewType().addAll(Stream.of(IConstant.InterviewType.values())
                        .map(IConstant.InterviewType::getValue)
                        .collect(Collectors.toList()));
                break;
            case INTERVIEW_MODE:
                master.getInterviewMode().addAll(Stream.of(IConstant.InterviewMode.values())
                        .map(IConstant.InterviewMode::getValue)
                        .collect(Collectors.toList()));
                break;
            case INTERVIEW_CANCELLATION_REASONS:
                master.getCancellationReasons().putAll(MasterDataBean.getInstance().getCancellationReasons());
                break;
            case INTERVIEW_NO_SHOW_REASONS:
                master.getNoShowReasons().putAll(MasterDataBean.getInstance().getNoShowReasons());
                break;
            case INTERVIEW_CONFIRMATION:
                master.getInterviewConfirmation().addAll(MasterDataBean.getInstance().getInterviewConfirmation().keySet());
                break;
            case OTP_EXPIRY_MINUTES:
                master.setOtpExpiryMinutes(MasterDataBean.getInstance().getOtpExpiryMinutes());
                break;
            case CANDIDATE_REJECTION_REASONS:
                master.getCandidateRejectionReasonMap().putAll(MasterDataBean.getInstance().getCandidateRejectionReasonMap());
                break;
            case HIRING_MANAGER_REJECTION_REASONS:
                master.getHiringManagerRejectReasonMap().putAll(MasterDataBean.getInstance().getHiringManagerRejectReasonMap());
            case JOB_INDUSTRY:
                master.getJobIndustry().putAll(MasterDataBean.getInstance().getJobIndustryMap());
                break;
            case JOB_FUNCTION:
                master.getFunction().putAll(MasterDataBean.getInstance().getFunctionMap());
                break;
            case JOB_ROLE:
                master.getRole().putAll(MasterDataBean.getInstance().getRoleMap());
                break;
            case SALARY_RANGE:
                master.getSalaryRange().putAll(MasterDataBean.getInstance().getSalaryRange());
                break;
            case ARCHIVE_STATUS:
                master.getArchiveStatus().putAll(IConstant.ArchiveStatus);
                break;
            case ARCHIVE_REASON:
                master.getArchiveReason().putAll(IConstant.ArchiveReason);
                break;
            case STATEMENT_BLOCKS:
                master.getStatementBlocks().addAll(MasterDataBean.getInstance().getStatementBlocks());
                break;
            case JOB_ATTRIBUTE:
                master.getAttributeMap().putAll(MasterDataBean.getInstance().getAttributeMap());
                break;
            case CANDIDATE_NOT_INTERESTED_REASON:
                master.getCandidateNotInterestedReason().putAll(MasterDataBean.getInstance().getCandidateNotInterestedReason());
                break;
            default: //for all other properties, use reflection

                //handle to the getter method for the field
                ConfigurablePropertyAccessor fieldAccessor = PropertyAccessorFactory.forDirectFieldAccess(master);
                //handle to the getter method of the map in the master data singleton instance class
                ConfigurablePropertyAccessor mapAccessor = PropertyAccessorFactory.forDirectFieldAccess(MasterDataBean.getInstance());

                //add map from master data single instance to the response object
                ((Map)fieldAccessor.getPropertyValue(input)).putAll(
                        (Map) mapAccessor.getPropertyValue(input)
                );
        }
    }

    public void addIndustryMasterData(List<IndustryMasterDataRequestBean> industryMasterDataRequestBeanList){
        log.info("Received request to add Industry Master Data with data: {}", industryMasterDataRequestBeanList);
        for(IndustryMasterDataRequestBean industryMasterDataRequestBean:industryMasterDataRequestBeanList) {
            IndustryMasterData industryMasterData = null;
            String industry = industryMasterDataRequestBean.getIndustryName();
            industryMasterData = industryMasterDataRepository.findByIndustry(industry);
            if(null == industryMasterData){
                industryMasterData = new IndustryMasterData();
                industryMasterData.setIndustry(industry);
                industryMasterData = industryMasterDataRepository.save(industryMasterData);
            }
            List<IndustryFunction> industryFunctionsList = industryMasterDataRequestBean.getFunctions();
            if(!industryFunctionsList.isEmpty()){
                for(IndustryFunction industryFunction:industryMasterDataRequestBean.getFunctions()){
                    String function = industryFunction.getFunctionName();
                    FunctionMasterData functionMasterData = null;
                    functionMasterData = functionMasterDataRepository.findByFunctionAndIndustry(function, industryMasterData);
                    if(null == functionMasterData){
                        functionMasterData = new FunctionMasterData();
                        functionMasterData.setFunction(function);
                        functionMasterData.setIndustry(industryMasterData);
                        functionMasterData = functionMasterDataRepository.save(functionMasterData);
                    }
                    List<IndustryRole> industryRolesList = industryFunction.getRoles();
                    if(!industryRolesList.isEmpty()){
                        for(IndustryRole industryRole:industryRolesList){
                            String role = industryRole.getRoleName();
                            RoleMasterData roleMasterData = null;
                            roleMasterData = roleMasterDataRepository.findByRoleAndFunction(role, functionMasterData);
                            if(null == roleMasterData){
                                roleMasterData = new RoleMasterData();
                                roleMasterData.setRole(role);
                                roleMasterData.setFunction(functionMasterData);
                                roleMasterDataRepository.save(roleMasterData);
                            }
                        }
                    }
                }
            }
        }
        try {
            reloadMasterData();
        } catch (Exception e) {
            Util.getStackTrace(e);
        }
        log.info("Successfully added industry Master Data for {}", industryMasterDataRequestBeanList);
    }
}
