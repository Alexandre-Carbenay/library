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
