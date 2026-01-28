#!/bin/bash
set -e

# =============================================================================
# 설정
# =============================================================================
AWS_REGION="ap-northeast-2"
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
ECR_REGISTRY="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
PROJECT="spot"
CLUSTER_NAME="spot-dev-cluster"

# 버전 태그 생성 (타임스탬프)
VERSION_TAG=$(date +%Y%m%d-%H%M%S)

# 서비스 목록
SERVICES=("gateway" "order" "payment" "store" "user")

# 자동 재배포 옵션 (기본값: true)
AUTO_DEPLOY="${AUTO_DEPLOY:-true}"

echo "📦 버전: ${VERSION_TAG}"
echo ""

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
    IMAGE_TAG_LATEST="${ECR_REGISTRY}/${ECR_REPO}:latest"
    IMAGE_TAG_VERSION="${ECR_REGISTRY}/${ECR_REPO}:${VERSION_TAG}"

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
    docker build --no-cache --platform linux/amd64 -t ${ECR_REPO}:${VERSION_TAG} .

    # 3. 태그 지정 (버전 + latest)
    docker tag ${ECR_REPO}:${VERSION_TAG} ${IMAGE_TAG_VERSION}
    docker tag ${ECR_REPO}:${VERSION_TAG} ${IMAGE_TAG_LATEST}

    # 4. ECR에 푸시 (버전 + latest)
    echo "⬆️  ECR에 푸시 중..."
    docker push ${IMAGE_TAG_VERSION}
    docker push ${IMAGE_TAG_LATEST}

    echo "✅ ${SERVICE} 완료:"
    echo "   - ${IMAGE_TAG_VERSION}"
    echo "   - ${IMAGE_TAG_LATEST}"

    cd ..
done

echo ""
echo "=============================================="
echo "🎉 모든 서비스 빌드 및 푸시 완료!"
echo "=============================================="

# =============================================================================
# ECS 서비스 자동 재배포
# =============================================================================
if [ "${AUTO_DEPLOY}" = "true" ]; then
    echo ""
    echo "=============================================="
    echo "🔄 ECS 서비스 자동 재배포 시작..."
    echo "=============================================="
    echo ""

    for SERVICE in "${SERVICES[@]}"; do
        SERVICE_NAME="${PROJECT}-${SERVICE}-service"
        TASK_FAMILY="${PROJECT}-${SERVICE}-task"
        IMAGE_URI="${ECR_REGISTRY}/${PROJECT}-${SERVICE}:${VERSION_TAG}"

        echo "🔄 ${SERVICE_NAME} 태스크 정의 업데이트 중..."

        # 1. 현재 태스크 정의 가져오기
        echo "   현재 태스크 정의 가져오는 중..."
        TASK_DEF=$(aws ecs describe-task-definition \
            --task-definition ${TASK_FAMILY} \
            --region ${AWS_REGION} \
            --query 'taskDefinition')

        if [ $? -ne 0 ]; then
            echo "⚠️  ${TASK_FAMILY} 태스크 정의를 찾을 수 없습니다. 스킵합니다."
            continue
        fi

        # 2. 새 태스크 정의 생성 (이미지 태그만 변경)
        echo "   새 태스크 정의 생성 중 (이미지: ${IMAGE_URI})..."
        NEW_TASK_DEF=$(echo "${TASK_DEF}" | jq --arg img "${IMAGE_URI}" '
            del(.taskDefinitionArn, .revision, .status, .requiresAttributes, .compatibilities, .registeredAt, .registeredBy) |
            .containerDefinitions[0].image = $img
        ')

        if [ $? -ne 0 ]; then
            echo "⚠️  태스크 정의 JSON 파싱 실패"
            continue
        fi

        # 3. 새 태스크 정의 등록 (임시 파일 사용)
        echo "   새 태스크 정의 등록 중..."
        TEMP_FILE=$(mktemp)
        echo "${NEW_TASK_DEF}" > "${TEMP_FILE}"

        REGISTER_OUTPUT=$(aws ecs register-task-definition \
            --region ${AWS_REGION} \
            --cli-input-json "file://${TEMP_FILE}" 2>&1)

        REGISTER_EXIT_CODE=$?
        rm -f "${TEMP_FILE}"

        if [ ${REGISTER_EXIT_CODE} -ne 0 ]; then
            echo "⚠️  ${TASK_FAMILY} 태스크 정의 등록 실패:"
            echo "${REGISTER_OUTPUT}"
            continue
        fi

        NEW_REVISION=$(echo "${REGISTER_OUTPUT}" | jq -r '.taskDefinition.revision')

        echo "✅ ${TASK_FAMILY}:${NEW_REVISION} 등록 완료 (이미지: ${VERSION_TAG})"

        # 4. ECS 서비스를 새 태스크 정의로 업데이트
        aws ecs update-service \
            --cluster ${CLUSTER_NAME} \
            --service ${SERVICE_NAME} \
            --task-definition ${TASK_FAMILY}:${NEW_REVISION} \
            --region ${AWS_REGION} \
            > /dev/null 2>&1

        if [ $? -eq 0 ]; then
            echo "✅ ${SERVICE_NAME} 재배포 시작됨"
        else
            echo "⚠️  ${SERVICE_NAME} 재배포 실패 (서비스가 존재하지 않을 수 있습니다)"
        fi
    done

    echo ""
    echo "=============================================="
    echo "🎉 재배포 명령 완료!"
    echo "=============================================="
    echo ""
    echo "💡 배포 상태 확인:"
    echo "   aws ecs describe-services --cluster ${CLUSTER_NAME} --services ${PROJECT}-gateway-service --region ${AWS_REGION}"
    echo ""
    echo "📌 배포된 버전: ${VERSION_TAG}"
else
    echo ""
    echo "ℹ️  자동 재배포가 비활성화되어 있습니다."
    echo "   수동으로 재배포하려면: ./scripts/force-deploy.sh"
fi

echo ""
