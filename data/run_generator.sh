#!/bin/bash

# 더미 데이터 생성 스크립트
# Usage: ./run_generator.sh [output_file]

OUTPUT_FILE=${1:-"dummy_data.sql"}

echo "=== Food Delivery Platform Dummy Data Generator ==="
echo "Output file: $OUTPUT_FILE"
echo ""

# Faker 패키지 설치 확인
if ! python3 -c "import faker" 2>/dev/null; then
    echo "Installing required package: faker..."
    pip install faker --break-system-packages
fi

# 더미 데이터 생성
echo "Generating dummy data..."
python3 generate_dummy_data.py > "$OUTPUT_FILE"

# 결과 확인
if [ $? -eq 0 ]; then
    echo ""
    echo "✓ Data generation completed successfully!"
    echo "✓ Output file: $OUTPUT_FILE"
    echo "✓ File size: $(du -h "$OUTPUT_FILE" | cut -f1)"
    echo ""
    echo "To import the data into PostgreSQL:"
    echo "  psql -U your_username -d your_database -f $OUTPUT_FILE"
    echo ""
    echo "Or for MySQL:"
    echo "  mysql -u your_username -p your_database < $OUTPUT_FILE"
else
    echo ""
    echo "✗ Data generation failed!"
    exit 1
fi
