#!/bin/bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CLUSTER_NAME="spot-cluster"
REGISTRY_NAME="spot-registry.localhost"
REGISTRY_PORT="5111"

export NO_PROXY="${NO_PROXY:-},localhost,127.0.0.1,spot-registry.localhost,*.localhost"
export no_proxy="${no_proxy:-},localhost,127.0.0.1,spot-registry.localhost,*.localhost"

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

deploy_db() {
    log_info "Deploying DB resources (Postgres, Redis)..."

    kubectl apply -f "$SCRIPT_DIR/infra/k8s/base/namespace.yaml"

    kustomize build "$SCRIPT_DIR/infra/k8s/" --load-restrictor LoadRestrictionsNone \
        | kubectl apply -f - \
        --selector='app in (postgres,redis)' \
        --prune=false 2>/dev/null || true

    kubectl apply -f "$SCRIPT_DIR/infra/k8s/base/configmap.yaml"
    kubectl apply -f "$SCRIPT_DIR/infra/k8s/base/postgres.yaml"
    kubectl apply -f "$SCRIPT_DIR/infra/k8s/base/redis.yaml"

    if [ -f "$SCRIPT_DIR/.env" ]; then
        kubectl create secret generic spot-secrets \
            --namespace=spot \
            --from-env-file="$SCRIPT_DIR/.env" \
            --dry-run=client -o yaml | kubectl apply -f -
    fi

    log_info "Waiting for DB to be ready..."
    kubectl wait --for=condition=available deployment/postgres -n spot --timeout=180s
    kubectl wait --for=condition=available deployment/redis -n spot --timeout=180s

    log_info "DB deployed successfully!"
}

deploy_monitoring() {
    log_info "Deploying monitoring stack (Loki, Grafana, Fluent-bit)..."

    kubectl create namespace monitoring --dry-run=client -o yaml | kubectl apply -f -

    kubectl apply -f "$SCRIPT_DIR/infra/k8s/base/monitoring/loki/loki-config.yaml"
    kubectl apply -f "$SCRIPT_DIR/infra/k8s/base/monitoring/loki/loki.yaml"

    kubectl apply -f "$SCRIPT_DIR/infra/k8s/base/monitoring/grafana/grafana-config.yaml"
    kubectl apply -f "$SCRIPT_DIR/infra/k8s/base/monitoring/grafana/grafana.yaml"

    kubectl apply -f "$SCRIPT_DIR/infra/k8s/base/monitoring/fluent-bit/fluent-bit-config.yaml"
    kubectl apply -f "$SCRIPT_DIR/infra/k8s/base/monitoring/fluent-bit/fluent-bit.yaml"

    log_info "Waiting for Loki to be ready..."
    kubectl wait --for=condition=available deployment/loki-deploy -n monitoring --timeout=180s

    log_info "Waiting for Grafana to be ready..."
    kubectl wait --for=condition=available deployment/grafana-deploy -n monitoring --timeout=180s

    log_info "Waiting for Fluent-bit to be ready..."
    kubectl rollout status daemonset/fluent-bit-daemon -n monitoring --timeout=180s

    log_info "Applying Grafana dashboard ConfigMaps..."
    kustomize build "$SCRIPT_DIR/infra/k8s/monitoring/" --load-restrictor LoadRestrictionsNone | kubectl apply -f -

    log_info "Monitoring stack deployed successfully!"
}

restart_grafana_for_provisioning() {
    log_info "Restarting Grafana to apply provisioning..."
    kubectl -n monitoring rollout restart deployment/grafana-deploy || true
    kubectl -n monitoring rollout status deployment/grafana-deploy --timeout=180s || true
}

install_argocd() {
    log_info "Installing ArgoCD..."

    kubectl create namespace argocd --dry-run=client -o yaml | kubectl apply -f -

    kubectl apply -n argocd --server-side -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

    log_info "Waiting for ArgoCD to be ready..."
    kubectl wait --for=condition=available deployment/argocd-server -n argocd --timeout=300s

    if [ -f "$SCRIPT_DIR/infra/argo/argocd-ingress.yaml" ]; then
        kubectl apply -f "$SCRIPT_DIR/infra/argo/argocd-ingress.yaml"
    fi

    log_info "ArgoCD installed successfully!"
}

show_status() {
    log_info "=== Cluster Status ==="
    kubectl get nodes
    echo ""
    log_info "=== DB Pods ==="
    kubectl get pods -n spot
    echo ""
    log_info "=== ArgoCD Pods ==="
    kubectl get pods -n argocd
    echo ""
    log_info "=== Monitoring Pods ==="
    kubectl get pods -n monitoring

    echo ""
    echo "Access points:"
    echo "  - ArgoCD UI:  http://localhost:30090"
    echo "  - Grafana UI: http://grafana.localhost"
    echo ""
    echo "ArgoCD credentials:"
    echo "  - Username: admin"
    ARGOCD_PASSWORD=$(kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" 2>/dev/null | base64 -d || echo "Not available yet")
    echo "  - Password: $ARGOCD_PASSWORD"
    echo ""
    echo "Useful commands:"
    echo "  - kubectl get pods -n spot          # Check DB pods"
    echo "  - kubectl get pods -n argocd        # Check ArgoCD pods"
    echo "  - kubectl get pods -n monitoring    # Check monitoring pods"
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
        --status)
            show_status
            exit 0
            ;;
    esac

    check_prerequisites
    cleanup_existing
    create_cluster
    deploy_db
    install_argocd
    deploy_monitoring
    restart_grafana_for_provisioning
    show_status
}

main "$@"