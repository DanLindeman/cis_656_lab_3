package edu.gvsu.cis.cis656.client;

import edu.gvsu.cis.cis656.clock.VectorClock;
import edu.gvsu.cis.cis656.message.Message;
import edu.gvsu.cis.cis656.message.MessageComparator;
import edu.gvsu.cis.cis656.queue.PriorityQueue;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ClientListener implements Runnable {

    private DatagramSocket listenSocket;
    private PriorityQueue priorityQueue;
    private VectorClock vectorClock;

    public ClientListener(DatagramSocket listenerSocket, VectorClock vectorClock) {
        this.listenSocket = listenerSocket;
        this.vectorClock = vectorClock;
    }

    public void run() {

        MessageComparator comparator = new MessageComparator();
        this.priorityQueue = new PriorityQueue(comparator);
        listenForMessages();

    }

    public void syncClockFromClient(VectorClock clientVectorClock) {
        this.vectorClock.update(clientVectorClock);
    }

    private void listenForMessages() {
        while (true) {
            try {
                byte[] buf = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                try {
                    this.listenSocket.receive(packet);
                    String recvd = new String(packet.getData(), 0, packet.getLength());
                    Message parsedMessage = Message.parseMessage(recvd);
                    switch (parsedMessage.type) {
                        case 2:
                            this.priorityQueue.add(parsedMessage);
                            break;
                    }
                    Message topMessage = (Message) priorityQueue.peek();
                    printIncomingMessages(topMessage);
                } catch (Exception e) {
                }
            } catch (Exception e) {
            }
        }
    }

    private void printIncomingMessages(Message topMessage) {
        while (topMessage != null) {
            if (isPrintableTopMessage(topMessage)) {
                System.out.println(topMessage.sender + " : " + topMessage.message);
                this.vectorClock.update(topMessage.ts);
                this.priorityQueue.remove(topMessage);
                topMessage = (Message) this.priorityQueue.peek();
            } else {
                topMessage = null;
            }
        }
    }

    private boolean isPrintableTopMessage(Message topMessage) {
        boolean condition1 = false;
        boolean condition2 = false;
        if (vectorClock.getTime(topMessage.pid) + 1 == topMessage.ts.getTime(topMessage.pid)) {
            // I expected this message, because I saw your previous message
            condition1 = true;
        }

        VectorClock tmp = new VectorClock();
        tmp.setClock(topMessage.ts);
        tmp.addProcess(topMessage.pid, this.vectorClock.getTime(topMessage.pid));

        if (this.vectorClock.happenedBefore(tmp)) {
            condition2 = true;
        }

        return (condition1 && condition2);
    }

}
