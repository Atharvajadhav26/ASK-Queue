# Smart Queue System — Queue Engine Algorithm

## Overview

`QueueEngine` is the single source of truth for all queue operations. All queue-mutating operations **must** go through this service. The frontend is never the source of truth.

## 1. Token Generation (Join Queue)

```
1. Acquire PESSIMISTIC_WRITE lock on ServiceQueue
2. Validate: queue.status == OPEN
3. Validate: user has no active token in this queue
4. Validate: activeCount < maxCapacity
5. Atomic: queue.currentTokenSeq++
6. Generate tokenNumber = prefix + "-" + seq (e.g. "OPD-1001")
7. Calculate position = max(active positions) + 1
8. Save token with status=WAITING, position=newPosition
9. Record history: null → WAITING, action=JOIN
```

## 2. Next Token (Call Next Customer)

```
1. Acquire PESSIMISTIC_WRITE lock on queue
2. If currentServingToken exists and status=SERVING → auto-complete it
3. Fetch all WAITING tokens for queue
4. Sort by: priority DESC (EMERGENCY=3, VIP=2, NORMAL=1), position ASC
5. Select top token
6. Transition: WAITING → SERVING
7. Set calledTime, servingStartTime = now
8. Set queue.currentServingTokenId = token.id
9. Recalculate dense-rank positions for remaining
10. Record history: WAITING → SERVING, action=NEXT
```

**Priority Selection Logic:**
```
EMERGENCY tokens → served first regardless of position
VIP tokens → served before NORMAL, after EMERGENCY
NORMAL tokens → served in FIFO order
Within same priority → lowest queue position wins
```

## 3. Previous Token (Revert)

```
1. Acquire PESSIMISTIC_WRITE lock
2. Find last NEXT action in token_history for this queue
3. Revert current SERVING token → WAITING at position 1
4. Find most recently COMPLETED token → restore to SERVING
5. Recalculate positions
6. Record history: SERVING → WAITING (PREVIOUS), COMPLETED → SERVING (PREVIOUS_RESTORE)
```

## 4. Hold / Resume

**Hold (WAITING → HELD):**
```
1. Validate token.status == WAITING
2. Set holdStartTime = now
3. Set holdExpiryTime = now + holdTimeMinutes (from queue_settings)
4. Transition: WAITING → HELD
5. Recalculate positions (held tokens don't occupy a position)
```

**Resume (HELD → WAITING):**
```
1. Validate token.status == HELD
2. Place at END of queue (fair policy): position = maxPosition + 1
3. Clear holdStartTime, holdExpiryTime
4. Transition: HELD → WAITING
5. Recalculate positions
```

**Hold Expiry (Scheduler — runs every 30s):**
```
1. Find all HELD tokens where holdExpiryTime < now
2. For each expired token:
   - If behavior == "MOVE_TO_END": HELD → WAITING at end
   - If behavior == "EXPIRE": HELD → EXPIRED
3. Recalculate positions
```

## 5. Skip (Move Back in Queue)

```
1. Validate: token.status == WAITING, 1 ≤ positions ≤ 10
2. Find tokens behind this one in queue
3. Move the next N tokens forward (decrement their position by 1)
4. Increment target token's position by N
5. Increment token.skipCount
6. Recalculate dense-rank positions
```

## 6. Cancel / Leave

```
1. Validate: status not in (COMPLETED, CANCELLED, EXPIRED)
2. Transition: currentStatus → CANCELLED
3. Set cancelledTime, cancelledBy, cancelReason
4. If token was the serving token → clear queue.currentServingTokenId
5. Recalculate positions
```

## 7. Complete

```
1. Validate: token.status == SERVING
2. Transition: SERVING → COMPLETED
3. Set completedTime = now
4. Clear queue.currentServingTokenId
```

## 8. Dense-Rank Position Recalculation

After every queue-mutating operation:
```java
List<Token> waiting = findByQueueAndStatus(queue, WAITING)
    .orderByQueuePositionAsc();
int pos = 1;
for (Token t : waiting) {
    if (t.queuePosition != pos) {
        t.queuePosition = pos;
        save(t);
    }
    pos++;
}
```
This ensures contiguous 1-based positions with no gaps.

## 9. Estimated Wait Time

```
peopleAhead = count(WAITING tokens with position < myPosition)
estimatedWait = (peopleAhead + 1) × avgTimePerPersonMinutes
```

## 10. Concurrency Control

| Mechanism | Where | Purpose |
|-----------|-------|---------|
| `PESSIMISTIC_WRITE` | `findByIdWithLock()` on ServiceQueue | Serialize join/next/previous |
| `@Version` | Token entity `version` field | Detect concurrent token updates |
| `@Transactional` | All QueueEngine public methods | Atomic operations, auto-rollback |
