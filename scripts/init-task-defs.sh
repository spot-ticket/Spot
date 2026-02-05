#!/bin/bash

# 1. 설정
REGION="ap-northeast-2"
OUTPUT_DIR="task-definitions"
# 서비스별 태스크 정의 이름 (실제 AWS에 등록된 이름으로 수정하세요)
TASKS=("gateway-task" "user-task" "store-task" "order-task" "payment-task")

# 2. 저장할 폴더 생성
mkdir -p $OUTPUT_DIR

echo "🚀 ECS Task Definitions 다운로드 시작..."

for TASK_NAME in "${TASKS[@]}"
do
    echo "-----------------------------------------------"
    echo "📦 대상: $TASK_NAME"
    
    # 3. AWS에서 다운로드 및 불필요한 필드 제거 (jq 사용)
    # register-task-definition 시 에러를 유발하는 항목들을 삭제합니다.
    aws ecs describe-task-definition \
        --task-definition "spot-dev-$TASK_NAME" \
        --region "$REGION" \
        --query 'taskDefinition' \
        | jq 'del(.taskDefinitionArn, .revision, .status, .requiresAttributes, .compatibilities, .registeredAt, .registeredBy)' \
        > "$OUTPUT_DIR/dev-$TASK_NAME.json"

    if [ $? -eq 0 ]; then
        echo "✅ 성공: $OUTPUT_DIR/$TASK_NAME.json"
    else
        echo "❌ 실패: $TASK_NAME (이름을 확인해주세요)"
    fi
done

echo "-----------------------------------------------"
echo "✨ 모든 작업이 완료되었습니다!"