# The Complete Curriculum — Java Backend, 3-5 YOE Depth
**Created:** June 25, 2026
**Goal:** 16-25 LPA fintech/BFSI. Survive 4-6 round loops, not just Round 1 screening.

## THE GOVERNING PRINCIPLE (from 2026 market research)
"At 3 years you know the APIs. At 5 years you know the CONTRACTS —
what each API guarantees, what it does NOT, and how systems fail when
you misunderstand the difference."

Every topic below must be learned at CONTRACT level, not API level.
For each: (1) what it does, (2) what it does NOT guarantee, (3) how it
fails in production, (4) a real example from my own code/Greytip.

Depth over breadth. 30 topics I can discuss with specifics + tradeoffs
+ real example beats 200 memorized definitions.

---

## MODULE 0 — FOUNDATIONS (the syntax muscle — do FIRST)

WHY THIS EXISTS: Jun 25 revealed syntax wobbles — Map vs map, char[] vs
Char[], charArr[i] vs charArr(i), constructor with return type, getOrDefault
arg placement. These aren't concept gaps — the hands haven't learned Java
yet. Concepts are ahead of syntax. Only fix: write code BY HAND, daily,
until automatic. NO copy-paste. NO IDE autocomplete during practice.

### 0.1 Syntax that keeps breaking (drill until automatic)
- TYPE is capital, variable is lowercase: `Map<Character,Integer> map`
- Wrapper types capital: Character, Integer, Long, Boolean, Double
- Primitive types lowercase: char, int, long, boolean, double
- Arrays use [ ]: `charArr[i]` NEVER charArr(i)
- Methods use ( ): `s.length()`, `s.toCharArray()` — parens required
- Fields no parens: `arr.length` (no parens — it's a field)
- Constructors have NO return type: `public BankAccount(...)` not `public void`
- getOrDefault: `map.getOrDefault(key, 0) + 1` — +1 OUTSIDE, on the value
- new keyword: `new HashMap<>()`, `new int[]{1,2}`
- String → char array: `char[] c = s.toCharArray();`
- char array → String: `new String(c)`

### 0.2 The skeletons to write cold 20x each (muscle memory)
```java
// for loop
for (int i = 0; i < arr.length; i++) { }

// enhanced for
for (String word : words) { }
for (char c : word.toCharArray()) { }

// HashMap declare + populate
Map<Character, Integer> map = new HashMap<>();
map.put(key, map.getOrDefault(key, 0) + 1);

// HashSet
Set<Integer> seen = new HashSet<>();
if (seen.contains(x)) return true;
seen.add(x);

// iterate map values
for (int v : map.values()) { }

// class skeleton
public class Foo {
    private final int x;
    public Foo(int x) { this.x = x; }
    public int getX() { return x; }
}

// method signature
public static int[] twoSum(int[] nums, int target) { }
```

### 0.3 Language basics (the "shaken at basics" list)
- Primitives vs objects, wrapper classes, autoboxing/unboxing
- Integer cache -128..127 (== gotcha)
- public/private/protected/default access modifiers
- static vs instance (method + variable)
- this keyword, constructor chaining (this(), super())
- Long vs long for entity IDs (wrapper nullable, primitive defaults 0)
- void vs return type, method parameters vs arguments
- if/else, switch, while, do-while, for, break, continue
- Array declaration: int[] a = new int[5]; vs int[] a = {1,2,3};
- 2D arrays, array length (field not method)
- String methods: length(), charAt(), substring(), equals(), split(),
  toCharArray(), toLowerCase(), contains(), indexOf()
- == vs .equals() — references vs value
- null, NullPointerException — when it happens

### 0.4 JVM/JDK/JRE basics (asked at EVERY level)
- JDK = JRE + dev tools (javac compiler, debugger)
- JRE = JVM + core libraries (run, not develop)
- JVM = executes bytecode, platform-specific, gives platform independence
- Compilation: .java → javac → .class (bytecode) → JVM runs
- public static void main(String[] args) — entry point, why each keyword

### 0.5 Daily foundation drill (15 min, EVERY morning before concepts)
Write ONE of these cold, by hand, no peeking:
- A class with private final fields + constructor + getters
- A for loop that builds a HashMap frequency count
- A method that takes int[] and returns int[]
- A two-pointer while loop on an array
- Iterate a map and check values
Rotate through them. The goal: syntax becomes invisible, brain focuses
on logic. Until syntax is automatic, every interview coding question
costs double brainpower.

---

## MODULE 1 — CORE JAVA (contract-level)

### 1.1 OOP & Object contracts
- 4 pillars WITH tradeoffs ("prefer composition when you don't control
  base class; inheritance when relationship is genuinely hierarchical")
- equals/hashCode contract — what breaks if you override one not both
- == vs equals — Integer cache -128..127 (the gotcha)
- Comparable vs Comparator
- Overloading (compile-time type) vs Overriding (runtime type) — THE gotcha
- Covariant return types
- Static vs instance initializer blocks — execution order

### 1.2 Immutability & finals  [LOCKED Jun 17]
- Immutable class 5 rules + defensive copy for mutable fields
- final / finally / finalize
- Why String is immutable (security, caching, thread-safety)

### 1.3 Exceptions (contract-level)
- Checked vs unchecked — WHEN to use each
- try-with-resources, custom exception hierarchy
- @Transactional rollback: runtime rolls back, checked does NOT by default
- Exception in finally block swallows the original (gotcha)

### 1.4 Generics
- Wildcards, bounded types, PECS (Producer Extends Consumer Super)
- Type erasure — what's lost at runtime

### 1.5 Strings
- String pool, intern(), StringBuilder vs StringBuffer
- Immutability consequences

---

## MODULE 2 — COLLECTIONS (contract-level)

- ArrayList vs LinkedList — internal structure, when each wins
- HashMap internals: buckets, hashing, collision, Java 8 tree-ification at 8+
- HashMap vs Hashtable vs ConcurrentHashMap — locking models
- Modifying a HashMap key after insertion (the gotcha — lost entry)
- TreeMap (sorted), LinkedHashMap (insertion order), PriorityQueue (heap)
- fail-fast vs fail-safe iterators (ConcurrentModificationException)
- ArrayList vs Vector vs CopyOnWriteArrayList

---

## MODULE 3 — JAVA 8 → 21 (heavily tested in 2026)

### 3.1 Java 8
- Streams: filter/map/collect/groupingBy/reduce/flatMap/partitioningBy
- Stream single-use rule (IllegalStateException) [LOCKED Jun 17]
- Lazy evaluation — intermediate ops don't run until terminal op
- Functional interfaces: Predicate/Function/Consumer/Supplier (the core 4)
- Optional: orElse vs orElseGet vs orElseThrow
- Lambda vs anonymous class (this scope difference)
- Method references, default/static interface methods

### 3.2 Java 17  [LOCKED Jun 17]
- Records, Sealed classes, Text blocks
- Pattern matching instanceof, Switch expressions

### 3.3 Java 21 (the 2026 differentiator)
- Virtual threads (Project Loom) — lightweight JVM threads, NOT OS threads
- "The question isn't whether you've used them in prod (most haven't).
  It's whether you understand the model, limitations, and how it relates
  to reactive (Project Reactor/RxJava)"
- Use case: high-concurrency I/O-bound (perfect for wallet reconciliation)

---

## MODULE 4 — MULTITHREADING (where seniority shows most)

- Thread lifecycle, Runnable vs Callable vs Thread
- synchronized (method vs block), volatile (VISIBILITY not half-state)
- wait/notify/notifyAll, producer-consumer with BlockingQueue
- ExecutorService, thread pools, Future
- Deadlock: what causes it, how to prevent (lock ordering)
- ReentrantLock vs synchronized
- AtomicInteger (CAS), CountDownLatch, Semaphore
- Thread-safe Singleton (DCL + volatile) — MUST write cold
- IMPLEMENT: thread-safe counter 3 ways, producer-consumer

---

## MODULE 5 — JVM (5 YOE expectation)

- JVM vs JRE vs JDK
- Heap (young/old gen), Stack (per thread), Metaspace
- Garbage collection: GC roots, stop-the-world, generational
- Classloading: bootstrap/extension/application
- OutOfMemoryError vs StackOverflowError — causes
- JIT compilation basics

---

## MODULE 6 — SPRING BOOT (contract-level)

- Bean lifecycle, @PostConstruct/@PreDestroy
- Bean scopes (singleton/prototype/request/session) [LOCKED]
- @Component/@Service/@Repository (Repository = exception translation)
- @SpringBootApplication = Config + AutoConfig + ComponentScan
- DI: constructor vs field vs setter (constructor wins — final, testable, no half-state)
- @Primary vs @Qualifier
- @Transactional DEEP: propagation (REQUIRED/REQUIRES_NEW), isolation,
  readOnly (Hibernate optimization NOT isolation), self-invocation gotcha
  (@Transactional on private method does NOTHING — proxy can't intercept)
- AOP: @Aspect/@Before/@After/@Around — IMPLEMENT a logging aspect
- @Async, Filter vs Interceptor vs AOP
- Actuator: health, metrics, custom endpoints
- @RestControllerAdvice + @ExceptionHandler global handling

---

## MODULE 7 — JPA / HIBERNATE (contract-level)

- Entity lifecycle: transient → persistent → detached → removed
- Lazy vs Eager (defaults: OneToMany=LAZY, ManyToOne=EAGER) [LOCKED]
- LazyInitializationException — lazy access after session close (the gotcha)
- N+1 problem + JOIN FETCH / @EntityGraph fix [LOCKED]
- Optimistic (@Version) vs Pessimistic (SELECT FOR UPDATE) locking
- L1 (session) vs L2 (shared) cache
- @OneToMany/@ManyToOne/@ManyToMany — FK placement, mappedBy
- JPQL vs native queries
- save vs saveAndFlush, persist vs merge

---

## MODULE 8 — SQL / DATABASE (contract-level)

- All JOINs (INNER/LEFT/RIGHT/FULL) — direction matters
- GROUP BY + HAVING (WHERE before grouping, HAVING after)
- Window functions: ROW_NUMBER/RANK/DENSE_RANK/LAG/LEAD
- 2nd highest salary (3 patterns), top-N-per-group
- Self-join (more than N reports pattern)
- Indexes: B-tree, composite, covering, partial
- EXPLAIN ANALYZE: Index Scan (good) vs Seq Scan (bad)
- DDL vs DML [LOCKED], ACID, isolation levels
- Stored procedures vs functions
- Connection pooling, prepared statements, batch processing
- Double-entry ledger balance query (my wallet project)

---

## MODULE 9 — MICROSERVICES (contract-level)

- Monolith vs Microservices — real tradeoffs, when monolith is right
- API Gateway, Service Discovery (Eureka), Config Server
- Circuit Breaker (CLOSED/OPEN/HALF-OPEN) — Resilience4j
- Saga (choreography vs orchestration), Outbox pattern
- CQRS, Event Sourcing, Strangler Fig, Bulkhead, Sidecar
- Sync (REST/Feign) vs Async (Kafka/ActiveMQ) communication
- Distributed tracing, idempotency, eventual consistency
- Service mesh basics

---

## MODULE 10 — SYSTEM DESIGN (Round 2-3 killer)

THE FRAMEWORK (what graders want):
1. ASK clarifying questions FIRST (scale, consistency, failure tolerance)
   — "drawing boxes without asking = scoring yourself down"
2. State approach in one sentence
3. Draw components, justify each
4. Make tradeoffs EXPLICIT
5. Acknowledge constraints & failure modes

THE 4 CANONICAL PROMPTS (from 2026 research):
- Design high-throughput order processing (10,000 req/sec)
- Design notification service (email/SMS/push) reliable at scale
- Design rate-limiting layer for public API
- Design caching strategy for read-heavy service

Building blocks: load balancing, horizontal scaling, read replicas,
sharding, cache-aside/write-through, message queues for spike absorption,
CAP theorem, rate limiting algorithms (token bucket/sliding window)

---

## MODULE 11 — DSA (screening rounds)

DONE: Two Sum, Contains Duplicate, Missing Number, Best Time Stock,
Move Zeroes, Second Largest, Reverse String, Palindrome,
First Non-Repeating, Valid Anagram, Pair With Sum

REMAINING:
- LinkedList: reverse, detect cycle (Floyd's), merge sorted
- Stack: valid parentheses, min stack
- Binary search (O(log N) — halves each step)
- Sliding window: longest substring without repeat
- Recursion: factorial, fibonacci, basic backtracking
- Trees (nice-to-have): height, BFS, DFS, level-order

SIGNAL→PATTERN MAP (the meta-skill):
"seen before?" → HashSet
"count frequency" → HashMap getOrDefault
"two sum to X" → HashMap complement
"sorted + pair" → two pointer
"in-place" → two pointer
"window of size K" → sliding window
"running max init" → MIN_VALUE never 0
"all combinations" → recursion/backtracking

---

## MODULE 12 — GREYTIP DEEP-DIVE (the "tell me about your work" rounds)

Source files: Pengo_Diagrams, Workflow_Documentation,
End_to_End_Flow_Payroll_Processing, Pengo_Codebase_Explain

### Architecture answer
Pengo = comprehensive HR/Payroll system (Spring Boot, domain-driven
modules: payroll, loan, FBP, reimbursement, forms, user, workflow).
Integrations: AWS S3 (files), Kafka (async events), SMTP (mail).
Layered: Controller → Service → Repository → Model, DTOs between layers.

### Payroll processing flow (memorize the call chain)
Admin → PayrollController POST /payroll/process → PayrollService.processPayroll()
→ fetch eligible employees → per employee: calc gross/deductions/tax
→ create Payroll + PayrollItem → save via PayrollRepository
→ PayslipGenerator (PDF) → S3 upload → MailService → KafkaProducer event

### CI/CD migration answer (from Workflow doc)
- JIB Gradle plugin (no Docker daemon needed) builds images
- Base image pengo-base:1.0 in Google Artifact Registry
- GitHub Actions: base-image.yaml + cicd.yaml workflows
- Triggers: push to master/dev/nexora-qa, file changes, manual dispatch
- Sonar quality gate, deploy to Cloudstack/GCP

### LOP / LOP-reversal / LOP-retrospective (the features)
- LOP = Loss of Pay. Per-day rate = gross / divisor (calendar days or 30)
- LOP deduction = per-day rate × LOP days. Cascades to PF/ESI/TDS
- LOP Reversal = positive correction using PAST month's rate
- LOP Retrospective (built) = missed past-month LOP applied current month
  Hard parts: salary revision between months, carry-forward when retro
  exceeds salary, ordering of multiple retros, 12-month statutory limit

### The HDFC bug story (primary "difficult bug" answer)
Generic "invalid file format" rejection. Byte-level diff vs accepted file.
Root cause: Windows \r\n vs required Unix \n line endings. Fix: enforce \n
in file writer. Lesson: when error is generic, diff at the bytes.

### Questions they'll ask (prep all):
- "How did the migration happen, from what to what?" (legacy → Spring Boot Java 8)
- "What challenges did you face and how did you resolve them?"
- "How did service X communicate with service Y?" (Kafka events)
- "Walk me through a feature you built end to end" (LOP retrospective)
- "What was the hardest bug?" (HDFC line-endings)
- "How was it deployed?" (JIB + GitHub Actions + GCP)

---

## HOW EACH TOPIC GETS LEARNED (the method)

**MODULE 0 RUNS ALONGSIDE EVERYTHING — every single day starts with a
15-min hand-written syntax drill until syntax is automatic. This is the
foundation the rest sits on. Skipping it = continuing to burn double
brainpower on syntax during real interview coding.**

For every topic in Modules 1-12, 4 layers:
1. THEORY — contract level (guarantees + non-guarantees + failure modes)
2. PRACTICAL — write it cold in a blank file, no peeking, BY HAND
3. PROJECT — implement it in the wallet platform where applicable
4. ARTICULATE — one-liner for the interview, hostile-probe rehearsed

Rotation: each week owns 1-2 modules deeply. Daily drills pull from
ALL covered modules (spaced repetition). Foundation drill + DSA every
day. Mock every weekend.

THE ORDER:
Week 1 = Module 0 (foundations) + Module 1 (core Java) — get the base solid
Week 2 = Module 2 (collections) + Module 3 (Java 8-21)
Week 3 = Module 4 (multithreading) + Module 5 (JVM)
Week 4 = Module 6 (Spring) + Module 7 (JPA)
Week 5 = Module 8 (SQL) + Module 9 (microservices)
Week 6 = Module 10 (system design) + Module 12 (Greytip deep-dive)
Throughout: Module 11 (DSA) daily, Module 0 drill daily, project build,
applications daily, GitHub contributions from week 2.

---

## GITHUB CONTRIBUTION TRACK (real code, not docs)

Targets: baeldung/tutorials (easiest), resilience4j (relevant),
spring-projects/spring-boot (prestigious)
Process: find good-first-issue → read codebase → write fix + test → PR
Resume line when merged: "Contributed bug fix to [repo]"
Bonus: writing business logic + handling real bugs in someone else's
codebase = exactly the "can you own a service" signal interviewers want
