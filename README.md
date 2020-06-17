
#Step 1 – Generate provider

Execute `sbt provider/run` then execute `curl -X GET localhost:5000/get`. Alternatively 
- build the provider image `sbt provider/docker:publishLocal`
- run a container `docker-compose up -d`
- invoke the provider on method `get` using `curl -X GET localhost:5000/get`
- clean up `docker-compose down`

#Step 2 – Register a list of providers

To demonstrate the functionality
- build the provider image `sbt provider/docker:publishLocal` and the balancer image
`sbt balancer/docker:publishLocal`
- start the cluster with, say, 4 providers by executing `docker-compose up -d --scale provider=4`
- retrieve currently registered providers `curl -X GET localhost:8080/provider`


#Step 3 – Random invocation 
to demonstrate the functionality
- build the provider image `sbt provider/docker:publishLocal` and the balancer image
`sbt balancer/docker:publishLocal`
- start the cluster with, say, 4 providers by executing `docker-compose up -d --scale provider=4`
- execute `curl -X GET localhost:8080/get` several time to see responses from different providers.