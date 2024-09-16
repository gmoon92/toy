package com.gmoon.timesorteduniqueidentifier.users.user.application.port.out;

import com.gmoon.timesorteduniqueidentifier.users.user.domain.User;

public interface UpdateUserPasswordPort {

	User save(User user);
}
