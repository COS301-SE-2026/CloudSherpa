# CloudSherpa Deployment files and scripts
This directory tracks files and scripts used for CloudSherpa deployment as part of our version control

|                      |   |
| -------------------- | - |
| `deploy.sh`          | Deployment script that takes a commit hash as a commandline argument and configures the necessary environment variables to be able to pull the correct images |
| `docker-compose.yml` | Configured to pull images from the ECR after the script configured the environment |
| `cloudsherpa`        | NGINX reverse proxy config |
| `ec2-init.sh` | User data to be passed to EC2 instance on initialization. Assumes ubuntu AWS AMI. Logs written to `/var/log/user-data-init`. Password for basic auth generated and written to `/opt/cloudsherpa/basic-auth-pass`, delete after initialization |
