# Orchestration Saga
<span style="font-weight: lighter">
Orchestration saga is a centralized saga pattern.<br>
A central orchestrator sends a command message to each participant, and asynchronously receives a response and processes the next step.
</span><br>

## Event Flow
<img src="https://user-images.githubusercontent.com/17774927/184935990-fa372298-f522-4578-aa80-cb77598cb2c9.png" alt="event flow"></img>
1. Create pending order and send a `ORDER_CREATED` event to order-customer topic that customer service subscribes to.
2. Customer service receives an order aggregate and verify the value of customerId field is valid.
   Sends a `CUSTOMER_APPROVED` event if it is a valid value, or a `CUSTOMER_REJECTED` event if it is not valid.
3. If a message from order-reply topic tells customerId is verified, <br>
then send a `ORDER_CREATED` event to order-restaurant topic that restaurant service subscribes to.
4. Restaurant service receives an order aggregate and creates a pending ticket if available.<br>
   Sends `TICKET_REJECTED` if ticket creation fails, `TICKET_CREATED` event if successful.
5. If a message from order-reply topic tells ticket is created successfully, <br>
then send `ORDER_CREATED` event to order-payment topic that payment service subscribes to.
6. Payment service receives an order aggregate and approve payment if available.<br>
Sends `PAYMENT_REJECETED` if payment approval fails, `PAYMENT_APPROVED` event if successful. 
7. If a message from order-reply topic tells payment is approved, <br>
then changes state of the order to `ACCEPTED` and send `ORDER_APPROVED` to order-restaurant topic.
8. Restaurant service receives a `ORDER_APPROVED` event and changes the state of the ticket to `ACCEPTED`.
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
<td>order-service</td>
<td>createOrder()</td>
<td>rejectOrder()</td>
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
<td>restaurant-service</td>
<td>createTicket()</td>
<td>rejectTicket()</td>
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
<td>order-service</td>
<td>approveOrder()</td>
<td></td>
</tr>
<tr>
<td>6</td>
<td>Retriable Transaction</td>
<td>restaurant-service</td>
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
<img src="https://user-images.githubusercontent.com/17774927/184551539-a2d2b4e5-798d-4f65-a4bd-10d8f01209c0.png" alt="saga as a state machine">


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
<p style="font-weight: bold; font-size: 13pt">1. Create an order</p>

    # if you want to force an error in the saga processing,
    # add "errorRate" param to the url.
    # eg) localhost:8001/order?errorRate=0.7
    curl -XPOST -H "Content-Type: application/json" localhost:8001/order -d'
    {
        {
            "customerId": 1,
            "restaurantId": 1,
            "payment": {
                "cvc": "123",
                "number": "0123456789",
                "yy": "24",
                "mm": "06"
            },
            "orderItems": [
            {
                "menuItemId": "1",
                "quantity": 2
            },
            {
                "menuItemId": "2",
                "quantity": 1
            }
            ]
        }
    }
    '


<p style="font-weight: bold; font-size: 13pt">2. Get an order by id</p>

    curl -XGET localhost:8001/order/$ORDER_ID
