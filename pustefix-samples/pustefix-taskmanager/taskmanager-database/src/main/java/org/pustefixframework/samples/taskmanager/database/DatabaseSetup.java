package org.pustefixframework.samples.taskmanager.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

public class DatabaseSetup {

    private DataSource dataSource;
    
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    public void setup() throws SQLException {
        Connection con = null;
        try {
            con = dataSource.getConnection();
            if(!tableExists(con, "user")) {
                Statement stmt = con.createStatement();
                stmt.execute(
                        "CREATE TABLE user (" +
                        "  id       INTEGER GENERATED BY DEFAULT AS IDENTITY," +
                        "  name     VARCHAR(80) NOT NULL," +
                        "  password VARCHAR(80) NOT NULL," +
                        "  email    VARCHAR(80)," +
                        "  PRIMARY KEY (id)," +
                        "  UNIQUE (name)" +
                        ")");
                stmt.close();
            }
            if(!tableExists(con, "tasklist")) {
                Statement stmt = con.createStatement();
                stmt.execute(
                        "CREATE TABLE tasklist (" +
                        "  id          INTEGER GENERATED BY DEFAULT AS IDENTITY," +
                        "  user        INTEGER NOT NULL," +
                        "  name        VARCHAR(80)," +
                        "  description VARCHAR(255)," +
                        "  PRIMARY KEY (id)," +
                        "  FOREIGN KEY (user) REFERENCES user(id) ON DELETE CASCADE" +
                        ")");
                stmt.close();
            }
            if(!tableExists(con, "priority")) {
                Statement stmt = con.createStatement();
                stmt.execute(
                        "CREATE TABLE priority (" +
                        "  id   INTEGER GENERATED BY DEFAULT AS IDENTITY," +
                        "  name VARCHAR(10) NOT NULL," +
                        "  PRIMARY KEY (id)," +
                        "  UNIQUE (name)" +
                        ")");
                stmt.close();
            } 
            if(!tableExists(con, "state")) {
                Statement stmt = con.createStatement();
                stmt.execute(
                        "CREATE TABLE state (" +
                        "  id   INTEGER GENERATED BY DEFAULT AS IDENTITY," +
                        "  name VARCHAR(10) NOT NULL," +
                        "  PRIMARY KEY (id)," +
                        "  UNIQUE (name)" +
                        ")");
                stmt.close();
            }
            if(!tableExists(con, "task")) {
                Statement stmt = con.createStatement();
                stmt.execute(
                        "CREATE TABLE task (" +
                        "  id            INTEGER GENERATED BY DEFAULT AS IDENTITY," +
                        "  tasklist      INTEGER NOT NULL," +
                        "  summary       VARCHAR(80)," +
                        "  description   VARCHAR(255)," +
                        "  priority      INTEGER NOT NULL," +
                        "  state         INTEGER NOT NULL," +
                        "  creation_date DATE NOT NULL," +
                        "  target_date   DATE NOT NULL," +
                        "  PRIMARY KEY (id)," +
                        "  FOREIGN KEY (tasklist) REFERENCES tasklist(id) ON DELETE CASCADE," +
                        "  FOREIGN KEY (priority) REFERENCES priority(id)," +
                        "  FOREIGN KEY (state) REFERENCES state(id)" +
                        ")");
                stmt.close();
            }
            if(!tableExists(con, "task_dependency")) {
                Statement stmt = con.createStatement();
                stmt.execute(
                        "CREATE TABLE task_dependency (" +
                        "  task_source INTEGER NOT NULL," +
                        "  task_target INTEGER NOT NULL," +
                        "  PRIMARY KEY (task_source, task_target)," +
                        "  FOREIGN KEY (task_source) REFERENCES task(id) ON DELETE CASCADE," +
                        "  FOREIGN KEY (task_target) REFERENCES task(id) ON DELETE CASCADE" +
                        ")");
                stmt.close();
            }
            
            Statement stmt = con.createStatement();
            ResultSet result = stmt.executeQuery("SELECT * FROM priority WHERE id=0");
            if(!result.next()) {
            	stmt.close();
                stmt = con.createStatement();
                stmt.execute("INSERT INTO priority (name) VALUES ('normal')");
            }
            
            stmt = con.createStatement();
            result = stmt.executeQuery("SELECT * FROM state WHERE id=0");
            if(!result.next()) {
            	stmt.close();
                stmt = con.createStatement();
                stmt.execute("INSERT INTO state (name) VALUES ('open')");
            }
            
            stmt = con.createStatement();
            result = stmt.executeQuery("SELECT * FROM user WHERE name='test'");
            if(!result.next()) {
                stmt.close();
                stmt = con.createStatement();
                stmt.execute("INSERT INTO user (name,password) VALUES ('test', 'test')");
                stmt.close();
                stmt = con.createStatement();
                stmt.execute("INSERT INTO tasklist (user, name, description) VALUES (0, 'Home tasks', 'Tasks to be done at home')");
                stmt.close();
                stmt = con.createStatement();
                stmt.execute("INSERT INTO task (tasklist, summary, description, priority, state, creation_date, target_date) VALUES (0, 'A task', 'A task to be done', 0, 0, '2005-07-16 07:39:25', '2005-07-16 07:39:25')");
                stmt.close();
                stmt = con.createStatement();
                stmt.execute("INSERT INTO task (tasklist, summary, description, priority, state, creation_date, target_date) VALUES (0, 'A task', 'A task to be done', 1, 1, '2005-07-16 07:39:25', '2005-07-16 07:39:25')");
                stmt.close();
            }
            
            stmt = con.createStatement();
            result = stmt.executeQuery("SELECT * FROM user WHERE name='guest'");
            if(!result.next()) {
                stmt.close();
                stmt = con.createStatement();
                stmt.execute("INSERT INTO user (name,password) VALUES ('guest', 'guest')");
                stmt.close();
                stmt = con.createStatement();
                stmt.execute("INSERT INTO tasklist (user, name, description) VALUES (1, 'Work tasks', 'Tasks to be done at work')");
                stmt.close();
            }
           
        
        } finally {
            if(con != null) con.close();
        } 
    
    }

    private boolean tableExists(Connection connection, String tableName) throws SQLException {
        ResultSet result = null;
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            result = metaData.getTables(null, null, tableName.toUpperCase(), null);
            return result.next();
        } finally {
            if(result != null) result.close();
        }
    }
    
}
