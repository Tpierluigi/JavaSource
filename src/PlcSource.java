
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;
import org.apache.flume.PollableSource;
import org.apache.flume.conf.Configurable;
import org.apache.flume.source.AbstractSource;
import org.apache.flume.Context;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.Event;
import org.apache.flume.FlumeException;
import org.libnodave.Nodave;
import org.libnodave.PLCinterface;
import org.libnodave.TCPConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlcSource extends AbstractSource implements Configurable, PollableSource {

    protected String plcAddr;
    protected int rack;
    protected int slot;
    protected String address;
    protected int numItems;

    private TCPConnection dc;
    private PLCinterface di;
    private Logger logger;

    @Override
    public void configure(Context context) {
        //leggo i parametri dal file di configurazione
        this.plcAddr = context.getString("plcAddr", "localhost");
        this.rack = Integer.parseInt(context.getString("rack", "0"));
        this.slot = Integer.parseInt(context.getString("slot", "2"));
        this.numItems = Integer.parseInt(context.getString("numItems", "2"));
        this.address = context.getString("address", "");
    }

    @Override
    public void start() {
        //iinizializzo il logger per i messaggi di errore
        logger = LoggerFactory.getLogger(PlcSource.class);
        //inizializzo la connessione al PLC
        OutputStream oStream = null;
        InputStream iStream = null;

        Socket sock;
        try {
            sock = new Socket(this.plcAddr, 102);
        } catch (IOException e) {
            sock = null;
            logger.error(e.getMessage());
            throw new FlumeException(e);
        }
        if (sock != null) {
            //provo ad inizializzare gli stream
            try {
                oStream = sock.getOutputStream();
                iStream = sock.getInputStream();
            } catch (IOException e) {
                logger.error(e.getMessage());
                oStream = null;
                iStream = null;
                throw new FlumeException(e);
            }
            if (iStream != null && oStream != null) {
                this.di = new PLCinterface(
                        oStream,
                        iStream,
                        "IF1",
                        0,
                        Nodave.PROTOCOL_ISOTCP);
                this.dc = new TCPConnection(this.di, 0, this.slot);
            }
        }
    }

    @Override
    public void stop() {
        // Disconnect from external client and do any additional cleanup
        // (e.g. releasing resources or nulling-out field values) ..
        if (dc != null) {
            dc.disconnectPLC();
        }
        if (di != null) {
            di.disconnectAdapter();
        }
    }

    @Override
    public Status process() throws EventDeliveryException {
        Status status = null;
        
        try {
            Event e =new Event() {

                @Override
                public Map<String, String> getHeaders() {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public void setHeaders(Map<String, String> map) {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public byte[] getBody() {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public void setBody(byte[] bytes) {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }
            };
      // This try clause includes whatever Channel/Event operations you want to do

            // Receive new data
            // Event e = getSomeData();
            // Store the Event into this Source's associated Channel(s)
            // getChannelProcessor().processEvent(e);
            status = Status.READY;
        } catch (Throwable t) {
            // Log exception, handle individual exceptions as needed

            status = Status.BACKOFF;

            // re-throw all Errors
            if (t instanceof Error) {
                throw (Error) t;
            }
        } finally {

        }
        return status;
    }
}
