resource "aws_instance" "web" {
    ami = "ami-0a71e3eb8b23101ed"
    instance_type = "t3.micro"
    subnet_id = aws_subnet.public.id
    vpc_security_group_ids = [aws_security_group.web.id]
    associate_public_ip_address = true
    key_name = aws_key_pair.terraform_key.key_name

    tags = {
        Name = "terraform-web"
    }
}