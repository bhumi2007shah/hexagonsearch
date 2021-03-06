/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import io.litmusblox.server.model.Country;
import io.litmusblox.server.model.CreateJobPageSequence;
import io.litmusblox.server.model.MasterData;
import io.litmusblox.server.model.ScreeningQuestions;
import lombok.Data;

import java.util.*;

/**
 * @author : Shital Raval
 * Date : 2/7/19
 * Time : 2:26 PM
 * Class Name : MasterDataResponse
 * Project Name : server
 */
@Data
public class MasterDataResponse {
    private List<Country> countries = new ArrayList<Country>();
    private Map<Long,String> questionType  = new HashMap<>();
    private Map<Long,String> experienceRange  = new HashMap<>();
    private Map<Long,String> addressType  = new HashMap<>();
    private List<String> stage = new ArrayList<>();
    private Map<Long,String> process = new HashMap<>();
    private Map<Long,String> function  = new HashMap<>();
    private Map<Long, MasterData> expertise  = new HashMap<>();
    private Map<Long,String> education  = new HashMap<>();
    private Map<Long,String> industry = new HashMap<>();
    private Map<Long, String> keySkills = new HashMap<>();
    private Map<Long, String> noticePeriod = new HashMap<>();
    private List<ScreeningQuestions> screeningQuestions = new ArrayList<>();
    private ConfigSettings configSettings;
    private List<String> supportedFileFormats = new ArrayList<>();
    private List<String> supportedCvFileFormats = new ArrayList<>();
    private List<CreateJobPageSequence> addJobPages = new ArrayList<>();
    private List<String> currencyList = new ArrayList<>();
    private List<String> role = new ArrayList<>();
    private List<String> reasonForChange = new ArrayList<>();
    private Map<Long, String> defaultExportFormats = new HashMap<>();
    private List<String> callOutCome = new ArrayList<>();
    private Map<Long, String> referrerRelation = new HashMap<>();
    private Map<Long, String> jobType = new HashMap<>();
    private Map<String, Long> stageStepMasterMap = new LinkedHashMap<>();
    private List<String> interviewType = new ArrayList<>();
    private List<String> interviewMode = new ArrayList<>();
    private Map<Long, String> cancellationReasons = new HashMap<>();
    private Map<Long, String> noShowReasons = new HashMap<>();
    private List<String> interviewConfirmation = new ArrayList<>();
    private int otpExpiryMinutes;
}