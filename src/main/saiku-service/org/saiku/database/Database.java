package org.saiku.database;

import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletContext;

import org.saiku.service.datasource.IDatasourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by bugg on 01/05/14.
 */

// Modify by huanqiang 2017-05-22 12:26:26.
public class Database {
    private static final Logger log = LoggerFactory.getLogger(Database.class);

    @Autowired
    ServletContext servletContext;

    private String datasourcetype;
    private IDatasourceManager dsm;

    public Database() {

    }

    public void setDatasourceManager(IDatasourceManager dsm) {
        this.dsm = dsm;
    }

    public IDatasourceManager getDsm() {
        return dsm;
    }

    public void setDsm(IDatasourceManager dsm) {
        this.dsm = dsm;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void init() throws SQLException {
        String url = servletContext.getInitParameter("db.url");
        String user = servletContext.getInitParameter("db.user");
        String password = servletContext.getInitParameter("db.password");
        String encrypt = servletContext.getInitParameter("db.encryptpassword");

        if (this.datasourcetype.equals("mysql")) {
            DatabaseForMysql dbMysql = new DatabaseForMysql(url, user,
                    password, encrypt,dsm);
            dbMysql.init();
        } else if (this.datasourcetype.equals("h2")) {
            DatabaseForH2 dbH2 = new DatabaseForH2(url, user, password, encrypt,dsm);
            dbH2.init();
        }
    }

    public List<String> getUsers() throws java.sql.SQLException {
        // Stub for EE.
        return null;
    }

    public void addUsers(List<String> l) throws java.sql.SQLException {
        // Stub for EE.
    }

    public String getDatasourcetype() {
        return datasourcetype;
    }

    public void setDatasourcetype(String datasourcetype) {
        this.datasourcetype = datasourcetype;
    }

}
