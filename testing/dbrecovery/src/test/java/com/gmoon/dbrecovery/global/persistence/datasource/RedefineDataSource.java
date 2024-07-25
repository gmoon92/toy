package com.gmoon.dbrecovery.global.persistence.datasource;

import java.sql.Connection;
import java.sql.SQLException;

interface RedefineDataSource {
	Connection getConnection() throws SQLException;

	Connection getConnection(String username, String password) throws SQLException;
}
