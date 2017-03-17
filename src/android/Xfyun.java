package org.cordova.plugin.xfyun;

import android.content.Context;
import android.util.Log;
import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.GrammarListener;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.LexiconListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.util.ContactManager;
import com.iflytek.cloud.util.ContactManager.ContactListener;
import com.iflytek.cloud.util.ResourceUtil;
import com.iflytek.cloud.util.ResourceUtil.RESOURCE_TYPE;
import com.iflytek.cloud.SpeechUtility;

/**
 * cordova科大讯飞插件
 * create by wilhan.tian
 */
public class Xfyun extends CordovaPlugin {
    private SpeechRecognizer mAsr;
    private String TAG = "cordova-plguin-xfyun";
    private CallbackContext mBuildGrammarCallbackContext;

    private CallbackContext mNoPerGrammarListeningCallbackContext;
    private JSONArray mNoPerGrammarArgs;

    private String [] permissions = { 
        Manifest.permission.RECORD_AUDIO, 
        // Manifest.permission.WRITE_EXTERNAL_STORAGE,
        // Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("init")) {
            this.init(args, callbackContext);
            return true;
        }
        if (action.equals("buildGrammar")) {
            this.buildGrammar(args, callbackContext);
            return true;
        }
        if (action.equals("startListeningGrammar")) {
            this.startListeningGrammar(args, callbackContext);
            return true;
        }
        if (action.equals("stopListeningGrammar")) {
            this.stopListeningGrammar(args, callbackContext);
            return true;
        }
        if (action.equals("cancelGrammar")) {
            this.cancelGrammar(args, callbackContext);
            return true;
        }
        
