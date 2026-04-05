# HR Interview Questions

## Tell Me About Yourself
```
"I am a Java Backend Developer with 3 years
of experience. I started my career at HCLTech
where I worked for 2.5 years, transitioning
from testing to backend development.

Currently I am working independently on
production-grade backend systems. My most
significant project is Fraud Shield — a
real-time fraud detection system built with
Spring Boot Microservices, Kafka, Redis and
PostgreSQL that mirrors architecture used by
companies like Razorpay and PhonePe.

I am passionate about backend engineering,
distributed systems and fintech domain.
I am now looking for opportunities in product
companies where I can contribute to building
scalable, high-impact systems."
```

---

## Tell Me About Your Project
```
"I built Fraud Shield — a production-grade
real-time fraud detection system targeting
the fintech domain.

The system processes financial transactions
through 8 microservices. When a transaction
comes in through the API Gateway, it goes to
the Transaction Service which saves it to
PostgreSQL and publishes an event to Kafka.

The Fraud Detection Service consumes this
event and runs it through a Rules Engine
implementing Chain of Responsibility and
Strategy patterns. Four rules check for:
blacklisted entities, velocity fraud,
unusual amounts and location anomalies.

Results are published to Kafka as fraud alerts,
consumed by Alert Service for real-time
WebSocket notifications and Report Service
for analytics.

The entire infrastructure uses Spring Cloud —
Eureka for service discovery, Config Server
with Spring Cloud Bus for zero-downtime
config refresh, and Spring Cloud Gateway
for JWT validation and routing.

This project gave me hands-on experience with
Kafka, Redis, MongoDB, WebSockets, JWT security
and AWS deployment."
```

---

## What Was Your Biggest Challenge?
```
"The most challenging part was implementing
the Kafka type mapping between microservices.

When Transaction Service published
TransactionEvent, the class package path
was embedded in the Kafka message header.
Fraud Detection Service had a different
package structure and couldn't deserialize
the message, throwing ClassNotFoundException.

I solved it by configuring:
spring.json.use.type.headers: false
spring.json.value.default.type: OurClass

This tells the consumer to ignore the
type header and always deserialize to
our local class. This was a non-obvious
solution that required deep understanding
of Spring Kafka's deserialization mechanism.

This taught me the importance of having
a shared contract between microservices
and the trade-offs between tight and
loose coupling in distributed systems."
```

---

## Why Are You Looking for a Change?
```
"I have been working independently on
building production-grade systems to
strengthen my backend engineering skills.

Now I am ready to take my skills to a
product company where I can work on
real-world problems at scale, collaborate
with strong engineering teams, and grow
into a senior backend engineer role.

I am particularly interested in fintech
companies because my Fraud Shield project
has given me deep domain knowledge in
payment processing and fraud detection,
which are core problems companies like
yours are solving."
```

---

## Where Do You See Yourself in 5 Years?
```
"In 5 years I see myself as a Senior
Backend Engineer or Tech Lead, architecting
distributed systems that handle millions of
transactions per day.

My roadmap:
→ Near term: Deep expertise in AWS,
  Kubernetes and system design
→ Mid term: Technical leadership,
  mentoring junior developers
→ Long term: Principal Engineer or
  Engineering Manager

My Fraud Shield project has already
exposed me to senior-level architecture
decisions — polyglot persistence, event
driven design, distributed tracing.
I want to continue growing in this
direction."
```

---

## What Is Your Expected CTC?
```
"Based on my 3 years of experience,
the production-grade system I have built,
and my expertise in the fintech domain
with Spring Boot, Kafka, Redis and
microservices architecture, I am
expecting between 10 to 15 LPA.

However I am flexible and open to
discussion based on the role, growth
opportunities and overall package
including benefits and learning."
```

---

## Why Do You Want to Join Our Company?
```
For Fintech (Razorpay, CRED, PhonePe):
"Your company processes millions of
transactions daily using exactly the
technology stack I have built expertise
in — Spring Boot, Kafka, Redis and
microservices. My Fraud Shield project
directly solves problems your engineering
teams work on. I want to contribute to
building payment infrastructure at scale
and learn from world-class engineers."

For Enterprise (IBM, Capgemini):
"Your company works on large-scale
enterprise systems that require exactly
the backend expertise I have built.
I am excited about the opportunity to
work on distributed systems at enterprise
scale and contribute to mission-critical
infrastructure."
```

---

## What Are Your Strengths?
```
"Three key strengths:

1. Deep technical curiosity
   → I don't just use technologies
   → I understand WHY they work
   → This helps me make better
     architecture decisions

2. Problem solving under pressure
   → Fraud Shield had many technical
     challenges — Kafka deserialization,
     circular dependencies, timezone issues
   → I debugged systematically and
     always found the root cause

3. Building production-grade systems
   → Not just tutorial projects
   → Full documentation
   → Security considerations
   → Monitoring and observability
   → This is rare at my experience level"
```

---

## What Are Your Weaknesses?
```
"I sometimes spend too much time
perfecting technical solutions when
a simpler approach would work.

For example, while building the
Rules Engine for Fraud Shield, I
spent extra time making it perfectly
extensible with proper design patterns
when a simpler if-else might have
been faster to build initially.

I am learning to balance perfection
with pragmatism — delivering working
solutions first and refining later.
This is an important skill in
product company environments."
```

---

## Notice Period / Availability
```
"I am currently working independently
and can join immediately or within
7 days as per your requirement."
```

---

## Salary Negotiation Script
```
If offer is below expectation:

"Thank you for the offer. I am very
excited about this opportunity and
the team. Based on my research and
the production-grade systems I have
built, particularly Fraud Shield which
directly aligns with your tech stack,
I was expecting closer to X LPA.

Is there flexibility to get closer
to that number? I am confident I will
add significant value from day one."

If they say no flexibility:

"I understand. Could we discuss
other aspects of the package like
performance bonus, learning budget
or earlier review cycle that could
bridge the gap?"
```