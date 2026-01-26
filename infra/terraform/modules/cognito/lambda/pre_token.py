def lambda_handler(event, context):
    print("PRE_TOKEN CALLED")
    print(json.dumps(event))

# request/userAttributes 안전 처리
    req = event.get("request") or {}
    attrs = req.get("userAttributes") or {}

    user_id = attrs.get("custom:user_id")
    role = attrs.get("custom:role") or "CUSTOMER"

    # response가 None일 수 있으니 강제로 dict로 만든다
    if not isinstance(event.get("response"), dict):
        event["response"] = {}

    resp = event["response"]

    # claimsAndScopeOverrideDetails도 None일 수 있으니 강제로 dict
    if not isinstance(resp.get("claimsAndScopeOverrideDetails"), dict):
        resp["claimsAndScopeOverrideDetails"] = {}

    cas = resp["claimsAndScopeOverrideDetails"]

    if not isinstance(cas.get("accessTokenGeneration"), dict):
        cas["accessTokenGeneration"] = {}

    atg = cas["accessTokenGeneration"]

    if not isinstance(atg.get("claimsToAddOrOverride"), dict):
        atg["claimsToAddOrOverride"] = {}

    claims = atg["claimsToAddOrOverride"]

    # Access Token에 커스텀 클레임 주입
    if user_id is not None:
        claims["user_id"] = str(user_id)
    claims["role"] = str(role)

    return event
