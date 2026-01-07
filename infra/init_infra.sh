#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
TERRAFORM_DIR="$SCRIPT_DIR/terraform"
ANSIBLE_DIR="$SCRIPT_DIR/ansible"

echo "=========================================="
echo "0. Check & Install Dependencies"
echo "=========================================="

# Check if Homebrew is installed
if ! command -v brew &> /dev/null; then
    echo "Homebrew not found. Installing..."
    /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

    # Add brew to PATH for Apple Silicon
    if [[ $(uname -m) == "arm64" ]]; then
        eval "$(/opt/homebrew/bin/brew shellenv)"
    fi
fi

# Check and install Terraform
if ! command -v terraform &> /dev/null; then
    echo "Terraform not found. Installing via Homebrew..."
    brew install terraform
else
    echo "Terraform is already installed: $(terraform version | head -n1)"
fi

# Check and install Ansible
if ! command -v ansible &> /dev/null; then
    echo "Ansible not found. Installing via Homebrew..."
    brew install ansible
else
    echo "Ansible is already installed: $(ansible --version | head -n1)"
fi

echo "=========================================="
echo "1. Terraform Init & Apply"
echo "=========================================="

cd "$TERRAFORM_DIR"
terraform init
terraform apply -auto-approve

# Get EC2 IP from Terraform output
EC2_IP=$(terraform output -raw instance_public_ip)
echo "EC2 IP: $EC2_IP"

echo "=========================================="
echo "2. Generate Ansible Inventory"
echo "=========================================="

cd "$ANSIBLE_DIR"
sed "s/\${ec2_ip}/$EC2_IP/g" inventory.ini.tpl > inventory.ini
echo "Inventory generated:"
cat inventory.ini

echo "=========================================="
echo "3. Wait for EC2 to be ready (SSH)"
echo "=========================================="

echo "Waiting for SSH to be available..."
for i in {1..30}; do
    if ssh -i "$TERRAFORM_DIR/terraform-key.pem" -o StrictHostKeyChecking=no -o ConnectTimeout=5 ubuntu@$EC2_IP "echo 'SSH Ready'" 2>/dev/null; then
        echo "SSH is ready!"
        break
    fi
    echo "Attempt $i/30: Waiting for SSH..."
    sleep 10
done

echo "=========================================="
echo "4. Run Ansible Playbook"
echo "=========================================="

ansible-playbook -i inventory.ini playbook.yml

echo "=========================================="
echo "Setup Complete!"
echo "=========================================="
echo ""
echo "EC2 IP: $EC2_IP"
echo "SSH: ssh -i $TERRAFORM_DIR/terraform-key.pem ubuntu@$EC2_IP"
echo ""
echo "Next steps:"
echo "1. Update docker-compose.yml with your Docker image"
echo "2. Set GitHub Secrets (DOCKER_REPO, EC2_HOST, etc.)"
echo "3. Push to main branch to trigger deployment"
