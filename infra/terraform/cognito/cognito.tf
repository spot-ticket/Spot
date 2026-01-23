resource "aws_cognito_user_pool" "pool" {
  name = "spot_cognito_user_pool"

  schema {
      name                = "user_id"
      attribute_data_type = "String"
      mutable             = true
      required            = false
    }

    schema {
      name                = "role"
      attribute_data_type = "String"
      mutable             = true
      required            = false
    }


    lambda_config {
        post_confirmation = aws_lambda_function.post_confirm.arn

        # access token customization을 위해 "pre_token_generation_config" 사용(Trigger event version V2_0)
        pre_token_generation_config {
          lambda_arn     = aws_lambda_function.pre_token.arn
          lambda_version = "V2_0"
        }
      }


}



