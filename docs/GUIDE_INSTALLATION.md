# JaFeur

JaFeur est une application backend en Java (Spring Boot) permettant de gérer dynamiquement des conteneurs Docker via une API REST.

## Installation via Docker

### Clonez le dépôt JaFeur
```bash
git clone https://github.com/aurianecodebien/JaFeur.git
```
ou
```bash
git clone git@github.com:aurianecodebien/JaFeur.git
```

### Lancer l'application

Mettez vous à la racine du projet JaFeur et lancer cette commande :
```bash
mvn clean install
```
Lancez ensuite la commande suivante :
```bash
java -jar target/JaFeur-0.0.1-SNAPSHOT.jar
```
L'application est maintenant lancée sur le **port 8080** par défaut.

Puis, créer un réseau Docker pour JaFeur :
```bash
docker network create traefik-network
```
Il faut maintenant lancer un conteneur traefik pour gérer les applications.
```bash
docker run -d   --name traefik-jafeur   --network traefik-network   -p 80:80   -p 8081:8080   -v /var/run/docker.sock:/var/run/docker.sock:ro traefik:v2.10   --api.insecure=true   --providers.docker=true   --entrypoints.web.address=:80
```


### Accès API

Une fois l'application lancée, vous pouvez accéder à l’interface Swagger pour tester l’API :

[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## Groupe JaFeur

Ce projet a été réalisé par trois élèves de la filière DO de l'école Polytech Montpellier :
- Auriane PUSEL
- Noa DESPAUX
- Nathan DILHAN
