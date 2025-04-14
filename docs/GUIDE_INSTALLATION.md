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
docker-compose up -d --build
```

Cela démarre automatiquement le backend JaFeur ainsi qu'un conteneur traefik pour gérer les applications.

### Accès API

Une fois le conteneur lancé, vous pouvez accéder à l’interface Swagger pour tester l’API :

[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

Le port peut être amené à changer selon les applications existantes dans la machine, le vérifier grâce à la commande suivante :
```bash
docker ps
```
Regardez ensuite la colonne PORTS pour le conteneur `jafeur`.

---

## Groupe JaFeur

Ce projet a été réalisé par trois élèves de la filière DO de l'école Polytech Montpellier :
- Auriane PUSEL
- Noa DESPAUX
- Nathan DILHAN
