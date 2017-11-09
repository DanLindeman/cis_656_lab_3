package edu.gvsu.cis.cis656.message;

import java.util.Comparator;

/**
 * Message comparator class. Use with PriorityQueue.
 */
public class MessageComparator implements Comparator<Message> {

    @Override
    public int compare(Message lhs, Message rhs) {
        if (lhs.ts.happenedBefore(rhs.ts)) {
            return -1;
        }
        if (rhs.ts.happenedBefore(lhs.ts)) {
            return 1;
        }
        return 0;
    }

}
