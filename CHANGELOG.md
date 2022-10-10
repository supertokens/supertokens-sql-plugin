# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

- Updates `password_hash` and `third_party_user_id` column sizes
- Adds remaining changes for UserId mapping queries
- Adds LOG_LEVEL support
- Sets up SessionFactory connection and modifies how initial connection retries are done.
- Sets up logging handlers for hibernate and jboss
- Adds hibernate dependencies to build.gradle and modifies the implementationDependencies.json file
- Adds support to export test runs details from github actions
- Migrate UserMetadata Queries to use Hibernate
- Adds UserIdMapping Queries to Hibernate
- Migrates SessionInfo Queries to use Hibernate
- Migrates Passwordless Queries to use Hibernate
- Migrates JWTSigning Queries to use Hibernate
- Mark Non-Implemented Methods with @Deprecated
- Migrate UserRoles Queries to use Hibernate
- Migrate EmailVerification Queries to use Hibernate