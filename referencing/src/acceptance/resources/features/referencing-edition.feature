Feature: Reference a new edition
  As a librarian, I want to reference a new edition so that I can add copies of that edition in the library

  Rule: Edition can be referenced with minimal required information

    Scenario Outline: Reference an edition with its book, publisher, ISBN, language and publication date
      Given Estelle is a librarian
      And <author> is present in the list of authors
      And <title> written in <language> by <author> is present in the list of books
      And <publisher> is present in the list of publishers
      And <isbn> is not present in the list of editions
      When she references new edition with ISBN <isbn> edited in <language> by <publisher> the <publication_date> for <title>
      Then edition with ISBN <isbn> is referenced
      And <isbn> is now present in the list of editions
      And edition <isbn> has the title and summary of its book in <language>
      Examples:
        | title             | author                | publisher        | isbn          | language | publication_date |
        | Du contrat social | Jean-Jacques Rousseau | Flammarion       | 9782081275232 | French   | 2012-01-04       |
        | Les Confessions   | Jean-Jacques Rousseau | Gallimard        | 9782070399697 | French   | 2015-06-26       |
        | Bullshit jobs     | David Graeber         | Simon & Schuster | 9781501143335 | English  | 2019-05-07       |

  Rule: Edition can be referenced with complete information

    Scenario Outline: Reference an edition with its book, publisher, ISBN, language, publication date, title and summary
      Given Estelle is a librarian
      And <author> is present in the list of authors
      And <title> written in <language> by <author> is present in the list of books
      And <publisher> is present in the list of publishers
      And <isbn> is not present in the list of editions
      When she references new edition titled <edition_title> with ISBN <isbn> edited in <language> by <publisher> the <publication_date> for <title>, with a summary
      Then edition with ISBN <isbn> is referenced
      And <isbn> is now present in the list of editions
      And edition <isbn> has the specified title and summary
      Examples:
        | title             | author                | publisher | isbn          | language | publication_date | edition_title                                     |
        | Du contrat social | Jean-Jacques Rousseau | J'ai lu   | 9782290385050 | French   | 2023-02-08       | Du contrat social ou Principes du droit politique |

  Rule: Edition cannot be referenced with missing required information

    Scenario: Reference an edition with only its publisher, ISBN, language and publication date
      Given Estelle is a librarian
      And Bantam is present in the list of publishers
      And 9780553103540 is not present in the list of editions
      When she references new edition with ISBN 9780553103540 edited in English by Bantam the 1996-08-01
      Then the edition referencing fails with book required

    Scenario: Reference an edition with only its book, ISBN, language and publication date
      Given Estelle is a librarian
      And George Raymond Richard Martin is present in the list of authors
      And A Game of Thrones written in English by George Raymond Richard Martin is present in the list of books
      And 9780553103540 is not present in the list of editions
      When she references new edition with ISBN 9780553103540 edited in English the 1996-08-01 for A Game of Thrones
      Then the edition referencing fails with publisher required

    Scenario: Reference an edition with only its book, publisher, language and publication date
      Given Estelle is a librarian
      And George Raymond Richard Martin is present in the list of authors
      And Bantam is present in the list of publishers
      And A Game of Thrones written in English by George Raymond Richard Martin is present in the list of books
      And 9780553103540 is not present in the list of editions
      When she references new edition edited in English by Bantam the 1996-08-01 for A Game of Thrones
      Then the edition referencing fails with ISBN required

    Scenario: Reference an edition with only its book, publisher, ISBN and publication date
      Given Estelle is a librarian
      And George Raymond Richard Martin is present in the list of authors
      And Bantam is present in the list of publishers
      And A Game of Thrones written in English by George Raymond Richard Martin is present in the list of books
      And 9780553103540 is not present in the list of editions
      When she references new edition with ISBN 9780553103540 edited by Bantam the 1996-08-01 for A Game of Thrones
      Then the edition referencing fails with language required

    Scenario: Reference an edition with only its book, publisher, ISBN and language
      Given Estelle is a librarian
      And George Raymond Richard Martin is present in the list of authors
      And Bantam is present in the list of publishers
      And A Game of Thrones written in English by George Raymond Richard Martin is present in the list of books
      And 9780553103540 is not present in the list of editions
      When she references new edition with ISBN 9780553103540 edited in English by Bantam for A Game of Thrones
      Then the edition referencing fails with publication date required

  Rule: Edition can be referenced in any language

    Scenario: Reference an edition with a language used to localize book information
      Given Estelle is a librarian
      And George Raymond Richard Martin is present in the list of authors
      And J'ai lu is present in the list of publishers
      And A Game of Thrones written in English by George Raymond Richard Martin is present in the list of books, with title A Game of Thrones in French
      And 9782290208878 is not present in the list of editions
      When she references new edition titled Le trône de fer l'intégrale 1 with ISBN 9782290208878 edited in French by J'ai lu the 2019-04-10 for A Game of Thrones, with a summary
      Then edition with ISBN 9782290208878 is referenced
      And 9782290208878 is now present in the list of editions

    Scenario: Reference an edition with a language not used to localize book information
      Given Estelle is a librarian
      And George Raymond Richard Martin is present in the list of authors
      And Fantasy Productions is present in the list of publishers
      And A Game of Thrones written in English by George Raymond Richard Martin is present in the list of books, with title A Game of Thrones in French
      And 9783890645322 is not present in the list of editions
      When she references new edition titled Eisenthron with ISBN 9783890645322 edited in German by Fantasy Productions the 2004-01-01 for A Game of Thrones, with a summary
      Then edition with ISBN 9783890645322 is referenced
      And 9783890645322 is now present in the list of editions

  Rule: Edition inherits its book title and description, defaulting to the book original language's localization if none exists in the edition's language

    Scenario: Reference an edition without title and summary, in a language that exists for the book
      Given Estelle is a librarian
      And Antoine de Saint-Exupéry is present in the list of authors
      And Le Petit Prince written in French by Antoine de Saint-Exupéry is present in the list of books, with title The Little Prince in English
      And Clarion Books is present in the list of publishers
      And 9780156012195 is not present in the list of editions
      When she references new edition with ISBN 9780156012195 edited in English by Clarion Books the 2000-06-29 for Le Petit Prince
      Then edition with ISBN 9780156012195 is referenced
      And 9780156012195 is now present in the list of editions
      And edition 9780156012195 has the title and summary of its book in English

    Scenario: Reference an edition without title and summary, in a language that does not exist for the book
      Given Estelle is a librarian
      And Gustave Flaubert is present in the list of authors
      And Salammbô written in French by Gustave Flaubert is present in the list of books
      And East India Publishing Company is present in the list of publishers
      And 9781774268148 is not present in the list of editions
      When she references new edition with ISBN 9781774268148 edited in English by East India Publishing Company the 2023-03-31 for Salammbô
      Then edition with ISBN 9781774268148 is referenced
      And 9781774268148 is now present in the list of editions
      And edition 9781774268148 has the title and summary of its book in French

  Rule: Different editions cannot be referenced with same ISBN

    Scenario: Reference an edition that already exists
      Given Estelle is a librarian
      And Jean-Jacques Rousseau is present in the list of authors
      And Julie ou la Nouvelle Héloïse written in French by Jean-Jacques Rousseau is present in the list of books
      And Flammarion is present in the list of publishers
      And edition with ISBN 9782081409842 edited in French by Flammarion for Julie ou la Nouvelle Héloïse is present in the list of editions
      When she references new edition with ISBN 9782081409842 edited in French by Flammarion the 2018-11-07 for Julie ou la Nouvelle Héloïse
      Then the edition referencing fails with duplication
      And 9782081409842 is present only once in the list of editions
