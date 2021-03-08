/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.service.impl;

import io.litmusblox.server.constant.IConstant;
import io.litmusblox.server.constant.IErrorMessages;
import io.litmusblox.server.error.ValidationException;
import io.litmusblox.server.error.WebException;
import io.litmusblox.server.model.Company;
import io.litmusblox.server.model.Country;
import io.litmusblox.server.model.User;
import io.litmusblox.server.repository.*;
import io.litmusblox.server.security.JwtTokenUtil;
import io.litmusblox.server.service.AbstractAccessControl;
import io.litmusblox.server.service.LoginResponseBean;
import io.litmusblox.server.service.UserWorkspaceBean;
import io.litmusblox.server.utils.Util;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Userdetails implementation class
 *
 * @author : Shital Raval
 * Date : 18/7/19
 * Time : 10:43 AM
 * Class Name : LbUserDetailsService
 * Project Name : server
 */
@Service
@Log4j2
public class LbUserDetailsService extends AbstractAccessControl implements UserDetailsService {

    @Resource
    UserRepository userRepository;

    @Resource
    JobCandidateMappingRepository jobCandidateMappingRepository;

    @Resource
    CompanyRepository companyRepository;

    @Resource
    CountryRepository countryRepository;

    @Resource
    JobRepository jobRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Resource
    JcmProfileSharingDetailsRepository jcmProfileSharingDetailsRepository;

    @Autowired
    JwtTokenUtil jwtTokenUtil;

    @Autowired
    CompanyService companyService;

    @Autowired
    ProcessOtpService processOtpService;

    private static Pattern USER_DESIGNATION_PATTERN = Pattern.compile(IConstant.REGEX_FOR_USER_DESIGNATION);

