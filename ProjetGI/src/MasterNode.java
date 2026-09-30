import gridsim.*;
import gridsim.net.*;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;
import eduni.simjava.Sim_event;

public class MasterNode extends GridSim {

    public static List<Integer> finalResult = null;
    public static List<Integer> generatedData = null;

    // Full simulation log for the UI
    public static StringBuilder simulationLog = new StringBuilder();

    private int workers;
    private int N;
    private List<List<Integer>> receivedLists = new ArrayList<>();

    public static final int SEND_DATA = 1;
    public static final int RETURN_DATA = 2;

    public MasterNode(String name, int workers, int N,
                      double baud, double delay, int mtu) throws Exception {
        super(name, new SimpleLink(name + "_link", baud, delay, mtu));
        this.workers = workers;
        this.N = N;
    }

    @Override
    public void body() {
        runMaster();
        shutdownUserEntity();
        terminateIOEntities();
    }

    public static synchronized void addLog(String message) {
        System.out.println(message);
        simulationLog.append(message).append("\n");
    }

    private void runMaster() {
        gridSimHold(2);

        // reset static outputs before each run
        finalResult = null;
        generatedData = null;
        simulationLog.setLength(0);

        List<Integer> data = new ArrayList<>();
        Random rand = new Random();

        for (int i = 0; i < N; i++) {
            data.add(rand.nextInt(200) - 100);
        }

        generatedData = new ArrayList<>(data);
        addLog("Master generated list: " + data);

        int partSize = N / workers;
        int index = 0;

        for (int i = 0; i < workers; i++) {
            int end = (i == workers - 1) ? N : index + partSize;
            List<Integer> sub = new ArrayList<>(data.subList(index, end));

            String workerName = "Worker_" + i;
            int workerId = GridSim.getEntityId(workerName);

            addLog("Master sent to " + workerName + ": " + sub);
            send(workerId, 0.0, SEND_DATA, sub);

            index = end;
        }

        for (int i = 0; i < workers; i++) {
            Sim_event ev = new Sim_event();
            sim_get_next(ev);

            if (ev.get_tag() == RETURN_DATA) {
                Object[] payload = (Object[]) ev.get_data();

                String workerName = (String) payload[0];

                @SuppressWarnings("unchecked")
                List<Integer> sorted = (List<Integer>) payload[1];

                addLog("Master received from " + workerName + ": " + sorted);
                receivedLists.add(sorted);
            }
        }

        List<Integer> finalList = mergeKLists(receivedLists);
        finalResult = finalList;

        addLog("Master merged final list: " + finalList);
    }

    private List<Integer> mergeKLists(List<List<Integer>> lists) {
        PriorityBlockingQueue<int[]> pq =
                new PriorityBlockingQueue<>(10, Comparator.comparingInt(a -> a[0]));

        List<Integer> result = new ArrayList<>();

        for (int i = 0; i < lists.size(); i++) {
            if (!lists.get(i).isEmpty()) {
                pq.add(new int[]{lists.get(i).get(0), i, 0});
            }
        }

        while (!pq.isEmpty()) {
            int[] top = pq.poll();

            int value = top[0];
            int listIndex = top[1];
            int elemIndex = top[2];

            result.add(value);

            if (elemIndex + 1 < lists.get(listIndex).size()) {
                int nextVal = lists.get(listIndex).get(elemIndex + 1);
                pq.add(new int[]{nextVal, listIndex, elemIndex + 1});
            }
        }

        return result;
    }
}