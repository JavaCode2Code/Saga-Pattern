The Saga pattern is a design pattern for managing data consistency across multiple microservices without using a distributed (two-phase commit) transaction.

A saga breaks one large business transaction into a sequence of smaller local transactions. Each service performs its own local transaction and then triggers the next step. If any step fails, the saga runs compensating transactions to undo the work already completed by earlier steps.

Why it is needed

In a microservices architecture, each service owns its own database. A single ACID transaction cannot span them, and distributed transactions (2PC/XA) are slow, tightly coupled and do not scale well. The saga gives you eventual consistency instead.

Key concepts
Local transaction: a step that commits in one service’s own database.
Compensating transaction: an action that semantically reverses a completed step (e.g., cancelOrder undoes createOrder, refundPayment undoes chargePayment). It is a new business action, not a database rollback.
Eventual consistency: the system may be briefly inconsistent while the saga is in progress, but it ends in a consistent state.
Example: order flow
Create Order → Reserve Inventory → Charge Payment → Ship Order

If Charge Payment fails:

Release Inventory (compensate) → Cancel Order (compensate)
Two implementation styles
	Choreography	Orchestration
Control	Decentralized: services react to each other’s events	Centralized: an orchestrator tells each service what to do
Communication	Event-driven (Kafka, RabbitMQ)	Commands/replies via the orchestrator
Pros	Simple for few steps, loose coupling	Clear flow, easier to track and debug
Cons	Hard to follow as steps grow, risk of cyclic dependencies	Orchestrator is extra logic and a possible bottleneck
In Java

Common tools: Spring Boot with Kafka/RabbitMQ for choreography, and Axon Framework, Eventuate Tram Saga, Camunda/Temporal, or Netflix Conductor for orchestration.

Short definition (for notes or exams)

The Saga pattern manages distributed transactions in microservices by splitting them into a sequence of local transactions, each with a compensating transaction that is executed if a later step fails, ensuring eventual consistency without two-phase commit.
