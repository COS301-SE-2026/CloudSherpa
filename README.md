<div align="center">
  <img src="docs/assets/team-photos/TeamLogo.png" alt="CloudSherpa Logo" width="250"/>

  <p><strong><span style="font-size: 56px; line-height: 1.1;">CloudSherpa</span></strong></p>
  <p><strong>An AI-Driven Multi-Cloud FinOps Platform</strong></p>
</div>

## Documentation

For a comprehensive overview of CloudSherpa, visit our [official documentation site](https://cos301-se-2026.github.io/CloudSherpa/).

## GitHub Project

Follow our active development, feature tracking, and issue management on our [CloudSherpa Project](https://github.com/orgs/COS301-SE-2026/projects/39).

## Project Overview

CloudSherpa is an AI-driven cloud cost optimization platform designed to analyze infrastructure usage and spending patterns across major cloud providers, including Amazon Web Services, Microsoft Azure, and Google Cloud Platform. 

As organizations migrate to cloud-native environments, managing resource efficiency is a significant challenge. CloudSherpa acts as a set of financial guardrails by collecting operational monitoring and billing data, and applying machine learning models to detect anomalies and predict future costs. Through an interactive web-based dashboard, users can visualize resource usage, forecast spending, and receive intelligent optimization recommendations to support data-driven cloud management decisions.

## Running the Project

CloudSherpa runs locally through Docker Compose. From the repository root, initialize local environment files once:

```bash
cd scripts
chmod +x env-init.sh
./env-init.sh
cd ..
```

Then start the stack:

```bash
docker compose -f infra/docker-compose.yml up --build
```

The dashboard frontend is available at `http://localhost:3000`.

For development workflows, individual service commands, ports, and troubleshooting, see [`docs/dev/CheatSheet.md`](docs/dev/CheatSheet.md), [`infra/README.md`](infra/README.md), and the app-specific READMEs under [`apps/`](apps/).

## About Team BitFlip

<div align="center">
  <img src="docs/assets/team-photos/TeamPhoto.png" alt="Team BitFlip" width="650"/>
</div>

Team BitFlip is a cross-functional group of dedicated software engineering students committed to transparency, accountability, and quality-focused delivery. We utilize an Agile delivery framework to ensure continuous alignment with our stakeholders' vision. 

**Role Allocations:**
* **Megan Norval** - *Team Lead & Core Backend Architect*
* **Gerard Jordaan** - *DevOps Architect, Full-Stack Contributor & Tester*
* **Karishma Boodhoo** - *Frontend UI/UX Engineer*
* **Cherise Heyl** - *API & Systems Integration Engineer, & Tester*
* **Flip Venter** - *Frontend Application Developer*

---
*Developed in partnership with BBD for the COS 301 Capstone Project (2026).*