Feature: Retrieve edition details
	As a library member, I want to retrieve edition details from the catalog to get information about its author and content

	Rule: Unknown editions cannot be found in the catalog

		Scenario Outline: Fail retrieving unknown edition details
			Given Georges is a library member
			When he retrieves the details of an edition with ISBN <isbn>
			Then the edition details cannot be retrieved because it does not exists
			Examples:
				| isbn          |
				| 9782081216440 |
				| 9782253160991 |
				| 9782290365182 |

	Rule: Editions in the catalog have the expected details and embed their authors

		Scenario Outline: Retrieve known edition details
			Given Georges is a library member
			When he retrieves the details of an edition with ISBN <isbn>
			Then the edition details have the expected "<title>", <publicationDate>, <language>, <authors> and "<summary>"
			Examples:
				| isbn          | title                                                          | publicationDate | language | authors                       | summary                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
				| 9782081275232 | Du contrat social                                              | 2012-01-04      | French   | Jean-Jacques Rousseau         | Paru en 1762, le Contrat social, en affirmant le principe de souveraineté du peuple, a constitué un tournant décisif pour la modernité et s'est imposé comme un des textes majeurs de la philosophie politique. Il a aussi acquis le statut de monument, plus célèbre que connu, plus révéré - ou honni - qu'interrogé. Retrouver, dans les formules fameuses et les pages d'anthologie, le mouvement de la réflexion et les questions vives qui nourrissent une œuvre beaucoup plus problématique qu'affirmative, c'est découvrir une pensée qui se tient au plus près des préoccupations d'aujourd'hui : comment intégrer les intérêts de tous dans la détermination de l'intérêt commun ? Comment lutter contre la pente de tout gouvernement à déposséder les citoyens de la souveraineté ? Comment former en chacun ce sentiment d'obligation sans lequel le lien social se défait ?\n |
				| 9780553801507 | A Feast for Crows                                              | 2005-11-08      | English  | George Raymond Richard Martin | After centuries of bitter strife, the seven powers dividing the land have beaten one another into an uneasy truce. But it’s not long before the survivors, outlaws, renegades, and carrion eaters of the Seven Kingdoms gather. Now, as the human crows assemble over a banquet of ashes, daring new plots and dangerous new alliances are formed while surprising faces—some familiar, others only just appearing—emerge from an ominous twilight of past struggles and chaos to take up the challenges of the terrible times ahead. Nobles and commoners, soldiers and sorcerers, assassins and sages, are coming together to stake their fortunes . . . and their lives. For at a feast for crows, many are the guests—but only a few are the survivors.                                                                                                                                 |
				| 9791020924636 | Au commencement était... - Une nouvelle histoire de l'humanité | 2023-11-08      | French   | David Graeber, David Wengrow  | Voici l'édition Poche collector du grand livre de davdi Graeber et David Wengrow. Depuis des siècles, nous nous racontons sur les origines des sociétés humaines et des inégalités sociales une histoire très simple. Pendant l'essentiel de leur existence sur terre, les êtres humains auraient vécu au sein de petits clans de chasseurs-cueilleurs. Puis l'agriculture aurait fait son entrée, et avec elle la propriété privée.\nEnfin seraient nées les villes, marquant l'apparition non seulement de la civilisation, mais aussi des guerres, de la bureaucratie, du patriarcat et de l'esclavage. Ce récit pose un gros problème : il est faux.                                                                                                                                                                                                                                    |
