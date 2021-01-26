package com.gmoon.hibernateenvers.revision.enums;

import lombok.Getter;

@Getter
public enum RevisionEventStatus {
  WAIT, ERROR, UNCHANGED, DIRTY_CHECKING
}
