# Fintech Wallet Platform — Master Plan
**Owner:** Ankur Salunke
**Last updated:** June 17, 2026
**Status:** Active job search + project rebuild

---

## CURRENT STATE (June 17, 2026)

### Job Search
- Wissen Technology: cancelled (no longer pursuing)
- Two other companies: interviewed, failed on immutable class + DSA
- Interviews failed on: immutable class (added setters), firstNonRepeating (syntax), stream reuse (IllegalStateException), functional interface names, lazy vs eager, N+1 problem
- Current pipeline: EMPTY — needs immediate refill
- Runway: ~6 weeks (₹70K remaining as of June 9)
- Target: 16-25 LPA fintech/BFSI backend role

### Resume status
- ATS 1-page version: Ankur_Salunke_Final.pdf (latest)
- Reference format 2-page version: Ankur_Salunke_9136920189_v2.pdf
- AI-Assisted Development added to skills
- Gap addressed in summary ("currently deepening distributed systems expertise")
- Both versions uploaded to Naukri + LinkedIn

### Project state (wallet platform)
**Built (needs refresh — been weeks since touched):**
- user-service Spring Boot 3.3, Java 21
- User entity, Account, Transaction, LedgerEntry entities + 4 enums
- Flyway V1 + V2 migrations
- GlobalExceptionHandler, custom exceptions
- UserService + WalletService with double-entry ledger
- UserController + WalletController
- BCrypt password hashing

**NOT built yet (remaining features):**
- Spring Security + JWT (in progress, filter partially written)
- Kafka outbox + notification-service
- Redis cache-aside on balance reads
- Resilience4j circuit breaker
- Testcontainers integration tests
- Docker Compose multi-service stack
- GitHub Actions CI pipeline

---

## THE PLAN (3 tracks, daily)

### Track 1 — Project (3 hrs/day)
Ship features in order. Each feature = resume skill moves from "In Progress" to "Built."

```
Week 1-2:  Spring Security + JWT → ship login endpoint
Week 3-4:  Kafka outbox + notification service
Week 5-6:  Redis cache-aside on balance reads
Week 7-8:  Resilience4j + Docker Compose + Testcontainers
```

### Track 2 — Interview gap-fixing (1.5 hrs/day)
Morning drill: 10 cold questions
Evening drill: 10 cold questions
DSA: 1 problem/day (never skip)

**Current locked topics:**
- HashMap internals, ConcurrentHashMap, ArrayList/Vector
- @Primary/@Qualifier, @RestController vs @Controller
- Singleton DCL, ACID, isolation levels
- JWT 4-component flow, Spring Security filter chain
- Microservices patterns (API Gateway, Circuit Breaker, Saga, Outbox, Eureka)
- Streams (filter/map/collect/groupingBy/reduce/flatMap)
- SQL balance query, 2nd highest, GROUP BY/HAVING, self-join
- Immutable class (NEWLY LOCKED)
- First non-repeating character (NEWLY LOCKED)
- Java 17 features (Records, Sealed Classes, Text Blocks)

**Active gaps to keep drilling:**
- Lazy vs Eager loading (@OneToMany default = LAZY)
- N+1 problem + JOIN FETCH fix
- volatile (visibility not half-state — this keeps coming back wrong)
- Functional interfaces (Predicate/Function/Consumer/Supplier — NOT Observer/Transformer)
- @Transactional(readOnly=true) = Hibernate optimization, NOT isolation
- Bean scopes (Singleton/Prototype/Request/Session)
- Thread-safe Singleton (DCL with volatile — must write cold)
- Immutable class 5 rules cold
- Inner class, anonymous class, lambda differences

**DSA remaining:**
- LinkedList: reverse, detect cycle
- Stack: valid parentheses, min stack
- Binary search implementation
- Recursion basics
- Trees: height, BFS, DFS (nice to have)

### Track 3 — Applications (1 hr/day)
10 targeted applications daily. No spray-and-pray.

**Primary targets:**
Razorpay, PhonePe, Juspay, Setu, M2P, Decentro, Perfios, Signzy,
Jupiter, Slice, BharatPe, CRED, Cashfree, Open Financial

**BFSI tech:**
HDFC Bank Tech, ICICI Lombard Tech, Axis Bank Tech, Mphasis,
Intellect Design Arena, Nucleus Software, Newgen, FIS Global

**Service companies:**
LTIMindtree, Hexaware, Persistent, Coforge, Birlasoft

**Rules:**
- Apply within 24 hours of posting
- Every application → 1 LinkedIn DM to an engineer there asking for referral
- Update Naukri profile daily (keeps you visible in recruiter searches)
- Naukri/LinkedIn headline: "Java Backend Engineer | Spring Boot | Microservices | Kafka | AWS | Fintech/BFSI"

### Track 4 — GitHub contributions (30 min/day, starts Week 2)
Real code contributions, not documentation.

**Target repos:**
- baeldung/tutorials (easiest entry, quick merge, Java + Spring)
- resilience4j/resilience4j (directly relevant to wallet project)
- spring-projects/spring-boot (harder, prestigious)

**Process:**
Week 1: Find a good-first-issue (label:good-first-issue language:Java)
Week 2: Read codebase, understand the issue, set up local build
Week 3: Write fix, submit PR, respond to review
Week 4+: Merged PR → add to resume under projects

---

## DAILY SCHEDULE

```
7:00   Wake. Phone across room.
7:30   Morning drill — 10 cold questions (30 min)
8:00   PROJECT BLOCK — 3 hours
11:00  Break (15 min)
11:15  Concepts + gap-fixing — 90 min
12:45  Lunch
1:45   Applications — 10 targeted + referral DMs (60 min)
2:45   DSA — 1-2 problems cold (45 min)
3:30   OSS contribution (30 min, from Week 2)
4:00   Project overflow or concept drill
6:00   Gym with Shubham
8:00   Dinner
9:00   Evening drill — 10 questions (30 min)
9:30   Wind down
10:30  Sleep
```

---

## INTERVIEW FAILURE LOG (lessons locked)

| Interview | Gap Found | Fix |
|---|---|---|
| Clover (Jun 17) | Immutable class: added setters | No setters ever. Final fields + constructor only |
| Clover (Jun 17) | firstNonRepeating: syntax bugs | Two-pass HashMap, iterate ORIGINAL array in pass 2 |
| Clover (Jun 17) | Stream reuse | Streams are single-use. Terminal op closes the stream |
| Clover (Jun 17) | Functional interfaces | Predicate/Function/Consumer/Supplier. NOT Observer/Transformer |
| Clover (Jun 17) | Binary search complexity | O(log N) not O(N). Each step halves search space |
| Practice (May) | @Primary/@Qualifier | Primary = default. Qualifier = pick specific by name |
| Practice (May) | groupingBy | employees.stream().collect(Collectors.groupingBy(Employee::getDesignation)) |
| Practice (May) | Circuit Breaker states | CLOSED → OPEN → HALF-OPEN |

---

## CONTRACTS (non-negotiable)

1. No more planning conversations — plan exists, execute it
2. Morning drill before anything else, every day
3. 10 applications daily with URLs — "trust me" doesn't count
4. Emergencies happen — resume next morning without guilt, without redesign
5. Spirals get named: Germany/robotics/AI product/revolutionary startup = call it out, park it
6. Sleep by 10:30 — rested brain retains 30% better
