package in.ac.iisc.cds.dsl.cdgclient.connectors;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import in.ac.iisc.cds.dsl.cdgclient.constants.PostgresCConfig;
import in.ac.iisc.cds.dsl.cdgclient.constants.PostgresCConfig.Key;
import in.ac.iisc.cds.dsl.cdgvendor.utils.DebugHelper;

public enum PostgresConn {
    INSTANCE;

    private final Connection conn;

    private PostgresConn() throws ExceptionInInitializerError {

        DebugHelper.printInfo("-------- Connecting PostgreSQL JDBC ------------");
        DebugHelper.printInfo("\t" + Key.CONN_DRIVERCLASS.name() + "\t: " + PostgresCConfig.getProp(Key.CONN_DRIVERCLASS));
        DebugHelper.printInfo("\t" + Key.CONN_CONNSTRING.name() + "\t: " + PostgresCConfig.getProp(Key.CONN_CONNSTRING));
        DebugHelper.printInfo("\t" + Key.CONN_USERNAME.name() + "\t\t: " + PostgresCConfig.getProp(Key.CONN_USERNAME));
        DebugHelper.printInfo("\t" + Key.CONN_PASSWD.name() + "\t\t: ******");

        try {
            Class.forName(PostgresCConfig.getProp(Key.CONN_DRIVERCLASS));
            conn = DriverManager.getConnection(PostgresCConfig.getProp(Key.CONN_CONNSTRING), PostgresCConfig.getProp(Key.CONN_USERNAME),
                    PostgresCConfig.getProp(Key.CONN_PASSWD));
            conn.setAutoCommit(false);
        } catch (ClassNotFoundException | SQLException ex) {
            throw new ExceptionInInitializerError(ex);
        }

        if (conn == null)
            throw new ExceptionInInitializerError("Failed to make PostgreSQL JDBC connection!");
        else {
            System.out.println("-------- PostgreSQL JDBC Connected ------------");
        }

    }

    public Statement createStatement() throws SQLException {
        return conn.createStatement();
    }

    public void rollback() throws SQLException {
        conn.rollback();
    }

}