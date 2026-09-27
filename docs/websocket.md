# Smart Queue System — WebSocket Architecture

## Overview

Real-time communication uses **STOMP protocol** over **SockJS** for instant queue updates.

## Server Configuration

```
Endpoint:       /ws (with SockJS fallback)
Broker:         In-memory simple broker
Topic prefix:   /topic, /queue
App prefix:     /app
User prefix:    /user
```

## Topics

| Topic | Audience | Triggered By |
|-------|----------|-------------|
| `/topic/queue/{queueId}` | All users viewing queue | Token join, next, hold, resume, skip, cancel, complete, queue open/close |
| `/topic/admin/queue/{queueId}` | Admin managing queue | Admin-specific updates |
| `/user/{userId}/queue/notifications` | Individual user | Personal notifications (turn approaching, your turn, etc.) |

## Event Payload Format

```json
{
  "action": "NEXT_TOKEN_CALLED",
  "queueId": "Q-8F42A1",
  "data": { "tokenNumber": "Q-1002", "status": "SERVING" },
  "timestamp": 1773091455000
}
```

## Event Types

| Event | Description |
|-------|-------------|
| `TOKEN_JOINED` | Customer joined queue |
| `NEXT_TOKEN_CALLED` | Next token called |
| `PREVIOUS_TOKEN_CALLED` | Previous operation |
| `TOKEN_HELD` | Token held by customer |
| `TOKEN_HELD_BY_ADMIN` | Token held by admin |
| `TOKEN_RESUMED` | Token resumed |
| `TOKEN_SKIPPED` | Token skipped positions |
| `TOKEN_CANCELLED` | Token cancelled |
| `TOKEN_COMPLETED` | Service completed |
| `TOKEN_PRIORITY_UPDATED` | Priority changed |
| `QUEUE_OPENED` | Queue opened |
| `QUEUE_CLOSED` | Queue closed |

## Client Connection (React)

```javascript
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client/dist/sockjs';

const client = new Client({
  webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
  reconnectDelay: 5000,
  heartbeatIncoming: 4000,
  heartbeatOutgoing: 4000,
  onConnect: () => {
    client.subscribe(`/topic/queue/${queueId}`, (msg) => {
      const data = JSON.parse(msg.body);
      // Handle queue update
    });
  }
});
client.activate();
```

## Reconnection Strategy

- **Auto-reconnect:** 5-second delay between attempts (`reconnectDelay: 5000`)
- **Heartbeats:** 4-second ping/pong to detect broken connections
- **SockJS fallback:** Automatic fallback to HTTP long-polling if WebSocket is blocked
