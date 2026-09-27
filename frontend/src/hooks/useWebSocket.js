import { useEffect, useRef, useCallback } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client/dist/sockjs';

const WS_URL = import.meta.env.VITE_WS_URL || 'http://localhost:8080/ws';

/**
 * Custom hook for WebSocket connection to receive real-time queue updates.
 * 
 * @param {string} queueId - Queue ID to subscribe to
 * @param {function} onUpdate - Callback when queue update is received
 * @param {string} userId - Optional user ID for personal notifications
 */
export function useWebSocket(queueId, onUpdate, userId) {
  const clientRef = useRef(null);
  const onUpdateRef = useRef(onUpdate);
  onUpdateRef.current = onUpdate;

  useEffect(() => {
    if (!queueId) return;

    const client = new Client({
      webSocketFactory: () => new SockJS(WS_URL),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        console.log('WebSocket connected');

        // Subscribe to queue updates
        client.subscribe(`/topic/queue/${queueId}`, (message) => {
          if (message.body) {
            try {
              const data = JSON.parse(message.body);
              onUpdateRef.current(data);
            } catch (e) {
              console.error('Failed to parse WebSocket message:', e);
            }
          }
        });

        // Subscribe to personal notifications if userId provided
        if (userId) {
          client.subscribe(`/topic/user/${userId}/notifications`, (message) => {
            if (message.body) {
              try {
                const data = JSON.parse(message.body);
                onUpdateRef.current({ action: 'NOTIFICATION', data });
              } catch (e) {
                console.error('Failed to parse notification:', e);
              }
            }
          });
        }
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame.headers?.message);
      },
      onDisconnect: () => {
        console.log('WebSocket disconnected');
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      if (clientRef.current) {
        clientRef.current.deactivate();
      }
    };
  }, [queueId, userId]);

  return clientRef;
}

/**
 * Hook for admin WebSocket updates.
 */
export function useAdminWebSocket(queueId, onUpdate) {
  const clientRef = useRef(null);
  const onUpdateRef = useRef(onUpdate);
  onUpdateRef.current = onUpdate;

  useEffect(() => {
    if (!queueId) return;

    const client = new Client({
      webSocketFactory: () => new SockJS(WS_URL),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        console.log('Admin WebSocket connected');

        client.subscribe(`/topic/queue/${queueId}`, (message) => {
          if (message.body) {
            try {
              onUpdateRef.current(JSON.parse(message.body));
            } catch (e) {
              console.error('Failed to parse message:', e);
            }
          }
        });

        client.subscribe(`/topic/admin/queue/${queueId}`, (message) => {
          if (message.body) {
            try {
              onUpdateRef.current(JSON.parse(message.body));
            } catch (e) {
              console.error('Failed to parse admin message:', e);
            }
          }
        });
      },
      onStompError: (frame) => {
        console.error('Admin STOMP error:', frame.headers?.message);
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      if (clientRef.current) {
        clientRef.current.deactivate();
      }
    };
  }, [queueId]);

  return clientRef;
}
