package com.gmoon.hibernatetype.global.type;

import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor;
import org.hibernate.type.descriptor.java.ImmutableMutabilityPlan;
import org.hibernate.type.descriptor.java.StringTypeDescriptor;

import com.gmoon.hibernatetype.global.crypt.CryptoUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EncryptedStringTypeDescriptor extends AbstractTypeDescriptor<String> {

	public static final EncryptedStringTypeDescriptor INSTANCE = new EncryptedStringTypeDescriptor();

	@SuppressWarnings({"unchecked"})
	private EncryptedStringTypeDescriptor() {
		super(String.class, ImmutableMutabilityPlan.INSTANCE);
	}

	@Override
	public String fromString(String string) {
		log.info("fromString: {}", string);
		return string;
	}

	// java -> sql
	@SuppressWarnings({"unchecked"})
	@Override
	public <X> X unwrap(String value, Class<X> type, WrapperOptions options) {
		log.info("unwrap value: {}, type: {}, options: {}", value, type, options);
		String encrypted = encrypt(value);
		return StringTypeDescriptor.INSTANCE.unwrap(encrypted, type, options);
	}

	private String encrypt(String value) {
		try {
			return CryptoUtils.encrypt(value, ColumnEncryptionConstants.KEY_SPEC);
		} catch (Exception e) {
			return value;
		}
	}

	// sql -> java
	@Override
	public <X> String wrap(X value, WrapperOptions options) {
		log.info("wrap value: {}, options: {}", value, options);
		X decrypt = decrypt(value);
		return StringTypeDescriptor.INSTANCE.wrap(decrypt, options);
	}

	private <X> X decrypt(X value) {
		try {
			return (X)CryptoUtils.decrypt((String)value, ColumnEncryptionConstants.KEY_SPEC);
		} catch (Exception e) {
			return value;
		}
	}
}
