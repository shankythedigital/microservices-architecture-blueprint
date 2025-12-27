
# common-service - Elastic Beanstalk deploy template

## What's included
- `src/main/resources/application.yml` - Spring Boot YAML using environment variables for DB config.
- `Procfile` - tells EB how to start the JAR.
- `.ebextensions/01-environment.config` - EB option settings (contains placeholders).
- **Important:** You must build your JAR into `target/common-service-0.0.1-SNAPSHOT.jar` before deploying.

## Before you deploy (replace placeholders)
1. Build your Spring Boot app:
   ```
   mvn clean package -DskipTests
   ```
   Ensure the generated JAR matches: `target/common-service-0.0.1-SNAPSHOT.jar` (or update Procfile accordingly).

2. Replace placeholders in `.ebextensions/01-environment.config` OR use `eb setenv` (recommended) to inject secrets:
   ```
   eb setenv RDS_HOST=db-common-service.xxxxxxx.region.rds.amazonaws.com RDS_PORT=3306 RDS_DBNAME=db_common-service RDS_USERNAME=adminA RDS_PASSWORD=YourStrongPassword
   ```

   *Do NOT commit real passwords to source control. Use `eb setenv` or AWS Secrets Manager.*

3. Initialize Elastic Beanstalk (if not already):
   ```
   eb init
   # choose region, platform (Java), and app name
   ```

4. Create environment (example):
   ```
   eb create common-service-env --instance_type t2.micro --single
   ```

5. Deploy:
   ```
   eb deploy
   eb open
   ```

## Security & notes
- The `.ebextensions` file contains placeholders for convenience; prefer `eb setenv` for secrets.
- Make sure each app's RDS instance allows connections from EB's security group (port 3306).
- To avoid AWS charges: terminate EB environments (`eb terminate`) and delete RDS instances when not in use.

## Helpful commands
- Show environment variables: `eb printenv`
- View logs: `eb logs`
- Delete environment: `eb terminate <env-name>`

