package ru19july.tgchart.utils;

import android.graphics.Color;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils{

    public static final int RESULT_CLOSE_ALL = 0;

    public static final int SERVER_TYPE_OAUTH = 1;
    public static final int SERVER_TYPE_LOGIN = 2;

    //цвета
    public static final int GREEN_COLOR = Color.argb(255, 0, 127, 0);
    public static final int RED_COLOR = Color.argb(255, 255, 0, 0);
    public static final int POLYGON_BG_COLOR = Color.argb(255, 220, 236, 249);
    public static final int LINE_COLOR = Color.argb(255, 0, 0, 58);
    public static final int RED_BLOCK_COLOR = Color.argb(32, 128, 0, 0);
    public static final int GREEN_BLOCK_COLOR = Color.argb(32, 0, 255, 0);
    public static final int PROFIT_COLOR = Color.argb(100, 153, 153, 153);
    public static final int CHART_LINE_COLOR = Color.argb(255, 79, 159, 222);
    public static final int TIME_COLOR = Color.argb(255, 50, 53, 58);
    public static final int NICESCALE_TEXT_COLOR = Color.argb(255, 153, 153, 153);
    public static final int MARKER_BG_COLOR = Color.argb(255, 50, 53, 58);
    public static final int MARKER_BG_COLOR_RED = Color.argb(255, 242, 84, 84);
    public static final int MARKER_TEXT_COLOR = Color.argb(255, 237, 165, 5);
    public static final int MARKER_TEXT_COLOR_WHITE = Color.argb(255, 255, 255, 255);
    public static final int QUOTE_LINE_BLUE_COLOR = Color.argb(255, 113, 151, 189);
    public static final int MARKER_SECOND_BG_COLOR = Color.argb(200, 50, 53, 58);
    public static final int MARKER_SECOND_TEXT_COLOR = Color.argb(200, 255, 255, 255);
    public static final int TAB_NUMBER_BLUE_COLOR = Color.argb(255, 143, 193, 245);

    public static final int DIRECTION_NONE = 0;
    public static final int DIRECTION_PUT = 1;
    public static final int DIRECTION_CALL = 2;

    //Коды статусов запросов к NetServer
    public static final int ERROR_NONE = 0;
    public static final int ERROR = 1;
    public static final int ERROR_EMPTY_REQUEST = 2;
    public static final int ERROR_BAD_REQUEST = 3;
    public static final int ERROR_INVALID_ARGUMENT = 4;
    public static final int ERROR_ACCESS_DENIED = 5;
    public static final int ERROR_ACCOUNTS_OVERFLOW = 6;
    public static final int ERROR_ALREADY_EXISTS = 7;
    public static final int ERROR_NO_DATA = 8;
    public static final int ERROR_OBJECT_USED = 9;
    public static final int ERROR_NOT_SUPPORTED = 10;

    //Статусы при открытии опциона
    public static final int STATUS_OK = 1;
    public static final int STATUS_INCORRECT_RATE = 2;
    public static final int STATUS_RATE_MORE_LIMIT = 3;
    public static final int STATUS_NOT_ENOUGHT_MONEY = 4;
    public static final int STATUS_NON_TRADING_TIME = 5;
    public static final int STATUS_DENIED = 6;
    public static final int STATUS_USER_DISABLED = 7;
    public static final int STATUS_INACTIVE_USER = 8;
    public static final int STATUS_DEMO_EXPIRED = 9;
    public static final int STATUS_REQUOTE = 10;
    public static final int STATUS_REQUOTE_LEVELS = 11;
    public static final int STATUS_LOCK_TIME = 12;
    public static final int STATUS_SMALL_LIQUID = 13;

    //Другие константы
    public static final float TAB_NUMBER_TEXT_SIZE_RATIO = 0.333f;
    public static final float TAB_NUMBER_Y_POSITION_RATIO = 0.333f;
    public static final float TAB_TEXT_Y_POSITION_RATIO = 0.75f;
    public static final int DEFAULT_DECIMAL_COUNT = 0;
    public static final float PROFIT_TEXT_X_POSITION_RATIO = 0.5f;
    public static final float PROFIT_TEXT_Y_POSITION_RATIO = 2.0f/3.0f;
    public static final int FLOATING_QUOTE_MARGIN_LEFT = 25;
    public static final float FLOATING_QUOTE_MARGIN_TOP_RATIO = 1.0f/15f;
    public static final float FLOATING_QUOTE_WIDTH_RATIO = 1.3f;
    public static final float FLOATING_QUOTE_MARGIN_BOTTOM_RATIO = 1.0f/20f;
    public static final float FLOATING_QUOTE_TEXT_SIZE_RATIO = 1.0f / 15f;
    public static final float[] HORIZ_LINE_INTERVALS = new float[]{1, 1};

    //GUID для команды get_reference
    public static String GUID_RECOVER_PASSWORD = "9f155319-01c4-8632-0075-66d85d3ed57c";
    public static String GUID_WITHDRAW = "9B073C8E-B517-BED5-676A-AFF3DA9E47FE";
    public static String GUID_TOPUP = "63BDF577-954D-F9E9-8DE7-2B81EFA1536E";
    public static String GUID_REGISTER = "91acbdb9-c7a7-0e08-0cd3-04ba3592fd42";
    public static String GUID_EDIT = "D26EBE38-D587-A18D-CFF4-ECF7CE111C09";
    public static String GUID_CHANGE_PASSWORD = "44D9A422-DAB2-477F-FF01-18B4C26D2D00";

    public static final Map<String, String> GUID_COMMANDS = new HashMap<String, String>(){{
        put(GUID_RECOVER_PASSWORD, "LINK_RECOVER_PASSWORD");// Восстановить пароль
        put(GUID_WITHDRAW, "LINK_WITHDRAW");                // Вывод
        put(GUID_TOPUP, "LINK_TOPUP");                      // Пополнение
        put(GUID_REGISTER, "LINK_REGISTER");                // Регистрация
        put(GUID_EDIT, "LINK_EDIT");                        // Редактировать личные данные
        put(GUID_CHANGE_PASSWORD, "LINK_CHANGE_PASSWORD");  // Сменить пароль
    }};

    public static String GetErrorDesc(int errorCode)
    {
        switch (errorCode) {
            case ERROR_NONE:
                return "Нет ошибок";
            case ERROR:
                return "Ошибка";
            case ERROR_EMPTY_REQUEST:
                return "Пустой запрос";
            case ERROR_BAD_REQUEST:
                return "Неверный запрос, обычно неверное описание заголовка, невалидный json запроса.";
            case ERROR_INVALID_ARGUMENT:
                return "Неверное значение параметра запроса, нехватка обязательного параметра";
            case ERROR_ACCESS_DENIED:
                return "Отказ в доступе к данной функциональности для данного клиента";
            case ERROR_ACCOUNTS_OVERFLOW:
                return "Все возможные аккаунты созданы(0 - 65535)";
            case ERROR_ALREADY_EXISTS:
                return "Уже существует, уже запущено и т.п.";
            case ERROR_NO_DATA:
                return "Нет данных";
            case ERROR_OBJECT_USED:
                return "Нельзя удалить, т.к. используется(например группа)";
            case ERROR_NOT_SUPPORTED:
                return "Функция не поддерживается";
        }
        return "UNKNOWN ERROR";
    }

    public static String GetStatusDesc(int errorCode)
    {
        switch (errorCode) {
            case STATUS_OK:
                return "OK";
            case STATUS_INCORRECT_RATE:
                return "Wrong sum";
            case STATUS_RATE_MORE_LIMIT:
                return "Investment must be more than {min}";
            case STATUS_NOT_ENOUGHT_MONEY:
                return "Insufficient funds";
            case STATUS_NON_TRADING_TIME:
                return "Wrong timeframe";
            case STATUS_DENIED:
                return "Wrong pair";
            case STATUS_USER_DISABLED:
                return "Wrong pair";
            case STATUS_INACTIVE_USER:
                return "User is inactive";
            case STATUS_DEMO_EXPIRED:
                return "Demo period ends";
            case STATUS_REQUOTE:
                return "Реквот";
            case STATUS_REQUOTE_LEVELS:
                return "Реквот уровней One Touch/Range";
            case STATUS_LOCK_TIME:
                return "Small time before end, try another timeframe";
            case STATUS_SMALL_LIQUID:
                return "Low market activity, please try again later";
        }
        return "UNKNOWN STATUS";
    }

    public static int SecondsByTimeframe(String timeframe) {
        Pattern p = Pattern.compile("(\\w)(\\d+)");
        Matcher m = p.matcher(timeframe);
        while(m.find())
        {
            String time = m.group(1).toString();
            int value = Integer.parseInt(m.group(2).toString());
            if(time.equals("S")) return value;
            if(time.equals("M")) return value * 60;
            if(time.equals("H")) return value * 3600;
        }
        return 1;
    }
/*
    public static String HttpsContent(String url) {
        DataLoader dl = new DataLoader();
        try {
            HttpResponse response = dl.secureLoadData(url);

            StringBuilder sb = new StringBuilder();
            sb.append("HEADERS:\n\n");

            Header[] headers = response.getAllHeaders();
            for (int i = 0; i < headers.length; i++) {
                Header h = headers[i];
                sb.append(h.getName()).append(":\t").append(h.getValue()).append("\n");
            }

            InputStream is = response.getEntity().getContent();
            StringBuilder out = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            for (String line = br.readLine(); line != null; line = br.readLine())
                out.append(line);
            br.close();

            return out.toString();
        }
        catch(Exception e){

        }
        return null;
    }
*/
    public static String UnixtimeToString(int unixtime, String datetimeFormat) {
        Date date = new Date(unixtime * 1000L);
        SimpleDateFormat sdfDate = new SimpleDateFormat(datetimeFormat);
        sdfDate.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdfDate.format(date);
    }


}
