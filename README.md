# CAFHEG — Système de gestion des demandes d’allocations

## Description

CAFHEG est une application web développée en Java avec Spring Boot, permettant de gérer les demandes d’allocations dans un cadre administratif suisse (cantonal). Elle offre une interface REST pour :
- consulter et modifier les données des allocataires (nom, prénom, numéro AVS) ;
- déterminer, selon les situations familiales et professionnelles, quel parent a droit à une allocation ;
- consulter les allocations en cours et les montants associés ;
- générer des relevés de versements et des états de droits au format PDF ;
- accéder à des statistiques de versement par année, y compris pour les allocations de naissance.

L’application est principalement destinée aux agents administratifs ou à des systèmes tiers via des appels API sécurisés.

---

## 🧩 Fonctionnalités principales

### Gestion des allocataires

- Rechercher les allocataires par nom (`findAllAllocataires`)
- Modifier le nom et prénom d’un allocataire
- Supprimer un allocataire **seulement s’il n’a reçu aucun versement**
- Données d’identification : nom, prénom, numéro AVS

### Évaluation du droit à l’allocation

- Traitement d’un formulaire (`ParentAllocationRequest`) contenant :
    - lieu de résidence de l’enfant
    - activité lucrative et lieu de résidence des deux parents
    - montant des salaires
    - statut de vie commune
- Détermination du parent ayant droit selon une règle métier :
    - priorité à l’activité lucrative
    - en cas d’égalité, comparaison des salaires (`getParentDroitAllocation`)

### Gestion des allocations

- Liste de toutes les allocations en cours (`findAllocationsActuelles`)
- Données des allocations :
    - montant (`Montant`)
    - période (début – fin)
    - canton attribuant

### Gestion des versements

- Structure des versements par parent et enfant (`VersementParentEnfant`)
- Accès aux montants versés via le service `VersementService`

### Statistiques annuelles

- Somme totale des allocations pour une année donnée :  
  `GET /allocations/{year}/somme`
- Somme des allocations **de naissance** pour une année donnée :  
  `GET /allocations-naissances/{year}/somme`

### Exportation PDF

- Génération de documents PDF pour :
    - les versements mensuels par date (`generatePDFVversement`)
    - les allocations par enfant (`generatePDFAllocataire`)
- Basé sur la bibliothèque **PDFBox**
- Inclut les informations de l’allocataire et des montants associés

### Interface REST complète

- Tous les services sont exposés via un `@RestController`
- Endpoints documentés et testables via **Swagger** ou un client HTTP
- Fonctionnalités exposées :
    - CRUD partiel sur les allocataires
    - Récupération des allocations
    - Statistiques par année
    - Export des documents PDF

---

## Technologies utilisées

