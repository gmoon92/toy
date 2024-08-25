package com.gmoon.hibernateenvers.test;

import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.gmoon.hibernateenvers.global.config.JpaConfig;
import com.gmoon.hibernateenvers.global.envers.listener.RevisionHistoryEventListener;
import com.gmoon.hibernateenvers.revision.infra.AuditedEntityRepositoryImpl;
import com.gmoon.hibernateenvers.revision.infra.RevisionHistoryRepositoryImpl;

@Import({
	 JpaConfig.class,
	 RevisionHistoryEventListener.class,
	 AuditedEntityRepositoryImpl.class,
	 RevisionHistoryRepositoryImpl.class
})
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Target(ElementType.TYPE)
@Retention(RUNTIME)
public @interface RepositoryTest {
}
