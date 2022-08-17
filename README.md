# saga-patterns
Sample project implementing two saga patterns<br>
 
* Choreography saga
* Orchestration saga
<br>

## What is SAGA?
Saga is a mechanism that sustains data atomicity without distributed transactions in a microservice architecture.<br>
Simply, it is a consecutive local transaction.<br><br>
Each local transaction updates its data using frameworks and libraries that support ACID transaction.<br>
If a local transaction fails, transactions that have been previously completed will be rolled back by compensation transaction.<br>
By compensation transaction, saga can ensure atomicity in distributed transactions.<br>
