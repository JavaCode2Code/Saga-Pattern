# Saga Pattern: Food Order (Spring Boot, orchestration)

Placing an order touches three services that each own their own database, so there is no single
transaction. A saga replaces it with a sequence of local transactions and, on failure, undo actions
(compensations) run in reverse.

```
POST /api/orders
      |
 order-service (orchestrator, :9001)
      |  1. reserve stock      ---> inventory-service :9002   undo: release stock
      |  2. charge payment     ---> payment-service   :9003   undo: refund
      |  3. schedule delivery  ---> delivery-service  :9004   undo: cancel delivery
      v
  CONFIRMED  (all steps ok)
  CANCELLED  (a step failed, every earlier step was undone)
  COMPENSATION_FAILED (an undo kept failing: needs attention, see below)
```

Java 21, Spring Boot 3.3.5, H2 in memory (no database to install). Data resets on restart.

## Run

```bash
mvn clean package -DskipTests
./run-all.sh        # starts all four jars in the background, logs in ./logs
./stop-all.sh
```

## Scenarios

Seed data: stock PIZZA x10 @ 300, BURGER x2 @ 150. Accounts alice = 1000, bob = 50.
Price always comes from inventory, never from the request.

```bash
O=localhost:9001/api/orders
H='Content-Type: application/json'

# 1. Happy path -> CONFIRMED, amount 600
curl -X POST $O -H "$H" -d '{"customerId":"alice","itemId":"PIZZA","quantity":2,"address":"12 MG Road"}'

# 2. Step 1 fails (only 2 burgers) -> CANCELLED, nothing to undo
curl -X POST $O -H "$H" -d '{"customerId":"alice","itemId":"BURGER","quantity":5,"address":"12 MG Road"}'

# 3. Step 2 fails (bob has 50, order costs 300) -> stock reserved, then released
curl -X POST $O -H "$H" -d '{"customerId":"bob","itemId":"PIZZA","quantity":1,"address":"12 MG Road"}'

# 4. Step 3 fails -> payment refunded AND stock released
curl -X POST $O -H "$H" -d '{"customerId":"alice","itemId":"PIZZA","quantity":1,"address":"UNSERVICEABLE zone"}'

# audit trail for any order, and the resulting state in each service
curl localhost:9001/api/orders/4/saga
curl localhost:9002/api/inventory/stock
curl localhost:9003/api/payments/accounts
curl localhost:9004/api/deliveries
```

Scenario 4 audit trail looks like: EXECUTE RESERVE_INVENTORY ok, EXECUTE CHARGE_PAYMENT ok,
EXECUTE SCHEDULE_DELIVERY failed, COMPENSATE SCHEDULE_DELIVERY ok, COMPENSATE CHARGE_PAYMENT ok,
COMPENSATE RESERVE_INVENTORY ok. Stock and balance end up exactly where they started.

To see a crash-style failure, stop payment-service and place an order: step 2 fails with
"Service unavailable", stock is released.

## Viewing the H2 databases

Each service has its own in-memory database with a web console (start the service first):

| Service | Console | JDBC URL |
|---|---|---|
| order-service | http://localhost:9001/h2-console | `jdbc:h2:mem:orders` |
| inventory-service | http://localhost:9002/h2-console | `jdbc:h2:mem:inventory` |
| payment-service | http://localhost:9003/h2-console | `jdbc:h2:mem:payment` |
| delivery-service | http://localhost:9004/h2-console | `jdbc:h2:mem:delivery` |

User `sa`, empty password. Useful queries: `SELECT * FROM ORDERS;` and `SELECT * FROM SAGA_LOG ORDER BY ID;`.
The data lives in the running JVM, so it is gone when the service stops. Dev only: turn the console off in production.

## Design notes

- **Orchestration, not choreography.** One class (`DefaultSagaOrchestrator`) owns the sequence, so the
  flow is readable in one place. New step = new `SagaParticipant` bean with an `@Order`.
- **Every call is idempotent**, keyed by order id (unique constraint per participant). The orchestrator
  can repeat any call after a timeout without double charging or double reserving.
- **The failed step is compensated too.** A timeout does not prove the step had no effect, so its
  undo runs as well. Undo is a safe no-op when the step never happened.
- **Compensation retries** 3 times with backoff. If one still fails the order is marked
  `COMPENSATION_FAILED`; fix the cause, then `POST /api/orders/{id}/retry-compensation`.
- **Crash recovery.** No DB transaction spans remote calls. If order-service dies mid-saga the order
  stays PENDING; `StuckSagaRecovery` runs every 30s and compensates orders PENDING for over 60s.
- **Audit log** (`saga_log` table) records every step and compensation with its outcome.

## Known limits (worth knowing before production)

- A late `reserve` that arrives *after* its compensation (slow network) can leave an orphan
  reservation. The usual fix is for compensation to write a "cancelled" tombstone that makes a later
  `reserve` for that order id fail.
- The sync REST calls mean the customer waits for the whole saga. Production systems usually go async
  (Kafka/RabbitMQ with the transactional outbox pattern) and return 202 immediately.
- Participants have no authentication, and H2 is not durable.
- No automated tests yet. A good first one: a participant stub that fails on step N, asserting the
  compensation order for each N.