- **Java 11** (Amazon Corretto 11.0.26)
- **Spring Boot 2.5.6** (framework principal de l'application)
- **Tomcat 9.0.54** (serveur embarqué intégré à Spring Boot)
- **Swagger** via Springfox 3.0.0 (documentation interactive de l’API)
- **H2 Database** (base de données en mémoire, utilisée pour le développement)
- **Flyway 7.7.3** (gestion des migrations de base de données)
- **PDFBox 2.0.24** (génération de fichiers PDF)
- **JUnit 5**, **Mockito**, **AssertJ** (frameworks de test unitaire)
- **SLF4J + Logback** (système de journalisation)
- **Dépendances gérées manuellement via le dossier `lib/`**

---

## Installation

### Prérequis
- Java JDK 11 (confirmé avec Amazon Corretto 11.0.26)
- IntelliJ IDEA (ou un autre IDE Java)
- Git (pour cloner le projet)
- Aucun outil de build requis (pas de Maven ni Gradle)

### Étapes
1. Cloner le projet :
```bash
git clone https://github.com/ArnaudBarth/cafheg-master.git
cd cafheg-master
```

2. Ouvrir le dossier `cafheg-master` dans IntelliJ IDEA :
   - Configurer le projet pour utiliser Java 11
   - Vérifier que tous les `.jar` du dossier `lib/` sont bien attachés au classpath

3. Lancer l’application :
   - Ouvrir la classe `Application.java` (dans `ch.hearc.cafheg.infrastructure.application`)
   - Faire un clic droit sur la méthode `main()` et sélectionner **Run 'Application.main()'**

4. Accéder à l’API :
   - Application : [http://localhost:8080/](http://localhost:8080/)
   - Interface Swagger : [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)


### Accès à l’API

---

## Utilisation

Une fois l’application lancée, l’interface REST permet aux utilisateurs (agents ou systèmes tiers) d’interagir avec les différentes fonctionnalités via Swagger ou un client HTTP.

### Interactions possibles

- **Accès Swagger UI** : [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **Requête d’allocataires** :
    - `GET /allocataires` : retourne tous les allocataires
    - `PUT /allocataires/{id}` : modifie un allocataire
    - `DELETE /allocataires/{id}` : supprime un allocataire (s'il n'a reçu aucun versement)
- **Détermination du droit à l’allocation** :
    - `POST /droits/quel-parent` avec un objet `ParentAllocationRequest` JSON
- **Consultation des allocations** :
    - `GET /allocations`
    - `GET /allocations/{year}/somme` pour les statistiques annuelles
    - `GET /allocations-naissances/{year}/somme` pour les naissances
- **Exportation PDF** :
    - `GET /allocataires/{id}/allocations` : allocations en PDF
    - `GET /allocataires/{id}/versements` : versements en PDF

### Données attendues

Les endpoints nécessitent parfois des paramètres (ex. : année, identifiant) ou un corps JSON conforme aux modèles décrits dans la documentation Swagger.
#### En cas d'erreur (ex. : données manquantes, table absente, etc.), une réponse structurée contenant un code HTTP et un message est retournée.

---

## Structure du projet (extrait)

```
📁 src/
├── 📁 main/
│   ├── 📁 java/
│   │   └── 📁 ch/hearc/cafheg/
│   │       ├── 📁 business/
│   │       │   ├── 📁 allocations/
│   │       │   ├── 📁 versements/
│   │       │   └── 📁 common/
│   │       └── 📁 infrastructure/
│   │           ├── 📁 api/
│   │           ├── 📁 application/
│   │           ├── 📁 openapi/
│   │           ├── 📁 pdf/
│   │           └── 📁 persistance/
│   └── 📁 resources/
│       ├── 📁 db/
│       │   ├── 📁 ddl/
│       │   │   └── 📄 V1__ddl.sql
│       │   └── 📁 dml/
│       │       └── 📄 V2__dml.sql
│       └── 📄 logback.xml
├── 📁 test/
└── 📄 README.md
```

---

## Tests

Les tests unitaires sont implémentés avec **JUnit 5**, **Mockito** et **AssertJ**.

### Technologies utilisées
- JUnit Jupiter 5.7
- Mockito
- AssertJ
- H2 (base en mémoire pour les composants liés à la persistance)

### Cas testés
#### AllocationServiceTest
- Recherche d’allocataires (résultat vide ou liste)
- Récupération des allocations en cours
- Détermination du parent ayant droit selon la règle métier (activité lucrative et salaires)

#### VersementServiceTest
- Calcul de la somme des **allocations naissance** par année
- Calcul de la somme des **allocations** classiques par année
- Exportation PDF des **allocataires** (allocations par enfant)
- Exportation PDF des **versements** mensuels (par date)

### Stratégie

Tous les composants de persistance (`Mapper`) sont mockés via Mockito. Les PDF sont générés avec un PDFExporter réel mais reposent sur des données simulées. Les assertions vérifient les sommes attendues et la non-nullité des documents générés.

### Exécution

Tu peux lancer les tests :
- depuis IntelliJ avec clic droit > Run sur la classe
- ou en ligne de commande (si `mvn test` ou `gradle test` est configuré)

---

## Évolutions possibles

Plusieurs pistes d’amélioration ont été identifiées pour faire évoluer le projet :

- Implémenter une gestion rigoureuse des transactions **ACID** (actuellement, autocommit implicite)
- Intégrer une librairie SQL comme **Spring JDBC Template**, **JooQ** ou **JDBI3**
- Intégrer un framework **ORM** tel que **JPA/Hibernate** ou **MyBatis**
- Utiliser **Spring Core** pour bénéficier d’un conteneur d’injection de dépendances
- Créer une interface utilisateur côté serveur avec **Spring MVC**
- Créer une interface utilisateur côté client avec un framework comme **Vue.js**
- Ajouter une couche de sécurité via **Spring Security** (authentification, autorisations)
- Étendre l’application en exposant d'autres **services web REST**

Ces idées constituent une base pour enrichir l’application dans un cadre pédagogique ou professionnel.

## Auteurs

Projet réalisé dans le cadre du cours **Pratiques de développement** à la **HEG-Arc**.

**Professeur**  
Antoine Induni — [antoine.induni@he-arc.ch](mailto:antoine.induni@he-arc.ch)

**Étudiant·e·s**
- Mélanie L'épée — [melanie.lepee@he-arc.ch](mailto:melanie.lepee@he-arc.ch)
- Arnaud Barth — [arnaud.barth@he-arc.ch](mailto:arnaud.barth@he-arc.ch)
- Praveen Prabaharan — [praveen.prabaharan@he-arc.ch](mailto:praveen.prabaharan@he-arc.ch)
- Ismaël Lehmann — [ismael.lehmann@he-arc.ch](mailto:ismael.lehmann@he-arc.ch)
- Arno Peter — [arno.peter@he-arc.ch](mailto:arno.peter@he-arc.ch)