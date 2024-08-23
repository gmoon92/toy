package com.gmoon.hibernatesequencegenerator.domain;

import org.hibernate.annotations.UuidGenerator;

import com.gmoon.hibernatesequencegenerator.constants.ColumnLength;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class Company {

	@Id
	@UuidGenerator
	@Column(length = ColumnLength.SYSTEM_UUID)
	private String id;

	private String name;

	public Company(String name) {
		this.name = name;
	}
}
