
# Documentation du Projet JaFeur
Ce document a pour objectif de fournir toutes les informations nécessaires pour configurer, comprendre et utiliser le projet JaFeur. Il s'adresse notamment aux professeurs et utilisateurs souhaitant tester et évaluer le projet.

## Table des Matières
[1. Présentation du Projet](#1-présentation-du-projet)
[2. Prérequis et Environnement](#2-prerequis-et-environnement)  
[3. Installation et Setup](#3-installation-et-setup)  
[4. Structure du Projet](#4-structure-du-projet)  
[5. Configuration Docker et Traefik](#5-configuration-docker-et-traefik)  
[6. Les Routes et l'API](#6-les-routes-et-lapi)  
[7. Développement et Tests](#7-developpement-et-tests)  
[8. Détails Techniques](#8-details-techniques)  
[9. Utilisation et Déploiement](#9-utilisation-et-deploiement)  
[10. Conclusion](#10-conclusion)


## 1. Présentation du Projet <a id="1-presentation-du-projet"></a>
   Le projet JaFeur est une application Java développée avec le framework Spring Boot et gérée via Maven. Il expose une API REST permettant de gérer des applications et de contrôler des conteneurs Docker.

## 2. Prérequis et Environnement <a id="2-prerequis-et-environnement"></a>
   Pour faire fonctionner et développer ce projet, vous devez disposer des éléments suivants :

- **Java** : Version 11 ou supérieure.

- **Maven** : Pour la gestion des dépendances et la compilation.

- **Docker** : Pour l'exécution des conteneurs, notamment en lien avec l'API.

- **Traefik** : Utilisé comme reverse proxy dans le dossier traefik pour la gestion du routage HTTP.

- **Git** : Pour cloner et gérer le code source.

## 3. Installation et Setup <a id="3-installation-et-setup"></a>

### Compilation et Exécution
#### 1. Compilation avec Maven :
Placez-vous à la racine du projet (le dossier jafeur) et exécutez :

```bash
./mvnw clean install
```
Cette commande compile l'application et génère un jar dans le dossier target.

#### 2.Exécution de l'Application :
Pour lancer l'application, exécutez :

```bash
java -jar target/JaFeur-0.0.1-SNAPSHOT.jar
```
### Setup Docker et Traefik
Le projet inclut une configuration pour Traefik dans le dossier traefik. Pour lancer l'environnement Docker, procédez comme suit :

1. Placez-vous dans le dossier jafeur/traefik.

2. Lancez la stack avec Docker Compose :

```bash
docker-compose up -d
```
Cette commande démarre Traefik qui s'occupera de rediriger les requêtes vers l'application.

## 4. Structure du Projet <a id="4-structure-du-projet"></a>
   La structure du projet est organisée de la manière suivante :

- /jafeur

   - /src/main/java : Code source Java de l'application.

        - `org.example.jafeur.JaFeurApplication.java` : Classe principale pour démarrer l'application.

        - /config : Contient la configuration Docker (ex. DockerConfig.java).

        - /controllers : Contrôleurs REST (ex. ApplicationController.java, DockerController.java) qui exposent les routes de l'API.

        - /model : Modèles de données (ex. Application.java, ContainerRunParam.java).

        - /repositories : Interfaces pour l'accès aux données (ex. ApplicationRepository.java).

        - /services : Logique métier (ex. ApplicationService.java, DockerService.java).

  - /src/main/resources : Fichiers de configuration (ex. application.properties).

  - /src/test : Tests unitaires pour l'application.

  - /traefik : Configuration de Traefik et fichier docker-compose.yml.

  - /documentation : Documentation et guides utilisateur (user_guides.md).

  - /target : Dossier de build généré par Maven.

## 5. Configuration Docker et Traefik <a id="5-configuration-docker-et-traefik"></a>
   Le dossier traefik contient la configuration nécessaire pour utiliser Traefik comme reverse proxy pour l’application.

- docker-compose.yml : Ce fichier définit les services Traefik et potentiellement d’autres services liés.

- Configuration Traefik : Généralement, Traefik est configuré pour écouter sur le port 80 ou 443 et rediriger les requêtes HTTP(S) vers l'application Java qui tourne sur un port interne (souvent 8080).

Pour adapter ou vérifier la configuration :

- Consultez le fichier docker-compose.yml pour connaître les ports exposés.

- Vérifiez la configuration de Traefik (souvent dans un fichier traefik.toml ou des labels Docker dans le docker-compose.yml) afin de s’assurer que les règles de routage sont correctes.

## 6. Les Routes et l'API <a id="6-les-routes-et-lapi"></a>
   L’application expose plusieurs routes via ses contrôleurs. Voici un aperçu des routes principales :

### ApplicationController
- GET /applications : Récupère la liste des applications gérées.

- POST /applications : Crée une nouvelle application.

- GET /applications/{id} : Récupère les détails d’une application spécifique.

- PUT /applications/{id} : Met à jour une application existante.

- DELETE /applications/{id} : Supprime une application.

### DockerController
- POST /docker/run : Lance un conteneur Docker avec les paramètres fournis.
Le corps de la requête attend des données du type ContainerRunParam, qui peut inclure des paramètres comme l'image, les ports, etc.

- GET /docker/status : Vérifie le statut des conteneurs ou retourne des informations sur l'exécution.

Remarque : Les routes exactes et leur fonctionnement peuvent être affinés en consultant les annotations dans les classes de contrôleurs (par exemple @RestController, @RequestMapping, etc.) dans le code source.

## 7. Développement et Tests <a id="7-developpement-et-tests"></a>
### Tests Unitaires
Les tests se trouvent dans le dossier src/test/java. Ils permettent de vérifier le bon fonctionnement de l’application et la validité des routes exposées. Pour lancer les tests, utilisez Maven :

```bash
./mvnw test
```
### Ajout de Nouvelles Fonctionnalités
- Modèles et Repositories : Pour ajouter de nouvelles entités ou modifier la logique d’accès aux données, travaillez dans les packages model et repositories.

- Services : La logique métier est centralisée dans le package services.

- Contrôleurs : Pour exposer de nouvelles routes ou modifier celles existantes, éditez les classes dans le package controllers.

## 8. Détails Techniques <a id="8-details-techniques"></a>
### Langage et Frameworks
- Java : Langage principal de développement.

- Spring Boot : Framework utilisé pour la création de l’application web et l’API REST.

- Maven : Outil de gestion et de compilation du projet.

- Docker : Pour la containerisation et le déploiement de l’application.

- Traefik : Reverse proxy et load balancer pour gérer le routage des requêtes HTTP.

### Configuration de l'Application
- Le fichier application.properties (situé dans src/main/resources) contient la configuration de base de l’application, comme le port d'écoute, la configuration de la base de données, etc.

- Des configurations spécifiques pour Docker peuvent être définies dans DockerConfig.java.

## 9. Utilisation et Déploiement <a id="9-utilisation-et-deploiement"></a>
Pour déployer l’application en production ou dans un environnement de test :

1. Préparation de l’environnement :

- Assurez-vous que Java, Maven, Docker et Traefik sont installés et configurés sur votre machine.

2. Build de l’application :

- Compilez le projet avec Maven (./mvnw clean install).

3. Exécution avec Docker :

- Lancez l’application via Docker en utilisant la configuration Traefik pour gérer le routage.

- Si nécessaire, adaptez le fichier docker-compose.yml aux besoins de votre infrastructure.

4. Accès à l’API :

- Une fois déployé, l’API est accessible via l’URL configurée (ex. http://localhost ou via un nom de domaine configuré avec Traefik).

## 10. Conclusion <a id="10-conclusion"></a>
Le projet JaFeur est une application moderne, basée sur Spring Boot et containerisée avec Docker. Elle offre une API REST complète pour la gestion d’applications et le contrôle de conteneurs. La documentation présentée ici vous guide à travers le setup initial, la structure du projet, les routes exposées et les aspects techniques. N’hésitez pas à consulter les commentaires dans le code source pour plus de détails et à adapter la configuration aux besoins spécifiques de votre environnement.

Cette documentation devrait fournir un aperçu complet et clair pour toute personne (notamment votre professeur) souhaitant comprendre, utiliser et développer le projet JaFeur.

Si tu as des questions ou besoin de précisions supplémentaires, n'hésite pas à me le faire savoir.