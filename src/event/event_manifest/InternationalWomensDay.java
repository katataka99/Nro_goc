package event.event_manifest;

/**
 *
 * @author HM
 */
import consts.ConstNpc;
import event.Event;
import jdbc.daos.EventDAO;

public class InternationalWomensDay extends Event {

    @Override
    public void init() {
        super.init();
        EventDAO.loadInternationalWomensDayEvent();
    }

    @Override
    public void npc() {
    }
}
