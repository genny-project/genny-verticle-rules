package life.genny.utils;

import life.genny.qwanda.message.QEventMessage;
import life.genny.qwanda.message.QScheduleMessage;
import life.genny.qwandautils.GennySettings;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.models.GennyToken;
import life.genny.qwandautils.QwandaUtils;
import life.genny.qwandautils.JsonUtils;
import java.time.LocalDateTime;
import java.io.IOException;

public class ShleemyUtils {

	public static void scheduleMessage(GennyToken userToken, String eventMsgCode, String scheduleMsgCode, LocalDateTime triggertime, String targetCode) {

		String shleemyEndPoint = GennySettings.projectUrl+"/api/schedule";

		QEventMessage evtMsg = new QEventMessage("SCHEDULE_EVT", eventMsgCode);
		evtMsg.setToken(userToken.getToken());
		
		evtMsg.getData().setTargetCode(targetCode);

		String[] rxList = new String[2];
		rxList[0] = "SUPERUSER";
		rxList[1] = userToken.getUserCode();
		evtMsg.setRecipientCodeArray(rxList);

		System.out.println("Scheduling event message for " + triggertime.toString());

		QScheduleMessage scheduleMessage = new QScheduleMessage(scheduleMsgCode, JsonUtils.toJson(evtMsg), userToken.getUserCode(), "project", triggertime, userToken.getRealm());
		System.out.println("scheduleMessage = " + scheduleMessage);

		try {
			QwandaUtils.apiPostEntity(shleemyEndPoint, JsonUtils.toJson(scheduleMessage), userToken.getToken());
		} catch (IOException e) {
			e.printStackTrace();
		}		

	}
		
}

