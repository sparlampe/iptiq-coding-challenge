
#Step 1 – Generate provider

Execute `sbt provider/run` then execute `curl -X GET localhost:5000/get`. Alternatively 
- build the provider image `sbt provider/docker:publishLocal`
- run a container `docker-compose up -d`
- invoke the provider on method `get` using `curl -X GET localhost:5000/get`
- clean up `docker-compose down`
