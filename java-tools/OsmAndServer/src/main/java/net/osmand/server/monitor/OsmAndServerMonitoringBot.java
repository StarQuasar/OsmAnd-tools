package net.osmand.server.monitor;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

@Component
public class OsmAndServerMonitoringBot extends TelegramLongPollingBot {

	@Autowired
	private MonitoringChatRepository chatRepo;

	@Autowired
	private OsmAndServerMonitorTasks monitoring;

	private static final Log LOG = LogFactory.getLog(OsmAndServerMonitoringBot.class);

	public Collection<MonitoringChatId> getMonitoringChatIds() {
		return chatRepo.findAll();
	}

	public void addMonitoringChatId(MonitoringChatId cid) {
		chatRepo.save(cid);
	}

	public void removeMonitoringChatId(Long chatId) {
		chatRepo.deleteById(chatId);
	}

	public void sendMonitoringAlertMessage(String text) {
		if (!isTokenPresent()) {
			return;
		}
		for (MonitoringChatId id : getMonitoringChatIds()) {
			SendMessage snd = new SendMessage();
			snd.setChatId(id.id);
			snd.setText(text);
			snd.enableHtml(true);
			try {
				sendText(snd);
			} catch (TelegramApiException e) {
				e.printStackTrace();
				LOG.error("Error sending update to " + id, e);
			}
		}
	}

	@Override
	public String getBotUsername() {
		if(System.getenv("OSMAND_DEV_TEST_BOT_TOKEN_MON") != null) {
			return System.getenv("OSMAND_DEV_TEST_BOT"); 
		}
		return "osmand_server_bot";
	}

	public void sendText(SendMessage snd) throws TelegramApiException {
		sendApiMethod(snd);
	}

	@Override
	public String getBotToken() {
		if(System.getenv("OSMAND_DEV_TEST_BOT_TOKEN_MON") != null) {
			return System.getenv("OSMAND_DEV_TEST_BOT_TOKEN_MON");
		}
		return System.getenv("OSMAND_SERVER_BOT_TOKEN");
	}

	public boolean isTokenPresent() {
		return getBotToken() != null;
	}

	@Override
	public void onUpdateReceived(Update update) {
		if (!update.hasMessage() || !update.getMessage().hasText()) {
			return;
		}
		Message msg = update.getMessage();
		try {
			SendMessage snd = new SendMessage();
			snd.setChatId(msg.getChatId());
			String coreMsg = msg.getText();
			if (coreMsg.startsWith("/")) {
				coreMsg = coreMsg.substring(1);
			}
			int at = coreMsg.indexOf("@");
			if (at > 0) {
				coreMsg = coreMsg.substring(0, at);
			}
			if (msg.isCommand() && "start_monitoring".equals(coreMsg)) {
				MonitoringChatId mid = new MonitoringChatId();
				mid.id = msg.getChatId();
				User from = msg.getFrom();
				if (from != null) {
					mid.firstName = from.getFirstName();
					mid.lastName = from.getLastName();
					mid.userId = from.getId() == null ? null : Long.valueOf(from.getId());
				}
				addMonitoringChatId(mid);
				snd.setText("Monitoring of OsmAnd server has started");
			} else if (msg.isCommand() && "stop_monitoring".equals(coreMsg)) {
				removeMonitoringChatId(msg.getChatId());
				snd.setText("Monitoring of OsmAnd server has stopped");
			} else if (msg.isCommand() && "refresh".equals(coreMsg)) {
				snd.enableHtml(true);
				snd.setText(monitoring.refreshAll());
			} else if (msg.isCommand() && "refresh-all".equals(coreMsg)) {
				String refreshAll = monitoring.refreshAll();
				for (MonitoringChatId l : getMonitoringChatIds()) {
					snd = new SendMessage();
					snd.enableHtml(true);
					snd.setChatId(l.id);
					snd.setText(refreshAll);
					sendText(snd);
				}
				return;
			} else if (msg.isCommand() && "status".equals(coreMsg)) {
				snd.enableHtml(true);
				snd.setText(monitoring.getStatusMessage());
			} else {
				snd.setText("Sorry, I don't know what to do");
			}

			sendApiMethod(snd);
		} catch (TelegramApiException e) {
			LOG.error(
					"Could not send message on update " + msg.getChatId() + " " + msg.getText() + ": " + e.getMessage(),
					e);
		}
	}

}