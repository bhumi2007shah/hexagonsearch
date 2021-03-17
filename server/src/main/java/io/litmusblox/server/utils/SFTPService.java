/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server.utils;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;

/**
 * @author : sameer
 * Date : 02/03/21
 * Time : 1:53 PM
 * Class Name : SFTPService
 * Project Name : server
 */
@Service
@Data
@NoArgsConstructor
@AllArgsConstructor
@Log4j2
public class SFTPService {
    private String host;
    private String user;
    private String pass;
    private int port;
    private Long sessionTimeout = 10000L;
    private Long channelTimeout = 5000L;

    private Session jschSession = null;
    private ChannelSftp channelSftp = null;

    public SFTPService(String host, String user, String pass, int port) {
        this.host = host;
        this.user = user;
        this.pass = pass;
        this.port = port;
    }

    public void connect(){
        try {
            JSch jSch = new JSch();
            jSch.setKnownHosts(new FileInputStream("test"));
            this.setJschSession(jSch.getSession(this.user, this.host, this.port));

            this.getJschSession().setPassword(this.pass);
            this.getJschSession().connect();

            Channel sftp = this.getJschSession().openChannel("sftp");

            this.setChannelSftp((ChannelSftp) sftp);

            this.getChannelSftp().connect();

            this.getJschSession().setServerAliveCountMax(5);
            this.getJschSession().setServerAliveInterval(5000);

        }catch (Exception e){
            log.error(e.getMessage(), e.getCause());
        }
    }

    public void disconnect(){
        if(null != this.getChannelSftp()) {
            this.getChannelSftp().exit();
        }
        if(null != this.getJschSession()) {
            this.getJschSession().disconnect();
        }
    }

    public void uploadFile(String localPath, String remotePath){
        try {
            this.getChannelSftp().put(localPath, remotePath);
        }catch (Exception e){
            log.error(e.getMessage(), e.getCause());
        }
    }

    public void downloadFile(String remotePath, String localPath){
        try{
            this.getChannelSftp().get(remotePath, localPath);
        }catch (Exception e){
            log.error(e.getMessage(), e.getCause());
        }
    }

    @Override
    public void finalize(){
        if(null != this.getChannelSftp()) {
            this.getChannelSftp().exit();
        }
        if(null != this.getJschSession()) {
            this.getJschSession().disconnect();
        }
    }
}
