package com.gmoon.resourceserver.cors;

import java.util.List;

public interface CorsHttpMethodRepositoryCustom {
	List<String> findAllByEnabled();
}
