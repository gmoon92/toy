package com.gmoon.springpoi.common.utils;

import com.github.f4b6a3.tsid.Tsid;
import com.github.f4b6a3.tsid.TsidCreator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TsidUtil {

	public static String generate() {
		Tsid tsid4096 = TsidCreator.getTsid4096();
		return tsid4096.toString();
	}
}
