Feature: Reference a new publisher
  As a librarian, I want to reference a new publisher so that I can reference editions for this publisher

  Rule: Publisher can be referenced with minimal required information

    Scenario: Reference a publisher with its name only
      Given Estelle is a librarian
      And Les liens qui libèrent is not present in the list of publishers
      When she references new publisher Les liens qui libèrent
      Then publisher Les liens qui libèrent is referenced
      And Les liens qui libèrent is now present in the list of publishers

  Rule: Publisher cannot be referenced with missing required information

    Scenario: Reference an author without name
      Given Estelle is a librarian
      When she references new publisher without name
      Then the publisher referencing fails with name required

  Rule: Different publishers cannot be referenced with same name

    Scenario Outline: Reference a publisher that already exists
      Given Estelle is a librarian
      And <publisher> is present in the list of publishers
      When she references new publisher <publisher>
      Then the publisher referencing fails with duplication
      And <publisher> is present only once in the list of publishers
      Examples:
        | publisher       |
        | Pocket Jeunesse |
        | Flammarion      |
