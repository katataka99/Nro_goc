package network;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import network.inetwork.IMessageHandler;
import network.inetwork.IMessageSendCollect;
import network.inetwork.ISession;

public final class Collector
        implements Runnable {

    private ISession session;
    private DataInputStream dis;
    private IMessageSendCollect collect;
    private IMessageHandler messageHandler;

    public Collector(ISession session, Socket socket) {
        this.session = session;
        this.setSocket(socket);
    }

    public Collector setSocket(Socket socket) {
        try {
            this.dis = new DataInputStream(socket.getInputStream());
        } catch (IOException iOException) {
            // empty catch block
        }
        return this;
    }

    @Override
    public void run() {
        try {
            while (this.session != null && this.session.isConnected()) {
                Message msg = this.collect.readMessage(this.session, this.dis);
                if (msg.command == -27) {
                    this.session.sendKey();
                } else {
                    this.messageHandler.onMessage(this.session, msg);
                }
                msg.cleanup();
            }
        } catch (Exception exception) {
            // empty catch block
        }
        try {
            Network.gI().getAcceptHandler().sessionDisconnect(this.session);
        } catch (Exception exception) {
            // empty catch block
        }
        if (this.session != null) {
            this.session.disconnect();
        }
    }

    public void setCollect(IMessageSendCollect collect) {
        this.collect = collect;
    }

    public void setMessageHandler(IMessageHandler handler) {
        this.messageHandler = handler;
    }

    public void close() {
        if (this.dis != null) {
            try {
                this.dis.close();
            } catch (IOException iOException) {
                // empty catch block
            }
        }
    }

    public void dispose() {
        this.session = null;
        this.dis = null;
        this.collect = null;
    }
}
