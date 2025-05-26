package com.ktt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchDto {
	private String origin;
	private String destination;
	private String fromDate;
	private String toDate;
	private int adults;
	private int infants;
	private int children;
	private String cabinClass;
	private List<SearchLegDto> legs;
	private int students;
	private int seniorCitizens;
	private String providerCode ;
//	private int armedForces;
}
