package com.gmoon.resourceserver.cors;

import org.springframework.data.repository.CrudRepository;
import org.springframework.http.HttpMethod;

public interface CorsHttpMethodRepository extends CrudRepository<CorsHttpMethod, HttpMethod>, CorsHttpMethodRepositoryCustom {
}
