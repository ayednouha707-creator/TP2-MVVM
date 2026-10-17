1. Pourquoi la liste n'est pas perdue lors d'une rotation ?
La liste est gardee dans le ViewModel et pas dans l'ecran. A la rotation l'activite est recreee mais le
ViewModel reste le meme, donc l'ecran recupere l'etat sans refaire la requete.
2. Pourquoi _uiState est prive et modifiable et uiState public en lecture seule ?
Pour que seul le ViewModel puisse modifier l'etat. L'ecran l'affiche seulement et appelle les
fonctions du ViewModel pour le changer (flux unidirectionnel).
3. Difference entre StateFlow et SharedFlow ?
StateFlow garde toujours la derniere valeur, c'est un etat (ex : uiState). SharedFlow envoie un
evenement une seule fois sans le garder (ex : le message de la Snackbar "patient ajoute").
4. Si age est Int et que l'API renvoie un patient sans age ?
La conversion du JSON echoue (SerializationException) et l'application affiche "Reponse inattendue
du serveur". C'est pour ca qu'on met Int? = null.
5. HttpException ou IOException ?
HttpException : le serveur repond avec une erreur, par exemple 404 si le patient n'existe pas.
IOException : le serveur ne repond pas, par exemple s'il est arrete.
6. Si on remplace l'API par une base Room ?
On modifie la source de donnees (Entity, Dao, RoomDatabase), le Repository et AppContainer. Le
ViewModel et l'ecran restent les memes car ils utilisent seulement le Repository.
