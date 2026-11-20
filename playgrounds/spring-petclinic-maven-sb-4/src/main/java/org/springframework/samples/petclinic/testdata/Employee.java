package org.springframework.samples.petclinic.testdata;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "employees")
public class Employee {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	// @Enumerated defaults to ORDINAL.
	@Enumerated
	private EmploymentType employmentType;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "department_id")
	private Department department;

	// Hibernate must check whether this optional inverse association exists.
	@OneToOne(mappedBy = "employee", fetch = FetchType.LAZY)
	private Contract contract;

	@OneToMany(mappedBy = "employee")
	private Set<Document> documents = new HashSet<>();

	@ManyToMany
	@JoinTable(name = "employee_projects", joinColumns = @JoinColumn(name = "employee_id"),
			inverseJoinColumns = @JoinColumn(name = "project_id"))
	private Set<Project> projects = new HashSet<>();

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Department getDepartment() {
		return department;
	}

	public Contract getContract() {
		return contract;
	}

	public Set<Document> getDocuments() {
		return documents;
	}

	public Set<Project> getProjects() {
		return projects;
	}

	private enum EmploymentType {

		FULL_TIME, PART_TIME

	}

}
