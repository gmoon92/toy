package com.rsupport.rv5x.springaccesslog.users.model;

import com.rsupport.rv5x.springaccesslog.http.annotation.ApiRequestField;
import com.rsupport.rv5x.springaccesslog.http.annotation.ApiRequestModel;
import com.rsupport.rv5x.springaccesslog.http.constant.ApiRequestFieldName;
import com.rsupport.rv5x.springaccesslog.users.domain.User;
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
