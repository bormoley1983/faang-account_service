# Account Service
Service responsible for managing customer accounts, balances and related business logic.

## Quick start

Prerequisites:
- Java 25+ (JDK)
- Docker (for container runs)
- [faang-infra services](https://github.com/bormoley1983/faang-infra) running locally or accessible

Run locally:
```sh
./gradlew bootRun
```

Run tests:
```sh
./gradlew test --info
```

Build and run in Docker:
```sh
./gradlew build
docker build -t account-service .
docker run -p 8090:8090 account-service
```

## Configuration

Main config: [src/main/resources/application.yaml](src/main/resources/application.yaml)  
Test config: [src/test/resources/application-test.yaml](src/test/resources/application-test.yaml)

## API (main endpoints)

- Accounts: `/accounts` — controller: [`faang.school.accountservice.controller.AccountController`](src/main/java/faang/school/accountservice/controller/AccountController.java)  
- Balance: `/balance` — controller: [`faang.school.accountservice.controller.BalanceController`](src/main/java/faang/school/accountservice/controller/BalanceController.java)  
- Savings accounts: `/savingsAccount` — controller: [`faang.school.accountservice.controller.SavingsAccountController`](src/main/java/faang/school/accountservice/controller/SavingsAccountController.java)  
- Tariffs: `/tariff` — controller: [`faang.school.accountservice.controller.TariffController`](src/main/java/faang/school/accountservice/controller/TariffController.java)

## Internals / Important services

- Account management: [`faang.school.accountservice.service.AccountService`](src/main/java/faang/school/accountservice/service/AccountService.java)  
- Free account number generator: [`faang.school.accountservice.service.FreeAccountNumberService`](src/main/java/faang/school/accountservice/service/FreeAccountNumberService.java)  
- Balance logic & auditing: [`faang.school.accountservice.service.BalanceService`](src/main/java/faang/school/accountservice/service/BalanceService.java) and [`faang.school.accountservice.service.BalanceAuditService`](src/main/java/faang/school/accountservice/service/BalanceAuditService.java)  
- Scheduler for interest accrual: [`faang.school.accountservice.scheduler.InterestAccrualScheduler`](src/main/java/faang/school/accountservice/scheduler/InterestAccrualScheduler.java)

## Test infra

Project uses Testcontainers for integration tests. CI config: [.github/workflows/ci.yml](.github/workflows/ci.yml)

## Contribution workflow

Submit changes to `dev-local` through a short-lived branch and pull request. The
private Jenkins multibranch job discovers pushed pull-request revisions through
authenticated manual or periodic indexing; direct pushes to `dev-local` are not
part of the accepted workflow.

## Notes and links

- Dockerfile: [Dockerfile](Dockerfile)  
- Build file: [build.gradle.kts](build.gradle.kts)

## Suggested improvements

- Move DB credentials to environment variables and document them in README; see configuration at [src/main/resources/application.yaml](src/main/resources/application.yaml).  
- Add a docker-compose example, or reference the root infrastructure stack, for local PostgreSQL and Redis dependencies.
- Add an OpenAPI / Swagger usage example and endpoint to README (project already includes springdoc in [build.gradle.kts](build.gradle.kts)).  
- Document important domain flows (balance auditing, free-account-number generation) and link to implementation: [`faang.school.accountservice.aspects.BalanceAuditingAspect`](src/main/java/faang/school/accountservice/aspects/BalanceAuditingAspect.java) and [`faang.school.accountservice.service.FreeAccountNumberService`](src/main/java/faang/school/accountservice/service/FreeAccountNumberService.java).  
- Improve CONTRIBUTING section and add example requests for key endpoints (create account, credit/debit, open savings) referencing controllers above.

**Note:** Base code structure and architecture patterns are based on [FAANG School](https://github.com/faang-school) educational project.
