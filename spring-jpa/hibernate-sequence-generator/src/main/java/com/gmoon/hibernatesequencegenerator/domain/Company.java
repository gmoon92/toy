package com.gmoon.hibernatesequencegenerator.domain;

import com.gmoon.hibernatesequencegenerator.constants.ColumnLength;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class Company {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(length = ColumnLength.SYSTEM_UUID)
	private String id;

	private String name;

	public Company(String name) {
		this.name = name;
	}
}
