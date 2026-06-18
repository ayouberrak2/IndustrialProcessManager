# Backend Java natif

Ce backend utilise Java natif avec `HttpServer`, JDBC et MySQL.

Structure simple :
- `controller` : recoit les requetes HTTP ;
- `service` : contient la logique metier ;
- `repository` : communique avec la base de donnees ;
- `dto` : objets envoyes ou recus par l'API ;
- `model` : classes qui representent les donnees.

La configuration de la base se trouve dans :
`src/main/resources/database.properties`
