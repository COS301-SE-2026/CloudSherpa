#!/bin/bash

# Utilities
log() {
    if (( $1 == 0 )); then
        echo "$(date) SUCCESS: $2" >> /var/log/user-data-init
    elif (( $1 == 1 )); then
        echo "$(date) ERROR: $2" >> /var/log/user-data-init
    else
        echo "$(date): $2" >> /var/log/user-data-init
    fi
}

sherpa-copy() {
    if cp $1 $2; then
        log 0 "Copied $1 to $2"
    else
        log 1 "Failed to copy $1 to $2"
    fi
}

HOME=/home/ubuntu

# logs: /var/log/user-data-init
touch /var/log/user-data-init

# Clone repo
# ! TEMP SWITCH TO STAGING BRANCH
(cd /tmp && git clone https://github.com/COS301-SE-2026/CloudSherpa.git && cd CloudSherpa && git fetch origin && git checkout feature/staging) 
mkdir -p /opt/CloudSherpa
chown -R ubuntu:ubuntu /opt/CloudSherpa

# ---------DEPENDENCIES---------
# DOCKER

# code taken from: https://docs.docker.com/engine/install/ubuntu/#install-using-the-repository
# Add Docker's official GPG key:
# UPDATE VERY IMPORTANT, else apt package repo not working
sudo apt update
sudo apt install ca-certificates curl -y
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc

# Add the repository to Apt sources:
sudo tee /etc/apt/sources.list.d/docker.sources <<EOF
Types: deb
URIs: https://download.docker.com/linux/ubuntu
Suites: $(. /etc/os-release && echo "${UBUNTU_CODENAME:-$VERSION_CODENAME}")
Components: stable
Architectures: $(dpkg --print-architecture)
Signed-By: /etc/apt/keyrings/docker.asc
EOF

sudo apt update

sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin 
# end of borrowed code

# Docker hello world exit code success otherwise write to logs
if docker run hello-world &> /dev/null; then
    log 0 "Docker installed successfully"
else
    log 1 "Docker installation failed"
fi

# Add ubuntu user to docker 
groupadd docker
usermod -aG docker ubuntu

# NGINX
sudo apt install apache2-utils nginx certbot python3-certbot-nginx -y
# create .htpasswd file
PASS=$(openssl rand -base64 12)
echo "$PASS" > /opt/CloudSherpa/basic-auth-pass
chmod 644 /opt/CloudSherpa/basic-auth-pass

htpasswd -bc /etc/nginx/.htpasswd onthebigstage "$PASS"
chmod 644 /etc/nginx/.htpasswd

# Nginx unlink default config
unlink /etc/nginx/sites-enabled/default 

# Copy staging config to sites
# ? Permissions
sherpa-copy "/tmp/CloudSherpa/infra/deployment/nginx/CloudSherpa-staging" "/etc/nginx/sites-available/CloudSherpa-staging"

# symlink
ln -s /etc/nginx/sites-available/CloudSherpa-staging /etc/nginx/sites-enabled/CloudSherpa-staging

# Enable Nginx
systemctl enable nginx
# start nginx
systemctl start nginx

# Install varlock
curl -sSfL https://varlock.dev/install.sh | sh -s -- --dir=/usr/local/bin

if /home/ubuntu/.config/varlock/bin/varlock --help &> /dev/null; then
    log 0 "Varlock installed"
else
    log 1 "Varlock installation failed"
fi

# Move to /opt/CloudSherpa
sherpa-copy "/tmp/CloudSherpa/infra/deployment/deploy.sh" "/opt/CloudSherpa"
chmod +x /opt/CloudSherpa/deploy.sh

sherpa-copy "/tmp/CloudSherpa/infra/deployment/docker-compose.yml" "/opt/CloudSherpa/docker-compose.yml"

mkdir -p /opt/CloudSherpa/apps/service /opt/CloudSherpa/apps/ingestion /opt/CloudSherpa/apps/dashboard /opt/CloudSherpa/persistence/sherpadb

sherpa-copy "/tmp/CloudSherpa/scripts/env-validation.sh" "/opt/CloudSherpa/env-validation.sh"
chmod +x /opt/CloudSherpa/env-validation.sh 

sherpa-copy "/tmp/CloudSherpa/apps/ingestion/.env.schema" "/opt/CloudSherpa/apps/ingestion/.env.schema"
sherpa-copy "/tmp/CloudSherpa/apps/dashboard/.env.schema" "/opt/CloudSherpa/apps/dashboard/.env.schema"
sherpa-copy "/tmp/CloudSherpa/apps/service/.env.schema" "/opt/CloudSherpa/apps/service/.env.schema"

# Persistence
sherpa-copy "/tmp/CloudSherpa/persistence/sherpadb/.env.schema" "/opt/CloudSherpa/persistence/sherpadb/.env.schema"
sherpa-copy "/tmp/CloudSherpa/persistence/sherpadb/sherpadb-schema.sql" "/opt/CloudSherpa/persistence/sherpadb/sherpadb-schema.sql"


log 2 "REMINDER: Env and NGINX certs configuration currently manual"

rm -rf /tmp/CloudSherpa

# AWS CLI
apt install unzip -y
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "/tmp/awscliv2.zip"
unzip /tmp/awscliv2.zip
(cd aws && ./install)

if aws --version &> /dev/null; then
    log 0 "AWS CLI installed"
else
    log 1 "AWS CLI installation failed"
fi

log 2 "DONE" 
# Ready for pipeline