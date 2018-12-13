// ILogCat.aidl
package sjj.novel.logcat;
import sjj.novel.logcat.LogCatIBinderCallBack;


interface LogCatIBinder {
   void register(LogCatIBinderCallBack callback);
   void unRegister(LogCatIBinderCallBack callback);
}
