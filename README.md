## **GitHub proxy application**


Swagger endpoint (in case app will be exposed with 8080): http://localhost:8080/swagger-ui/index.html#


## How to run app

To run app in Docker there's created a Dockerfile.  
*mvn clean package*  
*docker build --tag=git-proxy-server:latest .*  
*docker run -p8080:8080 it-proxy-server:latest*

**Important info:**  
For integration tests it would be the best option to use wiremock to mock rests (with custom urls defined in test properties), but I had no time to do it.