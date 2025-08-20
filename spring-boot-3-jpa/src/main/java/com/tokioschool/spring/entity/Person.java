package com.tokioschool.spring.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="persons")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @ToString
@Builder
public class Person {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String name;
	private String lastname;

	@Transient
	public String fullName;

	@Embedded
	private Audit audit;

	@Column(name = "progaming_language")
	private String programingLanguage;

	@PostLoad
	public void postLoad(){
		this.fullName = this.name + " "+ this.lastname;
	}
}
