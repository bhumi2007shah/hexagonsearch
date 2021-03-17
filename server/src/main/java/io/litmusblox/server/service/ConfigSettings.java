/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service;

import lombok.Data;

/**
 * POJO to capture all configuration settings
 *
 * @author : Shital Raval
 * Date : 29/7/19
 * Time : 11:22 AM
 * Class Name : ConfigSettings
 * Project Name : server
 */
@Data
public class ConfigSettings {
    int maxScreeningQuestionsLimit;
    int dailyCandidateUploadPerUserLimit;
    int dailyCandidateInviteLimit;
    int candidatesPerFileLimit;
    int sendEmail;
    int sendSms;
    int maxCvFiles;
    long maxUploadDataLimit;
    int mlCall;
    int maxCapabilities;
    int cvRatingTimeout;
    int otpExpiryMinutes;
    int maxQuestions;
    int maxQuestionsPerSkill;
}
