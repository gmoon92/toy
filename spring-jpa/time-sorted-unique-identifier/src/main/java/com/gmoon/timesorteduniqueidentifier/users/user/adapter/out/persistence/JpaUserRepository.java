package com.gmoon.timesorteduniqueidentifier.users.user.adapter.out.persistence;

import com.gmoon.timesorteduniqueidentifier.users.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

interface JpaUserRepository extends JpaRepository<User, String> {
}
