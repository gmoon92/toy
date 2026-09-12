package com.gmoon.cacheinvalidation.core.invalidation;

import com.gmoon.cacheinvalidation.core.event.ChangeSource;
import com.gmoon.cacheinvalidation.core.event.EntityChange;

public interface CacheInvalidator {

	void invalidate(EntityChange change, ChangeSource source);
}
