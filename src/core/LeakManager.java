package core;

import model.LeakReport;
import java.util.*;

public class LeakManager {
    private PriorityQueue<LeakReport> queue = new PriorityQueue<>();

    public void reportLeak(LeakReport leak) { queue.add(leak); }
    public LeakReport getNextCriticalLeak() { return queue.poll(); }

    public List<LeakReport> listAllPending() {
        List<LeakReport> sorted = new ArrayList<>(queue);
        Collections.sort(sorted);
        return sorted;
    }
}