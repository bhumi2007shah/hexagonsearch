/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author : shital
 * Date : 17/1/20
 * Time : 10:11 AM
 * Class Name : TestFileio
 * Project Name : server
 */
public class TestFileio {
    public static void main(String args[]) throws Exception {
        //sudo chmod o+w /etc/apache2/sites-available
        //sudo chmod o+w /etc/apache2/sites-enabled/

        File configFile = new File("/etc/apache2/sites-available/api.conf");
        FileWriter fw = new FileWriter(configFile);
        fw.write("<VirtualHost *:443>\n" +
                "  ServerName api.litmusblox.net\n" +
                "  ServerAlias api.litmusblox.net\n" +
                "  Redirect permanent / http://api.litmusblox.net/\n" +
                "</VirtualHost>\n" +
                "<VirtualHost *:80>\n" +
                "    ServerAdmin admin@litmusblox.net\n" +
                "    ServerName  api.litmusblox.net\n" +
                "    ServerAlias api.litmusblox.net\n" +
                "\n" +
                "    ProxyRequests on\n" +
                "    ProxyPreserveHost On\n" +
                "\n" +
                "    ProxyPass / http://localhost:8080/\n" +
                "    ProxyPassReverse / http://localhost:8080/\n" +
                "\n" +
                "       ErrorLog ${APACHE_LOG_DIR}/error.log\n" +
                "        CustomLog ${APACHE_LOG_DIR}/access.log combined\n" +
                "\n" +
                "</VirtualHost>");
        fw.close();

        //create symbolic link
        Path link = Paths.get("/etc/apache2/sites-enabled/","api.conf");
        if (Files.exists(link)) {
            Files.delete(link);
        }
        Files.createSymbolicLink(link, Paths.get("/etc/apache2/sites-available/api.conf"));
    }
}