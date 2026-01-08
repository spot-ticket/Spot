#!/bin/bash

# Java 코드 자동 포맷팅 스크립트
# checkstyle 설정에 맞춰 import 정렬

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

cd "$PROJECT_ROOT"

# import 정렬 (checkstyle 규칙에 맞게)
# 순서: static imports → java → javax → org → com
fix_imports() {
    local file="$1"
    local temp_file=$(mktemp)

    # 파일 내용 읽기
    local in_imports=false
    local static_imports=()
    local java_imports=()
    local javax_imports=()
    local org_imports=()
    local com_imports=()
    local other_imports=()
    local before_imports=()
    local after_imports=()
    local found_first_import=false
    local finished_imports=false

    while IFS= read -r line || [[ -n "$line" ]]; do
        if [[ "$line" =~ ^import[[:space:]] ]]; then
            found_first_import=true
            if [[ "$line" =~ ^import[[:space:]]+static[[:space:]] ]]; then
                static_imports+=("$line")
            elif [[ "$line" =~ ^import[[:space:]]+java\. ]]; then
                java_imports+=("$line")
            elif [[ "$line" =~ ^import[[:space:]]+javax\. ]]; then
                javax_imports+=("$line")
            elif [[ "$line" =~ ^import[[:space:]]+org\. ]]; then
                org_imports+=("$line")
            elif [[ "$line" =~ ^import[[:space:]]+com\. ]]; then
                com_imports+=("$line")
            else
                other_imports+=("$line")
            fi
        elif [[ "$found_first_import" == false ]]; then
            before_imports+=("$line")
        else
            finished_imports=true
            after_imports+=("$line")
        fi
    done < "$file"

    # 마지막 import 그룹 확인 (뒤에서부터 찾아서 빈 줄 추가용)
    local last_group=""
    if [[ ${#other_imports[@]} -gt 0 ]]; then
        last_group="other"
    elif [[ ${#com_imports[@]} -gt 0 ]]; then
        last_group="com"
    elif [[ ${#org_imports[@]} -gt 0 ]]; then
        last_group="org"
    elif [[ ${#javax_imports[@]} -gt 0 ]]; then
        last_group="javax"
    elif [[ ${#java_imports[@]} -gt 0 ]]; then
        last_group="java"
    elif [[ ${#static_imports[@]} -gt 0 ]]; then
        last_group="static"
    fi

    # 정렬된 내용 작성
    {
        # package 및 import 이전 내용
        for line in "${before_imports[@]}"; do
            echo "$line"
        done

        # static imports (알파벳 순)
        if [[ ${#static_imports[@]} -gt 0 ]]; then
            printf '%s\n' "${static_imports[@]}" | sort -u
            if [[ "$last_group" != "static" ]]; then
                echo ""
            fi
        fi

        # java imports (알파벳 순)
        if [[ ${#java_imports[@]} -gt 0 ]]; then
            printf '%s\n' "${java_imports[@]}" | sort -u
            if [[ "$last_group" != "java" ]]; then
                echo ""
            fi
        fi

        # javax imports (알파벳 순)
        if [[ ${#javax_imports[@]} -gt 0 ]]; then
            printf '%s\n' "${javax_imports[@]}" | sort -u
            if [[ "$last_group" != "javax" ]]; then
                echo ""
            fi
        fi

        # org imports (알파벳 순)
        if [[ ${#org_imports[@]} -gt 0 ]]; then
            printf '%s\n' "${org_imports[@]}" | sort -u
            if [[ "$last_group" != "org" ]]; then
                echo ""
            fi
        fi

        # com imports (알파벳 순)
        if [[ ${#com_imports[@]} -gt 0 ]]; then
            printf '%s\n' "${com_imports[@]}" | sort -u
            if [[ "$last_group" != "com" ]]; then
                echo ""
            fi
        fi

        # other imports (알파벳 순)
        if [[ ${#other_imports[@]} -gt 0 ]]; then
            printf '%s\n' "${other_imports[@]}" | sort -u
        fi

        # import 이후 내용 (첫 줄이 빈 줄이 아니면 빈 줄 추가)
        local first_after=true
        for line in "${after_imports[@]}"; do
            if [[ "$first_after" == true ]]; then
                first_after=false
                # 첫 줄이 빈 줄이 아니면 빈 줄 추가
                if [[ -n "$line" ]]; then
                    echo ""
                fi
            fi
            echo "$line"
        done
    } > "$temp_file"

    # 3줄 이상 연속된 빈 줄을 2줄로 줄이기 (import 그룹 간 빈 줄 + 클래스 전 빈 줄 유지)
    awk '
    BEGIN { blank_count = 0 }
    /^[[:space:]]*$/ {
        blank_count++
        if (blank_count <= 1) print
        next
    }
    {
        blank_count = 0
        print
    }
    ' "$temp_file" > "$file"
    rm "$temp_file"
}

# Java 파일 import 정렬
format_imports() {
    local target_path="${1:-src}"

    echo "import 정렬 중: $target_path"
    echo "순서: static → java → javax → org → com"

    find "$target_path" -name "*.java" -type f | while read -r file; do
        echo "  처리: $file"
        fix_imports "$file"
    done

    echo "import 정렬 완료"
}

# checkstyle 검사 실행
run_checkstyle() {
    echo "Checkstyle 검사 실행 중..."
    ./gradlew checkstyleMain checkstyleTest --quiet || {
        echo ""
        echo "Checkstyle 오류 발견. 리포트 확인:"
        echo "  - build/reports/checkstyle/main.html"
        echo "  - build/reports/checkstyle/test.html"
        return 1
    }
    echo "Checkstyle 검사 통과"
}

# 사용법 출력
usage() {
    echo "사용법: $0 [옵션] [경로]"
    echo ""
    echo "옵션:"
    echo "  imports [경로]    import 정렬 (기본: src)"
    echo "  check             Checkstyle 검사 실행"
    echo "  all [경로]        import 정렬 + Checkstyle 검사"
    echo "  help              도움말 출력"
    echo ""
    echo "import 순서 (checkstyle 설정 기준):"
    echo "  1. static imports"
    echo "  2. java.*"
    echo "  3. javax.*"
    echo "  4. org.*"
    echo "  5. com.*"
    echo ""
    echo "예시:"
    echo "  $0 imports                    # src 전체 import 정렬"
    echo "  $0 imports src/main           # src/main만 정렬"
    echo "  $0 check                      # Checkstyle 검사"
    echo "  $0 all                        # 정렬 후 Checkstyle 검사"
}

# 메인 실행
main() {
    local command="${1:-help}"
    local target_path="${2:-src}"

    case "$command" in
        imports)
            format_imports "$target_path"
            ;;
        check)
            run_checkstyle
            ;;
        all)
            format_imports "$target_path"
            run_checkstyle
            ;;
        help|--help|-h)
            usage
            ;;
        *)
            echo "알 수 없는 명령어: $command"
            echo ""
            usage
            exit 1
            ;;
    esac
}

main "$@"