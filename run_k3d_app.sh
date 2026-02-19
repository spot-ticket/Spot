#!/bin/bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CLUSTER_NAME="spot-cluster"
REGISTRY_NAME="spot-registry.localhost"
REGISTRY_PORT="5111"

# 로컬 레지스트리는 프록시 우회
export NO_PROXY="${NO_PROXY:-},localhost,127.0.0.1,spot-registry.localhost,*.localhost"
export no_proxy="${no_proxy:-},localhost,127.0.0.1,spot-registry.localhost,*.localhost"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

check_prerequisites() {
    log_info "Checking prerequisites..."

    if ! command -v docker &> /dev/null; then
        log_error "Docker is not installed. Please install Docker first."
        exit 1
    fi

    if ! command -v k3d &> /dev/null; then
        log_warn "k3d is not installed. Installing k3d..."
        curl -s https://raw.githubusercontent.com/k3d-io/k3d/main/install.sh | bash
    fi

    if ! command -v kubectl &> /dev/null; then
        log_error "kubectl is not installed. Please install kubectl first."
        exit 1
    fi

    if ! command -v kustomize &> /dev/null; then
        log_warn "kustomize is not installed. Installing kustomize..."
        brew install kustomize
    fi

    if ! command -v helm &> /dev/null; then
        log_warn "helm is not installed. Installing helm..."
        brew install helm
    fi

    log_info "All prerequisites are met."
}

cleanup_existing() {
    log_info "Cleaning up existing resources..."

    if [ -f "$SCRIPT_DIR/docker-compose.yaml" ]; then
        docker compose -f "$SCRIPT_DIR/docker-compose.yaml" down --remove-orphans 2>/dev/null || true
    fi
    
    if k3d cluster list | grep -q "$CLUSTER_NAME"; then
        log_info "Deleting existing k3d cluster: $CLUSTER_NAME"
        k3d cluster delete "$CLUSTER_NAME"
    fi

    if docker ps -a | grep -q "k3d-$REGISTRY_NAME"; then
        log_info "Removing existing registry..."
        docker rm -f "k3d-$REGISTRY_NAME" 2>/dev/null || true
    fi
}

create_cluster() {
    log_info "Creating k3d cluster with config..."
    k3d cluster create --config "$SCRIPT_DIR/infra/k3d/cluster-config.yaml"

    log_info "Waiting for cluster to be ready..."
    kubectl wait --for=condition=ready node --all --timeout=180s

    log_info "Cluster created successfully!"
}

build_and_push_images() {
    log_info "Building and pushing Docker images to local registry..."

    SERVICES=("spot-gateway" "spot-user" "spot-store" "spot-order" "spot-payment")
  
    log_info "Building Kafka Connect with Debezium..."
    docker build -t "$REGISTRY_NAME:$REGISTRY_PORT/spot-connect-custom:latest" "$SCRIPT_DIR/infra/k8s/base/kafka/"
    n=0
    until [ $n -ge 3 ]; do
        docker push "$REGISTRY_NAME:$REGISTRY_PORT/spot-connect-custom:latest" && break
        n=$((n+1))
        log_warn "Push failed for spot-connect-custom. Retrying ($n/3)..."
        sleep 2
    done
    
    total=${#SERVICES[@]}
    idx=0
    for service in "${SERVICES[@]}"; do
        idx=$((idx+1))
        log_info "[$idx/$total] Building $service... "
        (cd "$SCRIPT_DIR/$service" && ./gradlew bootJar -x test)

        docker build -t "$REGISTRY_NAME:$REGISTRY_PORT/$service:latest" "$SCRIPT_DIR/$service"

        n=0
        until [ $n -ge 3 ]; do
            docker push "$REGISTRY_NAME:$REGISTRY_PORT/$service:latest" && break
            n=$((n+1))
            log_warn "[$idx/$total] Push failed for $service. Retrying ($n/3)..."
            sleep 2
        done
        log_info "[$idx/$total] $service image pushed successfully!"
    done
}

install_strimzi() {
  log_info "Installing Strimzi Kafka Operator via Helm..."
  
  kubectl create namespace spot --dry-run=client -o yaml | kubectl apply -f -

  helm repo add strimzi https://strimzi.io/charts/ >/dev/null 2>&1 || true
  helm repo update

  helm install strimzi-operator strimzi/strimzi-kafka-operator \
    --namespace kafka \
    --create-namespace \
    --set crds.enabled=true
  
  log_info "Strimzi Operator installed successfully!"
}

deploy_infra() {
    log_info "Deploying infra resources (Kafka, Temporal)..."

    # Ingress Controller 설치
    kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.14.3/deploy/static/provider/cloud/deploy.yaml
    kubectl wait --for=condition=ready pod -n ingress-nginx --selector=app.kubernetes.io/component=controller --timeout=120s

    kustomize build "$SCRIPT_DIR/infra/k8s/base/" --load-restrictor LoadRestrictionsNone | kubectl apply -f -

    log_info "Waiting for Kafka Cluster (KRaft)..."
    kubectl wait --for=condition=Ready kafka/spot-cluster -n spot --timeout=300s

    log_info "Waiting for Kafka Connect..."
    kubectl wait --for=condition=Ready kafkaconnect/spot-connect -n spot --timeout=300s

    log_info "Waiting for Kafka UI..."
    kubectl wait --for=condition=available deployment/kafka-ui -n spot --timeout=180s

    log_info "Waiting for Temporal..."
    kubectl wait --for=condition=available deployment/temporal -n spot --timeout=180s
    kubectl wait --for=condition=available deployment/temporal-ui -n spot --timeout=180s

    log_info "Infra deployed successfully!"
}

deploy_apps() {
    log_info "Deploying Spot app resources..."

    kustomize build "$SCRIPT_DIR/infra/k8s/apps/" --load-restrictor LoadRestrictionsNone | kubectl apply -f -

    log_info "Spot apps deployed successfully!"
}


show_status() {
    log_info "=== Cluster Status ==="
    kubectl get nodes
    kubectl get pods -n spot

    echo ""
    echo "Access points:"
    echo "  - Gateway API: http://spot.localhost"
    echo ""
    echo "  - Username: admin"
    echo ""
    echo "Useful commands:"
    echo "  - kubectl get pods -n spot          # Check application pods"
    echo "  - kubectl logs -f <pod> -n spot     # View pod logs"
    echo "  - k3d cluster stop $CLUSTER_NAME    # Stop cluster"
    echo "  - k3d cluster delete $CLUSTER_NAME  # Delete cluster"
    echo "=============================================="
}

main() {
    case "${1:-}" in
        --clean)
            cleanup_existing
            exit 0
            ;;
        --build_only)
            build_and_push_images
            exit 0
            ;;
        --deploy-only)
            deploy_infra
            deploy_apps
            exit 0
            ;;
    esac

    check_prerequisites
    cleanup_existing
    create_cluster
    build_and_push_images
    install_strimzi
    deploy_infra
    deploy_apps
    show_status
}

main "$@"