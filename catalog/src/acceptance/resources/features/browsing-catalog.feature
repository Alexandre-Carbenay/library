Feature: Browse the catalog
	As a library member, I want to browse the catalog to find the books that could interest me and I could borrow

	Rule: The catalog is browsable with pagination, starting with page 0

		Scenario Outline: Retrieve the catalog pages
			Given Georges is a library member
			When he browses the catalog to page <page> showing <pageSize> books
			Then the catalog returns page <page> containing <pageElements> books over <pageSize> requested
			And the catalog contains <totalElements> books in a total of <totalPages> pages
			Examples:
				| page | pageSize | pageElements | totalElements | totalPages |
				| 0    | 10       | 10           | 67            | 7          |
				| 0    | 25       | 25           | 67            | 3          |
				| 1    | 25       | 25           | 67            | 3          |
				| 2    | 25       | 17           | 67            | 3          |
				| 0    | 50       | 50           | 67            | 2          |
				| 1    | 50       | 17           | 67            | 2          |
				| 0    | 100      | 67           | 67            | 1          |

		Scenario: Retrieve the catalog by default will get the first page with 50 elements
			Given Georges is a library member
			When he browses the catalog for the first time
			Then the catalog returns page 0 containing 50 books over 50 requested

	Rule: The catalog provides links to navigate through pagination:
	- first: navigates to the first page
	- prev: navigates to the previous page
	Those navigation links are present only if there is more than one page
	- next: navigates to the next page
	- last: navigates to the last page
	Those navigation links are present only if navigation to the page is allowed and not already reached

		Scenario Outline:
			Given Georges is a library member
			When he browses the catalog to page <page> showing <pageSize> books
			Then the catalog returns page <page> with available <navigation> links
			Examples:
				| page | pageSize | navigation                    |
				| 0    | 10       | self, first, next, last       |
				| 0    | 25       | self, first, next, last       |
				| 1    | 25       | self, first, prev, next, last |
				| 2    | 25       | self, first, prev, last       |
				| 0    | 50       | self, first, next, last       |
				| 1    | 50       | self, first, prev, last       |
				| 0    | 100      | self                          |

		Scenario Outline:
			Given Georges is a library member
			When he browses the catalog to page <page> showing <pageSize> books
			And he navigates through the catalog with <navigation> link
			Then the catalog returns page <newPage> containing <pageElements> books over <pageSize> requested
			Examples:
				| page | pageSize | navigation | newPage | pageElements |
				| 0    | 10       | self       | 0       | 10           |
				| 0    | 10       | next       | 1       | 10           |
				| 0    | 10       | last       | 6       | 7            |
				| 1    | 10       | self       | 1       | 10           |
				| 1    | 10       | first      | 0       | 10           |
				| 1    | 10       | prev       | 0       | 10           |
				| 1    | 10       | next       | 2       | 10           |
				| 2    | 10       | first      | 0       | 10           |
				| 2    | 10       | prev       | 1       | 10           |
				| 1    | 25       | self       | 1       | 25           |
				| 1    | 25       | prev       | 0       | 25           |
				| 1    | 25       | next       | 2       | 17           |

	Rule: The catalog provides each book only one time within all pages

		Scenario Outline:
			Given Georges is a library member
			When he browses the catalog to page <page> showing <pageSize> books
			Then the page <page> contains books corresponding to the expected <isbns>
			Examples:
				| page | pageSize | isbns                                                                                                                                                |
				| 0    | 10       | 9782081275232, 9782081275256, 9782081206922, 9782070399697, 9782081409842, 9782267046885, 9782267046892, 9782267046908, 9782266339667, 9782267044706 |
				| 1    | 10       | 9782290208878, 9782290215661, 9782290221686, 9782290221693, 9782290221709, 9780553103540, 9780553108033, 9780553106633, 9780553801507, 9780553801477 |
				| 2    | 10       | 9782070360536, 9782070360550, 9782070360529, 9782070360635, 9782070379668, 9782290227268, 9782290311257, 9782290319024, 9782290327944, 9782290332757 |
				| 3    | 10       | 9782290311165, 9782070624522, 9782070624539, 9782070624546, 9782070624553, 9782070624560, 9782070624904, 9782070624911, 9782070625192, 9782072762086 |
				| 4    | 10       | 9782073004215, 9782072729935, 9782070361519, 9782070409341, 9782253006305, 9782253098041, 9782253099994, 9782253098058, 9782253098065, 9782070417681 |
				| 5    | 10       | 9791020923769, 9782330061258, 9791020924636, 9782070449996, 9782070450022, 9782070449941, 9782070449934, 9782070409181, 9782070468485, 9782070468508 |
				| 6    | 10       | 9788490019481, 9780192862426, 9782072927522, 9782072927515, 9782072847929, 9782070462872, 9782073052872                                              |

	Rule: The catalog provides author for each book within a page

		Scenario Outline:
			Given Georges is a library member
			When he browses the catalog to page <page> showing <pageSize> books
			Then the page <page> contains <authors> corresponding to the books
			Examples:
				| page | pageSize | authors                                                                      |
				| 0    | 10       | Jean-Jacques Rousseau, John Ronald Reuel Tolkien                             |
				| 1    | 10       | George Raymond Richard Martin                                                |
				| 2    | 10       | Isaac Asimov                                                                 |
				| 3    | 10       | Isaac Asimov, J. K. Rowling, Ernest Hemingway                                |
				| 4    | 10       | Ernest Hemingway, Honoré de Balzac, Alexandre Dumas                          |
				| 5    | 10       | David Graeber, David Wengrow, Molière, Pierre Corneille, William Shakespeare |
				| 6    | 10       | William Shakespeare, Alain Damasio, Franz Kafka                              |
