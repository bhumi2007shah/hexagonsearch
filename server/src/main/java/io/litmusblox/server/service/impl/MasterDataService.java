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
import io.litmusblox.server.service.ConfigSettings;
import io.litmusblox.server.service.IMasterDataService;
import io.litmusblox.server.service.MasterDataBean;
import io.litmusblox.server.service.MasterDataResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.ConfigurablePropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
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

        //add all pages that need to be displayed for the add job process
        createJobPageSequenceRepository.findByDisplayFlagIsTrueOrderByPageDisplayOrderAsc().stream().forEach(page-> {
            MasterDataBean.getInstance().getAddJobPages().add(page);
            MasterDataBean.getInstance().getJobPageNamesInOrder().add(page.getPageName());
        });

       stageStepMasterRepository.findAllByOrderByIdAsc().forEach(stageStepMaster -> {
            MasterDataBean.getInstance().getStage().add(stageStepMaster.getStage());
            MasterDataBean.getInstance().getStageStepMasterMap().put(stageStepMaster.getStage(), stageStepMaster.getId());
            MasterDataBean.getInstance().getStageStepMap().put(stageStepMaster.getId(), stageStepMaster);
        });

        currencyRepository.findAll().stream().forEach(currency -> {
            MasterDataBean.getInstance().getCurrencyList().add(currency.getCurrencyShortName());
        });

        List<MasterData> masterDataFromDb = masterDataRepository.findAll();

        List<SkillsMaster> keySkillsList = skillMasterRepository.findAll();
        keySkillsList.stream().forEach(keySkill ->
                MasterDataBean.getInstance().getKeySkills().put(keySkill.getId(), keySkill.getSkillName())
                );

        MasterDataBean.getInstance().getScreeningQuestions().addAll(screeningQuestionsRepository.findAll());

        //handle to the getter method of the map in the master data singleton instance class
        ConfigurablePropertyAccessor mapAccessor = PropertyAccessorFactory.forDirectFieldAccess(MasterDataBean.getInstance());

        //For every master data record from database, populate the corresponding map with key-value pairs
        masterDataFromDb.forEach(data -> {

            if(data.getType().equalsIgnoreCase("role"))
                MasterDataBean.getInstance().getRole().add(data.getValue());
            else if(data.getType().equalsIgnoreCase("reasonForChange"))
                MasterDataBean.getInstance().getReasonForChange().add(data.getValue());
            else if(data.getType().equalsIgnoreCase("callOutCome"))
                MasterDataBean.getInstance().getCallOutCome().add(data.getValue());
            else
                ((Map)mapAccessor.getPropertyValue(data.getType())).put(data.getId(), data.getValue());

            if(data.getType().equalsIgnoreCase("noticePeriod"))
                MasterDataBean.getInstance().getNoticePeriodMapping().put(data.getValue(), data);

            if(data.getType().equalsIgnoreCase("expertise"))
                MasterDataBean.getInstance().getExpertise().put(data.getId(), data);

            if(data.getValue().equalsIgnoreCase(IConstant.DEFAULT_JOB_TYPE))
                MasterDataBean.getInstance().setDefaultJobType(data);
        });

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
        fetchItemList.stream().forEach(item -> getMasterData(master, item));

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

            getMasterData(master, item);
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
    private static final String ROLE = "role";
    private static final String REASON_FOR_CHANGE = "reasonForChange";
    private static final String DEFAULT_EXPORT_FORMAT = "defaultExportFormats";
    private static final String CALL_OUT_COME = "callOutCome";
    private static final String STAGE_MASTER_DATA = "stage";


    /**
     * Method to fetch specific master data from cache
     * @param master the response bean to be populated
     * @param input the requested master data
     *
     */
    private void getMasterData(MasterDataResponse master, String input) {

        switch (input) {
            case COUNTRY_MASTER_DATA:
                master.getCountries().addAll(MasterDataBean.getInstance().getCountryList());
                break;
            case STAGE_STEP_MASTER_DATA:
                master.getStageStepMasterMap().putAll(MasterDataBean.getInstance().getStageStepMasterMap());
                break;
            case ADD_JOB_PAGES:
                master.getAddJobPages().addAll(MasterDataBean.getInstance().getAddJobPages());
                break;
            case SCREENING_QUESTIONS_MASTER_DATA:
                master.getScreeningQuestions().addAll(MasterDataBean.getInstance().getScreeningQuestions());
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
            case ROLE:
                master.getRole().addAll(MasterDataBean.getInstance().getRole());
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
}
