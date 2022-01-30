package com.gmoon.resourceserver.cors;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CorsOriginRepository extends JpaRepository<CorsOrigin, Long>, CorsOriginRepositoryCustom {
}
