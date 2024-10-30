Feature: Retrieve author details
	As a library member, I want to retrieve author details from the catalog to get biographical information and authored books

	Rule: Unknown authors cannot be found in the catalog

		Scenario Outline: Fail retrieving unknown author details
			Given Georges is a library member
			When he retrieves the details of an author with ID <authorId>
			Then the author details cannot be retrieved because it does not exists
			Examples:
				| authorId                             |
				| a39db01f-c0b4-4376-8a06-f4308de23cc3 |
				| 8040f7aa-4882-4941-8659-744f9af64580 |
				| 5a19fb86-2263-48ba-83d1-949796f75c3d |

	Rule: Authors in the catalog have the expected details and embed their authored books

		Scenario Outline: Retrieve known author details
			Given Georges is a library member
			When he retrieves the details of an author with ID <authorId>
			Then the author details have the expected "<name>", <dateOfBirth>, <dateOfDeath> and authored notable <books>
			Examples:
				| authorId                             | name                  | dateOfBirth | dateOfDeath | books                                                                     |
				| 83b5bf5d-b8bc-4ea7-82dd-51d7bd1af725 | Jean-Jacques Rousseau | 1712-06-28  | 1778-07-02  | 9782081275232, 9782081275256, 9782081206922, 9782070399697, 9782081409842 |
				| 6e2051c4-8c1a-4611-9be1-0ff5755771d7 | David Wengrow         | 1972-07-25  | <none>      | 9791020924636                                                             |
				| 87979468-9c9b-4883-b7c4-06c15c7bcf77 | Alain Damasio         | 1969-08-01  | <none>      | 9782072927522, 9782072927515, 9782072847929                               |
