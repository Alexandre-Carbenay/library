workspace Library {
    model {
        librarian = person "Librarian" "Works in the library" {
            tags "Library"
        }
        member = person "Member" "Has access to the books of the library"
        library = softwareSystem "Library" "Integrated library system" {
            ui = container "Library Public Website" "Exposes the library public website to browse the library catalog" {
                tags "Website" "Library"
            }
            bo = container "Backoffice" "Exposes a website dedicated to library management" {
                tags "Website" "Library"
            }
            catalog = container "Catalog" "Exposes the library catalog" {
                tags "Service" "Library"
            }
            referencing = container "Referencing" "Provides services for books & editions referencing" {
                tags "Service" "Library"
            }
            tags "Library"
        }

        librarian -> bo "Manages the library catalog" "HTTP" "sync"
        librarian -> bo "Manages the books borrowings" "HTTP" "sync"
        member -> ui "Borrows books from the library" "HTTP" "sync"
        ui -> catalog "Retrieves books from the catalog" "HTTP" "sync"
        bo -> referencing "Uses for books & editions referencing" "HTTP" "sync"
        referencing -> catalog "Publishes information about books & editions" "PubSub" "async"
    }

    views {
        systemContext library "Library-Context" {
            include *
            autoLayout tb
        }

        container library "Library-Containers" {
            include *
            autoLayout tb
        }

        styles {
            element "Person" {
                shape person
            }
            element "Library" {
                background #1168bd
                color #ffffff
            }
            element "Website" {
                shape WebBrowser
            }
            element "Service" {
                shape Hexagon
            }
            relationship "sync" {
                style solid
            }
            relationship "async" {
                style dotted
            }
        }
    }
}
