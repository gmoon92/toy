package com.gmoon.hibernateenvers.revision.infra.exception;

import jakarta.persistence.EntityNotFoundException;

public class AuditedEntityNotFoundException extends EntityNotFoundException {

	public AuditedEntityNotFoundException(
		 Class<?> entityClass,
		 Object entityId,
		 Long revisionNumber,
		 Exception cause
	) {
		super(String.format(
			 "Not found audited entity... revision: %d, id: %s %n entityClass: %s",
			 revisionNumber, entityId, entityClass), cause);
	}
}
