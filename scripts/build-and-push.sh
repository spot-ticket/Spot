#!/bin/bash
set -e

# =============================================================================
# 설정
# =============================================================================
AWS_REGION="ap-northeast-2"
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
ECR_REGISTRY="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
PROJECT="spot"

# 서비스 목록
SERVICES=("gateway" "order" "payment" "store" "user")

# =============================================================================
# ECR 로그인
# =============================================================================
echo "🔐 ECR 로그인 중..."
aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REGISTRY}

# =============================================================================
# 빌드할 서비스 선택
# =============================================================================
if [ -n "$1" ]; then
    # 특정 서비스만 빌드
    SERVICES=("$1")
    echo "📦 ${1} 서비스만 빌드합니다."
else
    echo "📦 모든 서비스를 빌드합니다: ${SERVICES[*]}"
fi

# =============================================================================
# 각 서비스 빌드 및 푸시
# =============================================================================
for SERVICE in "${SERVICES[@]}"; do
    SERVICE_DIR="spot-${SERVICE}"
    ECR_REPO="${PROJECT}-${SERVICE}"
    IMAGE_TAG="${ECR_REGISTRY}/${ECR_REPO}:latest"

    echo ""
    echo "=============================================="
    echo "🚀 Building ${SERVICE_DIR}..."
    echo "=============================================="

    # 1. Gradle 빌드
    echo "📦 Gradle 빌드 중..."
    cd "${SERVICE_DIR}"
    ./gradlew clean build -x test

    # 2. Docker 이미지 빌드 (AMD64 for Fargate)
    echo "🐳 Docker 이미지 빌드 중..."
    docker build --no-cache --platform linux/amd64 -t ${ECR_REPO}:latest .

    # 3. 태그 지정
    docker tag ${ECR_REPO}:latest ${IMAGE_TAG}

    # 4. ECR에 푸시
    echo "⬆️  ECR에 푸시 중..."
    docker push ${IMAGE_TAG}

    echo "✅ ${SERVICE} 완료: ${IMAGE_TAG}"

    cd ..
done

echo ""
echo "=============================================="
echo "🎉 모든 서비스 빌드 및 푸시 완료!"
echo "=============================================="
