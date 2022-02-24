package life.genny.eventbus;

// import javax.enterprise.context.ApplicationScoped;
// import javax.inject.Inject;
// import javax.naming.NamingException;
// import io.vertx.core.eventbus.EventBus;
//// import life.genny.channel.Producer;
// import life.genny.cluster.CurrentVtxCtx;
// import life.genny.qwanda.entity.BaseEntity;

// @ApplicationScoped
public class EventBusVertx implements EventBusInterface {
  // private static final String WEBCMDS = "webcmds";
  // private static final String CMDS = "cmds";
  // private static final String WEBDATA = "webdata";
  // private static final String DATA = "data";
  // private static final String EVENTS = "events";
  // private static final String EVENT = "event";
  // private static final String SIGNALS = "signals";
  // private static final String MESSAGES = "messages";
  // private static final String STATEFULMESSAGES = "statefulmessages";
  // private static final String SERVICES = "services";

  //// @Inject
  //// Producer producer;

  // EventBus eventBus = null;

  // public EventBusVertx() {
  // eventBus = CurrentVtxCtx.getCurrentCtx().getClusterVtx().eventBus();
  // }

  // public EventBusVertx(EventBus eventBus) {
  // this.eventBus = eventBus;
  // }

  // public  void write(final String channel, final Object payload) throws NamingException
  // {//
  //// switch (channel) {
  //// case EVENT:
  //// case EVENTS:
  ////// if (Producer.getToServices().writeQueueFull()) {
  ////// log.error("EVENTS >> producer data is full hence message cannot be sent");
  //////
  // Producer.setToEvents(CurrentVtxCtx.getCurrentCtx().getClusterVtx().eventBus().publisher(EVENTS));
  ////// Producer.getToEvents().send(payload);
  ////// } else {
  //// producer.getToEvents().send(payload.toString());
  ////// }
  //// break;
  //// case DATA:
  ////// if (Producer.getToData().writeQueueFull()) {
  ////// log.error("DATA >> producer data is full hence message cannot be sent");
  //////
  // Producer.setToData(CurrentVtxCtx.getCurrentCtx().getClusterVtx().eventBus().publisher(DATA));
  ////// Producer.getToData().write(payload);
  ////// } else {
  ////// producer.getToData().write(payload.toString());
  //// producer.getToData().send(payload.toString());
  ////// }
  //// break;

  //// case WEBDATA:
  ////// if (Producer.getToWebData().writeQueueFull()) {
  ////// log.error("WEBDATA >> producer data is full hence message cannot be sent");
  //////
  // Producer.setToWebData(CurrentVtxCtx.getCurrentCtx().getClusterVtx().eventBus().publisher(WEBDATA));
  ////// Producer.getToWebData().write(payload);
  ////// } else {
  ////// producer.getToWebData().write(payload.toString());
  //// producer.getToWebData().send(payload.toString());
  ////// }
  //// break;
  //// case CMDS:
  ////// if (Producer.getToCmds().writeQueueFull()) {
  ////// log.error("CMDS >> producer data is full hence message cannot be sent");
  //////
  // Producer.setToCmds(CurrentVtxCtx.getCurrentCtx().getClusterVtx().eventBus().publisher(CMDS));
  ////// Producer.getToCmds().send(payload);
  ////// } else {
  //// producer.getToCmds().send(payload.toString());
  ////// }
  //// break;

  //// case WEBCMDS:
  ////// if (Producer.getToWebCmds().writeQueueFull()) {
  ////// log.error("WEBCMDS >> producer data is full hence message cannot be sent");
  //////
  // Producer.setToWebCmds(CurrentVtxCtx.getCurrentCtx().getClusterVtx().eventBus().publisher(WEBCMDS));
  ////// Producer.getToWebCmds().send(payload);
  ////// } else {
  //// producer.getToWebCmds().send(payload.toString());
  ////// }
  //// break;
  //// case SERVICES:
  ////// if (Producer.getToServices().writeQueueFull()) {
  ////// log.error("SERVICES >> producer data is full hence message cannot be sent");
  //////
  // Producer.setToServices(CurrentVtxCtx.getCurrentCtx().getClusterVtx().eventBus().publisher(SERVICES));
  ////// Producer.getToServices().send(payload);
  ////// } else {
  //// producer.getToServices().send(payload.toString());
  ////// }
  //// break;

  //// case MESSAGES:
  ////// if (Producer.getToMessages().writeQueueFull()) {
  ////// log.error("MESSAGES >> producer data is full hence message cannot be sent");
  //////
  // Producer.setToMessages(CurrentVtxCtx.getCurrentCtx().getClusterVtx().eventBus().publisher(MESSAGES));
  ////// Producer.getToMessages().send(payload);
  ////// } else {
  //// producer.getToMessages().send(payload.toString());
  ////// }

