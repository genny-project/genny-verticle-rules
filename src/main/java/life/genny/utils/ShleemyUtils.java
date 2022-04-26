package life.genny.utils;

import life.genny.qwanda.message.QEventMessage;
import life.genny.qwanda.message.QScheduleMessage;
import life.genny.qwandautils.GennySettings;
import life.genny.models.GennyToken;
import life.genny.qwandautils.QwandaUtils;
import life.genny.qwandautils.JsonUtils;
import java.time.LocalDateTime;
import java.io.IOException;

public class ShleemyUtils {

	public static void scheduleMessage(GennyToken userToken, String eventMsgCode, String scheduleMsgCode, LocalDateTime triggertime, String targetCode) {

		String shleemyEndPoint = GennySettings.projectUrl+"/api/schedule";
		System.out.println(" sending scheduler shleemyEndPoint " + shleemyEndPoint);
		QEventMessage evtMsg = new QEventMessage("SCHEDULE_EVT", eventMsgCode);
		evtMsg.setToken(userToken.getToken());
		
		evtMsg.getData().setTargetCode(targetCode);

		String[] rxList = new String[2];
		rxList[0] = "SUPERUSER";
		rxList[1] = userToken.getUserCode();
		evtMsg.setRecipientCodeArray(rxList);

		System.out.println("Scheduling event: " + eventMsgCode + ", Trigger time: " + triggertime.toString());

		QScheduleMessage scheduleMessage = new QScheduleMessage(scheduleMsgCode, JsonUtils.toJson(evtMsg), userToken.getUserCode(), "project", triggertime, userToken.getRealm());
		System.out.println("scheduleMessage = " + scheduleMessage);

		try {
			QwandaUtils.apiPostEntity2(shleemyEndPoint, JsonUtils.toJson(scheduleMessage), userToken, null);
		} catch (IOException e) {
			e.printStackTrace();
		}		

	}
		
	public static void deleteSchedule(GennyToken userToken, String scheduleMsgCode) {

		String deleteEndpoint = GennySettings.projectUrl+"/api/schedule/code";

		try {
			System.out.println("Attempting to delete scheduled message with code " + scheduleMsgCode);
			QwandaUtils.apiDelete(deleteEndpoint + "/" + scheduleMsgCode, userToken.getToken());
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
}

