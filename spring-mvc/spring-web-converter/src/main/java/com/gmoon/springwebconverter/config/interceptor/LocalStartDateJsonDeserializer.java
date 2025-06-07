package com.gmoon.springwebconverter.config.interceptor;

import java.time.ZoneId;
import java.util.Date;

import com.gmoon.javacore.util.DateUtils;

public class LocalStartDateJsonDeserializer extends AbstractDateJsonDeserializer {
	@Override
	public Date getDate(Date wallTime, ZoneId userZoneId) {
		return DateUtils.truncateAndAdjust(wallTime, userZoneId);
	}
}
