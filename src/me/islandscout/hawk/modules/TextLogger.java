package me.islandscout.hawk.modules;

import me.islandscout.hawk.Hawk;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TextLogger {

    private Hawk hawk;
    private File storageFile;
    private List<String> buffer = new ArrayList<>();
    private boolean enabled;

    public TextLogger(Hawk hawk, boolean enabled) {
        this.hawk = hawk;
        this.enabled = enabled;
    }

    public void prepare(File loggerFile) {
        storageFile = loggerFile;
        if(!storageFile.exists() && enabled) {
            try {
                //noinspection ResultOfMethodCallIgnored
                storageFile.createNewFile();
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void logMessage(String message) {
        if(!enabled) return;
        message = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('§', message));
        Calendar date = Calendar.getInstance();
        String hour = date.get(Calendar.HOUR_OF_DAY) < 10 ? "0" + date.get(Calendar.HOUR_OF_DAY) : "" + date.get(Calendar.HOUR_OF_DAY);
        String minute = date.get(Calendar.MINUTE) < 10 ? "0" + date.get(Calendar.MINUTE) : "" + date.get(Calendar.MINUTE);
        String second = date.get(Calendar.SECOND) < 10 ? "0" + date.get(Calendar.SECOND) : "" + date.get(Calendar.SECOND);
        buffer.add("[" + (date.get(Calendar.MONTH) + 1) + "/" + date.get(Calendar.DAY_OF_MONTH) + "/" + date.get(Calendar.YEAR) + "] [" + hour + ":" + minute + ":" + second + "] " + message);
    }

    public void updateFile() {
        if(!enabled) return;
        if(buffer.size() == 0) return;
        List<String> asyncList = new ArrayList<>();
        asyncList.addAll(buffer);
        buffer.clear();
        BukkitScheduler hawkLogger = Bukkit.getServer().getScheduler();
        hawkLogger.runTaskAsynchronously(hawk, () -> {
            try(FileWriter fw = new FileWriter(storageFile, true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw)) {
                for (String aBuffer : asyncList) {
                    out.println(aBuffer);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
