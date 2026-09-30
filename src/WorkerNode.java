import gridsim.*;
import gridsim.net.*;
import java.util.*;
import eduni.simjava.Sim_event;

public class WorkerNode extends GridSim {

    public WorkerNode(String name, double baud, double delay, int mtu) throws Exception {
        super(name, new SimpleLink(name + "_link", baud, delay, mtu));
    }

    @Override
    public void body() {
        runWorker();
        shutdownUserEntity();
        terminateIOEntities();
    }

    private void runWorker() {
        while (true) {
            Sim_event ev = new Sim_event();
            sim_get_next(ev);

            if (ev.get_tag() == MasterNode.SEND_DATA) {
                @SuppressWarnings("unchecked")
                List<Integer> sub = (List<Integer>) ev.get_data();

                MasterNode.addLog(get_name() + " received list: " + sub);

                Collections.sort(sub);

                MasterNode.addLog(get_name() + " sent back sorted list: " + sub);

                int masterId = GridSim.getEntityId("Master");

                Object[] payload = new Object[2];
                payload[0] = get_name();
                payload[1] = sub;

                send(masterId, 0.0, MasterNode.RETURN_DATA, payload);
                break;
            }
        }
    }
}