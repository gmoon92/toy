package com.gmoon.junit5.jupiter.argumentssource.aggregator;

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;

import com.gmoon.junit5.member.Member;
import com.gmoon.junit5.member.Role;

public class MemberAggregator implements ArgumentsAggregator {

	@Override
	public Object aggregateArguments(ArgumentsAccessor accessor, ParameterContext context) throws
		 ArgumentsAggregationException {
		Member member = new Member();
		member.setName(accessor.getString(0));
		member.setRole(accessor.get(1, Role.class));
		member.setEnabled(accessor.getBoolean(2));
		return member;
	}
}
