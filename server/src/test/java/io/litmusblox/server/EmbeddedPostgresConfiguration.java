/*
 * Copyright © Litmusblox 2019. All rights reserved.
 */

package io.litmusblox.server;

import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.springframework.context.annotation.*;

import javax.sql.DataSource;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.Statement;

/**
 * @author : Shital Raval
 * Date : 1/11/19
 * Time : 4:50 PM
 * Class Name : EmbeddedPostgresConfiguration
 * Project Name : server
 */
@Configuration
@ComponentScan
@Profile("test")
@Log4j2
public class EmbeddedPostgresConfiguration {
    @Bean
    @Primary
    public DataSource inMemoryDS() throws Exception {
        DataSource embeddedPostgresDS = EmbeddedPostgres.builder()
                .start().getPostgresDatabase();

        try (Connection conn = embeddedPostgresDS.getConnection()) {
            Statement statement = conn.createStatement();
            statement.execute("CREATE DATABASE integrationTestsDb");
            statement.execute("CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\";");

            ScriptRunner sr = new ScriptRunner(conn);
            log.info("executing createDb script ");

            try {
                InputStream is = getClass().getResourceAsStream("/createDb.sql");

                //Creating a reader object
                Reader reader = new InputStreamReader(is);
                //Running the script
                sr.runScript(reader);

                log.info("executing insert data script");

                reader = new InputStreamReader(getClass().getResourceAsStream("/insertData.sql"));
                sr.runScript(reader);

                log.info("Done with executing sql scripts");
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

        return embeddedPostgresDS;
    }
}
