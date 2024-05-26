package com.gmoon.hibernatetype.global.type;

import java.io.Serial;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import com.gmoon.hibernatetype.global.crypt.CryptoUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EncryptedStringType implements UserType<String>, Serializable {

	@Serial
	private static final long serialVersionUID = -6430302304058598208L;

	@Override
	public int getSqlType() {
		return Types.VARCHAR;
	}

	@Override
	public Class<String> returnedClass() {
		return String.class;
	}

	@Override
	public boolean equals(String x, String y) {
		return Objects.equals(x, y);
	}

	@Override
	public int hashCode(String x) {
		return x.hashCode();
	}

	@Override
	public String nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner) throws
		 SQLException {
		String value = rs.getString(position);
		if (Objects.isNull(value)) {
			return null;
		}

		return decrypt(value);
	}

	private String decrypt(String value) {
		try {
			return CryptoUtils.decrypt(value, ColumnEncryptionConstants.KEY_SPEC);
		} catch (Exception e) {
			return value;
		}
	}

	@Override
	public void nullSafeSet(PreparedStatement preparedStatement, String value, int index,
		 SharedSessionContractImplementor session) throws SQLException {
		if (value == null) {
			preparedStatement.setNull(index, getSqlType());
		} else {
			preparedStatement.setString(index, encrypt(value));
		}
	}

	private String encrypt(String value) {
		try {
			return CryptoUtils.encrypt(value, ColumnEncryptionConstants.KEY_SPEC);
		} catch (Exception e) {
			return value;
		}
	}

	@Override
	public String deepCopy(String value) {
		return value;
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public Serializable disassemble(String value) {
		return value;
	}

	@Override
	public String assemble(Serializable cached, Object owner) {
		return (String)cached;
	}
}
