package de.schlund.pfixxml;

import de.schlund.pfixxml.*;
import de.schlund.pfixxml.serverutil.*;
import java.util.*;
import javax.servlet.http.*;
import org.apache.log4j.*;



/**
 * The <code>SessionCleaner</code> class is used to remove stored SPDocuments from the session
 * after a timout (currently 30sec). This helps in reducing the memory usage as those documents
 * are only stored for possible reuse by following subrequests (for frames). After 30secs one can
 * be reasonable sure that no subrequests will follow (During development, the AbstractXMLServer
 * makes sure not to call storeSPDocument() with the <code>starttask</code> parameter set to <b>false</b>,
 * because one may need the stored SPDocument for debugging purposes).
 *
 * Created: Thu Mar 20 16:45:31 2003
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version 1.0
 */
public class SessionCleaner {
    private static SessionCleaner instance     = new SessionCleaner();
    private        Timer          timer        = new Timer(true);
    private        long           OFFSET       = 20000;
    private        String         TASK_POSTFIX = "__TIMER_TASK";
    private        Category       CAT          = Category.getInstance(this.getClass());
    
    private SessionCleaner() {}

    /**
     * @return The <code>SessionCleaner</code> singleton.
     */
    public static SessionCleaner getInstance() {
        return instance;
    }

    /**
     * Called from the AbstractXMLServer to store a SPDocument into the supplied HttpSession. This will also start a TimerTask that
     * removes the stored SPDocument after a timeout.
     *
     * @param spdoc a <code>SPDocument</code> value
     * @param session a <code>HttpSession</code> value
     * @param conutil a <code>ContainerUtil</code> value
     * @param key a <code>String</code> value. The key under which the SPDocument will be stored in the session.
     * @param starttask a <code>boolean</code> value. True if the TimerTask should be startet, false if not.
     */
    public void storeSPDocument(SPDocument spdoc, HttpSession session, ContainerUtil conutil, String key, boolean starttask) {
        long   stamp   = System.currentTimeMillis();
        String taskkey = key + TASK_POSTFIX; 

        synchronized (session) {
            if (starttask) {
                SessionCleanerTask task   = (SessionCleanerTask) conutil.getSessionValue(session, taskkey);
                if (task != null) {
                    CAT.info("*** Found old TimerTask, trying to cancel... ");
                    try {
                        task.cancel();
                        CAT.info("*** DONE. ***");
                    } catch (IllegalStateException e) {
                        CAT.info("*** Could not cancel: " + e.getMessage() + " ***");
                    }
                }
                task = new SessionCleanerTask(session, conutil, key);
                timer.schedule(task, OFFSET);
                conutil.setSessionValue(session, taskkey, task);
            }
            
            spdoc.setTimestamp(stamp);
            conutil.setSessionValue(session, key, spdoc);
        }
    }

    private class SessionCleanerTask extends TimerTask {
        String        key;
        HttpSession   session;
        ContainerUtil conutil;
        
        public SessionCleanerTask(HttpSession session, ContainerUtil conutil, String key) {
            this.session = session;
            this.conutil = conutil;
            this.key     = key;
        }

        public void run() {
            try {
                CAT.info("*** CALLING TIMERTASK: Removing SPDoc '" + key + "' from session " + session.getId());
                synchronized (session) { conutil.setSessionValue(session, key, null); }
            } catch (IllegalStateException e) {
                CAT.info("*** Couldn't remove from session... " + e.getMessage() + " ***");
            }
            session = null; // we don't want to hold any spurious references to the session that may prohibit it being gc'ed
            key     = null;
            conutil = null;
        }
    }
} // SessionCleaner
