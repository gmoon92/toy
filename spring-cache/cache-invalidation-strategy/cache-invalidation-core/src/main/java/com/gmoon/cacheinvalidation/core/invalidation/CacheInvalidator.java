package com.gmoon.cacheinvalidation.core.invalidation;

import com.gmoon.cacheinvalidation.core.invalidation.change.ChangeSource;
import com.gmoon.cacheinvalidation.core.invalidation.change.EntityChange;

public interface CacheInvalidator {

	void invalidate(EntityChange change, ChangeSource source);
}
