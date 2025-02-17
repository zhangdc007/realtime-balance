#!/bin/bash

# 部署 Deployment
kubectl apply -f deployment.yaml

# 等待 Deployment 就绪
kubectl rollout status deployment/realtime-balance-deployment

# 部署 Service
kubectl apply -f service.yaml

# 部署 Horizontal Pod Autoscaler (HPA)
kubectl apply -f hpa.yaml

echo "Deployment completed successfully!"