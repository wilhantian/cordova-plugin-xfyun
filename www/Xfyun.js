/////////////////////////////////////////////////////////////
/// 科大讯飞语音识别插件
/// create by wilhan.tian
/////////////////////////////////////////////////////////////
var exec = require('cordova/exec');

/// 初始化Xfyun
/// appid: 应用ID 可在官网查询
/// engineMode: 引擎模式(如null则为'auto')
exports.init = function(appid, engineMode, success, error) {
    exec(success, error, "Xfyun", "init", [appid, engineMode]);
};

/// 构建命令
/// cloudGrammar: 命令语句 具体语法可参考官方实例
/// success(grammarId)
///     grammarId: 命令ID
/// error(errCode)
///     errCode: 错误码
exports.buildGrammar = function(cloudGrammar, success, error) {
    exec(success, error, "Xfyun", "buildGrammar", [cloudGrammar]);
};

/// 开始监听识别命令
/// grammarId: 命令ID
/// success(res)
///     res: {
///         action: string,//(onVolumeChanged | onResult | onBeginOfSpeech | onEndOfSpeech)
///         data: any,//({volume:number,data:byte[]} | {result:{},isLast:boolean} | void | void)
///     }
/// error(errCode)
///     errCode: 错误码（除官方错误码外，还包含本插件错误码。-1:未成功构建命令, -2:用户未赋予相关权限）
exports.startListeningGrammar = function(grammarId, success, error) {
    exec(success, error, "Xfyun", "startListeningGrammar", [grammarId]);
};

/// 停止命令识别(只是停止录制，停止后立即将语音进行识别，回调会继续执行。可通过startListeningGrammar继续监听)
exports.stopListeningGrammar = function(success, error) {
    exec(success, error, "Xfyun", "stopListeningGrammar", []);
};

/// 取消命令识别(完全释放语音识别功能，相关回调不在执行，需要重新构建命令[buildGrammar])
exports.cancelGrammar = function(success, error) {
    exec(success, error, "Xfyun", "cancelGrammar", []);
};