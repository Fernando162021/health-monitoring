# Health Monitoring API - Cloud Architecture

## Deploying to AWS

Simple guide for deploying the Health Monitoring API to the cloud using Amazon Web Services.

---

## What We're Deploying

A Spring Boot application that:
- Receives vital signs from medical devices
- Stores data in a database
- Sends alerts when values are abnormal
- Provides reports and CSV exports

---

## Cloud Architecture (Simplified)

### The Three Main Parts

**1. Application (Spring Boot API)**
- AWS ECS (container service)
- 2 copies of our app running
- If one crashes, the other keeps working

**2. Database (PostgreSQL)**
- AWS RDS (managed database)
- Stores all our data (users, devices, vitals, alerts)
- Automatic backups, no maintenance needed

**3. Load Balancer**
- AWS ALB (application load balancer)
- Splits traffic between the 2 app copies
- Better performance and reliability

---

## How It Works

```
User/Device Request
        ↓
   Load Balancer (splits traffic)
        ↓
   App Copy 1  or  App Copy 2
        ↓
    Database (shared)
```

---

## AWS Services Used

### 1. ECS Fargate (Run the App)

**What it does:** Runs our Spring Boot app in containers

**Settings:**
- 2 containers always running
- 1 CPU, 2 GB RAM each
- Scales to 4 containers if busy

**Why it's good:**
- No servers to manage
- Auto-scales with traffic
- Easy to update

---

### 2. RDS PostgreSQL (Database)

**What it does:** Stores all our data

**Settings:**
- PostgreSQL 15
- 2 CPUs, 4 GB RAM
- 50 GB storage (grows automatically)
- Daily backups (kept 7 days)

**Why it's good:**
- AWS handles maintenance
- Automatic backups
- Can't lose data

---

### 3. Application Load Balancer (Traffic Manager)

**What it does:** Sends requests to healthy app containers

**Why it's good:**
- Distributes load evenly
- Only sends traffic to working containers
- Handles HTTPS

**Cost:** $25/month

---

### 4. CloudWatch (Monitoring)

**What it does:** Shows logs and alerts when something breaks

**Why it's good:**
- See errors in real-time
- Get notified of issues
- Track performance

**Cost:** $5/month

---

## Database Tables

Simple structure:

1. **users** → Login accounts
2. **device** → Registered medical devices
3. **vital** → Heart rate, oxygen, temperature readings
4. **alert** → Warnings when vitals are abnormal
5. **threshold** → What's "normal" for each vital sign

---

## Deployment Flow

1. Push code to GitHub
2. AWS builds a Docker image
3. Updates containers one by one
4. Zero downtime (always available)

---

## High Availability

**What if something fails?**

- **App crashes:** Load balancer uses the other copy, starts a new one
- **Database fails:** Automatic switch to backup (1-2 minutes)
- **Whole datacenter down:** Can setup backup in another location

**Uptime:** 99.9% guaranteed

---

## Scaling

**Handles automatically:**
- 100-500 requests/second normally
- Up to 1000+ requests/second when busy
- Thousands of devices
- Millions of records

**If we need more:** Just increase container count or database size

---

## Security

**Protected by:**
- Private network (database not accessible from internet)
- Passwords stored securely (not in code)
- Encrypted data
- HTTPS only

---

## Why This Setup?

✅ **Simple:** Managed services, no server maintenance
✅ **Reliable:** Multiple copies, auto-failover
✅ **Scalable:** Handles growth automatically
✅ **Secure:** Isolated network, encryption
✅ **Affordable:** ~$200/month for production

---

## Development vs Production

**Development** (~$50/month)
- 1 small container
- Small database
- For testing

**Production** (~$250/month)
- 2-4 containers
- Medium database
- Always running

---

## Setup Time

- First time: 4-6 hours
- After setup: Deploy updates in 5 minutes
- No server management needed

---

## Summary

**Cloud Provider:** AWS
**Main Services:** ECS (app) + RDS (database) + ALB (load balancer)
**Complexity:** Low (all managed services)
**Maintenance:** Minimal (AWS handles infrastructure)
**Ready for production:** Yes

