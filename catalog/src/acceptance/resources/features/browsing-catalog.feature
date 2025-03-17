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

	Rule: The catalog provides each books only one time within all pages

		Scenario Outline:
			Given Georges is a library member
			When he browses the catalog to page <page> showing <pageSize> books
			Then the page <page> contains books corresponding to the expected <ids>
			Examples:
				| page | pageSize | ids                                                                                                |
				| 0    | 10       | b6608a30, 74d88208, 94856bb6, 5a47a523, 4082a49e, c2dd1a78, 434fcb17, e8cbad19, e3957d3f, aafe7eb6 |
				| 1    | 10       | 3b35e0ae, 2e601f14, 0a881b54, 907716df, 2869e847, e6854cf5, 273c8353, b7392578, 699b97dc, 65e5d471 |
				| 2    | 10       | 80211681, 1881ab54, 41e9e43c, c18a14f2, fddf98d1, 5df9d315, cfa4ce14, 14185458, 6cef0208, 3df147a8 |
				| 3    | 10       | a49d474e, 2ad81543, 9157773d, a8e23d8c, 105be558, f1e5c7c6, ac9526cd, d56a66a3, f1dbbe65, e2b0c423 |
				| 4    | 10       | 874c578c, ae5a92fc, ade812f1, a093173a, eb99a4d5, 481bb3f4, df2c8d87, 7b2eb526, d035a894, ac702d27 |
				| 5    | 10       | 89c1cce8, 809d3368, 9eab131c, 3c25ef66, c12f91f3, 1517f41e, 858eba66, 824ea2db, 97611133, e568c46d |
				| 6    | 10       | f4d4542f, fbd4d363, ea6cc5dc, bdd603ba, e1030011, 0c959d74, ace93305                               |

	Rule: The catalog provides author for each book within a page

		Scenario Outline:
			Given Georges is a library member
			When he browses the catalog to page <page> showing <pageSize> books
			Then the page <page> contains <authors> corresponding to the books
			Examples:
				| page | pageSize | authors                                                                    |
				| 0    | 10       | Jean-Jacques Rousseau, John Ronald Reuel Tolkien                           |
				| 1    | 10       | George Raymond Richard Martin, Isaac Asimov                                |
				| 2    | 10       | Isaac Asimov, J. K. Rowling                                                |
				| 3    | 10       | J. K. Rowling, Ernest Hemingway, Honoré de Balzac                          |
				| 4    | 10       | Honoré de Balzac, Alexandre Dumas, David Graeber, David Wengrow, Molière   |
				| 5    | 10       | Molière, Pierre Corneille, William Shakespeare, Alain Damasio, Franz Kafka |
				| 6    | 10       | Daniel Pennac, George Orwell                                               |
