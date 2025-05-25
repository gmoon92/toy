package com.gmoon.springbeanio.example;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.beanio.annotation.Field;
import org.beanio.annotation.Fields;
import org.beanio.annotation.Record;

@Record(name = "body", minOccurs = 0, maxOccurs = -1)
@Fields({
	 @Field(at = 0, name = "recordType", rid = true, literal = "D")
})
@Getter
@Setter
@ToString
public class AnnotationUserContactDto {
	@Field(at = 1, maxLength = 20)
	private String firstName;
	@Field(at = 2, required = true, maxLength = 30)
	private String lastName;
	@Field(at = 3, maxLength = 30)
	private String street;
	@Field(at = 4, maxLength = 25)
	private String city;
	@Field(at = 5, minLength = 2, maxLength = 2)
	private String state;
	@Field(at = 6, regex = "\\d{5}")
	private String zip;
}
