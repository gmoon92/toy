package com.gmoon.springaccesslog.users.model;

import com.gmoon.springaccesslog.http.annotation.ApiRequestField;
import com.gmoon.springaccesslog.http.annotation.ApiRequestModel;
import com.gmoon.springaccesslog.http.constant.ApiRequestFieldName;
import com.gmoon.springaccesslog.users.domain.User;
import lombok.*;

@ApiRequestModel
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class UserForm {

	@ApiRequestField(ApiRequestFieldName.USERNAME)
	private String username;
	@ApiRequestField(ApiRequestFieldName.AGE)
	private Integer age;

	public UserForm(User user) {
		username = user.getUsername();
		age = user.getAge();
	}
}
