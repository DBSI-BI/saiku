package org.saiku.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.h2.jdbcx.JdbcDataSource;
import org.saiku.service.datasource.IDatasourceManager;
import org.saiku.service.importer.LegacyImporter;
import org.saiku.service.importer.LegacyImporterImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Created by lixingxing on 03/06/2017.
 */
public class DatabaseForH2 {

	private String url;
	private String user;
	private String password;
	private String encrypt;
	private JdbcDataSource ds;
	private static final Logger log = LoggerFactory
			.getLogger(DatabaseForH2.class);
	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	private IDatasourceManager dsm;

	public DatabaseForH2(String url, String user, String password,
			String encrypt, IDatasourceManager dsm) {
		this.url = url;
		this.user = user;
		this.password = password;
		this.encrypt = encrypt;
		this.dsm = dsm;
	}

	public void setDatasourceManager(IDatasourceManager dsm) {
		this.dsm = dsm;
	}

	public void init() throws SQLException {
		initDB();
		loadUsers();
		loadLegacyDatasources();
	}

	private void initDB() {
		ds = new JdbcDataSource();
		ds.setURL(url);
		ds.setUser(user);
		ds.setPassword(password);
	}

	private void loadUsers() throws SQLException {

        Connection c = ds.getConnection();

        Statement statement = c.createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS LOG(time TIMESTAMP AS CURRENT_TIMESTAMP NOT NULL, log CLOB);");

        statement.execute("CREATE TABLE IF NOT EXISTS USERS(user_id INT(11) NOT NULL AUTO_INCREMENT, " +
                "username VARCHAR(45) NOT NULL UNIQUE, password VARCHAR(100) NOT NULL, email VARCHAR(100), " +
                "enabled TINYINT NOT NULL DEFAULT 1, PRIMARY KEY(user_id));");

        statement.execute("CREATE TABLE IF NOT EXISTS USER_ROLES (\n"
                + "  user_role_id INT(11) NOT NULL AUTO_INCREMENT,username VARCHAR(45),\n"
                + "  user_id INT(11) NOT NULL REFERENCES USERS(user_id),\n"
                + "  ROLE VARCHAR(45) NOT NULL,\n"
                + "  PRIMARY KEY (user_role_id));");

        ResultSet result = statement.executeQuery("select count(*) as c from LOG where log = 'insert users'");
        result.next();
        if (result.getInt("c") == 0) {
            dsm.createUser("admin");
            dsm.createUser("smith");
            statement.execute("INSERT INTO users(username,password,email, enabled)\n"
                    + "VALUES ('admin','admin', 'test@admin.com',TRUE);" +
                    "INSERT INTO users(username,password,enabled)\n"
                    + "VALUES ('smith','smith', TRUE);");
            statement.execute(
                    "INSERT INTO user_roles (user_id, username, ROLE)\n"
                            + "VALUES (1, 'admin', 'ROLE_USER');" +
                            "INSERT INTO user_roles (user_id, username, ROLE)\n"
                            + "VALUES (1, 'admin', 'ROLE_ADMIN');" +
                            "INSERT INTO user_roles (user_id, username, ROLE)\n"
                            + "VALUES (2, 'smith', 'ROLE_USER');");

            statement.execute("INSERT INTO LOG(log) VALUES('insert users');");
        }

        if(this.encrypt.equals("true") && !checkUpdatedEncyption()){
            log.debug("Encrypting User Passwords");
            updateForEncyption();
            log.debug("Finished Encrypting Passwords");
        }


    }

    private boolean checkUpdatedEncyption() throws SQLException{
        Connection c = ds.getConnection();

        Statement statement = c.createStatement();
        ResultSet result = statement.executeQuery("select count(*) as c from LOG where log = 'update passwords'");
        result.next();
        return result.getInt("c") != 0;
    }
    private void updateForEncyption() throws SQLException {
        Connection c = ds.getConnection();

        Statement statement = c.createStatement();
        statement.execute("ALTER TABLE users ALTER COLUMN password VARCHAR(100) DEFAULT NULL");

        ResultSet result = statement.executeQuery("select username, password from users");

        while(result.next()){
            statement = c.createStatement();

            String pword = result.getString("password");
            String hashedPassword = passwordEncoder.encode(pword);
            String sql = "UPDATE users " +
                        "SET password = '"+hashedPassword+"' WHERE username = '"+result.getString("username")+"'";
            statement.executeUpdate(sql);
        }
        statement = c.createStatement();

        statement.execute("INSERT INTO LOG(log) VALUES('update passwords');");

    }

	private void loadLegacyDatasources() throws SQLException {
		Connection c = ds.getConnection();

		Statement statement = c.createStatement();
		ResultSet result = statement
				.executeQuery("select count(*) as c from LOG where log = 'insert datasources'");

		result.next();
		if (result.getInt("c") == 0) {
			LegacyImporter l = new LegacyImporterImpl(dsm);
			l.importSchema();
			l.importDatasources();
			statement
					.execute("INSERT INTO LOG(log) VALUES('insert datasources');");

		}
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEncrypt() {
		return encrypt;
	}

	public void setEncrypt(String encrypt) {
		this.encrypt = encrypt;
	}
}
