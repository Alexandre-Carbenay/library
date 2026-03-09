package org.adhuc.library.referencing.acceptance.publishers;

import org.adhuc.library.referencing.acceptance.books.Book;

import java.util.List;
import java.util.Optional;

import static org.adhuc.library.referencing.acceptance.publishers.actions.PublishersListing.listPublishers;

public class PublisherRetriever {

    public static List<Publisher> findPublisherByName(String publisherName) {
        var publishers = listPublishers();
        return publishers.stream().filter(publisher -> publisher.hasName(publisherName)).toList();
    }

}
