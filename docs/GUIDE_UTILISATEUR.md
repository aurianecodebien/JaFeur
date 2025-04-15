# Guide Utilisateur – JaFeur

Bienvenue dans JaFeur, une application backend en Java Spring Boot permettant de gérer dynamiquement des conteneurs Docker via une API REST.

---

## Objectif de l'application

JaFeur est une interface de contrôle pour lancer, configurer, arrêter et surveiller des conteneurs Docker depuis une API REST.  

---

## Prise en main

## Structure du projet

- `controllers/` : endpoints REST pour démarrer, arrêter et relancer les conteneurs Docker.
- `services/` : logique métier pour interagir avec Docker en local.
- `model/` : objets de transfert de données.
- `config/` : configuration Spring, notamment l’accès à Docker.
- `src/test/` : tests unitaires des fonctions principales.

---
### Accès à l’API

Une fois l’application lancée (voir documentation d'installation), l’interface Swagger est accessible ici :

👉 [http://localhost:8080/swagger](http://localhost:8080/swagger)

Swagger permet de visualiser et tester tous les endpoints de l’application de manière interactive.

---


### Lancer et gérer des applications
- Démarrer une application (conteneur déjà existant) `PUT /Start/{name}`
- Redémarrer ou arrêter une application à la demande `PUT /Stop/{name}`
- Supprimer un conteneur devenu inutile `PUT /Remove/{name}`

---

### Configurer dynamiquement une application

Le point d’entrée `POST /Config/{id}` permet d’ajouter, de modifier ou de supprimer des variables d’environnement sur un conteneur existant.

Le corps de la requête doit être au format suivant :

```json
{
  "add": {
    "NEW_VAR": "new"
  },
  "update": {
    "EXISTING_VAR": "updated"
  },
  "delete": {
    "OLD_VAR": ""
  }
}
```

- `add` ajoute une ou plusieurs nouvelles variables.
- `update` modifie la valeur de variables existantes.
- `delete` supprime les variables spécifiées (en mettant une valeur vide).
---

### Surveiller les erreurs et crashs
- Détection automatique si un conteneur est dans un état anormal (crash) 
- Lister tous les conteneurs qui ont planté `PUT List/IsCrash`
- Permet de bâtir des outils de monitoring simples ou de diagnostic

---

### Gérer les images Docker
- **Pull** d’une image publique depuis Docker Hub (ex: `nginx:latest`) `POST /image/run/{applicationName}`
- **Build** d’une image à partir d’un Dockerfile local (ex: projet en développement) `POST /image/buildDockerfile`
- **Start** d’une image en lui passant directement les paramètres (pas besoin de docker run) `POST /image/start/`
- **Suppression** d’images obsolètes `DELETE /image`

---

### Accéder à l’état actuel du système Docker
- Voir tous les conteneurs lancés ou arrêtés `GET /containers{showAll}`

Si le paramètre `showAll` est à `true`, il permet de voir tous les conteneurs, même ceux qui ne sont pas lancés.
- Voir toutes les images présentes localement `GET /images`

---

### Mettre à jour une application
`POST /update`
- Rebuild depuis un Dockerfile donné
- Redéploiement automatique d’un conteneur avec la nouvelle version
- Très utile dans un workflow CI/CD manuel ou en déploiement progressif

---

## Astuces

- Si une image n’existe pas en local, Docker la téléchargera automatiquement (si disponible sur Docker Hub).
- Vérifier que les noms de conteneurs sont uniques pour éviter les conflits.

---

## Test et validation

L'application intègre des tests unitaires exécutables avec :

```bash
mvn test
```

Ces tests couvrent les principales fonctionnalités, notamment la gestion des conteneurs et les erreurs courantes (image non trouvée, redémarrage invalide, etc).

---

## Groupe JaFeur

Ce projet a été réalisé par trois élèves de la filière DO de l'école Polytech Montpellier :
- Auriane PUSEL
- Noa DESPAUX
- Nathan DILHAN