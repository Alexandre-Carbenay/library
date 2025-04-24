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

	Rule: Authors in the catalog have the expected details and embed their authored books.
	By default, if no accepted language is specified, each notable book is returned in its original language.
	Otherwise, the notable books returned in the author's details are filtered based on the specified languages,
	choosing the first language that corresponds to at least one book.
	If no book corresponds to the specified languages, each book is returned in its original language.

		Scenario Outline: Retrieve known author details with notable books in their original language
			Given Georges is a library member
			When he retrieves the details of an author with ID <authorId>
			Then the author details have the expected "<name>", <dateOfBirth>, <dateOfDeath> and authored notable <books>
			Examples:
				| authorId                             | name                  | dateOfBirth | dateOfDeath | books                                                                                                                                                               |
				| 83b5bf5d-b8bc-4ea7-82dd-51d7bd1af725 | Jean-Jacques Rousseau | 1712-06-28  | 1778-07-02  | Du contrat social, Discours sur l'origine et les fondements de l'inégalité parmi les hommes, Emile ou de l'éducation, Les Confessions, Julie ou la Nouvelle Héloïse |
				| 6e2051c4-8c1a-4611-9be1-0ff5755771d7 | David Wengrow         | 1972-07-25  | <none>      | The Dawn of Everything                                                                                                                                              |
				| 87979468-9c9b-4883-b7c4-06c15c7bcf77 | Alain Damasio         | 1969-08-01  | <none>      | La Zone du Dehors, La Horde du Contrevent, Les Furtifs                                                                                                              |

		Scenario Outline: Retrieve known author details with notable books in the expected language
			Given Georges is a library member
			When he retrieves the details of an author with ID <authorId> in <language>
			Then the author details have the expected "<name>", <dateOfBirth>, <dateOfDeath> and authored notable <books>
			Examples:
				| authorId                             | language | name          | dateOfBirth | dateOfDeath | books                                                          |
				| 6e2051c4-8c1a-4611-9be1-0ff5755771d7 | French   | David Wengrow | 1972-07-25  | <none>      | Au commencement était... - Une nouvelle histoire de l'humanité |
				| 87979468-9c9b-4883-b7c4-06c15c7bcf77 | French   | Alain Damasio | 1969-08-01  | <none>      | La Zone du Dehors, La Horde du Contrevent, Les Furtifs         |
				| 87979468-9c9b-4883-b7c4-06c15c7bcf77 | German   | Alain Damasio | 1969-08-01  | <none>      | Die Horde im Gegenwind                                         |

		Scenario Outline: Retrieve known author details with notable books in expected language determined from accept languages
			Given Georges is a library member
			When he retrieves the details of an author with ID <authorId> in "<acceptLanguages>"
			Then the author details have the expected "<name>", <dateOfBirth>, <dateOfDeath> and authored notable <books>
			Examples:
				| authorId                             | acceptLanguages        | name                  | dateOfBirth | dateOfDeath | books                                                                                                                                                               |
				| 6e2051c4-8c1a-4611-9be1-0ff5755771d7 | fr, en;q=0.9           | David Wengrow         | 1972-07-25  | <none>      | Au commencement était... - Une nouvelle histoire de l'humanité                                                                                                      |
				| 87979468-9c9b-4883-b7c4-06c15c7bcf77 | en, fr;q=0.8, de;q=0.7 | Alain Damasio         | 1969-08-01  | <none>      | La Zone du Dehors, La Horde du Contrevent, Les Furtifs                                                                                                              |
				| 87979468-9c9b-4883-b7c4-06c15c7bcf77 | en, de;q=0.9, fr;q=0.8 | Alain Damasio         | 1969-08-01  | <none>      | Die Horde im Gegenwind                                                                                                                                              |
				| 83b5bf5d-b8bc-4ea7-82dd-51d7bd1af725 | it, en;q=0.8           | Jean-Jacques Rousseau | 1712-06-28  | 1778-07-02  | Du contrat social, Discours sur l'origine et les fondements de l'inégalité parmi les hommes, Emile ou de l'éducation, Les Confessions, Julie ou la Nouvelle Héloïse |
