/*******************************************************************************
 * Copyright (c) 2011, 2017 Red Hat and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.kapua.service.liquibase;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class KapuaLiquibaseClient {

    private final String jdbcUrl;

    private final String username;

    private final String password;

    public KapuaLiquibaseClient(String jdbcUrl, String username, String password) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
    }

    public void update() {
        try {
            Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
            Liquibase liquibase = new Liquibase("liquibase.sql", new ClassLoaderResourceAccessor(), new JdbcConnection(connection));
            liquibase.update(null);
        } catch (LiquibaseException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
