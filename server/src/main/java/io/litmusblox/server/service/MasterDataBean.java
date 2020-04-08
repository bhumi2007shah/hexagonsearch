package io.litmusblox.server.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.model.*;
import lombok.Data;

import java.util.*;

/**
 * @author : Shital Raval
 * Date : 4/7/19
 * Time : 1:34 PM
 * Class Name : MasterDataBean
 * Project Name : server
 */
@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class MasterDataBean {

    @JsonIgnore
    private static MasterDataBean instance;

    private boolean loaded = false;

    public static MasterDataBean getInstance() {
        if (instance == null) {
            synchronized (MasterDataBean.class) {
                if (instance == null) {
                    instance = new MasterDataBean();
                }
            }
        }
        return instance;
    }

    private List<Country> countryList = new ArrayList<Country>();

    private Map<Long, String> keySkills = new HashMap<>();

    private Map<Long, String> questionType = new HashMap<>();
    private Map<Long, String> experienceRange = new HashMap<>();
    private Map<Long, String> addressType = new HashMap<>();
    private List<String> stage = new ArrayList<>();
    private Map<Long, String> process = new HashMap<>();
    private Map<Long, String> oldFunction = new HashMap<>();
    private Map<Long, MasterData> expertise = new HashMap<>();
    private Map<Long, String> education = new HashMap<>();
    private Map<Long, String> industry = new HashMap<>();
    private Map<Long, String> noticePeriod = new HashMap<>();
    private Map<Long, Map<String, List<ScreeningQuestions>>> screeningQuestions = new HashMap<>();
    private List<CreateJobPageSequence> addJobPages = new ArrayList<>();
    private List<String> jobPageNamesInOrder = new ArrayList<>();
    private List<String> currencyList = new ArrayList<>();
    private List<String> userRole = new ArrayList<>();
    private List<String> reasonForChange = new ArrayList<>();

    private Map<String, MasterData> noticePeriodMapping = new HashMap<>();

    private ConfigSettings configSettings = new ConfigSettings();

    private List<ExportFormatMaster> defaultExportFormats = new ArrayList<>();

    private List<String> callOutCome = new ArrayList<>();

    private Map<Long, String> referrerRelation = new HashMap<>();
    private Map<Long, String> jobType = new HashMap<>();
    private Map<String, Long> stageStepMasterMap = new LinkedHashMap<>();
    private Map<Long, StageStepMaster> stageStepMap = new HashMap<>();
    private MasterData defaultJobType = null;
    private Map<Long, String> cancellationReasons = new HashMap<>();
    private Map<Long, String> noShowReasons = new HashMap<>();
    private Map<String, MasterData> interviewConfirmation = new LinkedHashMap<>();
    private Map<String, List<RejectionReasonMasterData>> candidateRejectionReasonMap = new LinkedHashMap<>();
    private Map<Long, RejectionReasonMasterData> candidateRejections = new HashMap<>();
    private Map<Long, IndustryMasterData> jobIndustry = new HashMap<>();
    private Map<Long, FunctionMasterData> function = new HashMap<>();
    private Map<Long, RoleMasterData> role = new HashMap<>();
    private Map<String, Long> jobIndustryMap = new HashMap<>();
    private Map<Long, Map<String, Long>> functionMap = new HashMap<>();
    private Map<Long, Map<String, Long>> roleMap = new HashMap<>();
    private Map<String, MasterData> questionCategory = new HashMap<>();
    private Integer restConnectionTimeout = IConstant.REST_CONNECTION_TIME_OUT;
    private Integer restReadTimeout = IConstant.REST_READ_TIME_OUT;
    private Integer restReadTimeoutForCvParser = IConstant.REST_READ_TIME_OUT_FOR_CV_TEXT;

    // sentryDSN is only read from application.properties file as per profile it is not save in database
    private String sentryDSN=null;

    // OTP timeout in minutes
    private int otpExpiryMinutes;
}