        return false;
    }

    // 初始化
    private void init(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String appid = args.getString(0);
        String engine_mode = args.getString(1);

        if(engine_mode == null || engine_mode.isEmpty()){
            engine_mode = "auto";
        }

        Context context = cordova.getActivity().getApplicationContext();
        SpeechUtility.createUtility(context, "appid=" + appid + ",engine_mode=" + engine_mode);
        callbackContext.success();
    }

    // 构建命令
    private void buildGrammar(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String cloudGrammar = args.getString(0);

        // cloudGrammar = "#ABNF 1.0 UTF-8;\n"+
        //                 "language zh-CN;\n"+
        //                 "mode voice;\n"+
        //                 "root $main;\n"+
        //                 "$main = $place1 到 $place2;\n"+
        //                 "$place1 = 上海|合肥;\n"+
        //                 "$place2 = 北京|武汉|南京|天津|东京;";

        Context context = cordova.getActivity().getApplicationContext();
        if(mAsr == null){
            mAsr = SpeechRecognizer.createRecognizer(context, null);
        }

        mAsr.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
        int ret = mAsr.buildGrammar("abnf", cloudGrammar, new GrammarListener() {
            @Override
            public void onBuildFinish(String grammarId, SpeechError error) {
                if(error == null){
                    if(grammarId != null){ //构建语法成功，请保存grammarId用于识别
                        Log.d(TAG,"grammarId = " + grammarId);
                        mBuildGrammarCallbackContext.success(grammarId);
                    }
                    else{
                        Log.d(TAG,"未知错误");
                        mBuildGrammarCallbackContext.error("未知错误");
                    }
                }
                else{
                    Log.d(TAG,"语法构建失败,错误码:" + error.getErrorCode());
                    mBuildGrammarCallbackContext.error(error.getErrorCode());
                }
            }
        });

        if (ret != ErrorCode.SUCCESS){
            Log.d(TAG,"语法构建准备失败,错误码:" + ret);
            callbackContext.error(ret);
        }else{
            Log.d(TAG,"语法构建正常进行");
            mBuildGrammarCallbackContext = callbackContext;
        }
    }

    /// 监听命令
    private void startListeningGrammar(JSONArray args, CallbackContext callbackContext) throws JSONException {
        if(mAsr == null){
            callbackContext.error(-1);//"请先成功buildGrammar(构建命令)"
            return;
        }

        // 获取相关权限
        if(!hasPermisssion()){
            this.cordova.requestPermissions(this, 0, permissions);
            mNoPerGrammarArgs = args;
            mNoPerGrammarListeningCallbackContext = callbackContext;

            PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);
            return;
        }

        String grammarId = args.getString(0);
        Log.d(TAG,"-------- grammarId="+grammarId);

        mAsr.setParameter(SpeechConstant.ENGINE_TYPE, "cloud"); 
        mAsr.setParameter(SpeechConstant.CLOUD_GRAMMAR, grammarId);

        int ret = mAsr.startListening(new CustomRecognizerListener(callbackContext));
        if (ret != ErrorCode.SUCCESS) {
            Log.d(TAG,"启动监听器失败, 错误码: " + ret);
            callbackContext.error(ret);
        }
        else{
            PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);
        }
    }

    // 停止命令监听
    private void stopListeningGrammar(JSONArray args, CallbackContext callbackContext) throws JSONException {
        if(mAsr == null){
            Log.d(TAG,"mAsr为空，停止失败");
            callbackContext.error("当前无命令监听, 无法停止");
            return;
        }
        else{
            mAsr.stopListening();
            Log.d(TAG,"停止成功");
            callbackContext.success();
        }
    }

    // 取消命令
    private void cancelGrammar(JSONArray args, CallbackContext callbackContext) throws JSONException {
        if(mAsr == null){
            callbackContext.error("当前无命令监听, 无法取消");
            return;
        }
        else{
            mAsr.cancel();
            mAsr = null;
            callbackContext.success();
        }
    }

    @Override
    public boolean hasPermisssion() {
        for(String p : permissions)
        {
            if(!this.cordova.hasPermission(p))
                return false;
        }
        return true;
    }

    @Override
    public void requestPermissions(int requestCode){
        this.cordova.requestPermissions(this, requestCode, permissions);
    }

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException
    {
        if(mNoPerGrammarListeningCallbackContext != null) {
            for (int r : grantResults) {
                if (r == PackageManager.PERMISSION_DENIED) {
                    Log.e(TAG, "相关权限被拒绝!");
                    mNoPerGrammarListeningCallbackContext.error(-2);
                    return;
                }
            }
            this.startListeningGrammar(mNoPerGrammarArgs, mNoPerGrammarListeningCallbackContext);
        }
    }

    // 自定义监听器
    class CustomRecognizerListener implements RecognizerListener{
        private CallbackContext callbackContext;

        CustomRecognizerListener(CallbackContext callbackContext){
            this.callbackContext = callbackContext;
        }

        // 音量变化
        public void onVolumeChanged(int volume, byte[] data) {
            Log.d(TAG,"onVolumeChanged");
            try{
                JSONObject main = new JSONObject();
                main.put("action", "onVolumeChanged");

                JSONObject json = new JSONObject();
                json.put("volume", volume);
                json.put("data", data);
                main.put("data", json);

                PluginResult pResult = new PluginResult(PluginResult.Status.OK, main);
                pResult.setKeepCallback(true);
                callbackContext.sendPluginResult(pResult);
            }catch(JSONException e){}
        }

        // 返回结果
        public void onResult(final RecognizerResult result, boolean isLast) {
            Log.d(TAG,"onResult");
            try{
                JSONObject main = new JSONObject();
                main.put("action", "onResult");
                
                JSONObject json = new JSONObject();
                json.put("result", result == null ? "" : result.getResultString());
                json.put("isLast", isLast);
                main.put("data", json);

                PluginResult pResult = new PluginResult(PluginResult.Status.OK, main);
                pResult.setKeepCallback(true);
                callbackContext.sendPluginResult(pResult);
            }catch(JSONException e){}
        }

        // 开始说话
        public void onBeginOfSpeech() {
            Log.d(TAG,"onBeginOfSpeech");
            try{
                JSONObject main = new JSONObject();
                main.put("action", "onBeginOfSpeech");
                
                PluginResult pResult = new PluginResult(PluginResult.Status.OK, main);
                pResult.setKeepCallback(true);
                callbackContext.sendPluginResult(pResult);
            }catch(JSONException e){}
        }

        // 结束说话
        public void onEndOfSpeech() {
            Log.d(TAG,"onEndOfSpeech");
            try{
                JSONObject main = new JSONObject();
                main.put("action", "onEndOfSpeech");
                
                PluginResult pResult = new PluginResult(PluginResult.Status.OK, main);
                pResult.setKeepCallback(true);
                callbackContext.sendPluginResult(pResult);
            }catch(JSONException e){}
        }

        // 错误回调
        public void onError(SpeechError error) {
            Log.d(TAG,"发生错误，错误码:"+error.getErrorCode());
            callbackContext.error(error.getErrorCode());
        }

        // 件回调
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {}
    }
}
