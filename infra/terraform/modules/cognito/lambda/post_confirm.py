import os
import json
import urllib.request
import urllib.error
import boto3

cognito = boto3.client("cognito-idp")

USER_SERVICE_URL = os.environ.get("USER_SERVICE_URL", "").rstrip("/")

def _post_json(url: str, payload: dict) -> dict:
    data = json.dumps(payload).encode("utf-8")
    req = urllib.request.Request(url, data=data, method="POST")
    req.add_header("Content-Type", "application/json")
    with urllib.request.urlopen(req, timeout=5) as resp:
        body = resp.read().decode("utf-8")
        return json.loads(body) if body else {}

def lambda_handler(event, context):
    print("POST_CONFIRM CALLED")
    print(json.dumps(event))
    # user-service 연동 전이면 즉시 실패
    if not USER_SERVICE_URL:
        raise Exception("USER_SERVICE_URL is not set. Block sign-up until user-service is ready.")

    user_pool_id = event["userPoolId"]
    username = event["userName"]
    attrs = event.get("request", {}).get("userAttributes", {})

    sub = attrs.get("sub")
    email = attrs.get("email")
    role = attrs.get("custom:role") or "CUSTOMER"

    if not sub:
        raise Exception("Missing 'sub' in userAttributes")

    # 1) User DB 생성(실패하면 예외로 회원가입 흐름 막힘)
    try:
        created = _post_json(
            f"{USER_SERVICE_URL}/internal/users",
            {"cognitoSub": sub, "email": email, "role": role}
        )
    except urllib.error.HTTPError as e:
        raise Exception(f"user-service returned HTTPError: {e.code}") from e
    except Exception as e:
        raise Exception("Failed to call user-service") from e

    user_id = created.get("userId")
    if user_id is None:
        raise Exception("user-service response missing userId")

    # 2) Cognito custom attribute 업데이트
    cognito.admin_update_user_attributes(
        UserPoolId=user_pool_id,
        Username=username,
        UserAttributes=[
            {"Name": "custom:user_id", "Value": str(user_id)},
            {"Name": "custom:role", "Value": str(role)},
        ],
    )

    return event
