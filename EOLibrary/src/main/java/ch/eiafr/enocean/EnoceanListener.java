package ch.eiafr.enocean;

import ch.eiafr.enocean.telegram.CommonCommandTelegram;
import ch.eiafr.enocean.telegram.EventTelegram;
import ch.eiafr.enocean.telegram.LearnTelegram;
import ch.eiafr.enocean.telegram.RadioTelegram;
import ch.eiafr.enocean.telegram.RemoteManagementTelegram;
import ch.eiafr.enocean.telegram.ResponseTelegram;
import ch.eiafr.enocean.telegram.SmartAckTelegram;

public interface EnoceanListener {

	
	public void radioTelegram(RadioTelegram radioTelegram);
	
	public void responseTelegram(ResponseTelegram responseTelegram);
	
	public void eventTelegram(EventTelegram eventTelegram);
	
	public void commonCommandTelegram(CommonCommandTelegram commonCommandTelegram);
	
	public void smartAckTelegram(SmartAckTelegram smartAckTelegram);
	
	public void remoteManagementTelegram(RemoteManagementTelegram remoteManagementTelegram);

	public void learnTelegram(LearnTelegram learnTelegram);
}
