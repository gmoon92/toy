package com.gmoon.springbatch.global;

import lombok.Data;

import java.io.Serializable;

//public record StudentItem(
//	 String emailAddress,
//	 String name,
//	 long createdTime
//) implements Serializable {

@Data
public class StudentProItem implements Serializable{
	 private String emailAddress;
	 private String name;
	 private long createdTim;


}
