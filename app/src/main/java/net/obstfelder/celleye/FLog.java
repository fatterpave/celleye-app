package net.obstfelder.celleye;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.Log;

import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import java.io.IOException;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * A logger that uses the standard Android Log class to log exceptions, and also logs them to a
 * file on the device. Requires permission WRITE_EXTERNAL_STORAGE in AndroidManifest.xml.
 * @author Cindy Potvin
 */
public class FLog
{

    static private Context appContext;

    public static final int LEVEL_NONE=0;
    public static final int LEVEL_DEBUG=1;
    public static final int LEVEL_INFO=2;
    public static final int LEVEL_WARN=3;
    public static final int LEVEL_ERROR=4;

    private static final String TAG = "CELLEYE";
    public static final int FILE_SIZE = 1024*1024; //1MB
    private static String FILENAME="/celleye.log";
    private static int FileLogLevel=3;//3=DEBUG
    //private static final LOGFORMAT="";
    private static Logger logger;
    public static void SetContext(Context context){appContext=context;}

    public static void initializeLogger(Context context)
    {
        appContext=context;
        if(logger==null) {
            logger = Logger.getLogger(FLog.class.getName());
            try {
                //
                // Creating an instance of FileHandler with 5 logging files
                // sequences.
                //
                String filename = Environment.getExternalStorageDirectory() + FILENAME;
                FileHandler handler = new FileHandler(filename, FILE_SIZE, 5, true);
                handler.setFormatter(new Formatter() {
                    public String format(LogRecord record)
                    {

                        return getDateTimeStamp() + " " + loggerLevelText(record.getLevel()) + "  :  " + record.getMessage() + "\n";
                    }
                });
                logger.addHandler(handler);
                logger.setUseParentHandlers(false);
                logger.setLevel(Level.FINE);
            }
            catch (IOException e)
            {
                logger.warning("Failed to initialize logger handler." + e);
                logger=null;
            }
        }
    }

    public static void setFileLogLevel(String levelText)
    {
        switch(levelText)
        {
            case "0":
            case "Ingen": FileLogLevel = LEVEL_NONE; break; //none
            case "1":
            case "Debug": FileLogLevel = LEVEL_DEBUG; break;//debug
            case "2":
            case "Info": FileLogLevel = LEVEL_INFO; break;//info
            case "3":
            case "Advarsel": FileLogLevel = LEVEL_WARN; break;//warn
            case "4":
            case "Feil": FileLogLevel = LEVEL_ERROR; break;//error
            default: FileLogLevel = LEVEL_INFO; break; //default=info
        }
    }

    public static String loggerLevelText(Level level)
    {
        switch(level.getName())
        {
            case "OFF": return " "; //none
            case "FINEST": return "D"; //debug
            case "FINER": return "D"; //debug
            case "FINE": return "D"; //debug
            case "INFO": return "I"; //info
            case "WARNING": return "W"; //warn
            case "SEVERE": return "E"; //error
            default: return " "; //default=info
        }
    }

    public static void e(String logMessageTag, String logMessage)
    {
        //file logging
        logToFile(LEVEL_ERROR, logMessageTag, logMessage);

        //logcat logging
        Log.e(logMessageTag, logMessage);
    }

    public static void e(String logMessageTag, String logMessage, Throwable throwableException)
    {
        //file logging
        logToFile(LEVEL_ERROR,logMessageTag,logMessage + "\r\n" + Log.getStackTraceString(throwableException));

        //logcat logging
        Log.e(logMessageTag, logMessage, throwableException);
    }

    public static void w(String logMessageTag, String logMessage)
    {
        //file logging
        logToFile(LEVEL_WARN,logMessageTag, logMessage);
        //logcat logging
        Log.w(logMessageTag, logMessage);
    }

    public static void w( String logMessageTag, String logMessage, Throwable throwableException)
    {
        //file logging
        logToFile(LEVEL_WARN,logMessageTag,logMessage + "\r\n" + Log.getStackTraceString(throwableException));
        //logcat logging
        Log.w(logMessageTag, logMessage, throwableException);
    }

