[web]
${ec2_ip} ansible_user=ubuntu ansible_ssh_private_key_file=../terraform/terraform-key.pem ansible_ssh_common_args='-o StrictHostKeyChecking=no'
