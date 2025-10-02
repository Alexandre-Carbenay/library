Feature: Reference a new author
    As a librarian, I want to reference a new author so that I can reference books for this author

    Rule: Author can be referenced with minimal required information

        Scenario: Reference an author with its name and date of birth only
            Given Estelle is a librarian
            And Alain Damasio is not present in the list of authors
            When she references new author Alain Damasio born on 1969-08-01
            Then Alain Damasio is referenced
            And Alain Damasio is now present in the list of authors

        @PendingFeature
        Scenario: Reference an author with its surname and year of birth only
            Given Estelle is a librarian
            And Molière is not present in the list of authors
            When she references new author Molière born on 1622
            Then Molière is referenced
            And Molière is now present in the list of authors

        Scenario: Reference a dead author with its date of death
            Given Estelle is a librarian
            And Franz Kafka is not present in the list of authors
            When she references new author Franz Kafka born on 1883-07-03 and dead on 1924-06-03
            Then Franz Kafka is referenced
            And Franz Kafka is now present in the list of authors

    Rule: Author cannot be referenced with missing required information

        Scenario: Reference an author with only its name
            Given Estelle is a librarian
            And Alexandre Dumas is not present in the list of authors
            When she references new author Alexandre Dumas
            Then the referencing fails with date of birth required
            And Alexandre Dumas is still not present in the list of authors

    Rule: Different authors can be referenced with same name and date of birth
    To avoid blocking author referencing in case of multiple authors with the same name, we allow author duplication.
    This means that a librarian should be proposed the list of existing authors with the same name so that she can
    decide whether she needs to reference a new author or this author is already referenced in the system.

        Scenario: Reference an author that already exists
            Given Estelle is a librarian
            And George Orwell born on 1903-06-25 and dead on 1950-01-21 is present in the list of authors
            When she references new author George Orwell born on 1903-06-25 and dead on 1950-01-21
            Then George Orwell is referenced
            And George Orwell is present twice in the list of authors