    /**
     * Implementation for login functionality which will
     * 1. authenticate the user
     * 2. generate and return the jwt token
     *
     * @param user the user to be logged in
     * @return responsebean with jwt token
     * @throws Exception
     */
    @Transactional
    public LoginResponseBean login(User user, boolean isOtpAvailable) throws Exception {
        log.info("Received login request from " + user.getEmail());
        long startTime = System.currentTimeMillis();
        final User userDetails = (User)loadUserByUsername(user.getEmail());

        //check if company is active
        if(!userDetails.getCompany().getActive()){
            log.error("Company is blocked, LoggedIn user email : {}", user.getEmail());
            throw new WebException("Litmusblox cannot log you in right now. Please contact Litmusblox for further details", HttpStatus.FORBIDDEN);
        }

        if(IConstant.UserStatus.Blocked.toString().equals(userDetails.getStatus())){
            log.error("User is blocked, email: {}", user.getEmail());
            throw new WebException("Your account has been blocked. Please contact your admin to get access to Litmusblox.", HttpStatus.FORBIDDEN);
        }

        if(!isOtpAvailable)
            authenticate(user.getEmail(), user.getPassword());
        else{
            boolean isOtpVerify = false;
            if (null != user.getOtp() && user.getOtp().length() == 4 && user.getOtp().matches("[0-9]+")) {
                    log.info("Verifying Otp: {} against email: {}", user.getOtp(), user.getEmail());
                   if(!processOtpService.verifyOtp(user.getEmail(), Integer.parseInt(user.getOtp()))){
                       log.error("OTP verification failed for {}", user.getEmail());
                       throw new WebException("OTP verification failed", HttpStatus.UNAUTHORIZED);
                   }
            }
            else {
                throw new ValidationException("Invalid OTP : " + user.getOtp(), HttpStatus.UNPROCESSABLE_ENTITY);
            }
        }

        final String token = jwtTokenUtil.generateToken(userDetails, userDetails.getId(), userDetails.getCompany().getId());

        log.info("Completed processing login request in " + (System.currentTimeMillis() - startTime) +" ms.");

        return new LoginResponseBean(userDetails.getId(), token, userDetails.getDisplayName(), userDetails.getCompany(),jobCandidateMappingRepository.getUploadedCandidateCount(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()), userDetails), userDetails.getRole(), userDetails.getWorkspaceUuid());
    }

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(userName);
        if(null == user)
            throw new UsernameNotFoundException("User not found with email: " + userName);

        return user;
    }

    private void authenticate(String username, String password) throws Exception {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }

    /**
     * Service method to create a new user
     *
     * @param user the user to be created
     * @return
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public User createUpdateUser(User user) throws Exception {
        log.info("Inside createUpdateUser for user email : {} and mobile : {}", user.getEmail(), user.getMobile());
        User userFromDb = null;
        User loggedInUser = getLoggedInUser();

        //check if the user is duplicate
        checkForDuplicateUser(user, user.getId());
        validateUser(user);

        User u = new User();

        //TODO Need revisit this code after getting screens
        Company companyObjToUse = null;
        //If Call for update user then company could not be updated
        if(null == user.getId() && IConstant.UserRole.Names.SUPER_ADMIN.equals(loggedInUser.getRole()) && null == user.getCompany().getRecruitmentAgencyId()) {
            //Check if company exists
            Company userCompany = companyRepository.findByCompanyNameIgnoreCaseAndRecruitmentAgencyIdIsNull(user.getCompany().getCompanyName());

            if (null == userCompany) {
                //Validate Company short name
                if(null == user.getCompany().getRecruitmentAgencyId() && null != user.getCompany() && null != user.getCompany().getShortName()){
                    if(IConstant.COMPANY_SHORT_NAME < user.getCompany().getShortName().length())
                        throw new ValidationException("Company short name length is greater than "+IConstant.COMPANY_SHORT_NAME+ ", Company short name : " + user.getCompany().getShortName(), HttpStatus.BAD_REQUEST);

                    if(!user.getCompany().getShortName().matches(IConstant.REGEX_TO_VALIDATE_COMPANY_SHORT_NAME))
                        throw new ValidationException(IErrorMessages.INVALID_COMPANY_SHORT_NAME+", Company short name : " + user.getCompany().getShortName(), HttpStatus.BAD_REQUEST);
                }

                //Create a company
                companyObjToUse = companyService.addCompany(new Company(user.getCompany().getCompanyName(), true, user.getCompany().getCompanyType(),null, user.getCompany().getShortName(), user.getCompany().getCountryId(), new Date(), loggedInUser.getId()), loggedInUser);
                user.setRole(IConstant.ADMIN);
            } else {
                companyObjToUse = userCompany;
            }
        }else if(null == user.getId() && null != user.getCompany() && null != user.getCompany().getRecruitmentAgencyId() && IConstant.CompanyType.AGENCY.getValue().equals(loggedInUser.getCompany().getCompanyType())){
            Company userCompany = companyRepository.findByCompanyNameIgnoreCaseAndRecruitmentAgencyId(user.getCompany().getCompanyName(), loggedInUser.getCompany().getId());
            if(null==userCompany){
                //If Client company not found then do not create company throw exception
                throw new ValidationException("Client company not found for company name : " + user.getCompany().getCompanyName() + " ,For Recruitment agency : "+loggedInUser.getCompany().getCompanyName() , HttpStatus.BAD_REQUEST);
            }else {
                if(!userCompany.getRecruitmentAgencyId().equals(loggedInUser.getCompany().getId()))
                    throw new ValidationException("Client company : " + user.getCompany().getCompanyName() + " not belonging to agency : "+loggedInUser.getCompany().getCompanyName(), HttpStatus.UNAUTHORIZED);
                companyObjToUse = userCompany;
            }
        }else if(null != user.getId()){
            //Update user
            userFromDb = userRepository.findById(user.getId()).orElse(null);
            companyObjToUse = userFromDb.getCompany();
            u.setId(userFromDb.getId());
            if(null == user.getCompanyAddressId())
                user.setCompanyAddressId(userFromDb.getCompanyAddressId());
            if(null == user.getCompanyBuId())
                user.setCompanyBuId(userFromDb.getCompanyBuId());
        }

        if(null != user.getDesignation()){
            if(!USER_DESIGNATION_PATTERN.matcher(user.getDesignation()).matches())
                throw new ValidationException(IErrorMessages.USER_DESIGNATION_NOT_VALID, HttpStatus.BAD_REQUEST);
            else
                u.setDesignation(user.getDesignation());
        }
        u.setFirstName(Util.toSentenceCase(user.getFirstName()));
        u.setLastName(Util.toSentenceCase(user.getLastName()));
        u.setEmail(user.getEmail().toLowerCase());
        if(null == user.getId() && null == companyObjToUse)
            companyObjToUse=loggedInUser.getCompany();

        //Add or update CompanyAddressId and CompanyBuId in user
        if(null != user.getCompanyAddressId()){
            Boolean isCompanyPresent = false;
            if(companyObjToUse.getRecruitmentAgencyId() != null && !companyObjToUse.getId().equals(loggedInUser.getCompany().getId())){
                if(null != companyObjToUse.getCompanyAddressList() && companyObjToUse.getCompanyAddressList().size()>0){
                    List<Long> companyAddressList = new ArrayList<>();
                    companyObjToUse.getCompanyAddressList().forEach(companyAddress -> companyAddressList.add(companyAddress.getId()));
                    if(companyAddressList.contains(user.getCompanyAddressId())){
                        u.setCompanyAddressId(user.getCompanyAddressId());
                        isCompanyPresent = true;
                    }
                }
            }
            else {
                if (null != loggedInUser.getCompany().getCompanyAddressList() && loggedInUser.getCompany().getCompanyAddressList().size() > 0) {
                    List<Long> companyAddressList = new ArrayList<>();
                    loggedInUser.getCompany().getCompanyAddressList().forEach(companyAddress -> companyAddressList.add(companyAddress.getId()));
                    if (companyAddressList.contains(user.getCompanyAddressId())) {
                        u.setCompanyAddressId(user.getCompanyAddressId());
                        isCompanyPresent = true;
                    }
                }
            }
            if(!isCompanyPresent)
                log.error("Company Address Id is not related to logged in user company, CompanyAddressId : "+user.getCompanyAddressId());
        }
        if(null != user.getCompanyBuId()){
            Boolean isCompanyPresent = false;
            if(companyObjToUse.getRecruitmentAgencyId() != null && !companyObjToUse.getId().equals(loggedInUser.getCompany().getId())){
                if(null != companyObjToUse.getCompanyBuList() && companyObjToUse.getCompanyBuList().size()>0){
                    List<Long> companyBuList = new ArrayList<>();
                    companyObjToUse.getCompanyBuList().forEach(companyBu -> companyBuList.add(companyBu.getId()));
                    if(companyBuList.contains(user.getCompanyBuId())){
                        u.setCompanyBuId(user.getCompanyBuId());
                        isCompanyPresent = true;
                    }
                }
            }
            else {
                if (null != loggedInUser.getCompany().getCompanyBuList() && loggedInUser.getCompany().getCompanyBuList().size() > 0) {
                    List<Long> companyBuList = new ArrayList<>();
                    loggedInUser.getCompany().getCompanyBuList().forEach(companyBu -> companyBuList.add(companyBu.getId()));
                    if (companyBuList.contains(user.getCompanyBuId())) {
                        u.setCompanyBuId(user.getCompanyBuId());
                        isCompanyPresent = true;
                    }
                }
            }
            if(!isCompanyPresent)
                log.error("Company Bu Id is not related to logged in user company, CompanyBuId : "+user.getCompanyBuId());
        }

        u.setCompany(companyObjToUse);
        log.info("Create and update user for companyId : {}, companyName : {}",companyObjToUse.getId(),companyObjToUse.getCompanyName());

        if (null == user.getRole()) {
            //If user role is null then set default role is Recruiter
            if(null != user.getCompany().getRecruitmentAgencyId() && !user.getCompany().getId().equals(loggedInUser.getCompany().getId())){
                u.setRole(IConstant.UserRole.Names.BUSINESS_USER);
            }
            else {
                u.setRole(IConstant.UserRole.Names.RECRUITER);
            }
        }
        else {

            if(IConstant.HR_RECRUITER.equals(user.getRole()))
                user.setRole(IConstant.UserRole.Names.RECRUITER);
            else if(IConstant.HR_HEAD.equals(user.getRole()) || IConstant.ADMIN.equals(user.getRole()))
                user.setRole(IConstant.UserRole.Names.CLIENT_ADMIN);
            else if(IConstant.HIRING_MANAGER.equals(user.getRole()) || IConstant.INTERVIEWER.equals(user.getRole()))
                user.setRole(IConstant.UserRole.Names.BUSINESS_USER);

            //set role as present in the request
            //check that the role is valid and exists in the system
            if(Arrays.stream(IConstant.UserRole.values()).anyMatch((definedRole) -> definedRole.toString().equalsIgnoreCase(user.getRole()))) {
                //if logged in user is client admin, a new user with super admin role cannot be created
                if(IConstant.UserRole.Names.CLIENT_ADMIN.equals(loggedInUser.getRole())) {
                    if(IConstant.UserRole.Names.SUPER_ADMIN.equals(user.getRole()))
                        throw new ValidationException("User with Client Admin privilege: " + loggedInUser.getEmail() + " attempted to create a user with Super Admin privilege", HttpStatus.FORBIDDEN);
                }
                u.setRole(user.getRole());
            }
            else
                throw new ValidationException("Invalid role in create user request: " + user.getRole(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if(IConstant.UserRole.Names.BUSINESS_USER.equals(user.getRole()))
            u.setUserType(IConstant.UserType.BUSINESS.getValue());
        else
            u.setUserType(IConstant.UserType.RECRUITING.getValue());

        u.setCountryId(user.getCountryId());
        u.setMobile(user.getMobile());

        //For update user password should be reset
        if(null != user.getId()){
            u.setUpdatedOn(new Date());
            u.setUpdatedBy(loggedInUser.getId());
            u.setPassword(userFromDb.getPassword());
            u.setStatus(userFromDb.getStatus());
            u.setCreatedBy(userFromDb.getCreatedBy());
            u.setCreatedOn(userFromDb.getCreatedOn());
            u.setUserUuid(userFromDb.getUserUuid());
            u.setWorkspaceUuid(userFromDb.getWorkspaceUuid());
            if(!userFromDb.getEmail().equals(user.getEmail().toLowerCase()) && !IConstant.UserType.BUSINESS.getValue().equals(userFromDb.getUserType()) && !IConstant.UserStatus.New.name().equals(userFromDb.getStatus())){
                log.info("Reset password because update email, Old email : {}, new email : {}",userFromDb.getEmail(), user.getEmail().toLowerCase());
                u.setStatus(IConstant.UserStatus.Inactive.name());
                u.setPassword(null);
                u.setResetPasswordFlag(true);
                u.setResetPasswordEmailTimestamp(null);
                u.setUserUuid(UUID.randomUUID());
            }
            log.info("Update User : {}", user.getId());
        }else{
            u.setUserUuid(UUID.randomUUID());
            u.setWorkspaceUuid(UUID.randomUUID());
            if(user.getCreatedBy() == null)
                u.setCreatedBy(loggedInUser.getId());
            else
                u.setCreatedBy(user.getCreatedBy());
            u.setCreatedOn(new Date());
        }

        if(null == user.getId())
            companyService.saveCompanyHistory(companyObjToUse.getId(), "New user with email " + user.getEmail() + " created",loggedInUser);

        log.info("Logged in userId : {} and userRole : {}, Create or update user for email : {} and mobile : {}, role : {}",loggedInUser.getId(), loggedInUser.getRole(), u.getEmail(), u.getMobile(), u.getRole());
        return userRepository.save(u);
    }

    private User getLoggedInUser() {
        return (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private void checkForDuplicateUser(User user, Long userId) throws ValidationException {
        //check if user with email exists
        User dupUser = userRepository.findByEmail(user.getEmail());
        if (null != dupUser && (null == userId || !dupUser.getId().equals(userId))) {
            log.error("Duplicate user found: " + dupUser.toString());
            throw new ValidationException(IErrorMessages.DUPLICATE_USER_EMAIL + " - " + user.getEmail(), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        //following code is commented out as a result of ticket #85:  Allow super admin to create other users for a client
        /*
        if(IConstant.UserRole.Names.SUPER_ADMIN.equals(role)) {
            //superadmin can only create the first user for any company
            //check that a user for the same company does not exist
            Company userCompany = companyRepository.findByCompanyName(user.getCompany().getCompanyName());
            if (null != userCompany) {
                List<User> usersForCompany = userRepository.findByCompanyId(userCompany.getId());
                if (null != usersForCompany && usersForCompany.size() > 0) {
                    //users for the company already exist, cannot create another
                    throw new ValidationException(IErrorMessages.CLIENT_ADMIN_EXISTS_FOR_COMPANY + user.getCompany().getCompanyName(), HttpStatus.EXPECTATION_FAILED);
                }
            }
        }
        */
    }

    private void validateUser(User user) throws ValidationException {
        if (null == user.getCountryId())
            throw new ValidationException(IErrorMessages.USER_COUNTRY_NULL, HttpStatus.UNPROCESSABLE_ENTITY);
        //validate firstName
        Util.validateName(user.getFirstName());
        //validate lastName
        Util.validateName(user.getLastName());
        //validate email
        Util.validateEmail(user.getEmail(), null);
        //validate mobile
        Country country = countryRepository.findById(user.getCountryId().getId()).orElse(null);
        if(!Util.validateMobile(user.getMobile(), null != country?country.getCountryCode():null, null))
            throw new ValidationException(IErrorMessages.MOBILE_INVALID_DATA + " - " + user.getMobile(), HttpStatus.BAD_REQUEST);

    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void setPassword(User user) throws Exception {

        if (null == user.getCurrentPassword() || null == user.getConfirmPassword())
            throw new ValidationException(IErrorMessages.PASSWORD_MISMATCH, HttpStatus.UNPROCESSABLE_ENTITY);

        //verify that password and confirm password are same
        if(!user.getCurrentPassword().equals(user.getConfirmPassword()))
            throw new ValidationException(IErrorMessages.PASSWORD_MISMATCH, HttpStatus.UNPROCESSABLE_ENTITY);

        //verify that user exists
        User userToUpdate = userRepository.findByUserUuid(user.getUserUuid());
        if (null == userToUpdate)
            throw new ValidationException(IErrorMessages.USER_NOT_FOUND, HttpStatus.UNPROCESSABLE_ENTITY);
        User userByEmail = userRepository.findByEmail(user.getEmail());
        if (null == userByEmail)
            throw new ValidationException(IErrorMessages.USER_NOT_FOUND + "- " + user.getEmail(), HttpStatus.UNPROCESSABLE_ENTITY);
        if (userToUpdate.getId() != userByEmail.getId())
            throw new ValidationException(IErrorMessages.USER_EMAIL_TOKEN_MISMATCH, HttpStatus.UNPROCESSABLE_ENTITY);

        userToUpdate.setPassword(passwordEncoder.encode(user.getCurrentPassword()));
        userToUpdate.setUserUuid(null);
        userToUpdate.setStatus(IConstant.UserStatus.Active.name());
        userToUpdate.setUpdatedOn(new Date());
        userToUpdate.setResetPasswordFlag(false);
        userToUpdate.setResetPasswordEmailTimestamp(null);
        userRepository.save(userToUpdate);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public StringBuffer forgotPassword(String email) throws Exception {
        StringBuffer msgInfo = new StringBuffer();
        if(Util.isNull(email))
            throw new ValidationException(IErrorMessages.NO_EMAIL_PROVIDED, HttpStatus.BAD_REQUEST);

        User userToReset = userRepository.findByEmail(email);
        if (null == userToReset) {
            throw new ValidationException(IErrorMessages.USER_NOT_FOUND + email, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if (IConstant.UserStatus.Blocked.name().equals(userToReset.getStatus())) {
            throw new ValidationException(IErrorMessages.FORGOT_PASSWORD_USER_BLOCKED+email, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        else if(IConstant.UserStatus.New.name().equals(userToReset.getStatus())) {
            msgInfo.append("User not activated. ").append(email).append(". Another ");
        }
        //commented out the following as a result of backend ticket #385
        /*else if(!IConstant.UserStatus.Active.name().equals(userToReset.getStatus())){
            throw new ValidationException(IErrorMessages.FORGOT_PASSWORD_DUPLICATE_REQUEST+email, HttpStatus.UNPROCESSABLE_ENTITY);
        }*/
        msgInfo.append("Set password email has been sent to the ").append(email).append(". Please check your inbox.");
        userToReset.setPassword(null);
        userToReset.setUserUuid(UUID.randomUUID());
        userToReset.setStatus(IConstant.UserStatus.Inactive.name());
        userToReset.setResetPasswordFlag(true);
        userToReset.setResetPasswordEmailTimestamp(null);
        userRepository.save(userToReset);
        return msgInfo;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void blockUser(User user, boolean blockUser) {
        User objFromDb = userRepository.getOne(user.getId());
        if (null == objFromDb)
            throw new ValidationException("Invalid user", HttpStatus.UNPROCESSABLE_ENTITY);
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        validateloggedInUser(loggedInUser, objFromDb.getCompany().getId());

        //Currently company block functionality not depend on the client admin status
        /*if(IConstant.UserRole.Names.CLIENT_ADMIN.equals(objFromDb.getRole())) {
            if(blockUser) {
                Company companyToBlock = objFromDb.getCompany();
                companyToBlock.setActive(false);
                companyToBlock.setUpdatedBy(getLoggedInUser().getId());
                companyToBlock.setUpdatedOn(new Date());
                companyRepository.save(companyToBlock);
                log.info("Blocked company " + companyToBlock.getCompanyName());
                companyService.saveCompanyHistory(companyToBlock.getId(), "Company status changed to blocked", getLoggedInUser());
            }
            else {
                if(!objFromDb.getCompany().getActive())
                    throw new ValidationException("Cannot unblock user of a blocked company", HttpStatus.BAD_REQUEST);
            }
        }
        else {*/

        if (blockUser)
            objFromDb.setStatus(IConstant.UserStatus.Blocked.name());
        else {
            if (null == objFromDb.getPassword())
                objFromDb.setStatus(IConstant.UserStatus.Inactive.name());
            else
                objFromDb.setStatus(IConstant.UserStatus.Active.name());
        }
        objFromDb.setUpdatedBy(getLoggedInUser().getId());
        objFromDb.setUpdatedOn(new Date());

        userRepository.save(objFromDb);
        companyService.saveCompanyHistory(objFromDb.getCompany().getId(), "Status of user with email, " +objFromDb.getEmail()+ ", changed to " + objFromDb.getStatus(), getLoggedInUser());
    }

    /**
     * Service method to fetch a list of all users for a company
     * @param companyId the company for which users need to be fetched
     * @return list of all users for the company
     * @throws Exception
     */
    public List<UserWorkspaceBean> fetchUsers(Long companyId,Boolean setCount ) throws Exception {
        log.info("Received request to get list of users");
        long startTime = System.currentTimeMillis();
        User loggedInUser = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Long validCompanyId = validateCompanyId(loggedInUser, companyId);
        if(!validCompanyId.equals(companyId))
            log.error("Given company id : {} and valid company id : {} both are mismatched", companyId, validCompanyId);



        List<UserWorkspaceBean> responseBeans = userRepository.findWorkspaceData(validCompanyId);
        setCount=true;
            if(setCount)
            {
                responseBeans.forEach(userWorkspaceBean ->
                {
                    User user=new User();
                    user.setId(userWorkspaceBean.getUserId());
                    userWorkspaceBean.setNumberOfJobsCreated(jobRepository.countByCreatedBy(user));
                    userWorkspaceBean.setNumOfInvites(jobCandidateMappingRepository.getInviteCount(user.getId()));
                    List<Object[]> object = jobCandidateMappingRepository.getChatbotCountCompletedAndInCompleted(user.getId());
                    if (null != (object.get(0))[0]) {
                        userWorkspaceBean.setIncompleteChatbotCount(Integer.parseInt((object.get(0))[1].toString()));
                        userWorkspaceBean.setCompletedChatbotCount(Integer.parseInt((object.get(0))[0].toString()));
                    }
                    userWorkspaceBean.setAnalyticsSharedCount(jcmProfileSharingDetailsRepository.getProfileSharingCount(user.getId()));


                });
            }



        log.info("Completed processing list of users in " + (System.currentTimeMillis() - startTime) + "ms.");
        return responseBeans;
    }

    public User findById(Long userId) throws Exception {
        return userRepository.getOne(userId);
    }

    /**
     *Service method to fetch user details
     * @param userId for which user we want details
     * @return user details
     * @throws Exception
     */
    public User getUserDetails(Long userId) throws Exception {
        log.info("Inside getUserDetails");
        User user = findById(userId);

        if(null == user)
            throw new ValidationException("User not found for userId : "+userId, HttpStatus.BAD_REQUEST);
        else{
            User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            validateloggedInUser(loggedInUser, user.getCompany().getId());
            if(IConstant.UserRole.Names.RECRUITER.equals(user.getRole()))
                user.setRole(IConstant.HR_RECRUITER);
            else if(IConstant.UserRole.Names.CLIENT_ADMIN.equals(user.getRole()))
                user.setRole(IConstant.ADMIN);
            else if(IConstant.UserRole.Names.BUSINESS_USER.equals(user.getRole()))
                user.setRole(IConstant.HIRING_MANAGER);

            return user;
        }
    }
}