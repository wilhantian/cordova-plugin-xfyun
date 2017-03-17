/********* Xfyun.m Cordova Plugin Implementation *******/
/********* create by wilhan.tian *******/

#import <Cordova/CDV.h>
#import <iflyMSC/iflyMSC.h>

@interface Xfyun : CDVPlugin<IFlySpeechRecognizerDelegate>{
    NSString* mGrammarListeningCommandId;
}

- (void)init:(CDVInvokedUrlCommand*)command;
- (void)buildGrammar:(CDVInvokedUrlCommand*)command;
- (void)startListeningGrammar:(CDVInvokedUrlCommand*)command;
- (void)stopListeningGrammar:(CDVInvokedUrlCommand*)command;
- (void)cancelGrammar:(CDVInvokedUrlCommand*)command;
@end

///==========================================================================================

@implementation Xfyun

// 初始化
- (void)init:(CDVInvokedUrlCommand*)command
{
    NSString* appid = [command.arguments objectAtIndex:0];
    NSString* engine_mode = [command.arguments objectAtIndex:1];
    
    NSString *initString = [[NSString alloc] initWithFormat:@"appid=%@,engine_mode=%@", appid, engine_mode];
    [IFlySpeechUtility createUtility:initString];
    
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

// 构建命令
- (void)buildGrammar:(CDVInvokedUrlCommand*)command
{
    NSString* cloudGrammar = [command.arguments objectAtIndex:0];
    NSLog(cloudGrammar);
    
    IFlySpeechRecognizer* recognizer = [IFlySpeechRecognizer sharedInstance];
    recognizer.delegate = self;
    
    //开启候选结果
    [recognizer setParameter:@"1" forKey:@"asr_wbest"];
    //设置引擎类型，cloud 或者 local
    [recognizer setParameter:@"cloud" forKey:[IFlySpeechConstant ENGINE_TYPE]];
    //设置字符编码为 utf-8
    [recognizer setParameter:@"utf-8" forKey:[IFlySpeechConstant TEXT_ENCODING]];
    //语法类型，本地是 bnf，在线识别是 abnf
    [recognizer setParameter:@"abnf" forKey:[IFlyResourceUtil GRAMMARTYPE]];
    //设置服务类型为 asr 识别
    [recognizer setParameter:@"asr" forKey:[IFlySpeechConstant IFLY_DOMAIN]];
    //调用构建语法接口
    [recognizer buildGrammarCompletionHandler:^(NSString *grammarId, IFlySpeechError *error) {
        if(![error errorCode])
        {
            NSLog(@"构建语法成功 grammarId=%@", grammarId);
            CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:grammarId];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }
        else
        {
            NSLog(@"构建语法失败 errorCode=%d", [error errorCode]);
            CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsInt:[error errorCode]];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }
    } grammarType:@"abnf" grammarContent:cloudGrammar];
    
    // 保持回调
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT];
    [pluginResult setKeepCallbackAsBool:TRUE];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

// 开始监听命令
- (void)startListeningGrammar:(CDVInvokedUrlCommand*)command
{
    NSString* grammerID = [command.arguments objectAtIndex:0];
    
    // 设置命令ID
    IFlySpeechRecognizer* recognizer = [IFlySpeechRecognizer sharedInstance];
    [recognizer setParameter:grammerID forKey:[IFlySpeechConstant CLOUD_GRAMMAR]];
    
    // 启动识别
    [recognizer startListening];
    
    // 保持回调
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT];
    [pluginResult setKeepCallbackAsBool:TRUE];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    
    // 保存回调
    mGrammarListeningCommandId = command.callbackId;
    
    NSLog(@"开始监听");
}

// 停止监听
- (void)stopListeningGrammar:(CDVInvokedUrlCommand*)command
{
    IFlySpeechRecognizer* recognizer = [IFlySpeechRecognizer sharedInstance];
    
    if(recognizer.isListening)
    {
        [recognizer stopListening];
        [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId:command.callbackId];
        
        NSLog(@"停止监听成功");
    }
    else
    {
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"当前无命令监听, 无法停止"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        
        NSLog(@"停止监听失败，因为当前无监听");
    }
}

// 取消监听
- (void)cancelGrammar:(CDVInvokedUrlCommand*)command
{
    IFlySpeechRecognizer* recognizer = [IFlySpeechRecognizer sharedInstance];
    
    if(recognizer.isListening)
    {
        [recognizer cancel];
        [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK] callbackId:command.callbackId];
        
        NSLog(@"取消监听成功");
    }
    else
    {
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"当前无命令监听, 无法取消"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        
        NSLog(@"取消监听失败，因为当前无监听");
    }
}

//--------------------------------------------------------------
//结果返回代理
- (void) onResults:(NSArray *) results isLast:(BOOL) isLast
{
    NSMutableDictionary* json = [[NSMutableDictionary alloc] init];
    [json setValue:@"onResult" forKey:@"action"];
    
    NSMutableDictionary* data = [[NSMutableDictionary alloc] init];
    [data setValue:results forKey:@"result"];
    [data setValue:isLast ? @(YES) : @(NO) forKey:@"isLast"];
    [json setValue:data forKey:@"data"];
    
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:json];
    [pluginResult setKeepCallbackAsBool:TRUE];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:mGrammarListeningCommandId];
}
//音量回调
- (void) onVolumeChanged: (int)volume
{
    NSMutableDictionary* json = [[NSMutableDictionary alloc] init];
    [json setValue:@"onVolumeChanged" forKey:@"action"];
    
    NSMutableDictionary* data = [[NSMutableDictionary alloc] init];
    [data setValue:[NSNumber numberWithInt:volume] forKey:@"volume"];
    [json setValue:data forKey:@"data"];
    
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:json];
    [pluginResult setKeepCallbackAsBool:TRUE];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:mGrammarListeningCommandId];
}
//录音开始回调
- (void) onBeginOfSpeech
{
    NSMutableDictionary* json = [[NSMutableDictionary alloc] init];
    [json setValue:@"onBeginOfSpeech" forKey:@"action"];
    
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:json];
    [pluginResult setKeepCallbackAsBool:TRUE];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:mGrammarListeningCommandId];
}
//录音结束回调
- (void) onEndOfSpeech
{
    NSMutableDictionary* json = [[NSMutableDictionary alloc] init];
    [json setValue:@"onEndOfSpeech" forKey:@"action"];
    
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:json];
    [pluginResult setKeepCallbackAsBool:TRUE];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:mGrammarListeningCommandId];
}
//会话错误结束回调
- (void) onError:(IFlySpeechError*) error
{
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsInt:[error errorCode]];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:mGrammarListeningCommandId];
}
//会话取消回调
- (void) onCancel{}
    
@end
