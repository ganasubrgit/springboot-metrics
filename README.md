# springboot-metrics
spring boot app monitoring using prometheus actuator

System requirement:
- Rancher Desktop with Docker Buildkit
- Git cli

# instruction to build and run
- Clone this repo
- ```cd``` to the cloned directory
-  Build the image 
Buildkit is now the default build engine in Docker. Here I use it to run build on my Kubernetes cluster. 

##### Note: make sure to replace the docker image names and registry path as per your needs in the comments

#### (Optional) Run a build pod on your k8s cluster 
```
docker buildx create --name my-builder --driver kubernetes --driver-opt replicas=1 --use
```



(or)

#### Build and push the image (Buildkit "buildx" plugin is now part of Docker) 
```
docker buildx build --tag  myregistry/myorg/demo:latest --push .

#if you want to build for multiple architecture 
docker buildx build --tag myregistry/myorg/demo:latest --push --platform linux/amd64,linux/arm64 . 
```

# Run the image 
Run locally using Docker 
```
docker run -d -p 8080:8080 myregistry/myorg/demo:latest
```

(or)

# Deploy to Kubernetes 
```
kubectl create ns demo 
kubectl -n demo create deployment demo --image=myregistry/myorg/demo:latest --port=8080 --replicas=1 
kubectl -n demo expose deployment demo --type=LoadBalancer
```

