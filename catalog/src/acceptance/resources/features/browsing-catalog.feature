Feature: Browse the catalog
	As a library member, I want to browse the catalog to find the books that could interest me and I could borrow

	Rule: The catalog is browsable with pagination, starting with page 0, for a given language. Default language is French

		Scenario Outline: Retrieve the catalog pages
			Given Georges is a library member
			When he browses the catalog to page <page> showing <pageSize> books in <language>
			Then the catalog returns page <page> containing <pageElements> books over <pageSize> requested
			And the catalog contains <totalElements> books in a total of <totalPages> pages
			And the catalog returns page in <language>
			Examples:
				| page | pageSize | pageElements | totalElements | totalPages | language |
				| 0    | 10       | 10           | 67            | 7          | French   |
				| 0    | 25       | 25           | 67            | 3          | French   |
				| 1    | 25       | 25           | 67            | 3          | French   |
				| 2    | 25       | 17           | 67            | 3          | French   |
				| 0    | 50       | 50           | 67            | 2          | French   |
				| 1    | 50       | 17           | 67            | 2          | French   |
				| 0    | 100      | 67           | 67            | 1          | French   |
				| 0    | 10       | 10           | 44            | 5          | English  |

		Scenario: Retrieve the catalog by default will get the first page with 50 elements
			Given Georges is a library member
			When he browses the catalog for the first time
			Then the catalog returns page 0 containing 50 books over 50 requested

		Scenario: Retrieve the catalog by default will get the page in French language
			Given Georges is a library member
			When he browses the catalog for the first time
			Then the catalog returns page in French

	Rule: The catalog provides links to navigate through pagination:
	- first: navigates to the first page
	- prev: navigates to the previous page
	Those navigation links are present only if there is more than one page
	- next: navigates to the next page
	- last: navigates to the last page
	Those navigation links are present only if navigation to the page is allowed and not already reached

		Scenario Outline: Retrieve a catalog page with navigation links
			Given Georges is a library member
			When he browses the catalog to page <page> showing <pageSize> books in <language>
			Then the catalog returns page <page> with available <navigation> links
			Examples:
				| page | pageSize | navigation                    | language |
				| 0    | 10       | self, first, next, last       | French   |
				| 0    | 25       | self, first, next, last       | French   |
				| 1    | 25       | self, first, prev, next, last | French   |
				| 2    | 25       | self, first, prev, last       | French   |
				| 0    | 50       | self, first, next, last       | French   |
				| 1    | 50       | self, first, prev, last       | French   |
				| 0    | 100      | self                          | French   |
				| 0    | 25       | self, first, next, last       | English  |
				| 1    | 25       | self, first, prev, last       | English  |

		Scenario Outline: Retrieve a catalog page following a link
			Given Georges is a library member
			When he browses the catalog to page <page> showing <pageSize> books in <language>
			And he navigates through the catalog with <navigation> link
			Then the catalog returns page <newPage> containing <pageElements> books over <pageSize> requested
			Examples:
				| page | pageSize | navigation | newPage | pageElements | language |
				| 0    | 10       | self       | 0       | 10           | French   |
				| 0    | 10       | next       | 1       | 10           | French   |
				| 0    | 10       | last       | 6       | 7            | French   |
				| 1    | 10       | self       | 1       | 10           | French   |
				| 1    | 10       | first      | 0       | 10           | French   |
				| 1    | 10       | prev       | 0       | 10           | French   |
				| 1    | 10       | next       | 2       | 10           | French   |
				| 2    | 10       | first      | 0       | 10           | French   |
				| 2    | 10       | prev       | 1       | 10           | French   |
				| 1    | 25       | self       | 1       | 25           | French   |
				| 1    | 25       | prev       | 0       | 25           | French   |
				| 1    | 25       | next       | 2       | 17           | French   |
				| 3    | 10       | next       | 4       | 4            | English  |

	Rule: The catalog provides each book only one time within all pages

		Scenario Outline: Browse the catalog pages and expect only one occurrence for each book
			Given Georges is a library member
			When he browses the catalog to page <page> showing <pageSize> books in <language>
			Then the page <page> contains books corresponding to the expected <ids>
			Examples:
				| page | pageSize | ids                                                                                                | language |
				| 0    | 10       | b6608a30, 74d88208, 94856bb6, 5a47a523, 4082a49e, c2dd1a78, 434fcb17, e8cbad19, e3957d3f, aafe7eb6 | French   |
				| 1    | 10       | 3b35e0ae, 2e601f14, 0a881b54, 907716df, 2869e847, e6854cf5, 273c8353, b7392578, 699b97dc, 65e5d471 | French   |
				| 2    | 10       | 80211681, 1881ab54, 41e9e43c, c18a14f2, fddf98d1, 5df9d315, cfa4ce14, 14185458, 6cef0208, 3df147a8 | French   |
				| 3    | 10       | a49d474e, 2ad81543, 9157773d, a8e23d8c, 105be558, f1e5c7c6, ac9526cd, d56a66a3, f1dbbe65, e2b0c423 | French   |
				| 4    | 10       | 874c578c, ae5a92fc, ade812f1, a093173a, eb99a4d5, 481bb3f4, df2c8d87, 7b2eb526, d035a894, ac702d27 | French   |
				| 5    | 10       | 89c1cce8, 809d3368, 9eab131c, 3c25ef66, c12f91f3, 1517f41e, 858eba66, 824ea2db, 97611133, e568c46d | French   |
				| 6    | 10       | f4d4542f, fbd4d363, ea6cc5dc, bdd603ba, e1030011, 0c959d74, ace93305                               | French   |
				| 0    | 10       | c2dd1a78, 434fcb17, e8cbad19, e3957d3f, aafe7eb6, 3b35e0ae, 2e601f14, 0a881b54, 907716df, 2869e847 | English  |
				| 1    | 10       | e6854cf5, 273c8353, b7392578, 699b97dc, 65e5d471, 80211681, 1881ab54, 41e9e43c, c18a14f2, fddf98d1 | English  |
				| 2    | 10       | 5df9d315, cfa4ce14, 14185458, 6cef0208, 3df147a8, a49d474e, 2ad81543, 9157773d, a8e23d8c, 105be558 | English  |
				| 3    | 10       | f1e5c7c6, ac9526cd, d56a66a3, eb99a4d5, 481bb3f4, df2c8d87, 9eab131c, 3c25ef66, c12f91f3, 97611133 | English  |
				| 4    | 10       | e568c46d, e1030011, 0c959d74, ace93305                                                             | English  |

	Rule: The catalog provides author for each book within a page

		Scenario Outline: Retrieve a catalog page with authors corresponding to books in the page
			Given Georges is a library member
			When he browses the catalog to page <page> showing <pageSize> books in <language>
			Then the page <page> contains <authors> corresponding to the books
			Examples:
				| page | pageSize | authors                                                                          | language |
				| 0    | 10       | Jean-Jacques Rousseau, John Ronald Reuel Tolkien                                 | French   |
				| 1    | 10       | George Raymond Richard Martin, Isaac Asimov                                      | French   |
				| 2    | 10       | Isaac Asimov, J. K. Rowling                                                      | French   |
				| 3    | 10       | J. K. Rowling, Ernest Hemingway, Honoré de Balzac                                | French   |
				| 4    | 10       | Honoré de Balzac, Alexandre Dumas, David Graeber, David Wengrow, Molière         | French   |
				| 5    | 10       | Molière, Pierre Corneille, William Shakespeare, Alain Damasio, Franz Kafka       | French   |
				| 6    | 10       | Daniel Pennac, George Orwell                                                     | French   |
				| 0    | 10       | John Ronald Reuel Tolkien, George Raymond Richard Martin                         | English  |
				| 1    | 10       | Isaac Asimov                                                                     | English  |
				| 2    | 10       | Isaac Asimov, J. K. Rowling, Ernest Hemingway                                    | English  |
				| 3    | 10       | Ernest Hemingway, David Graeber, David Wengrow, William Shakespeare, Franz Kafka | English  |
				| 4    | 10       | Franz Kafka, George Orwell                                                       | English  |
