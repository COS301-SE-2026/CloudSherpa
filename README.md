<div align="center">
  <img src="docs/assets/team-photos/bitflip.svg" alt="CloudSherpa Logo" width="300"/>

  # CloudSherpa
</div>

<div style="display: flex; justify-content: center; align-items: center; flex-direction: column;">

<div align="center">

![GitHub Issues or Pull Requests](https://img.shields.io/github/issues/COS301-SE-2026/CloudSherpa?label=open%20issues)
![GitHub Actions Workflow Status](https://img.shields.io/github/actions/workflow/status/COS301-SE-2026/CloudSherpa/dashboard-ci.yml?label=dashboard%20build)
![GitHub Actions Workflow Status](https://img.shields.io/github/actions/workflow/status/COS301-SE-2026/CloudSherpa/ingestion-ci.yml?label=ingestion%20build)
![GitHub Actions Workflow Status](https://img.shields.io/github/actions/workflow/status/COS301-SE-2026/CloudSherpa/service-ci.yml?label=service%20build)

</div>

<div align="center">

![Website](https://img.shields.io/website?url=https%3A%2F%2Fcloudsherpa.gjjcs.org%2Fhi%2F&label=cloudsherpa%20site)
[![CloudSherpa service coverage](https://img.shields.io/endpoint?url=https%3A%2F%2Fsherpa-coverage.gjjcs.org%2Fbadges%2Fservice)](https://sherpa-coverage.gjjcs.org/badges/service)
[![CloudSherpa dashboard coverage](https://img.shields.io/endpoint?url=https%3A%2F%2Fsherpa-coverage.gjjcs.org%2Fbadges%2Fdashboard)](https://sherpa-coverage.gjjcs.org/badges/dashboard)
[![CloudSherpa ingestion coverage](https://img.shields.io/endpoint?url=https%3A%2F%2Fsherpa-coverage.gjjcs.org%2Fbadges%2Fingestion)](https://sherpa-coverage.gjjcs.org/badges/ingestion)

</div>
</div>

## Important Links

🚀 [CloudSherpa Deployed](https://cloudsherpa.gjjcs.org)

🔗 [GitHub Project](https://github.com/orgs/COS301-SE-2026/projects/39)

📚 [Developer Documentation](https://bitflip301.gjjcs.org/docs/)

⚙️ [System Requirements Documentation]()

🎨 [Design System](https://bitflip301.gjjcs.org/design-system/designSystem.html)

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

### Meet the Team

**Megan Norval**

[LinkedIn](www.linkedin.com/in/megan-norval)

I am a dedicated problem solver with a strong background in Computer Science and a passion for building reliable software systems. I enjoy tackling complex technical challenges and designing solutions. My approach combines analytical thinking with strong leadership and collaboration skills to ensure high-quality outcomes across the entire development process. As the Team Lead and Core Backend Architect for CloudSherpa, I designed and implemented the database schema, normalization logic, and key backend workflows that form the foundation of our platform. I was responsible for structuring and integrating backend components to support efficient data processing and storage.

---

**Gerard Jordaan**

[LinkedIn](https://www.linkedin.com/in/gerard-jordaan/)

I am a process-oriented software engineering student with interests in DevOps and full stack development. I enjoy solving complex problems by leveraging problem decomposition and developing robust, performant, and maintainable solutions.
In our BitFlip team working on CloudSherpa for BBD, I acted as our DevOps Engineer, Full Stack Contributor, and Co-Tester, where I focused on configuring consistent development and production environments, setting up CI pipelines, implementing the SSE subsystem for streaming metrics, integrating Spring Security with JWT infrastructure, and ensuring the team could move forward efficiently at all times.

---

**Karishma Boodhoo**

[LinkedIn](www.linkedin.com/in/karishmaboodhoo)

I am a passionate developer who thrives on turning complex data into clean, usable interfaces. With a Computer Science background, I combine technical problem-solving with a keen eye for design to create seamless experiences that users would enjoy working with.
In our BitFlip team, while working on CloudSherpa for BBD, I acted as the UI/UX Engineer and focused on translating complex FinOps data into intuitive visual layouts. I drove the implementation of responsive, interactive features that prioritize clarity and usability.

---

**Cherise Heyl**

[LinkedIn](http://www.linkedin.com/in/cherise-heyl-5464211ba)

I am a results-driven software developer with experience across frontend, back-
end, and systems-level development. With a background in electronic engineering
and computer science, I bring strong analytical thinking and a solid understanding of
hardware–software integration to modern software design.
In our BitFlip team working on CloudSherpa for BBD, I am acting as our API & Systems Integration Engineer and co-tester, where I have been primarily focused on the Connector logic and endpoints associated with retrieving CloudMetrics from providers such as AWS, GCP and Azure.

---

**Flip Venter**

[LinkedIn](http://www.linkedin.com/in/flippie-venter-3a0356341)

I am a developer with a deep interest in the intersection of technical architecture and UI/UX design. My background in Information and Knowledge Systems has given me a strong foundation in how users interact with data. I’m not just looking to build a functional tool, but a reliable, high-performance solution that users genuinely enjoy engaging with.
In our BitFlip team, while working on CloudSherpa for BBD I acted as Frontend Application Developer and focused on implementing login and registration ui, as well dashboard state handling and grid layout and resizing logic.

---


**Role Allocations:**
* **Megan Norval** - *Team Lead & Core Backend Architect*
* **Gerard Jordaan** - *DevOps Architect, Full-Stack Contributor & Tester*
* **Karishma Boodhoo** - *Frontend UI/UX Engineer*
* **Cherise Heyl** - *API & Systems Integration Engineer, & Tester*
* **Flip Venter** - *Frontend Application Developer*

---
*Developed in partnership with BBD for the COS 301 Capstone Project (2026).*