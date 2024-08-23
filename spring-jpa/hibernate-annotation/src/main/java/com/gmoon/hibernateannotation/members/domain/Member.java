package com.gmoon.hibernateannotation.members.domain;

import java.io.Serializable;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
public class Member implements Serializable {

	@Id
	@UuidGenerator
	@Column(length = 50)
	private String id;

	private String name;

}
