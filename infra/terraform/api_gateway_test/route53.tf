// Route53에서 구매한 도메인의 Hosted Zone 참조
data "aws_route53_zone" "spot" {
  name         = "spotorder.org"
  private_zone = false
}

// API 서브도메인 레코드
resource "aws_route53_record" "api" {
  zone_id = data.aws_route53_zone.spot.zone_id
  name    = "api.spotorder.org"
  type    = "A"

  alias {
    name                   = aws_lb.spot.dns_name
    zone_id                = aws_lb.spot.zone_id
    evaluate_target_health = true
  }
}

// ACM 인증서 DNS 검증 레코드
resource "aws_route53_record" "cert_validation" {
  for_each = {
    for dvo in aws_acm_certificate.spot.domain_validation_options : dvo.domain_name => {
      name   = dvo.resource_record_name
      record = dvo.resource_record_value
      type   = dvo.resource_record_type
    }
  }

  allow_overwrite = true
  name            = each.value.name
  records         = [each.value.record]
  ttl             = 60
  type            = each.value.type
  zone_id         = data.aws_route53_zone.spot.zone_id
}

// ACM 인증서 검증 완료 대기
resource "aws_acm_certificate_validation" "spot" {
  certificate_arn         = aws_acm_certificate.spot.arn
  validation_record_fqdns = [for record in aws_route53_record.cert_validation : record.fqdn]
}

// ********** //
// 라우팅 테이블 //
// ********** //
resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main.id
  }

  tags = {
    Name = "spot-public-rt"
  }
}

resource "aws_route_table_association" "public_a" {
  subnet_id      = aws_subnet.public_a.id
  route_table_id = aws_route_table.public.id
}

resource "aws_route_table_association" "public_c" {
  subnet_id      = aws_subnet.public_c.id
  route_table_id = aws_route_table.public.id
}

// Private 서브넷 라우팅 테이블 (NAT Gateway 사용)
resource "aws_route_table" "private_a" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block     = "0.0.0.0/0"
    nat_gateway_id = aws_nat_gateway.a.id
  }

  tags = {
    Name = "spot-private-rt-a"
  }
}

resource "aws_route_table" "private_c" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block     = "0.0.0.0/0"
    nat_gateway_id = aws_nat_gateway.c.id
  }

  tags = {
    Name = "spot-private-rt-c"
  }
}

resource "aws_route_table_association" "private_a" {
  subnet_id      = aws_subnet.private_a.id
  route_table_id = aws_route_table.private_a.id
}

resource "aws_route_table_association" "private_c" {
  subnet_id      = aws_subnet.private_c.id
  route_table_id = aws_route_table.private_c.id
}
