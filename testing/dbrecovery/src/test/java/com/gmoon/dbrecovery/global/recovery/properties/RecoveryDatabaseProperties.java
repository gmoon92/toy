package com.gmoon.dbrecovery.global.recovery.properties;

import com.gmoon.javacore.util.BooleanUtils;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "service.dbrecovery")
//@ConstructorBinding
@RequiredArgsConstructor
@ToString
public class RecoveryDatabaseProperties {

	public final Boolean enable;
	public final String schema;
	public final String recoverySchema;

	public boolean isEnabled() {
		return BooleanUtils.toBoolean(enable);
	}

	public String getSchema() {
		return schema;
	}

	public String getRecoverySchema() {
		return recoverySchema;
	}
}
