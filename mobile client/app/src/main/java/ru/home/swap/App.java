package ru.home.swap;

public class App {

    public static final String TAG = "TAG";

    public static final class Config {
        /**
         *  Should be in sync with server config. In case you need more flexibility
         *  consider to add page size as an option to server API. (at this moment
         *  is not supported)
         *  */
        private static final int DEV_PAGE_SIZE = 10;
        private static final int DEFAULT_PAGE_SIZE = 20;
        public static int getPageSize() {
            return BuildConfig.DEBUG ? DEV_PAGE_SIZE : DEFAULT_PAGE_SIZE;
        }
    }

}
