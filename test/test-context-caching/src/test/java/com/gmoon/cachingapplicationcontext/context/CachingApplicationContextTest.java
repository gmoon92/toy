package com.gmoon.cachingapplicationcontext.context;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationContext;

import com.gmoon.cachingapplicationcontext.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class CachingApplicationContextTest {

	static final Map<Integer, ApplicationContext> CONTEXT_CACHE = new HashMap<>();

	@AfterAll
	static void afterAll() {
		log.info("cache context size: {}", CONTEXT_CACHE.size());
	}

	@Nested
	@SpringBootTest
	class Case1Test {

		@Autowired
		ApplicationContext context;

		@Test
		void case1() {
			CONTEXT_CACHE.put(context.hashCode(), context);
		}

		@Test
		void case2() {
			CONTEXT_CACHE.put(context.hashCode(), context);
		}
	}

	@Nested
	@SpringBootTest
	class Case2Test {

		@Autowired
		ApplicationContext context;
		@MockBean
		UserService userService;

		@Test
		void case1() {
			CONTEXT_CACHE.put(context.hashCode(), context);
		}

		@Test
		void case2() {
			CONTEXT_CACHE.put(context.hashCode(), context);
		}
	}

	@Nested
	@SpringBootTest
	class Case3Test {

		@Autowired
		ApplicationContext context;

		@Test
		void case1() {
			CONTEXT_CACHE.put(context.hashCode(), context);
		}

		@Test
		void case2() {
			CONTEXT_CACHE.put(context.hashCode(), context);
		}
	}

	@Nested
	@SpringBootTest
	class Case4Test {

		@Autowired
		ApplicationContext context;
		@MockBean
		UserService userService;

		@Test
		void case1() {
			CONTEXT_CACHE.put(context.hashCode(), context);
		}

		@Test
		void case2() {
			CONTEXT_CACHE.put(context.hashCode(), context);
		}
	}

	@Nested
	@SpringBootTest
	class Case5Test {

		@Autowired
		ApplicationContext context;
		@SpyBean
		UserService userService;

		@Test
		void case1() {
			CONTEXT_CACHE.put(context.hashCode(), context);
		}

		@Test
		void case2() {
			CONTEXT_CACHE.put(context.hashCode(), context);
		}
	}
}
