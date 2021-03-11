/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.impl.Scoring;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.litmusblox.server.model.Job;
import io.litmusblox.server.model.JobCandidateMapping;
import io.litmusblox.server.model.JobScreeningQuestions;
import io.litmusblox.server.service.IScoreService;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author : sameer
 * Date : 04/03/21
 * Time : 5:41 PM
 * Class Name : ScoreService
 * Project Name : server
 */
@Service
@Log4j2
public class ScoreService implements IScoreService {

    public Map<String, Map> scoreJcm(Job job, JobCandidateMapping jcm) {
        Map<String, Map>  categoryQuestionScore = new HashMap<>();
        try {
            long startTime = System.currentTimeMillis();
            List<JobScreeningQuestions> screeningQuestions = null != job.getExpectedAnswer() ? Arrays.asList(new ObjectMapper().treeToValue(job.getExpectedAnswer(), JobScreeningQuestions[].class)) : job.getJobScreeningQuestionsList();
            if(screeningQuestions.size()>0){
                screeningQuestions.forEach(question -> {

                    if(
                            null != question.getTechScreeningQuestionId() && null == categoryQuestionScore.get(question.getTechScreeningQuestionId().getQuestionCategory())
                    ){
                        categoryQuestionScore.put(question.getTechScreeningQuestionId().getQuestionCategory(), new HashMap<String, ArrayList>());
                        categoryQuestionScore.get(question.getTechScreeningQuestionId().getQuestionCategory()).put("questions", new ArrayList<Score>());
                    }

                    if(null != question.getTechScreeningQuestionId()){
                        Long optionCount = null != question.getTechScreeningQuestionId().getAnswerSelection()?
                                Long.parseLong(question.getTechScreeningQuestionId().getAnswerSelection().replaceAll("[^0-9]", "")):null;

                        String [] candidateResponse = null != jcm.getCandidateChatbotResponse() && null != jcm.getCandidateChatbotResponse().get(question.getId().toString())?(jcm.getCandidateChatbotResponse().get(question.getId().toString()).split("%\\$")): null;

                        Score score = new Score();

                        score.setQuestion(question.getTechScreeningQuestionId().getTechQuestion());
                        score.setExpectedAnswer(question.getTechScreeningQuestionId().getDefaultAnswers());
                        score.setCandidateResponse(candidateResponse);
                        score.setQuestionTag(question.getTechScreeningQuestionId().getQuestionTag());
                        if("Flat".equals(question.getTechScreeningQuestionId().getScoringType()) && null != optionCount){

                            if(null == candidateResponse){
                                score.setScore("Greyed");
                            }
                            else if(Arrays.stream(candidateResponse).anyMatch(s -> s.contains("no experience") || s.contains("I wish not to answer"))){
                                score.setScore("Missing");
                            }
                            else if(optionCount == candidateResponse.length) {
                                if (Arrays.asList(score.getExpectedAnswer()).containsAll(Arrays.asList(candidateResponse))) {
                                    score.setScore("At_Par");
                                } else {
                                    score.setScore("Weaker");
                                }
                            }
                            else if(optionCount < candidateResponse.length){
                                long expectedAnswerSelectionCount = Arrays.stream(score.getExpectedAnswer())
                                        .filter(s -> Arrays.stream(candidateResponse).anyMatch(s1 -> s1.equals(s)))
                                        .count();
                                if(expectedAnswerSelectionCount<optionCount){
                                    score.setScore("Weaker");
                                }
                                else if(expectedAnswerSelectionCount>optionCount){
                                    score.setScore("Stronger");
                                }
                                else {
                                    score.setScore("At_Par");
                                }
                            }
                            else if(optionCount>candidateResponse.length){
                                score.setScore("Weaker");
                            }
                        }
                        else if("Graded".equals(question.getTechScreeningQuestionId().getScoringType())){
                            int expectedAnswerIndex=-1;
                            int answerIndex = -1;
                            if(null != score.getExpectedAnswer()){
                                expectedAnswerIndex = Arrays.asList(question.getTechScreeningQuestionId().getOptions()).indexOf(score.getExpectedAnswer()[0]);
                            }

                            if(null != candidateResponse){
                                answerIndex = Arrays.asList(question.getTechScreeningQuestionId().getOptions()).indexOf(score.getCandidateResponse()[0]);
                                if (Arrays.stream(candidateResponse).anyMatch(s -> s.contains("no experience") || s.contains("I wish not to answer"))){
                                    score.setScore("Missing");
                                }
                                else if( answerIndex < expectedAnswerIndex ){
                                    score.setScore("Weaker");
                                }
                                else if( answerIndex == expectedAnswerIndex ){
                                    score.setScore("At_Par");
                                }
                                else{
                                    score.setScore("Stronger");
                                }
                            }
                            else{
                                score.setScore("Greyed");
                            }
                        }

                        Map<String, ArrayList> categoryMap = categoryQuestionScore.get(question.getTechScreeningQuestionId().getQuestionCategory());
                        categoryMap.get("questions").add(score);

                    }
                });

                categoryQuestionScore.keySet().forEach(category -> {
                    List<Score> categoryScores = (List<Score>) categoryQuestionScore.get(category).get("questions");
                    long correctAnswerCount = categoryScores.stream().filter(obj -> obj.getScore().equals("Stronger") || obj.getScore().equals("At Par")).count();
                    categoryQuestionScore.get(category).put("overallScore", ((correctAnswerCount*100)/categoryScores.size()));
                    categoryQuestionScore.get(category).put("overallScoreCount", correctAnswerCount);
            });
            }

        }catch (Exception e){
            log.error(e.getMessage(), e.getCause());
        }
        return categoryQuestionScore;
    }
}


@Data
class Score{
    private String question;
    String [] expectedAnswer;
    String [] candidateResponse;
    String questionTag;
    String score;
}