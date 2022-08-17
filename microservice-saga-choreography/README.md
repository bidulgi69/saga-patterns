# Choreography Saga
<span style="font-weight: lighter">
Choreography saga is a method in which the central orchestrator does not exist, and saga participants subscribe to events with each other.
<br>It is a highly likely way to create a cyclic dependence between services(e.g. see airline-service and payment-service in the below image) and increased interdependence between services.
</span><br>

## Event Flow
<img src="https://user-images.githubusercontent.com/17774927/185086464-a4bc8f35-db6b-4993-a4e8-e076e806f9a8.png" alt="event flow">

1. Create pending ticket and send a `TICKET_CREATED` event to tickets topic that customer service subscribes to.
2. Customer service receives a ticket aggregate and verify the value of customerId field is valid.<br>
   Sends a `CUSTOMER_APPROVED` event if it is a valid value, or a `CUSTOMER_REJECTED` event if it is not valid.<br>
   The event is forwarded through the topic "customers" that airline service subscribes to.
3. Airline service receives a ticket aggregate and check if the seat is available for reservation.<br>
Sends a `SEAT_RESERVED` event if the seat is available, or a `SEAT_REJECTED` event if it is not.
The event is forwarded through the topic "airlines" that payment service subscribes to.
4. Payment service receives a ticket aggregate and approve payment if available.<br>
Sends a `PAYMENT_REJECTED` if payment approval fails, `PAYMENT_APPROVED` event if successful.
5. `CUSTOER_REJECTED`, `SEAT_REJECTED`, `PAYMENT_REJECTED` events are forwarded through the topic "pivot" and then
execute the compensation transactions like `rejectTicket()`, `cancelSeat()`.
<br></br>

## Saga
<table style="text-align: left; vertical-align: center; font-weight: lighter">
<tr>
<td style="background-color: dodgerblue;">Stages</td>
<td>Type</td>
<td>Service Name</td>
<td>Transaction</td>
<td>Compensation Transaction</td>
</tr>
<tr>
<td>1</td>
<td>Compensatable Transaction</td>
<td>ticket-service</td>
<td>createTicket()</td>
<td>rejectTicket()</td>
</tr>
<tr>
<td>2</td>
<td>Compensatable Transaction</td>
<td>customer-service</td>
<td>verifyCustomer()</td>
<td></td>
</tr>
<tr>
<td>3</td>
<td>Compensatable Transaction</td>
<td>airline-service</td>
<td>reserveSeat()</td>
<td>cancelSeat()</td>
</tr>
<tr>
<td>4</td>
<td>Pivot Transaction</td>
<td>payment-service</td>
<td>approvePayment()</td>
<td></td>
</tr>
<tr>
<td>5</td>
<td>Retriable Transaction</td>
<td>airline-service</td>
<td>approveSeat()</td>
<td></td>
</tr>
<tr>
<td>6</td>
<td>Retriable Transaction</td>
<td>ticket-service</td>
<td>approveTicket()</td>
<td></td>
</tr>
</table>

- <strong>Compensatable Transaction</strong><br>
A transaction that can be rolled back by a compensation transaction 
<br><br>
- <strong>Pivot Transaction</strong><br>
If a pivot transaction is committed, saga will be executed to the end. (stage 6)<br>
Although it is not a compensatable transaction nor retriable transaction,<br>
it can be a last compensatable transaction or first retriable transaction.
<br><br>
- <strong>Retriable Transaction</strong><br>
A transaction after the pivot transaction (always succeed)
<br>


## State Machine Diagram
<img src="https://user-images.githubusercontent.com/17774927/185086884-2c747b0f-ed88-4285-8935-a2c7adcfe700.png" alt="state machine">

## Usage
1. Build servers and docker images

        make compile
2. Launch servers, database and kafka

        make run
3. Wait until the servers to run (<a href="https://github.com/stedolan/jq">jq</a> required)

        ./health-check-servers
4. Cleanup

        make clean

## Apis
<p style="font-weight: bold; font-size: 13pt">1. Create a ticket</p>

    # There is a 50% chance that the payment service causes an error. 
    curl -XPOST -H "Content-Type: application/json" localhost:8001/ticket -d'
    {
      "type": "ONE_WAY",
	  "customerId": "1",
	  "payment": {
         "cvc": "343",
		 "number": "0123456789",
		 "yy": "24",
		 "mm": "06"
	  },
	  "airlineId": "3",
	  "airplaneId": "3",
	  "seat": {
	  	 "airplaneId": "3",
		 "seatClass": "ECONOMY_CLASS",
		 "seatNumber": 38
	  },
	  "departure": {
		 "name": "Incheon Airport",
		 "location": { "lat": 37.463333, "long": 126.440002 }
	  },
	  "arrival": {
		 "name": "Munich Airport",
		 "location": { "lat": 48.3536621, "long": 11.77502789 }
	  },
	  "departureTime": "2022-08-18T17:30:00",
	  "arrivalTime": "2022-08-19T10:00:00"
    }
    '

<p style="font-weight: bold; font-size: 13pt">2. Get a ticket by id</p>

    curl -XGET localhost:8001/ticket/$TICKET_ID
