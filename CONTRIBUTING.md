# Contributing

We're so excited you're interested in helping with SuperTokens! We are happy to help you get started, even if you don't
have any previous open-source experience :blush:

## New to Open Source?

1. Take a look
   at [How to Contribute to an Open Source Project on GitHub](https://egghead.io/courses/how-to-contribute-to-an-open-source-project-on-github)
2. Go thorugh
   the [SuperTokens Code of Conduct](https://github.com/supertokens/supertokens-postgresql-plugin/blob/master/CODE_OF_CONDUCT.md)

## Where to ask Questions?

1. Check our [Github Issues](https://github.com/supertokens/supertokens-postgresql-plugin/issues) to see if someone has
   already answered your question.
2. Join our community on [Discord](https://supertokens.io/discord) and feel free to ask us your questions

## Development Setup

### Prerequisites

- OS: Linux or macOS
- IDE: Intellij (recommended) or equivalent IDE
- PostgreSQL

### Project Setup

1. Setup the `supertokens-core` by
   following [this guide](https://github.com/supertokens/supertokens-core/blob/master/CONTRIBUTING.md#development-setup)
   . If you are not modifying the `supertokens-core` repo, then you do not need to fork that.
2. Start PostgreSQL on port `5432`, listening to `locahost` or `0.0.0.0`.
3. Create a PostgreSQL user (if not already exists) with username `root` and password `root`
4. Create a database called `supertokens`.
5. Fork the `supertokens-pstgresql-plugin` repository
6. Open `modules.txt` in the `supertokens-root` directory and change it so that it looks like (the last line has
   changed):
   ```
   // put module name like module name,branch name,github username(if contributing with a forked repository) and then call ./loadModules script        
   core,master
   plugin-interface,master
   sql-plugin,master,<your github username>
   ```
7. Run `./loadModules` in the `supertokens-root` directory. This will clone your forked `supertokens-sql-plugin` repo.
8. Follow
   the [CONTRIBUTING.md](https://github.com/supertokens/supertokens-core/blob/master/CONTRIBUTING.md#modifying-code)
   guide from `supertokens-core` repo for modifying and testing.

## Pull Request

1. Before submitting a pull request make sure all tests have passed
2. Reference the relevant issue or pull request and give a clear description of changes/features added when submitting a
   pull request
3. Make sure the PR title follows [conventional commits](https://www.conventionalcommits.org/en/v1.0.0/) specification

## SuperTokens Community

SuperTokens is made possible by a passionate team and a strong community of developers. If you have any questions or
would like to get more involved in the SuperTokens community you can check out:

- [Github Issues](https://github.com/supertokens/supertokens-sql-plugin/issues)
- [Discord](https://supertokens.io/discord)
- [Twitter](https://twitter.com/supertokensio)
- or [email us](mailto:team@supertokens.io)

Additional resources you might find useful:

- [SuperTokens Docs](https://supertokens.io/docs/community/getting-started/installation)
- [Blog Posts](https://supertokens.io/blog/)

## Guide on using Hibernate

The setup for SuperTokens + Hibernate is atypical. We need to support all kinds of sql and no sql dbs, therefore, the
plugin interface cannot expose the domain objects classes to the core. This means that within a transaction, if the same
domain object instance needs to be used multiple times, we must find another way to store and fetch that instance -
transparently to the core.

We make use of Hibernate's L1 cache to achieve this for the most part. The problem with only using this is that it does
not save null values within a transaction, and this then leads to multiple unnecessary queries to the db. To solve this,
we maintain a separate null value cache per `Session`.

In order to minimise the number of queries to the db, we have to be careful about how we query the db in different
situations:

### One query per transaction

Here we can freely use HQL or one of the session `get` / `save` / `update` functions.

### Multiple queries per transaction

There are two types of these:

- #### One of the queries in the transaction has `ON CONFLICT DO ...` queries

  HQL has not way to to specify `ON CONFLICT DO ...`. This means that we must get and then update / save the entity by
  ourselves. Since this is all happening in a transaction, there is issue with race conditions.

  In order to do this efficiently, we must make use of our null value cache (since the first fetch of an entity may
  yield a null result, when we fetch again, we want to avoid querying the db again). Therefore, we must AVOID using HQL
  queries here.

- #### No query in the transaction has `ON CONFLICT DO ...`

  Here we can freely use HQL or one of the session `get` / `save` / `update` functions.

### Foreign key constraint

There are several ways of achieving this in Hibernate. We use the following method(Table `Q` depends on a column from
Table `F`):

- In `Q`'s DO, we add a field referencing `F`'s DO.
- This field is marked with the following annotations:
    ```
    @Getter(value = AccessLevel.PRIVATE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    ```
  `ManyToOne` may change based on the relationship. The `name` field in `JoinColumn` will change too.
- Notice that we set the getter of this to be private. The reason is that we don't want anyone outside to use this
  getter as if they use it, most likely, there will be a db query that is run to fetch all the info, even if all the
  user might want is the primary key of the `F`.
- We should manually add getter functions to get the primary key of `F` which will reference the `F`'s variable and call
  the getter on that to get the primary key. Even the internal `.equals` function should use this manual getter
  function.
- As an example, see the `PasswordResetTokensPk.java` code.

### Deprecated Methods

Since we maintain a Custom Wrapper over Session and Query interfaces, we allow only certain implementations to be 
used.  
Thus, many methods are marked as deprecated so that we can get IDE level assistance to ensure users do not use the 
methods and find it failing at runtime.