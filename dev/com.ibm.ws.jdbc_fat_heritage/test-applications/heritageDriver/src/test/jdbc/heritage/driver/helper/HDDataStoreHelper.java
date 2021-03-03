/*******************************************************************************
 * Copyright (c) 2021 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package test.jdbc.heritage.driver.helper;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.security.auth.Subject;

import com.ibm.ws.jdbc.heritage.AccessIntent;
import com.ibm.ws.jdbc.heritage.DataStoreHelperMetaData;
import com.ibm.ws.jdbc.heritage.GenericDataStoreHelper;

import test.jdbc.heritage.driver.HDConnection;

/**
 * Data store helper for the test JDBC driver.
 */
public class HDDataStoreHelper extends GenericDataStoreHelper {
    private final HDDataStoreHelperMetaData metadata = new HDDataStoreHelperMetaData();

    private final int defaultQueryTimeout;

    private AtomicReference<?> dsConfigRef;

    public HDDataStoreHelper(Properties props) {
        String value = props == null ? null : props.getProperty("queryTimeout");
        defaultQueryTimeout = value == null || value.length() <= 0 ? 0 : Integer.parseInt(value);
    }

    @Override
    public boolean doConnectionCleanup(Connection con) throws SQLException {
        ((HDConnection) con).setClientInfoKeys(); // defaults
        return false;
    }

    @Override
    public boolean doConnectionCleanupPerCloseConnection(Connection con, boolean isCMP, Object unused) throws SQLException {
        ((HDConnection) con).cleanupCount.incrementAndGet();
        try (CallableStatement stmt = con.prepareCall("CALL SYSCS_UTIL.SYSCS_SET_STATISTICS_TIMING(0)")) {
            stmt.execute();
        }
        return true;
    }

    @Override
    public void doConnectionSetup(Connection con) throws SQLException {
        try (CallableStatement stmt = con.prepareCall("CALL SYSCS_UTIL.SYSCS_SET_RUNTIMESTATISTICS(1)")) {
            stmt.setPoolable(false);
            stmt.execute();
        }
    }

    @Override
    public boolean doConnectionSetupPerGetConnection(Connection con, boolean isCMP, Object props) throws SQLException {
        ((HDConnection) con).setupCount.incrementAndGet();
        try (CallableStatement stmt = con.prepareCall("CALL SYSCS_UTIL.SYSCS_SET_STATISTICS_TIMING(1)")) {
            stmt.execute();
        }
        return true;
    }

    @Override
    public void doConnectionSetupPerTransaction(Subject subject, String user, Connection con, boolean reauthRequired, Object props) throws SQLException {
        AtomicInteger count = ((HDConnection) con).transactionCount;
        boolean first = Boolean.parseBoolean(((Properties) props).getProperty("FIRST_TIME_CALLED"));
        if (first)
            count.set(1);
        else
            count.incrementAndGet();
    }

    @Override
    public void doStatementCleanup(PreparedStatement stmt) throws SQLException {
        stmt.setCursorName(null);
        stmt.setFetchDirection(ResultSet.FETCH_FORWARD);
        stmt.setMaxFieldSize(HDConnection.DEFAULT_MAX_FIELD_SIZE);
        stmt.setMaxRows(0);

        Integer queryTimeout = dsConfigRef == null ? null : (Integer) readConfig("queryTimeout");
        if (queryTimeout == null)
            queryTimeout = defaultQueryTimeout;
        stmt.setQueryTimeout(queryTimeout);
    }

    @Override
    public int getIsolationLevel(AccessIntent unused) {
        return Connection.TRANSACTION_SERIALIZABLE;
    }

    @Override
    public DataStoreHelperMetaData getMetaData() {
        return metadata;
    }

    private Object readConfig(String fieldName) {
        Object dsConfig = dsConfigRef.get();
        try {
            return AccessController.doPrivileged((PrivilegedExceptionAction<?>) () -> //
            dsConfig.getClass().getField(fieldName).get(dsConfig));
        } catch (PrivilegedActionException x) {
            throw new RuntimeException(x);
        }
    }

    @Override
    public void setConfig(Object configRef) {
        dsConfigRef = (AtomicReference<?>) configRef;
    }
}