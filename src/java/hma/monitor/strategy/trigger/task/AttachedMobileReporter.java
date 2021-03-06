/**
 *
 */
package hma.monitor.strategy.trigger.task;

import hma.conf.Configuration;
import hma.monitor.MonitorManager;
import hma.util.EasyWebGet;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;

/**
 * @author guoyezhi
 *
 */
public class AttachedMobileReporter extends MonitorStrategyAttachedTask {

    private String[] servers = null;

    private String hmaGSMsend = null;

    private static SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * @param taskWorker
     * @param workerArgs
     * @param taskArgs
     */
    public AttachedMobileReporter(
            String taskWorker,
            String[] workerArgs,
            MonitorStrategyAttachedTaskArguments taskArgs) {
        super(taskWorker, workerArgs, taskArgs);
        this.servers =
                MonitorManager.getGlobalConf().getStrings("baidu.gsm.servers");
        this.hmaGSMsend =
                System.getProperty("hma.home.dir") + "/conf/" +
                        MonitorManager.getMonitoredClusterName() + "/gsmsend.hma";
    }

    private void doSendSM(String[] receivers, String message) {

        if(MonitorManager.isAlarm() == false){
            System.out.println("Oops..." + message + ", monitor is shutdown ...");
            return;
        }

        Runtime rt = Runtime.getRuntime();

        for (int i = 0; i < receivers.length; i++) {
            for (int j = 0; j < servers.length; j++) {
                try {
                    for (String str : new String[] {
                            "gsmsend", "-s", servers[j].trim(),
                            receivers[i].trim() + "@" + '"' + message + '"' }) {
                        System.out.print(str + " ");
                    }
                    System.out.println();

                    rt.exec(new String[] {
                            hmaGSMsend,
                            "-s", servers[j].trim(),
                            receivers[i].trim(),
                            message });

                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }






    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        String[] receivers = getWorkerArgs();
        MonitorStrategyAttachedTaskArguments taskArgs =
                getAttachedTaskArguments();
        String infoLevel = taskArgs.getAlarmLevel();
        Configuration conf = MonitorManager.getGlobalConf();
        String alarmThres = conf.get(
                "mobile.reporter.alarm.level.threshold",
                "NOTICE");

        if (MonitorStrategyAttachedTaskAdapter.compareAlarmLevel(
                infoLevel, alarmThres) <= 0) {
            return;
        }

        StringBuilder msgBuilder = new StringBuilder();
        msgBuilder.append("[HMA]");
        msgBuilder.append("[" + taskArgs.getTargetSystemName() + "]");
        msgBuilder.append("[" + taskArgs.getAlarmLevel() + "]");
        msgBuilder.append("[" + taskArgs.getMonitorStrategyName() + "]");
        msgBuilder.append("[" + taskArgs.getKeyInfo() + "]");
        msgBuilder.append("[" + dateFormat.format(taskArgs.getTimestamp()) + "]");

        doSendSM(receivers, msgBuilder.toString());
		
	/*
	 * add by yangsen01
	 * for Hermes.  important alerm should call 
	 */
        String url = "http://jingle.baidu.com/EIPCommunicator/pushIncident";
        String params="alertJson={"+
                "\"service\":\"dpf\","+
                "\"token\":\"551256bc833bd8d80f3c9869\","+
                "\"smsReceiver\":\"yangsen01\","+
                "\"details\":\""+ msgBuilder.toString() +"\","+
                "\"nodeId\":\"100030575\","+
                "\"nodeName\":\"BAIDU_INF_HADOOP\""+
                "}";
        EasyWebGet.post(url, params);
    }

    public static void main(String[] args) throws IOException {

        String message = "涓枃鐭俊娴嬭瘯";
        byte[] buff =
                Charset.forName("gbk").encode(message).array();
        message = "";
        DataOutputStream out = new DataOutputStream(
                new FileOutputStream("message.dat"));
        out.write(buff);
        out.close();

        Runtime rt = Runtime.getRuntime();
        rt.exec(new String[] {
                        "gsmsend", "-s", "emp01.baidu.com:15009",
                        "13811569457@$(cat message.dat)" },
                new String[] { "LANG=\"zh_CN.gbk\"" });
    }
}
