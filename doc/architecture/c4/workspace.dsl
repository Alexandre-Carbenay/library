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
            tags "Library"
        }

        librarian -> bo "Manages the library catalog"
        librarian -> bo "Manages the books borrowings"
        member -> ui "Borrows books from the library"
        ui -> catalog "Retrieves books from the catalog"
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
        }
    }
}
