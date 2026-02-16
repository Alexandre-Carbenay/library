Feature: Reference a new book
  As a librarian, I want to reference a new book so that I can reference editions for this book

  Rule: Book can be referenced with minimal required information

    Scenario Outline: Reference a book with its author, original language, title and description in this language
      Given Estelle is a librarian
      And <author> is present in the list of authors
      And <title> is not present in the list of books
      When she references new book <title> written in <original_language> by <author> with a description
      Then book <title> is referenced
      And <title> is now present in the list of books
      And <title> does not have a wikipedia link
      Examples:
        | author        | original_language | title          |
        | George Orwell | English           | Animal Farm    |
        | Daniel Pennac | French            | Comme un roman |

  Rule: Book cannot be referenced with missing required information

    Scenario: Reference a book with only its author, original language and description in this language
      Given Estelle is a librarian
      And George Orwell is present in the list of authors
      When she references new book written in English by George Orwell with a description but no title
      Then the referencing fails with title in English required

    Scenario: Reference a book with only its original language, title and description in this language
      Given Estelle is a librarian
      And Homage to Catalonia is not present in the list of books
      When she references new book Homage to Catalonia written in English with a description
      Then the referencing fails with author required
      And Homage to Catalonia is still not present in the list of books

    Scenario: Reference a book with only its author, title and description
      Given Estelle is a librarian
      And George Orwell is present in the list of authors
      And Homage to Catalonia is not present in the list of books
      When she references new book Homage to Catalonia written by George Orwell with a description
      Then the referencing fails with original language required
      And Homage to Catalonia is still not present in the list of books

    Scenario: Reference a book with only its author, original language and title in this language
      Given Estelle is a librarian
      And George Orwell is present in the list of authors
      And Homage to Catalonia is not present in the list of books
      When she references new book Homage to Catalonia written in English by George Orwell without description
      Then the referencing fails with description in English required
      And Homage to Catalonia is still not present in the list of books

  Rule: Book can be written by more than one author

    Scenario: Reference a book with many authors, original language, title and description in this language
      Given Estelle is a librarian
      And David Graeber is present in the list of authors
      And David Wengrow is present in the list of authors
      And The Dawn of Everything is not present in the list of books
      When she references new book The Dawn of Everything written in English by David Graeber and David Wengrow with a description
      Then book The Dawn of Everything is referenced
      And The Dawn of Everything is now present in the list of books

  Rule: Book can be referenced in another language than its original one

    Scenario: Reference a book with its author, original language, title and description in another language
      Given Estelle is a librarian
      And George Orwell is present in the list of authors
      And 1984 is not present in the list of books
      When she references new book 1984 written in English by George Orwell, with title and description in French
      Then book 1984 is referenced
      And 1984 is now present in the list of books

  Rule: Book can be referenced with a wikipedia link

    Scenario: Reference a book with its author, original language, title description and wikipedia link in this language
      Given Estelle is a librarian
      And Daniel Pennac is present in the list of authors
      And La Fée Carabine is not present in the list of books
      When she references new book La Fée Carabine written in French by Daniel Pennac with a description and wikipedia link
      Then book La Fée Carabine is referenced
      And La Fée Carabine is now present in the list of books
      And La Fée Carabine has a wikipedia link

  Rule: Different books cannot be referenced with same author and title
  This rule only ensure that a book that exists in its original language, or any other language used during referencing
  cannot be duplicated in the catalog. This does not guarantee that the same book cannot be referenced twice in different
  languages or with spelling mistakes.
  This means that a librarian should be proposed the list of existing books for the same author(s) to let her decide
  whether she should reference a new book or edit an existing one.

    Scenario Outline: Reference a book that already exists in the same language
      Given Estelle is a librarian
      And <author> is present in the list of authors
      And <title> is present in the list of books
      When she references new book <title> written in <original_language> by <author>, with title and description in <reference_language>
      Then the book referencing fails with duplication
      And <title> is present only once in the list of books
      Examples:
        | author        | original_language | reference_language | title                       |
        | David Graeber | English           | English            | Debt: The First 5,000 Years |
        | Daniel Pennac | French            | French             | Au bonheur des ogres        |
        | Franz Kafka   | German            | French             | La Métamorphose             |
