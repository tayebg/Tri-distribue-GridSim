import gridsim.*;
import gridsim.net.*;
import java.util.*;

public class MainSimulation {

    public static void runSimulation(int N, int workers) {
        try {
            GridSim.init(workers + 1, Calendar.getInstance(), false);

            double baud = 1000000;
            double delay = 10;
            int mtu = 1500;

            MasterNode master = new MasterNode(
                    "Master", workers, N, baud, delay, mtu
            );

            ArrayList<WorkerNode> workerList = new ArrayList<>();
            for (int i = 0; i < workers; i++) {
                workerList.add(
                        new WorkerNode("Worker_" + i, baud, delay, mtu)
                );
            }

            Router router = new RIPRouter("router", false);

            router.attachHost(master, new FIFOScheduler("master_sched"));

            for (int i = 0; i < workers; i++) {
                router.attachHost(workerList.get(i),
                        new FIFOScheduler("worker_sched_" + i));
            }

            GridSim.startGridSimulation();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}