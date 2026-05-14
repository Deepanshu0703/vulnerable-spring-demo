---
description: Detect and fix runtime vulnerabilities in vulnerable-spring-demo (SQL Injection, Command Injection, Path Traversal, SSRF, XXE, XSS)
---

# Vulnerability Detection & Fix Workflow

## Phase 1 — Build & Start the Application

// turbo
1. Build the project:
```
cd /Users/deepanshukumar/Desktop/vulnerable-spring-demo && ./gradlew build -x test
```

// turbo
2. Start the application:
```
cd /Users/deepanshukumar/Desktop/vulnerable-spring-demo && java -jar build/libs/vulnerable-spring-demo-0.0.1-SNAPSHOT.jar &
```

3. Confirm the server is up:
```
curl -s http://localhost:8080/api/users/list | head -c 200
```

---

## Phase 2 — Read and Understand the Codebase

Read every controller file under `src/main/java/com/demo/controller/` and the resource files under `src/main/resources/`.

For each controller, reason about:
- What inputs does each endpoint accept from the caller?
- How is that input used internally — in queries, system calls, file operations, network calls, or output?
- Could a malicious caller supply input that causes the application to behave in an unintended or dangerous way?

Identify all insecure patterns present in the code. Do not rely on any hints — read the logic and reason about it from first principles.

---

## Phase 3 — Test: Probe Each Endpoint with Crafted Inputs

For each suspicious endpoint you identified, craft HTTP requests that would expose the insecure behavior at runtime and run them against the live application. Confirm whether the application responds in a way that demonstrates the vulnerability is exploitable.

Use `curl` to send the requests. Examine the responses to determine whether the input was processed unsafely.

---

## Phase 4 — Fix Each Vulnerability

For each vulnerability you confirmed:

1. Decide on the correct, industry-standard remediation for that class of issue.
2. Apply the fix directly to the source file using the `edit` or `multi_edit` tool.
3. Make the minimal change required — do not refactor unrelated code.

Apply fixes across all affected controller files.

---

## Phase 5 — Rebuild and Re-verify

// turbo
1. Stop the running server and rebuild:
```
pkill -f vulnerable-spring-demo && cd /Users/deepanshukumar/Desktop/vulnerable-spring-demo && ./gradlew build -x test
```

2. Restart with the fixed build:
```
cd /Users/deepanshukumar/Desktop/vulnerable-spring-demo && java -jar build/libs/vulnerable-spring-demo-0.0.1-SNAPSHOT.jar &
```

3. Re-run the same crafted inputs from Phase 3. Confirm that the application now handles them safely — the dangerous behavior should no longer be reproducible.
