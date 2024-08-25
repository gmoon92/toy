package com.gmoon.hibernateenvers.revision.domain.vo;

import lombok.Getter;

@Getter
public enum RevisionStatus {
	WAIT, ERROR, UNCHANGED, DIRTY_CHECKING
}
