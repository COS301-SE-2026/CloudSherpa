#!/bin/bash

# Utilities
log() {
    if (( $1 == 0 )); then
        "$(date) SUCCESS: $2" >> /var/log/user-data-init
    elif (( $1 == 1 )); then
        "$(date) ERROR: $2" >> /var/log/user-data-init
    else
        "$(date): $2" >> /var/log/user-data-init
    fi
}

sherpa-copy() {
    if cp $1 $2; then
        log 0 "Copied $1 to $2"
    else
        log 1 "Failed to copy $1 to $2"
    fi
}

# logs: /var/log/user-data-init
touch /var/log/user-data-init

# Clone repo
# ! TEMP SWITCH TO STAGING BRANCH
(cd /tmp && git clone https://github.com/COS301-SE-2026/CloudSherpa.git && cd CloudSherpa && git fetch origin && git checkout feature/staging ) 
mkdir -p /opt/cloudsherpa
chown ubuntu:ubuntu /opt/cloudsherpa

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
sudo apt install apache2-utils nginx -y
# create .htpasswd file
PASS=$(openssl rand -base64 12)
echo "$PASS" > /opt/cloudsherpa/basic-auth-pass
chmod 644 /opt/cloudsherpa/basic-auth-pass

htpasswd -bc /etc/nginx/.htpasswd onthebigstage "$PASS"
chmod 644 /etc/nginx/.htpasswd

# Nginx unlink default config
unlink /etc/nginx/sites-enabled/default 

# Copy staging config to sites
# ? Permissions
cp /tmp/cloudsherpa/infra/deployment/nginx/cloudsherpa-staging /etc/nginx/sites-available

# symlink
ln -s /etc/nginx/sites-available/cloudsherpa-staging /etc/nginx/sites-enabled/cloudsherpa-staging

if nginx -t &> /dev/null; then
    log 0 "NGINX configuration enabled and valid"
else
    log 1 "NGINX configuration invalid"
fi

# Enable Nginx
systemctl enable nginx
# start nginx
systemctl start nginx

# Install varlock
curl -sSfL https://varlock.dev/install.sh | sh -s
echo "export PATH="${XDG_CONFIG_HOME:-~/.config}/varlock/bin:$PATH"" >> ~/.bashrc
source ~/.bashrc

if varlock --help &> /dev/null; then
    log 0 "Varlock installed"
else
    log 1 "Varlock installation failed"
fi

# Move to /opt/cloudsherpa
sherpa-copy "/tmp/CloudSherpa/infra/deployment/deploy.sh" "/opt/cloudsherpa"
chmod +x /opt/cloudsherpa/deploy.sh

sherpa-copy "/tmp/CloudSherpa/infra/deployment/docker-compose.yml" "/opt/cloudsherpa/docker-compose.yml"

mkdir -p /opt/cloudsherpa/apps/service /opt/cloudsherpa/apps/ingestion /opt/cloudsherpa/apps/dashboard

sherpa-copy "/tmp/CloudSherpa/scripts/env-validation.sh" "/opt/cloudsherpa/env-validation.sh"
chmod +x /opt/cloudsherpa/env-validation.sh 

sherpa-copy "/tmp/CloudSherpa/apps/ingestion/.env.schema" "/opt/cloudsherpa/apps/ingestion/.env.schema"
sherpa-copy "/tmp/CloudSherpa/apps/dashboard/.env.schema" "/opt/cloudsherpa/apps/dashboard/.env.schema"
sherpa-copy "/tmp/CloudSherpa/apps/service/.env.schema" "/opt/cloudsherpa/apps/service/.env.schema"

log 2 "REMINDER: Env configuration currently manual"

rm -rf /tmp/CloudSherpa

echo 2 "DONE" 
# Ready for pipeline