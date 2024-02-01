package com.gmoon.hibernatetype.global.type;

import org.hibernate.dialect.Dialect;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.DiscriminatorType;
import org.hibernate.type.descriptor.sql.VarcharTypeDescriptor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EncryptStringType
	extends AbstractSingleColumnStandardBasicType<String>
	implements DiscriminatorType<String> {

	public EncryptStringType() {
		super(VarcharTypeDescriptor.INSTANCE, EncryptStringTypeDescriptor.INSTANCE);
	}

	@Override
	public String stringToObject(String xml) throws Exception {
		log.info("xml: {}", xml);
		return fromString(xml);
	}

	@Override
	public String objectToSQLString(String value, Dialect dialect) throws Exception {
		log.info("toSqlString: {}", value);
		return toString(value);
	}

	@Override
	public String getName() {
		return "rcrypt";
	}
}
