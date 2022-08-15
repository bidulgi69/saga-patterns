function wait() {
  echo "Wait for: $1... "
  while [[ "$retry" -lt 10 ]]
  do
      status=$(curl -XGET "$1" -s | jq '.status')
      if [[ "$status" == "\"UP\"" ]]
      then
        echo "Server $2 is running."
        break
      fi
      sleep 2
      ((retry++))
  done
}

wait localhost:8001/actuator/health order-service
wait localhost:8002/actuator/health customer-service
wait localhost:8003/actuator/health restaurant-service
wait localhost:8004/actuator/health payment-service

echo "All services are running now."