    public static void i(String logMessageTag, String logMessage)
    {
        //file logging
        logToFile(LEVEL_INFO,logMessageTag, logMessage);
        //logcat logging
        Log.i(logMessageTag, logMessage);
    }

    public static void i(String logMessageTag, String logMessage, Throwable throwableException)
    {
        //file logging
        logToFile(LEVEL_INFO,logMessageTag, logMessage + "\r\n" + Log.getStackTraceString(throwableException));
        //logcat logging
        Log.i(logMessageTag, logMessage, throwableException);
    }

    public static void d(String logMessageTag, String logMessage)
    {
        //file logging
        logToFile(LEVEL_DEBUG, logMessageTag, logMessage);
        //logcat logging
        Log.d(logMessageTag, logMessage);
    }

    public static void d(String logMessageTag, String logMessage, Throwable throwableException)
    {
        //file logging
        logToFile(LEVEL_DEBUG,logMessageTag, logMessage + "\r\n" + Log.getStackTraceString(throwableException));
        //logcat logging
        Log.d(logMessageTag, logMessage, throwableException);
    }



    /**
     * Writes a message to the log file on the device.
     * @param logMessageTag A tag identifying a group of log messages.
     * @param logMessage The message to add to the log.
     */
    private static void oldLogToFile(Context context, String level, String logMessageTag, String logMessage)
    {
        try
        {
            if(logMessage.length()>800)//truncate if very long message
            {
                logMessage=logMessage.substring(0,800);
            }

            // Gets the log file from the root of the primary storage. If it does
            // not exist, the file is created.
            File logFile = new File(Environment.getExternalStorageDirectory(), "IO-APP-LOG.txt");
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            // Write the message to the log with a timestamp
            BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true));
            writer.write(String.format("%1s %2s [%3s]:%4s\r\n",
                    getDateTimeStamp(), level, logMessageTag, logMessage));
            writer.close();

            // Refresh the data so it can seen when the device is plugged in a
            // computer. You may have to unplug and replug to see the latest
            // changes
            if(context !=null)
            {
                MediaScannerConnection.scanFile(context,
                        new String[] { logFile.toString() },
                        null,
                        null);
            }
        }
        catch (IOException e)
        {
            Log.e("com.cindypotvin.FLog", "Unable to log exception to file.");
        }
    }

    /**
     * Writes a message to the log file on the device.
     * @param logMessageTag A tag identifying a group of log messages.
     * @param logMessage The message to add to the log.
     */
    private static void logToFile(int log_level, String logMessageTag, String logMessage)
    {
        if(logger==null)
        {
            Log.w(TAG,"Logger not initialized");
            return;
        }

        if(FileLogLevel <= log_level && FileLogLevel!=LEVEL_NONE) {
            try
            {
                if (logMessage.length() > 800)//truncate if very long message
                {
                    logMessage = logMessage.substring(0, 800);
                }


                logger.log(convertLogLevel(log_level), logMessage);


            } catch (Exception e)
            {
                Log.e("com.cindypotvin.FLog", "Unable to log exception to file.");
            }
        }
    }

    /**
     * Gets a stamp containing the current date and time to write to the log.
     * @return The stamp for the current date and time.
     */
    private static String getDateTimeStamp()
    {
        Date dateNow = Calendar.getInstance().getTime();
        // My locale, so all the log files have the same date and time format
        return (DateFormat.getDateTimeInstance
                (DateFormat.SHORT, DateFormat.MEDIUM, Locale.GERMAN).format(dateNow));
    }

    private static Level convertLogLevel(int log_level)
    {
        switch(log_level)
        {
            case LEVEL_NONE: return Level.OFF;
            case LEVEL_DEBUG: return Level.FINE;
            case LEVEL_INFO: return Level.INFO;
            case LEVEL_WARN: return Level.WARNING;
            case LEVEL_ERROR: return Level.SEVERE;
            default: return Level.INFO;
        }
    }
}
