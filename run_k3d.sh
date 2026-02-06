#!/bin/bash

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CLUSTER_NAME="spot-cluster"
REGISTRY_NAME="spot-registry.localhost"
REGISTRY_PORT="5111"

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
        log_error "k3d is not installed. Installing k3d..."
        curl -s https://raw.githubusercontent.com/k3d-io/k3d/main/install.sh | bash
    fi

    if ! command -v kubectl &> /dev/null; then
        log_error "kubectl is not installed. Please install kubectl first."
        exit 1
    fi

    log_info "All prerequisites are met."
}

cleanup_existing() {
    log_info "Cleaning up existing resources..."

    # Stop docker-compose if running
    if [ -f "$SCRIPT_DIR/docker-compose.yaml" ]; then
        docker compose -f "$SCRIPT_DIR/docker-compose.yaml" down --remove-orphans 2>/dev/null || true
    fi

    # Delete existing k3d cluster
    if k3d cluster list | grep -q "$CLUSTER_NAME"; then
        log_info "Deleting existing k3d cluster: $CLUSTER_NAME"
        k3d cluster delete "$CLUSTER_NAME"
    fi

    # Delete existing registry
    if docker ps -a | grep -q "k3d-$REGISTRY_NAME"; then
        log_info "Removing existing registry..."
        docker rm -f "k3d-$REGISTRY_NAME" 2>/dev/null || true
    fi
}

create_cluster() {
    log_info "Creating k3d cluster with config..."
    k3d cluster create --config "$SCRIPT_DIR/infra/k3d/cluster-config.yaml"

    log_info "Waiting for cluster to be ready..."
    kubectl wait --for=condition=ready node --all --timeout=120s

    log_info "Cluster created successfully!"
}

build_and_push_images() {
    log_info "Building and pushing Docker images to local registry..."

    SERVICES=("spot-gateway" "spot-user" "spot-store" "spot-order" "spot-payment")

    for service in "${SERVICES[@]}"; do
        log_info "Building $service..."

        # Build jar
        (cd "$SCRIPT_DIR/$service" && ./gradlew bootJar -x test)

        # Build Docker image
        docker build -t "$REGISTRY_NAME:$REGISTRY_PORT/$service:latest" "$SCRIPT_DIR/$service"

        # Push to registry
        docker push "$REGISTRY_NAME:$REGISTRY_PORT/$service:latest"

        log_info "$service image pushed successfully!"
    done
}

install_argocd() {
    log_info "Installing ArgoCD..."

    # Create argocd namespace
    kubectl create namespace argocd --dry-run=client -o yaml | kubectl apply -f -

    # Install ArgoCD
    # [수정] --server-side 추가하여 에러 발생하지 않도록 했음
    kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml --server-side

    log_info "Waiting for ArgoCD to be ready..."
    kubectl wait --for=condition=available deployment/argocd-server -n argocd --timeout=300s

    # Apply ArgoCD NodePort service
    kubectl apply -f "$SCRIPT_DIR/infra/argo/argocd-ingress.yaml"

    # Get ArgoCD admin password
    log_info "ArgoCD installed successfully!"
    log_info "ArgoCD UI: http://localhost:30090"
    log_info "Getting ArgoCD admin password..."

    sleep 5
    ARGOCD_PASSWORD=$(kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d)
    log_info "ArgoCD admin password: $ARGOCD_PASSWORD"
}

deploy_infrastructure() {
    log_info "Deploying infrastructure components..."

    # Apply namespace and configmaps
    kubectl apply -f "$SCRIPT_DIR/infra/k8s/base/namespace.yaml"
    kubectl apply -f "$SCRIPT_DIR/infra/k8s/base/configmap.yaml"

    # Apply infrastructure (postgres, redis, kafka)
    kubectl apply -f "$SCRIPT_DIR/infra/k8s/base/postgres.yaml"
    kubectl apply -f "$SCRIPT_DIR/infra/k8s/base/redis.yaml"
    kubectl apply -f "$SCRIPT_DIR/infra/k8s/base/kafka.yaml"

    # Apply monitoring system (loki, grafana, fluent-bit)
    kubectl apply -f "$SCRIPT_DIR/infra/k8s/base/loki.yaml"
    kubectl apply -f "$SCRIPT_DIR/infra/k8s/base/grafana.yaml"
    kubectl apply -f "$SCRIPT_DIR/infra/k8s/base/fluent-bit.yaml"

    log_info "Waiting for infrastructure to be ready..."
    kubectl wait --for=condition=available deployment/postgres -n spot --timeout=120s
    kubectl wait --for=condition=available deployment/redis -n spot --timeout=120s
    kubectl wait --for=condition=available deployment/kafka -n spot --timeout=120s

    log_info "Infrastructure deployed successfully!"

    log_info "Waiting for monitoring system to be ready..."
    kubectl wait --for=condition=available deployment/loki-deploy -n monitoring --timeout=120s
    kubectl wait --for=condition=available deployment/grafana-deploy -n monitoring --timeout=120s
    kubectl rollout status daemonset/fluent-bit-daemon -n monitoring --timeout=120s

    log_info "Monitoring System deployed successfully!"
}

deploy_applications() {
    log_info "Deploying applications..."

    # Apply application manifests
    kubectl apply -f "$SCRIPT_DIR/infra/k8s/apps/"

    log_info "Waiting for applications to be ready..."
    kubectl wait --for=condition=available deployment/spot-user -n spot --timeout=180s || true
    kubectl wait --for=condition=available deployment/spot-store -n spot --timeout=180s || true
    kubectl wait --for=condition=available deployment/spot-order -n spot --timeout=180s || true
    kubectl wait --for=condition=available deployment/spot-payment -n spot --timeout=180s || true
    kubectl wait --for=condition=available deployment/spot-gateway -n spot --timeout=180s || true

    log_info "Applications deployed!"
}

show_status() {
    log_info "=== Cluster Status ==="
    echo ""

    log_info "Nodes:"
    kubectl get nodes
    echo ""

    log_info "Pods in spot namespace:"
    kubectl get pods -n spot
    echo ""

    log_info "Services in spot namespace:"
    kubectl get svc -n spot
    echo ""

    log_info "ArgoCD pods:"
    kubectl get pods -n argocd
    echo ""

    log_info "Pods in monitoring namespace:"
    kubectl get pods -n monitoring
    echo ""

    echo "=============================================="
    echo -e "${GREEN}K3d cluster is ready!${NC}"
    echo ""
    echo "Access points:"
    echo "  - Gateway API: http://localhost:30080"
    echo "  - ArgoCD UI:   http://localhost:30090"
    echo "  - Grafana UI:  http://localhost:30070"
    echo ""
    echo "ArgoCD credentials:"
    echo "  - Username: admin"
    ARGOCD_PASSWORD=$(kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" 2>/dev/null | base64 -d || echo "Not available yet")
    echo "  - Password: $ARGOCD_PASSWORD"
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
            log_info "Cleanup completed!"
            exit 0
            ;;
        --status)
            show_status
            exit 0
            ;;
        --help)
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --clean     Clean up existing cluster and resources"
            echo "  --status    Show cluster status"
            echo "  --help      Show this help message"
            echo ""
            echo "Without options, creates a new k3d cluster with all services."
            exit 0
            ;;
    esac

    check_prerequisites
    cleanup_existing
    create_cluster
    build_and_push_images
    install_argocd
    deploy_infrastructure
    deploy_applications
    show_status
}

main "$@"
