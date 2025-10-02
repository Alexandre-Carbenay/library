package org.adhuc.library.referencing.authors;

import org.adhuc.library.referencing.authors.AuthorsMother.Authors;
import org.adhuc.library.referencing.authors.internal.InMemoryAuthorsRepository;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Authors referencing service should")
class AuthorsReferencingServiceTests {

    private InMemoryAuthorsRepository authorsRepository;
    private AuthorsReferencingService service;

    @BeforeEach
    void setUp() {
        authorsRepository = new InMemoryAuthorsRepository();
        service = new AuthorsReferencingService(authorsRepository);
    }

    @Test
    @DisplayName("reference alive author, providing an author with generated ID")
    void referenceAliveAuthor() {
        var name = Authors.name();
        var dateOfBirth = Authors.aliveDateOfBirth();
        var command = new ReferenceAuthor(name, dateOfBirth);
        var author = service.referenceAuthor(command);

        SoftAssertions.assertSoftly(s -> {
            s.assertThat(author.id()).isNotNull();
            s.assertThat(author.name()).isEqualTo(name);
            s.assertThat(author.dateOfBirth()).isEqualTo(dateOfBirth);
            s.assertThat(author.dateOfDeath()).isNotPresent();
        });
    }

    @Test
    @DisplayName("reference dead author, providing an author with generated ID")
    void referenceDeadAuthor() {
        var name = Authors.name();
        var dateOfBirth = Authors.dateOfBirth();
        var dateOfDeath = Authors.deadDateOfDeath(dateOfBirth);
        var command = new ReferenceAuthor(name, dateOfBirth, dateOfDeath);
        var author = service.referenceAuthor(command);

        SoftAssertions.assertSoftly(s -> {
            s.assertThat(author.id()).isNotNull();
            s.assertThat(author.name()).isEqualTo(name);
            s.assertThat(author.dateOfBirth()).isEqualTo(dateOfBirth);
            s.assertThat(author.dateOfDeath()).isPresent().contains(dateOfDeath);
        });
    }

    @Test
    @DisplayName("save author when referencing it")
    void referenceAuthorSaveIt() {
        var name = Authors.name();
        var dateOfBirth = Authors.dateOfBirth();
        var command = new ReferenceAuthor(name, dateOfBirth);
        var author = service.referenceAuthor(command);

        assertThat(authorsRepository.findById(author.id())).isPresent().contains(author);
    }

}
