# Documentation de indexation-sujet-batch

Ce dépôt héberge le code source du composant **indexation-sujet-batch** du projet **INDEXATION_SUJET** conçu et maintenu par l'**ABES** (Agence Bibliographique de l'Enseignement Supérieur). 

Ce module est un traitement planifié (Batch) qui extrait des informations concernant des notices bibliographiques, et les transfert sur les machines donut de l'ABES sous forme de fichier CSV. Ce fichier sera utilisé par le module [indexation-sujet-vectorisation](https://github.com/abes-esr/indexation-sujet-vectorisation) afin de vectoriser chaque notice.

Les données sont extraites de la base de données via la requête SQL du fichier [notices_query.sql](https://github.com/abes-esr/indexation-sujet-batch/blob/develop/src/main/resources/sql/notices_query.sql).

---
## 1. Liens vers les autres parties du projet

Le projet INDEXATION_SUJET est composé de plusieurs modules complémentaires hébergés sur l'organisation [abes-esr](https://github.com/abes-esr) :

| Dépôt GitHub                                                                                     | Rôle & Description                                                                                                                    |
| :----------------------------------------------------------------------------------------------- | :------------------------------------------------------------------------------------------------------------------------------------ |
| [**indexation-sujet-docker**](https://github.com/abes-esr/indexation-sujet-docker)               | Configuration Docker Compose pour le déploiement global de la plateforme (API, Qdrant, Ollama/vLLM).                                  |
| [**indexation-sujet-api**](https://github.com/abes-esr/indexation-sujet-api)                     | Service web backend d'inférence (FastAPI) exposant le service de suggestion d'indexation sujet RAMEAU.                                |
| [**indexation-sujet-vectorisation**](https://github.com/abes-esr/indexation-sujet-vectorisation) | **Ce dépôt** : Pipeline de vectorisation batch des notices RAMEAU et de peuplement des collections Qdrant.                            |
| [**indexation-sujet-batch-docker**](https://github.com/abes-esr/indexation-sujet-batch-docker)   | Déploiement Docker pour les traitements par lots et les interfaces de mise à jour des données.                                        |
| [**indexation-sujet-batch**](https://github.com/abes-esr/indexation-sujet-batch)                 | Extraction, transformation et génération des dumps de données d'autorités RAMEAU et notices Sudoc.                                    |
| [**indexation-sujet-script-winibw**](https://github.com/abes-esr/indexation-sujet-script-winibw) | Script client (VBScript) s'intégrant au client lourd de catalogage **WinIBW** pour interroger l'API depuis le poste des catalogueurs. |
## 2. Flux entrants et sortants

### 2.1. Flux entrants

Le batch utilise les données issues de la base de données bibliographique.

Les informations exploitées comprennent notamment :
- Les titres et résumés des notices bibliographiques.
- Les sujets RAMEAU associés aux notices.
- Les informations nécessaires à la sélection des notices.

Les données sont extraites au moyen d'une [requête SQL](https://github.com/abes-esr/indexation-sujet-batch/blob/develop/src/main/resources/sql/notices_query.sql) qui appliquent les règles de sélection et de préparation nécessaires à la constitution du jeu de données.

Le batch prend un paramètre à l'exécution qui décide du périmètre traité :

- **Mode** `init` : extraction faite sur l'entièreté de la base de données.
- **Mode** `update` : extraction limitée aux notices modifiées depuis moins d'une semaine.

### 2.2. Flux sortants

Le batch produit un fichier CSV contenant les données préparées pour le composant de vectorisation.

Avant son transfert, le fichier est alimenté à partir des résultats de l'extraction et les données récupérées font l'objet de contrôles, notamment pour éviter les doublons.

Le fichier est ensuite envoyé au composant `indexation-sujet-vectorisation` via l'url `https://indexation-sujet.sudoc.fr/api/v1/init/upload` ou `https://indexation-sujet.sudoc.fr/api/v1/update/upload`.

***
### 3. Schéma de fonctionnement

```text
┌──────────────────────────────────────┐
│          Base de données             │
│                                      │
│  - Notices                           │
│  - Titres et résumés                 │
│  - Sujets RAMEAU associés            │
│                                      │
└──────────────────┬───────────────────┘
                   │
                   │ Requête SQL
                   ▼
┌──────────────────────────────────────┐
│         serveur diplotaxis8          │
│                                      │
│  Batch Spring                        │
│  - Extraction des données            │
│  - Contrôle de cohérence             │
│  - Détection des doublons            │
│  - Génération du CSV                 │
└──────────────────┬───────────────────┘
                   │
                   │ Transfert du fichier CSV
                   ▼
┌──────────────────────────────────────┐
│           Serveur donut              │
│                                      │
│  indexation-sujet-vectorisation      │
│  - Réception des données             │
│  - Vectorisation                     │
│  - Insertion dans la base            │
│    vectorielle Qdrant                │
└──────────────────────────────────────┘
```

## 4. Procédure de déploiement / d'installation

> ℹ️ **Architecture de déploiement ABES** : Sur les serveurs de test et de production (`diplotaxis8-test` et `diplotaxis8-prod`), seul le dépôt [**indexation-sujet-batch-docker**](https://github.com/abes-esr/indexation-sujet-batch-docker) est installé (dans `/opt/pod/indexation-sujet-batch-docker/`). Il instancie l'image Docker précompilée du batch. Les procédures ci-dessous décrivent le build local ou l'exécution autonome du code source pour les développeurs.

### A. Déploiement avec Docker (Recommandé)

Le projet s'exécute de façon conteneurisée via le [`Dockerfile`](https://github.com/abes-esr/indexation-sujet-batch/blob/test/Dockerfile).

```shell
# 1. Cloner le dépôt sur la machine hôte
git clone https://github.com/abes-esr/indexation-sujet-batch.git
cd indexation-sujet-batch

# 2. Copier le fichier d'environnement
cp .env-dist .env
# 3. Éditer le fichier .env selon l'environnement ciblé (localhost, test ou prod)

# 4. Construire l'image Docker de production
docker build -t abesesr/indexation-sujet:batch --target batch-image .

# 5. Lancer le conteneur. Le batch s'exécute une fois par semaine
docker run -d \
  --name indexation-sujet-batch \
  --restart unless-stopped \
  --env-file .env 
```
  
### B. Installation locale pour le développement

```shell
# 1. Cloner le dépôt sur la machine hôte
git clone https://github.com/abes-esr/indexation-sujet-batch.git
cd indexation-sujet-batch

# 2. Copier le fichier d'environnement
cp .env-dist .env
# 3. Éditer le fichier .env selon l'environnement ciblé (localhost, test ou prod)

# 4. Lancer le batch via maven
mvn spring-boot:run

```
