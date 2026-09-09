package network;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import network.inetwork.ISession;

public class SessionManager {

    private static SessionManager instance;
    private final List<ISession> sessions = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, ISession> accountSessions = new ConcurrentHashMap<>();

    public static SessionManager gI() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void putSession(ISession session) {
        this.sessions.add(session);
    }

    public synchronized void bindAccount(String account, ISession session) {
        ISession old = accountSessions.get(account);
        if (old != null && old.isConnected()) {
            old.disconnect();
        }
        accountSessions.put(account, session);
        if (!sessions.contains(session)) {
            sessions.add(session);
        }
    }

    public void removeSession(ISession session) {
        this.sessions.remove(session);
        accountSessions.values().removeIf(s -> s == session);
    }

    public List<ISession> getSessions() {
        return this.sessions;
    }

    public ISession getSessionByAccount(String account) {
        return accountSessions.get(account);
    }

    public void cleanupSessions() {
        Iterator<ISession> iterator = sessions.iterator();
        while (iterator.hasNext()) {
            ISession session = iterator.next();
            if (!session.isConnected()) {
                iterator.remove();
                removeSession(session);
                session.dispose();
            }
        }
    }

    public void startCleanupThread() {
        Thread cleanupThread = new Thread(() -> {
            while (true) {
                cleanupSessions();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }

    public ISession findByID(long id) throws Exception {
        for (ISession session : this.sessions) {
            if (session.getID() == id) {
                return session;
            }
        }
        throw new Exception("Session " + id + " does not exist");
    }

    public int getNumSession() {
        return this.sessions.size();
    }
}