  //// break;
  //// case STATEFULMESSAGES:
  ////// if (Producer.getToStatefulMessages().writeQueueFull()) {
  ////// log.error("STATEFULMESSAGES >> producer data is full hence message cannot be sent");
  //////
  // Producer.setToStatefulMessages(CurrentVtxCtx.getCurrentCtx().getClusterVtx().eventBus().publisher(STATEFULMESSAGES));
  ////// Producer.getToStatefulMessages().send(payload);
  ////// } else {
  //// producer.getToStatefulMessages().send(payload.toString());
  ////// }
  //// break;
  //// case SIGNALS:
  ////// if (Producer.getToSignals().writeQueueFull()) {
  ////// log.error("SIGNALS >> producer data is full hence message cannot be sent");
  //////
  // Producer.setToSignals(CurrentVtxCtx.getCurrentCtx().getClusterVtx().eventBus().publisher(SIGNALS));
  ////// Producer.getToSignals().write(payload);
  ////// } else {
  //// producer.getToSignals().send(payload.toString());
  ////// }
  //// break;
  //// default:
  //////
  // Producer.setToDirect(CurrentVtxCtx.getCurrentCtx().getClusterVtx().eventBus().publisher(channel));
  ////// Producer.getToDirect().write(payload);

  //// log.error("Channel does not exist: " + channel);
  //// }
  // }

  // public  void send(final String channel, final Object payload) throws NamingException
  // {
  // switch (channel) {
  ////   case "event":
  //// case "events":
  //// producer.getToEvents().send(payload.toString());
  //// ;
  //// break;
  //// case "data":
  //// producer.getToData().send(payload.toString());
  //// break;

  //// case "webdata":
  //// producer.getToWebData().send(payload.toString());
  //// break;
  //// case CMDS:
  ////// if (Producer.getToCmds().writeQueueFull()) {
  ////// log.error("WEBSOCKET EVT >> producer data is full hence message cannot be sent");
  //////
  // Producer.setToCmds(CurrentVtxCtx.getCurrentCtx().getClusterVtx().eventBus().publisher(CMDS));
  ////// Producer.getToCmds().send(payload);

  ////// } else {
  //// producer.getToCmds().send(payload.toString());
  ////// }
  //// break;

  //// case WEBCMDS:
  ////// if (Producer.getToWebCmds().writeQueueFull()) {
  ////// log.error("WEBSOCKET EVT >> producer data is full hence message cannot be sent");
  //////
  // Producer.setToWebCmds(CurrentVtxCtx.getCurrentCtx().getClusterVtx().eventBus().publisher(WEBCMDS));
  ////// Producer.getToWebCmds().send(payload);

  ////// } else {
  //// producer.getToWebCmds().send(payload.toString());
  ////// }
  //// break;
  //// case "services":
  //// producer.getToServices().send(payload.toString());
  //// break;
  //// case "messages":
  //// producer.getToMessages().send(payload.toString());
  //// break;
  //// case "statefulmessages":
  //// producer.getToStatefulMessages().send(payload.toString());
  //// break;
  //// case "signals":
  //// producer.getToSignals().send(payload.toString());
  //// break;
  //// default:
  //// log.error("Channel does not exist: " + channel);
  // }
  // }

  // @Override
  // public void publish(BaseEntity user, String channel, Object payload, final String[]
  // filterAttributes) {
  ////   // Actually Send ....
  //// switch (channel) {
  //// case "event":
  //// case "events":
  //// producer.getToEvents().send(payload.toString());
  //// ;
  //// break;
  //// case "data":
  //// producer.getToData().send(payload.toString());
  //// break;

  //// case "webdata":
  //// payload = EventBusInterface.privacyFilter(user, payload, filterAttributes);
  //// producer.getToWebData().send(payload.toString());
  //// break;
  //// case "cmds":
  //// case "webcmds":
  //// payload = EventBusInterface.privacyFilter(user, payload, filterAttributes);
  ////// if (Producer.getToWebCmds().writeQueueFull()) {
  ////// log.error("WEBSOCKET EVT >> producer data is full hence message cannot be sent");
  //////
  // Producer.setToWebCmds(CurrentVtxCtx.getCurrentCtx().getClusterVtx().eventBus().publisher(WEBCMDS));
  ////// Producer.getToWebCmds().send(payload);

  ////// } else {
  //// producer.getToWebCmds().send(payload.toString());
  ////// }
  //// break;
  //// case "services":
  //// producer.getToServices().send(payload.toString());
  //// break;
  //// case "messages":
  //// producer.getToMessages().send(payload.toString());
  //// break;
  //// case "statefulmessages":
  //// producer.getToStatefulMessages().send(payload.toString());
  //// break;
  //// case "signals":
  //// producer.getToSignals().send(payload.toString());
  //// break;
  //// default:
  //// log.error("Channel does not exist: " + channel);
  //// }
  // }
}
