## **GitHub proxy application**

Swagger endpoint (in case app will be exposed with 8080): http://localhost:8080/swagger-ui/index.html#

## How to run app

To run app in Docker there's created a Dockerfile.

* docker build -t git_proxy:latest -f Dockerfile .
* docker run -p8080:8080 git_proxy:latest